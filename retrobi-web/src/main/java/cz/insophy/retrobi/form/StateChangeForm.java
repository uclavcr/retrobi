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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;

/**
 * A form to change the card state.
 * 
 * @author Vojtěch Hordějčuk
 */
public class StateChangeForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent navigator panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * card model
     */
    private final IModel<Card> card;
    /**
     * card state
     */
    private final IModel<CardState> state;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent navigator panel
     * @param card
     * card model
     */
    public StateChangeForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id);
        
        this.parent = parent;
        
        // create models
        
        this.card = card;
        this.state = Model.of(card.getObject().getState());
        
        // create components
        
        final DropDownChoice<CardState> stateInput = new DropDownChoice<CardState>("select", this.state, Arrays.asList(CardState.values())) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
                    return false;
                }
                
                return super.isVisible();
            }
        };
        
        // place components
        
        this.add(stateInput);
    }
    
    @Override
    protected void onSubmit() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
            this.error("Měnit stav lístku může až editor.");
            return;
        }
        
        // change card state
        
        try {
            RetrobiOperations.changeCardState(
                    this.card.getObject().getId(),
                    this.state.getObject(),
                    RetrobiWebSession.get().getLoggedUser(),
                    RetrobiWebApplication.getCSVLogger());
            
            this.info("Změna stavu byla úspěšně provedena.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
        
        // reload the cards
        
        this.parent.requestCardViewerUpdate();
    }
}
