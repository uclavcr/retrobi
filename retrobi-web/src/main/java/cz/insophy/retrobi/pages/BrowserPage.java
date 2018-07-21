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
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.link.BookmarkableCatalogLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.panel.card.navigator.BrowserNavigatorPanel;
import cz.insophy.retrobi.utils.CardCatalogRange;
import cz.insophy.retrobi.utils.CardRange;
import cz.insophy.retrobi.utils.DataLoader;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card browser page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BrowserPage extends AbstractCardPage {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrowserPage.class);
    /**
     * card range displayed
     */
    private IModel<CardCatalogRange> range;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public BrowserPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        this.range = new Model<CardCatalogRange>();
        this.resetViewer();
    }
    
    @Override
    public void resetViewer() {
        this.range.setObject(this.getDefaultRange(this.getPageParameters()));
    }
    
    @Override
    protected AbstractCardNavigatorPanel createNavigatorPanel(final String id, final boolean alwaysCompact) {
        return new BrowserNavigatorPanel(id, this, alwaysCompact, this.range);
    }
    
    @Override
    public boolean isDetailEnabled() {
        return this.range.getObject().getRange().isSingle();
    }
    
    @Override
    public boolean areSingleCardsDisplayed() {
        return this.range.getObject().getRange().isSingleStep();
    }
    
    @Override
    protected List<Card> getCards() {
        try {
            // get cards
            
            BrowserPage.LOG.debug("Loading cards from the batch for range: " + this.range.getObject().toString());
            
            final List<String> cardIds = this.range.getObject().getRange().useForPick(new DataLoader<String>() {
                @Override
                public List<String> loadData(final int offset, final int limit) {
                    try {
                        return RetrobiApplication.db().getCardRepository().getCardIds(
                                BrowserPage.this.range.getObject().getCatalog(),
                                BrowserPage.this.range.getObject().getBatch(),
                                offset,
                                limit);
                    } catch (final GeneralRepositoryException x) {
                        return Collections.emptyList();
                    }
                }
            });
            
            final List<Card> cards = RetrobiApplication.db().getCardRepository().getCards(cardIds);
            
            // update range
            
            final int newCount = RetrobiApplication.db().getCardRepository().getBatchSize(
                    this.range.getObject().getCatalog(),
                    this.range.getObject().getBatch());
            
            BrowserPage.LOG.debug("Updating range with a new count: " + newCount);
            this.range.setObject(this.range.getObject().createForOtherCount(newCount));
            BrowserPage.LOG.debug("Resulting range: " + this.range.getObject().toString());
            
            // return the cards
            
            return Collections.unmodifiableList(cards);
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
            return Collections.emptyList();
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Returns the default card range for the given page parameters.
     * 
     * @param parameters
     * page parameters
     * @return card catalog range
     */
    private CardCatalogRange getDefaultRange(final PageParameters parameters) {
        // initialize variables
        
        Catalog catalog = Catalog.O;
        String batch = "";
        Card card = null;
        int offset = 0;
        int count = 0;
        
        // acquire parameters
        
        final String pCatalog = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_CATALOG));
        final String pBatch = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_BATCH));
        final String pCardId = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_CARD));
        
        // process parameters
        
        if (pCardId != null) {
            try {
                // load the card
                
                card = RetrobiApplication.db().getCardRepository().getCard(pCardId);
                
                // change the remaining parameters from the card
                
                offset = card.getNumberInBatch() - 1;
                catalog = card.getCatalog();
                batch = card.getBatch();
            } catch (final NotFoundRepositoryException x) {
                this.error(x.getMessage());
            } catch (final GeneralRepositoryException x) {
                this.error(x.getMessage());
            }
        }
        
        if ((card == null) && (pCatalog != null)) {
            catalog = Catalog.valueOf(pCatalog);
        }
        
        if ((card == null) && (pBatch != null)) {
            batch = pBatch;
        }
        
        // load the card count
        
        try {
            count = RetrobiApplication.db().getCardRepository().getBatchSize(catalog, batch);
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
        
        // get the previous and next batches
        
        final String previousBatch = CardCatalogModel.getInstance().getPreviousBatch(catalog, pBatch);
        final String nextBatch = CardCatalogModel.getInstance().getNextBatch(catalog, pBatch);
        
        // initialize range (safely)
        
        try {
            return new CardCatalogRange(
                    catalog,
                    batch,
                    previousBatch,
                    nextBatch,
                    (card == null)
                            ? new CardRange(offset, count, RetrobiWebSession.get().getCardView().getStep())
                            : new CardRange(offset, count, 1, RetrobiWebSession.get().getCardView().getStep()));
        } catch (final IllegalArgumentException x) {
            RetrobiWebSession.get().error("Nesprávný rozsah zobrazení. Doporučujeme přečíslovat skupinu.");
            throw new RestartResponseException(WebApplication.get().getHomePage());
        }
    }
    
    @Override
    protected String getPageTitle() {
        return this.range.getObject().toPlainString();
    }
    
    @Override
    public String getPageName() {
        return "Katalog";
    }
    
    @Override
    public BookmarkablePageLink<? extends Page> getPageLink(final String id) {
        return new BookmarkableCatalogLink(id);
    }
}
