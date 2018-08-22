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

package cz.insophy.retrobi.form;

import org.apache.wicket.markup.html.form.Form;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;

/**
 * Delete profile form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteProfileForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public DeleteProfileForm(final String id) {
        super(id);
    }
    
    @Override
    protected void onSubmit() {
        // get the logger user
        
        final User loggedUser = RetrobiWebSession.get().getLoggedUser();
        
        // do not allow to remove an administrator
        
        if (loggedUser.hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Administrátor nemůže být smazán.");
            return;
        }
        
        // log the user out first
        
        RetrobiWebSession.get().logout();
        
        try {
            // remove the user and keep his card edits
            
            RetrobiOperations.removeUser(loggedUser, RetrobiWebApplication.getCSVLogger());
            this.info("Profil uživatele byl smazán.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
    }
}
