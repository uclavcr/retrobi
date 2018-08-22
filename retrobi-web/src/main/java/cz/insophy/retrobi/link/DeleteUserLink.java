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
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;

/**
 * A link that removes the given user completely after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteUserLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * the user to by removed
     */
    private final IModel<User> userToRemove;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param userToRemove
     * user to remove
     */
    public DeleteUserLink(final String id, final IModel<User> userToRemove) {
        super(id);
        
        this.userToRemove = userToRemove;
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Mazat uživatele může pouze administrátor.");
            return;
        }
        
        if (this.userToRemove.getObject().getId().equals(RetrobiWebSession.get().getLoggedUser().getId())) {
            this.error("Nemůžete smazat sami sebe.");
            return;
        }
        
        try {
            // reset card content fixed by this user
            
            RetrobiOperations.resetRewritesByUser(this.userToRemove.getObject().getId());
            this.info("Neschválené přepisy uživatele byly smazány.");
            
            // remove user from the database
            
            RetrobiOperations.removeUser(this.userToRemove.getObject(), RetrobiWebApplication.getCSVLogger());
            this.info("Uživatel byl odstraněn ze systému.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
    }
}
