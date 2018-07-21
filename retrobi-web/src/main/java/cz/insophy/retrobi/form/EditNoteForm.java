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

package cz.insophy.retrobi.form;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.longtask.NoteModification;
import cz.insophy.retrobi.model.task.BatchModificationTask;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card note editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class EditNoteForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * new note value model
     */
    private final IModel<String> note;
    /**
     * append note flag model
     */
    private final IModel<Boolean> append;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    public EditNoteForm(final String id) {
        super(id);
        
        // prepare models
        
        this.note = Model.of("");
        this.append = Model.of(true);
        
        // create components
        
        final TextArea<String> noteField = new TextArea<String>("input", this.note);
        final CheckBox appendCheck = new CheckBox("append", this.append);
        
        // place components
        
        this.add(noteField);
        this.add(appendCheck);
    }
    
    @Override
    protected void onSubmit() {
        try {
            // modify cards
            
            final CardModification modification = new NoteModification(
                    SimpleStringUtils.nullToEmpty(this.note.getObject()).trim(),
                    this.append.getObject());
            
            RetrobiWebSession.get().scheduleTask(new BatchModificationTask(
                    RetrobiWebSession.get().getCardContainer().getBasketCardIds(),
                    RetrobiWebSession.get().getCardContainer().getBatchModificationResult(),
                    modification));
        } catch (final InterruptedException x) {
            this.error(x.getMessage());
        }
    }
}
