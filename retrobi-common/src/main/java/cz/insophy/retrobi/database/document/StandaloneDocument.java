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

import org.svenson.JSONProperty;

/**
 * Base class for all entity database documents. Each document has a
 * database-wide unique ID and a revision. The <code>CouchDB</code> database has
 * one nice feature: it never allows update conflicts to happen. If two
 * instances of one document are modified and both saved, the second one causes
 * an exception on save, because the revision of the stored document has
 * changed.
 * 
 * @author Vojtěch Hordějčuk
 */
public class StandaloneDocument extends BasicDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * CouchDB document ID
     */
    private String id;
    /**
     * CouchDB document revision
     */
    private String revision;
    
    /**
     * Creates a new instance.
     */
    public StandaloneDocument() {
        this.id = null;
        this.revision = null;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns document ID.
     * 
     * @return document ID
     */
    @JSONProperty(value = "_id", ignoreIfNull = true)
    public String getId() {
        return this.id;
    }
    
    /**
     * Returns document revision.
     * 
     * @return document revision
     */
    @JSONProperty(value = "_rev", ignoreIfNull = true)
    public String getRevision() {
        return this.revision;
    }
    
    @Override
    public String toString() {
        return String.format("CouchDB document (id = %s, revision = %s)", this.id, this.revision);
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the document ID.
     * 
     * @param value
     * document ID
     */
    public void setId(final String value) {
        this.id = value;
    }
    
    /**
     * Sets the document revision.
     * 
     * @param value
     * document revision
     */
    public void setRevision(final String value) {
        this.revision = value;
    }
    
    // =======
    // UTILITY
    // =======
    
    /**
     * Checks if two documents are equal by ID. Any <code>null</code> values are
     * handled correctly.
     * 
     * @param d1
     * first document or <code>null</code>
     * @param d2
     * second document or <code>null</code>
     * @return <code>true</code> if the document IDs are not <code>null</code>
     * and are equal, <code>false</code> otherwise
     */
    public static boolean equalsById(final StandaloneDocument d1, final StandaloneDocument d2) {
        if ((d1 == null) || (d2 == null)) {
            return false;
        }
        
        if ((d1.getId() == null) || (d2.getId() == null)) {
            return false;
        }
        
        return d1.getId().equals(d2.getId());
    }
}
