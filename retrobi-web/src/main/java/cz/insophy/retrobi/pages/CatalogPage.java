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
import org.apache.wicket.markup.html.basic.Label;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.link.BookmarkableCatalogLink;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.panel.CatalogListView;

/**
 * Page that shows a list of catalogs.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CatalogPage extends AbstractCatalogPage {
    /**
     * catalog list
     */
    private List<Catalog> catalogs;
    /**
     * batch count
     */
    private int batchCount;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public CatalogPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        // load values
        
        this.catalogs = CardCatalogModel.getInstance().getCatalogs();
        this.batchCount = CardCatalogModel.getInstance().getBatchCount();
    }
    
    @Override
    protected void initComponents(final PageParameters parameters) {
        super.initComponents(parameters);
        
        // create components
        
        final Component catalogList = new CatalogListView("list.catalog", this.catalogs);
        final Component countLabel = new Label("label.count", String.valueOf(this.batchCount));
        final Component headLink = new BookmarkableCatalogLink("link.head");
        final Component helpLink = new BookmarkableHelpLink("link.help", TextType.L_HELP_CATALOG);
        
        // place components
        
        this.add(catalogList);
        this.add(countLabel);
        this.add(headLink);
        this.add(helpLink);
    }
    
    @Override
    protected String getPageTitle() {
        return "Katalog";
    }
}
