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

import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.longtask.BatchModification;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.model.task.BatchModificationTask;
import cz.insophy.retrobi.utils.CzechAlphabet;

/**
 * Card batch editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class EditBatchForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * catalog
     */
    private final IModel<Catalog> catalog;
    /**
     * batch
     */
    private final IModel<String> batch;
    /**
     * batch for sort
     */
    private final IModel<String> batchForSort;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public EditBatchForm(final String id) {
        super(id);
        
        // prepare models
        
        this.catalog = Model.of(Catalog.A);
        this.batch = Model.of("");
        this.batchForSort = Model.of("");
        
        // create components
        
        final DropDownChoice<Catalog> catalogCombo = new DropDownChoice<Catalog>("select.catalog", this.catalog, Arrays.asList(Catalog.values()));
        final TextField<String> batchField = new TextField<String>("input.batch", this.batch);
        final TextField<String> batchForSortField = new TextField<String>("input.batch_sort", this.batchForSort);
        
        // setup components
        
        catalogCombo.setLabel(Model.of("Nový katalog"));
        catalogCombo.setRequired(true);
        catalogCombo.setNullValid(false);
        batchField.setRequired(true);
        batchField.setLabel(Model.of("Nová skupina"));
        batchForSortField.setRequired(false);
        batchForSortField.setLabel(Model.of("Nová skupina pro řazení"));
        
        // place components
        
        this.add(catalogCombo);
        this.add(batchField);
        this.add(batchForSortField);
    }
    
    @Override
    protected void onSubmit() {
        // get the new values
        
        final Catalog newCatalog = this.catalog.getObject();
        final String newBatch = this.batch.getObject().trim();
        final String newBatchForSort = (this.batchForSort.getObject() == null)
                ? CzechAlphabet.getDefaultBatchForSort(newBatch)
                : this.batchForSort.getObject().trim();
        
        try {
            // modify cards
            
            final CardModification modification = new BatchModification(newCatalog, newBatch, newBatchForSort);
            
            RetrobiWebSession.get().scheduleTask(new BatchModificationTask(
                    RetrobiWebSession.get().getCardContainer().getBasketCardIds(),
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult(),
                    modification));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
