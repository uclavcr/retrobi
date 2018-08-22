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
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Edit password form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class EditLoginForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * login
     */
    private final IModel<String> login;
    /**
     * e-mail
     */
    private final IModel<String> email;
    /**
     * password
     */
    private final IModel<String> password1;
    /**
     * password again
     */
    private final IModel<String> password2;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public EditLoginForm(final String id) {
        super(id);
        
        // initialize models
        
        this.login = Model.of(RetrobiWebSession.get().getLoggedUser().getLogin());
        this.email = Model.of(RetrobiWebSession.get().getLoggedUser().getEmail());
        this.password1 = Model.of("");
        this.password2 = Model.of("");
        
        // create components
        
        final TextField<String> loginField = RegisterForm.createLoginField("login", this.login);
        final TextField<String> emailField = RegisterForm.createEmailField("email", this.email);
        final PasswordTextField password1Field = new PasswordTextField("password1", this.password1);
        final PasswordTextField password2Field = new PasswordTextField("password2", this.password2);
        
        // setup components
        
        password1Field.setRequired(false);
        password2Field.setRequired(false);
        password1Field.add(StringValidator.lengthBetween(User.MIN_PASSWORD_LENGTH, User.MAX_PASSWORD_LENGTH));
        password2Field.add(StringValidator.lengthBetween(User.MIN_PASSWORD_LENGTH, User.MAX_PASSWORD_LENGTH));
        this.add(new EqualPasswordInputValidator(password1Field, password2Field));
        
        // place components
        
        this.add(loginField);
        this.add(emailField);
        this.add(password1Field);
        this.add(password2Field);
    }
    
    @Override
    protected void onSubmit() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Není přihlášen žádný uživatel.");
            return;
        }
        
        // update user object
        
        final User user = RetrobiWebSession.get().getLoggedUser();
        
        user.setEmail(this.email.getObject());
        user.setLogin(this.login.getObject());
        
        // save user
        
        try {
            RetrobiApplication.db().getUserRepository().updateUser(user);
            this.info("Změny profilu byly uloženy.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        // change password
        
        final String p1 = this.password1.getObject();
        final String p2 = this.password2.getObject();
        
        if (!SimpleStringUtils.isEmpty(p1) && !SimpleStringUtils.isEmpty(p2)) {
            try {
                RetrobiApplication.db().getUserRepository().changeUserPassword(RetrobiWebSession.get().getLoggedUser(), p1);
                this.info("Heslo bylo změněno.");
            } catch (final GeneralRepositoryException x) {
                this.error(x.getMessage());
            }
        }
    }
}
