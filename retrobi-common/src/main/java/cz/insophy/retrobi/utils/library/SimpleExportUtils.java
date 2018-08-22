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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.jcouchdb.util.StringUtil;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.DataToExport;

/**
 * Utility class for exporting into ZIP and RTF.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleExportUtils {
    /**
     * RTF page width in pixels (used for image resizing)
     */
    private static final int RTF_PAGE_PIXEL_WIDTH = 450;
    /**
     * CR LF line end for use in RTF
     */
    private static final String RTF_LINE_END = "\r\n";
    /**
     * line end for use in ZIP (should be Windows compatible)
     */
    private static final String ZIP_LINE_END = "\r\n";
    
    // ========
    // SKELETON
    // ========
    
    /**
     * Writes the given card into the ZIP output stream provided.
     * 
     * @param target
     * target ZIP stream
     * @param options
     * data to export
     * @throws IOException
     * I/O exception during writing
     * @throws GeneralRepositoryException
     * general exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public static void writeCardAsZip(final ZipOutputStream target, final DataToExport options) throws IOException, GeneralRepositoryException, NotFoundRepositoryException {
        // ---------------
        // TEXTUAL VERSION
        // ---------------
        
        switch (options.getTextSource()) {
            case BEST:
                // best textual version
                SimpleExportUtils.writeZipText(
                        target,
                        SimpleExportUtils.zipEntryName(options.getCard(), "prepis.txt"),
                        SimpleSegmentUtils.getCardAsText(options.getCard(), SimpleExportUtils.ZIP_LINE_END));
                break;
            case OCR:
                // original OCR
                SimpleExportUtils.writeZipText(
                        target,
                        SimpleExportUtils.zipEntryName(options.getCard(), "prepis.txt"),
                        options.getCard().getOcr());
                break;
            case FIXED:
                // fixed OCR
                SimpleExportUtils.writeZipText(
                        target,
                        SimpleExportUtils.zipEntryName(options.getCard(), "prepis.txt"),
                        options.getCard().getOcrFixOrDefault());
                break;
            case NONE:
                // NOP
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
        
        // ------
        // IMAGES
        // ------
        
        for (final String imageName : options.getImageNames()) {
            switch (options.getImageQuality()) {
                case JPEG_HIGH:
                    // JPEG high
                    SimpleExportUtils.writeZipImage(
                            target,
                            SimpleExportUtils.zipEntryName(options.getCard(), imageName + ".jpg"),
                            SimpleExportUtils.getJpegImage(options.getCard(), imageName, true), "jpg");
                    break;
                case JPEG_LOW:
                    // JPEG low
                    SimpleExportUtils.writeZipImage(
                            target,
                            SimpleExportUtils.zipEntryName(options.getCard(), imageName) + ".jpg",
                            SimpleExportUtils.getJpegImage(options.getCard(), imageName, false), "jpg");
                    break;
                case PNG:
                    // PNG original
                    final InputStream stream = SimpleExportUtils.getPngImage(options.getCard(), imageName);
                    try {
                        SimpleExportUtils.writeZipImage(
                                target,
                                SimpleExportUtils.zipEntryName(options.getCard(), imageName) + ".png",
                                SimpleExportUtils.getPngImage(options.getCard(), imageName));
                    } finally {
                        stream.close();
                    }
                    break;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
        
        // ----------
        // ATTRIBUTES
        // ----------
        
        if ((options.getTree() != null) && !options.getTree().isEmpty()) {
            SimpleExportUtils.writeZipAttributes(
                    target,
                    SimpleExportUtils.zipEntryName(options.getCard(), "rozpis.txt"),
                    options.getTree());
        }
        
        // --------
        // COMMENTS
        // --------
        
        int commentNumber = 1;
        
        for (final Comment comment : options.getComments()) {
            SimpleExportUtils.writeZipComment(
                    target,
                    SimpleExportUtils.zipEntryName(options.getCard(), String.format("komentar_%d.txt", commentNumber++)),
                    comment);
        }
    }
    
    /**
     * Writes the given card into the stream provided as RTF.
     * 
     * @param target
     * target writer
     * @param options
     * data to export
     * @param number
     * card number
     * @param total
     * total card count
     * @param user
     * user who is exporting the cards
     * @throws IOException
     * I/O exception during writing
     * @throws GeneralRepositoryException
     * general exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public static void writeCardAsRtf(final Writer target, final DataToExport options, final int number, final int total, final User user) throws IOException, GeneralRepositoryException, NotFoundRepositoryException {
        // card header
        
        SimpleExportUtils.writeRtfCardHeader(target, options.getCard(), number, total, user);
        
        // ---------------
        // TEXTUAL VERSION
        // ---------------
        
        switch (options.getTextSource()) {
            case BEST:
                // best textual version
                SimpleExportUtils.writeRtfParagraph(target, SimpleSegmentUtils.getCardAsText(options.getCard()), false);
                SimpleExportUtils.writeRtfLineBreak(target);
                break;
            case OCR:
                // original OCR
                SimpleExportUtils.writeRtfParagraph(target, options.getCard().getOcr(), false);
                SimpleExportUtils.writeRtfLineBreak(target);
                break;
            case FIXED:
                // rewritten OCR
                SimpleExportUtils.writeRtfParagraph(target, options.getCard().getOcrFixOrDefault(), false);
                SimpleExportUtils.writeRtfLineBreak(target);
                break;
            case NONE:
                // NOP
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
        
        // ------
        // IMAGES
        // ------
        
        for (final String imageName : options.getImageNames()) {
            switch (options.getImageQuality()) {
                case JPEG_HIGH:
                    // JPEG high
                    SimpleExportUtils.writeRtfImage(target, SimpleExportUtils.getJpegImage(options.getCard(), imageName, true), "jpg");
                    break;
                case JPEG_LOW:
                    // JPEG low
                    SimpleExportUtils.writeRtfImage(target, SimpleExportUtils.getJpegImage(options.getCard(), imageName, false), "jpg");
                    break;
                case PNG:
                    // PNG original
                    final InputStream stream = SimpleExportUtils.getPngImage(options.getCard(), imageName);
                    try {
                        SimpleExportUtils.writeRtfImage(target, stream);
                    } finally {
                        stream.close();
                    }
                    break;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
        
        // ----------
        // ATTRIBUTES
        // ----------
        
        if ((options.getTree() != null) && !options.getTree().isEmpty()) {
            SimpleExportUtils.writeRtfAttributes(target, options.getTree());
        }
        
        // --------
        // COMMENTS
        // --------
        
        if (options.getComments().size() > 0) {
            SimpleExportUtils.writeRtfPageBreak(target);
            
            for (final Comment comment : options.getComments()) {
                SimpleExportUtils.writeRtfComment(target, comment);
            }
        }
        
        // page break
        
        if (number < total) {
            SimpleExportUtils.writeRtfPageBreak(target);
        }
        
        target.flush();
    }
    
    // ===========
    // ZIP UTILITY
    // ===========
    
    /**
     * Writes card attribute tree to ZIP.
     * 
     * @param target
     * target stream
     * @param name
     * entry name
     * @param node
     * attribute tree root
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeZipAttributes(final ZipOutputStream target, final String name, final AttributeNode node) throws IOException {
        final StringBuilder b = new StringBuilder(1024);
        SimpleExportUtils.writeZipAttributes(b, node, 0);
        SimpleExportUtils.writeZipText(target, name, b.toString());
    }
    
    /**
     * Writes card attribute tree to string buffer (as a plain text).
     * 
     * @param b
     * target buffer
     * @param node
     * attribute tree root
     * @param level
     * level (from 0 to N)
     */
    private static void writeZipAttributes(final StringBuilder b, final AttributeNode node, final int level) {
        final String indent = StringUtil.join(Collections.nCopies(level, "\t"), "");
        
        if (node instanceof AtomicAttributeNode) {
            // atomic attribute
            
            final AtomicAttributeNode an = (AtomicAttributeNode) node;
            
            if (!an.isEmpty()) {
                b.append(String.format("%s%s: %s%s", indent, an.getTitle(), an.getValue(), SimpleExportUtils.ZIP_LINE_END));
            }
        } else if (node instanceof ComposedAttributeNode) {
            // composed attribute
            
            final ComposedAttributeNode can = (ComposedAttributeNode) node;
            
            if (!can.isEmpty()) {
                b.append(String.format("%s[%s]%s", indent, can.getTitle(), SimpleExportUtils.ZIP_LINE_END));
                
                for (final AttributeNode child : can.getChildren()) {
                    SimpleExportUtils.writeZipAttributes(b, child, level + 1);
                }
            }
        }
    }
    
    /**
     * Writes comment to ZIP.
     * 
     * @param target
     * target stream
     * @param name
     * entry name
     * @param comment
     * comment to write
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeZipComment(final ZipOutputStream target, final String name, final Comment comment) throws IOException {
        final String contents = String.format("Komentář přidán %s:%s%s",
                comment.getAdded().toString(),
                SimpleExportUtils.ZIP_LINE_END + SimpleExportUtils.ZIP_LINE_END,
                comment.getText());
        
        SimpleExportUtils.writeZipText(target, name, contents);
    }
    
    /**
     * Writes general text to ZIP.
     * 
     * @param target
     * target stream
     * @param name
     * entry name
     * @param contents
     * text contents
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeZipText(final ZipOutputStream target, final String name, final String contents) throws IOException {
        target.putNextEntry(new ZipEntry(name));
        target.write(contents.getBytes("UTF-8"));
        target.closeEntry();
    }
    
    /**
     * Writes an image to ZIP.
     * 
     * @param target
     * target stream
     * @param name
     * entry name
     * @param image
     * image to write
     * @param format
     * image format (jpg, png)
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeZipImage(final ZipOutputStream target, final String name, final BufferedImage image, final String format) throws IOException {
        target.putNextEntry(new ZipEntry(name));
        ImageIO.write(image, format, target);
        target.closeEntry();
    }
    
    /**
     * Writes an image to ZIP.
     * 
     * @param target
     * target stream
     * @param name
     * entry name
     * @param stream
     * input image stream
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeZipImage(final ZipOutputStream target, final String name, final InputStream stream) throws IOException {
        target.putNextEntry(new ZipEntry(name));
        IOUtils.copy(stream, target);
        target.closeEntry();
    }
    
    /**
     * Generates a valid ZIP archive entry name for the given card.
     * 
     * @param card
     * card
     * @param suffix
     * suffix to add (must be valid ZIP entry name!)
     * @return valid ZIP entry name
     */
    private static String zipEntryName(final Card card, final String suffix) {
        return String.format(
                "%s_%s_%05d_%s",
                SimpleStringUtils.normalizeToAscii(card.getCatalog().name()),
                SimpleStringUtils.normalizeToAscii(card.getBatch()),
                card.getNumberInBatch(),
                suffix);
    }
    
    // ===========
    // RTF UTILITY
    // ===========
    
    /**
     * Writes a RTF header.
     * 
     * @param target
     * target writer
     * @throws IOException
     * I/O exception during writing
     */
    public static void writeRtfHeader(final Writer target) throws IOException {
        target.write("{\\rtf1\\ansi\\deff0\\paperw11906\\paperh16838\\margl500\\margr500\\margt500\\margb500");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        target.write("{\\fonttbl{\\f0\\fnil Arial;}}");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        target.write("{\\footer\\fs20\\i");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        SimpleExportUtils.writeRtfEncodedString(target, "Ústav pro českou literaturu AV ČR, v. v. i.");
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfEncodedString(target, "Retrospektivní bibliografie české literární vědy");
        SimpleExportUtils.writeRtfLineBreak(target);
        target.write("{\\qr Strana {\\field{\\fldinst{page}}} z {\\field{\\fldinst{numpages}}}}");
        target.write("}");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        target.write("\\fs24");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
    }
    
    /**
     * Writes RTF footer.
     * 
     * @param target
     * target writer
     * @throws IOException
     * I/O exception during writing
     */
    public static void writeRtfFooter(final Writer target) throws IOException {
        target.write("}");
    }
    
    /**
     * Writes card header to RTF.
     * 
     * @param target
     * target writer
     * @param card
     * card to write
     * @param number
     * card number
     * @param total
     * total card count
     * @param user
     * user who is exporting the card
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfCardHeader(final Writer target, final Card card, final int number, final int total, final User user) throws IOException {
        target.write("{\\fs20\\i");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        SimpleExportUtils.writeRtfEncodedString(target, String.format(
                "Lístek %d z %d",
                number,
                total));
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfEncodedString(target, String.format(
                "Část: %s, Skupina: %s, Pořadí: %d",
                card.getCatalog().toString(),
                card.getBatch(),
                card.getNumberInBatch()));
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfEncodedString(target, String.format(
                "Datum: %s, Uživatel: %s",
                Time.now().toString(),
                (user == null) ? "-" : user.getEmail()));
        target.write("}");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        SimpleExportUtils.writeRtfLineBreak(target);
    }
    
    /**
     * Writes a original PNG image to RTF.
     * 
     * @param target
     * target writer
     * @param imageStream
     * input image stream
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfImage(final Writer target, final InputStream imageStream) throws IOException {
        SimpleExportUtils.writeRtfCardImage(target, "pngblip", Settings.TARGET_IMAGE_WIDTH, imageStream);
    }
    
    /**
     * Writes an image to RTF.
     * 
     * @param target
     * target writer
     * @param image
     * image to write
     * @param format
     * image format (png, jpg)
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfImage(final Writer target, final BufferedImage image, final String format) throws IOException {
        // check format and create blip
        
        final String blip;
        
        if (format.equals("png")) {
            blip = "pngblip";
        } else if (format.equals("jpg")) {
            blip = "jpegblip";
        } else {
            throw new IllegalArgumentException("Neplatný formát obrázku: " + format);
        }
        
        // dump image data to byte buffer
        
        final byte[] buffer = SimpleImageUtils.toImageData(image, format);
        
        // create reader of this buffer
        
        final InputStream stream = new ByteArrayInputStream(buffer);
        
        try {
            // write image
            
            SimpleExportUtils.writeRtfCardImage(target, blip, image.getWidth(), stream);
        } finally {
            // do not forget to close the stream
            
            stream.close();
        }
    }
    
    /**
     * Writes the card image.
     * 
     * @param target
     * target writer
     * @param blip
     * blip (pngblip or jpegblip)
     * @param width
     * image width
     * @param imageStream
     * image input stream (will not be closed)
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfCardImage(final Writer target, final String blip, final int width, final InputStream imageStream) throws IOException {
        final double ratio = (double) SimpleExportUtils.RTF_PAGE_PIXEL_WIDTH / (double) width;
        final int scale = (int) (100.0 * ratio);
        int counter = 0;
        int data = imageStream.read();
        
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        target.write(String.format("{\\par\\qc{\\pict\\picscalex%d\\picscaley%d\\%s", scale, scale, blip));
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        
        while (data != -1) {
            final String hexdata = Integer.toHexString(data);
            
            if (hexdata.length() == 1) {
                target.write('0' + hexdata);
            } else {
                target.write(hexdata);
            }
            
            data = imageStream.read();
            counter++;
            
            if (counter == 60) {
                SimpleExportUtils.writeRtfInternalLineEnd(target);
                counter = 0;
            }
        }
        
        target.write("}\\pard}\\line");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
    }
    
    /**
     * Writes comment to RTF.
     * 
     * @param target
     * target writer
     * @param comment
     * comment to write
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfComment(final Writer target, final Comment comment) throws IOException {
        SimpleExportUtils.writeRtfParagraph(target, "Komentář", true);
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfParagraph(target, String.format("Datum: %s", comment.getAdded().toString()), false);
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfParagraph(target, comment.getText(), false);
        SimpleExportUtils.writeRtfLineBreak(target);
        SimpleExportUtils.writeRtfLineBreak(target);
    }
    
    /**
     * Writes card attribute tree to RTF.
     * 
     * @param target
     * target writer
     * @param tree
     * card attribute tree root
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfAttributes(final Writer target, final AttributeNode tree) throws IOException {
        SimpleExportUtils.writeRtfAttributeRecursive(target, tree, 0);
    }
    
    /**
     * Writes card attribute tree to RTF.
     * 
     * @param target
     * target writer
     * @param subtree
     * subtree root
     * @param level
     * level (from 0 to N)
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfAttributeRecursive(final Writer target, final AttributeNode subtree, final int level) throws IOException {
        if (subtree instanceof AtomicAttributeNode) {
            // atomic node
            
            final AtomicAttributeNode an = (AtomicAttributeNode) subtree;
            
            if (!an.isEmpty()) {
                SimpleExportUtils.writeRtfIndentedParagraph(target, level, an.getTitle() + ": " + an.getValue(), false);
            }
        } else if (subtree instanceof ComposedAttributeNode) {
            // composed node
            
            final ComposedAttributeNode can = (ComposedAttributeNode) subtree;
            
            if (!can.isEmpty()) {
                SimpleExportUtils.writeRtfIndentedParagraph(target, level, can.getTitle(), true);
                
                for (final AttributeNode child : can.getChildren()) {
                    SimpleExportUtils.writeRtfAttributeRecursive(target, child, level + 1);
                }
            }
        }
    }
    
    /**
     * Writes a text paragraph.
     * 
     * @param target
     * target writer
     * @param string
     * string to write
     * @param bold
     * make paragraph bold
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfParagraph(final Writer target, final String string, final boolean bold) throws IOException {
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        target.write("{\\par\\pard");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        
        if (bold) {
            target.write("{\\b");
            SimpleExportUtils.writeRtfInternalLineEnd(target);
            SimpleExportUtils.writeRtfEncodedString(target, string, true);
            target.write("}");
        } else {
            SimpleExportUtils.writeRtfEncodedString(target, string, true);
        }
        
        target.write("}");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
    }
    
    /**
     * Writes an indented text paragraph.
     * 
     * @param target
     * target writer
     * @param level
     * indent level (from 0 to N)
     * @param string
     * string to write
     * @param bold
     * make paragraph bold
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfIndentedParagraph(final Writer target, final int level, final String string, final boolean bold) throws IOException {
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        target.write(String.format("{\\par\\pard \\li%d\\bullet", 400 * level));
        SimpleExportUtils.writeRtfInternalLineEnd(target);
        
        if (bold) {
            target.write("{\\b");
            SimpleExportUtils.writeRtfInternalLineEnd(target);
            SimpleExportUtils.writeRtfEncodedString(target, string, true);
            target.write("}");
        } else {
            SimpleExportUtils.writeRtfEncodedString(target, string, true);
        }
        
        target.write("}");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
    }
    
    /**
     * Writes a page break.
     * 
     * @param target
     * target writer
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfPageBreak(final Writer target) throws IOException {
        target.write("\\page");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
    }
    
    /**
     * Writes a page break.
     * 
     * @param target
     * target writer
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfLineBreak(final Writer target) throws IOException {
        target.write("\\line");
        SimpleExportUtils.writeRtfInternalLineEnd(target);
    }
    
    /**
     * Writes a RTF line end. It is not the same as line break, because line end
     * does not cause text to be wrapped. It is just a way to shorten line end
     * in the resulting RTF code.
     * 
     * @param target
     * target write
     * @throws IOException
     * I/O exception
     */
    private static void writeRtfInternalLineEnd(final Writer target) throws IOException {
        target.write(SimpleExportUtils.RTF_LINE_END);
    }
    
    /**
     * Writes a string encoded.
     * 
     * @param target
     * target writer
     * @param string
     * string to be encoded and written
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfEncodedString(final Writer target, final String string) throws IOException {
        SimpleExportUtils.writeRtfEncodedString(target, string, false);
    }
    
    /**
     * Writes an encoded string. Supports UTF-8 characters and preserves all
     * possible line ends if wanted to (parameter switch). Following control
     * characters are encoded: <code>{</code>, <code>}</code>, <code>~</code>,
     * <code>\</code>.
     * 
     * @param target
     * target writer
     * @param string
     * string to be encoded and written
     * @param preserveNewLines
     * preserve new lines = system line end will be converted to
     * <code>\line</code> RTF macro
     * @throws IOException
     * I/O exception during writing
     */
    private static void writeRtfEncodedString(final Writer target, final String string, final boolean preserveNewLines) throws IOException {
        final String string2 = preserveNewLines
                ? SimpleStringUtils.fixNewLines(string, "\n")
                : SimpleStringUtils.fixWhitespace(string);
        
        for (int i = 0; i < string2.length(); i++) {
            final int cp = string2.codePointAt(i);
            
            if (cp > 127) {
                // unicode character
                target.write("\\u" + cp + "?");
            } else {
                // ASCII character
                if (string2.charAt(i) == '\n') {
                    SimpleExportUtils.writeRtfLineBreak(target);
                } else {
                    if (cp == '{') {
                        target.write("\\{");
                    } else if (cp == '}') {
                        target.write("\\}");
                    } else if (cp == '\\') {
                        target.write("\\\\");
                    } else {
                        target.write(cp);
                    }
                }
            }
        }
    }
    
    // =======
    // UTILITY
    // =======
    
    /**
     * Returns a card image as JPEG.
     * 
     * @param card
     * card
     * @param imageName
     * image name
     * @param highQuality
     * use high quality JPEG (bigger)
     * @return card image as JPEG
     * @throws GeneralRepositoryException
     * general exception
     */
    private static BufferedImage getJpegImage(final Card card, final String imageName, final boolean highQuality) throws GeneralRepositoryException {
        if (highQuality) {
            return RetrobiApplication.db().getCardImageRepository().getCardImage(card.getId(), imageName);
        }
        
        final BufferedImage image = RetrobiApplication.db().getCardImageRepository().getCardImage(card.getId(), imageName);
        return SimpleImageUtils.makeThumbnailImage(image, Settings.PREVIEW_IMAGE_WIDTH, false);
    }
    
    /**
     * Returns a card image as PNG in a stream. Please close the stream.
     * 
     * @param card
     * card
     * @param imageName
     * image name
     * @return card image as PNG (in a stream)
     * @throws GeneralRepositoryException
     * general exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    private static InputStream getPngImage(final Card card, final String imageName) throws GeneralRepositoryException, NotFoundRepositoryException {
        return RetrobiApplication.db().getCardImageRepository().getCardImageResponse(card.getId(), imageName);
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleExportUtils() {
        throw new UnsupportedOperationException();
    }
}
