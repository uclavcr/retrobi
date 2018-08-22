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

import java.util.List;

/**
 * Long task interface
 * 
 * @author Vojtěch Hordějčuk
 */
public interface LongTask {
    /**
     * Starts the task.
     */
    public void start();
    
    /**
     * Stops the task.
     */
    public void stop();
    
    /**
     * Checks if the task is quick. Quick here means that it is possible to
     * execute this task in the matter of a few seconds (about 5). Quick tasks
     * are not added to the queue but executed immediately.
     * 
     * @return <code>true</code> if the task is quick and can be executed
     * immediately, <code>false</code> otherwise (the task is long and has to be
     * executed later)
     */
    public boolean isQuick();
    
    /**
     * Returns the task name.
     * 
     * @return the task name
     */
    public String getName();
    
    /**
     * Returns the task status.
     * 
     * @return the task status
     */
    public String getStatus();
    
    /**
     * Returns a list of errors. May be empty.
     * 
     * @return a list of errors
     */
    public List<Exception> getErrors();
    
    /**
     * Checks if the task if finished.
     * 
     * @return <code>true</code> if the task is finished, <code>false</code>
     * otherwise
     */
    public boolean isFinished();
}
