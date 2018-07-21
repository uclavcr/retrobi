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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.db.Response;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.jcouchdb.exception.NotFoundException;
import org.jcouchdb.exception.UpdateConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleDatabaseUtils;

/**
 * Abstract base class for all repositories. Contains basic algorithms and
 * support methods for handling documents.
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractProtoRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtoRepository.class);
    /**
     * instance of <code>jcouchdb</code> database layer
     */
    private final Database database;
    /**
     * database locker
     */
    private final ReentrantLock databaseLock;
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database connector instance
     */
    protected AbstractProtoRepository(final Database database) {
        this.database = database;
        this.databaseLock = new ReentrantLock();
    }
    
    // =========
    // DOCUMENTS
    // =========
    
    /**
     * Loads document by its ID.
     * 
     * @param <E>
     * static class information
     * @param type
     * runtime class information
     * @param documentId
     * document ID
     * @return loaded entity bean
     * @throws NotFoundRepositoryException
     * not found repository exception
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected <E> E loadDocument(final Class<E> type, final String documentId) throws NotFoundRepositoryException, GeneralRepositoryException {
        if (documentId == null) {
            AbstractProtoRepository.LOG.error("Cannot load document with ID = NULL.");
            throw new NullPointerException();
        }
        
        this.databaseLock.lock();
        
        try {
            // get document from the database
            
            return this.database.getDocument(type, documentId);
        } catch (final NotFoundException x) {
            AbstractProtoRepository.LOG.error("Document not found: " + documentId);
            throw new NotFoundRepositoryException(documentId, x);
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Could not load document: " + x.getMessage());
            throw new GeneralRepositoryException(String.format("Obecná chyba při načítání dokumentu [ID = %s]: %s", documentId, x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Creates a new document in the database.
     * 
     * @param document
     * document bean
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected void createDocument(final Object document) throws GeneralRepositoryException {
        this.databaseLock.lock();
        
        try {
            // create a document in the database
            
            AbstractProtoRepository.LOG.debug("Creating document...");
            this.database.createDocument(document);
            AbstractProtoRepository.LOG.debug("Document created.");
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Error creating document: " + x.getMessage());
            throw new GeneralRepositoryException(String.format("Chyba při vytváření dokumentu: %s", x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Updates an existing document in the database. Checks for update conflicts
     * and fails, if occurred.
     * 
     * @param document
     * document bean
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected void updateDocument(final Object document) throws GeneralRepositoryException {
        this.databaseLock.lock();
        
        try {
            // update the document in the database
            
            AbstractProtoRepository.LOG.debug("Updating document...");
            this.database.updateDocument(document);
            AbstractProtoRepository.LOG.debug("Document updated.");
        } catch (final UpdateConflictException x) {
            AbstractProtoRepository.LOG.warn(String.format("Update conflict of document: " + document.toString()));
            throw new GeneralRepositoryException(String.format("Váš dokument již není aktuální, opakujte prosím operaci. Dokument: " + document.toString()), x);
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Error updating document: " + x.getMessage());
            throw new GeneralRepositoryException(String.format("Chyba při aktualizaci dokumentu: %s", x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Deletes a document.
     * 
     * @param document
     * document bean
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected void deleteDocument(final Object document) throws GeneralRepositoryException {
        this.databaseLock.lock();
        
        try {
            // remove the document from the database
            
            AbstractProtoRepository.LOG.debug("Deleting document...");
            this.database.delete(document);
            AbstractProtoRepository.LOG.debug("Document deleted.");
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Error deleting document: " + x.getMessage());
            throw new GeneralRepositoryException(String.format("Chyba při mazání dokumentu: %s", x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    // ===========
    // ATTACHMENTS
    // ===========
    
    /**
     * Returns an input stream of the given attachment. Please do not forget to
     * close the stream after use (via <code>close()</code>) method.
     * 
     * @param documentId
     * document ID
     * @param attachmentName
     * attachment name
     * @return an input stream of the attachment (close it after use)
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found repository exception
     */
    protected InputStream getAttachmentResponse(final String documentId, final String attachmentName) throws GeneralRepositoryException, NotFoundRepositoryException {
        this.databaseLock.lock();
        
        try {
            // get the attachment response
            
            final byte[] data = this.database.getAttachment(documentId, attachmentName);
            return new ByteArrayInputStream(data);
        } catch (final NotFoundException x) {
            AbstractProtoRepository.LOG.error("Attachment not found: " + attachmentName);
            throw new NotFoundRepositoryException(documentId, attachmentName, x);
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Error getting attachment response: " + attachmentName);
            throw new GeneralRepositoryException(String.format("Chyba při načítání přílohy: %s", x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Creates an attachment, then updates the document revision.
     * 
     * @param document
     * target document
     * @param name
     * attachment name
     * @param mime
     * MIME type
     * @param stream
     * stream to read from (not closed here)
     * @param length
     * attachment stream length
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected void createAttachment(final StandaloneDocument document, final String name, final String mime, final InputStream stream, final long length) throws GeneralRepositoryException {
        this.databaseLock.lock();
        
        try {
            // create attachment
            
            AbstractProtoRepository.LOG.debug("Creating attachment...");
            final String newRevision = this.database.createAttachment(document.getId(), document.getRevision(), name, mime, stream, length);
            document.setRevision(newRevision);
            AbstractProtoRepository.LOG.debug("Attachment created.");
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Error creating attachment: " + name);
            throw new GeneralRepositoryException(String.format("Chyba při vytváření přílohy: %s", x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Deletes an attachment, then updates the document revision.
     * 
     * @param document
     * target document
     * @param name
     * attachment name to delete
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected void deleteAttachment(final StandaloneDocument document, final String name) throws GeneralRepositoryException {
        this.databaseLock.lock();
        
        try {
            // delete the attachment
            
            AbstractProtoRepository.LOG.debug("Deleting attachment...");
            final String newRevision = this.database.deleteAttachment(document.getId(), document.getRevision(), name);
            document.setRevision(newRevision);
            AbstractProtoRepository.LOG.debug("Attachment deleted.");
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error("Error deleting attachment: " + name);
            throw new GeneralRepositoryException(String.format("Chyba při mazání přílohy: %s", x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    // =====
    // VIEWS
    // =====
    
    /**
     * Sends a query to the view and returns the result.
     * 
     * @param <E>
     * static result value class information
     * @param shortViewName
     * short view name
     * @param type
     * runtime result value class information
     * @param options
     * view options
     * @return view result
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected <E> ViewResult<E> queryView(final String shortViewName, final Class<E> type, final Options options) throws GeneralRepositoryException {
        return this.queryCustomView(SimpleDatabaseUtils.getFullViewName(this, shortViewName), type, options);
    }
    
    /**
     * Sends a query to the view and returns the result.
     * 
     * @param <E>
     * static result value class information
     * @param designDocumentId
     * design document ID
     * @param shortViewName
     * short view name
     * @param type
     * runtime result value class information
     * @param options
     * view options
     * @return view result
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected <E> ViewResult<E> queryView(final String designDocumentId, final String shortViewName, final Class<E> type, final Options options) throws GeneralRepositoryException {
        return this.queryCustomView(SimpleDatabaseUtils.getFullViewName(designDocumentId, shortViewName), type, options);
    }
    
    /**
     * Internal method for querying a view.
     * 
     * @param <E>
     * static result value class information
     * @param fullViewName
     * short view name (with the design document prefix)
     * @param type
     * runtime result value class information
     * @param options
     * view options
     * @return view result
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private <E> ViewResult<E> queryCustomView(final String fullViewName, final Class<E> type, final Options options) throws GeneralRepositoryException {
        AbstractProtoRepository.LOG.debug(String.format("Querying view '%s' with query '%s' for value class '%s'...", fullViewName, options.toQuery(), type.getSimpleName()));
        
        this.databaseLock.lock();
        
        try {
            // send a query to the view
            
            final ViewResult<E> result;
            result = this.database.queryView(SimpleDatabaseUtils.getViewNameNoPrefix(fullViewName), type, options, null);
            AbstractProtoRepository.LOG.debug(String.format("%d row(s) ready in the result (view size = %d).", result.getRows().size(), result.getTotalRows()));
            return result;
        } catch (final Exception x) {
            AbstractProtoRepository.LOG.error(String.format("Error querying view '%s'.", fullViewName));
            throw new GeneralRepositoryException(String.format("Chyba při provádění databázového dotazu na pohled '%s': %s", fullViewName, x.getMessage()), x);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    // ==============
    // INITIALIZATION
    // ==============
    
    /**
     * Creates views for this design document.
     * 
     * @return map of views
     */
    public Map<String, View> createViews() {
        AbstractProtoRepository.LOG.info("No views will be created.");
        return null;
    }
    
    /**
     * Creates a startup documents. These documents are created if a design
     * document does not exist and it is created. This situation probably occurs
     * only during a new installation.
     * 
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void createStartupDocuments() throws GeneralRepositoryException {
        AbstractProtoRepository.LOG.info("No startup documents created.");
    }
    
    // =======
    // UTILITY
    // =======
    
    /**
     * Returns the server response for a GET request.
     * 
     * @param url
     * request URL (will be send as GET)
     * @return response for the given GET request
     */
    protected Response getGetResponse(final String url) {
        AbstractProtoRepository.LOG.debug(String.format("Getting a server response for GET: %s", url));
        
        this.databaseLock.lock();
        
        try {
            return this.database.getServer().get(url);
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Returns the server response for a POST request.
     * 
     * @param url
     * request URL (will be sent as POST)
     * @return response for the given POST request
     */
    protected Response getPostResponse(final String url) {
        AbstractProtoRepository.LOG.debug(String.format("Getting a server response for POST: %s", url));
        
        this.databaseLock.lock();
        
        try {
            return this.database.getServer().post(url, "");
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Returns the database name.
     * 
     * @return database name
     */
    protected String getDatabaseName() {
        this.databaseLock.lock();
        
        try {
            return this.database.getName();
        } finally {
            this.databaseLock.unlock();
        }
    }
    
    /**
     * Returns a view that uses built-in reduce function <code>_count</code>.
     * This reduce function counts a number of rows. The view should be used
     * with <code>group = false</code> and <code>reduce = true</code> settings.
     * The map function is preserved from the original view.
     * 
     * @param originalView
     * the original view
     * @return a new view with a counting reduce function
     */
    protected static View createCountView(final View originalView) {
        return new View(originalView.getMap(), "_count");
    }
}
