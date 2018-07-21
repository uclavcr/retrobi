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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.SearchResult;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.link.BookmarkableSearchLink;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.panel.card.navigator.SearchNavigatorPanel;
import cz.insophy.retrobi.utils.CardRange;
import cz.insophy.retrobi.utils.component.TextLabel;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;

/**
 * Card search page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SearchPage extends AbstractCardPage {
    /**
     * current range for viewing cards
     */
    private IModel<CardRange> range;
    /**
     * current search query
     */
    private IModel<SearchQuery> query;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public SearchPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        // initialize models
        
        this.range = new Model<CardRange>();
        this.query = new Model<SearchQuery>();
        this.resetViewer();
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create components
        
        final Component help = new TextLabel("text", TextType.L_HELP_SEARCH_SUMMARY) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (SearchPage.this.range.getObject().getCount() != 0) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // place components
        
        this.add(help);
    }
    
    /**
     * Resets the range to default.
     */
    @Override
    public void resetViewer() {
        this.range.setObject(new CardRange(
                0,
                0,
                RetrobiWebSession.get().getCardView().getStep(),
                true));
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
    public SearchQuery getSearchQuery() {
        return this.query.getObject();
    }
    
    @Override
    protected List<Card> getCards() {
        if (this.query.getObject() == null) {
            return Collections.emptyList();
        }
        
        if (!this.range.getObject().isFlat()) {
            throw new IllegalStateException("Nepodporovaný rozsah zobrazení výsledků (musí být plochý).");
        }
        
        if (!this.range.getObject().isSingleStep()) {
            throw new IllegalStateException("Nepodporovaný rozsah zobrazení výsledků (krok musí být 1).");
        }
        
        try {
            final SearchResult result;
            
            if (this.query.getObject().isBasketOnly()) {
                // do the search in basket only
                
                result = RetrobiApplication.db().getCardSearchRepository().search(
                        RetrobiWebSession.get().getCardContainer().getBasketCardIds(),
                        this.query.getObject().getIndex(),
                        this.query.getObject().getQuery(),
                        this.query.getObject().isSensitive(),
                        this.query.getObject().getCatalogFilter(),
                        this.query.getObject().getStateFilter(),
                        this.range.getObject().getFirstOffset(),
                        this.range.getObject().getLimit());
            } else {
                // do the search in basket only
                
                result = RetrobiApplication.db().getCardSearchRepository().search(
                        this.query.getObject().getIndex(),
                        this.query.getObject().getQuery(),
                        this.query.getObject().isSensitive(),
                        this.query.getObject().getCatalogFilter(),
                        this.query.getObject().getStateFilter(),
                        this.range.getObject().getFirstOffset(),
                        this.range.getObject().getLimit());
            }
            
            // show debug information
            
            if (result != null) {
                this.info(String.format(
                        "Výsledky: %d (hledání: %.1f s / načítání: %.1f s / parsování: %.1f s)",
                        result.getTotalRows(),
                        result.getSearchDuration() * 0.001,
                        result.getFetchDuration() * 0.001,
                        result.getParseDuration() * 0.001));
            }
            
            // get cards
            
            final List<String> cardIds = SimpleSearchUtils.extractCardIds(result);
            final List<Card> cards = RetrobiApplication.db().getCardRepository().getCards(cardIds);
            
            // update range
            
            if (result != null) {
                this.range.setObject(this.range.getObject().createForOtherCount(result.getTotalRows()));
            }
            
            // return the cards
            
            return Collections.unmodifiableList(cards);
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
            return Collections.emptyList();
        } catch (final GeneralRepositoryException x) {
            if (x.getMessage().contains("Search timed out") || x.getMessage().contains("OS process timed out")) {
                this.error("Vypršel časový limit - pravděpodobně se právě generují indexy, prosíme o strpení.");
            } else if (x.getMessage().contains("Connection refused")) {
                this.error("Spojení odmítnuto - vyhledávací server pravděpodobně neběží, prosíme o strpení.");
            } else if (x.getMessage().contains("Bad query syntax")) {
                this.error("Špatná syntaxe vyhledávacího dotazu.");
            } else {
                this.error("Neočekávaná chyba při vyhledávání, omlouváme se.");
            }
            this.warn(x.getMessage().replace("\\n", "\n").replace("\\\"", "\""));
            return Collections.emptyList();
        }
    }
    
    @Override
    protected AbstractCardNavigatorPanel createNavigatorPanel(final String id, final boolean alwaysCompact) {
        return new SearchNavigatorPanel(id, this, alwaysCompact, this.range, this.query);
    }
    
    @Override
    public TextType getHelpTextType() {
        return TextType.L_HELP_SEARCH;
    }
    
    @Override
    protected String getPageTitle() {
        if (this.range.getObject().getCount() < 1) {
            return "Zadejte dotaz";
        }
        
        return "Vyhledávání / " + this.range.getObject().toString();
    }
    
    @Override
    public String getPageName() {
        return "Vyhledávání";
    }
    
    @Override
    public BookmarkablePageLink<? extends Page> getPageLink(final String id) {
        return new BookmarkableSearchLink(id);
    }
}
