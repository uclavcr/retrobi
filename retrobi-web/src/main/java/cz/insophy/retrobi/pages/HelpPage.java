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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.utils.component.TextLabel;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Help page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class HelpPage extends AbstractBasicPage {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * displayed text type
     */
    private TextType text;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * parameters
     */
    public HelpPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        // initialize variables
        
        this.text = HelpPage.extractTextType(parameters);
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create components
        
        final Component homeLink = new BookmarkableHelpLink("link.back");
        final Component headLink = new BookmarkableHelpLink("link.head");
        
        final Component titleLabel = new Label(
                "label.title",
                this.text == null ? "Rozcestník" : this.text.toString());
        
        final Component linkList = new ListView<TextType>("list.link", TextType.helpValues()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<TextType> item) {
                final WebMarkupContainer link = new BookmarkableHelpLink("link", item.getModelObject());
                link.add(new Label("label", item.getModelObject().toString()));
                item.add(link);
            }
        };
        
        final Component textLabel = (this.text == null)
                ? new Label("text", "(žádný text)")
                : new TextLabel("text", this.text);
        
        // setup components
        
        if (this.text == null) {
            textLabel.setVisibilityAllowed(false);
        }
        
        // place components
        
        this.add(titleLabel);
        this.add(headLink);
        this.add(homeLink);
        this.add(textLabel);
        this.add(linkList);
    }
    
    @Override
    protected String getPageTitle() {
        if (this.text == null) {
            return "Nápověda";
        }
        
        return this.text.toString();
    }
    
    // ==============
    // URL EXTRACTION
    // ==============
    
    /**
     * Extracts a text type from page parameters.
     * 
     * @param parameters
     * page parameters
     * @return text type or <code>null</code> if undefined or wrong
     */
    private static TextType extractTextType(final PageParameters parameters) {
        final String parameter = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_TEXT));
        
        if (parameter == null) {
            return null;
        }
        
        try {
            return TextType.valueOf(parameter.toUpperCase());
        } catch (final IllegalArgumentException x) {
            return null;
        }
    }
}
