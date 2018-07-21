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
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.pages.BrowserPage;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Bookmarkable page link to the batch page.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BookmarkableBatchLink extends BookmarkablePageLink<BrowserPage> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param catalog
     * target catalog
     * @param batch
     * target batch
     */
    public BookmarkableBatchLink(final String id, final Catalog catalog, final String batch) {
        super(id, BrowserPage.class, BookmarkableBatchLink.createParameters(catalog, batch));
    }
    
    /**
     * Creates parameters for viewing the given catalog position.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch
     * @return page parameters
     */
    private static PageParameters createParameters(final Catalog catalog, final String batch) {
        final PageParameters parameters = new PageParameters();
        parameters.add(RetrobiWebApplication.PARAM_CATALOG, SimpleStringUtils.encodeForUrl(catalog.name()));
        parameters.add(RetrobiWebApplication.PARAM_BATCH, SimpleStringUtils.encodeForUrl(batch));
        return parameters;
    }
}
