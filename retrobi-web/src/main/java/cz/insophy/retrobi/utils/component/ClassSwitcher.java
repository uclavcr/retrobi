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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Boolean class switcher for components. This is a child of
 * <code>AttributeModifier</code> class and allows component to change its class
 * dynamically depending on the current boolean model value.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ClassSwitcher extends AttributeModifier {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param falseClass
     * class activated when the model is <code>false</code>
     * @param trueClass
     * class activated when the model is <code>true</code>
     * @param model
     * the boolean value model
     */
    public ClassSwitcher(final String falseClass, final String trueClass, final IModel<Boolean> model) {
        super("class", true, new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getObject() {
                if (model.getObject()) {
                    return trueClass;
                }
                
                return falseClass;
            }
        });
    }
    
    /**
     * Creates a new simple instance.
     * 
     * @param falseClass
     * class activated when the model is <code>false</code>
     * @param trueClass
     * class activated when the model is <code>true</code>
     * @param model
     * the boolean value
     */
    public ClassSwitcher(final String falseClass, final String trueClass, final boolean model) {
        this(falseClass, trueClass, Model.of(model));
    }
}
