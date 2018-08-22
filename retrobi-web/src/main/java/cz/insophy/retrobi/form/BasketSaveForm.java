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

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Cardset;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.exception.OverLimitException;

/**
 * Basket save form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasketSaveForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * title model
     */
    private final IModel<String> title;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public BasketSaveForm(final String id) {
        super(id);
        
        // prepare models
        
        this.title = Model.of((String) null);
        
        // create components
        
        final TextField<String> titleField = new TextField<String>("field.title", this.title);
        
        // place components
        
        this.add(titleField);
    }
    
    @Override
    protected void onSubmit() {
        // check login
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Schránku si může uložit do databáze jen přihlášený uživatel.");
            return;
        }
        
        // check card count
        
        if (RetrobiWebSession.get().getCardContainer().getBasketSize() < 1) {
            this.error("Prázdnou schránku nelze uložit.");
            return;
        }
        
        // check title
        
        if ((this.title.getObject() == null) || (this.title.getObject().length() < 3)) {
            this.error("Název schránky musí mít alespoň tři znaky.");
            return;
        }
        
        // create card set
        
        final Cardset newSet = new Cardset(
                RetrobiWebSession.get().getLoggedUser().getId(),
                this.title.getObject().trim(),
                RetrobiWebSession.get().getCardContainer().getBasketCardIds());
        
        try {
            // save the set into the database
            
            final boolean overwritten = RetrobiApplication.db().getCardsetRepository().saveCardset(
                    newSet,
                    RetrobiWebSession.get().getCardsetLimit());
            
            if (overwritten) {
                this.info("Existující schránka se stejným názvem byla přepsána.");
            } else {
                this.info("Vaše schránka byla úspěšně uložena do databáze.");
            }
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        } catch (final OverLimitException x) {
            this.error(x.getMessage());
        }
    }
}
