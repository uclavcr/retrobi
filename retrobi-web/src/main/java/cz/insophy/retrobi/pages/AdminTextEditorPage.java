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
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.form.TextEditorForm;
import cz.insophy.retrobi.model.setup.AdminViewMode;
import cz.insophy.retrobi.utils.component.TextLabel;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Page for editing the texts.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AdminTextEditorPage extends AbstractAdminPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AdminTextEditorPage(final PageParameters parameters) { // NO_UCD
        super(parameters, AdminViewMode.TEXTS);
        
        // initialize variables
        
        TextType text = null;
        
        // acquire parameters
        
        final String pText = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_TEXT));
        
        if (pText != null) {
            text = TextType.valueOf(pText);
        }
        
        // create components
        
        final Component nameLabel = new Label("label.name", Model.of(text == null ? "(neznámé)" : text.toString()));
        final Component htmlLabel = new WebMarkupContainer("label.html");
        final TextEditorForm form = new TextEditorForm("form", text);
        
        final Component tagList = new ListView<String>("list.tag", TextLabel.getTagKeys()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<String> item) {
                item.add(new Label("label", item.getModelObject()));
            }
        };
        
        // setup components
        
        htmlLabel.setVisible((text != null) && text.isHtml());
        
        // place components
        
        this.add(form);
        this.add(nameLabel);
        this.add(htmlLabel);
        this.add(tagList);
    }
}
