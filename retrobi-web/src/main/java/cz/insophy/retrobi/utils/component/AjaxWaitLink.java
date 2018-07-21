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

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.model.IModel;

/**
 * Abstract AJAX link that displays busy indicator when busy. TODO: fallback
 * link
 * 
 * @author Vojtěch Hordějčuk
 * @param <T>
 * model class
 */
abstract public class AjaxWaitLink<T> extends AjaxFallbackLink<T> implements IAjaxIndicatorAware {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     */
    protected AjaxWaitLink(final String id) {
        super(id);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param model
     * component model
     */
    protected AjaxWaitLink(final String id, final IModel<T> model) {
        super(id, model);
    }
    
    @Override
    public String getAjaxIndicatorMarkupId() {
        // this is the ID of a HTML component that displays
        // it is shown during the AJAX operation is active
        
        return "ajax_busy";
    }
}
