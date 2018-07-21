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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * Abstract base class for all repositories. Each subclass can override its
 * methods to create its own design document and/or startup documents after a
 * new empty database is created. This class also contains useful basic methods
 * for manipulating database documents and their attachments - loading,
 * creating, updating and deleting.
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractRepository extends AbstractProtoRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRepository.class);
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * <code>jcouchdb</code> database layer
     */
    protected AbstractRepository(final Database database) {
        super(database);
    }
    
    // =================
    // DOCUMENT FETCHING
    // =================
    
    /**
     * Loads documents by their IDs.
     * 
     * @param <E>
     * static class information
     * @param type
     * runtime class information
     * @param documentIds
     * document ID list
     * @return list of loaded entity beans
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found repository exception
     */
    protected <E extends StandaloneDocument> List<E> loadDocuments(final Class<E> type, final List<String> documentIds) throws NotFoundRepositoryException, GeneralRepositoryException {
        if (documentIds == null) {
            AbstractRepository.LOG.error("Cannot load documents with IDs = NULL.");
            throw new NullPointerException();
        }
        
        if (documentIds.size() < 1) {
            AbstractRepository.LOG.debug("Document ID list is empty, returning empty list of documents.");
            return Collections.emptyList();
        }
        
        AbstractRepository.LOG.debug(String.format("Loading %d document(s) by their IDs...", documentIds.size()));
        final List<E> result = new ArrayList<E>(documentIds.size());
        
        // load each document from the database
        
        for (final String documentId : documentIds) {
            result.add(this.loadDocument(type, documentId));
        }
        
        AbstractRepository.LOG.debug("Documents loaded successfully.");
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Helper method for common loading of document IDs. The view must return
     * document IDs as value. Key does not matter.
     * 
     * @param viewName
     * view name
     * @param options
     * options to be used in query
     * @return list of document IDs
     * @throws GeneralRepositoryException
     * general repository exception
     */
    protected List<String> loadDocumentIds(final String viewName, final Options options) throws GeneralRepositoryException {
        // run the query
        
        final ViewResult<String> result = this.queryView(viewName, String.class, options);
        
        // process results
        
        final List<String> ids = new ArrayList<String>(32);
        
        for (final ValueRow<String> row : result.getRows()) {
            ids.add(row.getValue());
        }
        
        // return results
        
        return Collections.unmodifiableList(ids);
    }
    
    // ==============
    // PAGED FETCHING
    // ==============
    
    /**
     * Loads paged document IDs and a number of pages. Each of the information
     * is fetched from a different view. The view with document IDs must return
     * document IDs as values, while the view with count must return count as
     * value (should use <code>_count</code> or similar as a reduce function).
     * The start key and end key from the provided options is used.
     * 
     * @param docView
     * name of the view with document IDs (the view must return IDs as values)
     * @param countView
     * page count view name with (the view must return count as value)
     * @param startKey
     * start key (or <code>null</code>)
     * @param endKey
     * end key (or <code>null</code>)
     * @param descending
     * descending flag
     * @param page
     * page number (0..N) - influences the offset
     * @param limit
     * limit (number of rows per page) - influences the row count
     * @return a tuple (page count, list of rows)
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws GeneralRepositoryException
     * general exception
     */
    protected Tuple<Integer, List<String>> loadPagedDocumentIds(final String docView, final String countView, final Object startKey, final Object endKey, final boolean descending, final int page, final int limit) throws NotFoundRepositoryException, GeneralRepositoryException {
        if ((page < 0) || (limit <= 0)) {
            throw new IllegalArgumentException("Stránka musí být nulová či kladná, limit musí být nenulový a kladný.");
        }
        
        AbstractRepository.LOG.debug(String.format("Loading paged documents on page %d (limit %d)...", page, limit));
        
        // get the page count
        
        final int pageCount = this.loadPageCount(
                countView,
                startKey,
                endKey,
                descending,
                limit);
        
        // fix the page number
        
        final int fixedPage = SimpleGeneralUtils.limit(page, 0, pageCount - 1);
        
        // get the documents
        
        final Options options = new Options().descending(descending).skip(fixedPage * limit).limit(limit);
        
        if (startKey != null) {
            options.startKey(startKey);
        }
        
        if (endKey != null) {
            options.endKey(endKey);
        }
        
        final List<String> ids = this.loadDocumentIds(docView, options);
        
        // create the result tuple
        
        return Tuple.of(pageCount, Collections.unmodifiableList(ids));
    }
    
    /**
     * Returns the page count. The page count is based on the given limit
     * (number of items per page) and a number of rows in the specified view.
     * The view must use <code>_count</code> or similar reduce function.
     * 
     * @param countView
     * name of the view with the count
     * @param startKey
     * start key (or <code>null</code>)
     * @param endKey
     * end key (or <code>null</code>)
     * @param descending
     * descending flag
     * @param limit
     * limit (number of items per page)
     * @return number of pages
     * @throws GeneralRepositoryException
     * general exception
     */
    private int loadPageCount(final String countView, final Object startKey, final Object endKey, final boolean descending, final int limit) throws GeneralRepositoryException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit musí být nenulový a kladný.");
        }
        
        // prepare options
        
        final Options options = new Options().descending(descending);
        
        if (startKey != null) {
            AbstractRepository.LOG.debug("Start key: " + startKey);
            options.startKey(startKey);
        }
        
        if (endKey != null) {
            AbstractRepository.LOG.debug("End key: " + endKey);
            options.endKey(endKey);
        }
        
        // get the result
        
        final ViewResult<Integer> countResult = this.queryView(countView, Integer.class, options);
        
        // extract page count from the result and return it
        
        int resultCount = 0;
        
        if (countResult.getRows().size() == 1) {
            resultCount = countResult.getRows().get(0).getValue();
        } else {
            AbstractRepository.LOG.warn("Expected 1 result only, was: " + countResult.getRows().size());
        }
        
        final double dResultCount = resultCount;
        final double dLimit = limit;
        
        AbstractRepository.LOG.debug("Resulting count: " + resultCount);
        
        return (int) Math.ceil(dResultCount / dLimit);
    }
}
