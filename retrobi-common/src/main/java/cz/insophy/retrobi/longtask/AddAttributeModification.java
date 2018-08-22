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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.AlreadyModifiedException;

/**
 * An attribute adder. This will add a node value. The node must be atomic. The
 * behavior is as follows: If the value is already among the atomic nodes, the
 * change is skipped. If there is exactly one empty attribute to be set, the
 * change is made and the modification is successful. Other case are error.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AddAttributeModification extends AbstractAttributeModification {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AddAttributeModification.class);
    /**
     * an attribute to add
     */
    private final AttributePrototype attributeToAdd;
    /**
     * a value to set into the new attribute node
     */
    private final String valueToAdd;
    
    /**
     * Creates a new instance.
     * 
     * @param root
     * the root of the attribute prototype tree
     * @param attributeToAdd
     * an attribute to add
     * @param valueToAdd
     * an attribute value to set
     */
    public AddAttributeModification(final AttributePrototype root, final AttributePrototype attributeToAdd, final String valueToAdd) {
        super(root);
        
        this.attributeToAdd = attributeToAdd;
        this.valueToAdd = valueToAdd;
    }
    
    @Override
    protected boolean internalModify(final AttributeNode tree) throws AlreadyModifiedException {
        // check if the attribute is atomic
        
        if (!this.attributeToAdd.isAtomic()) {
            AddAttributeModification.LOG.debug("The attribute must be atomic to add.");
            return false;
        }
        
        // find the candidate pivot nodes
        
        final List<AttributeNode> pivotNodes = tree.find(this.attributeToAdd);
        
        // check the pivot nodes
        
        if (pivotNodes.isEmpty()) {
            AddAttributeModification.LOG.debug("No feasible nodes found, not adding at all.");
            return false;
        }
        
        // check if the value is already among nodes
        // collect the empty nodes at the same time
        
        final List<AtomicAttributeNode> emptyNodes = new LinkedList<AtomicAttributeNode>();
        
        for (final AttributeNode pivotNode : pivotNodes) {
            if (pivotNode instanceof AtomicAttributeNode) {
                final AtomicAttributeNode atomicPivotNode = (AtomicAttributeNode) pivotNode;
                
                if (atomicPivotNode.isEmpty()) {
                    // empty node was found, add it into the list
                    
                    emptyNodes.add(atomicPivotNode);
                } else if (this.valueToAdd.equals(atomicPivotNode.getValue())) {
                    // value we want to add is already there, so we end
                    
                    throw new AlreadyModifiedException();
                }
            }
        }
        
        switch (emptyNodes.size()) {
            case 0:
                // no empty node = must create new sibling node
                
                final AttributeNode lastPivotNode = pivotNodes.get(pivotNodes.size() - 1);
                
                if (lastPivotNode instanceof AtomicAttributeNode) {
                    final AttributeNode newPivotSibling = lastPivotNode.createSibling();
                    
                    if (newPivotSibling instanceof AtomicAttributeNode) {
                        ((AtomicAttributeNode) newPivotSibling).setValue(this.valueToAdd);
                        AddAttributeModification.LOG.debug("The last pivot node was cloned.");
                        return true;
                    }
                }
                AddAttributeModification.LOG.debug("The last pivot node is not atomic.");
                return false;
            case 1:
                // exactly one empty node = that is what we want
                
                AddAttributeModification.LOG.debug("The one empty attribute was found and will be set.");
                emptyNodes.get(0).setValue(this.valueToAdd);
                return true;
            default:
                // more than one empty node = we do not know which is best
                
                AddAttributeModification.LOG.debug("More than one empty attribute, we end.");
                return false;
        }
    }
    
    @Override
    public String getTitle() {
        return String.format("Přidat hodnotu '%s' do atributu '%s'", this.valueToAdd, this.attributeToAdd.getTitle());
    }
}
