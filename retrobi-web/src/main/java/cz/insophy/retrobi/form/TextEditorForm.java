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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * Custom text editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class TextEditorForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * editing text type
     */
    private final TextType text;
    /**
     * new text contents
     */
    private final IModel<String> contents;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param text
     * text type
     */
    public TextEditorForm(final String id, final TextType text) {
        super(id);
        
        // prepare models
        
        this.text = text;
        this.contents = Model.of(RetrobiApplication.db().getTextRepository().getText(this.text));
        
        // create components
        
        final TextArea<String> editor = new TextArea<String>("editor", this.contents);
        
        // setup components
        
        editor.setLabel(Model.of("Text"));
        
        if (!text.isHtml()) {
            editor.setRequired(true);
        }
        
        // place components
        
        this.add(editor);
    }
    
    @Override
    protected void onSubmit() {
        try {
            RetrobiApplication.db().getTextRepository().setText(this.text, this.contents.getObject().trim());
            this.info("Text byl uložen.");
        } catch (final GeneralRepositoryException x) {
            this.error("Chyba při ukládání textu: " + x.getMessage());
        }
    }
}
