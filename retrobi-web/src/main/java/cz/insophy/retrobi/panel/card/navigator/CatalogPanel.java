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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.link.BookmarkableCatalogLink;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.model.CardCatalogModel;

/**
 * Catalog navigator panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CatalogPanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * active catalog
     */
    private final Catalog catalog;
    /**
     * active letter
     */
    private final String letter;
    
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
    public CatalogPanel(final String id, final Catalog catalog, final String letter) {
        super(id);
        
        this.catalog = catalog;
        this.letter = letter;
        
        // place components
        
        this.addHeaderComponents();
        this.addLetterComponents();
    }
    
    /**
     * Adds header components.
     */
    private void addHeaderComponents() {
        // initialize variables
        
        final String title = String.format("%s / Písmeno %s", this.catalog.toString(), this.letter.toUpperCase());
        
        // create components
        
        final Component headLink = new BookmarkableCatalogLink("link.head");
        final Component helpLink = new BookmarkableHelpLink("link.help", TextType.L_HELP_CATALOG);
        final Component titleLabel = new Label("label.title", title);
        
        // place components
        
        this.add(headLink);
        this.add(helpLink);
        this.add(titleLabel);
    }
    
    /**
     * Adds letter components.
     */
    private void addLetterComponents() {
        // create components
        
        final Component upLink = new BookmarkableCatalogLink("link.up");
        
        final Component letterLeftLink = BrowserNavigatorPanel.createLetterLink(
                "link.letter.p",
                "label",
                this.catalog,
                CardCatalogModel.getInstance().getPreviousLetter(this.letter));
        
        final Component letterRightLink = BrowserNavigatorPanel.createLetterLink(
                "link.letter.n",
                "label",
                this.catalog,
                CardCatalogModel.getInstance().getNextLetter(this.letter));
        
        // place components
        
        this.add(upLink);
        this.add(letterLeftLink);
        this.add(letterRightLink);
    }
}
