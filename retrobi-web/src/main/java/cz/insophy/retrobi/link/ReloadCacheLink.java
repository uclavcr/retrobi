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
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.model.CardCatalogModel;

/**
 * A link that reloads the catalog cache on click.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ReloadCacheLink extends Link<Object> {
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
    public ReloadCacheLink(final String id) {
        super(id);
    }
    
    @Override
    public void onClick() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Nemáte dostatečná oprávnění k této akci.");
            return;
        }
        
        try {
            CardCatalogModel.getInstance().update();
            this.info("Cache katalogu (části a skupiny) se znovu plní.");
        } catch (final GeneralRepositoryException x) {
            this.error(x.getMessage());
        }
    }
}
