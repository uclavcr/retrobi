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

package cz.insophy.retrobi.utils.library;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Time;

/**
 * General utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleGeneralUtils {
    /**
     * Returns the first not-null argument.
     * 
     * @param <E>
     * argument type
     * @param values
     * arguments
     * @return first not-null argument or <code>null</code> if no such argument
     */
    public static <E> E coalesce(final E... values) {
        for (final E value : values) {
            if (value != null) {
                return value;
            }
        }
        
        return null;
    }
    
    /**
     * Generic method for moving elements in the list. Some of the list elements
     * may be picked to the <code>movables</code> list and inserted
     * <b>before</b> or <b>after</b> the pivot element.
     * 
     * @param <E>
     * entity class
     * @param entities
     * source list of entities
     * @param movables
     * entities to be moved
     * @param pivot
     * pivot entity
     * @param after
     * <code>true</code> = insert after pivot, <code>false</code> = insert
     * before pivot
     * @return the resulting list
     */
    public static <E> Collection<E> moveItems(final Collection<E> entities, final Collection<E> movables, final E pivot, final boolean after) {
        if (movables.isEmpty()) {
            return entities;
        }
        
        if (!entities.contains(pivot)) {
            throw new IllegalArgumentException();
        }
        
        final List<E> result = new LinkedList<E>();
        
        for (final E entity : entities) {
            if (entity.equals(pivot)) {
                // -----
                // PIVOT
                // -----
                
                if (after) {
                    // pivot: insert pivot (if not in movables), then movables
                    
                    if (!movables.contains(pivot)) {
                        result.add(pivot);
                    }
                    
                    result.addAll(movables);
                } else {
                    // pivot: insert movables, then pivot (if not in movables)
                    
                    result.addAll(movables);
                    
                    if (!movables.contains(pivot)) {
                        result.add(pivot);
                    }
                }
            } else {
                // ---------
                // NON-PIVOT
                // ---------
                
                if (movables.contains(entity)) {
                    // movable entity: skip
                    
                    continue;
                }
                
                // regular entity: add
                
                result.add(entity);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Checks if a string value was changed to another value. It considers
     * <code>null</code> values as an empty string (<code>""</code>).
     * 
     * @param oldValue
     * old string value (or <code>null</code>)
     * @param newValue
     * new string value (or <code>null</code>)
     * @return <code>true</code> if the new value is different from the old
     * value (considering <code>null</code> values as empty strings),
     * <code>false</code> otherwise
     */
    public static boolean wasChangedAsString(final String oldValue, final String newValue) {
        final String oldString = (oldValue == null ? "" : oldValue);
        final String newString = (newValue == null ? "" : newValue);
        return SimpleGeneralUtils.wasChanged(oldString, newString);
    }
    
    /**
     * Checks if a value was changed to another value. Robust to
     * <code>null</code> values.
     * 
     * @param oldValue
     * old value (or <code>null</code>)
     * @param newValue
     * new value (or <code>null</code>)
     * @return <code>true</code> if the new value is different than the old
     * value (by the <code>equals()</code> method), <code>false</code>
     * otherwise; if one value is <code>null</code> and the second one is not,
     * <code>true</code> is returned
     */
    public static boolean wasChanged(final Object oldValue, final Object newValue) {
        if (oldValue == null) {
            if (newValue != null) {
                // NULL -> something
                return true;
            }
            // NULL -> NULL
            return false;
        }
        if (newValue == null) {
            // something -> NULL
            return true;
        }
        // something -> something
        return !oldValue.equals(newValue);
    }
    
    /**
     * Ensures the number value to stay in the specified range.
     * 
     * @param value
     * the input number value
     * @param min
     * minimal value
     * @param max
     * maximal value
     * @return the input number value limited to the given minimum and maximum
     * (always stays in the range)
     */
    public static int limit(final int value, final int min, final int max) {
        if (value <= min) {
            return min;
        }
        
        if (value >= max) {
            return max;
        }
        
        return value;
    }
    
    /**
     * Sorts the given list of cards so they are sorted by their numbers
     * (ascending) and time of update (descending) so the lowest numbers and
     * latest update times are in the front.
     * 
     * @param cards
     * writable list of cards that will be sorted
     */
    public static void sortByNumberAndTime(final List<Card> cards) {
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(final Card o1, final Card o2) {
                if (o1.getNumberInBatch() < o2.getNumberInBatch()) {
                    // number 1 less than number 2 >>> -1
                    return -1;
                } else if (o1.getNumberInBatch() > o2.getNumberInBatch()) {
                    // number 1 greater than number 2 >>> +1
                    return 1;
                } else {
                    // the same numbers, sort by modification date
                    // (newest first - that is why a negative is used here)
                    return -Time.compare(o1.getUpdated(), o2.getUpdated());
                }
            }
        });
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleGeneralUtils() {
        throw new UnsupportedOperationException();
    }
}
