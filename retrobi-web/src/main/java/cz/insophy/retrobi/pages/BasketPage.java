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

package cz.insophy.retrobi.pages;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.link.BookmarkableBasketLink;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.panel.card.navigator.BasketNavigatorPanel;
import cz.insophy.retrobi.utils.CardRange;

/**
 * Card basket page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketPage extends AbstractCardPage {
    /**
     * card range displayed
     */
    private IModel<CardRange> range;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public BasketPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        this.range = new Model<CardRange>();
        this.resetViewer();
    }
    
    /**
     * Resets the displayed card range.
     */
    @Override
    public void resetViewer() {
        this.range.setObject(new CardRange(
                0,
                RetrobiWebSession.get().getCardContainer().getBasketSize(),
                RetrobiWebSession.get().getCardView().getStep()));
    }
    
    @Override
    protected List<Card> getCards() {
        try {
            // get cards
            
            final List<String> basketCardIds = this.range.getObject().useForPick(RetrobiWebSession.get().getCardContainer().getBasketCardIds());
            final List<Card> basketCards = RetrobiApplication.db().getCardRepository().getCards(basketCardIds);
            
            // update range
            
            final int newCount = RetrobiWebSession.get().getCardContainer().getBasketSize();
            this.range.setObject(this.range.getObject().createForOtherCount(newCount));
            
            // return the cards
            
            return Collections.unmodifiableList(basketCards);
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
            return Collections.emptyList();
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    protected AbstractCardNavigatorPanel createNavigatorPanel(final String id, final boolean alwaysCompact) {
        return new BasketNavigatorPanel(id, this, alwaysCompact, this.range);
    }
    
    @Override
    public boolean isDetailEnabled() {
        return this.range.getObject().isSingle();
    }
    
    @Override
    public boolean areSingleCardsDisplayed() {
        return this.range.getObject().isSingleStep();
    }
    
    @Override
    public boolean areBasketMoveLinksDisplayed() {
        return this.areSingleCardsDisplayed() && !this.isDetailEnabled();
    }
    
    @Override
    public TextType getHelpTextType() {
        return TextType.L_HELP_BASKET;
    }
    
    @Override
    protected String getPageTitle() {
        if (this.range.getObject().getCount() < 1) {
            return "Obsah schránky";
        }
        
        return "Schránka / " + this.range.getObject().toString();
    }
    
    @Override
    public String getPageName() {
        return "Schránka";
    }
    
    @Override
    public BookmarkablePageLink<? extends Page> getPageLink(final String id) {
        return new BookmarkableBasketLink(id);
    }
}
