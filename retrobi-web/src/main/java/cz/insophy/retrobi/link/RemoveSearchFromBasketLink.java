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
import cz.insophy.retrobi.model.SearchQuery;
import cz.insophy.retrobi.model.task.RemoveSearchFromBasketTask;

/**
 * Removes the whole search result from the basket after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RemoveSearchFromBasketLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * search query to use
     */
    private final IModel<SearchQuery> query;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param query
     * search query
     */
    public RemoveSearchFromBasketLink(final String id, final IModel<SearchQuery> query) {
        super(id);
        this.query = query;
    }
    
    @Override
    public void onClick() {
        if (this.query.getObject() == null) {
            this.error("Položte prosím nejprve vyhledávací dotaz.");
            return;
        }
        
        try {
            RetrobiWebSession.get().scheduleTask(new RemoveSearchFromBasketTask(
                    this.query.getObject(),
                    RetrobiWebSession.get().getCardContainer()));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
