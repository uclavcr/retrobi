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

package cz.insophy.retrobi.utils.type;

/**
 * Text source for download.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum DownloadTextSource {
    /**
     * use no text
     */
    NONE("Žádný"),
    /**
     * use original OCR only
     */
    OCR("Původní OCR"),
    /**
     * use fixed OCR (or previous option as fallback)
     */
    FIXED("Přepis OCR"),
    /**
     * use the best text available (or previous option as fallback)
     */
    BEST("Nejlepší (segmentace)");
    /**
     * source title
     */
    private final String title;
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * title
     */
    private DownloadTextSource(final String title) {
        this.title = title;
    }
    
    @Override
    public String toString() {
        return this.title;
    }
}
