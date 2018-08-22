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
import cz.insophy.retrobi.pages.AdminTextEditorPage;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Bookmarkable edit text link.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BookmarkableEditTextLink extends BookmarkablePageLink<AdminTextEditorPage> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param text
     * text to edit
     */
    public BookmarkableEditTextLink(final String id, final TextType text) {
        super(id, AdminTextEditorPage.class, BookmarkableEditTextLink.createPageParameters(text));
    }
    
    /**
     * Creates parameters for editing the text.
     * 
     * @param text
     * text to edit
     * @return page parameters
     */
    private static PageParameters createPageParameters(final TextType text) {
        final PageParameters parameters = new PageParameters();
        parameters.add(RetrobiWebApplication.PARAM_TEXT, SimpleStringUtils.encodeForUrl(text.name()));
        return parameters;
    }
}
