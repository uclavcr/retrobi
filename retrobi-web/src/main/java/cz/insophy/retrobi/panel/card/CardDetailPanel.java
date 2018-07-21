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

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.form.StateChangeForm;
import cz.insophy.retrobi.link.AddCardToBasketLink;
import cz.insophy.retrobi.link.RemoveCardFromBasketLink;
import cz.insophy.retrobi.link.SetCardViewModeLink;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.card.detail.AttributeDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.BasicDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.CommentDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.ImageDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.MessageDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.MoveDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.OcrDetailCardPanel;
import cz.insophy.retrobi.panel.card.detail.TextDetailCardPanel;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.CardImage;
import cz.insophy.retrobi.utils.component.ClassSwitcher;
import cz.insophy.retrobi.utils.component.OnClickWindowOpener;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;

/**
 * Card detail panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardDetailPanel extends Panel {
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
     * @param row
     * row index
     */
    public CardDetailPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card, final int row) {
        super(id);
        
        // create models
        
        final IModel<List<String>> imageNames = new AbstractReadOnlyModel<List<String>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<String> getObject() {
                return RetrobiWebSession.get().getCardView().getImageViewMode().filterImageNames(
                        card.getObject().getAttachmentNamesSorted(),
                        parent.getViewSettings().isDetailEnabled());
            }
        };
        
        final IModel<List<CardViewMode>> modes = new AbstractReadOnlyModel<List<CardViewMode>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<CardViewMode> getObject() {
                return CardViewMode.valuesForRole(RetrobiWebSession.get().getUserRole());
            }
        };
        
        // create components
        
        final ListView<String> imageList = new ListView<String>("list.image", imageNames) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<String> item) {
                final Dimension dim = SimpleImageUtils.getCardImageDimension(Settings.DISPLAY_IMAGE_WIDTH);
                final CardImage image = new CardImage("image", card.getObject(), item.getModelObject(), Settings.DISPLAY_IMAGE_WIDTH, false);
                final WebMarkupContainer link = image.createDownloadLink("link");
                link.add(new OnClickWindowOpener("image" + item.getModelObject(), dim.width + 20, dim.height + 20));
                link.add(image);
                item.add(link);
            }
        };
        
        final ListView<CardViewMode> tabList = new ListView<CardViewMode>("list.tab", modes) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<CardViewMode> item) {
                // create components
                
                final Component label = new Label("label", item.getModelObject().getTitle());
                final WebMarkupContainer link = new SetCardViewModeLink("link", item.getModelObject());
                
                // setup components
                
                item.add(new ClassSwitcher("", "selected", new AbstractReadOnlyModel<Boolean>() {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public Boolean getObject() {
                        return RetrobiWebSession.get().getCardView().getCardViewMode().equals(item.getModelObject());
                    }
                }));
                
                // place components
                
                link.add(label);
                item.add(link);
            }
        };
        
        final ListView<CardViewMode> panelList = new ListView<CardViewMode>("list.panel", Arrays.asList(CardViewMode.values())) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateItem(final ListItem<CardViewMode> item) {
                // create components
                
                final Component panel = CardDetailPanel.createPanel("panel", item.getModelObject(), parent, card, imageNames);
                
                // place components
                
                item.add(panel);
            }
        };
        
        final Component statusLabel = new Label("label.status", card.getObject().getState().getDescription()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
                    return false;
                }
                
                if (card.getObject().getState().isLowerThan(CardState.REWRITTEN)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final AddCardToBasketLink basketAddLink = new AddCardToBasketLink("link.basket.add", card.getObject().getId());
        final RemoveCardFromBasketLink basketRemoveLink = new RemoveCardFromBasketLink("link.basket.remove", card.getObject().getId());
        final WebMarkupContainer form = new StateChangeForm("form", parent, card);
        
        // setup components
        
        basketAddLink.setSiblingLink(basketRemoveLink);
        basketRemoveLink.setSiblingLink(basketAddLink);
        
        // place components
        
        form.add(basketAddLink);
        form.add(basketRemoveLink);
        form.add(statusLabel);
        this.add(form);
        this.add(imageList);
        this.add(tabList);
        this.add(panelList);
    }
    
    /**
     * Creates a panel for the given card view mode.
     * 
     * @param id
     * detail panel component ID
     * @param mode
     * card view mode
     * @param parent
     * parent navigator panel
     * @param card
     * card to be displayed
     * @param imageNames
     * image name list
     * @return a new panel
     */
    private static Panel createPanel(final String id, final CardViewMode mode, final AbstractCardNavigatorPanel parent, final IModel<Card> card, final IModel<List<String>> imageNames) {
        switch (mode) {
            case ATTRIBUTE:
                return new AttributeDetailCardPanel(id, parent, card);
            case BASIC:
                return new BasicDetailCardPanel(id, card);
            case COMMENT:
                return new CommentDetailCardPanel(id, parent, card);
            case IMAGE:
                return new ImageDetailCardPanel(id, parent, card);
            case MESSAGE:
                return new MessageDetailCardPanel(id, card, imageNames);
            case MOVE:
                return new MoveDetailCardPanel(id, parent, card);
            case OCR:
                return new OcrDetailCardPanel(id, parent, card);
            case HELP_CARD:
                return new TextDetailCardPanel(id, mode, TextType.L_HELP_CARD);
            case HELP_MESSAGE:
                return new TextDetailCardPanel(id, mode, TextType.L_HELP_MESSAGE);
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
