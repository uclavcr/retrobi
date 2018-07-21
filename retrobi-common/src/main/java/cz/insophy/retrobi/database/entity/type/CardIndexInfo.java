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

package cz.insophy.retrobi.database.entity.type;

import java.io.Serializable;

/**
 * Information about card search index.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardIndexInfo implements Serializable, Comparable<CardIndexInfo> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * name (system name)
     */
    private String name;
    /**
     * title (human name)
     */
    private String title;
    /**
     * minimal user role for viewing
     */
    private UserRole role;
    /**
     * order (weight - lower values first)
     */
    private int order;
    
    /**
     * Creates a new instance.
     */
    public CardIndexInfo() {
        this.name = null;
        this.title = null;
        this.role = UserRole.GUEST;
        this.order = 0;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param name
     * name
     * @param title
     * title
     * @param role
     * minimal role for using
     * @param order
     * order
     */
    public CardIndexInfo(final String name, final String title, final UserRole role, final int order) {
        this.name = name;
        this.title = title;
        this.role = role;
        this.order = order;
    }
    
    /**
     * Returns the name (system).
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
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
     * Returns the minimal user role for viewing.
     * 
     * @return the minimal user role
     */
    public UserRole getRole() {
        return this.role;
    }
    
    /**
     * Returns the order.
     * 
     * @return the order
     */
    public int getOrder() {
        return this.order;
    }
    
    /**
     * Sets the name.
     * 
     * @param value
     * the name
     */
    public void setName(final String value) {
        this.name = value;
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
     * Sets the minimal user role for viewing.
     * 
     * @param value
     * the user role
     */
    public void setRole(final UserRole value) {
        this.role = value;
    }
    
    /**
     * Sets the order.
     * 
     * @param value
     * the order
     */
    public void setOrder(final int value) {
        this.order = value;
    }
    
    @Override
    public String toString() {
        return String.format("Card index '%s' (title: '%s', minimal role: '%s', order: %d)", this.name, this.title, this.role.name(), this.order);
    }
    
    @Override
    public int compareTo(final CardIndexInfo o) {
        if (this.order < o.order) {
            return -1;
        } else if (this.order > o.order) {
            return 1;
        } else {
            return 0;
        }
    }
}
