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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.BooleanLabel;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card table viewer.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardTableViewer extends ListView<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent navigator panel
     */
    private final AbstractCardNavigatorPanel parent;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent navigator panel
     */
    public CardTableViewer(final String id, final AbstractCardNavigatorPanel parent) {
        super(id);
        
        this.parent = parent;
    }
    
    @Override
    public boolean isVisible() {
        if (this.getList().isEmpty()) {
            return false;
        }
        
        return super.isVisible();
    }
    
    @Override
    protected void populateItem(final ListItem<Card> item) {
        final Card card = item.getModelObject();
        final int row = item.getIndex();
        
        final WebMarkupContainer link = this.parent.createDownLink("link", row);
        
        item.add(new Label("label.catalog", card.getCatalog().name()));
        item.add(new Label("label.batch", card.getBatch()));
        item.add(new Label("label.number", String.valueOf(card.getNumberInBatch())));
        item.add(new BooleanLabel("label.ocr.original", !SimpleStringUtils.isEmpty(card.getOcr())));
        item.add(new BooleanLabel("label.ocr.fixed", !SimpleStringUtils.isEmpty(card.getOcrFix())));
        item.add(new BooleanLabel("label.segmentation", !SimpleSegmentUtils.isSegmentationEmpty(card)));
        item.add(new Label("label.images", String.valueOf(card.getAttachmentCount())));
        link.add(new Label("label", link.getDefaultModel()));
        item.add(link);
    }
}
