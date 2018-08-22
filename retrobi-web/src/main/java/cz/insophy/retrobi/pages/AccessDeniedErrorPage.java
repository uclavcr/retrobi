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
 * Access denied error page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class AccessDeniedErrorPage extends AbstractErrorPage {
    /**
     * Creates a new instance.
     * 
     * @param parameters
     * page parameters
     */
    public AccessDeniedErrorPage(final PageParameters parameters) { // NO_UCD
        super(parameters, "Nedostatečná oprávnění", "Nemáte dostatečná oprávnění pro vstup na tuto stránku.");
    }
}
