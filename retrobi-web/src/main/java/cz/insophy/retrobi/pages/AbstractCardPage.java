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

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.model.setup.CardSearchSettings;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.model.setup.CardViewSettings;
import cz.insophy.retrobi.model.setup.ImageViewMode;
import cz.insophy.retrobi.panel.card.CardListPanel;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;

/**
 * Abstract base class for all card list based pages.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractCardPage extends AbstractCatalogPage implements CardViewSettings, CardSearchSettings {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCardPage.class);
    /**
     * card viewer component
     */
    private CardListPanel viewer;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    protected AbstractCardPage(final PageParameters parameters) {
        super(parameters);
        
        // update the viewer
        
        this.updateViewer();
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create components
        
        final AbstractCardNavigatorPanel primaryNavigator = this.createNavigatorPanel("panel.navigator", false);
        final AbstractCardNavigatorPanel secondaryNavigator = this.createNavigatorPanel("panel.navigator.foot", true);
        this.viewer = new CardListPanel("panel.card", primaryNavigator);
        
        // place components
        
        this.add(primaryNavigator);
        this.add(secondaryNavigator);
        this.add(this.viewer);
    }
    
    /**
     * Updates all the card list viewers on this page. This is done by invoking
     * the {@link #getCards()} method to gather fresh card list and putting this
     * new list to each component.
     */
    public void updateViewer() {
        AbstractCardPage.LOG.debug("Updating the viewer(s)...");
        this.viewer.setList(this.getCards());
    }
    
    /**
     * Resets the card viewer to the default view. Useful after a big change in
     * the card view settings, like a step change or similar.
     */
    abstract public void resetViewer();
    
    /**
     * Returns the fresh list of cards. This method is probably time expensive
     * and will be called only when necessary. For loading the right cards, all
     * settings / filters considered on each child page must be used.
     * 
     * @return fresh list of cards
     */
    abstract protected List<Card> getCards();
    
    /**
     * Creates a card navigator panel.
     * 
     * @param id
     * component ID
     * @param alwaysCompact
     * make this panel always compact (useful for footer navigator)
     * @return a card navigator panel
     */
    abstract protected AbstractCardNavigatorPanel createNavigatorPanel(final String id, final boolean alwaysCompact);
    
    @Override
    public CardViewMode getCardViewMode() {
        return RetrobiWebSession.get().getCardView().getCardViewMode();
    }
    
    @Override
    public ImageViewMode getImageViewMode() {
        return RetrobiWebSession.get().getCardView().getImageViewMode();
    }
    
    @Override
    public boolean areBasketBasicLinksDisplayed() {
        // show basket links only if single cards are displayed
        
        return this.areSingleCardsDisplayed();
    }
    
    @Override
    public boolean areBasketMoveLinksDisplayed() {
        // hide basket move links by default (override to change)
        
        return false;
    }
    
    @Override
    public boolean isCompactModeEnabled() {
        return RetrobiWebSession.get().getCardView().isCompactModeEnabled();
    }
    
    @Override
    public SearchQuery getSearchQuery() {
        return null;
    }
    
    /**
     * Returns the simple and plain page name for use in navigator panel.
     * 
     * @return simple page name (one word)
     */
    abstract public String getPageName();
    
    /**
     * Returns a link pointing to this page. May return link with parameters.
     * 
     * @param id
     * link component ID
     * @return bookmarkable page link
     */
    abstract public BookmarkablePageLink<? extends Page> getPageLink(String id);
    
    @Override
    public void onLongTaskFinished() {
        // reset the viewer after each long task
        // this prevents card lists to become obsolete
        // NOTE: do not just reset the range, but RELOAD all cards
        
        this.updateViewer();
    }
}
