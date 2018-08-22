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
 * Base class for document attachments. Each document in CouchDB can have any
 * number of attachments. Each attachment is a file somewhat associated with the
 * document.<br>
 * <br>
 * Working with attachments is separated from documents, only stubs are
 * provided. Attachment contents can be accessed only via special HTTP GET/PUT
 * requests.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DocumentAttachment extends BasicDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * file MIME type
     */
    private String contentType;
    /**
     * content length in bytes
     */
    private long length;
    
    /**
     * Creates a new instance.
     */
    public DocumentAttachment() {
        super();
        
        this.contentType = null;
        this.length = 0;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the file content type.
     * 
     * @return file content type
     */
    @JSONProperty("content_type")
    public String getContentType() {
        return this.contentType;
    }
    
    /**
     * Returns the file size in bytes.
     * 
     * @return file size in bytes
     */
    public long getLength() {
        return this.length;
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the content type.
     * 
     * @param value
     * content type
     */
    public void setContentType(final String value) {
        this.contentType = value;
    }
    
    /**
     * Sets the file size in bytes.
     * 
     * @param value
     * file size
     */
    public void setLength(final long value) {
        this.length = value;
    }
}
