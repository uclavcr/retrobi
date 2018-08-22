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

package cz.insophy.retrobi.model.task;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.model.SessionCardContainer;

/**
 * A task that sorts the batch.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SortBasketTask extends AbstractLongTask {
    /**
     * target card container
     */
    private final SessionCardContainer target;
    
    /**
     * Creates a new instance.
     * 
     * @param target
     * target container
     */
    public SortBasketTask(final SessionCardContainer target) {
        super();
        this.target = target;
    }
    
    @Override
    public boolean isQuick() {
        if (this.target.getBasketSize() <= Settings.MANY_CARDS) {
            return true;
        }
        
        return super.isQuick();
    }
    
    @Override
    public void start() {
        this.initProgress(1);
        
        try {
            // sort the basket
            
            this.target.sortBasket();
        } catch (final NotFoundRepositoryException x) {
            this.addError(x);
        } catch (final GeneralRepositoryException x) {
            this.addError(x);
        }
        
        // finish
        
        this.setDone();
    }
    
    @Override
    public String getName() {
        return "Seřadit schránku";
    }
}
