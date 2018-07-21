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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.SearchResult;
import cz.insophy.retrobi.database.entity.SearchResultRow;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * Search utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleSearchUtils {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleSearchUtils.class);
    /**
     * current lucene version
     */
    private static final Version LUCENE_VERSION = Version.LUCENE_34;
    /**
     * default query analyzer
     */
    private static final Analyzer ANALYZER = new WhitespaceAnalyzer(SimpleSearchUtils.LUCENE_VERSION);
    
    /**
     * Converts a query to lowercase. Respects the following keywords:
     * <ul>
     * <li>range query: <code>[12 TO 34]</code>
     * <li>range query: <code>{12 TO 34}</code>
     * <li>string range query: <code>[aaa TO bbb]</code>
     * <li>string range query: <code>{aaa TO bbb}</code>
     * <li>AND operator with spaces: <code>_&&_</code>
     * <li>OR operator with spaces: <code>_||_</code>
     * <li>NOT operator with space after: <code>!_</code>
     * </ul>
     * 
     * @param query
     * the input query
     * @return the input query in lower case
     */
    public static String queryToLowercase(final String query) {
        return query.toLowerCase()
                .replace(" and ", " && ")
                .replace(" or ", " || ")
                .replace("not ", "! ")
                .replaceAll("\\[([\\w]+) to (\\w+)\\]", "[$1 TO $2]")
                .replaceAll("\\{(\\w+) to (\\w+)\\}", "{$1 TO $2}");
    }
    
    /**
     * Normalizes the user query with filter to be used as a valid Lucene query.
     * 
     * @param userQuery
     * user query
     * @param sensitive
     * case sensitive flag
     * @param catalogFilter
     * desired catalogs
     * @param stateFilter
     * desired card state
     * @return normalized user query to be used as a Lucene query
     */
    public static String extendQuery(final String userQuery, final boolean sensitive, final Set<Catalog> catalogFilter, final CardState stateFilter) {
        if ((userQuery == null) || (userQuery.trim().length() < 1)) {
            return "";
        }
        
        // created fielded user query
        
        final String finalQueryCode = String.format(
                "+%s:(%s)",
                SimpleSearchUtils.getQueryField(sensitive),
                (sensitive)
                        ? userQuery
                        : SimpleSearchUtils.queryToLowercase(userQuery));
        
        String catalogFilterQuery = "";
        String stateFilterQuery = "";
        
        // add ignored catalog filter
        
        if ((catalogFilter != null) && !catalogFilter.isEmpty()) {
            final BooleanQuery catalogFilterTemp = new BooleanQuery();
            
            for (final Catalog catalog : catalogFilter) {
                catalogFilterTemp.add(new TermQuery(new Term("catalog", catalog.name())), Occur.SHOULD);
            }
            
            catalogFilterQuery = String.format(" +(%s)", catalogFilterTemp.toString());
        }
        
        // add state filter
        
        if (stateFilter != null) {
            final Query stateFilterTemp = new TermQuery(new Term("state", stateFilter.name()));
            
            stateFilterQuery = String.format(" +(%s)", stateFilterTemp.toString());
        }
        
        // combine all sub-queries together
        
        return finalQueryCode + catalogFilterQuery + stateFilterQuery;
    }
    
    /**
     * Returns the correct indexed field for the given case sensitivity.
     * 
     * @param sensitive
     * case sensitive flag
     * @return the correct indexed field for the given case sensitivity
     */
    protected static String getQueryField(final boolean sensitive) {
        return sensitive ? "default" : "default_lc";
    }
    
    /**
     * Creates a Lucene query from the given query and case sensitivity flag.
     * 
     * @param userQuery
     * user query (for a single field)
     * @param sensitive
     * case sensitive flag
     * @return Lucene query
     * @throws ParseException
     * an exception during the parsing
     */
    private static Query createQuery(final String userQuery, final boolean sensitive) throws ParseException {
        // created fielded user query
        
        final String userQueryCased = (sensitive)
                ? userQuery
                : SimpleSearchUtils.queryToLowercase(userQuery);
        
        final String finalQueryCode = String.format(
                "%s:(%s)",
                SimpleSearchUtils.getQueryField(sensitive),
                userQueryCased);
        
        // create Lucene query parser
        
        final QueryParser luceneParser = new QueryParser(
                SimpleSearchUtils.LUCENE_VERSION,
                SimpleSearchUtils.getQueryField(sensitive),
                SimpleSearchUtils.ANALYZER);
        
        // set the query parser up
        
        luceneParser.setAllowLeadingWildcard(true);
        luceneParser.setLowercaseExpandedTerms(!sensitive);
        
        // parse the fielded user query
        
        return luceneParser.parse(finalQueryCode);
    }
    
    // ============
    // HIGHLIGHTING
    // ============
    
    /**
     * Highlights the best fragment found in the given card using the query and
     * settings provided. Returns <code>null</code> if nothing found.
     * 
     * @param formatter
     * formatter to use for highlighting results
     * @param data
     * a data for the highlighter (= value to highlight in)
     * @param userQuery
     * user search query (in Lucene syntax)
     * @param sensitive
     * case sensitive flag
     * @return input text with highlighted best fragment found or
     * <code>null</code>
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public static String highlight(final Formatter formatter, final String data, final String userQuery, final boolean sensitive) throws GeneralRepositoryException {
        if (data == null) {
            return null;
        }
        
        try {
            final Query luceneQuery = SimpleSearchUtils.createQuery(userQuery, sensitive);
            final Scorer luceneScorer = new QueryScorer(luceneQuery);
            final Highlighter luceneHighlighter = new Highlighter(formatter, luceneScorer);
            final String luceneData = sensitive ? data : data.toLowerCase();
            
            // return the best fragment highlighted
            
            SimpleSearchUtils.LOG.debug(String.format("Highlighting [%s] in [%s]...", luceneQuery.toString(), luceneData));
            
            return luceneHighlighter.getBestFragment(
                    SimpleSearchUtils.ANALYZER,
                    SimpleSearchUtils.getQueryField(sensitive),
                    luceneData);
        } catch (final ParseException x) {
            throw new GeneralRepositoryException("Chyba při parsování dotazu.", x);
        } catch (final IOException x) {
            throw new GeneralRepositoryException("Chyba vstupu/výstupu.", x);
        } catch (final InvalidTokenOffsetsException x) {
            throw new GeneralRepositoryException("Neplatný offset tokenu.", x);
        }
    }
    
    /**
     * Finds all the tree nodes which have the required prototype and joins the
     * string representation of their values using the string provided.
     * 
     * @param tree
     * an input attribute tree to search in
     * @param prototype
     * prototype to be found
     * @param joiner
     * string used as a delimiter
     * @return node values joined
     */
    public static String toHighlightData(final AttributeNode tree, final AttributePrototype prototype, final String joiner) {
        if (tree == null) {
            return null;
        }
        
        final StringBuilder b = new StringBuilder(256);
        
        for (final AttributeNode node : tree.find(prototype)) {
            if (b.length() != 0) {
                b.append(joiner);
            }
            
            b.append(SimpleSearchUtils.toHighlightData(node, joiner));
        }
        
        return b.toString().trim();
    }
    
    /**
     * Finds all the atomic tree nodes and joins the string representation of
     * their values using the string provided.
     * 
     * @param tree
     * an input attribute tree to search in
     * @param joiner
     * string used as a delimiter
     * @return atomic node values joined
     */
    public static String toHighlightData(final AttributeNode tree, final String joiner) {
        if (tree == null) {
            return null;
        }
        
        final StringBuilder b = new StringBuilder(256);
        SimpleSearchUtils.dumpAtomicChildren(tree, b, joiner);
        return b.toString().trim();
    }
    
    /**
     * Recursive method that evaluates a node and recursively calls itself to
     * visit all the atomic node in the given subtree.
     * 
     * @param parent
     * parent node
     * @param b
     * output string buffer
     * @param joiner
     * string used as a delimiter
     */
    private static void dumpAtomicChildren(final AttributeNode parent, final StringBuilder b, final String joiner) {
        if (parent instanceof AtomicAttributeNode) {
            // dump the atomic value (if not empty)
            
            final String value = ((AtomicAttributeNode) parent).getValue();
            
            if (!SimpleStringUtils.isEmpty(value)) {
                if (b.length() != 0) {
                    b.append(joiner);
                }
                
                b.append(value.trim());
            }
        } else if (parent instanceof ComposedAttributeNode) {
            // continue recursively
            
            for (final AttributeNode child : ((ComposedAttributeNode) parent).getChildren()) {
                SimpleSearchUtils.dumpAtomicChildren(child, b, joiner);
            }
        } else if (parent == null) {
            // NOP
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Converts an object value to string, which can be used as a highlighter
     * input data. This method generally returns the best string representation
     * of the given object as possible. It supports <code>null</code>,
     * iterables, maps, strings and primitive types.
     * 
     * @param value
     * input value (or <code>null</code>)
     * @param joiner
     * value splitter string
     * @return string representation of the given object
     */
    public static String objectToString(final Object value, final String joiner) {
        if (value == null) {
            // NULL
            
            return "";
        } else if (value instanceof Iterable) {
            // iterable value - join by line end
            
            final Iterable<?> iterable = (Iterable<?>) value;
            
            final StringBuilder b = new StringBuilder(256);
            
            for (final Object o : iterable) {
                final String s = SimpleSearchUtils.objectToString(o, joiner);
                
                if (s.length() > 0) {
                    // add non-empty strings only
                    
                    if (b.length() != 0) {
                        b.append(joiner);
                    }
                    
                    b.append(s);
                }
            }
            
            return b.toString().trim();
        } else if (value instanceof Map) {
            // map value - iterate the values
            
            final Map<?, ?> map = ((Map<?, ?>) value);
            
            return SimpleSearchUtils.objectToString(map.values(), joiner);
        } else if (value instanceof String) {
            // string value as is
            
            return (String) value;
        } else {
            // other (probably primitive) values
            
            return String.valueOf(value);
        }
    }
    
    // =======
    // UTILITY
    // =======
    
    /**
     * Gathers document IDs from a search result.
     * 
     * @param result
     * search result
     * @return list of document IDs gathered from the search result
     */
    public static List<String> extractCardIds(final SearchResult result) {
        if ((result == null) || (result.getRows() == null) || result.getRows().isEmpty()) {
            return Collections.emptyList();
        }
        
        final List<String> cardIds = new ArrayList<String>(result.getRows().size());
        
        for (final SearchResultRow row : result.getRows()) {
            cardIds.add(row.getId());
        }
        
        return Collections.unmodifiableList(cardIds);
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleSearchUtils() {
        throw new UnsupportedOperationException();
    }
}
