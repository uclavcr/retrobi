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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;

/**
 * Card list viewer.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardListView extends ListView<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent navigator panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * inner component ID
     */
    private final String innerId;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param innerId
     * inner component ID
     * @param parent
     * parent container
     */
    public CardListView(final String id, final String innerId, final AbstractCardNavigatorPanel parent) {
        super(id);
        
        this.parent = parent;
        this.innerId = innerId;
    }
    
    @Override
    public boolean isVisible() {
        if (this.getList().isEmpty()) {
            return false;
        }
        
        return super.isVisible();
    }
    
    @Override
    protected void populateItem(final ListItem<Card> item) {
        item.add(CardListView.createListComponent(this.innerId, this.parent, item.getModel(), item.getIndex()));
    }
    
    // ===============
    // FACTORY METHODS
    // ===============
    
    /**
     * Creates a list component.
     * 
     * @param id
     * component ID
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     * @param row
     * row index
     * @return a list component
     */
    private static Component createListComponent(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        if (parent.getViewSettings().isDetailEnabled()) {
            return CardListView.createDetailComponent(id, parent, card, row);
        }
        
        return CardListView.createSummaryComponent(id, parent, card, row);
    }
    
    /**
     * Creates a summary card component.
     * 
     * @param id
     * component ID
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     * @param row
     * row index
     * @return a summary component
     */
    private static Component createSummaryComponent(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        return new CardSummaryPanel(id, parent, card, row);
    }
    
    /**
     * Creates a detail card component.
     * 
     * @param id
     * component ID
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     * @param row
     * row index
     * @return a detail component
     */
    private static Component createDetailComponent(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        return new CardDetailPanel(id, parent, card, row);
    }
}
