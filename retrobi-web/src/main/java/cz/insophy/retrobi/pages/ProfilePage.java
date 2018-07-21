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

package cz.insophy.retrobi.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.form.DeleteProfileForm;
import cz.insophy.retrobi.form.EditLoginForm;
import cz.insophy.retrobi.form.EditProfileForm;

/**
 * Page with the user profile editor.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ProfilePage extends AbstractBasicPage {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * parameters
     */
    public ProfilePage(final PageParameters parameters) { // NO_UCD
        super(parameters, UserRole.USER);
        
        // create components
        
        this.add(new EditProfileForm("form.profile.edit"));
        this.add(new EditLoginForm("form.login.edit"));
        this.add(new DeleteProfileForm("form.profile.delete"));
        
        this.add(new Label("label.count_login", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return "?";
                }
                
                return String.valueOf(RetrobiWebSession.get().getLoggedUser().getLoginCount());
            }
        }));
        
        this.add(new Label("label.count_edit", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return "?";
                }
                
                return String.valueOf(RetrobiWebSession.get().getLoggedUser().getEditCount());
            }
        }));
        
        this.add(new Label("label.id", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return "?";
                }
                
                return RetrobiWebSession.get().getLoggedUser().getId();
            }
        }));
    }
    
    @Override
    protected String getPageTitle() {
        return "Úprava profilu";
    }
}
