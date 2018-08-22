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

package cz.insophy.retrobi.pages;

import org.apache.wicket.PageParameters;

/**
 * Runtime error page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class RuntimeErrorPage extends AbstractErrorPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public RuntimeErrorPage(final PageParameters parameters) { // NO_UCD
        super(parameters);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     * @param error
     * error to show
     */
    public RuntimeErrorPage(final PageParameters parameters, final Throwable error) {
        super(parameters, error.getClass().getSimpleName(), error);
    }
}
