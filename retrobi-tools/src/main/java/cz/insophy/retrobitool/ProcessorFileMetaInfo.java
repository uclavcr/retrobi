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
import cz.insophy.retrobi.utils.library.SimpleFileUtils;

/**
 * Helper structure for storing file meta information.
 */
public class ProcessorFileMetaInfo implements FileMetaInfo {
    /**
     * source image file
     */
    private final File file;
    /**
     * catalog
     */
    private Catalog catalog;
    /**
     * batch name
     */
    private final String batch;
    /**
     * first card number
     */
    private final int firstNumber;
    /**
     * card number
     */
    private final int number;
    /**
     * page number
     */
    private final int page;
    /**
     * last page number
     */
    private final int lastPage;
    /**
     * new name of the file
     */
    private String newName;
    /**
     * new extension of the file
     */
    private final String newExtension;
    /**
     * flag indicating the paper is empty (NULL means unknown)
     */
    private Boolean empty;
    
    /**
     * Creates a new instance.
     * 
     * @param file
     * source image file
     * @param catalog
     * catalog
     * @param batch
     * batch name
     * @param firstNumber
     * first card number
     * @param number
     * card number
     * @param page
     * page number
     * @param lastPage
     * last page number
     * @param extension
     * image file extension (tif, png)
     */
    public ProcessorFileMetaInfo(final File file, final Catalog catalog, final String batch, final int firstNumber, final int number, final int page, final int lastPage, final String extension) {
        this.file = file;
        this.catalog = catalog;
        this.batch = batch;
        this.firstNumber = firstNumber;
        this.number = number;
        this.page = page;
        this.lastPage = lastPage;
        this.newName = "";
        this.newExtension = extension;
        this.empty = null;
        this.updateNewName();
    }
    
    // =======
    // GETTERS
    // =======
    
    @Override
    public File getFile() {
        return this.file;
    }
    
    @Override
    public String getBatch() {
        return this.batch;
    }
    
    @Override
    public int getNumber() {
        return this.number;
    }
    
    /**
     * Returns the first card number.
     * 
     * @return first card number
     */
    public int getFirstNumber() {
        return this.firstNumber;
    }
    
    @Override
    public int getPage() {
        return this.page;
    }
    
    /**
     * Returns the last page number (= page count).
     * 
     * @return last page number
     */
    public int getLastPage() {
        return this.lastPage;
    }
    
    /**
     * Returns the temporary new file name.
     * 
     * @return temporary new file name
     */
    public String getTempNewName() {
        return this.newName;
    }
    
    /**
     * Checks whether the image was checked for emptiness. That should be
     * checked before calling the relevant "getter" method.
     * 
     * @return <code>true</code> if the paper was checked for emptiness,
     * <code>false</code> otherwise
     */
    public boolean wasCheckedEmpty() {
        return (this.empty != null);
    }
    
    /**
     * Returns the empty flag. If the image was not yet checked, a runtime
     * exception is thrown (we do not know the emptiness before we check it).
     * 
     * @return <code>true</code> if the paper is empty, <code>false</code>
     * otherwise
     */
    public boolean isEmpty() {
        if (this.empty == null) {
            throw new IllegalStateException();
        }
        
        return this.empty;
    }
    
    @Override
    public String toString() {
        return this.file.getName();
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the catalog file.
     * 
     * @param catalog
     * catalog
     */
    public void setCatalog(final Catalog catalog) {
        this.catalog = catalog;
        this.updateNewName();
    }
    
    /**
     * Sets the empty flag.
     * 
     * @param value
     * flag value
     */
    public void setEmpty(final boolean value) {
        this.empty = value;
        this.updateNewName();
    }
    
    /**
     * Resets the empty flag.
     */
    public void resetEmptyCheck() {
        this.empty = null;
        this.updateNewName();
    }
    
    // ======
    // UPDATE
    // ======
    
    /**
     * Updates the new filename.
     */
    private void updateNewName() {
        this.newName = SimpleFileUtils.produceImageFileName(
                this.catalog,
                this.batch,
                this.number,
                this.page,
                this.newExtension,
                this.empty == null ? false : this.empty);
    }
}
