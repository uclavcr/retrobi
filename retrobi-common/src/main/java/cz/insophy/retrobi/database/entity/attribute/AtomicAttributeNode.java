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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Atomic attribute node that carries a value.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AtomicAttributeNode extends AbstractAttributeNode {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * node value (cannot be <code>null</code>)
     */
    private String value;
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     * parent node
     * @param prototype
     * node prototype
     */
    public AtomicAttributeNode(final AttributeNode parent, final AttributePrototype prototype) {
        super(parent, prototype);
        
        this.value = "";
    }
    
    @Override
    public boolean removeFromChildren(final AttributeNode nodeToRemove) {
        if (this == nodeToRemove) {
            throw new IllegalArgumentException("Uzel nemůže odstranit sám sebe: " + nodeToRemove);
        }
        
        return false;
    }
    
    @Override
    public boolean isEmpty() {
        if (SimpleStringUtils.isEmpty(this.value)) {
            // no value at all
            return true;
        }
        
        if (this.value.equals(this.getPrototype().getValue())) {
            // only a default value
            return true;
        }
        
        return false;
    }
    
    @Override
    public int getSize() {
        return 1;
    }
    
    /**
     * Returns the value. Never returns <code>null</code>.
     * 
     * @return the value
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * Sets the value. If the new value given is <code>null</code>, sets an
     * empty string instead (to prevent <code>null</code> from spreading).
     * 
     * @param newValue
     * the new value or <code>null</code>
     */
    public void setValue(final String newValue) {
        if (newValue == null) {
            this.value = "";
        } else {
            this.value = newValue.trim();
        }
    }
    
    @Override
    public List<AttributeNode> find(final AttributePrototype prototype) {
        if (this.hasPrototype(prototype)) {
            return Arrays.asList((AttributeNode) this);
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public void reset() {
        this.value = "";
    }
    
    @Override
    public Object normalize() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return "<li><b>" + super.toString() + ":</b> " + this.value + "</li>";
    }
}
