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

/**
 * Card modification.
 * 
 * @author Vojtěch Hordějčuk
 */
public interface CardModification {
    /**
     * Returns the modification title.
     * 
     * @return the title
     */
    public String getTitle();
    
    /**
     * Modifies the given card. If the modification is done successfully,
     * returns <code>true</code>. If it is not possible to modify this card with
     * the modifier, returns <code>false</code>.
     * 
     * @param cardToEdit
     * a card to be modified
     * @return  <code>true</code> if the card was modified correctly,
     * <code>false</code> otherwise
     * @throws AlreadyModifiedException
     * this exception is thrown if the given card has not to be modified (the
     * modification is already applied) - this is for example the case when
     * setting a batch "ABC" to a card which already has batch "ABC" set
     */
    public boolean modify(Card cardToEdit) throws AlreadyModifiedException;
}
