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

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.link.DownloadUserCSVLink;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.panel.UserListView;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.component.PagedLazyListViewPager;

/**
 * Page for managing users.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminUsersPage extends AbstractAdminPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminUsersPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.USERS);
        
        // create components
        
        final Component downloadLink = new DownloadUserCSVLink("link.download");
        final UserListView userList = new UserListView("list.user");
        final Component pager = new PagedLazyListViewPager("panel.pager", userList);
        
        final ListView<Tuple<String, Integer>> counter = new ListView<Tuple<String, Integer>>("list.counter") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Tuple<String, Integer>> item) {
                item.add(new Label("label.title", item.getModelObject().getFirst()));
                item.add(new Label("label.count", String.valueOf(item.getModelObject().getSecond())));
            }
        };
        
        // setup components
        
        try {
            counter.setList(RetrobiApplication.db().getUserRepository().getUserCount());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        // place components
        
        this.add(downloadLink);
        this.add(pager);
        this.add(counter);
        this.add(userList);
    }
}
