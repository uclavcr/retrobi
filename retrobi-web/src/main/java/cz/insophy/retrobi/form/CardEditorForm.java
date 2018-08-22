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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Custom editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardEditorForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent container
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * batch model
     */
    private final IModel<Card> card;
    /**
     * card URL address
     */
    private final IModel<String> url;
    /**
     * card note
     */
    private final IModel<String> note;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent container
     * @param card
     * model
     */
    public CardEditorForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id);
        
        // create models
        
        this.parent = parent;
        this.card = card;
        this.url = Model.of(card.getObject().getUrl());
        this.note = Model.of(card.getObject().getNote());
        
        // create components
        
        final TextField<String> urlInput = new TextField<String>("input.url", this.url);
        final TextArea<String> noteInput = new TextArea<String>("input.note", this.note);
        
        // place components
        
        this.add(urlInput);
        this.add(noteInput);
    }
    
    @Override
    protected void onSubmit() {
        // check privileges
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
            this.error("Nemáte dostatečná oprávnění k provedení úpravy.");
            return;
        }
        
        try {
            // assign card properties and update card
            
            final String newUrl = SimpleStringUtils.emptyToNull(this.url.getObject());
            final String newNote = SimpleStringUtils.emptyToNull(this.note.getObject());
            
            RetrobiOperations.updateCard(
                    this.card.getObject().getId(),
                    newUrl,
                    newNote,
                    RetrobiWebSession.get().getLoggedUser(),
                    RetrobiWebApplication.getCSVLogger());
            
            this.info("Základní údaje na lístku byly uloženy.");
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
        
        // reload viewer after edit
        
        this.parent.requestCardViewerUpdate();
    }
}
