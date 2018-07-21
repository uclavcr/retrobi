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

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.utils.component.ClassSwitcher;

/**
 * Abstract base class for all administration pages. This base page class
 * includes some common elements, such as the tab bar for switching the
 * administration sections. All the pages are accessible for administrator only.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractAdminPage extends AbstractBasicPage {
    /**
     * administration mode
     */
    private final AdminViewMode mode;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     * @param mode
     * administration mode
     */
    protected AbstractAdminPage(final PageParameters parameters, final AdminViewMode mode) {
        super(parameters, mode.getMinRole());
        
        // initialize models
        
        this.mode = mode;
        
        final IModel<List<AdminViewMode>> modes = new AbstractReadOnlyModel<List<AdminViewMode>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<AdminViewMode> getObject() {
                return AdminViewMode.valuesForRole(RetrobiWebSession.get().getUserRole());
            }
        };
        
        // create components
        
        final ListView<AdminViewMode> list = new ListView<AdminViewMode>("list.mode", modes) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<AdminViewMode> item) {
                // create components
                
                final Label label = new Label("label", item.getModelObject().toString());
                final AbstractLink link = item.getModelObject().createLink("link");
                
                // place components
                
                link.add(label);
                item.add(link);
                
                // add class appenders
                
                item.add(new ClassSwitcher("", "selected", item.getModelObject().equals(mode)));
            }
        };
        
        // place components
        
        this.add(list);
    }
    
    @Override
    protected String getPageTitle() {
        return this.mode.toString();
    }
}
