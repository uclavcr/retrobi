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

package cz.insophy.retrobi.utils.library;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.View;
import org.jcouchdb.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.AbstractProtoRepository;
import cz.insophy.retrobi.database.document.BasicDocument;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * A class that manages design document for views. This document is only one for
 * all repositories to simplify the indexing process.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleDatabaseUtils {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDatabaseUtils.class);
    
    /**
     * Ensures that the central view design document exists and it is filled
     * with all necessary views from the given repositories.
     * 
     * @param database
     * database object
     * @param repositories
     * repositories available
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public static void ensureDesignDocument(final Database database, final AbstractProtoRepository... repositories) throws GeneralRepositoryException {
        // delete old design document
        
        final boolean ddExisted = SimpleDatabaseUtils.deleteDesignDocument(database);
        
        // create a new design document
        
        final DesignDocument document = SimpleDatabaseUtils.createDesignDocument();
        
        for (final AbstractProtoRepository repository : repositories) {
            SimpleDatabaseUtils.addViewsToDocument(repository, document);
            
            if (!ddExisted) {
                // initialize startup documents
                
                repository.createStartupDocuments();
            }
        }
        
        // save the new design document
        
        SimpleDatabaseUtils.saveDesignDocument(database, document);
    }
    
    /**
     * Creates a new empty design document.
     * 
     * @return a new design document
     */
    private static DesignDocument createDesignDocument() {
        return new DesignDocument(Settings.VIEW_DOCUMENT_ID);
    }
    
    /**
     * Saves the design document.
     * 
     * @param database
     * database object
     * @param document
     * a design document to save
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private static void saveDesignDocument(final Database database, final DesignDocument document) throws GeneralRepositoryException {
        try {
            database.createDocument(document);
        } catch (final RuntimeException x) {
            throw new GeneralRepositoryException(x.getMessage(), x);
        }
    }
    
    /**
     * Deletes the existing design document.
     * 
     * @param database
     * database object
     * @return <code>true</code> if the design document existed and was deleted,
     * <code>false</code> otherwise
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private static boolean deleteDesignDocument(final Database database) throws GeneralRepositoryException {
        try {
            final DesignDocument oldDocument = database.getDesignDocument(Settings.VIEW_DOCUMENT_ID);
            database.delete(oldDocument);
            return true;
        } catch (final NotFoundException x) {
            return false;
        } catch (final RuntimeException x) {
            throw new GeneralRepositoryException(x.getMessage(), x);
        }
    }
    
    /**
     * Adds views to the design document. The views are extracted from the
     * repository provided.
     * 
     * @param source
     * a repository to take the views from
     * @param target
     * target design document
     */
    private static void addViewsToDocument(final AbstractProtoRepository source, final DesignDocument target) {
        final Map<String, View> views = source.createViews();
        
        if (views != null) {
            for (final Entry<String, View> view : source.createViews().entrySet()) {
                final String viewName = SimpleDatabaseUtils.extendViewName(source, view.getKey());
                SimpleDatabaseUtils.LOG.info(String.format("Adding view '%s' to design document '%s'...", viewName, target.getId()));
                target.addView(viewName, view.getValue());
            }
        }
    }
    
    /**
     * Returns the full view name.
     * 
     * @param designDocumentId
     * design document ID
     * @param shortViewName
     * short view name
     * @return full view name
     */
    public static String getFullViewName(final String designDocumentId, final String shortViewName) {
        if (SimpleStringUtils.isEmpty(designDocumentId)) {
            throw new IllegalArgumentException("Design document ID must be not empty.");
        }
        
        if (SimpleStringUtils.isEmpty(shortViewName)) {
            throw new IllegalArgumentException("Short view name must be not empty.");
        }
        
        return designDocumentId + "/" + shortViewName;
    }
    
    /**
     * Returns the full view name.
     * 
     * @param repository
     * repository
     * @param shortViewName
     * short view name
     * @return full view name
     */
    public static String getFullViewName(final AbstractProtoRepository repository, final String shortViewName) {
        return Settings.VIEW_DOCUMENT_ID + "/" + SimpleDatabaseUtils.extendViewName(repository, shortViewName);
    }
    
    /**
     * Trims the common design document prefix for use in views.
     * 
     * @param id
     * design document ID
     * @return design document ID with no common prefix
     */
    public static String getViewNameNoPrefix(final String id) {
        if (id.startsWith("_design/")) {
            return id.substring(8);
        }
        
        return id;
    }
    
    /**
     * This method pings all views in the specified design document.
     * 
     * @param database
     * database object
     * @param designDocumentId
     * design document ID
     */
    public static void pingViewsOfDesignDocument(final Database database, final String designDocumentId) {
        SimpleDatabaseUtils.LOG.info("Pinging all views in document: " + designDocumentId);
        
        if (!designDocumentId.startsWith("_design/")) {
            throw new IllegalArgumentException();
        }
        
        SimpleDatabaseUtils.pingViews(database, SimpleDatabaseUtils.getViews(database, designDocumentId));
        SimpleDatabaseUtils.LOG.info("Done.");
    }
    
    /**
     * Loads all view names defined in the specified design document.
     * 
     * @param database
     * database object
     * @param designDocumentId
     * design document ID
     * @return a collection of view names (starting with the document ID)
     */
    private static Collection<String> getViews(final Database database, final String designDocumentId) {
        final Collection<String> views = new LinkedHashSet<String>();
        
        SimpleDatabaseUtils.LOG.debug("Loading view design document: " + designDocumentId);
        
        final DesignDocument document = database.getDesignDocument(designDocumentId);
        
        for (final String view : document.getViews().keySet()) {
            views.add(document.getId() + "/" + view);
        }
        
        SimpleDatabaseUtils.LOG.debug("Views found: " + views.size());
        
        return Collections.unmodifiableCollection(views);
    }
    
    /**
     * Sends a ping to all the views in the specified collection. The query is
     * limited to 0 rows, so it is basically a PING that does nothing but causes
     * views to update, which is important.
     * 
     * @param database
     * database object
     * @param viewNames
     * collection of view names
     */
    private static void pingViews(final Database database, final Collection<String> viewNames) {
        int current = 1;
        final int total = viewNames.size();
        
        for (final String viewName : viewNames) {
            // fix the view name
            
            final String viewNameFixed = SimpleDatabaseUtils.getViewNameNoPrefix(viewName);
            SimpleDatabaseUtils.LOG.debug(String.format("PING %d/%d: %s", current, total, viewNameFixed));
            
            try {
                // execute the query with limit = 0
                
                database.queryView(viewNameFixed, BasicDocument.class, new Options().limit(0), null);
            } catch (final RuntimeException x) {
                SimpleDatabaseUtils.LOG.error(x.getMessage(), x);
            } finally {
                current++;
            }
        }
    }
    
    /**
     * Extends the short view name by the repository prefix.
     * 
     * @param repository
     * repository
     * @param shortViewName
     * short view name
     * @return view name extended by the repository prefix
     */
    private static String extendViewName(final AbstractProtoRepository repository, final String shortViewName) {
        if (SimpleStringUtils.isEmpty(shortViewName)) {
            throw new IllegalArgumentException("Short view name to extend must be not empty.");
        }
        
        return repository.getClass().getSimpleName() + "_" + shortViewName;
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleDatabaseUtils() {
        throw new UnsupportedOperationException();
    }
}
