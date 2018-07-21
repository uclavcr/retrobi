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

import java.util.List;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * A task that removes a batch upon execution.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteBatchTask extends AbstractLongTask {
    /**
     * a catalog to which batch belongs to
     */
    private final Catalog catalog;
    /**
     * batch to be removed
     */
    private final String batch;
    /**
     * user who did the change(s)
     */
    private final User user;
    
    /**
     * Creates a new instance.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch
     * @param user
     * user who did the change(s)
     */
    public DeleteBatchTask(final Catalog catalog, final String batch, final User user) {
        super();
        this.catalog = catalog;
        this.batch = batch;
        this.user = user;
    }
    
    @Override
    public void start() {
        this.initProgress(1);
        
        try {
            // load cards
            
            final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsInBatch(this.catalog, this.batch);
            
            this.incrementProgress();
            
            // remove cards individually
            
            this.initProgress(cardIds.size());
            
            for (final String cardId : cardIds) {
                if (this.shouldStop()) {
                    break;
                }
                
                try {
                    // remove the card
                    
                    RetrobiOperations.deleteCard(cardId, this.user, RetrobiWebApplication.getCSVLogger());
                } catch (final GeneralOperationException x) {
                    this.addError(x);
                } finally {
                    // increments progress
                    
                    this.incrementProgress();
                }
            }
        } catch (final GeneralRepositoryException x) {
            this.addError(x);
            this.setDone();
            return;
        } finally {
            // finish
            
            this.setDone();
        }
    }
    
    @Override
    public String getName() {
        return String.format("Smazat skupinu '%s' z katalogu '%s'", this.batch, this.catalog);
    }
}
