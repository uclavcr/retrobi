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

package cz.insophy.retrobi.database.entity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import cz.insophy.retrobi.database.document.BasicDocument;

/**
 * Search result class containing search result rows. This entity class catches
 * the most important search result values in its own variables, the rest is
 * saved in the map provided by its parent class.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SearchResult extends BasicDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * search query
     */
    private String query;
    /**
     * result rows
     */
    private List<SearchResultRow> rows;
    /**
     * search duration (in [ms])
     */
    private int searchDuration;
    /**
     * fetch duration (in [ms])
     */
    private int fetchDuration;
    /**
     * parse duration (in [ms])
     */
    private int parseDuration;
    /**
     * total number of rows
     */
    private int totalRows;
    
    /**
     * Creates a new instance.
     */
    public SearchResult() {
        super();
        
        this.rows = new LinkedList<SearchResultRow>();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param query
     * user query
     */
    public SearchResult(final String query) {
        this();
        
        this.query = query;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns query.
     * 
     * @return query
     */
    @JSONProperty(value = "q")
    public String getQuery() {
        return this.query;
    }
    
    /**
     * Returns search rows.
     * 
     * @return search rows
     */
    @JSONTypeHint(SearchResultRow.class)
    public List<SearchResultRow> getRows() {
        return Collections.unmodifiableList(this.rows);
    }
    
    /**
     * Returns a search duration in [ms].
     * 
     * @return search duration
     */
    @JSONProperty(value = "search_duration")
    public int getSearchDuration() {
        return this.searchDuration;
    }
    
    /**
     * Returns a fetch duration in [ms].
     * 
     * @return fetch duration
     */
    @JSONProperty(value = "fetch_duration")
    public int getFetchDuration() {
        return this.fetchDuration;
    }
    
    /**
     * Returns a parse duration in [ms].
     * 
     * @return parse duration
     */
    @JSONProperty(value = "parse_duration")
    public int getParseDuration() {
        return this.parseDuration;
    }
    
    /**
     * Returns a total count of rows.
     * 
     * @return total count of rows
     */
    @JSONProperty(value = "total_rows")
    public int getTotalRows() {
        return this.totalRows;
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets query.
     * 
     * @param value
     * query
     */
    public void setQuery(final String value) {
        this.query = value;
    }
    
    /**
     * Sets rows.
     * 
     * @param value
     * rows
     */
    public void setRows(final List<SearchResultRow> value) {
        this.rows = value;
    }
    
    /**
     * Sets search duration.
     * 
     * @param value
     * search duration
     */
    public void setSearchDuration(final int value) {
        this.searchDuration = value;
    }
    
    /**
     * Sets fetch duration.
     * 
     * @param value
     * fetch duration
     */
    public void setFetchDuration(final int value) {
        this.fetchDuration = value;
    }
    
    /**
     * Sets parse duration.
     * 
     * @param value
     * parse duration
     */
    public void setParseDuration(final int value) {
        this.parseDuration = value;
    }
    
    /**
     * Sets total row count.
     * 
     * @param value
     * total row count
     */
    public void setTotalRows(final int value) {
        this.totalRows = value;
    }
}
