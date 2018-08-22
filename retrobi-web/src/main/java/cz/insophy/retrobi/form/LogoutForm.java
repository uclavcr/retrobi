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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.http.WebApplication;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.link.BookmarkableEditProfileLink;
import cz.insophy.retrobi.pages.AbstractAdminPage;
import cz.insophy.retrobi.pages.ProfilePage;

/**
 * Logout form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LogoutForm extends Form<Object> {
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
    public LogoutForm(final String id) {
        super(id);
        
        // create components
        
        final WebMarkupContainer profileLink = new BookmarkableEditProfileLink("link.profile") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER) && super.isVisible();
            }
        };
        
        final Component nameLabel = new Label("label.name", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return RetrobiWebSession.get().getLoggedUser().getLogin();
            }
        });
        
        final Component roleLabel = new Label("label.role", new AbstractReadOnlyModel<UserRole>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public UserRole getObject() {
                if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return RetrobiWebSession.get().getLoggedUser().getRole();
                }
                
                return UserRole.GUEST;
            }
        });
        
        // place components
        
        this.add(nameLabel);
        this.add(roleLabel);
        this.add(profileLink);
    }
    
    @Override
    public boolean isVisible() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            return false;
        }
        
        return super.isVisible();
    }
    
    @Override
    protected void onSubmit() {
        // logout and clear the session
        
        RetrobiWebSession.get().logout();
        
        this.info("Odhlášení bylo úspěšné.");
        
        if ((this.getPage() instanceof AbstractAdminPage) || (this.getPage() instanceof ProfilePage)) {
            // redirect to the home page if was on unwanted page
            
            this.setResponsePage(WebApplication.get().getHomePage());
        }
    }
}
