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

import java.util.List;

import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * Abstract attribute node. Provides the common functionality for the nodes
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractAttributeNode implements AttributeNode {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent node (can be <code>null</code>)
     */
    private final AttributeNode parent;
    /**
     * the node prototype
     */
    private final AttributePrototype prototype;
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     * parent node or <code>null</code> if root
     * @param prototype
     * the node prototype (required)
     */
    protected AbstractAttributeNode(final AttributeNode parent, final AttributePrototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("Každý atribut musí mít prototyp.");
        }
        
        this.parent = parent;
        this.prototype = prototype;
    }
    
    @Override
    public String getTitle() {
        return this.prototype.getTitle();
    }
    
    @Override
    public AttributeNode getParent() {
        return this.parent;
    }
    
    @Override
    public AttributePrototype getPrototype() {
        return this.prototype;
    }
    
    @Override
    public boolean canBeRemovedByUser() {
        if (this.parent == null) {
            // cannot remove the root
            
            return false;
        }
        
        if (this.parent.find(this.prototype).size() < 2) {
            // cannot remove the last occurrence of a prototype
            // (2 occurrences = this and some other node)
            
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean canBeClonedByUser() {
        // cloning possible only if the node is repeatable
        
        return this.getPrototype().isRepeat();
    }
    
    @Override
    public AttributeNode createClone(final AttributeNode newParent) {
        return SimpleAttributeUtils.createFromPrototype(this.getPrototype(), newParent);
    }
    
    @Override
    public AttributeNode createSibling() {
        // first find the first repeatable node in hierarchy
        // from this on, this node will be called "pivot"
        
        final AttributeNode pivot = this.firstRepeatableInHierarchy();
        
        if (pivot == null) {
            return null;
        }
        
        // create a clone of pivot
        // from this on, this node will be called "branch"
        
        final AttributeNode branch = pivot.createClone(pivot.getParent());
        
        // connect branch to the same parent as pivot
        // add the branch just behind the pivot
        
        if (branch.getParent() instanceof ComposedAttributeNode) {
            ((ComposedAttributeNode) branch.getParent()).addChildAfter(branch, pivot);
        }
        
        // find nodes with the same prototype in the new branch
        // from this on, these nodes will be called "candidates"
        
        final List<AttributeNode> candidates = branch.find(this.getPrototype());
        
        if (!candidates.isEmpty()) {
            // return the first of the candidates
            
            return candidates.get(0);
        }
        
        // something went wrong
        
        return null;
    }
    
    /**
     * Finds the first repeatable node in the hierarchy (if any), starting from
     * this node (can be repeatable by itself).
     * 
     * @return first repeatable node in hierarchy (can be the node itself) or
     * <code>null</code> if no repeatable node was found
     */
    private AttributeNode firstRepeatableInHierarchy() {
        AttributeNode temp = this;
        
        while (temp != null) {
            final AttributePrototype parentPrototype = temp.getPrototype();
            
            if (parentPrototype.isRepeat()) {
                // first repeatable parent found
                return temp;
            }
            
            // continue to the next parent
            temp = temp.getParent();
        }
        
        // something went wrong
        return null;
    }
    
    /**
     * Checks if the node has the specified prototype.
     * 
     * @param aPrototype
     * a prototype
     * @return <code>true</code> if the prototypes are equal (reference equality
     * check), <code>false</code> otherwise
     */
    protected boolean hasPrototype(final AttributePrototype aPrototype) {
        if (this.prototype == aPrototype) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        if (this.parent == null) {
            return this.prototype.toString() + " (kořen)";
        }
        
        return this.prototype.toString();
    }
}
