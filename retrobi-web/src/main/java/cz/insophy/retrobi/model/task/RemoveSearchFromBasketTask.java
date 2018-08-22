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
import cz.insophy.retrobi.database.entity.SearchResult;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.model.SessionCardContainer;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;

/**
 * A task that removes the search result from the basket.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RemoveSearchFromBasketTask extends AbstractLongTask {
    /**
     * search query
     */
    private final SearchQuery query;
    /**
     * source card container
     */
    private final SessionCardContainer source;
    
    /**
     * Creates a new instance.
     * 
     * @param query
     * search query
     * @param source
     * source container
     */
    public RemoveSearchFromBasketTask(final SearchQuery query, final SessionCardContainer source) {
        super();
        this.query = query;
        this.source = source;
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
                    Integer.MAX_VALUE);
        } catch (final GeneralRepositoryException x) {
            this.addError(x);
        }
        
        // search done
        
        this.incrementProgress();
        
        // remove results from the basket
        
        if (result != null) {
            final List<String> cardIds = SimpleSearchUtils.extractCardIds(result);
            this.source.removeFromBasket(cardIds);
        }
        
        // removing done
        
        this.incrementProgress();
        
        // finish
        
        this.setDone();
    }
    
    @Override
    public String getName() {
        return String.format("Odebrat dotaz '%s' ze schránky", this.query.getQuery());
    }
}
