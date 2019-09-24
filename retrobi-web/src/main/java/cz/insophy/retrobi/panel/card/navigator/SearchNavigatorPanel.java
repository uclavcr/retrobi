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

package cz.insophy.retrobi.panel.card.navigator;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.form.CardSearchForm;
import cz.insophy.retrobi.link.AddSearchToBasketLink;
import cz.insophy.retrobi.link.RemoveSearchFromBasketLink;
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.pages.AbstractCardPage;
import cz.insophy.retrobi.utils.CardRange;

/**
 * Search navigator panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SearchNavigatorPanel extends AbstractCardNavigatorPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * mutable card range model
     */
    private final IModel<CardRange> mutableRange;
    /**
     * mutable query model
     */
    private final IModel<SearchQuery> mutableQuery;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param page
     * parent page
     * @param alwaysCompact
     * make this panel always compact
     * @param mutableRange
     * mutable range model
     * @param mutableQuery
     * mutable query model
     */
    public SearchNavigatorPanel(final String id, final AbstractCardPage page, final boolean alwaysCompact, final IModel<CardRange> mutableRange, final IModel<SearchQuery> mutableQuery) {
        super(id, page, alwaysCompact);
        
        // create models
        
        this.mutableRange = new IModel<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                return mutableRange.getObject();
            }
            
            @Override
            public void setObject(final CardRange object) {
                mutableRange.setObject(object);
                SearchNavigatorPanel.this.requestCardViewerUpdate();
            }
            
            @Override
            public void detach() {
                // NOP
            }
        };
        
        this.mutableQuery = new IModel<SearchQuery>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public SearchQuery getObject() {
                return mutableQuery.getObject();
            }
            
            @Override
            public void setObject(final SearchQuery object) {
                mutableQuery.setObject(object);
                SearchNavigatorPanel.this.requestCardViewerReset();
            }
            
            @Override
            public void detach() {
                // NOP
            }
        };
        
        // place components
        
        this.addInfoComponents();
        this.addFormComponents(page.getPageParameters());
        this.addNavigationLinks();
    }
    
    /**
     * Adds informational components.
     */
    private void addInfoComponents() {
        // initialize models
        
        final IModel<String> countModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (SearchNavigatorPanel.this.mutableQuery.getObject() == null) {
                    return "(bez dotazu)";
                }
                
                return String.valueOf(SearchNavigatorPanel.this.mutableRange.getObject().getCount());
            }
        };
        
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapper("wrap.info", this.getViewSettings());
        final Component countLabel = new Label("label.count", countModel);
        final Component linkAddToBasket = new AddSearchToBasketLink("link.basket.add", this.mutableQuery);
        final Component linkRemoveFromBasket = new RemoveSearchFromBasketLink("link.basket.remove", this.mutableQuery);
        
        // place components
        
        wrapper.add(countLabel);
        wrapper.add(linkAddToBasket);
        wrapper.add(linkRemoveFromBasket);
        this.add(wrapper);
    }
    
    /**
     * Adds form components.
     *
     * @param pageParameters
     * page parameters
     */
    private void addFormComponents(final PageParameters pageParameters) {
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapper("wrap.form", this.getViewSettings());
        final CardSearchForm form = new CardSearchForm("form", this.mutableQuery, pageParameters);
        
        // place components
        
        wrapper.add(form);
        this.add(wrapper);
    }
    
    /**
     * Adds navigation links.
     */
    private void addNavigationLinks() {
        // create components
        
        final WebMarkupContainer wrapper = AbstractCardNavigatorPanel.createCardRangeWrapper("wrap.pager", this.mutableRange);
        
        final AbstractLink linkFirst = AbstractCardNavigatorPanel.createFirstLink("link.first", this.mutableRange);
        final AbstractLink linkLast = AbstractCardNavigatorPanel.createLastLink("link.last", this.mutableRange);
        final AbstractLink linkLeft = AbstractCardNavigatorPanel.createLeftLink("link.left", this.mutableRange);
        final AbstractLink linkRight = AbstractCardNavigatorPanel.createRightLink("link.right", this.mutableRange);
        final AbstractLink linkUp = AbstractCardNavigatorPanel.createUpLink("link.up", this.mutableRange);
        
        // place components
        
        linkFirst.add(new Label("label", linkFirst.getDefaultModel()));
        linkLast.add(new Label("label", linkLast.getDefaultModel()));
        linkLeft.add(new Label("label", linkLeft.getDefaultModel()));
        linkRight.add(new Label("label", linkRight.getDefaultModel()));
        linkUp.add(new Label("label", linkUp.getDefaultModel()));
        
        wrapper.add(linkFirst);
        wrapper.add(linkLast);
        wrapper.add(linkLeft);
        wrapper.add(linkRight);
        wrapper.add(linkUp);
        this.add(wrapper);
    }
    
    @Override
    public AbstractLink createDownLink(final String id, final int offset) {
        return AbstractCardNavigatorPanel.createDownLink(id, offset, this.mutableRange);
    }
}
