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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.component.AjaxWaitLink;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * Abstract base class for card attribute panels.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractCardAttributePanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * the root panel
     */
    private final Component container;
    /**
     * attribute node
     */
    private final AttributeNode node;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param container
     * parent container
     * @param node
     * node associated with this panel
     */
    protected AbstractCardAttributePanel(final String id, final Component container, final AttributeNode node) {
        super(id);
        
        this.container = container;
        this.node = node;
    }
    
    /**
     * Creates a panel for the given attribute and continues recursively.
     * 
     * @param id
     * component ID
     * @param container
     * parent container
     * @param card
     * card
     * @param node
     * the attribute node
     * @param root
     * the root attribute node
     * @return a panel
     */
    public static AbstractCardAttributePanel createAttributePanel(final String id, final Component container, final Card card, final AttributeNode node, final AttributeNode root) {
        if ((root == null) || (node == null)) {
            throw new NullPointerException("Žádný uzel určený k zobrazení nesmí být NULL.");
        }
        
        if (node instanceof AtomicAttributeNode) {
            return new AtomicCardAttributePanel(id, container, card, (AtomicAttributeNode) node, root);
        } else if (node instanceof ComposedAttributeNode) {
            return new ComposedCardAttributePanel(id, container, card, (ComposedAttributeNode) node, root);
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Creates the clone link. The link visibility and functionality is fully
     * determined by the user currently logged in.
     * 
     * @param id
     * component ID
     * @param card
     * card
     * @param nodeToClone
     * a node to clone
     * @param root
     * the root node
     * @return a clone link
     */
    protected AbstractLink createCloneLink(final String id, final Card card, final AttributeNode nodeToClone, final AttributeNode root) {
        return new AjaxWaitLink<Object>(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (!AbstractCardAttributePanel.this.hasRightsToEdit()) {
                    this.error("Nemáte dostatečná oprávnění ke změně struktury.");
                    return;
                }
                
                // do the cloning and update the card
                
                if (nodeToClone.createSibling() != null) {
                    AbstractCardAttributePanel.this.updateCard(card, root);
                } else {
                    this.error("Klonování atributu se nezdařilo.");
                }
                
                // update the panel
                
                if (target != null) {
                    target.addComponent(AbstractCardAttributePanel.this.container);
                }
            }
            
            @Override
            public boolean isVisible() {
                return nodeToClone.canBeClonedByUser() && AbstractCardAttributePanel.this.hasRightsToEdit();
            }
        };
    }
    
    /**
     * Creates the remove link. The link visibility and functionality is fully
     * determined by the user currently logged in.
     * 
     * @param id
     * component ID
     * @param card
     * card
     * @param nodeToRemove
     * a node to remove
     * @param root
     * the root node
     * @return a remove link
     */
    protected AbstractLink createRemoveLink(final String id, final Card card, final AttributeNode nodeToRemove, final AttributeNode root) {
        return new AjaxWaitLink<Object>(id) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                if (!AbstractCardAttributePanel.this.hasRightsToEdit()) {
                    this.error("Nemáte dostatečná oprávnění ke změně struktury.");
                    return;
                }
                
                // do the removal and update the card
                
                if (root.removeFromChildren(nodeToRemove)) {
                    AbstractCardAttributePanel.this.updateCard(card, root);
                } else {
                    this.error("Mazání atributu se nezdařilo.");
                }
                
                // update the panel
                
                if (target != null) {
                    target.addComponent(AbstractCardAttributePanel.this.container);
                }
            }
            
            @Override
            public boolean isVisible() {
                return nodeToRemove.canBeRemovedByUser() && AbstractCardAttributePanel.this.hasRightsToEdit();
            }
        };
    }
    
    /**
     * Creates the node title label.
     * 
     * @param id
     * component ID
     * @return a label
     */
    protected Component createTitleLabel(final String id) {
        return new Label(id, this.node.getTitle());
    }
    
    /**
     * Creates an index label.
     * 
     * @param id
     * component ID
     * @return a label
     */
    protected Component createIndexLabel(final String id) {
        final IModel<String> model = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (AbstractCardAttributePanel.this.node.getPrototype().getIndexes() == null) {
                    return "(bez indexů)";
                }
                
                return "Index: " + AbstractCardAttributePanel.this.node.getPrototype().getIndexes().toString();
            }
        };
        
        return new Label(id, model) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (AbstractCardAttributePanel.this.node.getPrototype().getIndexes() == null) {
                    return false;
                }
                
                if (AbstractCardAttributePanel.this.node.getPrototype().getIndexes().isEmpty()) {
                    return false;
                }
                
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
    }
    
    /**
     * Updates the card by the tree provided.
     * 
     * @param card
     * card to be edited
     * @param root
     * attribute tree root
     */
    protected void updateCard(final Card card, final AttributeNode root) {
        if (!this.hasRightsToEdit()) {
            this.error("Nemáte dostatečná oprávnění ke změně lístku.");
            return;
        }
        
        try {
            // put the attributes to the card
            
            SimpleAttributeUtils.toDocument(card, root);
            
            // update the card in the database
            
            RetrobiApplication.db().getCardRepository().updateCard(card);
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
    
    /**
     * Checks if the panel value is empty and should be hidden.
     * 
     * @return <code>true</code> if the panel value is empty, <code>false</code>
     * otherwise
     */
    private boolean isEmpty() {
        return this.node.isEmpty();
    }
    
    /**
     * Checks if the logged user (if any) has rights to display the value.
     * 
     * @return <code>true</code> if the value can be viewed by the currently
     * logged user, <code>false</code> otherwise
     */
    private boolean hasRightsToView() {
        if (this.hasRightsToEdit()) {
            return true;
        }
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(this.node.getPrototype().getRole())) {
            return false;
        }
        
        return !this.isEmpty();
    }
    
    /**
     * Checks if the logged user (if any) has rights to edit the value.
     * 
     * @return <code>true</code> if the value can be edited by the currently
     * logged user, <code>false</code> otherwise
     */
    protected boolean hasRightsToEdit() {
        return RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR);
    }
    
    @Override
    public boolean isVisible() {
        if (!this.hasRightsToView()) {
            return false;
        }
        
        if (this.isEmpty() && !RetrobiWebSession.get().getCardView().areEmptyAttributesShown()) {
            return false;
        }
        
        return super.isVisible();
    }
}
