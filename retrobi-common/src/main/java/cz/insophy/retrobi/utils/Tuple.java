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
 * Tuple is an ordered pair of two elements. Both elements are guaranteed NOT to
 * be <code>null</code> (therefore it is not possible to store any
 * <code>null</code> values here).
 * 
 * @author Vojtěch Hordějčuk
 * @param <F>
 * first value type
 * @param <S>
 * second value type
 */
public class Tuple<F, S> implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * first element
     */
    private final F first;
    /**
     * second element
     */
    private final S second;
    
    /**
     * Creates a new instance.
     * 
     * @param first
     * first element
     * @param second
     * second element
     */
    private Tuple(final F first, final S second) {
        super();
        
        if ((first == null) || (second == null)) {
            throw new NullPointerException();
        }
        
        this.first = first;
        this.second = second;
    }
    
    /**
     * Creates a tuple. This is just a convenience method.
     * 
     * @param <A>
     * first class
     * @param <B>
     * second class
     * @param first
     * the first element
     * @param second
     * the second element
     * @return a new tuple with the two elements provided
     */
    public static <A, B> Tuple<A, B> of(final A first, final B second) {
        return new Tuple<A, B>(first, second);
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the first element.
     * 
     * @return first element
     */
    public F getFirst() {
        return this.first;
    }
    
    /**
     * Returns the second element
     * 
     * @return second element
     */
    public S getSecond() {
        return this.second;
    }
    
    @Override
    public String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }
    
    // ========
    // EQUALITY
    // ========
    
    @Override
    public int hashCode() {
        return this.first.hashCode() - this.second.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof Tuple)) {
            return false;
        }
        
        final Tuple<?, ?> other = (Tuple<?, ?>) obj;
        
        return other.first.equals(this.first) && other.second.equals(this.second);
    }
}
