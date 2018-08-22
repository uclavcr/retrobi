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

import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * The main homepage.
 * 
 * @author Vojtěch Hordějčuk
 */
public class IndexPage extends AbstractBasicPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public IndexPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected String getPageTitle() {
        return "Domů";
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create components
        
        final Component text = new TextLabel("label.text", TextType.L_HOMEPAGE);
        
        // place components
        
        this.add(text);
    }
}
