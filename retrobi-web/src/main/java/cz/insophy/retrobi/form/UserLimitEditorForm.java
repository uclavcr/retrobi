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
import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * User limit editor form.
 */
public class UserLimitEditorForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * user displayed
     */
    private final User user;
    /**
     * basket size limit model
     */
    private final IModel<String> basketLimit;
    /**
     * cardset count model
     */
    private final IModel<String> cardsetLimit;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param user
     * user to edit
     */
    public UserLimitEditorForm(final String id, final User user) {
        super(id);
        
        // prepare models
        
        this.user = user;
        this.basketLimit = Model.of(String.valueOf(user.getBasketLimit()));
        this.cardsetLimit = Model.of(String.valueOf(user.getCardsetLimit()));
        
        // create components
        
        final TextField<String> basketLimitField = new TextField<String>("input.limit.basket", this.basketLimit);
        final TextField<String> cardsetLimitField = new TextField<String>("input.limit.cardset", this.cardsetLimit);
        
        // place components
        
        this.add(basketLimitField);
        this.add(cardsetLimitField);
    }
    
    @Override
    protected void onSubmit() {
        if (StandaloneDocument.equalsById(this.user, RetrobiWebSession.get().getLoggedUser())) {
            this.error("Nemůžete měnit svůj vlastní profil.");
            return;
        }
        
        if (this.basketLimit.getObject() == null) {
            this.basketLimit.setObject("-1");
        }
        
        if (this.cardsetLimit.getObject() == null) {
            this.cardsetLimit.setObject("-1");
        }
        
        try {
            // set the limits
            
            final int lbasket = Integer.valueOf(this.basketLimit.getObject());
            final int lcardset = Integer.valueOf(this.cardsetLimit.getObject());
            
            this.user.setBasketLimit(lbasket);
            this.user.setCardsetLimit(lcardset);
            
            // update user
            
            RetrobiApplication.db().getUserRepository().updateUser(this.user);
            this.info("Limity uživatele byly uloženy.");
        } catch (final NumberFormatException x) {
            this.error("Některé z čísel bylo chybně zadáno.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
