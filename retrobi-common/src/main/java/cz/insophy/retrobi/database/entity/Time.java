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

package cz.insophy.retrobi.database.entity;

import java.util.Calendar;

import org.svenson.JSONProperty;

/**
 * Simple class representing a time. This time class contains only hours and
 * minutes for there is no need to work with seconds.
 * 
 * @author Vojtěch Hordějčuk
 */
public class Time extends Date {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * hour (0-23)
     */
    private int timeHour;
    /**
     * minute (0-59)
     */
    private int timeMinute;
    /**
     * second (0-59)
     */
    private int timeSecond;
    
    /**
     * Creates a new time object initialized to current time.
     * 
     * @return time object set to the current time
     */
    public static Time now() {
        final Calendar c = Calendar.getInstance();
        
        final int d = c.get(Calendar.DAY_OF_MONTH);
        // NOTE: January is 0
        final int m = 1 + c.get(Calendar.MONTH);
        final int y = c.get(Calendar.YEAR);
        // NOTE: using HOUR_OF_DAY for 24 hour mode
        final int hh = c.get(Calendar.HOUR_OF_DAY);
        final int mm = c.get(Calendar.MINUTE);
        final int ss = c.get(Calendar.SECOND);
        
        return new Time(new Date(d, m, y), hh, mm, ss);
    }
    
    /**
     * Creates a new default instance.
     */
    public Time() {
        super();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param date
     * date
     * @param hour
     * hour
     * @param minute
     * minute
     * @param second
     * second
     */
    public Time(final Date date, final int hour, final int minute, final int second) {
        super(date.getDay(), date.getMonth(), date.getYear());
        
        this.timeHour = hour;
        this.timeMinute = minute;
        this.timeSecond = second;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns a hour.
     * 
     * @return hour
     */
    @JSONProperty(value = "hh")
    public int getHour() {
        return this.timeHour;
    }
    
    /**
     * Returns a minute.
     * 
     * @return minute
     */
    @JSONProperty(value = "mm")
    public int getMinute() {
        return this.timeMinute;
    }
    
    /**
     * Returns a minute.
     * 
     * @return minute
     */
    @JSONProperty(value = "ss")
    public int getSecond() {
        return this.timeSecond;
    }
    
    @Override
    public String toString() {
        return String.format(
                "%s %d:%02d:%02d",
                super.toString(),
                this.timeHour,
                this.timeMinute,
                this.timeSecond);
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets hour.
     * 
     * @param value
     * hour
     */
    public void setHour(final int value) {
        this.timeHour = value;
    }
    
    /**
     * Sets minute.
     * 
     * @param value
     * minute
     */
    public void setMinute(final int value) {
        this.timeMinute = value;
    }
    
    /**
     * Sets second.
     * 
     * @param value
     * second
     */
    public void setSecond(final int value) {
        this.timeSecond = value;
    }
    
    // ==========
    // COMPARSION
    // ==========
    
    /**
     * Compares two dates.
     * 
     * @param time1
     * the first time
     * @param time2
     * the second time
     * @return -1 if the first time is less than the second time, +1 if the
     * first time is greater than the second time, 0 if they are the same
     */
    public static int compare(final Time time1, final Time time2) {
        final int c = Date.compare(time1, time2);
        
        if (c != 0) {
            return c;
        }
        
        final String t1 = String.format("%02d:%02d:%02d", time1.timeHour, time1.timeMinute, time1.timeSecond);
        final String t2 = String.format("%02d:%02d:%02d", time2.timeHour, time2.timeMinute, time2.timeSecond);
        return t1.compareTo(t2);
    }
}
