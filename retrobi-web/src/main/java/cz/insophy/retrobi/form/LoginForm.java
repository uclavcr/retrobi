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
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.link.BookmarkableLostPasswordLink;
import cz.insophy.retrobi.link.BookmarkableRegisterLink;
import cz.insophy.retrobi.pages.ProfilePage;
import cz.insophy.retrobi.pages.RegisterPage;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Login form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LoginForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * login model
     */
    private final IModel<String> login;
    /**
     * password model
     */
    private final IModel<String> password;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public LoginForm(final String id) {
        super(id);
        
        // initialize models
        
        this.login = Model.of("");
        this.password = Model.of("");
        
        // create components
        
        final TextField<String> loginField = new TextField<String>("login", this.login);
        final PasswordTextField passwordField = new PasswordTextField("password", this.password);
        
        final AbstractLink registerLink = new BookmarkableRegisterLink("link.register") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final AbstractLink forgetLink = new BookmarkableLostPasswordLink("link.forget") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // setup components
        
        loginField.setLabel(Model.of("Login"));
        passwordField.setLabel(Model.of("Heslo"));
        
        // place components
        
        this.add(loginField);
        this.add(passwordField);
        this.add(registerLink);
        this.add(forgetLink);
        
        // add validators
        
        loginField.setRequired(true);
        passwordField.setRequired(true);
    }
    
    @Override
    public boolean isVisible() {
        if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            return false;
        }
        
        return super.isVisible();
    }
    
    @Override
    public final void onSubmit() {
        // get login values from the form model
        
        if ((this.login.getObject() != null) && (this.password.getObject() != null)) {
            final String userLogin = this.login.getObject();
            final String userPasswordHash = SimpleStringUtils.getHash(this.password.getObject());
            
            if (RetrobiWebSession.get().login(userLogin, userPasswordHash)) {
                // clear model
                
                this.login.setObject("");
                this.password.setObject("");
                
                // redirect if on a bad page
                
                if (this.getPage().getClass().equals(RegisterPage.class)) {
                    this.setResponsePage(ProfilePage.class);
                }
            }
        }
    }
}
