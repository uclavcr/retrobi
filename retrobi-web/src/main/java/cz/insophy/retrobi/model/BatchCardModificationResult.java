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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.longtask.CardModification;

/**
 * Batch card modification result container.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BatchCardModificationResult implements Serializable {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(BatchCardModificationResult.class);
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * the last modification done
     */
    private CardModification lastModification;
    /**
     * list of changed cards
     */
    private final List<Card> changedCards;
    /**
     * list of unchanged cards
     */
    private final List<Card> unchangedCards;
    /**
     * number of cards skipped (not modified at all)
     */
    private int skippedCards;
    
    /**
     * Creates a new instance.
     */
    public BatchCardModificationResult() {
        this.lastModification = null;
        this.changedCards = new LinkedList<Card>();
        this.unchangedCards = new LinkedList<Card>();
        this.skippedCards = 0;
    }
    
    /**
     * Clears the result.
     */
    public void clear() {
        synchronized (this.changedCards) {
            this.lastModification = null;
            this.changedCards.clear();
        }
        
        synchronized (this.unchangedCards) {
            this.unchangedCards.clear();
            this.skippedCards = 0;
        }
    }
    
    /**
     * Modifies the given card. If the modification is successful, it is added
     * to the <code>changed</code> set. If not, the card is added to the
     * <code>unchanged</code> set.
     * 
     * @param cardToEdit
     * card to edit
     * @param modification
     * card modification
     */
    public void modify(final Card cardToEdit, final CardModification modification) {
        BatchCardModificationResult.LOG.debug(String.format("Modifying '%s' by '%s'...", cardToEdit.toString(), modification.getTitle()));
        
        try {
            if (modification.modify(cardToEdit)) {
                // card done successfully
                
                synchronized (this.changedCards) {
                    BatchCardModificationResult.LOG.debug("OK: " + cardToEdit.toString() + " (ID = " + cardToEdit.getId() + ")");
                    this.changedCards.add(cardToEdit);
                    this.lastModification = modification;
                }
            } else {
                // card failed
                
                synchronized (this.unchangedCards) {
                    BatchCardModificationResult.LOG.debug("Failed: " + cardToEdit.toString() + " (ID = " + cardToEdit.getId() + ")");
                    this.unchangedCards.add(cardToEdit);
                }
            }
        } catch (final AlreadyModifiedException x) {
            // modification not needed, skip the card
            
            BatchCardModificationResult.LOG.debug("Modification is not needed, skipping the card.");
            
            synchronized (this.unchangedCards) {
                this.skippedCards++;
            }
        }
    }
    
    /**
     * Returns the last card modification title.
     * 
     * @return card modification title
     */
    public CardModification getLastModification() {
        synchronized (this.changedCards) {
            return this.lastModification;
        }
    }
    
    /**
     * Returns the count of changed cards.
     * 
     * @return the changed card count
     */
    public int getChangedCardsCount() {
        synchronized (this.changedCards) {
            return this.changedCards.size();
        }
    }
    
    /**
     * Returns the count of unchanged cards.
     * 
     * @return the unchanged card count
     */
    public int getUnchangedCardsCount() {
        synchronized (this.unchangedCards) {
            return this.unchangedCards.size();
        }
    }
    
    /**
     * Returns the count of skipped cards.
     * 
     * @return the skipped card count
     */
    public int getSkippedCardsCount() {
        synchronized (this.unchangedCards) {
            return this.skippedCards;
        }
    }
    
    /**
     * Returns the list of changed cards.
     * 
     * @return the list of changed cards
     */
    public Collection<Card> getChangedCards() {
        synchronized (this.changedCards) {
            return Collections.unmodifiableList(this.changedCards);
        }
    }
    
    /**
     * Retains only the given cards in the changed set.
     * 
     * @param cardsToRetain
     * cards to be retained in the changed set
     */
    public void retainChangedCards(final List<Card> cardsToRetain) {
        synchronized (this.changedCards) {
            this.changedCards.retainAll(cardsToRetain);
        }
    }
    
    /**
     * Removes all changed cards from the basket.
     * 
     * @param basket
     * container to remove cards from
     */
    public void removeChangedFromBasket(final SessionCardContainer basket) {
        synchronized (this.changedCards) {
            for (final Card card : this.changedCards) {
                basket.removeFromBasket(card.getId());
            }
        }
    }
    
    /**
     * Removes all unchanged cards from the basket.
     * 
     * @param basket
     * basket container to remove cards from
     */
    public void removeUnchangedFromBasket(final SessionCardContainer basket) {
        synchronized (this.unchangedCards) {
            for (final Card card : this.unchangedCards) {
                basket.removeFromBasket(card.getId());
            }
        }
    }
    
    /**
     * Adds all changed cards to the basket (existing contents will be removed).
     * 
     * @param basket
     * target container
     * @param basketLimit
     * basket size limit
     * @throws OverLimitException
     * over limit exception
     */
    public void putChangedToBasket(final SessionCardContainer basket, final int basketLimit) throws OverLimitException {
        basket.clearBasket();
        
        synchronized (this.changedCards) {
            for (final Card card : this.changedCards) {
                basket.addToBasket(card.getId(), basketLimit);
            }
        }
    }
    
    /**
     * Adds all unchanged cards to the basket (existing contents will be
     * removed).
     * 
     * @param basket
     * target container
     * @param basketLimit
     * basket size limit
     * @throws OverLimitException
     * over limit exception
     */
    public void putUnchangedToBasket(final SessionCardContainer basket, final int basketLimit) throws OverLimitException {
        basket.clearBasket();
        
        synchronized (this.unchangedCards) {
            for (final Card card : this.unchangedCards) {
                basket.addToBasket(card.getId(), basketLimit);
            }
        }
    }
    
    /**
     * Checks if the modification result is empty, which means that all changes
     * were confirmed, deleted or saved. Dirty state is a negation of the result
     * of this method.
     * 
     * @return <code>true</code> if the result is empty (changed and unchanged
     * card collections are empty), <code>false</code> otherwise
     */
    public boolean isEmpty() {
        synchronized (this.changedCards) {
            if (!this.changedCards.isEmpty()) {
                return false;
            }
        }
        
        synchronized (this.unchangedCards) {
            if (!this.unchangedCards.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
}
