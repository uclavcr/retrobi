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
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.model.task.DeleteBatchTask;

/**
 * A link that deletes a batch after click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteBatchLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * batch catalog
     */
    private final Catalog catalog;
    /**
     * batch to remove
     */
    private final String batch;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param catalog
     * batch catalog
     * @param batch
     * batch to remove
     */
    public DeleteBatchLink(final String id, final Catalog catalog, final String batch) {
        super(id);
        
        this.catalog = catalog;
        this.batch = batch;
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Smazat celou skupinu může pouze administrátor.");
            return;
        }
        
        // remove the batch
        
        try {
            RetrobiWebSession.get().scheduleTask(new DeleteBatchTask(
                    this.catalog,
                    this.batch,
                    RetrobiWebSession.get().getLoggedUser()));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
