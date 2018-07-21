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

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Edit profile form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class EditProfileForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * alma mater
     */
    private final IModel<String> alma;
    /**
     * primary branch
     */
    private final IModel<String> branch;
    /**
     * user type
     */
    private final IModel<String> type;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public EditProfileForm(final String id) {
        super(id);
        
        // initialize models
        
        this.alma = Model.of(RetrobiWebSession.get().getLoggedUser().getAlma());
        this.branch = Model.of(RetrobiWebSession.get().getLoggedUser().getBranch());
        this.type = Model.of(RetrobiWebSession.get().getLoggedUser().getType());
        
        // create components
        
        final DropDownChoice<String> almaField = new DropDownChoice<String>("select.alma", this.alma, RegisterForm.PRESET_ALMA);
        final DropDownChoice<String> branchField = new DropDownChoice<String>("select.branch", this.branch, RegisterForm.PRESET_BRANCH);
        final DropDownChoice<String> typeField = new DropDownChoice<String>("select.type", this.type, RegisterForm.PRESET_TYPE);
        
        // place components
        
        this.add(almaField);
        this.add(branchField);
        this.add(typeField);
    }
    
    @Override
    protected void onSubmit() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Není přihlášen žádný uživatel.");
            return;
        }
        
        // update user object
        
        final User user = RetrobiWebSession.get().getLoggedUser();
        
        user.setAlma(SimpleStringUtils.nullToEmpty(this.alma.getObject()).trim());
        user.setBranch(SimpleStringUtils.nullToEmpty(this.branch.getObject()).trim());
        user.setType(SimpleStringUtils.nullToEmpty(this.type.getObject()).trim());
        
        // save user
        
        try {
            RetrobiApplication.db().getUserRepository().updateUser(user);
            this.info("Změny profilu byly uloženy.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
