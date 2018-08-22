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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.Direction;
import cz.insophy.retrobi.link.AddCardToBasketLink;
import cz.insophy.retrobi.link.BookmarkableBatchLink;
import cz.insophy.retrobi.link.BookmarkableCardLink;
import cz.insophy.retrobi.link.RemoveCardFromBasketLink;
import cz.insophy.retrobi.link.ReorderBasketLink;
import cz.insophy.retrobi.model.setup.ImageViewMode;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.CardImage;
import cz.insophy.retrobi.utils.component.CardOcrLabel;
import cz.insophy.retrobi.utils.component.SearchFragmentLabel;
import cz.insophy.retrobi.utils.component.TagTitleAppender;

/**
 * Basic card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardSummaryPanel extends Panel {
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
     * parent component
     * @param card
     * card to be displayed
     * @param row
     * row index
     */
    public CardSummaryPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        super(id);
        
        // place components
        
        this.addMainComponents(parent, card, row);
        this.addImageComponents(parent, card, row);
        this.addBasketComponents(parent, card);
        this.addInfoComponents(parent, card);
        this.addSearchComponents(parent, card);
    }
    
    /**
     * Adds main components.
     * 
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     * @param row
     * row index
     */
    private void addMainComponents(final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        // create components
        
        final AbstractLink link = parent.createDownLink("link.label", row);
        final Component label = new Label("label", link.getDefaultModel());
        
        // place components
        
        link.add(label);
        this.add(link);
    }
    
    /**
     * Adds image components.
     * 
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     * @param row
     * row index
     */
    private void addImageComponents(final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        // initialize models
        
        final IModel<List<String>> imageNames = new AbstractReadOnlyModel<List<String>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<String> getObject() {
                // filter image names
                
                final ImageViewMode imageMode = RetrobiWebSession.get().getCardView().getImageViewMode();
                
                final List<String> someImageNames = imageMode.filterImageNames(
                        card.getObject().getAttachmentNamesSorted(),
                        parent.getViewSettings().isDetailEnabled());
                
                // if no images available, return an empty list
                
                if (someImageNames.isEmpty()) {
                    return Collections.emptyList();
                }
                
                // return the first image as a preview
                
                return Arrays.asList(someImageNames.get(0));
            }
        };
        
        // create components
        
        final Label ocrLabel = new CardOcrLabel("label.ocr", card) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (parent.getViewSettings().isDetailEnabled()) {
                    return false;
                }
                
                if (!imageNames.getObject().isEmpty()) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final WebMarkupContainer imageList = new ListView<String>("list.image", imageNames) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<String> item) {
                // create components
                
                final CardImage image = new CardImage(
                        "image",
                        card.getObject(),
                        item.getModelObject(),
                        Settings.PREVIEW_IMAGE_WIDTH,
                        parent.getViewSettings().getImageViewMode().isCrop());
                
                final WebMarkupContainer imageLink = parent.createDownLink("list.item.link.image", row);
                
                // setup components
                
                image.add(new TagTitleAppender(Model.of(card.getObject().toString())));
                
                // place components
                
                imageLink.add(image);
                item.add(imageLink);
            }
            
            @Override
            public boolean isVisible() {
                return !ocrLabel.isVisible();
            }
        };
        
        // place components
        
        this.add(imageList);
        this.add(ocrLabel);
    }
    
    /**
     * Adds basket components.
     * 
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     */
    private void addBasketComponents(final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        // create components
        
        final AddCardToBasketLink basketAddLink = new AddCardToBasketLink("link.basket.add", card.getObject().getId()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!parent.getViewSettings().areBasketBasicLinksDisplayed()) {
                    return false;
                }
                
                return super.isVisible();
            }
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                super.onClick(target);
                
                // notify the panel
                parent.requestCardViewerUpdate();
            }
        };
        
        final RemoveCardFromBasketLink basketRemoveLink = new RemoveCardFromBasketLink("link.basket.remove", card.getObject().getId()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return basketAddLink.isVisible();
            }
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                super.onClick(target);
                
                // notify the panel
                parent.requestCardViewerUpdate();
            }
        };
        
        final ReorderBasketLink basketMoveUpLink = new ReorderBasketLink("link.basket.up", card.getObject(), Direction.UP) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return parent.getViewSettings().areBasketMoveLinksDisplayed();
            }
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                super.onClick(target);
                
                // notify the panel
                parent.requestCardViewerUpdate();
                
                if (target != null) {
                    // update container
                    final Component top = this.findParent(CardListPanel.class);
                    if (top != null) {
                        target.addComponent(top);
                    }
                }
            }
        };
        
        final ReorderBasketLink basketMoveDownLink = new ReorderBasketLink("link.basket.down", card.getObject(), Direction.DOWN) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return basketMoveUpLink.isVisible();
            }
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                super.onClick(target);
                
                // notify the panel
                parent.requestCardViewerUpdate();
                
                if (target != null) {
                    // update container
                    final Component top = this.findParent(CardListPanel.class);
                    if (top != null) {
                        target.addComponent(top);
                    }
                }
            }
        };
        
        // setup siblings (to be updated via AJAX)
        
        basketAddLink.setSiblingLink(basketRemoveLink);
        basketRemoveLink.setSiblingLink(basketAddLink);
        
        // place components
        
        this.add(basketAddLink);
        this.add(basketRemoveLink);
        this.add(basketMoveUpLink);
        this.add(basketMoveDownLink);
    }
    
    /**
     * Adds search components.
     * 
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     */
    private void addSearchComponents(final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        // create components
        
        final Component searchInfo = new SearchFragmentLabel("search.highlight", card, parent.getSearchSettings().getSearchQuery()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (parent.getSearchSettings().getSearchQuery() == null) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // place components
        
        this.add(searchInfo);
    }
    
    /**
     * Adds informational components.
     * 
     * @param parent
     * parent container
     * @param card
     * card to be displayed
     */
    private void addInfoComponents(final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        // create models
        
        final IModel<String> catalogModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return card.getObject().getCatalog().getShortTitle();
            }
        };
        
        final IModel<String> batchModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return card.getObject().getBatch();
            }
        };
        
        final IModel<String> numberModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.format("Lístek č. %d", card.getObject().getNumberInBatch());
            }
        };
        
        final IModel<String> stateModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (card.getObject().getState() == null) {
                    return "N/A";
                }
                
                return card.getObject().getState().toString();
            }
        };
        
        // create components
        
        final Label catalogLabel = new Label("label.catalog", catalogModel);
        final Label batchLabel = new Label("label", batchModel);
        final Label numberLabel = new Label("label", numberModel);
        
        final BookmarkablePageLink<?> batchLink = new BookmarkableBatchLink("link.batch", card.getObject().getCatalog(), card.getObject().getBatch());
        final BookmarkablePageLink<?> numberLink = new BookmarkableCardLink("link.number", card.getObject().getId());
        
        final WebMarkupContainer wrapper = new WebMarkupContainer("panel.info") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (parent.getViewSettings().isDetailEnabled()) {
                    return false;
                }
                
                if (!parent.getViewSettings().areSingleCardsDisplayed()) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final Component stateLabel = new Label("label.state", stateModel) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && wrapper.isVisible();
            }
        };
        
        // place components
        
        batchLink.add(batchLabel);
        numberLink.add(numberLabel);
        this.add(stateLabel);
        wrapper.add(catalogLabel);
        wrapper.add(batchLink);
        wrapper.add(numberLink);
        this.add(wrapper);
    }
}
