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

package cz.insophy.retrobi.database.entity.attribute;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import cz.insophy.retrobi.database.entity.type.UserRole;

/**
 * Card attribute prototype including the attribute definition.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AttributePrototype implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * unique key (in English alphabet with no symbols)
     */
    private String key;
    /**
     * human readable title
     */
    private String title;
    /**
     * repeatable flag
     */
    private boolean repeat;
    /**
     * minimal role required for viewing
     */
    private UserRole role;
    /**
     * default value of the attribute (never <code>null</code>)
     */
    private String value;
    /**
     * list of indexes in which the attribute is searched for
     */
    private List<String> indexes;
    /**
     * children nodes (can be empty)
     */
    private List<AttributePrototype> children;
    
    /**
     * Creates a new instance.
     */
    public AttributePrototype() {
        this(null, null, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param key
     * attribute key
     * @param title
     * attribute title
     * @param repeat
     * repeatable flag
     * @param children
     * children nodes (can be empty)
     */
    public AttributePrototype(final String key, final String title, final boolean repeat, final AttributePrototype... children) {
        this(key, title, repeat, UserRole.GUEST, "", children);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param key
     * attribute key
     * @param title
     * attribute title
     * @param repeat
     * repeatable flag
     * @param value
     * default attribute value (<code>null</code> will become empty string)
     * @param children
     * children nodes (can be empty)
     */
    public AttributePrototype(final String key, final String title, final boolean repeat, final String value, final AttributePrototype... children) {
        this(key, title, repeat, UserRole.GUEST, value, children);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param key
     * attribute key
     * @param title
     * attribute title
     * @param repeat
     * repeatable flag
     * @param role
     * minimal role for viewing
     * @param value
     * default attribute value (<code>null</code> will become empty string)
     * @param children
     * children nodes (can be empty)
     */
    private AttributePrototype(final String key, final String title, final boolean repeat, final UserRole role, final String value, final AttributePrototype... children) {
        this.key = key;
        this.title = title;
        this.repeat = repeat;
        this.role = role;
        this.value = value == null ? "" : value;
        this.children = (children.length != 0) ? Arrays.asList(children) : null;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Checks if the node is atomic. That means, if it has any children.
     * 
     * @return <code>true</code> if the node is atomic, <code>false</code>
     * otherwise
     */
    @JSONProperty(ignore = true)
    public boolean isAtomic() {
        if (this.children != null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns the key.
     * 
     * @return the key
     */
    public String getKey() {
        return this.key;
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
     * Returns the repeatable flag.
     * 
     * @return the repeatable flag
     */
    public boolean isRepeat() {
        return this.repeat;
    }
    
    /**
     * Returns the minimal role for viewing.
     * 
     * @return the role
     */
    public UserRole getRole() {
        return this.role;
    }
    
    /**
     * Returns the default attribute value.
     * 
     * @return the default value (never <code>null</code>)
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * Returns the list of indexes or <code>null</code> if empty.
     * 
     * @return the list of indexes or <code>null</code>
     */
    @JSONProperty(ignoreIfNull = true)
    @JSONTypeHint(value = String.class)
    public List<String> getIndexes() {
        if ((this.indexes == null) || this.indexes.isEmpty()) {
            return null;
        }
        
        return Collections.unmodifiableList(this.indexes);
    }
    
    /**
     * Returns the list of children nodes or <code>null</code> if empty.
     * 
     * @return the list of children nodes or <code>null</code>
     */
    @JSONProperty(ignoreIfNull = true)
    @JSONTypeHint(value = AttributePrototype.class)
    public List<AttributePrototype> getChildren() {
        if ((this.children == null) || this.children.isEmpty()) {
            return null;
        }
        
        return Collections.unmodifiableList(this.children);
    }
    
    @Override
    public String toString() {
        if (this.repeat) {
            return this.title + " (opakovaný)";
        }
        
        return this.title;
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the key.
     * 
     * @param newKey
     * the new key
     */
    public void setKey(final String newKey) {
        this.key = newKey;
    }
    
    /**
     * Sets the title.
     * 
     * @param newTitle
     * the new title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }
    
    /**
     * Sets the repeatable flag.
     * 
     * @param newRepeat
     * the repeatable flag
     */
    public void setRepeat(final boolean newRepeat) {
        this.repeat = newRepeat;
    }
    
    /**
     * Sets the minimal role for viewing.
     * 
     * @param newRole
     * the minimal role for viewing
     */
    public void setRole(final UserRole newRole) {
        this.role = newRole;
    }
    
    /**
     * Sets the new default value. If the new value is <code>null</code>, it
     * will be converted to an empty string.
     * 
     * @param newValue
     * the new default value
     */
    public void setValue(final String newValue) {
        this.value = newValue == null ? "" : newValue;
    }
    
    /**
     * Sets the index list.
     * 
     * @param newIndexes
     * the new index list
     */
    public void setIndexes(final List<String> newIndexes) {
        this.indexes = newIndexes;
    }
    
    /**
     * Adds an index to the index list.
     * 
     * @param indexToAdd
     * an index name to be added
     */
    public void addIndex(final String indexToAdd) {
        if (this.indexes == null) {
            this.indexes = new LinkedList<String>();
        }
        
        if (!this.indexes.contains(indexToAdd)) {
            this.indexes.add(indexToAdd);
        }
    }
    
    /**
     * Sets the children nodes.
     * 
     * @param newChildren
     * the children nodes
     */
    public void setChildren(final List<AttributePrototype> newChildren) {
        this.children = newChildren;
    }
}
