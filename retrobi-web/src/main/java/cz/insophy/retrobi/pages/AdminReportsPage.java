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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.link.BookmarkableCardLink;
import cz.insophy.retrobi.link.RenumberBatchLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.utils.Triple;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.component.OnClickConfirmer;
import cz.insophy.retrobi.utils.component.OnClickWindowOpener;

/**
 * A page that shows various analysis and catalog information.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminReportsPage extends AbstractAdminPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminReportsPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.REPORTS);
        
        // load data
        
        List<User> topUsers = Collections.emptyList();
        
        try {
            topUsers = RetrobiApplication.db().getUserRepository().getTopUsers(30);
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
        
        // create components
        
        final Component topUserList = new ListView<User>("list.top", topUsers) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<User> item) {
                item.add(new Label("label.count", String.valueOf(item.getModelObject().getEditCount())));
                item.add(new Label("label.login", item.getModelObject().getLogin()));
                item.add(new Label("label.email", item.getModelObject().getEmail()));
            }
            
            @Override
            public boolean isVisible() {
                if (this.getList().isEmpty()) {
                    return false;
                }
                
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final ListView<Triple<Catalog, String, Integer>> listOfInvalidFirstNumbers = new ListView<Triple<Catalog, String, Integer>>("list.invalid_first") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Triple<Catalog, String, Integer>> item) {
                final Component link = new RenumberBatchLink("link.renumber", item.getModelObject().getFirst(), item.getModelObject().getSecond());
                link.add(new OnClickConfirmer("Opravdu chcete přečíslovat tuto skupinu?"));
                item.add(new Label("label.catalog", item.getModelObject().getFirst().getShortTitle()));
                item.add(new Label("label.batch", item.getModelObject().getSecond()));
                item.add(new Label("label.number", String.valueOf(item.getModelObject().getThird())));
                item.add(link);
            }
        };
        
        final ListView<Tuple<Catalog, String>> listOfInvalidNumbers = new ListView<Tuple<Catalog, String>>("list.invalid_numbers") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Tuple<Catalog, String>> item) {
                final Component link = new RenumberBatchLink("link.renumber", item.getModelObject().getFirst(), item.getModelObject().getSecond());
                link.add(new OnClickConfirmer("Opravdu chcete přečíslovat tuto skupinu?"));
                item.add(new Label("label.catalog", item.getModelObject().getFirst().getShortTitle()));
                item.add(new Label("label.batch", item.getModelObject().getSecond()));
                item.add(link);
            }
        };
        
        final ListView<Tuple<Triple<Catalog, String, String>, Integer>> listOfDifferentSortBatches = new ListView<Tuple<Triple<Catalog, String, String>, Integer>>("list.different_batch") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Tuple<Triple<Catalog, String, String>, Integer>> item) {
                item.add(new Label("label.catalog", item.getModelObject().getFirst().getFirst().getShortTitle()));
                item.add(new Label("label.batch1", item.getModelObject().getFirst().getSecond()));
                item.add(new Label("label.batch2", item.getModelObject().getFirst().getThird()));
                item.add(new Label("label.count", String.valueOf(item.getModelObject().getSecond())));
            }
        };
        
        final ListView<Tuple<String, String>> listOfCardProblems = new ListView<Tuple<String, String>>("list.error") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<Tuple<String, String>> item) {
                final AbstractLink link = new BookmarkableCardLink("link", item.getModelObject().getSecond());
                link.add(new Label("label", item.getModelObject().getSecond()));
                item.add(new Label("nr", String.valueOf(item.getIndex() + 1)));
                item.add(new Label("label", item.getModelObject().getFirst()));
                item.add(link);
            }
        };
        
        final AbstractLink statsLink = new ExternalLink("link.stats", Settings.WEB_STATS_URL);
        
        // setup components
        
        statsLink.add(new OnClickWindowOpener());
        
        // set data
        
        listOfInvalidFirstNumbers.setList(CardCatalogModel.getInstance().getInvalidFirstNumberBatches());
        listOfInvalidNumbers.setList(CardCatalogModel.getInstance().getInvalidNumberingBatches());
        
        try {
            listOfDifferentSortBatches.setList(RetrobiApplication.db().getAnalystRepository().listBatchSortDifferent());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        try {
            listOfCardProblems.setList(RetrobiApplication.db().getAnalystRepository().listCardProblems(100));
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        // place components
        
        this.add(topUserList);
        this.add(statsLink);
        this.add(listOfInvalidFirstNumbers);
        this.add(listOfDifferentSortBatches);
        this.add(listOfCardProblems);
        this.add(listOfInvalidNumbers);
    }
}
