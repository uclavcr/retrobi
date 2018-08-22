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

import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * User role editor form.
 */
public class UserRoleEditorForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * user displayed
     */
    private final User user;
    /**
     * user role model
     */
    private final IModel<UserRole> role;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param user
     * user to edit
     */
    public UserRoleEditorForm(final String id, final User user) {
        super(id);
        
        // prepare models
        
        this.user = user;
        this.role = new Model<UserRole>(user.getRole());
        
        // create components
        
        final DropDownChoice<UserRole> roleSelect = new DropDownChoice<UserRole>(
                "select.role",
                this.role,
                Arrays.asList(UserRole.values()));
        
        // place components
        
        this.add(roleSelect);
    }
    
    @Override
    protected void onSubmit() {
        if (StandaloneDocument.equalsById(this.user, RetrobiWebSession.get().getLoggedUser())) {
            this.error("Nemůžete měnit svůj vlastní profil.");
            return;
        }
        
        // change user role
        
        if (this.role.getObject() != null) {
            this.user.setRole(this.role.getObject());
        }
        
        try {
            // update user
            
            RetrobiApplication.db().getUserRepository().updateUser(this.user);
            this.info("Role uživatele byla uložena.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
