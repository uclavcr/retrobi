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

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.AbstractReadOnlyModel;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.link.BookmarkableEditTextLink;
import cz.insophy.retrobi.utils.component.LazyListView;

/**
 * Text list view for viewing the page texts.
 * 
 * @author Vojtěch Hordějčuk
 */
public class TextListView extends LazyListView<TextType> {
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
    public TextListView(final String id) {
        super(id);
    }
    
    @Override
    protected List<? extends TextType> getFreshList() {
        return RetrobiApplication.db().getTextRepository().getTexts();
    }
    
    @Override
    protected void populateItem(final ListItem<TextType> item) {
        // create components
        
        final Label dateLabel = new Label("label.name", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return item.getModelObject().toString();
            }
        });
        
        final AbstractLink editLink = new BookmarkableEditTextLink("link.edit", item.getModelObject());
        
        // place components
        
        item.add(dateLabel);
        item.add(editLink);
    }
}
