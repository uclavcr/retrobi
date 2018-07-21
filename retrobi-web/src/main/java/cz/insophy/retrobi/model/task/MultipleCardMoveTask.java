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

package cz.insophy.retrobi.model.task;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * A long task for moving multiple cards from multiple batches next to the given
 * pivot card. The cards can be moved AFTER or BEFORE the pivot card. All
 * touched batches are renumbered automatically after the process.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MultipleCardMoveTask extends AbstractLongTask {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(MultipleCardMoveTask.class);
    /**
     * list of card IDs to be moved
     */
    private final List<String> cardIdsToMove;
    /**
     * pivot card to which the cards will be moved
     */
    private final Card pivotCard;
    /**
     * insert cards AFTER the pivot, not BEFORE
     */
    private final boolean after;
    /**
     * user that did the change
     */
    private final User user;
    
    /**
     * Creates a new instance.
     * 
     * @param cardIdsToMove
     * IDs of cards to be moved
     * @param pivotCard
     * pivot card (moving target)
     * @param after
     * insert cards AFTER the pivot, not BEFORE
     * @param user
     * user that did the change
     */
    public MultipleCardMoveTask(final List<String> cardIdsToMove, final Card pivotCard, final boolean after, final User user) {
        super();
        this.cardIdsToMove = cardIdsToMove;
        this.pivotCard = pivotCard;
        this.after = after;
        this.user = user;
    }
    
    @Override
    public void start() {
        // load all pivot neighbors (or friends...)
        
        MultipleCardMoveTask.LOG.debug("Getting pivot neighbors...");
        
        final List<String> newPivotFriends;
        
        try {
            newPivotFriends = RetrobiApplication.db().getCardRepository().getCardIdsInBatch(
                    this.pivotCard.getCatalog(),
                    this.pivotCard.getBatch());
        } catch (final GeneralRepositoryException x) {
            this.addError(x);
            return;
        }
        
        // move all cards in a temporary list
        
        MultipleCardMoveTask.LOG.debug(String.format(
                "Moving %d card(s) near %s [after = %s]...",
                this.cardIdsToMove.size(),
                this.pivotCard.getId(),
                this.after));
        
        final Collection<String> result = SimpleGeneralUtils.moveItems(
                newPivotFriends,
                this.cardIdsToMove,
                this.pivotCard.getId(),
                this.after);
        
        MultipleCardMoveTask.LOG.debug("Updating all changed items...");
        
        // update all moved cards
        
        this.initProgress(result.size());
        
        int newNumber = 1;
        final Map<Catalog, Set<String>> toBeRenumbered = new LinkedHashMap<Catalog, Set<String>>();
        
        for (final String cardId : result) {
            if (this.shouldStop()) {
                break;
            }
            
            try {
                MultipleCardMoveTask.LOG.debug("Updating card: " + cardId);
                
                // get the card to be edited
                
                final Card card = RetrobiApplication.db().getCardRepository().getCard(cardId);
                
                // remember batch to be renumbered
                
                if (!toBeRenumbered.containsKey(card.getCatalog())) {
                    toBeRenumbered.put(card.getCatalog(), new LinkedHashSet<String>());
                }
                
                toBeRenumbered.get(card.getCatalog()).add(card.getBatch());
                
                // remember old data
                
                final Catalog oldCatalog = card.getCatalog();
                final String oldBatch = card.getBatch();
                final int oldNumber = card.getNumberInBatch();
                
                // update card
                
                MultipleCardMoveTask.LOG.debug("Card before: " + card.toString());
                
                card.setCatalog(this.pivotCard.getCatalog());
                card.setBatch(this.pivotCard.getBatch());
                card.setBatchForSort(this.pivotCard.getBatchForSort());
                card.setNumberInBatch(newNumber);
                
                MultipleCardMoveTask.LOG.debug("Card after: " + card.toString());
                
                // save updated card
                
                RetrobiApplication.db().getCardRepository().updateCard(card);
                
                // log the change
                
                RetrobiApplication.db().getMessageRepository().eventCardMoved(
                        RetrobiWebApplication.getCSVLogger(),
                        oldCatalog,
                        oldBatch,
                        oldNumber,
                        card,
                        this.user);
            } catch (final NotFoundRepositoryException x) {
                this.addError(x);
            } catch (final GeneralRepositoryException x) {
                this.addError(x);
            } finally {
                this.incrementProgress();
                
                // increment number to be given
                
                newNumber++;
            }
        }
        
        // log the change
        
        try {
            RetrobiApplication.db().getMessageRepository().eventMultipleCardsMoved(
                    RetrobiWebApplication.getCSVLogger(),
                    this.cardIdsToMove.size(),
                    this.pivotCard,
                    this.user);
        } catch (final GeneralRepositoryException x) {
            this.addError(x);
        }
        
        // renumber all batches touched
        
        MultipleCardMoveTask.LOG.debug("Renumbering all batches touched...");
        
        this.initProgress(toBeRenumbered.size());
        
        for (final Entry<Catalog, Set<String>> entry : toBeRenumbered.entrySet()) {
            for (final String batch : entry.getValue()) {
                if (this.shouldStop()) {
                    break;
                }
                
                try {
                    // renumber the batch touched
                    
                    RetrobiApplication.db().getCardRepository().renumberBatch(
                            entry.getKey(),
                            batch);
                } catch (final GeneralRepositoryException x) {
                    this.addError(x);
                } catch (final NotFoundRepositoryException x) {
                    this.addError(x);
                }
            }
            
            this.incrementProgress();
        }
        
        // all done
        
        this.setDone();
        MultipleCardMoveTask.LOG.debug("Moving complete.");
    }
    
    @Override
    public String getName() {
        return String.format(
                "Hromadný přesun %s %s lístek %s",
                SimpleStringUtils.inflect(this.cardIdsToMove.size(), "lístku", "lístků", "lístků"),
                this.after ? "ZA" : "PŘED",
                this.pivotCard.toString());
    }
}
