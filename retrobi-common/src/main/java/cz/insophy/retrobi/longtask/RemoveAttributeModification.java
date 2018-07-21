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

package cz.insophy.retrobi.longtask;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * An attribute remover. This will remove all nodes with the given prototype
 * from the tree (if possible).
 * 
 * @author Vojtěch Hordějčuk
 */
public class RemoveAttributeModification extends AbstractAttributeModification {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(RemoveAttributeModification.class);
    /**
     * an attribute to remove
     */
    private final AttributePrototype attributeToRemove;
    /**
     * an exact value to remove, may be <code>null</code>
     */
    private final String valueToRemove;
    
    /**
     * Creates a new instance.
     * 
     * @param root
     * the root of the prototype tree
     * @param attributeToRemove
     * an attribute to remove
     * @param valueToRemove
     * value to be removed or <code>null</code>
     */
    public RemoveAttributeModification(final AttributePrototype root, final AttributePrototype attributeToRemove, final String valueToRemove) {
        super(root);
        
        this.attributeToRemove = attributeToRemove;
        this.valueToRemove = valueToRemove;
    }
    
    @Override
    protected boolean internalModify(final AttributeNode tree) throws AlreadyModifiedException {
        // check if the attribute is atomic
        
        if (!this.attributeToRemove.isAtomic()) {
            RemoveAttributeModification.LOG.debug("The attribute must be atomic to remove.");
            return false;
        }
        
        // find nodes to be removed first
        
        final List<AttributeNode> nodes = this.findNodesToRemove(tree);
        
        // check if there is anything to remove at all
        
        if (nodes.isEmpty()) {
            RemoveAttributeModification.LOG.debug("No feasible nodes found, no need to remove.");
            throw new AlreadyModifiedException();
        }
        
        // start removing all the remaining nodes
        
        boolean removed = false;
        
        for (final AttributeNode nodeToRemove : nodes) {
            if (tree.removeFromChildren(nodeToRemove)) {
                removed = true;
                
                RemoveAttributeModification.LOG.debug(String.format(
                        "The node '%s' was found and removed.",
                        nodeToRemove.toString()));
            } else {
                RemoveAttributeModification.LOG.debug(String.format(
                        "The node '%s' was not removed (probably not found).",
                        nodeToRemove.toString()));
            }
        }
        
        // clean the attribute tree after operation
        
        if (SimpleAttributeUtils.removeEmptyChildren(tree)) {
            RemoveAttributeModification.LOG.debug("An empty node(s) were removed.");
        }
        
        return removed;
    }
    
    /**
     * Finds all the nodes to remove. If the value to remove is
     * <code>null</code>, all nodes with the given prototype are returned. If a
     * value to remove is not <code>null</code>, only atomic attribute nodes
     * with the same value are returned.
     * 
     * @param tree
     * tree to find in
     * @return a set of attribute nodes from the given tree to be removed
     */
    private List<AttributeNode> findNodesToRemove(final AttributeNode tree) {
        if (this.valueToRemove == null) {
            return tree.find(this.attributeToRemove);
        }
        
        final List<AttributeNode> result = new LinkedList<AttributeNode>();
        
        for (final AttributeNode node : tree.find(this.attributeToRemove)) {
            if (node instanceof AtomicAttributeNode) {
                if (this.valueToRemove.equals(((AtomicAttributeNode) node).getValue())) {
                    result.add(node);
                }
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    @Override
    public String getTitle() {
        if (this.valueToRemove != null) {
            return String.format("Odstranit hodnotu '%s' atributu '%s'", this.valueToRemove, this.attributeToRemove.getTitle());
        }
        
        return String.format("Odstranit atribut '%s'", this.attributeToRemove.getTitle());
    }
}
