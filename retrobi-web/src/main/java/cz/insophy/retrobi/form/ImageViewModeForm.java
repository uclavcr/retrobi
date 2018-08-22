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

import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiWebSession;
import cz.insophy.retrobi.model.setup.ImageViewMode;
import cz.insophy.retrobi.panel.card.navigator.AbstractCardNavigatorPanel;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * Image mode form.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImageViewModeForm extends Form<Object> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * parent panel
     */
    private final AbstractCardNavigatorPanel parent;
    /**
     * image view mode model
     */
    private final IModel<ImageViewMode> modeModel;
    /**
     * step size model
     */
    private final IModel<Integer> stepModel;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param parent
     * parent panel
     */
    public ImageViewModeForm(final String id, final AbstractCardNavigatorPanel parent) {
        super(id);
        
        this.parent = parent;
        this.modeModel = Model.of(this.getCurrentImageViewMode());
        this.stepModel = Model.of(this.getCurrentStep());
        
        final DropDownChoice<ImageViewMode> modeCombo = new DropDownChoice<ImageViewMode>(
                "select.mode",
                this.modeModel,
                Arrays.asList(ImageViewMode.values()));
        
        final DropDownChoice<Integer> stepCombo = new DropDownChoice<Integer>(
                "select.step",
                this.stepModel,
                Arrays.asList(5, 10, 20, 50, 100));
        
        this.add(modeCombo);
        this.add(stepCombo);
    }
    
    @Override
    protected void onSubmit() {
        boolean update = false;
        boolean reset = false;
        
        // get the new values
        
        final ImageViewMode newMode = this.modeModel.getObject();
        final Integer newStep = this.stepModel.getObject();
        
        // change view settings
        
        if (SimpleGeneralUtils.wasChanged(this.getCurrentImageViewMode(), newMode)) {
            RetrobiWebSession.get().getCardView().setImageViewMode(newMode);
            this.info("Změna grafického nastavení byla provedena.");
            update = true;
        }
        
        if (SimpleGeneralUtils.wasChanged(this.getCurrentStep(), newStep)) {
            RetrobiWebSession.get().getCardView().setStep(newStep);
            this.info("Změna počtu lístků byla provedena.");
            reset = true;
        }
        
        // update viewer if necessary
        
        if (reset) {
            this.parent.requestCardViewerReset();
        } else {
            if (update) {
                this.parent.requestCardViewerUpdate();
            }
        }
    }
    
    /**
     * Returns the current image view mode.
     * 
     * @return the current image view mode
     */
    private ImageViewMode getCurrentImageViewMode() {
        return RetrobiWebSession.get().getCardView().getImageViewMode();
    }
    
    /**
     * Returns the current step size.
     * 
     * @return the current step size
     */
    private int getCurrentStep() {
        return RetrobiWebSession.get().getCardView().getStep();
    }
}
