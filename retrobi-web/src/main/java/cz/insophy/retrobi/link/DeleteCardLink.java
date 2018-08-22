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

import java.util.Arrays;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;

/**
 * Removes the specified card after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteCardLink extends Link<String> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param cardId
     * ID of a card to be deleted
     */
    public DeleteCardLink(final String id, final String cardId) {
        super(id, Model.of(cardId));
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Nemáte dostatečná oprávnění pro mazání lístků.");
            return;
        }
        
        final String cardId = this.getModelObject();
        
        try {
            // remove the card
            
            RetrobiOperations.deleteCard(cardId, RetrobiWebSession.get().getLoggedUser(), RetrobiWebApplication.getCSVLogger());
            
            // remove the card from all user collections
            
            RetrobiWebSession.get().getCardContainer().removeFromBasket(Arrays.asList(cardId));
            
            // notify user
            
            this.info("Lístek byl úspěšně smazán.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
    }
}
