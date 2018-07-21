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

import cz.insophy.retrobi.form.LoginForm;
import cz.insophy.retrobi.form.LogoutForm;

/**
 * User login panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LoginPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * login form (hidden if user is logged in)
     */
    private final LoginForm loginForm;
    /**
     * logout form (hidden if user is logged out)
     */
    private final LogoutForm logoutForm;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public LoginPanel(final String id) {
        super(id);
        
        // create forms
        
        this.loginForm = new LoginForm("loginForm");
        this.logoutForm = new LogoutForm("logoutForm");
        
        // place components
        
        this.add(this.loginForm);
        this.add(this.logoutForm);
    }
}
