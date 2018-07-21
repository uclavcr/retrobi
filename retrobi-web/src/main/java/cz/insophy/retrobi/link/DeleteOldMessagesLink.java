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

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;

/**
 * A link that removes old confirmed messages after click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteOldMessagesLink extends Link<Object> {
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
    public DeleteOldMessagesLink(final String id) {
        super(id);
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Tato operace vyžaduje administrátorská oprávnění.");
            return;
        }
        
        try {
            // get old message IDs
            
            final List<String> ids = RetrobiApplication.db().getMessageRepository().getOldMessageIds(Settings.OLD_MESSAGE_LIMIT);
            
            // remove the messages
            
            for (final String id : ids) {
                RetrobiApplication.db().getMessageRepository().removeMessage(id);
            }
            
            this.info("Počet smazaných hlášení: " + ids.size());
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
