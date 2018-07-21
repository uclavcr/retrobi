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

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;

/**
 * A link that confirms or un-confirms the user message (or system event).
 * 
 * @author Vojtěch Hordějčuk
 */
public class ConfirmMessageLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * message model
     */
    private final IModel<Message> message;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param message
     * message model
     */
    public ConfirmMessageLink(final String id, final IModel<Message> message) {
        super(id);
        
        this.message = message;
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Potvrzení může uzavírat / otevírat pouze administrátor.");
            return;
        }
        
        try {
            // confirm the message
            
            RetrobiOperations.confirmOrUnconfirmMessage(this.message.getObject(), RetrobiWebSession.get().getLoggedUser());
            this.info("Stav hlášení byl úspěšně změněn.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
    }
}
