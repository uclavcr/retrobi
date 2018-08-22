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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.model.setup.ImageViewMode;
import cz.insophy.retrobi.pages.SearchPage;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card list panel that is capable of displaying a summary tables too.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardListPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * total count model
     */
    private final IModel<String> sumAllModel;
    /**
     * OCR count model
     */
    private final IModel<String> sumOcrModel;
    /**
     * fixed OCR count model
     */
    private final IModel<String> sumOcrFixModel;
    /**
     * segments count model
     */
    private final IModel<String> sumSegmentsModel;
    /**
     * images count model
     */
    private final IModel<String> sumImagesModel;
    /**
     * list viewer - shows cards in panels
     */
    private final CardListView listViewer;
    /**
     * table viewer - shows cards as rows in a table
     */
    private final CardTableViewer tableViewer;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param navigator
     * navigator panel which is used as an information source
     */
    public CardListPanel(final String id, final AbstractCardNavigatorPanel navigator) {
        super(id);
        
        this.setOutputMarkupId(true);
        
        // prepare models
        
        this.sumAllModel = Model.of("-");
        this.sumOcrModel = Model.of("-");
        this.sumOcrFixModel = Model.of("-");
        this.sumSegmentsModel = Model.of("-");
        this.sumImagesModel = Model.of("-");
        
        // create components
        
        this.listViewer = new CardListView("list.card", "list.item.card", navigator) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (navigator.getViewSettings().isDetailEnabled()) {
                    return true;
                }
                
                if (navigator.getViewSettings().getImageViewMode().equals(ImageViewMode.TABLE)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        this.tableViewer = new CardTableViewer("table.card", navigator) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (navigator.getViewSettings().isDetailEnabled()) {
                    return false;
                }
                
                if (!navigator.getViewSettings().getImageViewMode().equals(ImageViewMode.TABLE)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        final WebMarkupContainer sumViewer = new WebMarkupContainer("table.sum") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return !navigator.getViewSettings().isDetailEnabled() &&
                        navigator.getViewSettings().areSingleCardsDisplayed() &&
                        !CardListPanel.this.listViewer.getList().isEmpty() &&
                        super.isVisible();
            }
        };
        
        final WebMarkupContainer emptyWrap = new WebMarkupContainer("wrap.empty") {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                final Page page = this.getPage();
                
                if (page instanceof SearchPage) {
                    // special case for search page:
                    // do not show empty wrapper if the query is empty
                    
                    if (((SearchPage) page).getSearchQuery() == null) {
                        return false;
                    }
                }
                
                return CardListPanel.this.listViewer.getList().isEmpty();
            }
        };
        
        final Component labelSumAll = new Label("label.sum.all", this.sumAllModel);
        final Component labelSumOcr = new Label("label.sum.ocr", this.sumOcrModel);
        final Component labelSumOcrFix = new Label("label.sum.ocr_fix", this.sumOcrFixModel);
        final Component labelSumSegments = new Label("label.sum.segments", this.sumSegmentsModel);
        final Component labelSumImages = new Label("label.sum.images", this.sumImagesModel);
        
        // place components
        
        sumViewer.add(labelSumAll);
        sumViewer.add(labelSumOcr);
        sumViewer.add(labelSumOcrFix);
        sumViewer.add(labelSumSegments);
        sumViewer.add(labelSumImages);
        this.add(sumViewer);
        this.add(this.listViewer);
        this.add(this.tableViewer);
        this.add(emptyWrap);
    }
    
    /**
     * Checks if the card list is empty.
     * 
     * @return <code>true</code> if the list is empty, <code>false</code>
     * otherwise
     */
    public boolean isListEmpty() {
        return this.listViewer.getList().isEmpty();
    }
    
    /**
     * Updates a list of cards displayed in the panel.
     * 
     * @param cards
     * new cards to be displayed
     */
    public void setList(final List<Card> cards) {
        this.listViewer.setList(cards);
        this.tableViewer.setList(cards);
        this.updateStatistics(cards);
    }
    
    /**
     * Updates the card statistics. This method causes label models to update.
     * 
     * @param cards
     * cards to gather statistics from
     */
    private void updateStatistics(final List<Card> cards) {
        int counter = 0;
        int ocrCounter = 0;
        int ocrFixCounter = 0;
        int segmentsCounter = 0;
        final Map<Integer, Integer> pageCounter = new TreeMap<Integer, Integer>();
        
        for (final Card card : cards) {
            counter++;
            
            if (!SimpleStringUtils.isEmpty(card.getOcr())) {
                // not empty OCR
                ocrCounter++;
            }
            
            if (!SimpleStringUtils.isEmpty(card.getOcrFix())) {
                // not empty fixed OCR
                ocrFixCounter++;
            }
            
            if (!SimpleSegmentUtils.isSegmentationEmpty(card)) {
                // not empty segmentation
                segmentsCounter++;
            }
            
            // page count information
            
            final int pageCount = card.getPageCount();
            
            if (pageCounter.containsKey(pageCount)) {
                pageCounter.put(pageCount, pageCounter.get(pageCount) + 1);
            } else {
                pageCounter.put(pageCount, 1);
            }
        }
        
        this.sumAllModel.setObject(String.valueOf(counter));
        this.sumOcrModel.setObject(String.valueOf(ocrCounter));
        this.sumOcrFixModel.setObject(String.valueOf(ocrFixCounter));
        this.sumSegmentsModel.setObject(String.valueOf(segmentsCounter));
        this.sumImagesModel.setObject(CardListPanel.toReadable(pageCounter));
    }
    
    /**
     * Converts a page counter map into a human readable string.
     * 
     * @param pageCounter
     * a page counter map
     * @return human readable string
     */
    private static String toReadable(final Map<Integer, Integer> pageCounter) {
        final StringBuilder b = new StringBuilder();
        
        for (final Entry<Integer, Integer> entry : pageCounter.entrySet()) {
            if (b.length() != 0) {
                b.append(", ");
            }
            
            b.append(String.format("%dx [%d str.]", entry.getValue(), entry.getKey()));
        }
        
        return SimpleStringUtils.neverEmpty(b.toString());
    }
}
