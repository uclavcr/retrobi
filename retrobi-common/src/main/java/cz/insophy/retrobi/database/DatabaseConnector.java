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

import org.jcouchdb.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleDatabaseUtils;

/**
 * Class for managing database connection and getting repositories.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DatabaseConnector {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConnector.class);
    /**
     * singleton instance
     */
    private static DatabaseConnector singleton = null;
    /**
     * JCouchDB database layer
     */
    private final Database database;
    /**
     * card repository instance
     */
    private final CardRepository cardRepository;
    /**
     * card search repository instance
     */
    private final CardSearchRepository cardSearchRepository;
    /**
     * card image repository instance
     */
    private final CardImageRepository cardImageRepository;
    /**
     * card set repository instance
     */
    private final CardsetRepository cardsetRepository;
    /**
     * user repository instance
     */
    private final UserRepository userRepository;
    /**
     * comment repository instance
     */
    private final CommentRepository commentRepository;
    /**
     * message repository instance
     */
    private final MessageRepository messageRepository;
    /**
     * text repository instance
     */
    private final TextRepository textRepository;
    /**
     * analyst repository instance
     */
    private final AnalystRepository analystRepository;
    
    /**
     * Returns the singleton instance of this class.
     * 
     * @return a singleton instance
     */
    public synchronized static DatabaseConnector getInstance() {
        if (DatabaseConnector.singleton == null) {
            try {
                DatabaseConnector.singleton = new DatabaseConnector();
            } catch (final GeneralRepositoryException x) {
                DatabaseConnector.LOG.error(x.getMessage());
                throw new IllegalStateException();
            }
        }
        
        return DatabaseConnector.singleton;
    }
    
    /**
     * Creates a new instance.
     * 
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private DatabaseConnector() throws GeneralRepositoryException {
        // create database
        
        DatabaseConnector.LOG.debug("Creating database...");
        this.database = new Database(Settings.DB_HOST, Settings.DB_PORT, Settings.DB_NAME);
        DatabaseConnector.LOG.debug("Database created.");
        
        // create repositories
        
        DatabaseConnector.LOG.debug("Creating repositories...");
        
        this.cardRepository = new CardRepository(this.database);
        this.cardSearchRepository = new CardSearchRepository(this.database);
        this.cardImageRepository = new CardImageRepository(this.database);
        this.cardsetRepository = new CardsetRepository(this.database);
        this.userRepository = new UserRepository(this.database);
        this.commentRepository = new CommentRepository(this.database);
        this.messageRepository = new MessageRepository(this.database);
        this.textRepository = new TextRepository(this.database);
        this.analystRepository = new AnalystRepository(this.database);
        
        DatabaseConnector.LOG.debug("Repositories ready.");
        
        // connect to the database (and create if necessary)
        
        DatabaseConnector.LOG.debug("Connecting to the database...");
        
        if (this.database.getServer().createDatabase(Settings.DB_NAME)) {
            DatabaseConnector.LOG.info("The empty database was created.");
        }
        
        // prepare design documents
        
        SimpleDatabaseUtils.ensureDesignDocument(
                this.database,
                this.cardRepository,
                this.cardSearchRepository,
                this.cardImageRepository,
                this.cardsetRepository,
                this.userRepository,
                this.commentRepository,
                this.messageRepository,
                this.textRepository,
                this.analystRepository);
    }
    
    /**
     * Sends PING all the views in database. PING here means to send a query to
     * a view, limited to one result only. This causes database view indexer to
     * be awaken and start processing document changes. This should prevent
     * latter delays while processing views lazily (on user demand).
     */
    public void pingAllViews() {
        SimpleDatabaseUtils.pingViewsOfDesignDocument(this.database, Settings.VIEW_DOCUMENT_ID);
        SimpleDatabaseUtils.pingViewsOfDesignDocument(this.database, Settings.VALUE_INDEX_DOCUMENT_ID);
    }
    
    /**
     * Returns the card repository instance.
     * 
     * @return repository instance
     */
    public CardRepository getCardRepository() {
        return this.cardRepository;
    }
    
    /**
     * Returns the card search repository instance.
     * 
     * @return repository instance
     */
    public CardSearchRepository getCardSearchRepository() {
        return this.cardSearchRepository;
    }
    
    /**
     * Returns the card image repository instance.
     * 
     * @return repository instance
     */
    public CardImageRepository getCardImageRepository() {
        return this.cardImageRepository;
    }
    
    /**
     * Returns the card set repository instance.
     * 
     * @return repository instance
     */
    public CardsetRepository getCardsetRepository() {
        return this.cardsetRepository;
    }
    
    /**
     * Returns the user repository instance.
     * 
     * @return repository instance
     */
    public UserRepository getUserRepository() {
        return this.userRepository;
    }
    
    /**
     * Returns the comment repository instance.
     * 
     * @return repository instance
     */
    public CommentRepository getCommentRepository() {
        return this.commentRepository;
    }
    
    /**
     * Returns the message repository instance.
     * 
     * @return repository instance
     */
    public MessageRepository getMessageRepository() {
        return this.messageRepository;
    }
    
    /**
     * Returns the text repository instance.
     * 
     * @return repository instance
     */
    public TextRepository getTextRepository() {
        return this.textRepository;
    }
    
    /**
     * Returns the analyst repository instance.
     * 
     * @return repository instance
     */
    public AnalystRepository getAnalystRepository() {
        return this.analystRepository;
    }
}
