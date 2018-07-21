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

import cz.insophy.retrobi.database.entity.type.Catalog;

/**
 * File meta information extended by specific features needed for import.
 */
public class ImporterFileMetaInfo implements FileMetaInfo {
    /**
     * source file
     */
    private final File file;
    /**
     * drawer name
     */
    private final String drawer;
    /**
     * catalog
     */
    private final Catalog catalog;
    /**
     * batch name
     */
    private final String batch;
    /**
     * card order in a batch
     */
    private final int number;
    /**
     * page number
     */
    private final int page;
    /**
     * OCR files
     */
    private final String ocr;
    
    /**
     * Creates a new instance.
     * 
     * @param directory
     * source file directory (can be <code>null</code>)
     * @param file
     * source file
     * @param drawer
     * drawer name (can be <code>null</code>)
     * @param catalog
     * catalog
     * @param batch
     * batch name
     * @param number
     * card order in the batch
     * @param page
     * page number
     * @param ocr
     * OCR text
     */
    public ImporterFileMetaInfo(final File directory, final File file, final String drawer, final Catalog catalog, final String batch, final int number, final int page, final String ocr) {
        this.file = file;
        this.drawer = drawer;
        this.catalog = catalog;
        this.batch = batch;
        this.number = number;
        this.page = page;
        this.ocr = ocr;
    }
    
    // =======
    // GETTERS
    // =======
    
    @Override
    public File getFile() {
        return this.file;
    }
    
    /**
     * Returns the drawer name (if any).
     * 
     * @return drawer name or <code>null</code>
     */
    public String getDrawer() {
        return this.drawer;
    }
    
    /**
     * Returns the card catalog.
     * 
     * @return card catalog
     */
    public Catalog getCatalog() {
        return this.catalog;
    }
    
    @Override
    public String getBatch() {
        return this.batch;
    }
    
    @Override
    public int getNumber() {
        return this.number;
    }
    
    @Override
    public int getPage() {
        return this.page;
    }
    
    /**
     * Checks whether the file has its OCR.
     * 
     * @return <code>true</code> if the file has OCR, <code>false</code>
     * otherwise
     */
    public boolean hasOcr() {
        return (this.ocr != null);
    }
    
    /**
     * Returns the OCR text. If there is no OCR text loaded, returns empty
     * string (it never returns <code>null</code>).
     * 
     * @return OCR text
     */
    public String getOcr() {
        if (this.ocr == null) {
            return "";
        }
        
        return this.ocr;
    }
    
    @Override
    public String toString() {
        return this.file.getName();
    }
    
}
