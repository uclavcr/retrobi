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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.database.RetrobiOperations;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;

/**
 * Segment editor form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class SegmentEditorForm extends Form<Card> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * head segment model
     */
    private final IModel<String> modelH;
    /**
     * title segment model
     */
    private final IModel<String> modelT;
    /**
     * bibliography segment model
     */
    private final IModel<String> modelB;
    /**
     * annotation segment model
     */
    private final IModel<String> modelA;
    /**
     * excerpter segment model
     */
    private final IModel<String> modelE;
    
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
    public SegmentEditorForm(final String id, final AbstractCardNavigatorPanel parent, final IModel<Card> card) {
        super(id, card);
        
        this.parent = parent;
        
        // initialize models
        
        this.modelH = Model.of(card.getObject().getSegmentHead());
        this.modelT = Model.of(card.getObject().getSegmentTitle());
        this.modelB = Model.of(card.getObject().getSegmentBibliography());
        this.modelA = Model.of(card.getObject().getSegmentAnnotation());
        this.modelE = Model.of(card.getObject().getSegmentExcerpter());
        
        // create components
        
        final Component inputH = new TextArea<String>("input.segment.h", this.modelH);
        final Component inputT = new TextArea<String>("input.segment.t", this.modelT);
        final Component inputB = new TextArea<String>("input.segment.b", this.modelB);
        final Component inputA = new TextArea<String>("input.segment.a", this.modelA);
        final Component inputE = new TextArea<String>("input.segment.e", this.modelE);
        
        // place components
        
        this.add(inputH);
        this.add(inputT);
        this.add(inputB);
        this.add(inputA);
        this.add(inputE);
    }
    
    @Override
    protected void onSubmit() {
        if (!RetrobiWebSession.get().hasRoleAtLeast(UserRole.EDITOR)) {
            this.error("Nemáte dostatečná oprávnění pro změnu přepisu OCR.");
            return;
        }
        
        try {
            // change the card segments manually
            
            RetrobiOperations.doManualSegments(
                    this.getModelObject().getId(),
                    this.modelH.getObject(),
                    this.modelT.getObject(),
                    this.modelB.getObject(),
                    this.modelA.getObject(),
                    this.modelE.getObject(),
                    RetrobiWebSession.get().getLoggedUser(),
                    RetrobiWebApplication.getCSVLogger());
        } catch (final GeneralOperationException x) {
            this.error(x.getMessage());
        }
        
        // reload viewer after edit
        
        this.parent.requestCardViewerUpdate();
    }
}
