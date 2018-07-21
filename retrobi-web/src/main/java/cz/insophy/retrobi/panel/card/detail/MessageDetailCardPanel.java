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

package cz.insophy.retrobi.panel.card.detail;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.MessageStateOption;
import cz.insophy.retrobi.link.BookmarkableMessageLink;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.MessageListPanel;

/**
 * Message card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessageDetailCardPanel extends AbstractDetailCardPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card to be displayed
     * @param imageNames
     * image name list
     */
    public MessageDetailCardPanel(final String id, final IModel<Card> card, final IModel<List<String>> imageNames) {
        super(id, CardViewMode.MESSAGE);
        
        // prepare image name links
        
        final List<String> data = new LinkedList<String>();
        
        data.add(null);
        
        for (final String imageName : imageNames.getObject()) {
            data.add(imageName);
        }
        
        // create components
        
        final ListView<String> linkList = new ListView<String>("list.link.message", data) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<String> item) {
                final AbstractLink link = new BookmarkableMessageLink(
                        "link",
                        card.getObject(),
                        item.getModelObject());
                
                link.add(new Label(
                        "label",
                        (item.getModelObject() == null)
                                ? "Nahlásit chybu lístku"
                                : String.format("Nahlásit chybu obrázku '%s'", item.getModelObject())));
                
                item.add(link);
            }
        };
        
        final MessageListPanel problemList = new MessageListPanel("panel.message.problem") {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (this.getPagedView().isEmpty()) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final MessageListPanel eventList = new MessageListPanel("panel.message.event") {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (this.getPagedView().isEmpty()) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // setup components
        
        problemList.setCardId(card.getObject().getId());
        problemList.setState(MessageStateOption.UNCONFIRMED_PROBLEMS);
        eventList.setCardId(card.getObject().getId());
        eventList.setState(MessageStateOption.UNCONFIRMED_EVENTS);
        
        // place components
        
        this.add(linkList);
        this.add(problemList);
        this.add(eventList);
    }
}
