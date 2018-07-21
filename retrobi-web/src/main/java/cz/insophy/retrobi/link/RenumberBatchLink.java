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

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;

/**
 * A link that renumbers the given batch on click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RenumberBatchLink extends Link<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * batch catalog
     */
    private final Catalog catalog;
    /**
     * batch name
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
     * batch
     */
    public RenumberBatchLink(final String id, final Catalog catalog, final String batch) {
        super(id);
        
        this.catalog = catalog;
        this.batch = batch;
    }
    
    @Override
    public void onClick() {
        try {
            // renumber the given batch
            
            RetrobiApplication.db().getCardRepository().renumberBatch(this.catalog, this.batch);
            
            // notify user
            
            this.info("Skupina byla přečíslována.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        } catch (final NotFoundRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
