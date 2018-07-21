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

import cz.insophy.retrobi.database.entity.type.Catalog;

/**
 * A position in a catalog that specifies catalog, batch and range information.
 * The class is immutable.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardCatalogRange implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * current catalog
     */
    private final Catalog catalog;
    /**
     * current batch name
     */
    private final String batch;
    /**
     * previous batch name or <code>null</code> if none
     */
    private final String previousBatch;
    /**
     * next batch name or <code>null</code> if none
     */
    private final String nextBatch;
    /**
     * current card range
     */
    private final CardRange range;
    
    /**
     * Creates a new instance.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch name
     * @param pBatch
     * previous batch name or <code>null</code>
     * @param nBatch
     * next batch name or <code>null</code>
     * @param range
     * card range
     */
    public CardCatalogRange(final Catalog catalog, final String batch, final String pBatch, final String nBatch, final CardRange range) {
        if ((catalog == null) || (batch == null) || (range == null)) {
            throw new NullPointerException("Ani jeden z parametrů nesmí být NULL.");
        }
        
        this.catalog = catalog;
        this.batch = batch;
        this.previousBatch = pBatch;
        this.nextBatch = nBatch;
        this.range = range;
    }
    
    // ========
    // CREATION
    // ========
    
    /**
     * Creates a new catalog position with other count.
     * 
     * @param newCount
     * a new count
     * @return new catalog position
     */
    public CardCatalogRange createForOtherCount(final int newCount) {
        return new CardCatalogRange(
                this.catalog,
                this.batch,
                this.previousBatch,
                this.nextBatch,
                this.range.createForOtherCount(newCount));
    }
    
    /**
     * Creates a new catalog position with other range.
     * 
     * @param newRange
     * the new range to be set
     * @return new catalog position
     */
    public CardCatalogRange createForOtherRange(final CardRange newRange) {
        return new CardCatalogRange(
                this.catalog,
                this.batch,
                this.previousBatch,
                this.nextBatch,
                newRange);
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the catalog.
     * 
     * @return the catalog
     */
    public Catalog getCatalog() {
        return this.catalog;
    }
    
    /**
     * Returns the batch.
     * 
     * @return the batch
     */
    public String getBatch() {
        return this.batch;
    }
    
    /**
     * Checks if the view has a previous batch.
     * 
     * @return <code>true</code> if the view has a previous batch,
     * <code>false</code> otherwise
     */
    public boolean hasPreviousBatch() {
        return (this.previousBatch != null);
    }
    
    /**
     * Checks if the view has a next batch.
     * 
     * @return <code>true</code> if the view has a next batch,
     * <code>false</code> otherwise
     */
    public boolean hasNextBatch() {
        return (this.nextBatch != null);
    }
    
    /**
     * Returns the previous batch. If there is not any, throws an exception. The
     * existence must be checked using the correct methods before.
     * 
     * @return the previous batch
     */
    public String getPreviousBatch() {
        if (this.previousBatch == null) {
            throw new NullPointerException("Neexistuje předchozí skupina.");
        }
        
        return this.previousBatch;
    }
    
    /**
     * Returns the next batch. If there is not any, throws an exception. The
     * existence must be checked using the correct methods before.
     * 
     * @return the next batch
     */
    public String getNextBatch() {
        if (this.nextBatch == null) {
            throw new NullPointerException("Neexistuje další skupina.");
        }
        
        return this.nextBatch;
    }
    
    /**
     * Returns the range.
     * 
     * @return the range
     */
    public CardRange getRange() {
        return this.range;
    }
    
    @Override
    public String toString() {
        return this.range.toString();
    }
    
    /**
     * Returns the string representation of the catalog range.
     * 
     * @return string representation of the catalog range
     */
    public String toPlainString() {
        if (this.range.getCount() < 1) {
            return String.format("%s / %s (prázdné)", this.catalog.toString(), this.batch);
        }
        
        return String.format("%s / %s / %s", this.catalog.toString(), this.batch, this.range.toPlainString());
    }
}
