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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract long task. Contains most of the common task code and support code.
 * 
 * @author Vojtěch Hordějčuk
 */
public abstract class AbstractLongTask implements LongTask {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLongTask.class);
    /**
     * units already done
     */
    private int done;
    /**
     * total units to be done
     */
    private int total;
    /**
     * task is finished flag
     */
    private boolean finished;
    /**
     * task is to be stopped flag
     */
    private boolean stop;
    /**
     * list of exceptions during execution
     */
    private final List<Exception> errors;
    
    /**
     * Creates a new instance.
     */
    protected AbstractLongTask() {
        this.done = 0;
        this.total = 0;
        this.finished = false;
        this.stop = false;
        this.errors = new LinkedList<Exception>();
    }
    
    @Override
    public boolean isQuick() {
        return false;
    }
    
    @Override
    public synchronized void stop() {
        this.stop = true;
    }
    
    /**
     * Checks if the task should be stop ASAP.
     * 
     * @return <code>true</code> if the task should be stop ASAP,
     * <code>false</code> otherwise
     */
    protected synchronized boolean shouldStop() {
        return this.stop;
    }
    
    @Override
    public synchronized boolean isFinished() {
        return this.finished;
    }
    
    @Override
    public synchronized List<Exception> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }
    
    /**
     * Initializes the progress and resets the counters.
     * 
     * @param newTotal
     * total number of units
     */
    protected synchronized void initProgress(final int newTotal) {
        this.done = 0;
        this.total = newTotal;
    }
    
    /**
     * Increments the progress by one step.
     */
    protected synchronized void incrementProgress() {
        this.done++;
    }
    
    /**
     * Adds an error.
     * 
     * @param error
     * error to be added
     */
    protected synchronized void addError(final Exception error) {
        AbstractLongTask.LOG.error(error.getMessage());
        this.errors.add(error);
    }
    
    /**
     * Sets the task done.
     */
    protected synchronized void setDone() {
        AbstractLongTask.LOG.debug("Setting task done...");
        this.done = this.total;
        this.finished = true;
    }
    
    @Override
    public synchronized String getStatus() {
        if (this.stop) {
            return "Ukončuji...";
        }
        
        if (this.total == 0) {
            return "Spouštím...";
        }
        
        final double percents = (100.0 * this.done) / this.total;
        
        return String.format("%.0f%%", percents);
    }
}
