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

package cz.insophy.retrobi.longtask;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * A card note modification
 * 
 * @author Vojtěch Hordějčuk
 */
public class NoteModification implements CardModification {
    /**
     * new value (or value to be appended)
     */
    private final String newNote;
    /**
     * append the note instead of replacement
     */
    private final boolean append;
    
    /**
     * Creates a new instance.
     * 
     * @param newNote
     * new value to be set as the note
     * @param append
     * <code>true</code> if the note should be appended to the existing note (if
     * any), or <code>false</code> to replace the existing value
     */
    public NoteModification(final String newNote, final boolean append) {
        this.newNote = newNote;
        this.append = append;
    }
    
    @Override
    public boolean modify(final Card cardToEdit) throws AlreadyModifiedException {
        final String oldNote = cardToEdit.getNote();
        
        if (this.append) {
            if ((oldNote != null) && cardToEdit.getNote().endsWith(this.newNote)) {
                // the old note is not empty AND it ends with the new note
                // we do not need to append the note again
                throw new AlreadyModifiedException();
            }
            
            if (SimpleStringUtils.isEmpty(oldNote)) {
                cardToEdit.setNote(this.newNote);
            } else {
                cardToEdit.setNote(oldNote + Settings.NOTE_SEPARATOR + this.newNote);
            }
        } else {
            if (!SimpleGeneralUtils.wasChangedAsString(oldNote, this.newNote)) {
                // setting the same note again
                throw new AlreadyModifiedException();
            }
            
            cardToEdit.setNote(this.newNote);
        }
        
        return true;
    }
    
    @Override
    public String getTitle() {
        return String.format(
                "Nastavit poznámku '%s' (%s)",
                this.newNote,
                this.append ? "připojit" : "nahradit");
    }
    
    @Override
    public String toString() {
        return this.getTitle();
    }
}
