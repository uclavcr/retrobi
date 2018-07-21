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
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.model.BatchCardModificationResult;

/**
 * A task that does the batch card modification.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BatchModificationTask extends AbstractLongTask {
    /**
     * card IDs to be modified (a copy)
     */
    private final List<String> cardIds;
    /**
     * batch modification result
     */
    private final BatchCardModificationResult target;
    /**
     * card modification to use
     */
    private final CardModification modification;
    
    /**
     * Creates a new instance.
     * 
     * @param cardIds
     * card IDs (will be copied)
     * @param result
     * target result container
     * @param modification
     * modification to use
     */
    public BatchModificationTask(final List<String> cardIds, final BatchCardModificationResult result, final CardModification modification) {
        super();
        this.cardIds = new ArrayList<String>(cardIds);
        this.target = result;
        this.modification = modification;
    }
    
    @Override
    public String getName() {
        return this.modification.getTitle();
    }
    
    @Override
    public void start() {
        // reset the result before continuing
        
        this.target.clear();
        
        if (this.cardIds.size() <= Settings.MANY_BASKET_CARDS) {
            // reset progress
            
            this.initProgress(this.cardIds.size());
            
            for (final String cardId : this.cardIds) {
                if (this.shouldStop()) {
                    break;
                }
                
                try {
                    // load the given card
                    
                    final Card card = RetrobiApplication.db().getCardRepository().getCard(cardId);
                    
                    // modify the card
                    
                    this.target.modify(card, this.modification);
                } catch (final NotFoundRepositoryException x) {
                    this.addError(x);
                } catch (final GeneralRepositoryException x) {
                    this.addError(x);
                } finally {
                    // increment the progress
                    
                    this.incrementProgress();
                }
            }
        } else {
            // too many cards
            
            this.addError(new UnsupportedOperationException(String.format(
                    "Lístků je příliš mnoho - počet lístků je %d, maximální povolený počet je %d.",
                    this.cardIds.size(),
                    Settings.MANY_BASKET_CARDS)));
        }
        
        // finish
        
        this.setDone();
    }
}
