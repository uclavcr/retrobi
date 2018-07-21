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

package cz.insophy.retrobi.database.entity.type;

import org.svenson.JSONProperty;

import cz.insophy.retrobi.database.entity.Card;

/**
 * Abstract base class for card indexes.
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractCardIndex extends CardIndexInfo {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Returns the view Javascript code.
     * 
     * @return the view code (in Javascript)
     */
    @JSONProperty(ignore = true)
    public abstract String getCode();
    
    /**
     * Creates a new instance.
     * 
     * @param name
     * name
     * @param title
     * title
     * @param role
     * minimal role for using
     * @param order
     * order
     */
    protected AbstractCardIndex(final String name, final String title, final UserRole role, final int order) {
        super(name, title, role, order);
    }
    
    /**
     * Returns the highlight data for the given card, as extracted by the given
     * index. This method can return <code>null</code>, which means "no data" or
     * "highlighting not available for this index".
     * 
     * @param card
     * a card to be highlighted
     * @return highlight data as a string (or <code>null</code>)
     */
    abstract public String getHighlightData(final Card card);
    
}
