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

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;

import cz.insophy.retrobi.link.BookmarkableBatchLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.panel.card.navigator.CatalogPanel;
import cz.insophy.retrobi.utils.component.GridListView;

/**
 * Page that shows a list of batches in the letter.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LetterPage extends AbstractCatalogPage {
    /**
     * batch list
     */
    private List<String> batches;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public LetterPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        // load values
        
        this.batches = CardCatalogModel.getInstance().getBatches(this.getCatalog(), this.getLetter());
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create components
        
        final Component navigatorPanel = new CatalogPanel("panel.navigator", this.getCatalog(), this.getLetter());
        
        final Component titleLabel = new Label("label.title", String.format("Skupiny pod písmenem %s", this.getLetter()));
        final Component countLabel = new Label("label.count", String.valueOf(this.batches.size()));
        
        final Component batchGrid = new GridListView<String>("batches", "rows", "cols", this.batches, 3) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateCell(final ListItem<String> item) {
                final WebMarkupContainer link = new BookmarkableBatchLink("link", LetterPage.this.getCatalog(), item.getModelObject());
                link.add(new Label("label", item.getModelObject()));
                item.add(link);
            }
            
            @Override
            protected void populateEmptyCell(final ListItem<String> item) {
                final WebMarkupContainer link = new WebMarkupContainer("link");
                link.setVisibilityAllowed(false);
                link.add(new Label("label"));
                item.add(link);
            }
        };
        
        // place components
        
        this.add(navigatorPanel);
        this.add(titleLabel);
        this.add(countLabel);
        this.add(batchGrid);
    }
    
    @Override
    protected String getPageTitle() {
        return "Písmeno " + this.getLetter();
    }
}
