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

package cz.insophy.retrobi.link;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.pages.HelpPage;

/**
 * Bookmarkable page link to the help page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BookmarkableHelpLink extends BookmarkablePageLink<HelpPage> {
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
    public BookmarkableHelpLink(final String id) {
        super(id, HelpPage.class);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param text
     * text to show
     */
    public BookmarkableHelpLink(final String id, final TextType text) {
        super(id, HelpPage.class, BookmarkableHelpLink.createParameters(text));
    }
    
    /**
     * Creates a page parameters.
     * 
     * @param text
     * text to show
     * @return page parameters
     */
    private static PageParameters createParameters(final TextType text) {
        final PageParameters parameters = new PageParameters();
        parameters.put(RetrobiWebApplication.PARAM_TEXT, text.name());
        return parameters;
    }
}
