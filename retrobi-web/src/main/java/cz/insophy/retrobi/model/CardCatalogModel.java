/*
 * Copyright 2012 UCL AV CR v.v.i.
 *
 * This file is part of Retrobi.
 *
 * Retrobi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Retrobi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Retrobi. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.insophy.retrobi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiLocker;
import cz.insophy.retrobi.database.entity.type.BatchMetainfo;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.Direction;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.CzechAlphabet;
import cz.insophy.retrobi.utils.Triple;
import cz.insophy.retrobi.utils.Tuple;

/**
 * Cached catalog model. Contains a list of non-empty catalogs, batches and
 * letters. All these data are cached in a memory and may be quite big
 * (thousands of items). Therefore, the client should update only when needed,
 * as the cost of the update process is big.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardCatalogModel implements Serializable, Comparator<String> {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(CardCatalogModel.class);
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * singleton instance
     */
    private static CardCatalogModel singleton = null;
    /**
     * list of used letters by catalogs
     */
    private Map<Catalog, List<String>> usedLetters;
    /**
     * index map containing sorted list of all batches for each catalog
     */
    private Map<Catalog, List<String>> batchCache;
    /**
     * list of batches, where numbering starts not with card #1
     */
    private List<Triple<Catalog, String, Integer>> invalidFirstNumberBatchCache;
    /**
     * list of batches, where numbering is not continuous
     */
    private List<Tuple<Catalog, String>> invalidNumberingBatchCache;
    
    /**
     * Returns the singleton instance of this class.
     * 
     * @return the singleton instance of this class
     */
    public synchronized static CardCatalogModel getInstance() {
        if (CardCatalogModel.singleton == null) {
            CardCatalogModel.singleton = new CardCatalogModel();
        }
        
        return CardCatalogModel.singleton;
    }
    
    /**
     * Creates a new instance.
     */
    private CardCatalogModel() {
        this.usedLetters = new LinkedHashMap<Catalog, List<String>>();
        this.batchCache = new LinkedHashMap<Catalog, List<String>>();
        this.invalidFirstNumberBatchCache = new LinkedList<Triple<Catalog, String, Integer>>();
        this.invalidNumberingBatchCache = new LinkedList<Tuple<Catalog, String>>();
    }
    
    @Override
    public int compare(final String o1, final String o2) {
        return CzechAlphabet.getInstance().compare(o1, o2);
    }
    
    /**
     * Checks if the update is in progress right now.
     * 
     * @return <code>true</code> if the update is in progress,
     * <code>false</code> otherwise
     */
    public boolean isUpdating() {
        return false;
    }
    
    /**
     * Returns the current update status.
     * 
     * @return the current update status
     */
    public String getUpdateStatus() {
        return "Dokončeno";
    }
    
    /**
     * Returns the properly sorted list of catalogs.
     * 
     * @return list of catalogs
     */
    public List<Catalog> getCatalogs() {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            final List<Catalog> list = new LinkedList<Catalog>(this.usedLetters.keySet());
            Collections.sort(list);
            return Collections.unmodifiableList(list);
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
    }
    
    /**
     * Checks if a letter is used in the given catalog.
     * 
     * @param catalog
     * catalog
     * @param letter
     * letter
     * @return <code>true</code> if the letter is used within the given catalog,
     * <code>false</code> otherwise
     */
    public boolean isLetterUsed(final Catalog catalog, final String letter) {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            if (!this.usedLetters.containsKey(catalog)) {
                return false;
            }
            
            return this.usedLetters.get(catalog).contains(letter);
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
    }
    
    /**
     * Returns the list of all letters.
     * 
     * @return list of all letters
     */
    public List<String> getLetters() {
        return CzechAlphabet.getInstance().getAlphabet();
    }
    
    /**
     * Returns the previous letter of the given letter or <code>null</code>, if
     * there is no such letter.
     * 
     * @param letter
     * a letter
     * @return previous letter or <code>null</code>
     */
    public String getPreviousLetter(final String letter) {
        return CzechAlphabet.getInstance().getAlphabetPreviousLetter(letter);
    }
    
    /**
     * Returns the next letter of the given letter or <code>null</code>, if
     * there is no such letter.
     * 
     * @param letter
     * a letter
     * @return next letter or <code>null</code>
     */
    public String getNextLetter(final String letter) {
        return CzechAlphabet.getInstance().getAlphabetNextLetter(letter);
    }
    
    /**
     * Returns the letter for the given batch. If the mapping is not found,
     * returns the fallback letter (usually some kind of a special letter).
     * 
     * @param batch
     * the batch to find the letter of
     * @return the batch letter or a fallback letter defined in the alphabet
     */
    public String getLetterOfBatch(final String batch) {
        return CzechAlphabet.getInstance().getAlphabetFirstLetter(batch);
    }
    
    /**
     * Returns the batch count for all catalogs.
     * 
     * @return the batch count
     */
    public int getBatchCount() {
        int count = 0;
        
        for (final Catalog catalog : Catalog.values()) {
            count += this.getBatchCount(catalog);
        }
        
        return count;
    }
    
    /**
     * Returns the batch count for one given catalog.
     * 
     * @param catalog
     * a catalog
     * @return the batch count
     */
    public int getBatchCount(final Catalog catalog) {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            if (!this.batchCache.containsKey(catalog)) {
                CardCatalogModel.LOG.debug("No batches for catalog: " + catalog.name());
                return 0;
            }
            
            return this.batchCache.get(catalog).size();
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
    }
    
    /**
     * Returns a batch list for the given catalog. The list of batches is sorted
     * (sorting order of the cache is preserved).
     * 
     * @param catalog
     * catalog
     * @return sorted list of batches
     */
    public List<String> getBatches(final Catalog catalog) {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            if (!this.batchCache.containsKey(catalog)) {
                CardCatalogModel.LOG.debug("No batches for catalog: " + catalog.name());
                return Collections.emptyList();
            }
            
            return Collections.unmodifiableList(this.batchCache.get(catalog));
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
    }
    
    /**
     * Returns a batch list for the given letter and catalog. The list of
     * batches is sorted (sorting order of the cache is preserved).
     * 
     * @param catalog
     * catalog
     * @param letter
     * letter
     * @return sorted list of batches
     */
    public List<String> getBatches(final Catalog catalog, final String letter) {
        final List<String> selectedBatches = new LinkedList<String>();
        
        for (final String batch : this.getBatches(catalog)) {
            if (CzechAlphabet.getInstance().belongsToLetter(batch, letter)) {
                selectedBatches.add(batch);
            }
        }
        
        return Collections.unmodifiableList(selectedBatches);
    }
    
    /**
     * Returns the previous batch, or <code>null</code> if there is none.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch
     * @return the previous batch or <code>null</code>
     */
    public String getPreviousBatch(final Catalog catalog, final String batch) {
        return this.getNeighborBatch(catalog, batch, Direction.UP);
    }
    
    /**
     * Returns the next batch, or <code>null</code> if there is none.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch
     * @return the next batch or <code>null</code>
     */
    public String getNextBatch(final Catalog catalog, final String batch) {
        return this.getNeighborBatch(catalog, batch, Direction.DOWN);
    }
    
    /**
     * Utility method for getting the neighbor batch (the closes batch on each
     * side). If there is no such batch, returns <code>null</code>.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch
     * @param dir
     * direction (up = previous, down = next)
     * @return the neighbor batch by direction or <code>null</code>
     */
    private String getNeighborBatch(final Catalog catalog, final String batch, final Direction dir) {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            // get batches
            
            final List<String> tempBatches = this.getBatches(catalog);
            
            // locate batch in the list
            
            final int batchIndex = tempBatches.indexOf(batch);
            
            if (batchIndex != -1) {
                if (dir.equals(Direction.UP)) {
                    // --------
                    // PREVIOUS
                    // --------
                    
                    if (batchIndex > 0) {
                        return tempBatches.get(batchIndex - 1);
                    }
                } else if (dir.equals(Direction.DOWN)) {
                    // ----
                    // NEXT
                    // ----
                    
                    if (batchIndex < tempBatches.size() - 1) {
                        return tempBatches.get(batchIndex + 1);
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
        
        return null;
    }
    
    /**
     * Returns a list of batches that do not start with the first card. The
     * order of batches in the cache is preserved.
     * 
     * @return list of tuple (catalog, batch, first card number) for each
     * invalid batch
     */
    public List<Triple<Catalog, String, Integer>> getInvalidFirstNumberBatches() {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            return Collections.unmodifiableList(this.invalidFirstNumberBatchCache);
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
    }
    
    /**
     * Returns a list of batches that contain invalid card numbering - a card
     * number sequence is not continuous and contains gaps or doubled numbers.
     * Valid numbering is 1, 2, 3, ... , N, where N is the batch card count.
     * 
     * @return list of tuple (catalog, batch) for each invalid batch
     */
    public List<Tuple<Catalog, String>> getInvalidNumberingBatches() {
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            return Collections.unmodifiableList(this.invalidNumberingBatchCache);
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
    }
    
    /**
     * Updates the catalog cache state from the database.
     * 
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void update() throws GeneralRepositoryException {
        this.fillCache(this.loadFresh());
    }
    
    /**
     * Loads the fresh structure of the catalog.
     * 
     * @return the structure of the catalog (list of (catalog, batch for sort,
     * batch))
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private List<BatchMetainfo> loadFresh() throws GeneralRepositoryException {
        return RetrobiApplication.db().getCardRepository().getSortedBatches();
    }
    
    /**
     * Fills the cache with a fresh structure.
     * 
     * @param data
     * fresh structure of the catalog
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private void fillCache(final List<BatchMetainfo> data) throws GeneralRepositoryException {
        final Map<Catalog, List<String>> tempUsedLetters = new LinkedHashMap<Catalog, List<String>>();
        final Map<Catalog, List<String>> tempBatchCache = new LinkedHashMap<Catalog, List<String>>();
        final List<Triple<Catalog, String, Integer>> tempInvalidFirstNumberBatchCache = new LinkedList<Triple<Catalog, String, Integer>>();
        final List<Tuple<Catalog, String>> tempInvalidNumberingBatchCache = new LinkedList<Tuple<Catalog, String>>();
        
        // ----------
        // FILL CACHE
        // ----------
        
        CardCatalogModel.LOG.info(String.format("Filling catalog cache with %d row(s)...", data.size()));
        
        // group batches by catalog and put them into the map
        
        for (final BatchMetainfo batch : data) {
            // process letter
            
            final String letter = CzechAlphabet.getInstance().getAlphabetFirstLetter(batch.getName());
            
            final List<String> letters = CardCatalogModel.getSafeList(tempUsedLetters, batch.getCatalog(), 32);
            
            if (!letters.contains(letter)) {
                letters.add(letter);
            }
            
            // process batch
            
            CardCatalogModel.getSafeList(tempBatchCache, batch.getCatalog(), 2048).add(batch.getName());
            
            // process integrity check
            
            if (batch.getFirstCardNumber() != 1) {
                tempInvalidFirstNumberBatchCache.add(Triple.of(batch.getCatalog(), batch.getName(), batch.getFirstCardNumber()));
            }
            
            if (!batch.isContinuous()) {
                tempInvalidNumberingBatchCache.add(Tuple.of(batch.getCatalog(), batch.getName()));
            }
        }
        
        // ------------
        // UPDATE CACHE
        // ------------
        
        CardCatalogModel.LOG.info("Replacing old cached collections...");
        
        RetrobiLocker.CATALOG_MODEL_LOCK.lock();
        
        try {
            this.usedLetters = tempUsedLetters;
            this.batchCache = tempBatchCache;
            this.invalidFirstNumberBatchCache = tempInvalidFirstNumberBatchCache;
            this.invalidNumberingBatchCache = tempInvalidNumberingBatchCache;
        } finally {
            RetrobiLocker.CATALOG_MODEL_LOCK.unlock();
        }
        
        CardCatalogModel.LOG.info("Catalog cache updated.");
    }
    
    /**
     * Returns a list from the given map. If the map does not contain a list for
     * the given key, a new one is created with the default size provided.
     * 
     * @param <E>
     * list item type
     * @param <K>
     * map key type
     * @param map
     * source map
     * @param catalog
     * catalog
     * @param defaultSize
     * default list size
     * @return a list for the given key
     */
    private static <E, K> List<E> getSafeList(final Map<K, List<E>> map, final K catalog, final int defaultSize) {
        if (!map.containsKey(catalog)) {
            final List<E> newList = new ArrayList<E>(defaultSize);
            map.put(catalog, newList);
            return newList;
        }
        
        return map.get(catalog);
    }
}
