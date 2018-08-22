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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.form.OCREditorForm;
import cz.insophy.retrobi.form.SegmentEditorForm;
import cz.insophy.retrobi.model.CardPropertyListItem;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.card.CardPropertyListView;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.TextLabel;

/**
 * OCR card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class OcrDetailCardPanel extends AbstractDetailCardPanel {
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
    public OcrDetailCardPanel(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, CardViewMode.OCR);
        
        // create models
        
        final IModel<Boolean> isEditor = new AbstractReadOnlyModel<Boolean>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Boolean getObject() {
                // return TRUE if and only if the user role is editor or higher
                return RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR);
            }
        };
        
        final IModel<Boolean> isSegmented = new AbstractReadOnlyModel<Boolean>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Boolean getObject() {
                // return TRUE if and only if the card is segmented or higher
                return !card.getObject().hasLowerState(CardState.SEGMENTED);
            }
        };
        
        // create components
        
        // -----------
        // INFORMATION
        // -----------
        
        final Label infoLabel = new Label("label.state", card.getObject().getState().getDescription()) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && isSegmented.getObject() && !isEditor.getObject();
            }
        };
        
        final CardPropertyListView propertyList = new CardPropertyListView("list.property", OcrDetailCardPanel.createProperties(card.getObject())) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && isSegmented.getObject() && !isEditor.getObject();
            }
        };
        
        // ------------
        // EDITOR FORMS
        // ------------
        
        final Component ocrForm = new OCREditorForm("form.ocr", parent, card) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && !isSegmented.getObject();
            }
        };
        
        final Component segmentForm = new SegmentEditorForm("form.segment", parent, card) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && isEditor.getObject();
            }
        };
        
        final Component helpLabel = new TextLabel("label.help", TextType.L_HELP_OCR_SUMMARY);
        
        // place components
        
        this.add(infoLabel);
        this.add(propertyList);
        this.add(ocrForm);
        this.add(segmentForm);
        this.add(helpLabel);
    }
    
    /**
     * Creates a list of segment properties.
     * 
     * @param card
     * a source card
     * @return a list of properties
     */
    private static List<CardPropertyListItem> createProperties(final Card card) {
        return Arrays.asList(
                new CardPropertyListItem("Záhlaví", UserRole.GUEST, card.getSegmentHead(), false),
                new CardPropertyListItem("Názvová část", UserRole.GUEST, card.getSegmentTitle(), false),
                new CardPropertyListItem("Bibliografická část", UserRole.GUEST, card.getSegmentBibliography(), false),
                new CardPropertyListItem("Anotační část", UserRole.GUEST, card.getSegmentAnnotation(), false),
                new CardPropertyListItem("Excerptor", UserRole.GUEST, card.getSegmentExcerpter(), false));
    }
}
