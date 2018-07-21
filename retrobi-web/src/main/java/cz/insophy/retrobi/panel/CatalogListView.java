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

package cz.insophy.retrobi.panel;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.link.BookmarkableBatchLink;
import cz.insophy.retrobi.link.BookmarkableLetterLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.utils.component.GridListView;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Catalog list view.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CatalogListView extends ListView<Catalog> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param catalogs
     * catalog list
     */
    public CatalogListView(final String id, final List<Catalog> catalogs) {
        super(id, catalogs);
    }
    
    @Override
    protected void populateItem(final ListItem<Catalog> item) {
        // initialize models and variables
        
        final Catalog catalog = item.getModelObject();
        final List<String> letters = CardCatalogModel.getInstance().getLetters();
        final List<String> batches = CardCatalogModel.getInstance().getBatches(catalog);
        final boolean singleBatchOnly = batches.size() == 1;
        final String singleBatch = singleBatchOnly ? batches.get(0) : "";
        final int batchCount = CardCatalogModel.getInstance().getBatchCount(catalog);
        
        // create components
        
        final Label catalogLabel = new Label("label.title", singleBatchOnly
                ? catalog.toString()
                : String.format("%s - %s", catalog.toString(), SimpleStringUtils.inflect(batchCount, "skupina", "skupiny", "skupin")));
        
        final Component singleBatchLink = new BookmarkableBatchLink("link.single", catalog, singleBatch) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return singleBatchOnly;
            }
        };
        
        final Component letterList = new GridListView<String>("grid.letter", "rows", "cols", letters, 17) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateCell(final ListItem<String> innerItem) {
                final WebMarkupContainer link = new BookmarkableLetterLink("link", catalog, innerItem.getModelObject()) {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public boolean isEnabled() {
                        return CardCatalogModel.getInstance().isLetterUsed(catalog, innerItem.getModelObject());
                    }
                };
                
                link.add(new Label("label", innerItem.getModelObject().toString()));
                innerItem.add(link);
            }
            
            @Override
            protected void populateEmptyCell(final ListItem<String> innerItem) {
                final WebMarkupContainer link = new WebMarkupContainer("link");
                link.setVisibilityAllowed(false);
                link.add(new Label("label"));
                innerItem.add(link);
            }
            
            @Override
            public boolean isVisible() {
                return !singleBatchOnly;
            }
        };
        
        // place components
        
        item.add(catalogLabel);
        item.add(singleBatchLink);
        item.add(letterList);
    }
}
