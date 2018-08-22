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

package cz.insophy.retrobi;

import cz.insophy.retrobi.database.DatabaseConnector;

/**
 * Main application class. It manages the database connection and all related
 * repositories (classes aimed for working with the database).
 * 
 * @author Vojtěch Hordějčuk
 */
public final class RetrobiApplication {
    /**
     * Returns the database connector object, which is the main object for
     * accessing and manipulating data in the application database.
     * 
     * @return database connector
     */
    public static DatabaseConnector db() {
        return DatabaseConnector.getInstance();
    }
    
    /**
     * Cannot make instances of this class.
     */
    private RetrobiApplication() {
        throw new UnsupportedOperationException();
    }
}
