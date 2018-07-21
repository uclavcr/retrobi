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

package cz.insophy.retrobi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;

/**
 * Helper class for simple gathering of card counts for individual catalogs,
 * states, image counts and their grouping or summing. First, an object instance
 * is created. Then the data are inserted by calling various convenience
 * methods. At last, the counts can be retrieved safely and comfortably by
 * getter methods.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardCountContainer implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card count container (catalog -&gt; image count -&gt; card count)
     */
    private final Map<Catalog, Map<Integer, Integer>> imageCounter;
    /**
     * card count container (state -&gt card count)
     */
    private final Map<CardState, Integer> stateCounter;
    
    /**
     * Creates a new instance.
     */
    public CardCountContainer() {
        this.imageCounter = new HashMap<Catalog, Map<Integer, Integer>>();
        this.stateCounter = new HashMap<CardState, Integer>();
    }
    
    /**
     * Sets the card count for the given catalog and image count.
     * 
     * @param catalog
     * catalog
     * @param imageCount
     * image count
     * @param cardCount
     * card count for this catalog and image count
     */
    public void setCardCount(final Catalog catalog, final int imageCount, final int cardCount) {
        if (!this.imageCounter.containsKey(catalog)) {
            // prepare map for this catalog
            
            this.imageCounter.put(catalog, new HashMap<Integer, Integer>());
        }
        
        // put new data into the map (or replace existing)
        
        this.imageCounter.get(catalog).put(imageCount, cardCount);
    }
    
    /**
     * Sets the card count for the given state.
     * 
     * @param state
     * state
     * @param cardCount
     * card count for this state
     */
    public void setCardCount(final CardState state, final int cardCount) {
        this.stateCounter.put(state, cardCount);
    }
    
    /**
     * Returns the card count for the given state. If no data are available,
     * returns 0.
     * 
     * @param state
     * state
     * @return card count for this state
     */
    public int getCardCount(final CardState state) {
        if (!this.stateCounter.containsKey(state)) {
            // no data for this state
            
            return 0;
        }
        
        return this.stateCounter.get(state);
    }
    
    /**
     * Returns the card count for the given catalog and image count. If no data
     * are available, returns 0.
     * 
     * @param catalog
     * catalog
     * @param imageCount
     * image count
     * @return card count for this catalog and image count
     */
    public int getCardCount(final Catalog catalog, final int imageCount) {
        if (!this.imageCounter.containsKey(catalog)) {
            // no data for this catalog
            
            return 0;
        }
        
        final Map<Integer, Integer> subdata = this.imageCounter.get(catalog);
        
        if (!subdata.containsKey(imageCount)) {
            // no data for this image count
            
            return 0;
        }
        
        return subdata.get(imageCount);
    }
    
    /**
     * Returns the total card count for the given catalog. If no data are
     * available, returns 0.
     * 
     * @param catalog
     * catalog
     * @return card count for this catalog (all image counts)
     */
    public int getCardCount(final Catalog catalog) {
        final Map<Integer, Integer> submap = this.imageCounter.get(catalog);
        
        if (submap == null) {
            // no data for this catalog
            
            return 0;
        }
        
        int sum = 0;
        
        for (final Entry<Integer, Integer> entry : submap.entrySet()) {
            // add card count of this image count entry to sum
            
            sum += entry.getValue();
        }
        
        return sum;
    }
    
    /**
     * Returns the total card count for the given image count. If no data are
     * available, returns 0.
     * 
     * @param imageCount
     * image count
     * @return card count for this image count (all catalogs)
     */
    public int getCardCount(final int imageCount) {
        int sum = 0;
        
        for (final Entry<Catalog, Map<Integer, Integer>> entry : this.imageCounter.entrySet()) {
            final Integer subsum = entry.getValue().get(imageCount);
            
            if (subsum != null) {
                sum += subsum;
            }
        }
        
        return sum;
    }
    
    /**
     * Returns the total count of cards.
     * 
     * @return total count of cards
     */
    public int getTotalCardCount() {
        int sum = 0;
        
        for (final Entry<CardState, Integer> entry : this.stateCounter.entrySet()) {
            sum += entry.getValue();
        }
        
        return sum;
    }
    
    /**
     * Returns the total count of cards with at least one image.
     * 
     * @return count of cards with an image
     */
    public int getCardCountWithImage() {
        int sum = 0;
        
        for (final Entry<Catalog, Map<Integer, Integer>> entry : this.imageCounter.entrySet()) {
            for (final Entry<Integer, Integer> subentry : entry.getValue().entrySet()) {
                sum += subentry.getValue();
            }
        }
        
        return sum;
    }
    
    /**
     * Returns a list of used card states.
     * 
     * @return a list of card states
     */
    public List<CardState> getStates() {
        final List<CardState> states = new ArrayList<CardState>(this.stateCounter.keySet());
        Collections.sort(states);
        return Collections.unmodifiableList(states);
    }
    
    /**
     * Returns a list of used catalogs.
     * 
     * @return a list of catalogs
     */
    public List<Catalog> getCatalogs() {
        final List<Catalog> catalogs = new ArrayList<Catalog>(this.imageCounter.keySet());
        Collections.sort(catalogs);
        return Collections.unmodifiableList(catalogs);
    }
    
    /**
     * Returns a list of used image counts.
     * 
     * @return a list of image counts
     */
    public List<Integer> getImageCounts() {
        final Set<Integer> result = new TreeSet<Integer>();
        
        for (final Entry<Catalog, Map<Integer, Integer>> entry : this.imageCounter.entrySet()) {
            for (final Entry<Integer, Integer> subentry : entry.getValue().entrySet()) {
                result.add(subentry.getKey());
            }
        }
        
        return Collections.unmodifiableList(new ArrayList<Integer>(result));
    }
}
