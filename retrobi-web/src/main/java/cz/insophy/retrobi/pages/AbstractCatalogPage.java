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

import org.apache.wicket.PageParameters;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.model.CardCatalogModel;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Abstract base class for all catalog related pages.
 * 
 * @author Vojtěch Hordějčuk
 */
abstract public class AbstractCatalogPage extends AbstractBasicPage {
    /**
     * active catalog
     */
    private Catalog catalog;
    /**
     * active batch
     */
    private String batch;
    /**
     * active letter
     */
    private String letter;
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    protected AbstractCatalogPage(final PageParameters parameters) {
        super(parameters);
    }
    
    /**
     * Returns the catalog.
     * 
     * @return the catalog
     */
    public Catalog getCatalog() {
        return this.catalog;
    }
    
    /**
     * Returns the batch.
     * 
     * @return the batch
     */
    public String getBatch() {
        return this.batch;
    }
    
    /**
     * Returns the letter.
     * 
     * @return the letter
     */
    public String getLetter() {
        return this.letter;
    }
    
    @Override
    protected void initComponentModels(final PageParameters parameters) {
        super.initComponentModels(parameters);
        
        // extract catalog
        
        this.catalog = SimpleGeneralUtils.coalesce(AbstractCatalogPage.extractCatalog(parameters), Catalog.A);
        
        // extract batch
        
        this.batch = SimpleGeneralUtils.coalesce(AbstractCatalogPage.extractBatch(parameters), "");
        
        // extract letter (or letter from batch)
        
        final String letterFromBatch = CardCatalogModel.getInstance().getLetterOfBatch(this.batch);
        this.letter = SimpleGeneralUtils.coalesce(AbstractCatalogPage.extractLetter(parameters), letterFromBatch, "");
    }
    
    @Override
    public TextType getHelpTextType() {
        return TextType.L_HELP_CATALOG;
    }
    
    // ==============
    // URL EXTRACTION
    // ==============
    
    /**
     * Extracts a catalog from the page parameters.
     * 
     * @param parameters
     * page parameters
     * @return catalog or <code>null</code>
     */
    private static Catalog extractCatalog(final PageParameters parameters) {
        final String parameter = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_CATALOG));
        
        if (parameter == null) {
            return null;
        }
        
        return Catalog.valueOf(parameter);
    }
    
    /**
     * Extracts a letter from the page parameters.
     * 
     * @param parameters
     * page parameters
     * @return letter or <code>null</code>
     */
    private static String extractLetter(final PageParameters parameters) {
        final String parameter = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_LETTER));
        
        if (parameter == null) {
            return null;
        }
        
        return parameter;
    }
    
    /**
     * Extracts a batch from the page parameters.
     * 
     * @param parameters
     * page parameters
     * @return batch or <code>null</code>
     */
    private static String extractBatch(final PageParameters parameters) {
        final String parameter = SimpleStringUtils.decodeFromUrl(parameters.getString(RetrobiWebApplication.PARAM_BATCH));
        
        if (parameter == null) {
            return null;
        }
        
        return parameter;
    }
}
