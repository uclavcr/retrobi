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
 * Message state options.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum MessageStateOption {
    /**
     * confirmed only (both events and problems)
     */
    CONFIRMED_ALL,
    /**
     * unconfirmed only (both events and problems)
     */
    UNCONFIRMED_ALL,
    /**
     * confirmed problems (not events)
     */
    CONFIRMED_PROBLEMS,
    /**
     * unconfirmed problems (not events)
     */
    UNCONFIRMED_PROBLEMS,
    /**
     * confirmed events (not problems)
     */
    CONFIRMED_EVENTS,
    /**
     * unconfirmed events (not problems)
     */
    UNCONFIRMED_EVENTS;
    
    /**
     * Checks if two filters are compatible.
     * 
     * @param type
     * message type filter
     * @return <code>true</code> if the filters are compatible,
     * <code>false</code> otherwise
     */
    public boolean isCompatible(final MessageType type) {
        if (type == null) {
            // no limitation
            return true;
        }
        
        if (this.isAll()) {
            // compatible with both
            return true;
        }
        
        if (type.isEvent()) {
            // event to event
            return this.isEvent();
        }
        
        // problem to problem
        return this.isProblem();
    }
    
    /**
     * Checks if the enumeration constant represents an event.
     * 
     * @return <code>true</code> if the constant includes an event,
     * <code>false</code> otherwise
     */
    private boolean isEvent() {
        return this.name().endsWith("_EVENTS");
    }
    
    /**
     * Checks if the enumeration constant represents a problem.
     * 
     * @return <code>true</code> if the constant includes a problem,
     * <code>false</code> otherwise
     */
    private boolean isProblem() {
        return this.name().endsWith("_PROBLEMS");
    }
    
    /**
     * Checks if the enumeration constant includes both events and problems.
     * 
     * @return <code>true</code> if the constant represents both events and
     * problems, <code>false</code> otherwise
     */
    private boolean isAll() {
        return this.name().endsWith("_ALL");
    }
    
    @Override
    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "uzavřené (vše)";
            case 1:
                return "otevřené (vše)";
            case 2:
                return "uzavřená hlášení";
            case 3:
                return "otevřená hlášení";
            case 4:
                return "uzavřené události";
            case 5:
                return "otevřené události";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
