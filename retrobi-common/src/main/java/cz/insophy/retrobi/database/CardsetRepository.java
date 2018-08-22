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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Cardset;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.utils.Triple;

/**
 * Cardset repository. Supports loading and saving of named card baskets.
 * 
 * @author Vojtěch Hordějčuk
 */
final public class CardsetRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(CardsetRepository.class);
    /**
     * cardset list view name
     */
    private static final String V_LIST = "list";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected CardsetRepository(final Database database) {
        super(database);
    }
    
    /**
     * Adds or replaces a cardset in the database and informs about the result
     * of this process. If a cardset with the same name existed and was
     * overwritten, returns <code>true</code>. Otherwise it returns
     * <code>false</code>.
     * 
     * @param newCardset
     * a cardset to be saved
     * @param limit
     * cardset count limit (or -1 if there is none)
     * @return <code>true</code> if an existing cardset was overwritten during
     * the process, <code>false</code> otherwise
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws OverLimitException
     * an exception thrown when a limit is exceeded
     */
    public boolean saveCardset(final Cardset newCardset, final int limit) throws GeneralRepositoryException, NotFoundRepositoryException, OverLimitException {
        CardsetRepository.LOG.debug("Adding a new cardset...");
        
        // load cardsets of the user
        
        final List<Triple<String, String, String>> existingSets = this.getCardsetIds(newCardset.getUserId());
        
        // find an existing cardset with the same title
        
        String existingSetId = null;
        
        for (final Triple<String, String, String> existingSet : existingSets) {
            if (existingSet.getSecond().equalsIgnoreCase(newCardset.getTitle())) {
                existingSetId = existingSet.getFirst();
                break;
            }
        }
        
        if (existingSetId != null) {
            // overwrite the existing cardset
            
            final Cardset existingSet = this.getCardset(existingSetId);
            CardsetRepository.LOG.debug("Removing the existing cardset...");
            this.deleteCardset(existingSet);
            CardsetRepository.LOG.debug("Saving a new cardset (the old one WAS overwritten)...");
            this.createDocument(newCardset);
            CardsetRepository.LOG.debug("Cardset saved over the old one.");
            return true;
        }
        
        // check cardset limit
        
        if (!((limit < 0) || (existingSets.size() < limit))) {
            CardsetRepository.LOG.debug("Cardset limit exceeded.");
            throw new OverLimitException(limit);
        }
        
        // add new cardset to the database
        
        CardsetRepository.LOG.debug("Saving a new cardset (no overwriting)...");
        this.createDocument(newCardset);
        CardsetRepository.LOG.debug("Cardset saved.");
        return false;
    }
    
    /**
     * Removes an existing cardset from the database.
     * 
     * @param cardset
     * a cardset to remove
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void deleteCardset(final Cardset cardset) throws GeneralRepositoryException {
        CardsetRepository.LOG.debug(String.format("Removing cardset '%s'...", cardset.getId()));
        this.deleteDocument(cardset);
        CardsetRepository.LOG.debug("Cardset removed.");
    }
    
    /**
     * Loads the titles and IDs of cardsets of the given user.
     * 
     * @param userId
     * user ID
     * @return a tuple (cardset ID, cardset title, descriptive cardset title)
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public List<Triple<String, String, String>> getCardsetIds(final String userId) throws NotFoundRepositoryException, GeneralRepositoryException {
        CardsetRepository.LOG.debug(String.format("Loading cardsets for user '%s'...", userId));
        
        // prepare query
        
        final Object startKey = new Object[] { userId, null };
        final Object endKey = new Object[] { userId, Collections.emptyMap() };
        
        final Options options = new Options().startKey(startKey).endKey(endKey);
        
        // run the query
        
        final ViewResult<String> result = this.queryView(CardsetRepository.V_LIST, String.class, options);
        
        // process results
        
        final List<Triple<String, String, String>> list = new LinkedList<Triple<String, String, String>>();
        
        for (final ValueRow<String> row : result.getRows()) {
            // get key
            final List<?> key = (List<?>) row.getKey();
            // get title
            final String title = (String) key.get(1);
            // get year
            final long y = (Long) key.get(2);
            // get month
            final long m = (Long) key.get(3);
            // get day
            final long d = (Long) key.get(4);
            
            list.add(Triple.of(
                    row.getValue(),
                    title,
                    String.format("%s (uloženo %d.%d.%d)", title, d, m, y)));
        }
        
        // return results
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Loads the cardset with the given ID.
     * 
     * @param cardsetId
     * cardset ID
     * @return the cardset
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public Cardset getCardset(final String cardsetId) throws NotFoundRepositoryException, GeneralRepositoryException {
        CardsetRepository.LOG.debug(String.format("Loading cardset '%s'...", cardsetId));
        return this.loadDocument(Cardset.class, cardsetId);
    }
    
    @Override
    public Map<String, View> createViews() {
        final Map<String, View> views = new HashMap<String, View>();
        
        // ---------------
        // CARDSET LISTING
        // ---------------
        
        // key = [user ID, title, date added object]
        // value = cardset ID
        
        views.put(CardsetRepository.V_LIST, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_cardset) {\n" +
                        "    emit([doc.user_id, doc.title, doc.added.y, doc.added.m, doc.added.d], doc._id);\n" +
                        "  }\n" +
                        "}\n"));
        
        return Collections.unmodifiableMap(views);
    }
}
