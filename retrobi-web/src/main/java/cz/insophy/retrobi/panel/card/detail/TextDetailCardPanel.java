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

package cz.insophy.retrobi.panel.card.detail;

import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * Text detail card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class TextDetailCardPanel extends AbstractDetailCardPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param mode
     * card view mode
     * @param text
     * text type to show
     */
    public TextDetailCardPanel(final String id, final CardViewMode mode, final TextType text) {
        super(id, mode);
        
        // place components
        
        this.add(new TextLabel("text", text));
    }
}
