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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 * Label containing a HTML code.
 * 
 * @author Vojtěch Hordějčuk
 */
public class HtmlLabel extends Label {
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
     * model of HTML label contents
     */
    public HtmlLabel(final String id, final IModel<?> model) {
        super(id, model);
        
        // disable HTML escaping
        
        this.setEscapeModelStrings(false);
    }
    
    /**
     * Escapes special HTML entities in the text so they can be safely inserted
     * into the HTML code.
     * 
     * @param source
     * source string
     * @return source string with all HTML entities escaped
     */
    public static String escapeHtml(final String source) {
        return Strings.escapeMarkup(source).toString();
    }
}
