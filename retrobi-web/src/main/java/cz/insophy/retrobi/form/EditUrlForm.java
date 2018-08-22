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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.longtask.UrlModification;
import cz.insophy.retrobi.model.task.BatchModificationTask;

/**
 * Card WWW link editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class EditUrlForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * URL
     */
    private final IModel<String> url;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public EditUrlForm(final String id) {
        super(id);
        
        // prepare models
        
        this.url = Model.of("");
        
        // create components
        
        final TextField<String> urlField = new TextField<String>("input", this.url);
        
        // place components
        
        this.add(urlField);
    }
    
    @Override
    protected void onSubmit() {
        // get the new value
        
        final String newValue = (this.url.getObject() == null ? "" : this.url.getObject().trim());
        
        try {
            // modify cards
            
            final CardModification modification = new UrlModification(newValue);
            
            RetrobiWebSession.get().scheduleTask(new BatchModificationTask(
                    RetrobiWebSession.get().getCardContainer().getBasketCardIds(),
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult(),
                    modification));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
