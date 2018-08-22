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

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.form.RegisterForm;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * Page with the registration form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RegisterPage extends AbstractBasicPage {
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
    public RegisterPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
        
        // create components
        
        this.add(new RegisterForm("form.register"));
        this.add(new TextLabel("text", TextType.L_HELP_USER));
        
        // redirect if logged
        
        if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.setResponsePage(ProfilePage.class);
        }
    }
    
    @Override
    protected String getPageTitle() {
        return "Registrace";
    }
}
