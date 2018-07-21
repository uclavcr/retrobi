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

import java.io.IOException;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.exception.OverLimitException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;

/**
 * Custom form for basket upload.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketUploadForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent card navigator
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * file upload field model
     */
    private final IModel<FileUpload> model;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent panel
     */
    public BasketUploadForm(final String id, final AbstractCardNavigatorPanel parent) {
        super(id);
        
        // setup the form
        
        this.setMaxSize(Bytes.megabytes(1));
        
        // create models
        
        this.parent = parent;
        this.model = new Model<FileUpload>();
        
        // create components
        
        final FileUploadField fileInput = new FileUploadField("input", this.model);
        
        // place components
        
        this.add(fileInput);
    }
    
    @Override
    protected void onSubmit() {
        if (this.model.getObject() == null) {
            this.error("Vyberte soubor.");
            return;
        }
        
        if (!this.model.getObject().getContentType().equals("text/plain")) {
            this.error(String.format("Nesprávný typ souboru (má být 'text/plain', byl '%s').", this.model.getObject().getContentType()));
            return;
        }
        
        try {
            // import basket
            
            RetrobiWebSession.get().getCardContainer().importBasketFromStream(this.model.getObject().getInputStream(), RetrobiWebSession.get().getBasketLimit());
            
            // reset the card list viewer
            
            this.parent.requestCardViewerReset();
        } catch (final IOException x) {
            this.error("Chyba při importu schránky: " + x.getMessage());
        } catch (final OverLimitException x) {
            this.error(x.getMessage());
        }
    }
}
