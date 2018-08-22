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

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;

/**
 * Segment utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleSegmentUtils {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleSearchUtils.class);
    
    /**
     * Shortcut method.
     * 
     * @see SimpleSegmentUtils#getCardAsText(Card, String)
     * @param card
     * input card
     * @return textual representation
     */
    public static String getCardAsText(final Card card) {
        return SimpleSegmentUtils.getCardAsText(card, Settings.LINE_END + Settings.LINE_END);
    }
    
    /**
     * Shortcut method.
     * 
     * @see SimpleSegmentUtils#getCardAsText(Card, String)
     * @param card
     * input card
     * @return textual representation
     */
    public static String getCardAsTextForLabel(final Card card) {
        if (!SimpleSegmentUtils.isSegmentationEmpty(card)) {
            // custom conversion from segmentation to text
            
            SimpleSegmentUtils.LOG.debug(String.format("Using segments as a label text value of '%s'...", card.getId()));
            
            final StringBuilder sb = new StringBuilder();
            
            if (card.getSegmentHead() != null) {
                sb.append(card.getSegmentHead().toUpperCase());
            }
            
            if (card.getSegmentTitle() != null) {
                sb.append(Settings.LINE_END);
                sb.append(card.getSegmentTitle());
            }
            
            if (card.getSegmentBibliography() != null) {
                sb.append(Settings.LINE_END);
                sb.append(card.getSegmentBibliography());
            }
            
            if (card.getSegmentAnnotation() != null) {
                sb.append(Settings.LINE_END);
                sb.append(card.getSegmentAnnotation());
            }
            
            if (card.getSegmentExcerpter() != null) {
                sb.append(Settings.LINE_END);
                sb.append(card.getSegmentExcerpter());
            }
            
            return sb.toString().trim();
        }
        
        return SimpleSegmentUtils.getCardAsText(card, Settings.LINE_END);
    }
    
    /**
     * Returns the best textual representation possible of the given card. The
     * priority of the information is as follows:
     * <ol>
     * <li>segmentation</li>
     * <li>fixed OCR</li>
     * <li>original OCR</li>
     * <li>basic card attributes (catalog, batch, number)</li>
     * </ol>
     * 
     * @param card
     * input card
     * @param delimiter
     * new line delimiter (for segmentation)
     * @return textual representation
     */
    protected static String getCardAsText(final Card card, final String delimiter) {
        if (!SimpleSegmentUtils.isSegmentationEmpty(card)) {
            // use the segmentation
            
            SimpleSegmentUtils.LOG.debug(String.format("Using segments as a text value of '%s'...", card.getId()));
            return SimpleSegmentUtils.segmentsToString(card, delimiter);
        } else if (!SimpleStringUtils.isEmpty(card.getOcrFix())) {
            // use the fixed OCR
            
            SimpleSegmentUtils.LOG.debug(String.format("Using fixed OCR as a text value of '%s'...", card.getId()));
            return card.getOcrFix();
        } else if (!SimpleStringUtils.isEmpty(card.getOcr())) {
            // use the original OCR
            
            SimpleSegmentUtils.LOG.debug(String.format("Using OCR as a text value of '%s'...", card.getId()));
            return card.getOcr();
        } else {
            // use the empty string (no better information found)
            
            SimpleSegmentUtils.LOG.debug(String.format("Using a fallback text value of '%s'...", card.getId()));
            return card.toString() + " (bez přepisu)";
        }
    }
    
    /**
     * Checks if the given string should by segmented. That means the string
     * contains at least one segmentation character.
     * 
     * @param str
     * string to be checked
     * @return <code>true</code> if the string should be segmented (at least one
     * segmentation character), <code>false</code> otherwise
     */
    public static boolean shouldBeSegmented(final String str) {
        return str.contains(Settings.SYMBOL_SEGMENT);
    }
    
    /**
     * Runs the segmentation algorithm described by ÚČL and puts the resulting
     * segments to the provided target card. If the segmentation is successful,
     * removes the segmenting characters from the fixed OCR. If not, the
     * segments are reset to the initial <code>null</code> values. Warning: this
     * method changes the segments and fixed OCR in target card.
     * 
     * @param segmentator
     * segmentation character (dividing the segments)
     * @param targetCard
     * target card
     * @return <code>true</code> if the segmentation was done,
     * <code>false</code> otherwise
     */
    public static boolean segment(final String segmentator, final Card targetCard) {
        SimpleSegmentUtils.LOG.debug(String.format("Segmenting OCR '%s' using '%s' as segmentator.", targetCard.getOcrFix(), segmentator));
        
        // we limit the splitter to 5 + 1 parts, because
        // * 5 is the maximal allowed count of segments (4 seg. symbols at most)
        // * +1 to be able to detect 5 seg. symbols or more, which is wrong
        
        final String[] data = targetCard.getOcrFix().split(Pattern.quote(segmentator), 5 + 1);
        
        if ((data.length != 4) && (data.length != 5)) {
            SimpleSegmentUtils.LOG.debug("Invalid segment count: " + data.length);
            return false;
        }
        
        for (int i = 0; i < data.length; i++) {
            // strip whitespace
            
            data[i] = SimpleStringUtils.fixWhitespace(data[i]);
            SimpleSegmentUtils.LOG.debug(String.format("%d: %s", i, data[i]));
        }
        
        // there are two possible flows:
        // 1) head | title | bibliography | annotation | excerpter
        // 2) head | annotation | title | bibliography | excerpter
        
        final String head = data[0].trim();
        final String excerpter = data.length == 5 ? data[4].trim() : "";
        
        SimpleSegmentUtils.LOG.debug("Extracted head: " + head);
        SimpleSegmentUtils.LOG.debug("Extracted excerpter: " + excerpter);
        
        final String title1 = SimpleSegmentUtils.segmentReadTitle(data[1].trim());
        final String bibliography1 = SimpleSegmentUtils.segmentReadBibliography(data[2].trim());
        final String annotation1 = SimpleSegmentUtils.segmentReadAnnotation(data[3].trim());
        
        if ((title1 != null) && (bibliography1 != null) && (annotation1 != null)) {
            SimpleSegmentUtils.LOG.debug("First flow detected.");
            SimpleSegmentUtils.segmentFinalize(targetCard, head, title1, bibliography1, annotation1, excerpter);
            return true;
        }
        
        final String annotation2 = SimpleSegmentUtils.segmentReadAnnotation(data[1].trim());
        final String title2 = SimpleSegmentUtils.segmentReadTitle(data[2].trim());
        final String bibliography2 = SimpleSegmentUtils.segmentReadBibliography(data[3].trim());
        
        if ((annotation2 != null) && (title2 != null) && (bibliography2 != null)) {
            SimpleSegmentUtils.LOG.debug("Second flow detected.");
            SimpleSegmentUtils.segmentFinalize(targetCard, head, title2, bibliography2, annotation2, excerpter);
            return true;
        }
        
        SimpleSegmentUtils.LOG.debug("No flow detected.");
        SimpleSegmentUtils.segmentAssign(targetCard, null, null, null, null, null);
        return false;
    }
    
    /**
     * Reads the annotation segment. If the format (see the method body) is
     * valid, returns the input string. If the format is invalid, returns
     * <code>null</code>. The annotation can be empty.
     * 
     * @param str
     * input string (trimmed)
     * @return the input string (if the format is valid) or <code>null</code>
     */
    public static String segmentReadAnnotation(final String str) {
        if (str.length() < 1) {
            return "";
        }
        
        // body: first 4 character must not contain '='
        final String pbody = "(([^=]{4}.*)|([^=]{1,4}))";
        // round bracket
        final String p1 = "\\(" + pbody + "\\)";
        // square bracket
        final String p2 = "\\[" + pbody + "\\]";
        // divide operator
        final String p3 = "/" + pbody + "/";
        // plain body, no RF on the start
        final String p4 = "(^(?!(Rf|Ref)(\\.|:| )))" + pbody;
        
        if (str.matches(String.format("^((%s)|(%s)|(%s)|(%s))$", p1, p2, p3, p4))) {
            return str;
        }
        
        SimpleSegmentUtils.LOG.debug("Invalid annotation: " + str);
        return null;
    }
    
    /**
     * Reads the bibliographic segment. If the format (see the method body) is
     * valid, returns the input string. If the format is invalid, returns
     * <code>null</code>.
     * 
     * @param str
     * input string (trimmed)
     * @return the input string (if the format is valid) or <code>null</code>
     */
    public static String segmentReadBibliography(final String str) {
        // body: the first 4 body characters must contain '='
        final String pbody = ".{0,3}=.*";
        // round bracket
        final String p1 = "\\(" + pbody + "\\)";
        // square bracket
        final String p2 = "\\[" + pbody + "\\]";
        // divide operator
        final String p3 = "/" + pbody + "/";
        
        if (str.matches(String.format("(%s)|(%s)|(%s)", p1, p2, p3))) {
            return str;
        }
        
        SimpleSegmentUtils.LOG.debug("Invalid bibliography: " + str);
        return null;
    }
    
    /**
     * Reads the title segment from the given string. If the format (see the
     * method body) is valid, returns the input string. If the format is
     * invalid, returns <code>null</code>.
     * 
     * @param str
     * input string (trimmed)
     * @return the input string (if the format is valid) or <code>null</code>
     */
    public static String segmentReadTitle(final String str) {
        // first character must not be '(' or '[' or '/'
        final String pbody = "[^\\(\\[/].*";
        // starting keyword
        final String p = "(Rf|Ref)(\\.|:)" + pbody;
        
        if (str.matches(String.format("(%s)|(%s)", pbody, p))) {
            return str;
        }
        
        SimpleSegmentUtils.LOG.debug("Invalid title: " + str);
        return null;
    }
    
    /**
     * Creates the final segments and assigns them to the card.
     * 
     * @param targetCard
     * target card
     * @param h
     * header segment (or <code>null</code>)
     * @param t
     * title segment (or <code>null</code>)
     * @param b
     * bibliography segment (or <code>null</code>)
     * @param a
     * annotation segment (or <code>null</code>)
     * @param e
     * excerpter segment (or <code>null</code>)
     */
    private static void segmentFinalize(final Card targetCard, final String h, final String t, final String b, final String a, final String e) {
        SimpleSegmentUtils.segmentAssign(
                targetCard,
                SimpleStringUtils.fixTypographicSpaces(h),
                SimpleStringUtils.fixTypographicSpaces(SimpleSegmentUtils.finalizeTitle(t)),
                SimpleStringUtils.fixTypographicSpaces(SimpleSegmentUtils.finalizeBibliography(b)),
                SimpleStringUtils.fixTypographicSpaces(SimpleSegmentUtils.finalizeAnnotation(a)),
                SimpleStringUtils.fixTypographicSpaces(e));
    }
    
    /**
     * Finalizes the title segment (must be valid!).
     * 
     * @param segment
     * the valid segment content
     * @return the finalized segment
     */
    public static String finalizeTitle(final String segment) {
        final String[] vars = new String[] {
                "Rf:", "Ref:",
                "Rf.", "Ref.",
                "Rf ", "Ref "
        };
        
        for (final String var : vars) {
            if (segment.startsWith(var)) {
                return segment.substring(var.length()).trim() + " [Referát]";
            }
        }
        
        return segment;
    }
    
    /**
     * Finalizes the bibliography segment (must be valid!).
     * 
     * @param segment
     * the valid segment content
     * @return the finalized segment
     */
    public static String finalizeBibliography(final String segment) {
        final String text = segment.substring(
                segment.indexOf('=') + 1,
                segment.length() - 1);
        
        String trimmedText = text.trim();
        
        if (!trimmedText.endsWith(".")) {
            trimmedText = trimmedText + ".";
        }
        
        return "In: " + trimmedText;
    }
    
    /**
     * Finalizes the annotation segment (must be valid!).
     * 
     * @param segment
     * the valid segment content
     * @return the finalized segment
     */
    public static String finalizeAnnotation(final String segment) {
        if (segment.length() < 1) {
            return "";
        }
        
        String result = segment;
        
        if (!result.startsWith("[")) {
            if (result.startsWith("(") || result.startsWith("/")) {
                // do not close twice - start
                result = result.substring(1);
            }
            
            result = "[" + result;
        }
        
        if (!result.endsWith("]")) {
            if (result.endsWith(")") || result.endsWith("/")) {
                // do not close twice - end
                result = result.substring(0, result.length() - 1);
            }
            
            result = result + "]";
        }
        
        return result;
    }
    
    /**
     * @see SimpleSegmentUtils#segmentsToStringExcerpterRight(Card, String, int)
     * @param card
     * a source card
     * @param delimiter
     * a delimiter
     * @return output string with all the card segments divided by a delimiter
     */
    public static String segmentsToString(final Card card, final String delimiter) {
        return SimpleSegmentUtils.segmentsToStringExcerpterRight(card, delimiter, 0);
    }
    
    /**
     * Converts all card segments to a string. Can be used for logging, etc. The
     * segments are split by a delimiter provided. The <code>null</code> value
     * is replaced by "-" symbol. The excerpter is aligned right (if possible).
     * 
     * @param card
     * a source card
     * @param delimiter
     * a delimiter
     * @param width
     * row width (or 0, used for aligning excerpter)
     * @return output string with all the card segments divided by a delimiter
     */
    public static String segmentsToStringExcerpterRight(final Card card, final String delimiter, final int width) {
        return (card.getSegmentHead() == null ? "-" : card.getSegmentHead().toUpperCase()) + delimiter +
                (card.getSegmentTitle() == null ? "-" : card.getSegmentTitle()) + delimiter +
                (card.getSegmentBibliography() == null ? "-" : card.getSegmentBibliography()) + delimiter +
                (card.getSegmentAnnotation() == null ? "-" : card.getSegmentAnnotation()) + delimiter + delimiter +
                (card.getSegmentExcerpter() == null ? "-" : SimpleStringUtils.alignRightIfPossible(card.getSegmentExcerpter(), width));
    }
    
    /**
     * Check if the segmentation is empty. Currently the same as if the
     * bibliographic segment is empty.
     * 
     * @param card
     * card to evaluate
     * @return <code>true</code> if the segmentation is empty,
     * <code>false</code> otherwise
     */
    public static boolean isSegmentationEmpty(final Card card) {
        return SimpleStringUtils.isEmpty(card.getSegmentBibliography());
    }
    
    /**
     * Sets the segment information into the given card. This is just a
     * convenience method for doing it manually. Each parameter can be
     * <code>null</code> (that means: not set).
     * 
     * @param targetCard
     * target card
     * @param h
     * header segment (or <code>null</code>)
     * @param t
     * title segment (or <code>null</code>)
     * @param b
     * bibliography segment (or <code>null</code>)
     * @param a
     * annotation segment (or <code>null</code>)
     * @param e
     * excerpter segment (or <code>null</code>)
     */
    private static void segmentAssign(final Card targetCard, final String h, final String t, final String b, final String a, final String e) {
        SimpleSegmentUtils.LOG.debug("Setting head segment: " + h);
        targetCard.setSegmentHead(h);
        SimpleSegmentUtils.LOG.debug("Setting title segment: " + t);
        targetCard.setSegmentTitle(t);
        SimpleSegmentUtils.LOG.debug("Setting bibliography segment: " + b);
        targetCard.setSegmentBibliography(b);
        SimpleSegmentUtils.LOG.debug("Setting annotation segment: " + a);
        targetCard.setSegmentAnnotation(a);
        SimpleSegmentUtils.LOG.debug("Setting excerpter segment: " + e);
        targetCard.setSegmentExcerpter(e);
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleSegmentUtils() {
        throw new UnsupportedOperationException();
    }
}
