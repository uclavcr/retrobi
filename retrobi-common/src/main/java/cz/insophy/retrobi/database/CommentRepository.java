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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;

/**
 * Class that provides access to comments and sending e-mails.
 * 
 * @author Vojtěch Hordějčuk
 */
final public class CommentRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(CommentRepository.class);
    /**
     * comment list by card view name
     */
    private static final String V_BY_CARD = "by_card";
    /**
     * comment list by user view name
     */
    private static final String V_BY_USER = "by_user";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected CommentRepository(final Database database) {
        super(database);
    }
    
    /**
     * Adds a comment to the database.
     * 
     * @param comment
     * comment to be added
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void addComment(final Comment comment) throws GeneralRepositoryException {
        CommentRepository.LOG.debug("Adding comment...");
        this.createDocument(comment);
        CommentRepository.LOG.debug("Comment added.");
    }
    
    /**
     * Removes a comment from the database.
     * 
     * @param comment
     * comment to remove
     * @throws GeneralRepositoryException
     * general repository exception general repository exception not found
     * exception
     */
    public void deleteComment(final Comment comment) throws GeneralRepositoryException {
        CommentRepository.LOG.debug("Deleting comment...");
        this.deleteDocument(comment);
        CommentRepository.LOG.debug("Comment deleted.");
    }
    
    /**
     * Loads a list of comments by their IDs.
     * 
     * @param commentIds
     * comment IDs
     * @return list of comments
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found repository exception
     */
    public List<Comment> getComments(final List<String> commentIds) throws NotFoundRepositoryException, GeneralRepositoryException {
        CommentRepository.LOG.debug(String.format("Loading %d comment(s)...", commentIds.size()));
        return this.loadDocuments(Comment.class, commentIds);
    }
    
    /**
     * Loads all comments for the given card.
     * 
     * @param cardId
     * card ID
     * @return list of comment IDs
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<String> getCommentIdsForCard(final String cardId) throws GeneralRepositoryException, NotFoundRepositoryException {
        CommentRepository.LOG.debug(String.format("Loading comments for card '%s'...", cardId));
        
        // PREPARE QUERY
        
        final Object startKey = new Object[] { cardId };
        final Object endkey = new Object[] { cardId, Collections.emptyMap() };
        
        final Options options = new Options().startKey(startKey).endKey(endkey);
        
        // RETURN RESULTS
        
        return this.loadDocumentIds(CommentRepository.V_BY_CARD, options);
    }
    
    /**
     * Loads all comments for the given card and user.
     * 
     * @param cardId
     * card ID
     * @param userId
     * user ID
     * @return list of comment IDs
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<String> getCommentIdsForUser(final String cardId, final String userId) throws GeneralRepositoryException, NotFoundRepositoryException {
        CommentRepository.LOG.debug(String.format("Loading comments for card '%s' by user '%s'...", cardId, userId));
        
        // PREPARE QUERY
        
        final Object sentinel = Collections.singletonMap("ZZZ", Collections.emptyMap());
        final Object startKey = new Object[] { cardId, userId };
        final Object endkey = new Object[] { cardId, userId, sentinel };
        
        final Options options = new Options().startKey(startKey).endKey(endkey);
        
        // RETURN RESULTS
        
        return this.loadDocumentIds(CommentRepository.V_BY_CARD, options);
    }
    
    /**
     * Loads all comments for the given user.
     * 
     * @param userId
     * user ID
     * @return list of comment IDs
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<String> getCommentIdsForUser(final String userId) throws GeneralRepositoryException, NotFoundRepositoryException {
        CommentRepository.LOG.debug(String.format("Loading all comments for user '%s'...", userId));
        
        // PREPARE QUERY
        
        final Options options = new Options().key(userId);
        
        // RETURN RESULTS
        
        return this.loadDocumentIds(CommentRepository.V_BY_USER, options);
    }
    
    /**
     * Loads a comment.
     * 
     * @param commentId
     * comment ID
     * @return a comment
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found repository exception
     */
    public Comment getComment(final String commentId) throws NotFoundRepositoryException, GeneralRepositoryException {
        CommentRepository.LOG.debug("Loading comment '%s'...", commentId);
        return this.loadDocument(Comment.class, commentId);
    }
    
    @Override
    public Map<String, View> createViews() {
        final Map<String, View> views = new HashMap<String, View>();
        
        // ---------------
        // COMMENT LISTING
        // ---------------
        
        // key = user ID
        // value = comment ID
        
        views.put(CommentRepository.V_BY_USER, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_comment) {\n" +
                        "    emit(doc.user_id, doc._id);\n" +
                        "  }\n" +
                        "}\n"));
        
        // key = [card ID, user ID, date added object]
        // value = comment ID
        
        views.put(CommentRepository.V_BY_CARD, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_comment) {\n" +
                        "    emit([doc.card_id, doc.user_id, doc.date_added.y, doc.date_added.m, doc.date_added.d, doc.date_added.hh, doc.date_added.mm], doc._id);\n" +
                        "  }\n" +
                        "}\n"));
        
        return Collections.unmodifiableMap(views);
    }
}
