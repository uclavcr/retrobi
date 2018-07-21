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

package cz.insophy.retrobi.exception;

/**
 * Exception that occurs when no document is found.
 * 
 * @author Vojtěch Hordějčuk
 */
public class NotFoundRepositoryException extends Exception {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param docId
     * document ID
     * @param cause
     * cause of the exception
     */
    public NotFoundRepositoryException(final String docId, final Exception cause) {
        super(String.format("Dokument s ID = %s nebyl nalezen.", docId, cause));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param docId
     * document ID
     * @param attachmentId
     * attachment name
     * @param cause
     * cause of the exception
     */
    public NotFoundRepositoryException(final String docId, final String attachmentId, final Exception cause) {
        super(String.format("Příloha '%s' dokumentu s ID = %s nebyla nalezena.", attachmentId, docId, cause));
    }
}
