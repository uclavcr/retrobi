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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParseException;
import org.svenson.JSONParser;

import cz.insophy.retrobi.database.document.BasicDocument;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.database.entity.type.CardIndexInfo;
import cz.insophy.retrobi.utils.Tuple;

/**
 * Attribute utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleAttributeUtils {
    /**
     * card property holding the attribute tree branches
     */
    public static final String ATTRIBUTE_TREE_KEY = "tree";
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleAttributeUtils.class);
    
    // =========
    // TOP LEVEL
    // =========
    
    /**
     * Returns an attribute tree extracted from the given document. If the
     * document does not contain any tree, a default one is created and returned
     * instead. For more information about the process, see the documentation of
     * the method below.
     * 
     * @param document
     * source document
     * @param prototypeRoot
     * root of the attribute tree prototype
     * @return an attribute tree extracted from the document or the default tree
     * created from the prototype (if the document did not contain any tree)
     */
    public static AttributeNode fromDocumentEnsured(final BasicDocument document, final AttributePrototype prototypeRoot) {
        final AttributeNode root = SimpleAttributeUtils.fromDocument(document, prototypeRoot);
        
        if (root != null) {
            return root;
        }
        
        return SimpleAttributeUtils.createFromPrototype(prototypeRoot);
    }
    
    /**
     * Returns an attribute tree extracted from the given document. Any errors
     * in the value structure are skipped and ignored, missing attributes are
     * filled in from the prototype. Can return <code>null</code>.
     * 
     * @param document
     * source document
     * @param prototypeRoot
     * root of the attribute tree prototype
     * @return an attribute tree extracted from the document
     */
    public static AttributeNode fromDocument(final BasicDocument document, final AttributePrototype prototypeRoot) {
        // get the property value
        
        SimpleAttributeUtils.LOG.debug("Loading the value...");
        final Object value = document.getProperty(SimpleAttributeUtils.ATTRIBUTE_TREE_KEY);
        
        // extract the whole attribute tree and get the root
        
        SimpleAttributeUtils.LOG.debug("Denormalizing...");
        return SimpleAttributeUtils.denormalize(value, prototypeRoot);
    }
    
    /**
     * Puts a normalized attribute node to the given document. Any existing
     * property will be replaced.
     * 
     * @param document
     * target document
     * @param node
     * node to set
     */
    public static void toDocument(final BasicDocument document, final AttributeNode node) {
        SimpleAttributeUtils.LOG.debug("Setting the document value...");
        document.setProperty(SimpleAttributeUtils.ATTRIBUTE_TREE_KEY, node.normalize());
        SimpleAttributeUtils.LOG.debug("Document property updated.");
    }
    
    // =============
    // DENORMALIZING
    // =============
    
    /**
     * Creates an attribute node from the given value. The input value must be
     * normalized - that means, that it was created by a
     * <code>normalize()</code> method on the root node. Simply said, the value
     * must be a recursive tree structure of maps and lists, where each map is a
     * composed attribute and each string is an atomic attribute. A list of such
     * values means multiple attributes of the same type.<br>
     * <br>
     * The tree is being built according to the prototype given. Any unclear or
     * invalid values are skipped.
     * 
     * @param value
     * the input value (normalized structure of maps and lists)
     * @param prototype
     * the attribute prototype
     * @return the root of the tree of attributes or <code>null</code>
     */
    public static AttributeNode denormalize(final Object value, final AttributePrototype prototype) {
        SimpleAttributeUtils.LOG.debug("Creating the node from a normalized value...");
        
        if (prototype.isAtomic()) {
            return SimpleAttributeUtils.createAtomicNode(value, null, prototype);
        }
        
        return SimpleAttributeUtils.createComposedNode(value, null, prototype);
    }
    
    /**
     * Collects composed nodes from the given value. The input value should be a
     * list of <b>maps</b> (with the same keys) or a single <b>map</b>. Unknown
     * values will be skipped. If no nodes are found, a list of a single node is
     * returned, where the node is created from the prototype.
     * 
     * @param data
     * input data
     * @param parent
     * parent node (if any)
     * @param prototype
     * node prototype
     * @return list of resulting nodes found
     */
    public static List<AttributeNode> collectComposed(final Object data, final AttributeNode parent, final AttributePrototype prototype) {
        if (data instanceof List) {
            SimpleAttributeUtils.LOG.debug(String.format("Collecting composed nodes of '%s' from value '%s'...", prototype.getKey(), data));
            
            // cast the data to a list
            
            @SuppressWarnings("unchecked")
            final List<Object> list = (List<Object>) data;
            
            // extract nodes from this list
            
            final List<AttributeNode> nodes = new LinkedList<AttributeNode>();
            
            for (final Object object : list) {
                final ComposedAttributeNode node = SimpleAttributeUtils.createComposedNode(object, parent, prototype);
                
                if (node != null) {
                    nodes.add(node);
                }
            }
            
            SimpleAttributeUtils.LOG.debug("Composed nodes collected: " + nodes.size());
            SimpleAttributeUtils.ensureOneNode(nodes, parent, prototype);
            return nodes;
        }
        
        // the value is not a list, create one and repeat
        
        SimpleAttributeUtils.LOG.debug("Not a list, creating one and repeating.");
        return SimpleAttributeUtils.collectComposed(Collections.singletonList(data), parent, prototype);
    }
    
    /**
     * Collects atomic nodes from the given value. The input value should be a
     * list of <b>strings</b> or a single <b>string</b>. Unknown values will be
     * skipped. If no nodes are found, a list of a single node is returned,
     * where the node is created from the prototype.
     * 
     * @param data
     * input data
     * @param parent
     * parent node (if any)
     * @param prototype
     * node prototype
     * @return list of resulting nodes found
     */
    public static List<AttributeNode> collectAtomic(final Object data, final AttributeNode parent, final AttributePrototype prototype) {
        if (data instanceof List) {
            SimpleAttributeUtils.LOG.debug(String.format("Collecting atomic nodes of '%s' from value '%s'...", prototype.getKey(), data));
            
            // cast the data to a list
            
            @SuppressWarnings("unchecked")
            final List<Object> list = (List<Object>) data;
            
            // extract nodes from this list
            
            final List<AttributeNode> nodes = new LinkedList<AttributeNode>();
            
            for (final Object object : list) {
                final AtomicAttributeNode node = SimpleAttributeUtils.createAtomicNode(object, parent, prototype);
                
                if (node != null) {
                    nodes.add(node);
                }
            }
            
            SimpleAttributeUtils.LOG.debug("Atomic nodes collected: " + nodes.size());
            SimpleAttributeUtils.ensureOneNode(nodes, parent, prototype);
            return nodes;
        }
        
        // the value is not a list, create one and repeat
        
        SimpleAttributeUtils.LOG.debug("Not a list, creating one and repeating.");
        return SimpleAttributeUtils.collectAtomic(Collections.singletonList(data), parent, prototype);
    }
    
    /**
     * Creates an atomic node from an input value. The input value must be a
     * <b>string</b>. Other values are ignored and the method returns
     * <code>null</code>.
     * 
     * @param value
     * input value
     * @param parent
     * parent node
     * @param prototype
     * attribute prototype
     * @return an atomic node or <code>null</code>
     */
    private static AtomicAttributeNode createAtomicNode(final Object value, final AttributeNode parent, final AttributePrototype prototype) {
        if (value == null) {
            SimpleAttributeUtils.LOG.debug(String.format("No atomic node (proto = %s) because of NULL input value.", prototype.getKey()));
            return null;
        }
        
        SimpleAttributeUtils.LOG.debug(String.format("Creating atomic node (proto = %s) from value %s...", prototype.getKey(), value));
        
        if (value instanceof String) {
            // cast value to string
            
            final String string = (String) value;
            
            // create node
            
            final AtomicAttributeNode node = new AtomicAttributeNode(parent, prototype);
            node.setValue(string);
            return node;
        }
        
        SimpleAttributeUtils.LOG.debug(String.format("Invalid value, not a string: %s", value));
        return null;
    }
    
    /**
     * Creates a composed node from an input value. The input value must be a
     * <b>map</b> that maps child keys to child values. Other values are ignored
     * and the method returns <code>null</code>.
     * 
     * @param value
     * input value
     * @param parent
     * parent node
     * @param prototype
     * attribute prototype
     * @return a composed node or <code>null</code>
     */
    private static ComposedAttributeNode createComposedNode(final Object value, final AttributeNode parent, final AttributePrototype prototype) {
        if (value == null) {
            SimpleAttributeUtils.LOG.debug(String.format("No composed node (proto = %s) because of NULL input value.", prototype.getKey()));
            return null;
        }
        
        SimpleAttributeUtils.LOG.debug(String.format("Creating composed node (proto = %s) from value %s...", prototype.getKey(), value));
        
        if (value instanceof Map) {
            // cast value to map
            
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) value;
            
            // create node
            
            final ComposedAttributeNode node = new ComposedAttributeNode(parent, prototype);
            
            // create node children
            
            for (final AttributePrototype childPrototype : prototype.getChildren()) {
                final Object childValue = map.get(childPrototype.getKey());
                
                // collect child nodes
                
                SimpleAttributeUtils.LOG.debug(String.format(
                        "Fetching children (proto = %s) from value '%s'...",
                        childPrototype.getKey(),
                        childValue));
                
                final List<? extends AttributeNode> childNodes = (childPrototype.isAtomic())
                        ? SimpleAttributeUtils.collectAtomic(childValue, node, childPrototype)
                        : SimpleAttributeUtils.collectComposed(childValue, node, childPrototype);
                
                // add child nodes
                
                for (final AttributeNode childNode : childNodes) {
                    SimpleAttributeUtils.LOG.debug("Adding child: " + childNode);
                    node.addChild(childNode);
                }
            }
            
            return node;
        }
        
        SimpleAttributeUtils.LOG.debug(String.format("Invalid value, not a map: %s", value));
        return null;
    }
    
    /**
     * Ensures that at least one attribute node is present in the given list. In
     * other words it adds a single node created from the given prototype, if
     * the provided list is empty.
     * 
     * @param list
     * the target list
     * @param parent
     * parent node
     * @param prototype
     * node prototype
     */
    private static void ensureOneNode(final List<AttributeNode> list, final AttributeNode parent, final AttributePrototype prototype) {
        if (list.isEmpty()) {
            SimpleAttributeUtils.LOG.debug(String.format(
                    "The resulting node list is empty, adding a single node from the prototype (%s).",
                    prototype.getKey()));
            
            list.add(SimpleAttributeUtils.createFromPrototype(prototype, parent));
        }
    }
    
    // ==============
    // TRANSFORMATION
    // ==============
    
    /**
     * Gathers a prototype tree to a list.
     * 
     * @param root
     * the root attribute
     * @param atomicOnly
     * add atomic attributes only
     * @return a list of prototypes
     */
    public static List<Tuple<String, AttributePrototype>> gatherToList(final AttributePrototype root, final boolean atomicOnly) {
        final List<Tuple<String, AttributePrototype>> list = new LinkedList<Tuple<String, AttributePrototype>>();
        SimpleAttributeUtils.gatherToList(root, atomicOnly, null, list);
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Recursive method for gathering a prototype tree to a list.
     * 
     * @param root
     * the root attribute
     * @param atomicOnly
     * add atomic attributes only
     * @param path
     * existing path (mainly for human interaction)
     * @param target
     * target list
     */
    private static void gatherToList(final AttributePrototype root, final boolean atomicOnly, final String path, final List<Tuple<String, AttributePrototype>> target) {
        // create path
        
        final String newPath = (path == null)
                ? ""
                : (path.length() < 1)
                        ? root.getTitle()
                        : path + " / " + root.getTitle() +
                                (root.isRepeat()
                                        ? "*"
                                        : "");
        
        // add parent
        
        if (path != null) {
            if (!atomicOnly || root.isAtomic()) {
                target.add(Tuple.of(newPath, root));
            }
        }
        
        // add children
        
        if (!root.isAtomic()) {
            for (final AttributePrototype child : root.getChildren()) {
                SimpleAttributeUtils.gatherToList(child, atomicOnly, newPath, target);
            }
        }
    }
    
    /**
     * Converts a prototype into the attribute tree. Returns the tree root.
     * 
     * @param prototype
     * an attribute prototype to become a model
     * @return the root of the attribute tree
     */
    public static AttributeNode createFromPrototype(final AttributePrototype prototype) {
        return SimpleAttributeUtils.createFromPrototype(prototype, null);
    }
    
    /**
     * Recursive method for building the attribute tree from the attribute
     * prototype. Returns the tree root.
     * 
     * @param prototype
     * an attribute prototype to become a model
     * @param parentNode
     * parent attribute node (or <code>null</code> if root)
     * @return the root of the attribute tree
     */
    public static AttributeNode createFromPrototype(final AttributePrototype prototype, final AttributeNode parentNode) {
        if (prototype.isAtomic()) {
            // create an atomic node using the default value
            
            final AtomicAttributeNode node = new AtomicAttributeNode(parentNode, prototype);
            node.setValue(prototype.getValue());
            return node;
        }
        
        // create a composed node recursively
        
        final ComposedAttributeNode newParentNode = new ComposedAttributeNode(parentNode, prototype);
        
        for (final AttributePrototype prototypeChild : prototype.getChildren()) {
            newParentNode.addChild(SimpleAttributeUtils.createFromPrototype(prototypeChild, newParentNode));
        }
        
        return newParentNode;
    }
    
    // =======
    // FINDING
    // =======
    
    /**
     * Returns the Javascript code fragment for getting the attribute value from
     * a normalized structure. The "path" is returned without the first (usually
     * root) attribute to be shorter. Some examples of the possible result:
     * 
     * <pre>
     * tree.head.name
     * tree.author.address.street
     * person.address.city
     * </pre>
     * 
     * @param attribute
     * attribute
     * @param parent
     * parent mapping
     * @return code fragment in Javascript pointing to the attribute
     */
    public static List<String> getPath(final AttributePrototype attribute, final Map<AttributePrototype, AttributePrototype> parent) {
        final LinkedList<String> path = new LinkedList<String>();
        AttributePrototype temp = attribute;
        
        while (temp != null) {
            // add node to path
            
            path.add(temp.getKey());
            
            // go to parent
            
            temp = parent.get(temp);
        }
        
        if (!path.isEmpty()) {
            // remove the last node (usually root)
            
            path.removeLast();
        }
        
        // reverse the path to start with the root
        
        Collections.reverse(path);
        
        return Collections.unmodifiableList(path);
    }
    
    /**
     * Finds the parent relation between attributes.
     * 
     * @param root
     * the root attribute
     * @return a map with the parent relation (mapping each child to a parent)
     */
    public static Map<AttributePrototype, AttributePrototype> findParents(final AttributePrototype root) {
        final Map<AttributePrototype, AttributePrototype> parents = new HashMap<AttributePrototype, AttributePrototype>();
        SimpleAttributeUtils.findParents(root, parents);
        return Collections.unmodifiableMap(parents);
    }
    
    /**
     * Recursive method for finding the parent relation between attributes.
     * 
     * @param root
     * the current root attribute (starting node)
     * @param target
     * target map (will map each child to a parent)
     */
    private static void findParents(final AttributePrototype root, final Map<AttributePrototype, AttributePrototype> target) {
        if ((root != null) && !root.isAtomic()) {
            for (final AttributePrototype child : root.getChildren()) {
                target.put(child, root);
                SimpleAttributeUtils.findParents(child, target);
            }
        }
    }
    
    /**
     * Recursively removes all node children which are empty and can be removed
     * by user at the same time (the node provided is checked to be a composed
     * node automatically). As a result, only non-empty nodes or unremovable
     * nodes are kept in the provided attribute tree.
     * 
     * @param root
     * the current root attribute (starting node)
     * @return <code>true</code> if any attribute is removed at all,
     * <code>false</code> otherwise
     */
    public static boolean removeEmptyChildren(final AttributeNode root) {
        boolean removed = false;
        
        if (root instanceof ComposedAttributeNode) {
            SimpleAttributeUtils.LOG.debug("Finding empty children of node: " + root);
            
            // collect empty removable nodes to a set
            
            final Set<AttributeNode> emptyNodes = new LinkedHashSet<AttributeNode>();
            
            for (final AttributeNode child : ((ComposedAttributeNode) root).getChildren()) {
                if (child.isEmpty()) {
                    SimpleAttributeUtils.LOG.debug("Empty child found: " + child);
                    emptyNodes.add(child);
                }
            }
            
            // remove such nodes from the root children
            
            for (final AttributeNode emptyNode : emptyNodes) {
                SimpleAttributeUtils.LOG.debug("Removing empty node: " + emptyNode);
                
                if (emptyNode.canBeRemovedByUser()) {
                    if (root.removeFromChildren(emptyNode)) {
                        SimpleAttributeUtils.LOG.debug("Removed: " + emptyNode);
                        removed = true;
                    }
                } else {
                    SimpleAttributeUtils.LOG.debug("Cannot remove: " + emptyNode);
                }
            }
            
            // run removing recursively (on remaining children, if any)
            
            for (final AttributeNode child : ((ComposedAttributeNode) root).getChildren()) {
                if (SimpleAttributeUtils.removeEmptyChildren(child)) {
                    removed = true;
                }
            }
        }
        
        return removed;
    }
    
    // =======
    // INDEXES
    // =======
    
    /**
     * Collects all the index names defined in the given attribute tree.
     * 
     * @param root
     * the tree root
     * @return set of all indexes from the given attributes
     */
    protected static Map<String, Set<AttributePrototype>> collectIndexes(final AttributePrototype root) {
        final Set<AttributePrototype> set = new LinkedHashSet<AttributePrototype>();
        final Stack<AttributePrototype> stack = new Stack<AttributePrototype>();
        
        stack.push(root);
        
        while (!stack.isEmpty()) {
            // open
            
            final AttributePrototype fresh = stack.pop();
            
            // process
            
            set.add(fresh);
            
            if (!fresh.isAtomic()) {
                // expand
                
                stack.addAll(fresh.getChildren());
            }
        }
        
        return SimpleAttributeUtils.collectIndexes(set);
    }
    
    /**
     * Collects all the index names defined in the given set of attributes.
     * 
     * @param attributes
     * input set of attributes
     * @return set of all indexes from the given attributes
     */
    private static Map<String, Set<AttributePrototype>> collectIndexes(final Set<AttributePrototype> attributes) {
        SimpleAttributeUtils.LOG.debug(String.format("Collecting index mapping from the set of %d attributes...", attributes.size()));
        
        final Map<String, Set<AttributePrototype>> result = new LinkedHashMap<String, Set<AttributePrototype>>();
        
        // walk all attributes and their indexes
        
        for (final AttributePrototype attribute : attributes) {
            if ((attribute.getIndexes() != null) && !attribute.getIndexes().isEmpty()) {
                for (final String index : attribute.getIndexes()) {
                    // add a mapping from the index to the attribute
                    
                    if (!result.containsKey(index)) {
                        result.put(index, new LinkedHashSet<AttributePrototype>());
                    }
                    
                    result.get(index).add(attribute);
                    
                    SimpleAttributeUtils.LOG.debug(String.format(
                            "Found mapping from index '%s' to attribute '%s' ('%s').",
                            index,
                            attribute.getKey(),
                            attribute.getTitle()));
                }
            }
        }
        
        SimpleAttributeUtils.LOG.debug("Index mappings are prepared.");
        
        return Collections.unmodifiableMap(result);
    }
    
    // ================
    // LOADING / SAVING
    // ================
    
    /**
     * Loads the attribute tree definition from the file. If the file does not
     * exist, returns the <code>null</code> value.
     * 
     * @param file
     * input file
     * @return root of the tree loaded (or <code>null</code>)
     * @throws IOException
     * I/O exception or some kind of parsing exception
     */
    public static AttributePrototype loadDefinitions(final File file) throws IOException {
        SimpleAttributeUtils.LOG.debug("Loading attribute tree definition...");
        
        if (!file.exists() || !file.canRead()) {
            SimpleAttributeUtils.LOG.debug("The definition file was not found.");
            return null;
        }
        
        // read a JSON file with the tree definition
        
        final String definition = SimpleFileUtils.readFileToString(file);
        
        // parse the JSON
        
        try {
            SimpleAttributeUtils.LOG.debug("Parsing JSON...");
            return SimpleStringUtils.fromJson(definition, AttributePrototype.class);
        } catch (final JSONParseException x) {
            throw new IOException("Chyba během parsování JSON.", x);
        }
    }
    
    /**
     * Loads the index information from a given file.
     * 
     * @param file
     * input file
     * @return index information (or <code>null</code>)
     * @throws IOException
     * I/O exception or some kind of parsing exception
     */
    public static Collection<CardIndexInfo> loadIndexInfo(final File file) throws IOException {
        SimpleAttributeUtils.LOG.debug("Loading index naming definition...");
        
        if (!file.exists() || !file.canRead()) {
            SimpleAttributeUtils.LOG.debug("The index information file was not found.");
            return null;
        }
        
        // read a JSON file with the index naming
        
        final String info = SimpleFileUtils.readFileToString(file);
        
        // parse the JSON
        
        try {
            JSONParser.defaultJSONParser().addTypeHint("[]", CardIndexInfo.class);
            @SuppressWarnings("unchecked")
            final List<CardIndexInfo> list = JSONParser.defaultJSONParser().parse(List.class, info);
            return Collections.unmodifiableList(list);
        } catch (final JSONParseException x) {
            throw new IOException("Chyba během parsování JSON.", x);
        }
    }
    
    /**
     * Saves the attribute tree definition into the file.
     * 
     * @param attribute
     * root of the tree to save
     * @param file
     * output file
     */
    public static void saveDefinitions(final AttributePrototype attribute, final File file) {
        try {
            // write a JSON file with the tree definition
            
            SimpleAttributeUtils.LOG.debug("Parsing attribute tree to JSON...");
            final String definition = SimpleStringUtils.toJson(attribute, true);
            
            // parse the JSON
            
            SimpleAttributeUtils.LOG.debug("Writing JSON to the file...");
            SimpleFileUtils.writeStringToFile(definition, file);
        } catch (final IOException x) {
            SimpleAttributeUtils.LOG.error("Could not write the attribute tree definition.");
            SimpleAttributeUtils.LOG.error("Reason: " + x.getMessage());
            throw new IllegalStateException("Nepodařilo se uložit strom atributů.", x);
        }
    }
    
    /**
     * Saves the index information into the file.
     * 
     * @param data
     * index information to save
     * @param file
     * output file
     */
    public static void saveIndexInfo(final Collection<CardIndexInfo> data, final File file) {
        try {
            // write a JSON file with the index naming
            
            SimpleAttributeUtils.LOG.debug("Parsing index info to JSON...");
            final String definition = SimpleStringUtils.toJson(data, true);
            
            // parse the JSON
            
            SimpleAttributeUtils.LOG.debug("Writing JSON to the file...");
            SimpleFileUtils.writeStringToFile(definition, file);
        } catch (final IOException x) {
            SimpleAttributeUtils.LOG.error("Could not write the index info.");
            SimpleAttributeUtils.LOG.error("Reason: " + x.getMessage());
            throw new IllegalStateException("Nepodařilo se uložit informace o indexech.", x);
        }
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleAttributeUtils() {
        throw new UnsupportedOperationException();
    }
}
