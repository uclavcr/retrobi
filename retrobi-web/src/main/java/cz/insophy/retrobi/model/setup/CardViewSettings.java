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

package cz.insophy.retrobi.model.setup;

/**
 * Settings that specify the card view.
 * 
 * @author Vojtěch Hordějčuk
 */
public interface CardViewSettings {
    /**
     * Checks if the detail view is enabled.
     * 
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isDetailEnabled();
    
    /**
     * Checks if only single cards are displayed.
     * 
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean areSingleCardsDisplayed();
    
    /**
     * Checks if the basic basket links should be displayed.
     * 
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean areBasketBasicLinksDisplayed();
    
    /**
     * Checks if the move basket links should be displayed.
     * 
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean areBasketMoveLinksDisplayed();
    
    /**
     * Checks if the compact mode is enabled.
     * 
     * @return <code>true</code> if the compact mode is enabled,
     * <code>false</code> otherwise
     */
    public boolean isCompactModeEnabled();
    
    /**
     * Returns the card view mode.
     * 
     * @return card view mode
     */
    public CardViewMode getCardViewMode();
    
    /**
     * Returns the image view mode.
     * 
     * @return image view mode
     */
    public ImageViewMode getImageViewMode();
}
