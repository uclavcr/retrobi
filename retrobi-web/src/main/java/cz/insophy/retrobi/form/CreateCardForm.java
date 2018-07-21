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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.link.BookmarkableCardLink;
import cz.insophy.retrobi.model.setup.CardViewMode;

/**
 * Card create form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CreateCardForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * catalog to create card in
     */
    private final IModel<Catalog> catalog;
    /**
     * batch to create card in
     */
    private final IModel<String> batch;
    /**
     * a number to be assigned to the new card
     */
    private final IModel<String> number;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public CreateCardForm(final String id) {
        super(id);
        
        // prepare models
        
        this.catalog = Model.of(Catalog.A);
        this.batch = Model.of("");
        this.number = Model.of("1");
        
        // create components
        
        final DropDownChoice<Catalog> catalogSelect = new DropDownChoice<Catalog>("select.catalog", this.catalog, Arrays.asList(Catalog.values()));
        final TextField<String> batchField = new TextField<String>("input.batch", this.batch);
        final TextField<String> numberField = new TextField<String>("input.number", this.number);
        
        // setup components
        
        catalogSelect.setNullValid(false);
        catalogSelect.setRequired(true);
        batchField.setRequired(true);
        numberField.setRequired(true);
        
        // place components
        
        this.add(catalogSelect);
        this.add(batchField);
        this.add(numberField);
    }
    
    @Override
    protected void onSubmit() {
        try {
            // create a card
            
            final Catalog newCatalog = this.catalog.getObject();
            final String newBatch = this.batch.getObject().trim();
            final int newNumber = Integer.valueOf(this.number.getObject());
            
            final Card newCard = RetrobiOperations.createCard(
                    newCatalog,
                    newBatch,
                    newNumber,
                    RetrobiWebSession.get().getLoggedUser(),
                    RetrobiWebApplication.getCSVLogger());
            
            // inform the user
            
            this.info("Lístek byl vytvořen.");
            
            // redirect to the editor page
            
            RetrobiWebSession.get().getCardView().setCardViewMode(CardViewMode.BASIC);
            final BookmarkablePageLink<?> link = new BookmarkableCardLink("TEMP", newCard.getId());
            this.setResponsePage(link.getPageClass(), link.getPageParameters());
            
            // renumber the (possible existing) batch
            
            RetrobiApplication.db().getCardRepository().renumberBatch(newCatalog, newBatch);
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
