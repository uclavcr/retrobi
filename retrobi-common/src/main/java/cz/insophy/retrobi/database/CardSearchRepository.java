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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Response;
import org.jcouchdb.document.DesignDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.SearchResult;
import cz.insophy.retrobi.database.entity.SearchResultRow;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card search repository. It provides several methods for searching the card
 * catalog. The search engine used is the external Lucene fulltext indexer
 * (couchdb-lucene). First, a query is created, based on the user input. Then
 * the indexer is queried and search results are fetched (if any) and returned.
 * 
 * <pre>
 * Query  ::= ( Clause ) *
 * Clause ::= ["+", "-"] [&lt;TERM&gt; ":"] ( &lt;TERM&gt; | "(" Query ")" )
 * </pre>
 * 
 * @author Vojtěch Hordějčuk
 */
final public class CardSearchRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(CardSearchRepository.class);
    /**
     * URL path part format string
     */
    private static final String URL_PATH_FORMAT = "/%s/_fti/%s/%s";
    /**
     * cleanup URL format string (e.g. for cleanup)
     */
    private static final String URL_CLEANUP_FORMAT = "/%s/_fti/_cleanup";
    /**
     * URL query part format string
     * <ul>
     * <li>leading wildcards ON</li>
     * <li>stale results possible OFF</li>
     * <li>query split by commas OFF</li>
     * </ul>
     */
    private static final String URL_QUERY_FORMAT = "allow_leading_wildcard=true&lowercase_expanded_terms=%s&qsplit=false&skip=%d&limit=%d&q=%s";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected CardSearchRepository(final Database database) {
        super(database);
    }
    
    // =========
    // SEARCHING
    // =========
    
    /**
     * Default method for searching in the database. This method is aimed to
     * load only a small subset of all results and returns the rich search
     * result with stored values.
     * 
     * @param index
     * index view to use
     * @param userQuery
     * user search query (in Lucene syntax)
     * @param sensitive
     * case sensitive flag
     * @param catalogFilter
     * catalog filter (desired card catalogs, if empty = all)
     * @param stateFilter
     * state filter (desired card state)
     * @param offset
     * result offset
     * @param limit
     * result limit
     * @return search result for further processing
     * @throws GeneralRepositoryException
     * general exception
     */
    public SearchResult search(final AbstractCardIndex index, final String userQuery, final boolean sensitive, final Set<Catalog> catalogFilter, final CardState stateFilter, final int offset, final int limit) throws GeneralRepositoryException {
        CardSearchRepository.LOG.debug("STARTING SEARCH...");
        CardSearchRepository.LOG.debug("Index: " + index);
        CardSearchRepository.LOG.debug("User query: " + userQuery);
        CardSearchRepository.LOG.debug("Sensitive: " + sensitive);
        CardSearchRepository.LOG.debug("Catalog filter: " + catalogFilter);
        CardSearchRepository.LOG.debug("State filter: " + stateFilter);
        CardSearchRepository.LOG.debug("Offset: " + offset);
        CardSearchRepository.LOG.debug("Limit: " + limit);
        
        // prepare the search request URL
        
        final String url = CardSearchRepository.createSearchUrl(
                this.getDatabaseName(),
                index,
                userQuery,
                sensitive,
                catalogFilter,
                stateFilter,
                offset,
                limit);
        
        // call the given URL
        
        CardSearchRepository.LOG.info("Searching using an URL: " + url);
        
        Response response = null;
        
        try {
            // send a GET request to the indexing server
            
            response = this.getGetResponse(url);
            
            // parse the handler response
            
            return CardSearchRepository.parseSearchResult(response);
        } finally {
            // destroy the response in all cases
            
            if (response != null) {
                response.destroy();
            }
        }
    }
    
    /**
     * A method for searching in the database. The search is done only in the
     * card ID set specified. This method is not very fast as it must get all
     * the results from database and then remove all of them which are not in
     * the set provided.
     * 
     * @param cardIds
     * card IDs to search in
     * @param index
     * index view to use
     * @param userQuery
     * user search query (in Lucene syntax)
     * @param sensitive
     * case sensitive flag
     * @param catalogFilter
     * catalog filter (desired card catalogs, if empty = all)
     * @param stateFilter
     * state filter (desired card state)
     * @param offset
     * result offset
     * @param limit
     * result limit
     * @return search result for further processing
     * @throws GeneralRepositoryException
     * general exception
     */
    public SearchResult search(final List<String> cardIds, final AbstractCardIndex index, final String userQuery, final boolean sensitive, final Set<Catalog> catalogFilter, final CardState stateFilter, final int offset, final int limit) throws GeneralRepositoryException {
        if (cardIds.isEmpty()) {
            // no cards in the basket
            
            return new SearchResult(userQuery);
        }
        
        // do the search
        
        final SearchResult result = this.search(
                index,
                userQuery,
                sensitive,
                catalogFilter,
                stateFilter,
                0,
                Integer.MAX_VALUE);
        
        // prepare new result rows
        
        final List<SearchResultRow> newRows = new ArrayList<SearchResultRow>(Math.min(cardIds.size(), 500));
        
        for (final SearchResultRow row : result.getRows()) {
            if (cardIds.contains(row.getId())) {
                newRows.add(row);
            }
        }
        
        if (newRows.isEmpty()) {
            // no result rows available after filtering
            
            return new SearchResult(userQuery);
        }
        
        // apply paging offset and limit
        
        final int from = offset;
        final int to = Math.min(newRows.size(), offset + limit);
        
        final List<SearchResultRow> rangedRows = newRows.subList(from, to);
        
        // replace old rows
        
        result.setRows(rangedRows);
        result.setTotalRows(newRows.size());
        
        return result;
    }
    
    // =======
    // SUPPORT
    // =======
    
    /**
     * Updates the document with the fulltext index view definitions. If the
     * document does not exist, it will be created. If the document exists, it
     * will be overwritten.
     * 
     * @param replace
     * replace existing document
     * @param indexes
     * the collection of indexes
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void updateIndexDocument(final boolean replace, final Collection<AbstractCardIndex> indexes) throws GeneralRepositoryException {
        // remove the obsolete design document (if any)
        
        DesignDocument doc = null;
        
        try {
            doc = this.loadDocument(DesignDocument.class, Settings.INDEX_DOCUMENT_ID);
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
        
        doc = new DesignDocument(Settings.INDEX_DOCUMENT_ID);
        
        // put indexes into the document
        
        final Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        
        for (final AbstractCardIndex index : indexes) {
            final Map<String, String> submap = new HashMap<String, String>();
            submap.put("index", index.getCode());
            submap.put("analyzer", "whitespace");
            map.put(index.getName(), submap);
        }
        
        doc.setProperty("fulltext", map);
        
        // replace the current design document in the database (if any)
        
        this.createDocument(doc);
    }
    
    /**
     * Cleans the fulltext index. It is good to call this method from time to
     * time when changing DB or fulltext view definitions. It removes the
     * obsolete index data from disk.
     */
    public void cleanupIndex() {
        CardSearchRepository.LOG.debug("Cleanup index...");
        
        // create cleaning URL
        
        final String url = String.format(CardSearchRepository.URL_CLEANUP_FORMAT, this.getDatabaseName());
        
        CardSearchRepository.LOG.debug("Cleanup URL: " + url);
        
        Response response = null;
        
        try {
            // send a POST request to the indexing server
            
            response = this.getPostResponse(url);
            
            CardSearchRepository.LOG.debug(String.format(
                    "Response [code = %d]: %s",
                    response.getCode(),
                    new String(response.getContent())));
        } finally {
            // destroy the response in all cases
            
            if (response != null) {
                response.destroy();
            }
        }
        
        CardSearchRepository.LOG.debug("Index was cleaned up.");
    }
    
    /**
     * Optimizes the given index.
     * 
     * @param index
     * index to optimize
     */
    public void optimize(final AbstractCardIndex index) {
        CardSearchRepository.LOG.debug("Optimizing index: " + index.getCode());
        
        // create optimizing URL
        
        final String url = String.format(
                    CardSearchRepository.URL_PATH_FORMAT,
                    this.getDatabaseName(),
                    Settings.INDEX_DOCUMENT_ID,
                    index.getName()) + "/_optimize";
        
        CardSearchRepository.LOG.debug("Optimize URL: " + url);
        
        Response response = null;
        
        try {
            // send a POST request to the indexing server
            
            response = this.getPostResponse(url);
            
            CardSearchRepository.LOG.debug(String.format(
                        "Response [code = %d]: %s",
                        response.getCode(),
                        new String(response.getContent())));
        } finally {
            // destroy the response in all cases
            
            if (response != null) {
                response.destroy();
            }
        }
        
        CardSearchRepository.LOG.debug("Index was optimized.");
    }
    
    /**
     * Runs a test query to the given index. This basically creates a random
     * query to the index and runs two queries: one case sensitive and one case
     * insensitive. The limit is set to one result only.
     * 
     * @param index
     * index to ping to
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void ping(final AbstractCardIndex index) throws GeneralRepositoryException {
        final String randomQuery = SimpleStringUtils.getRandomString(8);
        CardSearchRepository.LOG.info(String.format("Ping to index '%s' with query '%s'...", index.getName(), randomQuery));
        this.search(index, randomQuery, false, null, null, 0, 1);
        this.search(index, randomQuery, true, null, null, 0, 1);
    }
    
    // ================
    // SEARCH UTILITIES
    // ================
    
    /**
     * Prepares a search URL with the given parameters. Basically, a query is a
     * HTTP request (= URL address) and the response is the search result with
     * individual documents. The request is processed by the listening Lucene
     * server.
     * 
     * @param database
     * database name
     * @param index
     * index to use
     * @param userQuery
     * user search query (in Lucene syntax)
     * @param sensitive
     * case sensitive flag
     * @param catalogFilter
     * catalog filter
     * @param stateFilter
     * state filter
     * @param offset
     * result offset
     * @param limit
     * result limit
     * @return final search URL for the indexer server
     */
    private static String createSearchUrl(final String database, final AbstractCardIndex index, final String userQuery, final boolean sensitive, final Set<Catalog> catalogFilter, final CardState stateFilter, final int offset, final int limit) {
        // prepare path part
        
        final String pathPart = String.format(
                CardSearchRepository.URL_PATH_FORMAT,
                database,
                Settings.INDEX_DOCUMENT_ID,
                index.getName());
        
        CardSearchRepository.LOG.debug("Path part: " + pathPart);
        
        // prepare query part
        
        final String normalizedLuceneQuery = SimpleSearchUtils.extendQuery(userQuery, sensitive, catalogFilter, stateFilter);
        final String encodedLuceneQuery = CardSearchRepository.encodeLuceneQuery(normalizedLuceneQuery);
        
        final String queryPart = String.format(
                CardSearchRepository.URL_QUERY_FORMAT,
                String.valueOf(!sensitive),
                offset,
                limit,
                encodedLuceneQuery);
        
        CardSearchRepository.LOG.debug("Query part: " + queryPart);
        
        // finalize URL
        
        final String finalUrl = pathPart + "?" + queryPart;
        
        CardSearchRepository.LOG.debug("Final query URL: " + finalUrl);
        
        return finalUrl;
    }
    
    /**
     * Encodes the Lucene query for use in the URL query.
     * 
     * @param luceneQuery
     * Lucene query
     * @return encoded Lucene query
     */
    private static String encodeLuceneQuery(final String luceneQuery) {
        CardSearchRepository.LOG.debug("Before encoding Lucene query: " + luceneQuery);
        final String finalUrl = SimpleStringUtils.encodeForUrl(luceneQuery);
        CardSearchRepository.LOG.debug("After encoding Lucene query: " + finalUrl);
        return finalUrl;
    }
    
    /**
     * Internal method for parsing the fulltext indexer response.
     * 
     * @param response
     * fulltext indexer response
     * @return response parsed to search result bean
     * @throws GeneralRepositoryException
     * general repository repository exception
     */
    private static SearchResult parseSearchResult(final Response response) throws GeneralRepositoryException {
        // check the response
        
        CardSearchRepository.LOG.debug("Search response code: " + response.getCode());
        
        if (!response.isOk()) {
            throw new GeneralRepositoryException("Invalid search response: " + response.getContentAsString());
        }
        
        // parse the response and convert it to the bean
        // JSON parse exception may occur here (will be caught)
        
        try {
            // parse response
            
            CardSearchRepository.LOG.debug("Parsing the search response...");
            
            final long timeStart = System.currentTimeMillis();
            final SearchResult result = response.getContentAsBean(SearchResult.class);
            final long timeEnd = System.currentTimeMillis();
            
            // log some response properties
            
            result.setParseDuration((int) (timeEnd - timeStart));
            
            CardSearchRepository.LOG.info(String.format(
                    "Query '%s' returned %d row(s) (from total %d) in %d ms (parsing = %d ms).",
                    result.getQuery(),
                    result.getRows().size(),
                    result.getTotalRows(),
                    result.getSearchDuration(),
                    result.getParseDuration()));
            
            // return parsed response
            
            return result;
        } catch (final Exception x) {
            CardSearchRepository.LOG.warn(x.getMessage());
            throw new GeneralRepositoryException("Response parser error: " + x.getMessage(), x);
        }
    }
}
