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

package cz.insophy.retrobi.database;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jcouchdb.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.document.BasicDocument;
import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;

/**
 * Text repository. This repository manages page text and other textual content
 * on the web site. Texts are saved in the database among other documents. Each
 * long text is in <code>XHTML 1.0 Strict</code> format, short texts are in
 * plain text. Short texts are distinct from long texts by the key prefix (L =
 * long, S = short).
 * 
 * @author Vojtěch Hordějčuk
 */
final public class TextRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(TextRepository.class);
    /**
     * key of the document's text attribute
     */
    private static final String TEXT_PROPERTY_KEY = "text";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected TextRepository(final Database database) {
        super(database);
    }
    
    /**
     * Saves a text to the database. If the document exists, it is overwritten.
     * 
     * @param key
     * a text key
     * @param text
     * new text
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void setText(final TextType key, final String text) throws GeneralRepositoryException {
        TextRepository.LOG.debug(String.format("Saving text '%s' (content length: %d)...", key.name(), text.length()));
        
        try {
            // load the existing document from the database
            
            final StandaloneDocument document = this.loadDocument(StandaloneDocument.class, TextRepository.getTextDocumentId(key));
            TextRepository.LOG.debug("Document with the text exists and will be updated.");
            document.setProperty(TextRepository.TEXT_PROPERTY_KEY, text);
            this.updateDocument(document);
        } catch (final NotFoundRepositoryException x) {
            // create new document
            
            TextRepository.LOG.debug("Document with the text does not exist and will be created.");
            this.createDocument(TextRepository.createTextDocument(key, text));
        }
        
        TextRepository.LOG.debug("Text document was updated.");
    }
    
    /**
     * Returns all texts. The order is same as in the text enumeration.
     * 
     * @return list of all texts
     */
    public List<TextType> getTexts() {
        return Collections.unmodifiableList(Arrays.asList(TextType.values()));
    }
    
    /**
     * Returns a text from database. Returns a default string if not found.
     * 
     * @param key
     * a text key
     * @return text from the database or a debug contents
     */
    public String getText(final TextType key) {
        return this.getText(key, key.getDefaultValue());
    }
    
    /**
     * Returns a text from database. If the text is not found, returns
     * <code>nothing</code> string. This <code>nothing</code> string should
     * contain either empty string or some user-friendly warning in HTML.
     * 
     * @param key
     * a text key
     * @param nothing
     * string returned when no record is found in the database
     * @return text from the database or <code>nothing</code> string
     */
    private String getText(final TextType key, final String nothing) {
        TextRepository.LOG.debug(String.format("Loading text with key '%s'...", key.name()));
        
        try {
            final BasicDocument doc = this.loadDocument(BasicDocument.class, TextRepository.getTextDocumentId(key));
            final Object value = doc.getProperty(TextRepository.TEXT_PROPERTY_KEY);
            return String.valueOf(value);
        } catch (final NotFoundRepositoryException x) {
            TextRepository.LOG.debug("Text not found.");
        } catch (final GeneralRepositoryException x) {
            TextRepository.LOG.warn("Error while loading text.");
        }
        
        return nothing;
    }
    
    /**
     * Returns a new text document with the text specified.
     * 
     * @param key
     * text key
     * @param text
     * text to be placed in the document
     * @return new text document
     */
    private static StandaloneDocument createTextDocument(final TextType key, final String text) {
        final StandaloneDocument document = new StandaloneDocument();
        document.setId(TextRepository.getTextDocumentId(key));
        document.setProperty(TextRepository.TEXT_PROPERTY_KEY, text);
        return document;
    }
    
    /**
     * Returns an ID for a document that contains the given text. All text
     * related documents have the same prefix.
     * 
     * @param key
     * a text key
     * @return document ID
     */
    private static String getTextDocumentId(final TextType key) {
        return TextRepository.TEXT_PROPERTY_KEY + "_" + key.name();
    }
    
    @Override
    public void createStartupDocuments() throws GeneralRepositoryException {
        TextRepository.LOG.info("Initializing texts in the database...");
        
        for (final TextType type : TextType.values()) {
            this.createDocument(TextRepository.createTextDocument(type, type.getDefaultValue()));
        }
    }
}
