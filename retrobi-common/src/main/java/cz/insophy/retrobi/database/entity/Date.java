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

import org.svenson.JSONProperty;

import cz.insophy.retrobi.database.document.BasicDocument;

/**
 * Simple class representing date.
 * 
 * @author Vojtěch Hordějčuk
 */
public class Date extends BasicDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * year
     */
    private int dateYear;
    /**
     * month (1-12)
     */
    private int dateMonth;
    /**
     * day (1-31)
     */
    private int dateDay;
    
    /**
     * Creates a new default instance.
     */
    public Date() {
        this(0, 0, 0);
    }
    
    /**
     * Creates a new instance (with parameters).
     * 
     * @param day
     * day
     * @param month
     * month
     * @param year
     * year
     */
    public Date(final int day, final int month, final int year) {
        super();
        
        this.dateYear = year;
        this.dateMonth = month;
        this.dateDay = day;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns a year.
     * 
     * @return year
     */
    @JSONProperty(value = "y")
    public int getYear() {
        return this.dateYear;
    }
    
    /**
     * Returns a month.
     * 
     * @return month
     */
    @JSONProperty(value = "m")
    public int getMonth() {
        return this.dateMonth;
    }
    
    /**
     * Returns a day of month.
     * 
     * @return day of month
     */
    @JSONProperty(value = "d")
    public int getDay() {
        return this.dateDay;
    }
    
    @Override
    public String toString() {
        return String.format("%d.%d.%04d", this.dateDay, this.dateMonth, this.dateYear);
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets year.
     * 
     * @param value
     * year
     */
    public void setYear(final int value) {
        this.dateYear = value;
    }
    
    /**
     * Sets month.
     * 
     * @param value
     * month
     */
    public void setMonth(final int value) {
        this.dateMonth = value;
    }
    
    /**
     * Sets day of month.
     * 
     * @param value
     * day of month
     */
    public void setDay(final int value) {
        this.dateDay = value;
    }
    
    // ==========
    // COMPARSION
    // ==========
    
    /**
     * Compares two dates.
     * 
     * @param date1
     * the first date
     * @param date2
     * the second date
     * @return -1 if the first date is less than the second date, +1 if the
     * first date is greater than the second date, 0 if they are the same
     */
    public static int compare(final Date date1, final Date date2) {
        final String d1 = date1 == null ? "" : String.format(
                "%04d-%02d-%02d",
                date1.dateYear,
                date1.dateMonth,
                date1.dateDay);
        
        final String d2 = date2 == null ? "" : String.format(
                "%04d-%02d-%02d",
                date2.dateYear,
                date2.dateMonth,
                date2.dateDay);
        
        return d1.compareTo(d2);
    }
}
