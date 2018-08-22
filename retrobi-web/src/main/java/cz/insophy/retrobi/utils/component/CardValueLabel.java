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

import java.io.Serializable;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Special label component for showing card values. This component is similar to
 * a regular label, but preserves string newlines by inserting &lt;br&gt; tags
 * explicitly.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardValueLabel extends HtmlLabel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param value
     * general value
     */
    public CardValueLabel(final String id, final Object value) {
        this(id, new AbstractReadOnlyModel<Serializable>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public Serializable getObject() {
                if ((value == null) || value.toString().trim().equals("")) {
                    return "-";
                } else if (value instanceof Serializable) {
                    return (Serializable) value;
                } else {
                    return "(neplatná hodnota)";
                }
            }
        });
    }
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param model
     * component model providing the OCR text
     */
    private CardValueLabel(final String id, final IModel<?> model) {
        super(id, new AbstractReadOnlyModel<Object>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                final String value = HtmlLabel.escapeHtml(SimpleStringUtils.neverEmpty(String.valueOf(model.getObject())));
                
                if (value.startsWith("http://")) {
                    return String.format("<a href=\"%s\" onclick=\"return !window.open(this.href);\">%s</a>", value, value);
                }
                
                return SimpleStringUtils.nl2br(value);
            }
        });
    }
}
