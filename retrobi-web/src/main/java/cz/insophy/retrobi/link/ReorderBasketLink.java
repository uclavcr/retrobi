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

package cz.insophy.retrobi.link;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.Direction;

/**
 * A link that reorders card in a basket up or down after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ReorderBasketLink extends AjaxFallbackLink<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card to be moved
     */
    private final IModel<Card> card;
    /**
     * desired move direction
     */
    private final IModel<Direction> direction;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card to move
     * @param direction
     * move direction
     */
    public ReorderBasketLink(final String id, final Card card, final Direction direction) {
        super(id);
        
        this.card = Model.of(card);
        this.direction = Model.of(direction);
    }
    
    @Override
    public boolean isEnabled() {
        if (!RetrobiWebSession.get().getCardContainer().canMoveInBasket(this.card.getObject().getId(), this.direction.getObject())) {
            // disable if the card cannot be moved
            
            return false;
        }
        
        return super.isEnabled();
    }
    
    @Override
    public void onClick(final AjaxRequestTarget target) {
        // move card in the basket
        
        RetrobiWebSession.get().getCardContainer().moveInBasket(this.card.getObject().getId(), this.direction.getObject());
    }
}
