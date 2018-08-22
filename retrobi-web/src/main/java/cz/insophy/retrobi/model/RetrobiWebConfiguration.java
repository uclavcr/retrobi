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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiLocker;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.database.entity.type.CardIndexInfo;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.library.DefaultAttributePrototyper;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;
import cz.insophy.retrobi.utils.library.SimpleIndexUtils;

/**
 * Class that contains web application configuration - index list, index names
 * and the attribute tree. All of these can be reloaded from configuration files
 * at any time (during runtime).
 * 
 * @author Vojtěch Hordějčuk
 */
public class RetrobiWebConfiguration {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(RetrobiWebConfiguration.class);
    /**
     * singleton instance of this class
     */
    private static RetrobiWebConfiguration singleton = null;
    /**
     * attribute prototype tree root
     */
    private AttributePrototype root;
    /**
     * card indexes
     */
    private List<AbstractCardIndex> indexes;
    /**
     * the default card index
     */
    private AbstractCardIndex defaultIndex;
    
    /**
     * Returns the singleton instance of this class.
     * 
     * @return the singleton instance of this class
     */
    public synchronized static RetrobiWebConfiguration getInstance() {
        if (RetrobiWebConfiguration.singleton == null) {
            RetrobiWebConfiguration.LOG.debug("Creating singleton...");
            RetrobiWebConfiguration.singleton = new RetrobiWebConfiguration();
            RetrobiWebConfiguration.LOG.debug("Loading configuration...");
            RetrobiWebConfiguration.singleton.reloadAttributeTreeNoError();
            RetrobiWebConfiguration.LOG.debug("Singleton ready.");
        }
        
        return RetrobiWebConfiguration.singleton;
    }
    
    /**
     * Creates a new instance.
     */
    private RetrobiWebConfiguration() {
        this.root = null;
        this.indexes = null;
        this.defaultIndex = null;
    }
    
    /**
     * Returns the attribute prototype tree root. If the tree is not created
     * yet, it is lazily initialized.
     * 
     * @return the attribute prototype tree root
     */
    public AttributePrototype getAttributeRoot() {
        RetrobiLocker.ATTRIBUTE_LOCK.lock();
        
        try {
            return this.root;
        } finally {
            RetrobiLocker.ATTRIBUTE_LOCK.unlock();
        }
    }
    
    /**
     * Returns the list of all available indexes.
     * 
     * @return the list of indexes
     */
    public List<AbstractCardIndex> getIndexes() {
        RetrobiLocker.INDEX_LOCK.lock();
        
        try {
            if (this.indexes == null) {
                return Collections.emptyList();
            }
            
            return Collections.unmodifiableList(this.indexes);
        } finally {
            RetrobiLocker.INDEX_LOCK.unlock();
        }
    }
    
    /**
     * Returns the default index.
     * 
     * @return the default index
     */
    public AbstractCardIndex getDefaultIndex() {
        RetrobiLocker.INDEX_LOCK.lock();
        
        try {
            return this.defaultIndex;
        } finally {
            RetrobiLocker.INDEX_LOCK.unlock();
        }
    }
    
    /**
     * Updates all design documents to be up-to-date with the loaded
     * configuration. This includes the view definition for attribute values and
     * index definition.
     * 
     * @param replace
     * replace existing documents
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void updateDesignDocuments(final boolean replace) throws GeneralRepositoryException {
        RetrobiApplication.db().getAnalystRepository().updateAttributeViews(replace, RetrobiWebConfiguration.getInstance().getAttributeRoot());
        RetrobiApplication.db().getCardSearchRepository().updateIndexDocument(replace, RetrobiWebConfiguration.getInstance().getIndexes());
    }
    
    /**
     * Reloads the attribute tree. Causes no error (only logging it).
     */
    private void reloadAttributeTreeNoError() {
        try {
            this.reloadAttributeTree();
        } catch (final Exception x) {
            RetrobiWebConfiguration.LOG.warn(x.getMessage());
        } finally {
            this.ensureDefaultData();
        }
    }
    
    /**
     * Ensures that the non-existing data are filled with default values.
     */
    private void ensureDefaultData() {
        RetrobiLocker.ATTRIBUTE_LOCK.lock();
        
        try {
            if (this.root == null) {
                RetrobiWebConfiguration.LOG.debug("No attribute, reset to default");
                this.root = DefaultAttributePrototyper.root();
            }
        } finally {
            RetrobiLocker.ATTRIBUTE_LOCK.unlock();
        }
        
        RetrobiLocker.INDEX_LOCK.lock();
        
        try {
            if (this.indexes == null) {
                RetrobiWebConfiguration.LOG.debug("No indexes, reset to empty.");
                this.defaultIndex = null;
                this.indexes = Collections.emptyList();
            }
        } finally {
            RetrobiLocker.INDEX_LOCK.unlock();
        }
    }
    
    /**
     * Reloads the attribute tree from file. If the file is not found, it is
     * created and filled with the default content.
     * 
     * @throws IOException
     * I/O exception
     */
    public void reloadAttributeTree() throws IOException {
        RetrobiWebConfiguration.LOG.debug("Reloading attribute tree...");
        
        RetrobiLocker.ATTRIBUTE_LOCK.lock();
        
        try {
            // first, try to load the attribute tree from a file
            
            this.root = SimpleAttributeUtils.loadDefinitions(Settings.ATTRIBUTE_DEFINITION_FILE);
            
            if (this.root == null) {
                // nothing loaded, create default tree and save it
                
                RetrobiWebConfiguration.LOG.debug("Default attribute tree will be saved.");
                this.root = DefaultAttributePrototyper.root();
                SimpleAttributeUtils.saveDefinitions(this.root, Settings.ATTRIBUTE_DEFINITION_FILE);
            } else {
                // tree loaded successfully
                
                RetrobiWebConfiguration.LOG.debug("Attribute tree loaded from file.");
            }
        } finally {
            RetrobiLocker.ATTRIBUTE_LOCK.unlock();
        }
        
        RetrobiWebConfiguration.LOG.debug("Attribute tree ready.");
        
        // reloading indexes
        
        this.reloadIndexes();
    }
    
    /**
     * Reloads the indexes from file. If the file is not found, it is created
     * and filled with the default content.
     * 
     * @throws IOException
     * I/O exception
     */
    public void reloadIndexes() throws IOException {
        RetrobiWebConfiguration.LOG.debug("Reloading index list...");
        
        RetrobiLocker.INDEX_LOCK.lock();
        
        try {
            // load indexes
            
            this.indexes = SimpleIndexUtils.getIndexes(this.getAttributeRoot());
            
            // find the default index
            
            this.defaultIndex = null;
            
            for (final AbstractCardIndex index : this.indexes) {
                RetrobiWebConfiguration.LOG.debug("Evaluating index: " + index.getName());
                
                if (index.getName().equals(Settings.DEFAULT_INDEX_NAME)) {
                    RetrobiWebConfiguration.LOG.debug("Default index found: " + index.toString());
                    this.defaultIndex = index;
                    break;
                }
            }
            
            RetrobiWebConfiguration.LOG.debug("Index list prepared.");
            RetrobiWebConfiguration.LOG.debug("Default index: " + this.defaultIndex);
        } finally {
            RetrobiLocker.INDEX_LOCK.unlock();
        }
        
        // reload index information too
        
        this.updateIndexInfo();
        
        // need to resort the indexes
        
        final List<AbstractCardIndex> sortedIndexes = new ArrayList<AbstractCardIndex>(this.indexes);
        Collections.sort(sortedIndexes);
        this.indexes = Collections.unmodifiableList(sortedIndexes);
    }
    
    /**
     * Reloads the index information. The index information includes index
     * naming, minimal user role for viewing, etc.
     * 
     * @throws IOException
     * I/O exception
     */
    private void updateIndexInfo() throws IOException {
        RetrobiWebConfiguration.LOG.debug("Reloading index information...");
        
        Collection<CardIndexInfo> data = null;
        
        // first, try to load the attribute tree from a file
        
        data = SimpleAttributeUtils.loadIndexInfo(Settings.INDEX_NAMING_FILE);
        
        if (data == null) {
            // nothing loaded, create default tree and save it
            
            RetrobiWebConfiguration.LOG.debug("Default index naming will be saved.");
            data = DefaultAttributePrototyper.info();
            SimpleAttributeUtils.saveIndexInfo(data, Settings.INDEX_NAMING_FILE);
        } else {
            // tree loaded successfully
            
            RetrobiWebConfiguration.LOG.debug("Attribute tree loaded from file.");
        }
        
        // update indexes (if possible)
        
        if (data != null) {
            for (final CardIndexInfo info : data) {
                this.updateIndex(info);
            }
        }
        
        RetrobiWebConfiguration.LOG.debug("Index information loaded.");
    }
    
    /**
     * Finds and updates the correct index with the information provided.
     * 
     * @param info
     * information to be put into the correct index
     */
    private void updateIndex(final CardIndexInfo info) {
        RetrobiWebConfiguration.LOG.debug("Searching for index update: " + info.toString());
        
        RetrobiLocker.INDEX_LOCK.lock();
        
        try {
            for (final AbstractCardIndex index : this.indexes) {
                if (index.getName().equals(info.getName())) {
                    RetrobiWebConfiguration.LOG.debug(String.format(
                            "Updating index '%s' with title '%s' and role '%s'...",
                            index.toString(),
                            info.getTitle(),
                            info.getRole().name()));
                    
                    index.setRole(info.getRole());
                    index.setTitle("Pole: " + info.getTitle());
                    index.setOrder(info.getOrder());
                }
            }
        } finally {
            RetrobiLocker.INDEX_LOCK.unlock();
        }
    }
}
