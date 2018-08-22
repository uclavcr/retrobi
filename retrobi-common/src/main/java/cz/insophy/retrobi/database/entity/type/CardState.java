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

/**
 * Card state. The state ordinal value is important, do not change the order.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum CardState {
    /**
     * fresh card, no user has touched it yet
     */
    FRESH("Nový", "Lístek byl nově uložen do databáze."),
    /**
     * OCR fixed (rewritten) by some user
     */
    REWRITTEN("Přepsaný", "Lístek byl editován uživatelem a přepis zatím neprošel redakcí."),
    /**
     * segmentation done by some user
     */
    SEGMENTED("Segmentovaný", "Lístek prošel redakcí a je pro další úpravy uzamčen."),
    /**
     * a tree structure is present
     */
    STRUCTURED("Strukturovaný", "Lístek obsahuje segmentaci a položkový rozpis."),
    /**
     * closed card, all changes confirmed, not editable anymore
     */
    CLOSED("Uzavřený", "Lístek je kompletně ověřený, přepsaný a uzavřený.");
    
    /**
     * short title
     */
    private final String title;
    /**
     * long description
     */
    private final String description;
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * title
     * @param description
     * long description
     */
    private CardState(final String title, final String description) {
        this.title = title;
        this.description = description;
    }
    
    /**
     * Returns the long description of the state.
     * 
     * @return the long description
     */
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public String toString() {
        return this.title;
    }
    
    /**
     * Checks if the current state is lower than the state given. Lower states
     * mean less information than higher states.
     * 
     * @param otherState
     * the other state
     * @return <code>true</code> if the current state is lower than the given
     * state, <code>false</code> otherwise
     */
    public boolean isLowerThan(final CardState otherState) {
        return (this.ordinal() < otherState.ordinal());
    }
    
    /**
     * Checks if the current state is higher than the state given. Higher states
     * mean more information than lower states.
     * 
     * @param otherState
     * the other state
     * @return <code>true</code> if the current state is higher than the given
     * state, <code>false</code> otherwise
     */
    public boolean isHigherThan(final CardState otherState) {
        return (this.ordinal() > otherState.ordinal());
    }
}
