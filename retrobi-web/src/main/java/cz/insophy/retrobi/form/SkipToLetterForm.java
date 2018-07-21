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

package cz.insophy.retrobi.form;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.link.BookmarkableLetterLink;
import cz.insophy.retrobi.model.CardCatalogModel;

/**
 * A form that skips to the letter.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SkipToLetterForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * catalog model
     */
    private final IModel<Catalog> catalog;
    /**
     * letter model
     */
    private final IModel<String> letter;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param catalog
     * current catalog
     * @param letter
     * current letter
     */
    public SkipToLetterForm(final String id, final Catalog catalog, final String letter) {
        super(id);
        
        // initialize models
        
        this.catalog = Model.of(catalog);
        this.letter = Model.of(letter);
        
        // create components
        
        final DropDownChoice<Catalog> catalogCombo = new DropDownChoice<Catalog>(
                "select.catalog",
                this.catalog,
                CardCatalogModel.getInstance().getCatalogs());
        
        final DropDownChoice<String> letterCombo = new DropDownChoice<String>(
                "select.letter",
                this.letter,
                CardCatalogModel.getInstance().getLetters());
        
        // place components
        
        this.add(catalogCombo);
        this.add(letterCombo);
    }
    
    @Override
    protected void onSubmit() {
        final BookmarkablePageLink<?> link = new BookmarkableLetterLink("TEMP", this.catalog.getObject(), this.letter.getObject());
        this.setRedirect(true);
        this.setResponsePage(link.getPageClass(), link.getPageParameters());
    }
}
