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

package cz.insophy.retrobi.longtask;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * Batch modification.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BatchModification implements CardModification {
    /**
     * new catalog
     */
    private final Catalog newCatalog;
    /**
     * new batch
     */
    private final String newBatch;
    /**
     * new batch for sort
     */
    private final String newBatchForSort;
    
    /**
     * Creates a new instance.
     * 
     * @param newCatalog
     * new catalog
     * @param newBatch
     * new batch
     * @param newBatchSort
     * new batch for sort
     */
    public BatchModification(final Catalog newCatalog, final String newBatch, final String newBatchSort) {
        this.newCatalog = newCatalog;
        this.newBatch = newBatch.trim();
        this.newBatchForSort = newBatchSort.trim().toUpperCase();
    }
    
    @Override
    public String getTitle() {
        return String.format(
                "Změnit katalog na '%s', skupinu na '%s' a řadit jako '%s'",
                this.newCatalog,
                this.newBatch,
                this.newBatchForSort);
    }
    
    @Override
    public boolean modify(final Card cardToEdit) throws AlreadyModifiedException {
        final boolean sameCatalog = !SimpleGeneralUtils.wasChanged(
                cardToEdit.getCatalog(),
                this.newCatalog);
        
        final boolean sameBatch = !SimpleGeneralUtils.wasChangedAsString(
                cardToEdit.getBatch(),
                this.newBatch);
        
        final boolean sameBatchForSort = !SimpleGeneralUtils.wasChangedAsString(
                cardToEdit.getBatchForSort(),
                this.newBatchForSort);
        
        if (sameCatalog && sameBatch && sameBatchForSort) {
            // both new batches are the same
            // we do not need to
            throw new AlreadyModifiedException();
        }
        
        cardToEdit.setCatalog(this.newCatalog);
        cardToEdit.setBatch(this.newBatch);
        cardToEdit.setBatchForSort(this.newBatchForSort);
        
        return true;
    }
    
    @Override
    public String toString() {
        return this.getTitle();
    }
}
