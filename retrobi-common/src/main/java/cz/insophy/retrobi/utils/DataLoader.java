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

package cz.insophy.retrobi.utils;

import java.util.List;

/**
 * Data loader interface. Can provide a part view on a data starting from the
 * given offset limited by a given number of elements.
 * 
 * @author Vojtěch Hordějčuk
 * @param <E>
 * element class
 */
public interface DataLoader<E> {
    /**
     * Loads a list of elements starting from the given offset (inclusive)
     * limited by the given number of elements.
     * 
     * @param offset
     * offset (must be 0 or greater)
     * @param limit
     * limit (must be greater than 0)
     * @return a list of elements
     */
    public List<E> loadData(int offset, int limit);
}
