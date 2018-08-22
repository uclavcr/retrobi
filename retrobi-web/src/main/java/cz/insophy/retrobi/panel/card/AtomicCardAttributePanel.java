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
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;

/**
 * A panel that shows an atomic card attribute.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AtomicCardAttributePanel extends AbstractCardAttributePanel {
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
    public AtomicCardAttributePanel(final String id, final Component container, final Card card, final AtomicAttributeNode node, final AttributeNode root) {
        super(id, container, node);
        
        // create models
        
        final IModel<String> valueModel = new Model<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return node.getValue();
            }
            
            @Override
            public void setObject(final String object) {
                node.setValue(object);
                AtomicCardAttributePanel.this.updateCard(card, root);
            }
        };
        
        // create components
        
        final Component labelTitle = this.createTitleLabel("label.title");
        final Component labelIndex = this.createIndexLabel("label.index");
        final AbstractLink linkAdd = this.createCloneLink("link.add", card, node, root);
        final AbstractLink linkRemove = this.createRemoveLink("link.remove", card, node, root);
        
        final Component labelValue = new AjaxEditableLabel<String>("editable.value", valueModel) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isEnabled() {
                return AtomicCardAttributePanel.this.hasRightsToEdit();
            }
            
            @Override
            protected String defaultNullLabel() {
                return ". . . . .";
            }
        };
        
        // place components
        
        this.add(linkAdd);
        this.add(linkRemove);
        this.add(labelTitle);
        this.add(labelIndex);
        this.add(labelValue);
    }
}
