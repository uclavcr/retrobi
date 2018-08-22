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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.card.AbstractCardAttributePanel;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * Attribute card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AttributeDetailCardPanel extends AbstractDetailCardPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent navigator panel
     * @param card
     * card to be displayed
     */
    public AttributeDetailCardPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, CardViewMode.ATTRIBUTE);
        
        // gather the attribute tree root
        
        final AttributeNode root = SimpleAttributeUtils.fromDocumentEnsured(
                card.getObject(),
                RetrobiWebConfiguration.getInstance().getAttributeRoot());
        
        // create components
        
        final AbstractCardAttributePanel panel = AbstractCardAttributePanel.createAttributePanel(
                "panel.attribute",
                this,
                card.getObject(),
                root,
                root);
        
        final Component linkViewAll = new Link<Object>("link.view_all") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isEnabled() {
                return !RetrobiWebSession.get().getCardView().areEmptyAttributesShown();
            }
            
            @Override
            public void onClick() {
                RetrobiWebSession.get().getCardView().setShowEmptyAttributes(true);
                parent.requestCardViewerUpdate();
            }
        };
        
        final Component linkViewNotEmpty = new Link<Object>("link.view_not_empty") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isEnabled() {
                return RetrobiWebSession.get().getCardView().areEmptyAttributesShown();
            }
            
            @Override
            public void onClick() {
                RetrobiWebSession.get().getCardView().setShowEmptyAttributes(false);
                parent.requestCardViewerUpdate();
            }
        };
        
        // place components
        
        this.add(linkViewAll);
        this.add(linkViewNotEmpty);
        this.add(panel);
    }
}
