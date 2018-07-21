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

import java.util.concurrent.locks.ReentrantLock;

/**
 * Retrobi locker class contains all re-entrant lock objects that are available
 * for the whole web application.
 * 
 * @author Vojtěch Hordějčuk
 */
final public class RetrobiLocker {
    /**
     * maintenance lock
     */
    public static final ReentrantLock MAINTENANCE_LOCK = new ReentrantLock();
    /**
     * thread pool lock
     */
    public static final ReentrantLock THREAD_POOL_LOCK = new ReentrantLock();
    /**
     * catalog model lock
     */
    public static final ReentrantLock CATALOG_MODEL_LOCK = new ReentrantLock();
    /**
     * attribute prototype tree root lock
     */
    public static final ReentrantLock ATTRIBUTE_LOCK = new ReentrantLock();
    /**
     * index list lock
     */
    public static final ReentrantLock INDEX_LOCK = new ReentrantLock();
    
    /**
     * Cannot make instances of this class.
     */
    private RetrobiLocker() {
        throw new UnsupportedOperationException();
    }
}
