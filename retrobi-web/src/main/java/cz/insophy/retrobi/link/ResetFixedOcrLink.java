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

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;

/**
 * A link that resets all OCRs fixed by a specified user.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ResetFixedOcrLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * user ID whose OCRs will be reseted
     */
    private final String userId;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param userId
     * ID of user whose OCRs will be reseted
     */
    public ResetFixedOcrLink(final String id, final String userId) {
        super(id);
        
        this.userId = userId;
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Přepisy uživatelů může mazat pouze administrátor.");
            return;
        }
        
        try {
            // reset card content fixed by this user
            
            RetrobiOperations.resetRewritesByUser(this.userId);
            this.info("Neschválené přepisy uživatele byly smazány.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
    }
}
