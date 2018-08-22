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

import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.model.setup.CardViewMode;

/**
 * Abstract base class for all detail card panels.
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractDetailCardPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card view mode of this panel (each mode has a distinct panel)
     */
    private final CardViewMode mode;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param mode
     * panel mode
     */
    protected AbstractDetailCardPanel(final String id, final CardViewMode mode) {
        super(id);
        this.mode = mode;
        this.setOutputMarkupId(true);
    }
    
    @Override
    public boolean isVisible() {
        if (!RetrobiWebSession.get().getCardView().getCardViewMode().equals(this.mode)) {
            // hide if the panel mode is not currently active
            
            return false;
        }
        
        return super.isVisible();
    }
}
