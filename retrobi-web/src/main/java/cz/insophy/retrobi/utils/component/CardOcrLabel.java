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

package cz.insophy.retrobi.utils.component;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Label that shows the best textual representation of a card.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardOcrLabel extends HtmlLabel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card model
     */
    public CardOcrLabel(final String id, final IModel<Card> card) {
        super(id, new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final String text = SimpleSegmentUtils.getCardAsTextForLabel(card.getObject());
                return SimpleStringUtils.nl2br(HtmlLabel.escapeHtml(text));
            }
        });
    }
}
