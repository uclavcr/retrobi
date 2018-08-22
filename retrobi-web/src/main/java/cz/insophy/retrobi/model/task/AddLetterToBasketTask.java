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
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.model.SessionCardContainer;

/**
 * A task that adds a whole letter to the basket.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AddLetterToBasketTask extends AbstractLongTask {
    /**
     * catalog
     */
    private final Catalog catalog;
    /**
     * letter
     */
    private final String letter;
    /**
     * target card container
     */
    private final SessionCardContainer target;
    /**
     * basket size limit
     */
    private final int limit;
    
    /**
     * Creates a new instance.
     * 
     * @param catalog
     * catalog
     * @param letter
     * letter
     * @param target
     * target container
     * @param limit
     * basket size limit
     */
    public AddLetterToBasketTask(final Catalog catalog, final String letter, final SessionCardContainer target, final int limit) {
        super();
        this.catalog = catalog;
        this.letter = letter;
        this.target = target;
        this.limit = limit;
    }
    
    @Override
    public String getName() {
        return String.format("Přidat %s (katalog: %s) do schránky", this.letter, this.catalog.name());
    }
    
    @Override
    public void start() {
        // get batches
        
        final List<String> batches = CardCatalogModel.getInstance().getBatches(this.catalog, this.letter);
        
        // reset progress
        
        this.initProgress(batches.size());
        
        // insert each batch to the basket
        
        for (final String batch : batches) {
            if (this.target.isBasketFull(this.limit)) {
                break;
            }
            
            if (this.shouldStop()) {
                break;
            }
            
            try {
                // load all card IDs in the given batch
                
                final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsInBatch(this.catalog, batch);
                
                // add cards to basket
                
                this.target.addToBasket(cardIds, this.limit);
            } catch (final GeneralRepositoryException x) {
                this.addError(x);
                break;
            } catch (final OverLimitException x) {
                this.addError(x);
                break;
            } finally {
                // increment the progress
                
                this.incrementProgress();
            }
        }
        
        // finished
        
        this.setDone();
    }
}
