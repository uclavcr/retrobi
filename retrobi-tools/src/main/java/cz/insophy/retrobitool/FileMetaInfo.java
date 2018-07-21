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

package cz.insophy.retrobitool;

import java.io.File;

/**
 * Common tagging interface for file meta information.
 * 
 * @author Vojtěch Hordějčuk
 */
public interface FileMetaInfo {
    /**
     * Returns the source image file.
     * 
     * @return batch name
     */
    public File getFile();
    
    /**
     * Returns the batch name.
     * 
     * @return batch name
     */
    public String getBatch();
    
    /**
     * Returns the card number.
     * 
     * @return card number
     */
    public int getNumber();
    
    /**
     * Returns the page number.
     * 
     * @return page number
     */
    public int getPage();
}
