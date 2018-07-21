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
import org.apache.wicket.markup.html.panel.Panel;

import cz.insophy.retrobi.form.BasketSaveForm;
import cz.insophy.retrobi.form.BasketUploadForm;
import cz.insophy.retrobi.form.LoadBasketForm;
import cz.insophy.retrobi.link.DownloadAsTxtLink;

/**
 * Basket save panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketSavePanel extends Panel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent navigator panel
     */
    public BasketSavePanel(final String id, final BasketNavigatorPanel parent) {
        super(id);
        
        // create components
        
        final Component loadForm = new LoadBasketForm("form.load", parent);
        final Component saveForm = new BasketSaveForm("form.save");
        final Component uploadForm = new BasketUploadForm("form.upload", parent);
        final Component linkDownloadTxt = new DownloadAsTxtLink("link.get.txt");
        
        // place components
        
        this.add(loadForm);
        this.add(saveForm);
        this.add(uploadForm);
        this.add(linkDownloadTxt);
    }
}
