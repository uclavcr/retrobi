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

import cz.insophy.retrobi.database.entity.type.UserRole;

/**
 * Card property list item. Encapsulates basic property data (key, title and
 * value) for viewing and editing purposes.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardPropertyListItem implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * property title (human readable name)
     */
    private final String title;
    /**
     * minimal user role
     */
    private final UserRole minRole;
    /**
     * property value
     */
    private final Object value;
    /**
     * show if empty
     */
    private final boolean isEmpty;
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * property title
     * @param minRole
     * minimal user role for showing
     * @param value
     * property value
     * @param isEmpty
     * the value is empty
     */
    public CardPropertyListItem(final String title, final UserRole minRole, final Object value, final boolean isEmpty) {
        this.title = title;
        this.minRole = minRole;
        this.value = value;
        this.isEmpty = isEmpty;
    }
    
    /**
     * Returns the property title.
     * 
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Returns the property value.
     * 
     * @return the value
     */
    public Object getValue() {
        return this.value;
    }
    
    /**
     * Checks if the property is visible.
     * 
     * @param loggedUserRole
     * a role of the logged user
     * @return <code>true</code> if the property is visible, <code>false</code>
     * otherwise
     */
    public boolean isVisible(final UserRole loggedUserRole) {
        if (loggedUserRole.ordinal() < this.minRole.ordinal()) {
            return false;
        }
        
        if (this.isEmpty) {
            return false;
        }
        
        return true;
    }
}
