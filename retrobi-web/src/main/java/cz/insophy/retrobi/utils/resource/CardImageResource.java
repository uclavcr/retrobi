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

package cz.insophy.retrobi.utils.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.wicket.Resource;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.IResourceStream;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.RetrobiWebApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Card image resource. It is a dynamically generated image in JPEG format.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardImageResource extends Resource {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    @Override
    public IResourceStream getResourceStream() {
        // generate image
        
        BufferedImage image = null;
        
        // acquire parameters
        
        final String pCardId = SimpleStringUtils.decodeFromUrl(this.getParameters().getString(RetrobiWebApplication.PARAM_CARD));
        final String pImageName = SimpleStringUtils.decodeFromUrl(this.getParameters().getString(RetrobiWebApplication.PARAM_IMAGE));
        final String pWidth = SimpleStringUtils.decodeFromUrl(this.getParameters().getString(RetrobiWebApplication.PARAM_WIDTH));
        final String pCrop = SimpleStringUtils.decodeFromUrl(this.getParameters().getString(RetrobiWebApplication.PARAM_CROP));
        
        if ((pCardId != null) && (pImageName != null) && (pWidth != null) && (pCrop != null)) {
            try {
                final String cardId = pCardId;
                final String imageName = pImageName;
                final int width = Math.min(Settings.TARGET_IMAGE_WIDTH, Integer.valueOf(pWidth));
                final boolean crop = Boolean.valueOf(pCrop);
                
                try {
                    // load the original image
                    
                    image = RetrobiApplication.db().getCardImageRepository().getCardImage(cardId, imageName);
                    
                    // make thumbnail
                    
                    image = SimpleImageUtils.makeThumbnailImage(image, width, false);
                    
                    // crop (if desired)
                    
                    if (crop) {
                        image = SimpleImageUtils.makeCroppedImage(image);
                    }
                    
                    // cross (if desired)
                    
                    if (ImageFlag.CROSSOUT.inImageName(imageName)) {
                        SimpleImageUtils.crossout(image);
                    }
                } catch (final GeneralRepositoryException x) {
                    // NOP
                }
                
                if (image == null) {
                    // handle the exceptional state (known parameters)
                    
                    image = SimpleImageUtils.makeErrorCardImage(width, crop);
                }
            } catch (final RuntimeException x) {
                // NOP
            }
        }
        
        if (image == null) {
            // handle the exceptional state (unknown parameters)
            
            image = SimpleImageUtils.makeErrorCardImage(Settings.PREVIEW_IMAGE_WIDTH, false);
        }
        
        // return the image as a JPEG stream
        
        final BufferedImage fimage = image;
        
        return new AbstractResourceStreamWriter() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public String getContentType() {
                return "image/jpeg";
            }
            
            @Override
            public void write(final OutputStream output) {
                try {
                    ImageIO.write(fimage, "jpg", output);
                } catch (final IOException ex) {
                    // NOP
                }
            }
        };
    }
}
