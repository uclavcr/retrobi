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

package cz.insophy.retrobi.link;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;

/**
 * Removes the specified comment after clicking.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DeleteCommentLink extends Link<Comment> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param comment
     * comment to be deleted
     */
    public DeleteCommentLink(final String id, final Comment comment) {
        super(id, Model.of(comment));
    }
    
    @Override
    public void onClick() {
        // check that the logged user is authorized to remove the comment
        
        final User loggedUser = RetrobiWebSession.get().getLoggedUser();
        
        if (loggedUser == null) {
            this.error("Komentář nelze smazat, nejste přihlášen.");
            return;
        }
        
        if (!this.getModelObject().getUserId().equals(loggedUser.getId()) && !loggedUser.hasRoleAtLeast(UserRole.ADMIN)) {
            this.error("Komentář nelze smazat, nejste totiž autor komentáře, ani administrátor.");
            return;
        }
        
        // delete the comment finally
        
        try {
            RetrobiApplication.db().getCommentRepository().deleteComment(this.getModelObject());
            this.info("Komentář byl úspěšně smazán.");
        } catch (final GeneralRepositoryException x) {
            this.error("Chyba při přidávání komentáře: " + x.getMessage());
        }
    }
}
