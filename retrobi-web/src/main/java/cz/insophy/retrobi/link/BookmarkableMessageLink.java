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
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.pages.MessagePage;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Bookmarkable link that allows user to report an error or send other message.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BookmarkableMessageLink extends BookmarkablePageLink<MessagePage> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param refCard
     * reference card (or <code>null</code>)
     * @param refImage
     * reference image (or <code>null</code>)
     */
    public BookmarkableMessageLink(final String id, final Card refCard, final String refImage) {
        super(id, MessagePage.class, BookmarkableMessageLink.createParameters(refCard, refImage));
    }
    
    /**
     * Creates parameters for sending a message.
     * 
     * @param refCard
     * reference card
     * @param refImage
     * reference image
     * @return page parameters
     */
    private static PageParameters createParameters(final Card refCard, final String refImage) {
        final PageParameters parameters = new PageParameters();
        
        if (refCard != null) {
            parameters.add(RetrobiWebApplication.PARAM_CARD, SimpleStringUtils.encodeForUrl(refCard.getId()));
        }
        
        if (refImage != null) {
            parameters.add(RetrobiWebApplication.PARAM_IMAGE, SimpleStringUtils.encodeForUrl(refImage));
        }
        
        return parameters;
    }
}
