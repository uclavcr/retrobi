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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ResourceLink;

import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;
import cz.insophy.retrobi.utils.resource.CardImageResourceReference;

/**
 * Card image component.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardImage extends Image {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * card ID
     */
    private final String cardId;
    /**
     * image name
     */
    private final String imageName;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card model
     * @param imageName
     * image name model
     * @param width
     * image width model
     * @param crop
     * crop the image
     */
    public CardImage(final String id, final Card card, final String imageName, final int width, final boolean crop) {
        super(id, new CardImageResourceReference(), CardImage.createParameters(card.getId(), imageName, width, crop));
        this.cardId = card.getId();
        this.imageName = imageName;
    }
    
    /**
     * Creates a download link component.
     * 
     * @param id
     * target component ID
     * @return a link component
     */
    public AbstractLink createDownloadLink(final String id) {
        final PageParameters parameters = CardImage.createParameters(this.cardId, this.imageName, Settings.TARGET_IMAGE_WIDTH, false);
        
        return new ResourceLink<Object>(id, new CardImageResourceReference(), parameters) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
                super.onComponentTagBody(markupStream, openTag);
                openTag.put("onclick", "return !window.open(this.href);");
            }
        };
    }
    
    /**
     * Creates target page parameters for the download link.
     * 
     * @param cardId
     * card ID
     * @param imageName
     * image name
     * @param width
     * card width
     * @param crop
     * crop flag
     * @return page parameters
     */
    private static PageParameters createParameters(final String cardId, final String imageName, final int width, final boolean crop) {
        final PageParameters parameters = new PageParameters();
        parameters.add(RetrobiWebApplication.PARAM_CARD, SimpleStringUtils.encodeForUrl(cardId));
        parameters.add(RetrobiWebApplication.PARAM_IMAGE, SimpleStringUtils.encodeForUrl(imageName));
        parameters.add(RetrobiWebApplication.PARAM_WIDTH, SimpleStringUtils.encodeForUrl(String.valueOf(width)));
        parameters.add(RetrobiWebApplication.PARAM_CROP, SimpleStringUtils.encodeForUrl(String.valueOf(crop)));
        return parameters;
    }
}
