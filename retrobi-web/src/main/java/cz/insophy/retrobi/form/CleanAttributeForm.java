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

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.longtask.CleanAttributesModification;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.task.BatchModificationTask;

/**
 * A form that cleans all empty and removable nodes from the attribute tree
 * after submitting (it starts a long task).
 * 
 * @author Vojtěch Hordějčuk
 */
public class CleanAttributeForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public CleanAttributeForm(final String id) {
        super(id);
    }
    
    @Override
    protected void onSubmit() {
        try {
            // modify cards
            
            final CardModification modification = new CleanAttributesModification(
                    RetrobiWebConfiguration.getInstance().getAttributeRoot());
            
            RetrobiWebSession.get().scheduleTask(new BatchModificationTask(
                    RetrobiWebSession.get().getCardContainer().getBasketCardIds(),
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult(),
                    modification));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
