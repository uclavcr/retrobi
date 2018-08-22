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

import org.svenson.JSONProperty;

import cz.insophy.retrobi.database.document.StandaloneDocument;

/**
 * User comment to a card. All comments are private by default and can be seen
 * only by the author. Each card can have multiple comments.
 * 
 * @author Vojtěch Hordějčuk
 */
public class Comment extends StandaloneDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card document ID
     */
    private String cardId;
    /**
     * user document ID
     */
    private String userId;
    /**
     * date added
     */
    private Time added;
    /**
     * comment text
     */
    private String text;
    
    /**
     * Creates a new instance.
     */
    public Comment() {
        super();
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the document type flag.
     * 
     * @return document type flag
     */
    @JSONProperty(value = "TAG_comment", readOnly = true)
    public boolean isComment() {
        return true;
    }
    
    /**
     * Returns the related card ID.
     * 
     * @return related card ID
     */
    @JSONProperty(value = "card_id")
    public String getCardId() {
        return this.cardId;
    }
    
    /**
     * Returns the author ID.
     * 
     * @return author ID
     */
    @JSONProperty(value = "user_id")
    public String getUserId() {
        return this.userId;
    }
    
    /**
     * Returns the date of addition.
     * 
     * @return date of addition
     */
    @JSONProperty(value = "date_added")
    public Time getAdded() {
        return this.added;
    }
    
    /**
     * Returns the comment text.
     * 
     * @return comment text
     */
    public String getText() {
        return this.text;
    }
    
    @Override
    public String toString() {
        return String.format("Comment '%s' for card '%s' by user '%s'", this.text, this.cardId, this.userId);
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets related card ID.
     * 
     * @param value
     * card ID
     */
    public void setCardId(final String value) {
        this.cardId = value;
    }
    
    /**
     * Sets related author ID.
     * 
     * @param value
     * author ID
     */
    public void setUserId(final String value) {
        this.userId = value;
    }
    
    /**
     * Sets date added.
     * 
     * @param value
     * date added
     */
    public void setAdded(final Time value) {
        this.added = value;
    }
    
    /**
     * Sets text.
     * 
     * @param value
     * text
     */
    public void setText(final String value) {
        this.text = value;
    }
}
