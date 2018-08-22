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

package cz.insophy.retrobi.model;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.model.task.LongTask;

/**
 * Long task manager. Each task is being processed by a one-shot worker thread.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LongTaskManager implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(LongTaskManager.class);
    /**
     * the active task or <code>null</code> if none
     */
    private LongTask activeTask;
    /**
     * version number (changing over time)
     */
    private long version;
    
    /**
     * Creates a new instance.
     */
    public LongTaskManager() {
        this.activeTask = null;
        this.version = 0;
    }
    
    /**
     * Returns the active task.
     * 
     * @return the active task or <code>null</code> if none
     */
    public synchronized LongTask getActiveTask() {
        return this.activeTask;
    }
    
    /**
     * Schedules a task to be executed. Allows only one task to be planned at
     * once - if another task is already running, cannot proceed.
     * 
     * @param task
     * task to be executed
     * @return <code>true</code> if no task is running and the new task was
     * planned, <code>false</code> otherwise (another task is running)
     */
    public synchronized boolean scheduleTask(final LongTask task) {
        if (this.activeTask != null) {
            // another task is running, do not proceed
            
            LongTaskManager.LOG.debug("Cannot start another task, running: " + this.activeTask.getName());
            return false;
        }
        
        // save the new task
        
        this.activeTask = task;
        
        // prepare a runnable for this task
        
        final Runnable taskRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // wait until the task can be seen
                    
                    LongTaskManager.LOG.debug("Waiting before the task start...");
                    
                    try {
                        Thread.sleep(Settings.TASK_QUEUE_UPDATE + 1000);
                    } catch (final InterruptedException x) {
                        // NOP
                    }
                    
                    // first, execute the new task
                    
                    LongTaskManager.LOG.debug("Running a new task: " + task.getName());
                    LongTaskManager.this.activeTask.start();
                    LongTaskManager.LOG.debug("Task finished.");
                } catch (final Exception x) {
                    // log any exception uncaught
                    
                    LongTaskManager.LOG.error("Error during task run: " + x.getMessage(), x);
                } finally {
                    // finally, reset the task manager
                    
                    LongTaskManager.this.onTaskFinished();
                }
            }
        };
        
        // execute the runnable in a common thread pool
        
        RetrobiWebApplication.executeInThreadPool(taskRunnable);
        return true;
    }
    
    /**
     * Stops the active task (if any).
     * 
     * @return <code>true</code> if a task was running and was stopped,
     * <code>false</code> otherwise
     */
    public synchronized boolean stopActiveTask() {
        if (this.activeTask != null) {
            try {
                // first, stop the active task
                
                LongTaskManager.LOG.debug("Stopping active task: " + this.activeTask.getName());
                this.activeTask.stop();
                LongTaskManager.LOG.debug("Active task stopped.");
            } finally {
                // finally, reset the task manager
                
                LongTaskManager.LOG.debug("Resetting the long task manager...");
                this.onTaskFinished();
                LongTaskManager.LOG.debug("Task manager is clear.");
            }
            
            return true;
        }
        
        // if no task is running, do nothing
        
        LongTaskManager.LOG.debug("No task running, cannot stop.");
        return false;
    }
    
    /**
     * Returns the task manager version. The version number differs each time a
     * task is executed so the page knows when to reload itself.
     * 
     * @return version number
     */
    public synchronized long getVersion() {
        return this.version;
    }
    
    /**
     * Informs task manager about a task done. Resets the active task and thread
     * and increments a version number.
     */
    private synchronized void onTaskFinished() {
        LongTaskManager.LOG.debug("Task finished, proceeding with reset...");
        this.activeTask = null;
        this.version++;
    }
}
