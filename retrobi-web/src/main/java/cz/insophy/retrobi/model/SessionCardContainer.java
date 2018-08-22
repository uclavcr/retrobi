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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Cardset;
import cz.insophy.retrobi.database.entity.type.Direction;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * Card container with a list of card IDs for each guest session.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SessionCardContainer implements Serializable {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SessionCardContainer.class);
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * list of IDs of cards in the basket
     */
    private final List<String> basketCardIds;
    /**
     * the last card batch modification result
     */
    private final BatchCardModificationResult batchModificationResult;
    
    /**
     * Creates a new instance.
     */
    public SessionCardContainer() {
        this.basketCardIds = new LinkedList<String>();
        this.batchModificationResult = new BatchCardModificationResult();
    }
    
    // ====================
    // BASKET RELATED STUFF
    // ====================
    
    /**
     * Returns a list of all cards in the basket (no range is used). This method
     * can be pretty exhausting if there is too much cards in the batch.
     * 
     * @return list of cards
     * @throws GeneralRepositoryException
     * general exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<Card> getAllBasketCards() throws NotFoundRepositoryException, GeneralRepositoryException {
        synchronized (this.basketCardIds) {
            return RetrobiApplication.db().getCardRepository().getCards(this.basketCardIds);
        }
    }
    
    /**
     * Returns the basket size.
     * 
     * @return the basket size
     */
    public int getBasketSize() {
        synchronized (this.basketCardIds) {
            return this.basketCardIds.size();
        }
    }
    
    /**
     * Checks if the basket is empty.
     * 
     * @return <code>true</code> when the basket is empty, <code>false</code>
     * otherwise
     */
    public boolean isBasketEmpty() {
        if (this.getBasketSize() > 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the basket is full.
     * 
     * @param limit
     * user limit
     * @return <code>true</code> if the basket is full, <code>false</code>
     * otherwise
     */
    public boolean isBasketFull(final int limit) {
        if (limit < 0) {
            return false;
        }
        
        if (this.getBasketSize() >= limit) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns the card ID list for the basket.
     * 
     * @return card ID list
     */
    public List<String> getBasketCardIds() {
        synchronized (this.basketCardIds) {
            return Collections.unmodifiableList(this.basketCardIds);
        }
    }
    
    /**
     * Checks if the given card is already in the basket.
     * 
     * @param cardId
     * card ID
     * @return <code>true</code> if the card is already in the basket,
     * <code>false</code> otherwise
     */
    public boolean isInBasket(final String cardId) {
        synchronized (this.basketCardIds) {
            return this.basketCardIds.contains(cardId);
        }
    }
    
    /**
     * Clears the whole basket.
     */
    public void clearBasket() {
        SessionCardContainer.LOG.debug("Clearing basket...");
        
        synchronized (this.basketCardIds) {
            this.basketCardIds.clear();
        }
    }
    
    /**
     * Adds a list of card IDs into the basket.
     * 
     * @param cardIds
     * card IDs to be added
     * @param limit
     * basket size limit (or -1 if there is none)
     * @throws OverLimitException
     * an exception thrown when the limit is exceeded
     */
    public void addToBasket(final Collection<String> cardIds, final int limit) throws OverLimitException {
        SessionCardContainer.LOG.debug(String.format("Adding %d card(s) to basket...", cardIds.size()));
        
        // PREPARE NEW CARD SET
        // --------------------
        
        final Set<String> newBasketCardIds;
        
        synchronized (this.basketCardIds) {
            // copy the existing cards
            
            newBasketCardIds = new LinkedHashSet<String>(this.basketCardIds);
        }
        
        // add the new cards (no duplicates stay)
        
        newBasketCardIds.addAll(cardIds);
        
        // REPLACE THE ORIGINAL CARD SET
        // -----------------------------
        
        synchronized (this.basketCardIds) {
            this.basketCardIds.clear();
            
            for (final String newBasketCardId : newBasketCardIds) {
                this.unsafeAddToBasket(limit, newBasketCardId);
            }
        }
    }
    
    /**
     * Adds a single card ID into the basket.
     * 
     * @param cardId
     * card ID to be added
     * @param limit
     * basket size limit (or -1 if there is none)
     * @throws OverLimitException
     * an exception thrown when the limit is exceeded
     */
    public void addToBasket(final String cardId, final int limit) throws OverLimitException {
        synchronized (this.basketCardIds) {
            if (!this.basketCardIds.contains(cardId)) {
                this.unsafeAddToBasket(limit, cardId);
            }
        }
    }
    
    /**
     * Adds a single card ID into the basket. NOTE: Does not synchronize access
     * to the <code>basketCardIds</code> collection!!! Use it very carefully.
     * 
     * @param limit
     * basket size limit (or -1 if there is none)
     * @param cardId
     * card ID to be added
     * @throws OverLimitException
     * an exception thrown when the limit is exceeded
     */
    private void unsafeAddToBasket(final int limit, final String cardId) throws OverLimitException {
        if ((limit < 0) || (this.basketCardIds.size() < limit)) {
            this.basketCardIds.add(cardId);
        } else {
            throw new OverLimitException(limit);
        }
    }
    
    /**
     * Removes a single card ID from the basket.
     * 
     * @param cardId
     * card ID to be removed
     */
    public void removeFromBasket(final String cardId) {
        synchronized (this.basketCardIds) {
            this.basketCardIds.remove(cardId);
        }
    }
    
    /**
     * Removes a list of card IDs from the basket.
     * 
     * @param cardIds
     * card IDs to be removed
     */
    public void removeFromBasket(final Collection<String> cardIds) {
        SessionCardContainer.LOG.debug(String.format("Removing %d card(s) from basket...", cardIds.size()));
        
        synchronized (this.basketCardIds) {
            this.basketCardIds.removeAll(cardIds);
        }
    }
    
    /**
     * Checks if the card can be moved in the list.
     * 
     * @param cardId
     * card ID
     * @param direction
     * direction to move
     * @return <code>true</code> if the card can be moved, <code>false</code>
     * otherwise
     */
    public boolean canMoveInBasket(final String cardId, final Direction direction) {
        synchronized (this.basketCardIds) {
            if (this.basketCardIds.size() < 1) {
                // the list is empty, no moving possible
                
                return false;
            }
            
            if (!this.basketCardIds.contains(cardId)) {
                // card ID is not in the list, no moving possible
                
                return false;
            }
            
            // get the card index
            
            final int index = this.basketCardIds.indexOf(cardId);
            
            // check the card index with bounds
            
            if ((index == 0) && (direction == Direction.UP)) {
                // first card + up = NOP
                
                return false;
            } else if ((index == this.basketCardIds.size() - 1) && (direction == Direction.DOWN)) {
                // last card + down = NOP
                
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * Moves the card in the list.
     * 
     * @param cardId
     * card ID
     * @param direction
     * direction to move
     */
    public void moveInBasket(final String cardId, final Direction direction) {
        if (!this.canMoveInBasket(cardId, direction)) {
            // cannot move, do nothing
            
            return;
        }
        
        synchronized (this.basketCardIds) {
            // get card indices
            
            final int oldIndex = this.basketCardIds.indexOf(cardId);
            final int newIndex = (direction.equals(Direction.DOWN) ? oldIndex + 1 : oldIndex - 1);
            
            // move the card
            
            SessionCardContainer.LOG.debug(String.format("Moving card in basket from %d to %d...", oldIndex, newIndex));
            Collections.swap(this.basketCardIds, oldIndex, newIndex);
            SessionCardContainer.LOG.debug("Card was moved.");
        }
    }
    
    /**
     * Moves the cards in the list.
     * 
     * @param movingCardIds
     * IDs of cards to be moved
     * @param pivotCardId
     * pivot card ID
     * @param direction
     * direction to move (up = before, down = after)
     */
    public void moveInBasket(final List<String> movingCardIds, final String pivotCardId, final Direction direction) {
        synchronized (this.basketCardIds) {
            // get the result with the cards moved
            
            final Collection<String> result = SimpleGeneralUtils.moveItems(
                    this.basketCardIds,
                    movingCardIds,
                    pivotCardId,
                    direction.equals(Direction.DOWN));
            
            // update the basket contents
            
            this.basketCardIds.clear();
            this.basketCardIds.addAll(result);
        }
    }
    
    /**
     * Sorts the cards in the basket as they are in the catalog.
     * 
     * @throws GeneralRepositoryException
     * general exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public void sortBasket() throws NotFoundRepositoryException, GeneralRepositoryException {
        // collect current basket cards into the new list
        
        final List<Card> temp = new ArrayList<Card>(this.getAllBasketCards());
        
        // get the comparator
        
        final Comparator<String> comparator = CardCatalogModel.getInstance();
        
        // sort the cards
        
        Collections.sort(temp, new Comparator<Card>() {
            @Override
            public int compare(final Card o1, final Card o2) {
                int c = o1.getCatalog().compareTo(o2.getCatalog());
                
                if (c == 0) {
                    c = comparator.compare(o1.getBatchForSort(), o2.getBatchForSort());
                }
                
                if (c == 0) {
                    c = Integer.valueOf(o1.getNumberInBatch()).compareTo(Integer.valueOf(o2.getNumberInBatch()));
                }
                
                return c;
            }
        });
        
        // put the sorted cards back into the basket
        
        synchronized (this.basketCardIds) {
            this.basketCardIds.clear();
            
            for (final Card card : temp) {
                this.basketCardIds.add(card.getId());
            }
        }
    }
    
    /**
     * Writes the basket contents to the output stream as plain text. The writer
     * is not closed after writing (it is flushed only).<br>
     * Format:
     * 
     * <pre>
     * CARD ID CR LF CARD ID CR LF CARD ID CR LF ...
     * </pre>
     * 
     * @param out
     * output stream to write into
     * @throws IOException
     * I/O exception
     */
    public void exportBasketToStream(final OutputStream out) throws IOException {
        SessionCardContainer.LOG.debug("Writing basket to stream...");
        
        final Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        
        for (final String cardId : this.getBasketCardIds()) {
            writer.append(String.format("%s\r\n", cardId));
        }
        
        writer.flush();
        
        SessionCardContainer.LOG.debug("Writing done.");
    }
    
    /**
     * Reads the basket contents from the input stream.<br>
     * Format:
     * 
     * <pre>
     * CARD ID CR LF CARD ID CR LF CARD ID CR LF ...
     * </pre>
     * 
     * @param in
     * input stream to read from
     * @param limit
     * basket size limit (or -1 if there is none)
     * @throws IOException
     * I/O exception
     * @throws OverLimitException
     * an exception thrown when the limit is exceeded
     */
    public void importBasketFromStream(final InputStream in, final int limit) throws IOException, OverLimitException {
        final List<String> validCardIds = new LinkedList<String>();
        
        // read card IDs from the stream and add to the basket
        
        SessionCardContainer.LOG.debug("Reading basket from stream...");
        
        final Scanner scanner = new Scanner(in);
        
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine().trim();
            
            if (line.matches("^[a-f0-9]{32}$")) {
                if (RetrobiApplication.db().getCardRepository().cardExists(line)) {
                    validCardIds.add(line);
                }
            }
        }
        
        scanner.close();
        
        SessionCardContainer.LOG.debug("Reading done. Now adding to basket...");
        
        // add read cards to the basket (valid only)
        
        this.clearBasket();
        this.addToBasket(validCardIds, limit);
        
        SessionCardContainer.LOG.debug("Basket import done.");
    }
    
    /**
     * Reads the basket content from a cardset.
     * 
     * @param cardset
     * cardset to load
     * @param limit
     * basket size limit (or -1 if there is none)
     * @throws OverLimitException
     * an exception thrown when the limit is exceeded
     */
    public void importBasketFromCardset(final Cardset cardset, final int limit) throws OverLimitException {
        SessionCardContainer.LOG.debug("Importing to basket from a cardset...");
        
        // validate all card IDs for existence
        
        final List<String> validCardIds = new LinkedList<String>();
        
        for (final String cardsetCardId : cardset.getCardIds()) {
            if (RetrobiApplication.db().getCardRepository().cardExists(cardsetCardId)) {
                validCardIds.add(cardsetCardId);
            }
        }
        
        // add read cards to the basket (valid only)
        
        this.clearBasket();
        this.addToBasket(validCardIds, limit);
        
        SessionCardContainer.LOG.debug("Basket import done.");
    }
    
    // ========================
    // BATCH MODIFICATION STUFF
    // ========================
    
    /**
     * Returns the batch card modification result.
     * 
     * @return the modification result
     */
    public BatchCardModificationResult getBatchModificationResult() {
        return this.batchModificationResult;
    }
    
    // ===============
    // UTILITY METHODS
    // ===============
    
    /**
     * Clears all the card ID lists.
     */
    public void clearAll() {
        this.clearBasket();
        this.batchModificationResult.clear();
    }
}
