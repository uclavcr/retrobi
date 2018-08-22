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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.form.UserLimitEditorForm;
import cz.insophy.retrobi.form.UserRoleEditorForm;
import cz.insophy.retrobi.link.DeleteUserLink;
import cz.insophy.retrobi.link.ResetFixedOcrLink;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.component.OnClickConfirmer;
import cz.insophy.retrobi.utils.component.PagedLazyListView;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * User list view.
 * 
 * @author Vojtěch Hordějčuk
 */
public class UserListView extends PagedLazyListView<User> {
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
    public UserListView(final String id) {
        super(id, 10);
    }
    
    @Override
    protected List<? extends User> getFreshList(final int page, final int limit) {
        try {
            // get the result
            
            final Tuple<Integer, List<String>> result = RetrobiApplication.db().getUserRepository().getUserIds(page, limit);
            
            // update the pager
            
            this.reset(result.getFirst());
            
            // return the list of results
            
            final List<User> users = RetrobiApplication.db().getUserRepository().getUsers(result.getSecond());
            return Collections.unmodifiableList(users);
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
        
        return Collections.emptyList();
    }
    
    @Override
    protected void populateItem(final ListItem<User> item) {
        final User user = item.getModelObject();
        
        // create components
        
        final Label loginLabel = new Label("label.login", SimpleStringUtils.neverEmpty(user.getLogin()));
        final Label emailLabel = new Label("label.email", SimpleStringUtils.neverEmpty(user.getEmail()));
        final Label countLoginLabel = new Label("label.count_login", String.valueOf(user.getLoginCount()));
        final Label countEditLabel = new Label("label.count_edit", String.valueOf(user.getEditCount()));
        final Component roleForm = new UserRoleEditorForm("form.role", user);
        final Component limitForm = new UserLimitEditorForm("form.limit", user);
        
        final AbstractLink resetOcrLink = new ResetFixedOcrLink("link.reset_ocr", user.getId()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                // update after click
                UserListView.this.reset();
            }
        };
        
        final AbstractLink removeLink = new DeleteUserLink("link.remove", Model.of(user)) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                // update after click
                UserListView.this.reset();
            }
        };
        
        // setup components
        
        resetOcrLink.add(new OnClickConfirmer("Opravdu chcete smazat neschválené přepisy uživatele?"));
        removeLink.add(new OnClickConfirmer("Opravdu chcete smazat uživatele včetně neschválených přepisů?"));
        
        // place components
        
        item.add(loginLabel);
        item.add(emailLabel);
        item.add(roleForm);
        item.add(countLoginLabel);
        item.add(countEditLabel);
        item.add(limitForm);
        item.add(resetOcrLink);
        item.add(removeLink);
    }
}
