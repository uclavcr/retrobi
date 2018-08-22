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

import java.util.ArrayList;
import java.util.List;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.model.BatchCardModificationResult;

/**
 * A task that saves batch card modification result.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SaveBatchModificationTask extends AbstractLongTask {
    /**
     * batch card modification result
     */
    private final BatchCardModificationResult result;
    /**
     * user that did the change
     */
    private final User user;
    
    /**
     * Creates a new instance.
     * 
     * @param result
     * modification result
     * @param user
     * user who did the change(s)
     */
    public SaveBatchModificationTask(final BatchCardModificationResult result, final User user) {
        super();
        this.result = result;
        this.user = user;
    }
    
    @Override
    public void start() {
        if (!this.result.getChangedCards().isEmpty()) {
            final List<Card> cardsWithError = new ArrayList<Card>(64);
            
            // reset progress
            
            this.initProgress(this.result.getChangedCards().size());
            
            // save each changed card and add it into the done list
            
            for (final Card card : this.result.getChangedCards()) {
                if (this.shouldStop()) {
                    break;
                }
                
                try {
                    // save the update card into the database
                    
                    RetrobiApplication.db().getCardRepository().updateCard(card);
                    
                    // log the change
                    
                    RetrobiApplication.db().getMessageRepository().eventCardModified(
                            RetrobiWebApplication.getCSVLogger(),
                            card,
                            this.result.getLastModification(),
                            this.user);
                } catch (final GeneralRepositoryException x) {
                    cardsWithError.add(card);
                    this.addError(x);
                } finally {
                    // increments progress
                    
                    this.incrementProgress();
                }
            }
            
            // log the change
            
            final int changedCardCount = this.result.getChangedCards().size() - cardsWithError.size();
            
            try {
                RetrobiApplication.db().getMessageRepository().eventMultipleCardsModified(
                        RetrobiWebApplication.getCSVLogger(),
                        changedCardCount,
                        this.result.getLastModification(),
                        this.user);
            } catch (final GeneralRepositoryException x) {
                this.addError(x);
            }
            
            // remove done cards from the result
            
            this.result.retainChangedCards(cardsWithError);
        }
        
        // finish
        
        this.setDone();
    }
    
    @Override
    public String getName() {
        return "Uložit změněné lístky";
    }
}
