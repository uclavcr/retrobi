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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.database.entity.type.UserRole;

/**
 * Index utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleIndexUtils {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleIndexUtils.class);
    /**
     * basic index name prefix
     */
    private static final String BASIC_INDEX_PREFIX = "basic_";
    
    /**
     * Returns the list of indexes.
     * 
     * @param root
     * the attribute prototype tree root
     * @return list of indexes
     */
    public static List<AbstractCardIndex> getIndexes(final AttributePrototype root) {
        final List<AbstractCardIndex> result = new LinkedList<AbstractCardIndex>();
        
        result.add(SimpleIndexUtils.createEverythingIndex(root));
        result.addAll(SimpleIndexUtils.createBasicIndexes());
        result.addAll(SimpleIndexUtils.createCustomIndexes(root));
        
        Collections.sort(result);
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Creates a special index that includes everything.
     * 
     * @param root
     * root of the attribute prototype tree
     * @return special all-including index
     */
    private static AbstractCardIndex createEverythingIndex(final AttributePrototype root) {
        SimpleIndexUtils.LOG.debug("Creating all-including index...");
        
        return new AbstractCardIndex(SimpleIndexUtils.BASIC_INDEX_PREFIX + "everything", "Všude", UserRole.GUEST, -2) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return "" +
                        "function (doc) {\n" +
                        "  if (!doc.TAG_card) return null;\n" +
                        "  log.debug('Indexing ALL...');\n" +
                        "  var d = new Document();\n" +
                        "  function index2(obj) {\n" +
                        "    if (typeof obj == 'object') {\n" +
                        "      for (var key in obj) {\n" +
                        "        index2(obj[key]);\n" +
                        "      }\n" +
                        "    } else {" + SimpleIndexUtils.getAddToIndexCode("d", "obj") + "}\n" +
                        "  };\n" +
                        "  index2(doc);\n" +
                        "  return d;\n" +
                        "}\n";
            }
            
            @Override
            public String getHighlightData(final Card card) {
                final AttributeNode tree = SimpleAttributeUtils.fromDocument(card, root);
                
                return SimpleSearchUtils.objectToString(Arrays.asList(
                        card.getAttachmentNamesSorted(),
                        card.getBatch(),
                        card.getBatchForSort(),
                        card.getCatalog().name(),
                        card.getDrawer(),
                        card.getFiles(),
                        card.getId(),
                        card.getNote(),
                        card.getNumberInBatch(),
                        card.getOcr(),
                        card.getOcrFix(),
                        card.getOcrFixUserId(),
                        card.getSegmentAnnotation(),
                        card.getSegmentBibliography(),
                        card.getSegmentExcerpter(),
                        card.getSegmentHead(),
                        card.getSegmentTitle(),
                        card.getState().name(),
                        card.getUrl(),
                        SimpleSearchUtils.toHighlightData(tree, Settings.LINE_END)),
                        Settings.LINE_END);
            }
        };
    }
    
    /**
     * Creates and returns the built-in indexes.
     * 
     * @return list of indexes
     */
    private static List<AbstractCardIndex> createBasicIndexes() {
        SimpleIndexUtils.LOG.debug("Creating basic indexes...");
        
        final List<AbstractCardIndex> indexes = new LinkedList<AbstractCardIndex>();
        
        indexes.add(new AbstractCardIndex("batch", "Název skupiny", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return SimpleIndexUtils.getBasicIndexCode("batch");
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return card.getBatch();
            }
        });
        
        indexes.add(new AbstractCardIndex("ocr_best", "Textový přepis: Nejlepší", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return "" +
                        "function (doc) {\n" +
                        "  if (!doc.TAG_card) return null;\n" +
                        "  if (!doc.ocr && !doc.ocr_fix) return null;\n" +
                        "  var d = new Document();\n" +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.segment_title") +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.segment_bibliography") +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.segment_annotation") +
                        "  if (doc.ocr_fix) {\n" +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.ocr_fix") +
                        "  } else {\n" +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.ocr") +
                        "  }\n" +
                        "  return d;\n" +
                        "}\n";
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return SimpleSearchUtils.objectToString(Arrays.asList(
                        card.getSegmentTitle(),
                        card.getSegmentBibliography(),
                        card.getSegmentAnnotation(),
                        ((card.getOcrFix() != null) && (card.getOcrFix().length() > 0))
                                ? card.getOcrFix()
                                : card.getOcr()),
                        Settings.LINE_END);
            }
        });
        
        indexes.add(new AbstractCardIndex("segment_all", "Textový přepis: Jen celá segmentace", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return "" +
                        "function (doc) {\n" +
                        "  if (!doc.TAG_card) return null;\n" +
                        "  if (!doc.segment_title && !doc.segment_bibliography && !doc.segment_annotation) return null;\n" +
                        "  var d = new Document();\n" +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.segment_title") +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.segment_bibliography") +
                        SimpleIndexUtils.getAddToIndexCode("d", "doc.segment_annotation") +
                        "  return d;\n" +
                        "}\n";
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return SimpleSearchUtils.objectToString(Arrays.asList(
                        card.getSegmentTitle(),
                        card.getSegmentBibliography(),
                        card.getSegmentAnnotation()),
                        Settings.LINE_END);
            }
        });
        
        indexes.add(new AbstractCardIndex("ocr_fixed", "Textový přepis: Jen opravené OCR", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return SimpleIndexUtils.getBasicIndexCode("ocr_fix");
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return card.getOcrFix();
            }
        });
        
        indexes.add(new AbstractCardIndex("ocr_original", "Textový přepis: Jen původní OCR", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return SimpleIndexUtils.getBasicIndexCode("ocr");
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return card.getOcr();
            }
        });
        
        indexes.add(new AbstractCardIndex("segment_title", "Segmentace: Názvová část", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return SimpleIndexUtils.getBasicIndexCode("segment_title");
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return card.getSegmentTitle();
            }
        });
        
        indexes.add(new AbstractCardIndex("segment_bibliography", "Segmentace: Bibliografická část", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return SimpleIndexUtils.getBasicIndexCode("segment_bibliography");
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return card.getSegmentBibliography();
            }
        });
        
        indexes.add(new AbstractCardIndex("segment_annotation", "Segmentace: Anotační část", UserRole.GUEST, -1) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return SimpleIndexUtils.getBasicIndexCode("segment_annotation");
            }
            
            @Override
            public String getHighlightData(final Card card) {
                return card.getSegmentBibliography();
            }
        });
        
        // rename indexes to start with a common prefix
        
        for (final AbstractCardIndex index : indexes) {
            index.setName(SimpleIndexUtils.BASIC_INDEX_PREFIX + index.getName());
        }
        
        return Collections.unmodifiableList(indexes);
    }
    
    /**
     * Creates and returns the indexes defined in the attribute prototype tree.
     * The indexes will be created with all values (name, minimal user role for
     * viewing) default. This properties may be changed later.
     * 
     * @param root
     * attribute prototype tree root
     * @return list of indexes
     */
    private static List<AbstractCardIndex> createCustomIndexes(final AttributePrototype root) {
        // prepare mapping and the parent relation
        
        final Map<String, Set<AttributePrototype>> mapping = SimpleAttributeUtils.collectIndexes(root);
        final Map<AttributePrototype, AttributePrototype> parent = SimpleAttributeUtils.findParents(root);
        
        // put indices to target
        
        final List<AbstractCardIndex> indexes = new LinkedList<AbstractCardIndex>();
        
        for (final String key : mapping.keySet()) {
            indexes.add(SimpleIndexUtils.createCustomIndex(
                    key,
                    String.format("{%s}", key),
                    mapping.get(key),
                    root,
                    parent));
        }
        
        return Collections.unmodifiableList(indexes);
    }
    
    /**
     * Creates a custom index according to the parameters given. All the custom
     * indexes are created the same way. The indexing schema is following:
     * <ol>
     * <li>function <code>index2</code> - traverses and indexes all non-empty
     * and non-null values in the given value recursively</li>
     * <li>function <code>index1</code> - walks through the given path and
     * indexes everything that lies on the end of this path using the
     * <code>index2</code> method</li>
     * <li>does a walkthrough using the <code>index1</code> function for each
     * attribute in the given set</li>
     * </ol>
     * 
     * @param name
     * index name
     * @param title
     * index title
     * @param prototypes
     * set of attributes to include in this index view
     * @param root
     * prototype tree root
     * @param parent
     * parent relation
     * @return a body of the index view function in Javascript
     */
    private static AbstractCardIndex createCustomIndex(final String name, final String title, final Set<AttributePrototype> prototypes, final AttributePrototype root, final Map<AttributePrototype, AttributePrototype> parent) {
        final List<List<String>> paths = new LinkedList<List<String>>();
        
        // create the dynamic part
        
        final String prefix = "doc." + SimpleAttributeUtils.ATTRIBUTE_TREE_KEY;
        
        final StringBuilder b = new StringBuilder(64);
        
        for (final AttributePrototype prototype : prototypes) {
            final List<String> path = SimpleAttributeUtils.getPath(prototype, parent);
            paths.add(path);
            final String jspath = SimpleStringUtils.toJson(path);
            
            b.append(String.format("log.debug('Custom index `%s` (%s)...');\n", name, path.toString()));
            b.append(String.format("index1(%s, %s);\n", prefix, jspath));
        }
        
        final String addValuesCode = b.toString();
        
        // create the whole body with the dynamic part included
        
        return new AbstractCardIndex(name, title, UserRole.GUEST, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getCode() {
                return "" +
                        "function (doc) {\n" +
                        "  if (!doc.TAG_card) return null;\n" +
                        "  var d = new Document();\n" +
                        "  function index2(obj) {\n" +
                        "    if (typeof obj == 'object') {\n" +
                        "      for (var key in obj) {\n" +
                        "        index2(obj[key]);\n" +
                        "      }\n" +
                        "    } else {" + SimpleIndexUtils.getAddToIndexCode("d", "obj") + "}\n" +
                        "  };\n" +
                        "  function index1(value,path) {\n" +
                        "    if (value == null || value == '') {\n" +
                        "      return;\n" +
                        "    }\n" +
                        "    if (path.length == 0) {\n" +
                        "      index2(value);\n" +
                        "    } else {\n" +
                        "      var subvalue = value[path[0]];\n" +
                        "      if (typeof subvalue == 'object') {\n" +
                        "        for (var subkey in subvalue) {\n" +
                        "          index1(subvalue[subkey], path.slice(1));\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  };\n" +
                        "  if (" + prefix + ") {" + addValuesCode + "}\n" +
                        "  return d;\n" +
                        "}\n";
            }
            
            @Override
            public String getHighlightData(final Card card) {
                final AttributeNode tree = SimpleAttributeUtils.fromDocument(card, root);
                final StringBuilder buffer = new StringBuilder(256);
                
                if (tree != null) {
                    for (final AttributePrototype prototype : prototypes) {
                        if (buffer.length() != 0) {
                            buffer.append(Settings.LINE_END);
                        }
                        
                        buffer.append(SimpleSearchUtils.toHighlightData(tree, prototype, Settings.LINE_END));
                    }
                }
                
                return buffer.toString().trim();
            }
        };
    }
    
    // ==============
    // CODE GENERATOR
    // ==============
    
    /**
     * Returns a common Javascript code fragment for adding to index. This
     * fragment ignores all empty (or <code>null</code>) values before adding.
     * 
     * @param targetDocVar
     * target document variable name (NOT a document being indexed, but the
     * target CouchDB-Lucene document!)
     * @param jsVar
     * Javascript variable name with the value to be indexed (must be STRING)
     * @return a Javascript code fragment for adding to the index
     */
    private static String getAddToIndexCode(final String targetDocVar, final String jsVar) {
        final String q = "" +
                "if ((%1$s != null) && (typeof %1$s == 'string')) {\n" +
                "  %2$s.add(%1$s,               {'field':'%3$s'});\n" +
                "  %2$s.add(%1$s.toLowerCase(), {'field':'%4$s'});\n" +
                "  if (doc.catalog) { %2$s.add(doc.catalog, {'field':'catalog','index':'not_analyzed_no_norms'}); }\n" +
                "  if (doc.state)   { %2$s.add(doc.state,   {'field':'state',  'index':'not_analyzed_no_norms'}); }\n" +
                "}\n";
        
        final String fieldSensitive = SimpleSearchUtils.getQueryField(true);
        final String fieldInsensitive = SimpleSearchUtils.getQueryField(false);
        return String.format(q, jsVar, targetDocVar, fieldSensitive, fieldInsensitive);
    }
    
    /**
     * Creates a default card index code.
     * 
     * @param varName
     * variable name (e.g. if you want to index <code>doc.tree.head</code>, you
     * should put <code>tree.head</code> into this parameter)
     * @return card index code
     */
    private static String getBasicIndexCode(final String varName) {
        final String fullVarName = "doc." + varName;
        
        return String.format("" +
                "function (doc) {\n" +
                "  if (!doc.TAG_card) return null;\n" +
                "  if (!%s) return null;\n" +
                "  var d = new Document();\n" +
                "  %s\n" +
                "  return d;\n" +
                "}\n",
                fullVarName,
                SimpleIndexUtils.getAddToIndexCode("d", fullVarName));
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleIndexUtils() {
        throw new UnsupportedOperationException();
    }
}
