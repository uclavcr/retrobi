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

package cz.insophy.retrobi.panel.card;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.model.CardPropertyListItem;
import cz.insophy.retrobi.utils.component.CardValueLabel;

/**
 * Card property list view.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardPropertyListView extends ListView<CardPropertyListItem> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param properties
     * list of properties
     */
    public CardPropertyListView(final String id, final List<CardPropertyListItem> properties) {
        super(id, properties);
    }
    
    @Override
    protected void populateItem(final ListItem<CardPropertyListItem> item) {
        // place components
        
        item.add(new Label("label.title", item.getModelObject().getTitle()));
        item.add(new CardValueLabel("label.value", item.getModelObject().getValue()));
        
        // hide row if needed
        
        item.setVisibilityAllowed(item.getModelObject().isVisible(RetrobiWebSession.get().getUserRole()));
    }
}
