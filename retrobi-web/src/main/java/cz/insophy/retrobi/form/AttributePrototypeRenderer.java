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

import org.apache.wicket.markup.html.form.IChoiceRenderer;

import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.utils.Tuple;

/**
 * Custom attribute renderer.
 */
public class AttributePrototypeRenderer implements IChoiceRenderer<Tuple<String, AttributePrototype>> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    @Override
    public Object getDisplayValue(final Tuple<String, AttributePrototype> object) {
        return object.getFirst();
    }
    
    @Override
    public String getIdValue(final Tuple<String, AttributePrototype> object, final int index) {
        return String.valueOf(index);
    }
}
