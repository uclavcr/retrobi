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
 * Card catalog batch meta information. Contains various interesting information
 * about the catalog batch.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BatchMetainfo {
    /**
     * catalog where the batch is located
     */
    private final Catalog catalog;
    /**
     * regular batch name
     */
    private final String name;
    /**
     * batch name used for sorting
     */
    private final String nameForSort;
    /**
     * number of the first card in the batch (should be 1)
     */
    private final int firstCardNumber;
    /**
     * batch number continuity flag
     */
    private final boolean continuous;
    
    /**
     * Creates a new instance.
     * 
     * @param catalog
     * catalog
     * @param name
     * batch name
     * @param nameForSort
     * batch name for sorting
     * @param firstCardNumber
     * first card number
     * @param continuous
     * batch number continuity flag
     */
    public BatchMetainfo(final Catalog catalog, final String name, final String nameForSort, final int firstCardNumber, final boolean continuous) {
        this.catalog = catalog;
        this.name = name;
        this.nameForSort = nameForSort;
        this.firstCardNumber = firstCardNumber;
        this.continuous = continuous;
    }
    
    /**
     * Returns the catalog.
     * 
     * @return the catalog
     */
    public Catalog getCatalog() {
        return this.catalog;
    }
    
    /**
     * Returns the regular batch name.
     * 
     * @return the regular batch name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the batch name for sorting.
     * 
     * @return the batch name for sorting
     */
    public String getNameForSort() {
        return this.nameForSort;
    }
    
    /**
     * Returns the first card number.
     * 
     * @return the first card number
     */
    public int getFirstCardNumber() {
        return this.firstCardNumber;
    }
    
    /**
     * Checks if the given batch card numbers are continuous. That is, if all
     * card numbers follow the arithmetic sequence 1, 2, 3... with increase 1
     * and there is no gap or doubled number.
     * 
     * @return <code>true</code> if the batch is continuous and contains no bad
     * regions (gaps or doubled numbers), <code>false</code> otherwise
     */
    public boolean isContinuous() {
        return this.continuous;
    }
}
