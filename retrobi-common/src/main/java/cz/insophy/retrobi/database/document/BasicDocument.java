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

package cz.insophy.retrobi.database.document;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.svenson.DynamicProperties;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Base class for all JSON documents. It can be used as a dynamic placeholder
 * for all types of documents. All document entity classes should extend this
 * class and add properties with getters/setters.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasicDocument implements DynamicProperties, Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * CouchDB document attachments (file stubs)
     */
    private Map<String, DocumentAttachment> attachments;
    /**
     * fallback/unresolved attributes map
     */
    private Map<String, Object> properties;
    
    /**
     * Creates a new instance.
     */
    public BasicDocument() {
        this.attachments = null;
        this.properties = null;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns document attachments or <code>null</code>.
     * 
     * @return document attachments or <code>null</code>
     */
    @JSONProperty(value = "_attachments", ignoreIfNull = true)
    @JSONTypeHint(DocumentAttachment.class)
    public Map<String, DocumentAttachment> getAttachments() {
        if ((this.attachments == null) || this.attachments.isEmpty()) {
            return null;
        }
        
        return this.attachments;
    }
    
    /**
     * Returns a count of attachments.
     * 
     * @return a count of attachments
     */
    @JSONProperty(ignore = true)
    public int getAttachmentCount() {
        if (this.attachments == null) {
            return 0;
        }
        
        return this.attachments.size();
    }
    
    /**
     * Returns names of all attachments. Never returns <code>null</code>.
     * 
     * @return names of all attachments
     */
    @JSONProperty(ignore = true)
    public Set<String> getAttachmentNames() {
        if ((this.attachments == null) || this.attachments.isEmpty()) {
            return Collections.emptySet();
        }
        
        return this.attachments.keySet();
    }
    
    /**
     * Returns names of all attachments, sorted. Never returns <code>null</code>
     * . The names are sorted by a natural string insensitive comparator.
     * 
     * @return names of all attachments sorted
     */
    @JSONProperty(ignore = true)
    public List<String> getAttachmentNamesSorted() {
        final List<String> names = new LinkedList<String>(this.getAttachmentNames());
        Collections.sort(names, SimpleStringUtils.getNaturalStringComparator());
        return Collections.unmodifiableList(names);
    }
    
    @Override
    public Set<String> propertyNames() {
        if (this.properties == null) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(this.properties.keySet());
    }
    
    @Override
    public Object getProperty(final String name) {
        if (this.properties == null) {
            return null;
        }
        
        return this.properties.get(name);
    }
    
    @Override
    public String toString() {
        return String.format("CouchDB document (%s)", String.valueOf(this.properties));
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the document attachments.
     * 
     * @param value
     * document attachments.
     */
    public void setAttachments(final Map<String, DocumentAttachment> value) {
        this.attachments = value;
    }
    
    @Override
    public void setProperty(final String key, final Object value) {
        if (this.properties == null) {
            // lazy initialization
            
            this.properties = new HashMap<String, Object>(3);
        }
        
        this.properties.put(key, value);
    }
    
    /**
     * Clears all properties.
     */
    public void clearProperties() {
        if (this.properties != null) {
            this.properties.clear();
        }
    }
}
