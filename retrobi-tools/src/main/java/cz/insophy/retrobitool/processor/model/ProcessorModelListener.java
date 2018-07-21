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

package cz.insophy.retrobitool.processor.model;

/**
 * Listener that reacts on image processor model changes.
 * 
 * @author Vojtěch Hordějčuk
 */
public interface ProcessorModelListener {
    /**
     * File list updated.
     */
    public void filesUpdated();
    
    /**
     * Processing started.
     */
    public void processStarted();
    
    /**
     * Process status updated.
     * 
     * @param total
     * total number of files
     * @param done
     * number of files done
     */
    public void processStatusUpdated(int total, int done);
    
    /**
     * Processing finished.
     */
    public void processFinished();
    
    /**
     * Processing failed.
     */
    public void processFailed();
}
