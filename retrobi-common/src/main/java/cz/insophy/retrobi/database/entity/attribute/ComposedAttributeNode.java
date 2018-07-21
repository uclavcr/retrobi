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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Composed attribute node with a children nodes (subtrees).
 * 
 * @author Vojtěch Hordějčuk
 */
public class ComposedAttributeNode extends AbstractAttributeNode {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * children nodes
     */
    private final List<AttributeNode> children;
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     * parent node
     * @param prototype
     * node prototype
     */
    public ComposedAttributeNode(final AttributeNode parent, final AttributePrototype prototype) {
        super(parent, prototype);
        
        this.children = new LinkedList<AttributeNode>();
    }
    
    @Override
    public boolean removeFromChildren(final AttributeNode nodeToRemove) {
        if (this == nodeToRemove) {
            throw new IllegalArgumentException("Uzel nemůže odstranit sám sebe: " + nodeToRemove);
        }
        
        boolean removed = false;
        
        // remove from children nodes
        
        final List<AttributeNode> childrenToRemove = new LinkedList<AttributeNode>();
        
        for (final AttributeNode child : this.children) {
            if (child == nodeToRemove) {
                childrenToRemove.add(child);
            }
        }
        
        for (final AttributeNode childToRemove : childrenToRemove) {
            if (this.find(childToRemove.getPrototype()).size() > 1) {
                this.children.remove(childToRemove);
            } else {
                childToRemove.reset();
            }
            
            removed = true;
        }
        
        // continue recursively
        
        for (final AttributeNode child : this.children) {
            if (!childrenToRemove.contains(child)) {
                if (child.removeFromChildren(nodeToRemove)) {
                    removed = true;
                }
            }
        }
        
        return removed;
    }
    
    @Override
    public boolean isEmpty() {
        for (final AttributeNode child : this.children) {
            if (!child.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int getSize() {
        int size = 1;
        
        for (final AttributeNode child : this.children) {
            size += child.getSize();
        }
        
        return size;
    }
    
    /**
     * Returns a list of children nodes.
     * 
     * @return children nodes
     */
    public List<AttributeNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    /**
     * Adds a children node (at the end).
     * 
     * @param childToAdd
     * new child to be added
     */
    public void addChild(final AttributeNode childToAdd) {
        this.children.add(childToAdd);
    }
    
    /**
     * Adds a children node (behind the existing child specified).
     * 
     * @param childToAdd
     * new child to be added
     * @param childToAddAfter
     * a child after which the new child should be added
     */
    public void addChildAfter(final AttributeNode childToAdd, final AttributeNode childToAddAfter) {
        if (!this.children.contains(childToAddAfter)) {
            throw new IllegalArgumentException();
        }
        
        final int index = this.children.indexOf(childToAddAfter) + 1;
        this.children.add(index, childToAdd);
    }
    
    @Override
    public List<AttributeNode> find(final AttributePrototype prototype) {
        final List<AttributeNode> result = new LinkedList<AttributeNode>();
        
        if (this.hasPrototype(prototype)) {
            // add parent as first
            result.add(this);
        }
        
        for (final AttributeNode child : this.children) {
            // add children as second
            result.addAll(child.find(prototype));
        }
        
        return Collections.unmodifiableList(result);
    }
    
    @Override
    public void reset() {
        for (final AttributeNode child : this.children) {
            child.reset();
        }
    }
    
    @Override
    public Object normalize() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        
        for (final AttributeNode child : this.children) {
            // create a list of values for each child
            
            final String key = child.getPrototype().getKey();
            final Object value = map.get(key);
            
            if (value instanceof List) {
                // use the existing list
                
                @SuppressWarnings("unchecked")
                final List<Object> list = (List<Object>) value;
                list.add(child.normalize());
            } else {
                // create a new list
                
                final List<Object> list = new LinkedList<Object>();
                list.add(child.normalize());
                map.put(key, list);
            }
        }
        
        return map;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(255);
        sb.append("<li>");
        sb.append(super.toString());
        sb.append("<ul>");
        for (final AttributeNode child : this.children) {
            sb.append(child.toString());
        }
        sb.append("</ul>");
        sb.append("</li>");
        return sb.toString();
    }
}
