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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.form.CardEditorForm;
import cz.insophy.retrobi.link.CleanTreeLink;
import cz.insophy.retrobi.link.DeleteBatchLink;
import cz.insophy.retrobi.link.DeleteCardLink;
import cz.insophy.retrobi.link.RenumberBatchLink;
import cz.insophy.retrobi.link.ReorderCardLink;
import cz.insophy.retrobi.link.ResetTreeLink;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.OnClickConfirmer;

/**
 * Editor card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MoveDetailCardPanel extends AbstractDetailCardPanel {
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
    public MoveDetailCardPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, CardViewMode.MOVE);
        
        // create components
        
        final WebMarkupContainer editorForm = new CardEditorForm("form", parent, card);
        
        final AbstractLink moveCardLinkBefore = new ReorderCardLink("link.move.before", card.getObject(), false) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerReset();
            }
        };
        
        final AbstractLink moveCardLinkAfter = new ReorderCardLink("link.move.after", card.getObject(), true) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerReset();
            }
        };
        
        final AbstractLink renumberLink = new RenumberBatchLink("link.renumber", card.getObject().getCatalog(), card.getObject().getBatch()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerReset();
            }
        };
        
        final AbstractLink resetTreeLink = new ResetTreeLink("link.reset_tree", card.getObject().getId()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerUpdate();
            }
        };
        
        final AbstractLink cleanTreeLink = new CleanTreeLink("link.clean_tree", card.getObject().getId()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerUpdate();
            }
        };
        
        final AbstractLink removeLink = new DeleteCardLink("link.delete", card.getObject().getId()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerUpdate();
            }
        };
        
        final AbstractLink removeBatchLink = new DeleteBatchLink("link.delete.batch", card.getObject().getCatalog(), card.getObject().getBatch()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick() {
                super.onClick();
                parent.requestCardViewerReset();
            }
        };
        
        // setup components
        
        moveCardLinkBefore.add(new OnClickConfirmer("Opravdu chcete vložit schránku PŘED tento lístek?"));
        moveCardLinkAfter.add(new OnClickConfirmer("Opravdu chcete vložit schránku ZA tento lístek?"));
        renumberLink.add(new OnClickConfirmer("Opravdu chcete přečíslovat tuto skupinu?"));
        removeLink.add(new OnClickConfirmer("Opravdu chcete SMAZAT tento lístek?"));
        removeBatchLink.add(new OnClickConfirmer("Opravdu chcete SMAZAT celou skupinu?"));
        
        // place components
        
        this.add(editorForm);
        this.add(moveCardLinkBefore);
        this.add(moveCardLinkAfter);
        this.add(renumberLink);
        this.add(resetTreeLink);
        this.add(cleanTreeLink);
        this.add(removeLink);
        this.add(removeBatchLink);
    }
}
