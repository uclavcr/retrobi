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

package cz.insophy.retrobi.panel;

import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.form.MessageForm;

/**
 * Message editor panel-
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessageEditorPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance. A card and image are attached.
     * 
     * @param id
     * component ID
     * @param refCard
     * referenced card
     * @param refImage
     * referenced image
     */
    public MessageEditorPanel(final String id, final Card refCard, final String refImage) {
        super(id);
        
        // place components
        
        this.add(new MessageForm("form", refCard, refImage));
    }
}
