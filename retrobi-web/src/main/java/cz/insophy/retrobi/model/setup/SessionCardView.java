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

import java.io.Serializable;

/**
 * Card view specification for each guest session. This class holds all state-
 * and card- related information for the current guest (browser position, search
 * results and position, basket content and position). This view is listening to
 * the card collection changes and updates the ranges to be valid all the time.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SessionCardView implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card view mode
     */
    private CardViewMode cardViewMode;
    /**
     * image view mode
     */
    private ImageViewMode imageViewMode;
    /**
     * view empty attributes too
     */
    private boolean showEmptyAttributes;
    /**
     * compact mode enabled
     */
    private boolean compactMode;
    /**
     * current card step
     */
    private int step;
    
    /**
     * Creates a new instance.
     */
    public SessionCardView() {
        this.cardViewMode = CardViewMode.BASIC;
        this.imageViewMode = ImageViewMode.CROPPED;
        this.showEmptyAttributes = true;
        this.compactMode = false;
        this.step = 10;
    }
    
    /**
     * Clears all the user specific view settings.
     */
    public void reset() {
        this.cardViewMode = CardViewMode.BASIC;
        this.imageViewMode = ImageViewMode.CROPPED;
        this.showEmptyAttributes = true;
        this.compactMode = false;
        this.step = 10;
    }
    
    // =======
    // QUERIES
    // =======
    
    /**
     * Returns the card view mode.
     * 
     * @return the card view mode
     */
    public CardViewMode getCardViewMode() {
        return this.cardViewMode;
    }
    
    /**
     * Returns the image view mode.
     * 
     * @return the image view mode
     */
    public ImageViewMode getImageViewMode() {
        return this.imageViewMode;
    }
    
    /**
     * Checks if the empty attributes are shown.
     * 
     * @return <code>true</code> if the empty attributes are shown,
     * <code>false</code> otherwise
     */
    public boolean areEmptyAttributesShown() {
        return this.showEmptyAttributes;
    }
    
    /**
     * Checks if the compact mode is enabled.
     * 
     * @return <code>true</code> if the compact mode is enabled,
     * <code>false</code> otherwise
     */
    public boolean isCompactModeEnabled() {
        return this.compactMode;
    }
    
    /**
     * Returns the current step size.
     * 
     * @return the step size
     */
    public int getStep() {
        return this.step;
    }
    
    // ========
    // MUTATORS
    // ========
    
    /**
     * Sets the card view mode.
     * 
     * @param newMode
     * the new card view mode
     */
    public void setCardViewMode(final CardViewMode newMode) {
        this.cardViewMode = newMode;
    }
    
    /**
     * Sets the image view mode.
     * 
     * @param newMode
     * the new image view mode
     */
    public void setImageViewMode(final ImageViewMode newMode) {
        this.imageViewMode = newMode;
    }
    
    /**
     * Sets whether the empty attributes should be displayed.
     * 
     * @param value
     * show empty attributes
     */
    public void setShowEmptyAttributes(final boolean value) {
        this.showEmptyAttributes = value;
    }
    
    /**
     * Sets whether the compact mode is enabled.
     * 
     * @param value
     * compact mode enabled flag
     */
    public void setCompactModeEnabled(final boolean value) {
        this.compactMode = value;
    }
    
    /**
     * Sets the card step.
     * 
     * @param newStep
     * the new card step
     */
    public void setStep(final int newStep) {
        this.step = newStep;
    }
}
