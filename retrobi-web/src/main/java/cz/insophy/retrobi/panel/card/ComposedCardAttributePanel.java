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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;

/**
 * A panel that shows a composed card attribute. Composed attribute means that
 * it has no value but child attributes. This allows us to build a hierarchical
 * structure of card attributes.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ComposedCardAttributePanel extends AbstractCardAttributePanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param container
     * parent container
     * @param card
     * card model
     * @param node
     * node to be shown on the panel
     * @param root
     * the attribute tree prototype root
     */
    public ComposedCardAttributePanel(final String id, final Component container, final Card card, final ComposedAttributeNode node, final AttributeNode root) {
        super(id, container, node);
        
        // create models
        
        final IModel<List<AttributeNode>> model = new AbstractReadOnlyModel<List<AttributeNode>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<AttributeNode> getObject() {
                return node.getChildren();
            }
        };
        
        // create components
        
        final AbstractLink linkAdd = this.createCloneLink("link.add", card, node, root);
        final AbstractLink linkRemove = this.createRemoveLink("link.remove", card, node, root);
        final Component labelTitle = this.createTitleLabel("label.title");
        final Component labelIndex = this.createIndexLabel("label.index");
        
        final ListView<AttributeNode> listChild = new ListView<AttributeNode>("list.child", model) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<AttributeNode> item) {
                // create a panel for each child attribute
                
                item.add(AbstractCardAttributePanel.createAttributePanel("list.item.child", container, card, item.getModelObject(), root));
            }
        };
        
        // place components
        
        this.add(linkRemove);
        this.add(labelTitle);
        this.add(labelIndex);
        this.add(linkAdd);
        this.add(listChild);
    }
}
