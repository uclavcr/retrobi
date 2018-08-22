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

import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * An attribute cleaner. This will clean all empty removable nodes from the
 * attribute trees of all cards being modified.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CleanAttributesModification extends AbstractAttributeModification {
    /**
     * Creates a new instance.
     * 
     * @param root
     * the root of the attribute prototype tree
     */
    public CleanAttributesModification(final AttributePrototype root) {
        super(root);
    }
    
    @Override
    public String getTitle() {
        return "Odebrat nepotřebné atributy";
    }
    
    @Override
    protected boolean internalModify(final AttributeNode tree) throws AlreadyModifiedException {
        if (!SimpleAttributeUtils.removeEmptyChildren(tree)) {
            throw new AlreadyModifiedException();
        }
        
        return true;
    }
}
