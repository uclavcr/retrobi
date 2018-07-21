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

package cz.insophy.retrobi.database;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.jcouchdb.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;

/**
 * Class that provides access to card images. Images are saved as attachments of
 * individual cards. Images are never fetched directly with the card as they are
 * too big, but separately (on demand) using this repository. Class provides
 * methods for managing these images as well (adding, removing).
 * 
 * @author Vojtěch Hordějčuk
 */
final public class CardImageRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(CardImageRepository.class);
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected CardImageRepository(final Database database) {
        super(database);
    }
    
    /**
     * Adds an image to the given card. If the card already contains image with
     * the same name, it will be overwritten. Use this method for adding files.
     * 
     * @param card
     * target card
     * @param file
     * file containing the image to be added
     * @param imageName
     * image name (no extension)
     * @param mimeType
     * mime type of the image (e.g. "image/png")
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void addImageToCard(final Card card, final File file, final String imageName, final String mimeType) throws GeneralRepositoryException {
        CardImageRepository.LOG.debug(String.format("Adding file '%s' to card '%s' as image '%s' with MIME type '%s'...", file.getName(), card, imageName, mimeType));
        
        // validate file
        
        if (!file.exists() || !file.canRead() || !file.isFile() || (file.length() < 100)) {
            CardImageRepository.LOG.warn(String.format("File '%s' is invalid or unreadable.", file.getAbsolutePath()));
            throw new GeneralRepositoryException(String.format("Neplatný nebo nečitelný soubor s obrázkem: %s", file.getAbsolutePath()));
        }
        
        InputStream stream = null;
        
        try {
            // create an attachment using the file stream
            
            CardImageRepository.LOG.debug("Creating attachment...");
            stream = new BufferedInputStream(new FileInputStream(file));
            this.createAttachment(card, imageName, mimeType, stream, file.length());
            CardImageRepository.LOG.debug("Image was successfully attached.");
        } catch (final IOException x) {
            CardImageRepository.LOG.error(String.format("I/O error while creating attachment: %s", x.getMessage()));
            throw new GeneralRepositoryException("Chyba během přiřazování obrázku k lístku: " + x.getMessage(), x);
        } finally {
            // close the stream if needed
            
            if (stream != null) {
                try {
                    stream.close();
                } catch (final IOException x) {
                    CardImageRepository.LOG.error(String.format("I/O error while closing stream: %s", x.getMessage()));
                    throw new GeneralRepositoryException("Chyba při uzavírání streamu: " + x.getMessage(), x);
                }
            }
        }
    }
    
    /**
     * Adds an image to the given card. If the card already contains image with
     * the same name, it will be overwritten. Use this method for adding
     * rendered images. Note: the card revision will be updated.
     * 
     * @param card
     * target card
     * @param image
     * image to be added
     * @param imageName
     * image name (no extension)
     * @param format
     * format name (e.g. "png")
     * @param mimeType
     * MIME type (e.g. "image/png")
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void addImageToCard(final Card card, final RenderedImage image, final String imageName, final String format, final String mimeType) throws GeneralRepositoryException {
        CardImageRepository.LOG.debug(String.format("Adding a rendered image '%s' of format '%s' and MIME type '%s' to card '%s'...", imageName, format, mimeType, card));
        
        File tempFile = null;
        
        try {
            // create a temporary file
            
            tempFile = File.createTempFile("retrobi_image_temp", format);
            CardImageRepository.LOG.debug(String.format("Using a temporary file '%s'...", tempFile.getAbsolutePath()));
            
            // write an image into the temporary file
            
            if (!ImageIO.write(image, format, tempFile)) {
                CardImageRepository.LOG.error(String.format("Image format '%s' is invalid.", format));
                throw new GeneralRepositoryException(String.format("Neplatný formát souboru (%s).", format));
            }
            
            // add the image to the card using the existing method
            
            this.addImageToCard(card, tempFile, imageName, mimeType);
        } catch (final IOException x) {
            CardImageRepository.LOG.error(String.format("I/O error while adding image: %s", x.getMessage()));
            throw new GeneralRepositoryException("Chyba při přiřazování obrázku.", x);
        } finally {
            // delete the temporary file
            
            if ((tempFile != null) && tempFile.exists()) {
                if (tempFile.delete()) {
                    CardImageRepository.LOG.debug(String.format("Temporary file '%s' was removed.", tempFile.getAbsolutePath()));
                } else {
                    CardImageRepository.LOG.warn(String.format("Removing of temporary file '%s' has failed.", tempFile.getAbsolutePath()));
                }
            }
        }
    }
    
    /**
     * Removes the specified image from card attachments. If the card does not
     * contain the specified image, an exception will be thrown. Note: the card
     * revision will be updated.
     * 
     * @param card
     * target card
     * @param imageName
     * image name (no extension)
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public void removeImageFromCard(final Card card, final String imageName) throws GeneralRepositoryException, NotFoundRepositoryException { // NO_UCD
        CardImageRepository.LOG.debug(String.format("Removing image '%s' from card '%s'...", imageName, card.getId()));
        
        try {
            // remove attachment
            
            this.deleteAttachment(card, imageName);
            CardImageRepository.LOG.debug("Image was successfully removed.");
        } catch (final GeneralRepositoryException x) {
            CardImageRepository.LOG.error(String.format("Error while deleting card image: %s", x.getMessage()));
            throw new GeneralRepositoryException("Chyba během mazání obrázku: " + x.getMessage(), x);
        }
    }
    
    /**
     * Returns the image input stream. Do not forget to close it after use via
     * the <code>close()</code> method.
     * 
     * @param cardId
     * card ID
     * @param imageName
     * image name (no extension)
     * @return an input stream of the image (close it after use)
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public InputStream getCardImageResponse(final String cardId, final String imageName) throws GeneralRepositoryException, NotFoundRepositoryException {
        CardImageRepository.LOG.debug(String.format("Getting attachment response of image '%s' of card '%s'", imageName, cardId));
        return this.getAttachmentResponse(cardId, imageName);
    }
    
    /**
     * Returns the buffered image instance of the card image.
     * 
     * @param cardId
     * card ID
     * @param imageName
     * image name (no extension)
     * @return buffered card image as a buffered image instance
     * @throws GeneralRepositoryException
     * repository exception
     */
    public BufferedImage getCardImage(final String cardId, final String imageName) throws GeneralRepositoryException {
        CardImageRepository.LOG.debug(String.format("Loading image '%s' of card '%s'...", imageName, cardId));
        
        InputStream is = null;
        
        try {
            // get the attachment input stream
            
            is = this.getCardImageResponse(cardId, imageName);
            
            // read the image from the (buffered) stream
            
            return ImageIO.read(is);
        } catch (final NotFoundRepositoryException x) {
            // document not found
            
            CardImageRepository.LOG.error(String.format("Unable to find image: %s", x.getMessage()));
            throw new GeneralRepositoryException("Could not find image: " + x.getMessage(), x);
        } catch (final IOException x) {
            // IO exception
            
            CardImageRepository.LOG.error(String.format("I/O exception while getting image: %s", x.getMessage()));
            throw new GeneralRepositoryException("Could not get image: " + x.getMessage(), x);
        } finally {
            // close the response (if any)
            
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException x) {
                    // NOP
                }
            }
        }
    }
}
