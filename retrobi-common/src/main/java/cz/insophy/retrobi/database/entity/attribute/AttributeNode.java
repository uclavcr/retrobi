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

package cz.insophy.retrobi.database.entity.attribute;

import java.io.Serializable;
import java.util.List;

/**
 * An attribute node interface.
 * 
 * @author Vojtěch Hordějčuk
 */
public interface AttributeNode extends Serializable {
    /**
     * Returns the human readable title of the node.
     * 
     * @return human readable title
     */
    public String getTitle();
    
    /**
     * Returns the node prototype.
     * 
     * @return the prototype
     */
    public AttributePrototype getPrototype();
    
    /**
     * Returns the parent node or <code>null</code> if there is none.
     * 
     * @return the parent node or <code>null</code>
     */
    public AttributeNode getParent();
    
    /**
     * Returns the subtree size. The size of a tree is defined as 1 + size of
     * each subtree (if any).
     * 
     * @return the subtree size
     */
    public int getSize();
    
    /**
     * Creates a clone of this subtree.
     * 
     * @param newParent
     * new parent node to use
     * @return a node clone (new instance)
     */
    public AttributeNode createClone(AttributeNode newParent);
    
    /**
     * Creates a sibling of the node. The sibling has the same subtree structure
     * as the original node, but is empty. Place where the sibling is added
     * depends on the type of the original node. Atomic repeatable nodes are
     * simple, but the other node types are more complex and are described in
     * the implementation.
     * 
     * @return a node sibling (new instance)
     */
    public AttributeNode createSibling();
    
    /**
     * Finds and removes the given node from the children (recursively).
     * 
     * @param nodeToRemove
     * node to be removed
     * @return <code>true</code> if the given node was found and removed from
     * the children, <code>false</code> otherwise
     */
    public boolean removeFromChildren(AttributeNode nodeToRemove);
    
    /**
     * Resets the node value (if any).
     */
    public void reset();
    
    /**
     * Returns a list of all nodes with the given prototype contained in this
     * specific subtree. The order of the list depends on the tree structure.
     * 
     * @param prototype
     * the prototype to find
     * @return a list of nodes (can be empty, but never <code>null</code>)
     */
    public List<AttributeNode> find(AttributePrototype prototype);
    
    /**
     * Checks if the node is empty. Returns <code>true</code> if the node value
     * is empty (if any) and all the children nodes are empty too (if any).
     * Furthermore, if the atomic attribute node value equals the default
     * prototype value, it is considered empty too.
     * 
     * @return <code>true</code> if the node is considered empty,
     * <code>false</code> otherwise
     */
    public boolean isEmpty();
    
    /**
     * Converts the node and all its children nodes to a normalized value. The
     * resulting value can be easily used for storing it in JSON.
     * 
     * @return a normalized value of the node
     */
    public Object normalize();
    
    /**
     * Checks if the given node can be removed by user.
     * 
     * @return <code>true</code> if the node can be removed by user,
     * <code>false</code> otherwise
     */
    public boolean canBeRemovedByUser();
    
    /**
     * Checks if the given node can be cloned by user.
     * 
     * @return <code>true</code> if the node can be cloned by user,
     * <code>false</code> otherwise
     */
    public boolean canBeClonedByUser();
}
