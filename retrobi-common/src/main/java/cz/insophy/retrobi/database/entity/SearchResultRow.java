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

import cz.insophy.retrobi.database.document.BasicDocument;

/**
 * Fulltext search result row.This entity class catches the most important
 * search result values in its own variables, the rest is saved in the map
 * provided by its parent class.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SearchResultRow extends BasicDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * matching document ID
     */
    private String id;
    /**
     * the normalized score (0,1) inclusive for the match
     */
    private double score;
    
    /**
     * Creates a new instance.
     */
    public SearchResultRow() {
        super();
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns a document ID.
     * 
     * @return document ID
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Returns a document score.
     * 
     * @return document score
     */
    public double getScore() {
        return this.score;
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets document ID.
     * 
     * @param value
     * document ID
     */
    public void setId(final String value) {
        this.id = value;
    }
    
    /**
     * Sets document score.
     * 
     * @param value
     * document score
     */
    public void setScore(final double value) {
        this.score = value;
    }
}
