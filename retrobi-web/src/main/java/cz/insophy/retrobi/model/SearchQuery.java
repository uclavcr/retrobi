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
import java.util.Set;

import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;

/**
 * Search query wrapper object.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SearchQuery implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * search source index
     */
    private final AbstractCardIndex index;
    /**
     * search query
     */
    private final String query;
    /**
     * case sensitivity flag
     */
    private final boolean sensitive;
    /**
     * search in basket only flag
     */
    private final boolean basketOnly;
    /**
     * search in the given state only (or <code>null</code>)
     */
    private final CardState stateFilter;
    /**
     * a set of catalogs to search in (or <code>null</code>)
     */
    private final Set<Catalog> catalogFilter;
    
    /**
     * Creates a new search query.
     * 
     * @param index
     * index to search in
     * @param query
     * the search query
     * @param sensitive
     * case sensitivity enabled
     * @param basketOnly
     * search in basket only
     * @param stateFilter
     * card state filter (or <code>null</code>)
     * @param catalogFilter
     * catalog filter (or <code>null</code>)
     */
    public SearchQuery(final AbstractCardIndex index, final String query, final boolean sensitive, final boolean basketOnly, final CardState stateFilter, final Set<Catalog> catalogFilter) {
        this.index = index;
        this.query = query;
        this.sensitive = sensitive;
        this.basketOnly = basketOnly;
        this.stateFilter = stateFilter;
        this.catalogFilter = catalogFilter;
    }
    
    /**
     * Returns the current search index.
     * 
     * @return the search index
     */
    public AbstractCardIndex getIndex() {
        return this.index;
    }
    
    /**
     * Returns the search query.
     * 
     * @return the search query
     */
    public String getQuery() {
        return this.query;
    }
    
    /**
     * Checks if the search is case sensitive.
     * 
     * @return <code>true</code> if the search is case sensitive,
     * <code>false</code> otherwise
     */
    public boolean isSensitive() {
        return this.sensitive;
    }
    
    /**
     * Checks if the searching only in the basket is enabled.
     * 
     * @return <code>true</code> if the searching only in the basket is enabled,
     * <code>false</code> otherwise
     */
    public boolean isBasketOnly() {
        return this.basketOnly;
    }
    
    /**
     * Return the current search state limitation. If <code>null</code>, no
     * restriction is applied.
     * 
     * @return the card state filter or <code>null</code>
     */
    public CardState getStateFilter() {
        return this.stateFilter;
    }
    
    /**
     * Returns the set of catalogs to search in. If <code>null</code>, no
     * restriction is applied.
     * 
     * @return catalog filter or <code>null</code>
     */
    public Set<Catalog> getCatalogFilter() {
        return this.catalogFilter;
    }
    
    @Override
    public String toString() {
        return String.format(
                "Query '%s' (sensitive = %s, basket = %s)",
                this.query,
                this.sensitive,
                this.basketOnly);
    }
}
