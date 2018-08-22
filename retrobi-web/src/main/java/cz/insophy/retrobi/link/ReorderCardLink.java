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

import java.util.List;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.model.task.MultipleCardMoveTask;
import cz.insophy.retrobi.utils.Tuple;

/**
 * A link that moves all cards in the batch to the given position in the catalog
 * after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ReorderCardLink extends Link<Tuple<Card, Boolean>> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card to move the basket after/before (pivot)
     * @param after
     * insert cards after, not before the pivot card
     */
    public ReorderCardLink(final String id, final Card card, final boolean after) {
        super(id, Model.of(Tuple.of(card, after)));
    }
    
    @Override
    public void onClick() {
        if (RetrobiWebSession.get().getCardContainer().getBasketSize() < 1) {
            // basket is empty, do nothing
            
            this.error("Schránka je prázdná, není co přesouvat.");
            return;
        }
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
            // user must be editor
            
            this.error("Přesouvat lístky může jen editor.");
            return;
        }
        
        // gather parameters
        
        final List<String> cardIds = RetrobiWebSession.get().getCardContainer().getBasketCardIds();
        final Card pivot = this.getModelObject().getFirst();
        final boolean after = this.getModelObject().getSecond();
        
        // run the task
        
        try {
            RetrobiWebSession.get().scheduleTask(new MultipleCardMoveTask(
                    cardIds,
                    pivot,
                    after,
                    RetrobiWebSession.get().getLoggedUser()));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
