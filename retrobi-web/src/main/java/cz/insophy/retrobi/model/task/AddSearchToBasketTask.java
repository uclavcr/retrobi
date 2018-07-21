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
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.SearchResult;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.model.SessionCardContainer;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;

/**
 * A task that adds the search result to the basket.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AddSearchToBasketTask extends AbstractLongTask {
    /**
     * search query
     */
    private final SearchQuery query;
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
     * @param query
     * search query
     * @param target
     * target container
     * @param limit
     * basket size limit
     */
    public AddSearchToBasketTask(final SearchQuery query, final SessionCardContainer target, final int limit) {
        super();
        this.query = query;
        this.target = target;
        this.limit = limit;
    }
    
    @Override
    public boolean isQuick() {
        if ((this.limit >= 0) && (this.limit <= Settings.MANY_CARDS)) {
            return true;
        }
        
        return super.isQuick();
    }
    
    @Override
    public void start() {
        // reset progress
        
        this.initProgress(2);
        
        SearchResult result = null;
        
        try {
            // do the search
            
            result = RetrobiApplication.db().getCardSearchRepository().search(
                    this.query.getIndex(),
                    this.query.getQuery(),
                    this.query.isSensitive(),
                    this.query.getCatalogFilter(),
                    this.query.getStateFilter(),
                    0,
                    this.limit < 0 ? Integer.MAX_VALUE : this.limit);
        } catch (final GeneralRepositoryException x) {
            this.addError(x);
        }
        
        // search done
        
        this.incrementProgress();
        
        // add results to the basket
        
        if (result != null) {
            final List<String> cardIds = SimpleSearchUtils.extractCardIds(result);
            
            try {
                this.target.addToBasket(cardIds, this.limit);
            } catch (final OverLimitException x) {
                this.addError(x);
            }
        }
        
        // adding done
        
        this.incrementProgress();
        
        // finish
        
        this.setDone();
    }
    
    @Override
    public String getName() {
        return String.format("Přidat dotaz '%s' do schránky", this.query.getQuery());
    }
}
