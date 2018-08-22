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

import java.io.Serializable;

/**
 * Triple is an ordered tuple of three elements. All elements are guaranteed NOT
 * to be <code>null</code> (therefore it is not possible to store any
 * <code>null</code> values here).
 * 
 * @author Vojtěch Hordějčuk
 * @param <F>
 * first value type
 * @param <S>
 * second value type
 * @param <T>
 * third value type
 */
public class Triple<F, S, T> implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * inner tuple holding the values
     */
    private final Tuple<F, Tuple<S, T>> tuple;
    
    /**
     * Creates a new instance.
     * 
     * @param first
     * first element
     * @param second
     * second element
     * @param third
     * third element
     */
    private Triple(final F first, final S second, final T third) {
        this.tuple = Tuple.of(first, Tuple.of(second, third));
    }
    
    /**
     * Creates a triple. This is just a convenience method.
     * 
     * @param <F>
     * first class
     * @param <S>
     * second class
     * @param <T>
     * third class
     * @param first
     * the first element
     * @param second
     * the second element
     * @param third
     * the third element
     * @return a new triple with the three elements provided
     */
    public static <F, S, T> Triple<F, S, T> of(final F first, final S second, final T third) {
        return new Triple<F, S, T>(first, second, third);
    }
    
    /**
     * Returns the first element.
     * 
     * @return the first element
     */
    public F getFirst() {
        return this.tuple.getFirst();
    }
    
    /**
     * Returns the second element.
     * 
     * @return the second element
     */
    public S getSecond() {
        return this.tuple.getSecond().getFirst();
    }
    
    /**
     * Returns the third element.
     * 
     * @return the third element
     */
    public T getThird() {
        return this.tuple.getSecond().getSecond();
    }
    
    @Override
    public String toString() {
        return "(" + this.tuple.getFirst() + ", " + this.tuple.getSecond().getFirst() + ", " + this.tuple.getSecond().getSecond() + ")";
    }
    
    // ========
    // EQUALITY
    // ========
    
    @Override
    public int hashCode() {
        return this.tuple.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof Triple)) {
            return false;
        }
        
        final Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
        
        return other.tuple.equals(this.tuple);
    }
}
