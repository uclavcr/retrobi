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
import java.util.List;

import org.svenson.JSONProperty;

import cz.insophy.retrobi.database.document.StandaloneDocument;

/**
 * Saved set of cards selected by a user.
 * 
 * @author Vojtěch Hordějčuk
 */
public class Cardset extends StandaloneDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * user ID
     */
    private String userId;
    /**
     * title
     */
    private String title;
    /**
     * time added
     */
    private Time added;
    /**
     * list of cards in this set
     */
    private List<String> cardIds;
    
    /**
     * Creates a new default instance.
     */
    public Cardset() {
        super();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param userId
     * user ID
     * @param title
     * title
     * @param cardIds
     * list of card IDs
     */
    public Cardset(final String userId, final String title, final List<String> cardIds) {
        this();
        
        this.added = Time.now();
        this.cardIds = cardIds;
        this.title = title;
        this.userId = userId;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the document type flag.
     * 
     * @return document type flag
     */
    @JSONProperty(value = "TAG_cardset", readOnly = true)
    public boolean isCardset() {
        return true;
    }
    
    /**
     * Returns the user ID.
     * 
     * @return the user ID
     */
    @JSONProperty(value = "user_id")
    public String getUserId() {
        return this.userId;
    }
    
    /**
     * Returns the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Returns the time of addition.
     * 
     * @return the time of addition
     */
    public Time getAdded() {
        return this.added;
    }
    
    /**
     * Returns the list of card IDs.
     * 
     * @return the list of card IDs
     */
    @JSONProperty(value = "cards")
    public List<String> getCardIds() {
        return Collections.unmodifiableList(this.cardIds);
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the user ID.
     * 
     * @param value
     * the user ID
     */
    public void setUserId(final String value) {
        this.userId = value;
    }
    
    /**
     * Sets the title.
     * 
     * @param value
     * the title
     */
    public void setTitle(final String value) {
        this.title = value;
    }
    
    /**
     * Sets the time of addition.
     * 
     * @param value
     * the time of addition
     */
    public void setAdded(final Time value) {
        this.added = value;
    }
    
    /**
     * Sets the card IDs.
     * 
     * @param value
     * the card IDs
     */
    public void setCardIds(final List<String> value) {
        this.cardIds = value;
    }
}
