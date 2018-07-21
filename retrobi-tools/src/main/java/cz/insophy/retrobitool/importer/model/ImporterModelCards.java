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

package cz.insophy.retrobitool.importer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.utils.CzechAlphabet;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;
import cz.insophy.retrobitool.ImporterFileMetaInfo;

/**
 * Helper class that contains cards made from file list.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImporterModelCards {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImporterModelCards.class);
    /**
     * list of cards
     */
    private final List<Card> cards;
    /**
     * list of card files
     */
    private final Map<Card, List<ImporterFileMetaInfo>> files;
    
    /**
     * Creates a new instance.
     */
    public ImporterModelCards() {
        this.cards = new ArrayList<Card>(500);
        this.files = new HashMap<Card, List<ImporterFileMetaInfo>>();
    }
    
    /**
     * Clears all cached instances. It leaves properties as they are.
     */
    protected void clear() {
        this.cards.clear();
        this.files.clear();
    }
    
    /**
     * Returns a list of loaded cards.
     * 
     * @return a list of loaded cards
     */
    protected List<Card> getLoadedCards() {
        return Collections.unmodifiableList(this.cards);
    }
    
    /**
     * Returns a list of files for the specified card. Each card should have at
     * least one file. If not so, an exception will be thrown.
     * 
     * @param card
     * card
     * @return list of files for the given card
     */
    protected List<ImporterFileMetaInfo> getFiles(final Card card) {
        if (!this.files.containsKey(card)) {
            throw new IllegalStateException("Každý lístek musí mít alespoň jeden soubor.");
        }
        
        return Collections.unmodifiableList(this.files.get(card));
    }
    
    /**
     * Checks whether the card list is empty.
     * 
     * @return <code>true</code> if the card list is empty, <code>false</code>
     * otherwise
     */
    protected boolean isEmpty() {
        return this.cards.isEmpty();
    }
    
    /**
     * Updates the card list based on the actual list of files taken from the
     * provided model. Each card is assigned date added and updated only.
     * 
     * @param fileModel
     * file model with actual files
     */
    protected void createFromFiles(final ImporterModelFiles fileModel) {
        // clear old data
        
        ImporterModelCards.LOG.debug("Clearing old data...");
        
        this.cards.clear();
        this.files.clear();
        
        // walk every file meta information
        // group files with the same batch and order together
        // file meta information is required to be ordered by batches and order
        
        ImporterModelCards.LOG.debug("Creating cards...");
        
        String lastBatch = null;
        Integer lastOrder = null;
        final List<ImporterFileMetaInfo> buffer = new LinkedList<ImporterFileMetaInfo>();
        
        for (final ImporterFileMetaInfo info : fileModel.getLoadedFilesInfo()) {
            if ((lastBatch == null) || (lastOrder == null) || !info.getBatch().equals(lastBatch) || !lastOrder.equals(info.getNumber())) {
                // ------------------
                // FILE OF A NEW CARD
                // ------------------
                
                // a new card will be started
                // first end the old one (if any)
                
                if ((lastBatch != null) && (lastOrder != null)) {
                    // create and add a new card
                    
                    this.finalizeCard(buffer);
                }
                
                // start new card
                // reset counters
                
                lastBatch = info.getBatch();
                lastOrder = info.getNumber();
                buffer.clear();
                buffer.add(info);
                ImporterModelCards.LOG.debug("Added first file: " + info.toString());
            } else {
                // -------------------
                // FILE OF AN OLD CARD
                // -------------------
                
                // the same card
                // just add the file in the buffer and continue
                
                buffer.add(info);
                ImporterModelCards.LOG.debug("Added next file: " + info.toString());
            }
        }
        
        // the last card must be finalized here too
        
        this.finalizeCard(buffer);
    }
    
    /**
     * Creates a new card, assigns its files and adds it into the model.
     * 
     * @param cardFiles
     * card file list (card images), cannot be empty
     */
    private void finalizeCard(final List<ImporterFileMetaInfo> cardFiles) {
        ImporterModelCards.LOG.debug("Creating new card with files: " + cardFiles.toString() + "...");
        
        if (cardFiles.isEmpty()) {
            throw new IllegalArgumentException("Seznam souborů u lístku nesmí být prázdný.");
        }
        
        // check continuity
        
        int expectedPage = 1;
        
        for (final ImporterFileMetaInfo cardFile : cardFiles) {
            if (cardFile.getPage() != expectedPage) {
                throw new IllegalArgumentException(String.format(
                        "Nesouvislá řada obrázků u souboru %s (očekávaná strana = %d).",
                        cardFile.getFile().getName(),
                        expectedPage));
            }
            
            expectedPage++;
        }
        
        // create a new card
        
        final Card card = new Card();
        
        card.setAdded(Time.now());
        card.setUpdated(Time.now());
        
        // put card and add its files into the model
        // files must by COPIED from the buffer to the map
        // (to avoid invalid references to the buffer instances)
        
        this.cards.add(card);
        this.files.put(card, new LinkedList<ImporterFileMetaInfo>(cardFiles));
    }
    
    /**
     * Updates properties of all loaded cards. Before using this method, ensure
     * that card and their files are properly set. The method assumes that each
     * card has at least one file.
     */
    protected void updateProperties() {
        ImporterModelCards.LOG.debug("Updating card properties...");
        
        for (final Card card : this.cards) {
            if (!this.files.containsKey(card)) {
                throw new NoSuchElementException();
            }
            
            card.clearProperties();
            
            // get ANY file from the list
            // the point is to extract information common for all card files
            // (we choose the first one here to be deterministic)
            
            final ImporterFileMetaInfo info = this.files.get(card).get(0);
            
            // time updated
            
            card.setUpdated(Time.now());
            
            // state
            
            card.setState(CardState.FRESH);
            
            // drawer
            
            if (info.getDrawer() != null) {
                card.setDrawer(info.getDrawer());
            } else {
                card.setDrawer(null);
            }
            
            // catalog position
            
            card.setCatalog(info.getCatalog());
            card.setBatch(info.getBatch());
            card.setNumberInBatch(info.getNumber());
            
            // batch for sort
            
            card.setBatchForSort(CzechAlphabet.getDefaultBatchForSort(info.getBatch()));
            
            // files
            
            card.clearFiles();
            
            for (final ImporterFileMetaInfo file : this.files.get(card)) {
                card.addFile(file.getFile().getName());
            }
            
            // OCR (fixed special characters)
            
            final StringBuilder builder = new StringBuilder();
            boolean anyOcrFile = false;
            
            for (final ImporterFileMetaInfo file : this.files.get(card)) {
                builder.append(file.getOcr());
                anyOcrFile = true;
            }
            
            final String ocr = SimpleStringUtils.fixSpecialChars(builder.toString().trim());
            
            if (ocr.length() > 0) {
                // regular OCR
                card.setOcr(ocr);
            } else {
                // no OCR
                if (anyOcrFile) {
                    // probably a hand-written card
                    card.setOcr("$bez_ocr$");
                }
            }
        }
    }
}
