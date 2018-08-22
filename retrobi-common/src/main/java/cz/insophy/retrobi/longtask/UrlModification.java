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

package cz.insophy.retrobi.longtask;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * A card URL modification
 * 
 * @author Vojtěch Hordějčuk
 */
public class UrlModification implements CardModification {
    /**
     * new value
     */
    private final String newUrl;
    
    /**
     * Creates a new instance.
     * 
     * @param newUrl
     * new value to be set as the URL
     */
    public UrlModification(final String newUrl) {
        this.newUrl = newUrl;
    }
    
    @Override
    public boolean modify(final Card cardToEdit) throws AlreadyModifiedException {
        if (!SimpleGeneralUtils.wasChangedAsString(cardToEdit.getUrl(), this.newUrl)) {
            throw new AlreadyModifiedException();
        }
        
        cardToEdit.setUrl(this.newUrl);
        return true;
    }
    
    @Override
    public String getTitle() {
        return String.format("Nastavit URL '%s'", this.newUrl);
    }
    
    @Override
    public String toString() {
        return this.getTitle();
    }
}
