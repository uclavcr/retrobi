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

package cz.insophy.retrobi.link;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;

/**
 * Just a Javascript link that adds the given string at the cursor position in
 * the given component. Please note, that the use of this component is bound on
 * the corresponding Javascript piece of code, which must be included on every
 * page this component is used.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AddSymbolLink extends AbstractLink {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * target component ID (XHTML, not Wicket)
     */
    private final String targetId;
    /**
     * string to add after clicking
     */
    private final String stringToAdd;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param targetId
     * ID of the target text component
     * @param stringToAdd
     * string to add when clicked (must be escaped)
     */
    public AddSymbolLink(final String id, final String targetId, final String stringToAdd) {
        super(id);
        
        this.targetId = targetId;
        this.stringToAdd = stringToAdd;
    }
    
    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        
        // modify the 'onclick' action attribute
        
        tag.put("onclick", String.format(
                "insertAtCursor('%s','%s');",
                this.targetId,
                this.stringToAdd));
    }
}
