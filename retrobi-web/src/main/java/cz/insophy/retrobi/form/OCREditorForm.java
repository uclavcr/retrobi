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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.link.AddSymbolLink;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.component.OnClickWindowOpener;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * OCR editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class OCREditorForm extends Form<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * original OCR model
     */
    private final IModel<String> ocrModel;
    /**
     * fixed OCR model
     */
    private final IModel<String> ocrFixModel;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent panel
     * @param card
     * card model
     */
    public OCREditorForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, card);
        
        this.parent = parent;
        
        // initialize models
        
        this.ocrModel = Model.of(card.getObject().getOcr());
        this.ocrFixModel = Model.of(card.getObject().getOcrFix());
        
        if (SimpleStringUtils.isEmpty(this.ocrFixModel.getObject())) {
            this.ocrFixModel.setObject(this.ocrModel.getObject());
        }
        
        // create components
        
        final Component input = new TextArea<String>("input.ocr", this.ocrFixModel);
        final Component linkAddSegment = new AddSymbolLink("link.add.segment", "input.ocr", " " + Settings.SYMBOL_SEGMENT_ENCODED);
        final Component linkAddAnonymous = new AddSymbolLink("link.add.anonymous", "input.ocr", "ʘ");
        final Component linkAddLeftBracket = new AddSymbolLink("link.add.lsquare", "input.ocr", "[");
        final Component linkAddRightBracket = new AddSymbolLink("link.add.rsquare", "input.ocr", "]");
        final Component linkAddLT = new AddSymbolLink("link.add.lt", "input.ocr", "&lt;");
        final Component linkAddGT = new AddSymbolLink("link.add.gt", "input.ocr", "&gt;");
        final Component linkHelp = new BookmarkableHelpLink("link.help", TextType.L_HELP_OCR);
        
        final Component label = new Label("label.ocr", this.ocrModel) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return super.isVisible() && !SimpleStringUtils.isEmpty(OCREditorForm.this.ocrModel.getObject());
            }
        };
        
        // setup components
        
        linkHelp.add(new OnClickWindowOpener());
        
        // place components
        
        this.add(label);
        this.add(input);
        this.add(linkAddSegment);
        this.add(linkAddAnonymous);
        this.add(linkAddLeftBracket);
        this.add(linkAddRightBracket);
        this.add(linkAddLT);
        this.add(linkAddGT);
        this.add(linkHelp);
    }
    
    @Override
    protected void onSubmit() {
        // check privileges
        
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.USER)) {
            this.error("Nemáte dostatečná oprávnění pro změnu přepisu OCR.");
            return;
        }
        
        // change input
        
        if (this.ocrFixModel.getObject() == null) {
            this.error("Zadejte přepis OCR.");
            return;
        }
        
        try {
            // change the card OCR
            
            RetrobiOperations.changeCardOcr(
                    this.getModelObject().getId(),
                    this.ocrFixModel.getObject(),
                    RetrobiWebSession.get().getLoggedUser(),
                    RetrobiWebApplication.getCSVLogger());
            
            this.info("Přepis OCR byl uložen.");
            
            if (SimpleSegmentUtils.shouldBeSegmented(this.ocrFixModel.getObject())) {
                // do the segmentation
                
                RetrobiOperations.doAutomaticSegments(
                        this.getModelObject().getId(),
                        RetrobiWebSession.get().getLoggedUser(),
                        RetrobiWebApplication.getCSVLogger());
                
                this.info("Segmentace byla provedena a uložena.");
            }
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
        
        // reload viewer after edit
        
        this.parent.requestCardViewerUpdate();
    }
}
