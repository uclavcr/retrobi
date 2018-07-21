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
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;

/**
 * Custom editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AddCommentForm extends Form<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * comment text model
     */
    private final IModel<String> text;
    
    /**
     * Creates a new instance instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent panel
     * @param card
     * card model
     */
    public AddCommentForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, card);
        
        this.parent = parent;
        
        // create models
        
        this.text = new Model<String>("");
        
        // create components
        
        final TextArea<String> input = new TextArea<String>("input", this.text);
        
        // place components
        
        this.add(input);
    }
    
    @Override
    protected void onSubmit() {
        // check privileges
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Nemáte dostatečná oprávnění k vložení komentáře.");
            return;
        }
        
        // create comment
        
        final Comment comment = new Comment();
        
        comment.setCardId(this.getModelObject().getId());
        comment.setAdded(Time.now());
        comment.setText(this.text.getObject());
        comment.setUserId(RetrobiWebSession.get().getLoggedUser().getId());
        
        try {
            // save comment and reset the form
            
            RetrobiApplication.db().getCommentRepository().addComment(comment);
            this.text.setObject("");
            this.info("Komentář byl přidán.");
        } catch (final GeneralRepositoryException x) {
            this.error("Chyba během přidávání komentáře: " + x.getMessage());
        }
        
        // reload viewer after edit
        
        this.parent.requestCardViewerUpdate();
    }
}
