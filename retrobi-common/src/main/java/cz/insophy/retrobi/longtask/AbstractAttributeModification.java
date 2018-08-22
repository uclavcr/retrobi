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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * Abstract base class for all card attribute modifications makes the creation
 * of the modifications easier by implementing the method skeletons.
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractAttributeModification implements CardModification {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAttributeModification.class);
    /**
     * the root of the attribute prototype tree
     */
    private final AttributePrototype root;
    
    /**
     * Creates a new instance.
     * 
     * @param root
     * the root of the attribute prototype tree
     */
    protected AbstractAttributeModification(final AttributePrototype root) {
        this.root = root;
    }
    
    @Override
    public boolean modify(final Card cardToEdit) throws AlreadyModifiedException {
        // get the tree from the document
        
        AbstractAttributeModification.LOG.debug("Extracting the tree for modification...");
        final AttributeNode tree = SimpleAttributeUtils.fromDocumentEnsured(cardToEdit, this.root);
        
        // modify the card internally
        
        AbstractAttributeModification.LOG.debug("Modifying the card: " + cardToEdit.toString());
        final boolean modified = this.internalModify(tree);
        AbstractAttributeModification.LOG.debug("Success? " + modified);
        
        // assign the modified tree to the document
        
        if (modified) {
            AbstractAttributeModification.LOG.debug("Putting the tree back to the document...");
            SimpleAttributeUtils.toDocument(cardToEdit, tree);
            return true;
        }
        
        AbstractAttributeModification.LOG.debug("Modification was not successfull.");
        return false;
    }
    
    /**
     * The internal modification method.
     * 
     * @param tree
     * the attribute tree
     * @return <code>true</code> if the tree was modified successfully,
     * <code>false</code> otherwise
     * @throws AlreadyModifiedException
     * an exception thrown in the case the modification would have no effect on
     * the card (there is no point of executing the change)
     */
    protected abstract boolean internalModify(AttributeNode tree) throws AlreadyModifiedException;
    
    @Override
    public String toString() {
        return this.getTitle();
    }
}
