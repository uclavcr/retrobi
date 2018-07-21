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

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.model.setup.CardViewMode;

/**
 * A link that sets the card view mode after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SetCardViewModeLink extends Link<CardViewMode> {
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
     * new view mode to set
     */
    public SetCardViewModeLink(final String id, final CardViewMode mode) {
        super(id, Model.of(mode));
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(this.getModelObject().getMinRole())) {
            this.error("Nemáte dostatečná oprávnění pro tento režim zobrazení.");
            return;
        }
        
        // set the new card view mode
        
        RetrobiWebSession.get().getCardView().setCardViewMode(this.getModelObject());
    }
}
