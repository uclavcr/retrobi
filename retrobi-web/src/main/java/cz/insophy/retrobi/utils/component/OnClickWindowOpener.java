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
import org.apache.wicket.model.Model;

/**
 * Class that adds an onclick window open to a link or button. This window is as
 * small as possible (no location bar, no status bar...).
 * 
 * @author Vojtěch Hordějčuk
 */
public class OnClickWindowOpener extends AttributeModifier {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance. This one shows a new tab or a full sized window
     * after component click.
     */
    public OnClickWindowOpener() {
        super("onclick", true, Model.of(OnClickWindowOpener.createCode(null, 0, 0)));
    }
    
    /**
     * Creates a new instance. This one shows a new rather minimalistic window
     * after component click. The window size, etc. can be changed.
     * 
     * @param name
     * window name (should be web safe)
     * @param width
     * window width
     * @param height
     * window height
     */
    public OnClickWindowOpener(final String name, final int width, final int height) {
        super("onclick", true, Model.of(OnClickWindowOpener.createCode(name, width, height)));
    }
    
    /**
     * Generates a javascript onclick code.
     * 
     * @param name
     * window name
     * @param width
     * window width
     * @param height
     * window height
     * @return Javascript code fragment
     */
    private static String createCode(final String name, final int width, final int height) {
        if (name == null) {
            return "return !window.open(this.href);";
        }
        
        return String.format("return !window.open(this.href,'%s','width=%d,height=%d,toolbar=0,menubar=0,location=0,directories=0,status=0');",
                name,
                width,
                height);
    }
}
