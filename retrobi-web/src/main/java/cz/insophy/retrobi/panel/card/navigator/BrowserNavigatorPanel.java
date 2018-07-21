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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.form.BrowserToBasketForm;
import cz.insophy.retrobi.form.SkipToLetterForm;
import cz.insophy.retrobi.link.BookmarkableBatchLink;
import cz.insophy.retrobi.link.BookmarkableLetterLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.pages.AbstractCardPage;
import cz.insophy.retrobi.utils.CardCatalogRange;
import cz.insophy.retrobi.utils.CardRange;

/**
 * Browser navigator panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BrowserNavigatorPanel extends AbstractCardNavigatorPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * mutable catalog range
     */
    private final IModel<CardCatalogRange> mutableRange;
    /**
     * form for batch basket operations
     */
    private BrowserToBasketForm basketForm;
    
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
     */
    public BrowserNavigatorPanel(final String id, final AbstractCardPage page, final boolean alwaysCompact, final IModel<CardCatalogRange> mutableRange) {
        super(id, page, alwaysCompact);
        
        // initialize models
        
        this.mutableRange = new IModel<CardCatalogRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardCatalogRange getObject() {
                return mutableRange.getObject();
            }
            
            @Override
            public void setObject(final CardCatalogRange object) {
                mutableRange.setObject(object);
                BrowserNavigatorPanel.this.basketForm.updateByRange();
                BrowserNavigatorPanel.this.requestCardViewerUpdate();
            }
            
            @Override
            public void detach() {
                // NOP
            }
        };
        
        // initialize components
        
        this.addBasketComponents();
        this.addLetterComponents();
        this.addBatchComponents();
        this.addNavigationLinks();
    }
    
    /**
     * Adds basket components.
     */
    private void addBasketComponents() {
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapper("wrap.basket", this.getViewSettings());
        this.basketForm = new BrowserToBasketForm("form.basket", this.mutableRange);
        
        // place components
        
        wrapper.add(this.basketForm);
        this.add(wrapper);
    }
    
    /**
     * Adds letter components.
     */
    private void addLetterComponents() {
        // initialize models
        
        final String letter = CardCatalogModel.getInstance().getLetterOfBatch(this.mutableRange.getObject().getBatch());
        
        // create components
        
        final WebMarkupContainer wrapper = this.createNonCompactWrapper("wrap.letter", this.getViewSettings());
        
        final Component linkLetterPrev = BrowserNavigatorPanel.createLetterLink(
                "link.letter.p",
                "label",
                this.mutableRange.getObject().getCatalog(),
                CardCatalogModel.getInstance().getPreviousLetter(letter));
        
        final Component linkLetterNext = BrowserNavigatorPanel.createLetterLink(
                "link.letter.n",
                "label",
                this.mutableRange.getObject().getCatalog(),
                CardCatalogModel.getInstance().getNextLetter(letter));
        
        final Component form = new SkipToLetterForm(
                "form.skip",
                this.mutableRange.getObject().getCatalog(),
                letter);
        
        // place components
        
        wrapper.add(linkLetterPrev);
        wrapper.add(linkLetterNext);
        wrapper.add(form);
        this.add(wrapper);
    }
    
    /**
     * Adds batch components.
     */
    private void addBatchComponents() {
        // create models
        
        final IModel<String> batchSizeModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                return String.valueOf(BrowserNavigatorPanel.this.mutableRange.getObject().getRange().getCount());
            }
        };
        
        // create components
        
        final Component linkBatchPrev = BrowserNavigatorPanel.createBatchLink(
                "link.batch.p",
                "label",
                this.mutableRange.getObject().getCatalog(),
                this.mutableRange.getObject().hasPreviousBatch()
                        ? this.mutableRange.getObject().getPreviousBatch()
                        : null);
        
        final Component linkBatchNext = BrowserNavigatorPanel.createBatchLink(
                "link.batch.n",
                "label",
                this.mutableRange.getObject().getCatalog(),
                this.mutableRange.getObject().hasNextBatch()
                        ? this.mutableRange.getObject().getNextBatch()
                        : null);
        
        final Component linkBatch = BrowserNavigatorPanel.createBatchLink(
                "link.batch",
                "label",
                this.mutableRange.getObject().getCatalog(),
                this.mutableRange.getObject().getBatch());
        
        final Component batchSizeLabel = new Label(
                "label.batch.size",
                batchSizeModel);
        
        // place components
        
        this.add(linkBatchPrev);
        this.add(linkBatchNext);
        this.add(linkBatch);
        this.add(batchSizeLabel);
    }
    
    /**
     * Adds navigation links.
     */
    private void addNavigationLinks() {
        // initialize model
        
        final IModel<CardRange> mutableRange2 = BrowserNavigatorPanel.translate(this.mutableRange);
        
        // create components
        
        final WebMarkupContainer wrapper = AbstractCardNavigatorPanel.createCatalogRangeWrapper("wrap.pager", this.mutableRange);
        
        final AbstractLink linkFirst = AbstractCardNavigatorPanel.createFirstLink("link.first", mutableRange2);
        final AbstractLink linkLast = AbstractCardNavigatorPanel.createLastLink("link.last", mutableRange2);
        final AbstractLink linkLeft = AbstractCardNavigatorPanel.createLeftLink("link.left", mutableRange2);
        final AbstractLink linkRight = AbstractCardNavigatorPanel.createRightLink("link.right", mutableRange2);
        final AbstractLink linkUp = AbstractCardNavigatorPanel.createUpLink("link.up", mutableRange2);
        
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
        return AbstractCardNavigatorPanel.createDownLink(id, offset, BrowserNavigatorPanel.translate(this.mutableRange));
    }
    
    /**
     * Creates a batch skip link.
     * 
     * @param id
     * component ID
     * @param innerId
     * inner label ID
     * @param catalog
     * catalog to skip in
     * @param batch
     * batch to skip in
     * @return a link
     */
    private static WebMarkupContainer createBatchLink(final String id, final String innerId, final Catalog catalog, final String batch) {
        if ((catalog == null) || (batch == null)) {
            return BrowserNavigatorPanel.createDummyLink(id, innerId);
        }
        
        final AbstractLink link = new BookmarkableBatchLink(id, catalog, batch);
        link.add(new Label(innerId, batch));
        return link;
    }
    
    /**
     * Creates a letter skip link.
     * 
     * @param id
     * component ID
     * @param innerId
     * inner label ID
     * @param catalog
     * catalog to skip in
     * @param letter
     * letter to skip in
     * @return a link
     */
    protected static WebMarkupContainer createLetterLink(final String id, final String innerId, final Catalog catalog, final String letter) {
        if ((catalog == null) || (letter == null)) {
            return BrowserNavigatorPanel.createDummyLink(id, innerId);
        }
        
        final AbstractLink link = new BookmarkableLetterLink(id, catalog, letter);
        link.add(new Label(innerId, letter));
        return link;
    }
    
    /**
     * Creates a dummy (empty) link.
     * 
     * @param id
     * component ID
     * @param innerId
     * inner label ID
     * @return a web markup container
     */
    private static WebMarkupContainer createDummyLink(final String id, final String innerId) {
        final WebMarkupContainer link = new WebMarkupContainer(id);
        link.add(new Label(innerId, "-"));
        link.setVisibilityAllowed(false);
        return link;
    }
    
    /**
     * Translates the catalog range model to the more common card range model.
     * 
     * @param range
     * range model to be translated
     * @return a translation model
     */
    private static IModel<CardRange> translate(final IModel<CardCatalogRange> range) {
        return new Model<CardRange>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public CardRange getObject() {
                return range.getObject().getRange();
            }
            
            @Override
            public void setObject(final CardRange object) {
                range.setObject(range.getObject().createForOtherRange(object));
            }
        };
    }
}
