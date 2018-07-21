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

package cz.insophy.retrobi.panel.card.detail;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.form.AddCommentForm;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.CommentListPanel;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * Comment card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CommentDetailCardPanel extends AbstractDetailCardPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent navigator panel
     * @param card
     * card to be displayed
     */
    public CommentDetailCardPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, CardViewMode.COMMENT);
        
        // create models
        
        final IModel<List<Comment>> model = new AbstractReadOnlyModel<List<Comment>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public List<Comment> getObject() {
                if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
                    return Collections.emptyList();
                }
                
                try {
                    final List<String> ids = RetrobiApplication.db().getCommentRepository().getCommentIdsForUser(
                            card.getObject().getId(),
                            RetrobiWebSession.get().getLoggedUser().getId());
                    
                    return RetrobiApplication.db().getCommentRepository().getComments(ids);
                } catch (final GeneralRepositoryException x) {
                    CommentDetailCardPanel.this.error(x.getMessage());
                    return Collections.emptyList();
                } catch (final NotFoundRepositoryException x) {
                    CommentDetailCardPanel.this.error(x.getMessage());
                    return Collections.emptyList();
                }
            }
        };
        
        // create components
        
        final Component form = new AddCommentForm("form", parent, card);
        final Component text = new TextLabel("text", TextType.L_HELP_COMMENT);
        
        final Component list = new CommentListPanel("list", model) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && !model.getObject().isEmpty();
            }
        };
        
        // place components
        
        this.add(form);
        this.add(list);
        this.add(text);
    }
}
