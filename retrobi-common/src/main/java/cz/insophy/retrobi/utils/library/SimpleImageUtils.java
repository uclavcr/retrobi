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

package cz.insophy.retrobi.utils.library;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.sanselan.Sanselan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;

/**
 * Image utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleImageUtils {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleImageUtils.class);
    
    // ====================
    // CARD IMAGE SYNTHESIS
    // ====================
    
    /**
     * Returns an expected card image dimension. The resulting dimension is
     * oriented as a landscape. The height is computed automatically so the
     * resulting image has the same side ratio as A4 paper.
     * 
     * @param width
     * image width
     * @return resulting image dimension (expected)
     */
    public static Dimension getCardImageDimension(final int width) {
        final int targetHeight = (int) (width * (105.0 / 148.0));
        return new Dimension(width, targetHeight);
    }
    
    /**
     * Creates a synthetic card images for the given card. The images are
     * created using the BEST information available at the moment. The synthesis
     * uses a fixed size monospaced font to keep the exact size of each letter
     * and all the newlines in the input data are respected. Current limits:
     * <ul>
     * <li>image width = 50 characters</li>
     * <li>image height = 14 lines</li>
     * </ul>
     * 
     * @param card
     * input card
     * @return list of synthesized card images (each of A6 size)
     */
    public static List<BufferedImage> synthesizeCardImages(final Card card) {
        // initialize (current settings = 14 rows, 50 columns)
        
        final Dimension paper = SimpleImageUtils.getCardImageDimension(Settings.TARGET_IMAGE_WIDTH);
        final int targetWidth = paper.width;
        final int targetHeight = paper.height;
        final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 25);
        final int maxCols = 50;
        final int maxLines = 14;
        final int lineHeight = 37;
        final int margin = 24;
        final int ascent = 28;
        
        // get all the lines to be written
        
        final String[] lines = SimpleImageUtils.getCardLines(card, maxCols);
        
        // start generating images
        
        final List<BufferedImage> images = new LinkedList<BufferedImage>();
        
        final int pageCount = Math.max(1, (int) (Math.ceil((double) lines.length / (double) maxLines)));
        
        for (int page = 0; page < pageCount; page++) {
            // create page image
            
            final BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
            final Graphics2D canvas = image.createGraphics();
            
            // prepare and clear the canvas
            
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.setColor(Color.WHITE);
            canvas.fillRect(0, 0, targetWidth, targetHeight);
            canvas.setColor(Color.BLACK);
            canvas.setFont(font);
            
            for (int line = 0; line < maxLines; line++) {
                // compute coordinates
                
                final int textX = margin;
                final int textY = margin + ascent + line * lineHeight;
                final int absline = page * maxLines + line;
                
                if (absline >= lines.length) {
                    break;
                }
                
                // draw the current line onto the page
                
                canvas.drawString(lines[absline], textX, textY);
            }
            
            // finalize page
            
            images.add(image);
            image.flush();
            canvas.dispose();
        }
        
        return Collections.unmodifiableList(images);
    }
    
    /**
     * Helper method for splitting the card information into an array of plain
     * text lines. The lines are based on the best textual representation
     * possible of the given card.
     * 
     * @param card
     * input card
     * @param width
     * wrap width
     * @return array of lines usable for doing an image synthesis
     */
    private static String[] getCardLines(final Card card, final int width) {
        final String divider = Settings.LINE_END + Settings.LINE_END;
        final String textSource;
        
        if (SimpleSegmentUtils.isSegmentationEmpty(card)) {
            // continue as usual
            textSource = SimpleSegmentUtils.getCardAsText(card, divider);
        } else {
            // continue as usual, BUT align excerpter to the right
            // (constant -2 here means "move excerpter to the left a bit")
            textSource = SimpleSegmentUtils.segmentsToStringExcerpterRight(card, divider, Math.max(0, width - 2));
        }
        
        final String textWrapped = SimpleStringUtils.wordwrap(textSource, width, Settings.LINE_END);
        return textWrapped.split(Pattern.quote(Settings.LINE_END));
    }
    
    /**
     * Loads the bitmap image file into a buffered image. Multiple formats are
     * supported (JPEG, BMP, PNG and TIFF).
     * 
     * @param file
     * input bitmap file
     * @return buffered image
     * @throws IOException
     * IO error
     */
    public static BufferedImage loadImageFromFile(final File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".tiff")) {
            // load TIFF file is complicated, leave it on another method
            
            return SimpleImageUtils.loadTiffImageFromFile(file);
        }
        
        // standard method for common image formats
        
        return ImageIO.read(file);
    }
    
    /**
     * Loads the TIFF file and returns it as a buffered image.
     * 
     * @param file
     * input bitmap file (TIFF file)
     * @return buffered image
     * @throws IOException
     * IO error
     */
    private static BufferedImage loadTiffImageFromFile(final File file) throws IOException {
        try {
            return Sanselan.getBufferedImage(file);
        } catch (final Exception x) {
            SimpleImageUtils.LOG.error(x.getMessage());
            throw new IOException(x.getMessage());
        }
    }
    
    /**
     * Saves a given image as a PNG file.
     * 
     * @param image
     * source image to write
     * @param file
     * output file
     * @throws IOException
     * IO error
     */
    public static void saveImageToPngFile(final RenderedImage image, final File file) throws IOException {
        ImageIO.write(image, "png", file);
    }
    
    // ====================
    // CREATING AND EDITING
    // ====================
    
    /**
     * Creates a placeholder for missing card image. This image looks like a red
     * cross in a white background. Colors are used from the web design.
     * 
     * @param width
     * maximal image width
     * @param crop
     * crop the card image (make the image narrower)
     * @return placeholder image for missing card image
     */
    public static BufferedImage makeErrorCardImage(final int width, final boolean crop) {
        // compute height
        
        final int height = crop ? (width / 6) : (width * 3) / 4;
        
        // create image
        
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // render error state somehow
        
        final Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(new Color(154, 10, 19));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(0, 0, image.getWidth(), image.getHeight());
        g2d.drawLine(image.getWidth(), 0, 0, image.getHeight());
        g2d.dispose();
        
        // return image
        
        return image;
    }
    
    /**
     * Crosses out the image. This operation is used to indicate that the image
     * is invalid or similar.
     * 
     * @param image
     * image to be crossed out
     */
    public static void crossout(final BufferedImage image) {
        final Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 128));
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(154, 10, 19));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(0, 0, image.getWidth(), image.getHeight());
        g2d.drawLine(image.getWidth(), 0, 0, image.getHeight());
        g2d.dispose();
    }
    
    /**
     * Returns the thumbnail (original image scaled down to the maximum width).
     * If the image is smaller, no changes are performed.
     * 
     * @param inputImage
     * original image
     * @param maxWidth
     * maximal width of the thumbnail
     * @param grayscale
     * convert to grayscale
     * @return thumbnail image constrained to the max width
     */
    public static BufferedImage makeThumbnailImage(final BufferedImage inputImage, final int maxWidth, final boolean grayscale) {
        // check input parameters
        
        if ((inputImage == null) || (inputImage.getWidth(null) < 1)) {
            return null;
        }
        
        // compute scale factor
        
        final double factor = (double) maxWidth / (double) inputImage.getWidth(null);
        
        if (factor >= 1.0) {
            // no scaling needed
            
            return inputImage;
        }
        
        // return scaled image instance
        
        final BufferedImage thumbImage = new BufferedImage(
                (int) (inputImage.getWidth(null) * factor),
                (int) (inputImage.getHeight(null) * factor),
                grayscale ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB);
        
        final Graphics2D g2 = thumbImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(inputImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), null);
        g2.dispose();
        return thumbImage;
    }
    
    /**
     * Returns a cropped instance of the input image. Cropping is an operation
     * that takes only the upper piece of the image and discards the rest.
     * Useful for the card preview. The crop ratio value is taken from the
     * application settings.
     * 
     * @param inputImage
     * the input image
     * @return cropped image instance
     */
    public static BufferedImage makeCroppedImage(final BufferedImage inputImage) {
        return inputImage.getSubimage(
                0,
                0,
                inputImage.getWidth(),
                (int) (inputImage.getHeight() / Settings.CROP_RATIO));
    }
    
    /**
     * Rotates the input image by 180 degrees and returns the result. Standard
     * affine transform available in Java 2D is used.
     * 
     * @param inputImage
     * original image
     * @return rotated image
     */
    public static BufferedImage makeImageRotatedBy180(final BufferedImage inputImage) {
        // create a new image
        
        final int w = inputImage.getWidth();
        final int h = inputImage.getHeight();
        
        final BufferedImage rotatedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        
        // use graphics and a transformation to draw the rotated image
        
        final AffineTransform t = new AffineTransform();
        t.translate(w, h);
        t.rotate(Math.PI);
        final Graphics2D g = rotatedImage.createGraphics();
        g.drawImage(inputImage, t, null);
        g.dispose();
        
        // return the resulting image
        
        return rotatedImage;
    }
    
    // ==============
    // EMPTY DETECTOR
    // ==============
    
    /**
     * Checks whether the images is likely to be empty. This method tries not to
     * make false-positives.
     * 
     * @param inputImage
     * input image
     * @param threshold
     * threshold (0 to 1 inclusive)
     * @param tolerance
     * tolerance (0 to 1 inclusive)
     * @param shave
     * (0 to 1 inclusive)
     * @return <code>true</code> if the image seems to be empty,
     * <code>false</code> otherwise
     */
    public static boolean isImageEmpty(final BufferedImage inputImage, final double threshold, final double tolerance, final double shave) {
        // create smaller image for evaluation
        
        SimpleImageUtils.LOG.debug("Testing image for emptiness...");
        SimpleImageUtils.LOG.debug("Threshold: " + threshold);
        SimpleImageUtils.LOG.debug("Tolerance: " + tolerance);
        SimpleImageUtils.LOG.debug("Shave: " + shave);
        
        // special cases
        
        if (threshold <= 0) {
            SimpleImageUtils.LOG.debug("Text luminance threshold too low. Marking as non empty.");
            return false;
        } else if (threshold >= 1) {
            SimpleImageUtils.LOG.debug("Text luminance threshold too high. Marking as empty.");
            return true;
        }
        
        if (tolerance <= 0) {
            SimpleImageUtils.LOG.debug("Tolerance too low. Marking as not empty.");
            return false;
        } else if (tolerance >= 1) {
            SimpleImageUtils.LOG.debug("Tolerance too high. Marking as empty.");
            return true;
        }
        
        if (shave >= 1) {
            SimpleImageUtils.LOG.debug("Shave too high. Marking as empty.");
            return true;
        }
        
        // make the input image smaller for testing purposes
        
        BufferedImage testImage = SimpleImageUtils.makeThumbnailImage(inputImage, Settings.TARGET_IMAGE_WIDTH, false);
        
        // crop the image edges if necessary
        
        if (shave > 0) {
            testImage = SimpleImageUtils.cropEdges(testImage, shave);
        }
        
        // compute paper luminance and text luminance
        
        final double paperLuminance = SimpleImageUtils.getLuminance(SimpleImageUtils.getPaperColor(testImage));
        final double textLuminance = paperLuminance * threshold;
        
        SimpleImageUtils.LOG.debug("Paper luminance: " + paperLuminance);
        SimpleImageUtils.LOG.debug("Text luminance: " + textLuminance);
        
        // initialize count of dark pixels
        
        long countDark = 0;
        
        // compute total count of pixels
        
        final long countTotal = inputImage.getWidth() * inputImage.getHeight();
        
        // iterate pixels and count all dark ones
        
        for (int ix = 0; ix < inputImage.getWidth(); ix++) {
            for (int iy = 0; iy < inputImage.getHeight(); iy++) {
                final int r = inputImage.getRaster().getSample(ix, iy, 0);
                final int g = inputImage.getRaster().getSample(ix, iy, 1);
                final int b = inputImage.getRaster().getSample(ix, iy, 2);
                final double pixelLuminance = SimpleImageUtils.getLuminance(r, g, b);
                
                if (pixelLuminance < textLuminance) {
                    countDark++;
                }
            }
        }
        
        // compute percentile
        
        final double percentDark = (double) countDark / (double) countTotal;
        SimpleImageUtils.LOG.debug("Relative amount of dark: " + percentDark);
        return (percentDark <= tolerance);
    }
    
    /**
     * Returns the new image based on the given image by cropping edges. The
     * center of the image is preserved and edges are cut by the amount based on
     * the given parameter. For example, when called with parameter 0.2 (20%),
     * the size of the resulting image is 90%x90% of the original image (10% is
     * cropped from each side).
     * 
     * @param inputImage
     * input image
     * @param shave
     * percent to crop from each dimension
     * @return input image with edges cropped
     */
    private static BufferedImage cropEdges(final BufferedImage inputImage, final double shave) {
        final int nw = (int) (inputImage.getWidth() * shave);
        final int nh = (int) (inputImage.getHeight() * shave);
        
        return inputImage.getSubimage(
                nw / 2,
                nh / 2,
                inputImage.getWidth() - nw,
                inputImage.getHeight() - nh);
    }
    
    /**
     * Returns the paper color of an image. The color is computed as a median.
     * 
     * @param inputImage
     * input image
     * @return paper color (median)
     */
    private static Color getPaperColor(final BufferedImage inputImage) {
        final int length = inputImage.getWidth() * inputImage.getHeight();
        
        // allocate arrays
        
        final int[] rvalues = new int[length];
        final int[] gvalues = new int[length];
        final int[] bvalues = new int[length];
        
        // fill arrays with image colors
        
        int i = 0;
        
        for (int ix = 0; ix < inputImage.getWidth(); ix++) {
            for (int iy = 0; iy < inputImage.getHeight(); iy++) {
                rvalues[i] = inputImage.getRaster().getSample(ix, iy, 0);
                gvalues[i] = inputImage.getRaster().getSample(ix, iy, 1);
                bvalues[i] = inputImage.getRaster().getSample(ix, iy, 2);
                i++;
            }
        }
        
        // sort arrays
        
        Arrays.sort(rvalues);
        Arrays.sort(gvalues);
        Arrays.sort(bvalues);
        
        // get median values (located in the middle)
        
        final int rmedian = rvalues[rvalues.length / 2];
        final int gmedian = gvalues[gvalues.length / 2];
        final int bmedian = bvalues[bvalues.length / 2];
        
        // return the resulting color
        
        return new Color(rmedian, gmedian, bmedian);
    }
    
    // =======
    // UTILITY
    // =======
    
    /**
     * Converts an image to an array of bytes. These bytes represent the image
     * raster in the format specified.
     * 
     * @param image
     * source image
     * @param format
     * target format (e.g. jpg or png)
     * @return array of image raster bytes
     * @throws IOException
     * I/O exception
     */
    protected static byte[] toImageData(final RenderedImage image, final String format) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        
        try {
            ImageIO.write(image, format, out);
            return out.toByteArray();
        } finally {
            out.close();
        }
    }
    
    /**
     * Returns a luminance of a color.
     * 
     * @param color
     * color
     * @return luminance
     */
    private static double getLuminance(final Color color) {
        return SimpleImageUtils.getLuminance(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Returns a luminance of color components. The luminance is computed as a
     * weighted sum of components based on sensitivity of human eye.
     * 
     * @param r
     * red component (0-255)
     * @param g
     * green component (0-255)
     * @param b
     * blue component (0-255)
     * @return luminance
     */
    private static double getLuminance(final int r, final int g, final int b) {
        return 0.3 * r + 0.59 * g + 0.11 * b;
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleImageUtils() {
        throw new UnsupportedOperationException();
    }
}
