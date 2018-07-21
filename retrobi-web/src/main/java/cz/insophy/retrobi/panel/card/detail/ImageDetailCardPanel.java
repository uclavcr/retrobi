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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.form.ImageEditorForm;
import cz.insophy.retrobi.form.ImageUploadForm;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.CardImage;
import cz.insophy.retrobi.utils.component.GridListView;

/**
 * Image editor card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImageDetailCardPanel extends AbstractDetailCardPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * grid image width (in pixels)
     */
    private static final int GRID_IMAGE_WIDTH = (int) (Settings.DISPLAY_IMAGE_WIDTH / 2.3);
    
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
    public ImageDetailCardPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, CardViewMode.IMAGE);
        
        // create components
        
        final Component addForm = new ImageUploadForm("form.add", parent, card);
        final Component editForm = new ImageEditorForm("form.edit", parent, card);
        
        final Component imageGrid = new GridListView<String>("grid.image", "rows", "cols", card.getObject().getAttachmentNamesSorted(), 2) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void populateCell(final ListItem<String> item) {
                item.add(new CardImage("image", card.getObject(), item.getModelObject(), ImageDetailCardPanel.GRID_IMAGE_WIDTH, false));
                item.add(new Label("label", item.getModel()));
            }
            
            @Override
            protected void populateEmptyCell(final ListItem<String> item) {
                final Component imageContainer = new WebMarkupContainer("image");
                imageContainer.setVisible(false);
                item.add(imageContainer);
                item.add(new Label("label"));
            }
        };
        
        // place components
        
        this.add(addForm);
        this.add(editForm);
        this.add(imageGrid);
    }
}
