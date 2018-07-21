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

import org.apache.wicket.PageParameters;

import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * About page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AboutPage extends AbstractBasicPage {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * parameters
     */
    public AboutPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // place components
        
        this.add(new TextLabel("text", TextType.L_ABOUT));
    }
    
    @Override
    protected String getPageTitle() {
        return "O katalogu";
    }
}
