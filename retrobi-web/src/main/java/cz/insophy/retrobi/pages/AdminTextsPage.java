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

import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.panel.TextListView;

/**
 * Page for managing page contents.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminTextsPage extends AbstractAdminPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminTextsPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.TEXTS);
        
        // create components
        
        final Component list = new TextListView("list.text");
        
        // place components
        
        this.add(list);
    }
}
