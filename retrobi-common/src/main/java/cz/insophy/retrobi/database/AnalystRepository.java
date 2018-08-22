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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.jcouchdb.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.Triple;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Repository to provide database content analysis.
 * 
 * @author Vojtěch Hordějčuk
 */
final public class AnalystRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnalystRepository.class);
    /**
     * card count view name
     */
    private static final String V_COUNT_CARDS = "count_cards";
    /**
     * image count view name (with all distinct image names)
     */
    private static final String V_COUNT_IMAGES = "count_images";
    /**
     * batch with different sorting batch view name
     */
    private static final String V_BATCH_SORT_DIFFERENT = "batch_sort_different";
    /**
     * card problems view name
     */
    private static final String V_CARD_PROBLEMS = "card_problems";
    /**
     * value view prefix text (of the view name)
     */
    private static final String VALUE_VIEW_PREFIX = "values_";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected AnalystRepository(final Database database) {
        super(database);
    }
    
    /**
     * Returns the card count for every card state present in the database.
     * 
     * @return a list of tuples (card state, card count)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Tuple<CardState, Integer>> getCardCount() throws GeneralRepositoryException {
        AnalystRepository.LOG.debug("Counting cards by state...");
        
        // prepare result list
        
        final List<Tuple<CardState, Integer>> list = new LinkedList<Tuple<CardState, Integer>>();
        
        // run the query
        
        final ViewResult<Integer> result = this.queryView(
                AnalystRepository.V_COUNT_CARDS,
                Integer.class,
                new Options().group(true));
        
        for (final ValueRow<Integer> row : result.getRows()) {
            if ((row.getKey() == null) || (row.getValue() == null)) {
                continue;
            }
            
            // get card state
            final CardState cardState = CardState.valueOf((String) row.getKey());
            // get card count
            final int cardCount = row.getValue();
            
            // add a new result row
            
            list.add(Tuple.of(cardState, cardCount));
        }
        
        // sort the resulting list by state
        
        Collections.sort(list, new Comparator<Tuple<CardState, Integer>>() {
            @Override
            public int compare(final Tuple<CardState, Integer> o1, final Tuple<CardState, Integer> o2) {
                return o1.getFirst().compareTo(o2.getFirst());
            }
        });
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Returns the count of cards in catalogs with the given image count. Please
     * note that the tuples returned does not have to contain all the required
     * data.
     * 
     * @return a list of tuples (catalog, image count, card count)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Triple<Catalog, Integer, Integer>> getImageCount() throws GeneralRepositoryException {
        AnalystRepository.LOG.debug("Counting individual images...");
        
        // prepare result list
        
        final List<Triple<Catalog, Integer, Integer>> data = new LinkedList<Triple<Catalog, Integer, Integer>>();
        
        // run the query
        
        final ViewResult<Integer> result = this.queryView(
                AnalystRepository.V_COUNT_IMAGES,
                Integer.class,
                new Options().group(true));
        
        for (final ValueRow<Integer> row : result.getRows()) {
            if ((row.getKey() == null) || (row.getValue() == null)) {
                continue;
            }
            
            // type key to a list
            final List<?> key = (List<?>) row.getKey();
            // get catalog
            final Catalog catalog = Catalog.valueOf((String) key.get(0));
            // get image count
            final int imageCount = ((Number) key.get(1)).intValue();
            // get card count
            final int cardCount = row.getValue();
            
            // add a new result row
            
            data.add(Triple.of(catalog, imageCount, cardCount));
        }
        
        return Collections.unmodifiableList(data);
    }
    
    /**
     * Returns a list of batches, where batch and batch for sort differs (case
     * insensitive comparison) with a number of corresponding documents.
     * 
     * @return list of tuples ((catalog, batch, batch for sort), count of
     * documents)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Tuple<Triple<Catalog, String, String>, Integer>> listBatchSortDifferent() throws GeneralRepositoryException {
        AnalystRepository.LOG.debug("Doing analysis (batch_sort_different)...");
        
        // prepare result list
        
        final List<Tuple<Triple<Catalog, String, String>, Integer>> data = new LinkedList<Tuple<Triple<Catalog, String, String>, Integer>>();
        
        // run the query
        
        final ViewResult<Integer> result = this.queryView(
                AnalystRepository.V_BATCH_SORT_DIFFERENT,
                Integer.class,
                new Options().group(true));
        
        for (final ValueRow<Integer> row : result.getRows()) {
            // type key to a list
            final List<?> key = (List<?>) row.getKey();
            // get catalog
            final Catalog catalog = Catalog.valueOf((String) key.get(0));
            // get batch
            final String batch = (String) key.get(1);
            // get batch for sort
            final String batchForSort = (String) key.get(2);
            // get card count
            final int count = row.getValue();
            
            // add a new result row
            
            data.add(Tuple.of(Triple.of(catalog, batch, batchForSort), count));
        }
        
        return Collections.unmodifiableList(data);
    }
    
    /**
     * Returns the list of card problems with card ID.
     * 
     * @param limit
     * limit of the result rows
     * @return list of tuples (problem title, card ID) with paging information
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Tuple<String, String>> listCardProblems(final int limit) throws GeneralRepositoryException {
        // run the query
        
        final ViewResult<String> result = this.queryView(
                AnalystRepository.V_CARD_PROBLEMS,
                String.class,
                new Options().limit(limit));
        
        // process results
        
        final List<Tuple<String, String>> list = new LinkedList<Tuple<String, String>>();
        
        for (final ValueRow<String> row : result.getRows()) {
            // get the problem
            final String problem = (String) row.getKey();
            // get the card ID
            final String cardId = row.getValue();
            
            // add a row to the result
            
            list.add(Tuple.of(problem, cardId));
        }
        
        // return results
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Returns a list of all distinct values for the given attribute.
     * 
     * @param attribute
     * attribute to be found
     * @param root
     * attribute tree root
     * @param staleOk
     * stale values are OK (do not update indexes right away)
     * @return list tuples (distinct value, card count)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Tuple<String, Integer>> listDistinctValues(final AttributePrototype attribute, final AttributePrototype root, final boolean staleOk) throws GeneralRepositoryException {
        AnalystRepository.LOG.debug(String.format("Getting distinct values for '%s'...", root.toString()));
        
        // get the path
        
        final Map<AttributePrototype, AttributePrototype> parents = SimpleAttributeUtils.findParents(root);
        final List<String> path = SimpleAttributeUtils.getPath(attribute, parents);
        
        // prepare result list
        
        final List<Tuple<String, Integer>> data = new LinkedList<Tuple<String, Integer>>();
        
        // prepare options
        
        final Options options = new Options().group(true);
        
        if (staleOk) {
            options.stale();
        }
        
        // run the query
        
        final ViewResult<Integer> result = this.queryView(
                Settings.VALUE_INDEX_DOCUMENT_ID,
                AnalystRepository.getValueViewName(path),
                Integer.class,
                options);
        
        for (final ValueRow<Integer> row : result.getRows()) {
            // get the distinct value from key
            final String value = (String) row.getKey();
            // get the card count from value
            final int count = row.getValue();
            
            // add a new result row
            
            data.add(Tuple.of(value, count));
        }
        
        return Collections.unmodifiableList(data);
    }
    
    /**
     * Updates an existing or creates a new design document that contains views
     * for viewing distinct values for each attribute in the database.
     * 
     * @param replace
     * replace existing document
     * @param root
     * attribute prototype root
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void updateAttributeViews(final boolean replace, final AttributePrototype root) throws GeneralRepositoryException {
        // remove the obsolete design document (if any)
        
        DesignDocument doc = null;
        
        try {
            doc = this.loadDocument(DesignDocument.class, Settings.VALUE_INDEX_DOCUMENT_ID);
        } catch (final NotFoundRepositoryException x) {
            // NOP
        }
        
        if (doc != null) {
            if (!replace) {
                // do not replace existing document
                
                return;
            }
            
            this.deleteDocument(doc);
        }
        
        // initialize actual design document
        
        doc = new DesignDocument(Settings.VALUE_INDEX_DOCUMENT_ID);
        
        final List<Tuple<String, AttributePrototype>> attributes = SimpleAttributeUtils.gatherToList(root, true);
        final Map<AttributePrototype, AttributePrototype> parents = SimpleAttributeUtils.findParents(root);
        
        for (final Tuple<String, AttributePrototype> attribute : attributes) {
            // check atomicity of the attribute prototype
            
            if (!attribute.getSecond().isAtomic()) {
                AnalystRepository.LOG.error("Attribute must be atomic.");
                throw new IllegalStateException("Atribut musí být atomický.");
            }
            
            // get the attribute path
            
            final List<String> path = SimpleAttributeUtils.getPath(attribute.getSecond(), parents);
            
            // add the new view to the design document
            
            doc.addView(AnalystRepository.getValueViewName(path), AnalystRepository.createValueView(path));
        }
        
        // replace the current design document in the database (if any)
        
        this.createDocument(doc);
    }
    
    /**
     * Creates a value view name for the given attribute path.
     * 
     * @param path
     * attribute path (must not be empty or <code>null</code>)
     * @return value view name
     */
    private static String getValueViewName(final List<String> path) {
        if ((path == null) || path.isEmpty()) {
            throw new IllegalArgumentException("Cesta k atributu nemůže být prázdná.");
        }
        
        return AnalystRepository.VALUE_VIEW_PREFIX + StringUtil.join(path, "_");
    }
    
    /**
     * Creates a value view for the given attribute path.
     * 
     * @param path
     * attribute path
     * @return a view for the given attribute path
     */
    private static View createValueView(final List<String> path) {
        final String mapfn = "" +
                "function (doc) {\n" +
                "  var used = new Object();\n" +
                "  function walk (source, path) {\n" +
                "    if (path.length == 0) {\n" +
                "      if (typeof source == 'string' && source != '') {\n" +
                "        if (!used[source]) {\n" +
                "          emit (source, 1);\n" +
                "          used[source] = true;\n" +
                "        }\n" +
                "      }\n" +
                "    } else {\n" +
                "      var node = path[0];\n" +
                "      if (source[node]) {\n" +
                "        for (var key in source[node]) {\n" +
                "          walk (source[node][key], path.slice(1));" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  if (doc.%s) {\n" +
                "    walk (doc.%s, %s);\n" +
                "  }\n" +
                "}\n";
        
        final String reducefn = "_sum";
        
        return new View(
                String.format(
                        mapfn,
                        SimpleAttributeUtils.ATTRIBUTE_TREE_KEY,
                        SimpleAttributeUtils.ATTRIBUTE_TREE_KEY,
                        SimpleStringUtils.toJson(path)),
                reducefn);
    }
    
    @Override
    public Map<String, View> createViews() {
        final Map<String, View> views = new HashMap<String, View>();
        
        // --------
        // COUNTERS
        // --------
        
        // key = card state name
        // value = count of cards
        
        views.put(AnalystRepository.V_COUNT_CARDS, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_card) {\n" +
                        "    emit(doc.state, 1);\n" +
                        "  }\n" +
                        "}\n",
                "" +
                        "_sum"));
        
        // key = [catalog, image count]
        // value = count of cards
        
        views.put(AnalystRepository.V_COUNT_IMAGES, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_card && doc._attachments) {\n" +
                        "    var cnt = 0;\n" +
                        "    for (var a in doc._attachments) {\n" +
                        "      cnt++;\n" +
                        "    }\n" +
                        "    emit([doc.catalog, cnt], 1);\n" +
                        "  }\n" +
                        "}\n",
                "" +
                        "_sum"));
        
        // ----------------------------------------
        // LIST OF BATCHES WHERE BATCH_SORT DIFFERS
        // ----------------------------------------
        
        // key = [batch, batch for sort]
        // value = count of cards
        
        views.put(AnalystRepository.V_BATCH_SORT_DIFFERENT, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_card && doc.batch.toLowerCase() != doc.batch_sort.toLowerCase()) {\n" +
                        "    emit([doc.catalog, doc.batch, doc.batch_sort], 1);\n" +
                        "  }\n" +
                        "}\n",
                "" +
                        "_sum"));
        
        // -------------
        // LIST OF CARDS
        // -------------
        
        // key = error message
        // value = card ID
        
        views.put(AnalystRepository.V_CARD_PROBLEMS, new View(
                "" +
                        "function(doc) {\n" +
                        "  if (doc.TAG_card) {\n" +
                        "    if (!doc.ocr) {\n" +
                        "      emit('chybí OCR', doc._id);\n" +
                        "    }\n" +
                        "    if (!doc._attachments) {\n" +
                        "      emit('chybí obrázek', doc._id);\n" +
                        "    }\n" +
                        "    var numbers = [];\n" +
                        "    for (var a in doc._attachments) {\n" +
                        "      if (doc._attachments[a].length < 1024) {\n" +
                        "        emit('nesprávná velikost: ' + a, doc._id);\n" +
                        "      }\n" +
                        "      if (!a.match(/^[0-9" + ImageFlag.codesForRegexp() + "]+$/)) {\n" +
                        "        emit('nesprávný název obrázku: ' + a, doc._id);\n" +
                        "      }\n" +
                        "      if (a.match(/^[0-9" + ImageFlag.ORIGINAL.getCode() + "]+$/)) {\n" +
                        "        var number = parseInt(a, 10);\n" +
                        "        numbers.push(number);\n" +
                        "      }\n" +
                        "    }\n" +
                        "    function sortnum(a, b) {\n" +
                        "      return a-b;\n" +
                        "    }\n" +
                        "    numbers.sort(sortnum);\n" +
                        "    var prev = 0;\n" +
                        "    for (var n in numbers) {\n" +
                        "      var next = numbers[n];\n" +
                        "      if (next != prev + 1) {\n" +
                        "        emit('nesouvislá řada obrázků (mezi ' + prev + ' a ' + next + ')', doc._id);\n" +
                        "      }\n" +
                        "      prev = next;\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n"));
        
        return Collections.unmodifiableMap(views);
    }
}
