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

package cz.insophy.retrobi.panel.card;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.link.DeleteCommentLink;
import cz.insophy.retrobi.utils.component.OnClickConfirmer;

/**
 * Comment list view.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CommentListView extends ListView<Comment> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param model
     * comment list model
     */
    public CommentListView(final String id, final IModel<List<Comment>> model) {
        super(id, model);
    }
    
    @Override
    protected void populateItem(final ListItem<Comment> item) {
        // create components
        
        final Component dateLabel = new Label("date", item.getModelObject().getAdded().toString());
        final Component textLabel = new Label("text", item.getModelObject().getText());
        final Component deleteLink = new DeleteCommentLink("link.delete", item.getModelObject());
        
        // setup components
        
        deleteLink.add(new OnClickConfirmer("Opravdu chcete SMAZAT tento komentář?"));
        
        // place components
        
        item.add(dateLabel);
        item.add(textLabel);
        item.add(deleteLink);
    }
}
