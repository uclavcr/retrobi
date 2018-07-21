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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.IllegalSelectorException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.jcouchdb.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONParser;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.utils.CzechAlphabet;

/**
 * String utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleStringUtils {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleStringUtils.class);
    /**
     * base64 alphabet
     */
    private static final String BASE64_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    /**
     * random string alphabet
     */
    private static final String BASIC_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    /**
     * allowed symbols (e.g. in a fixed OCR)
     */
    private static final String ALLOWED_SYMBOLS = "" +
            "()[]{}" +
            ",.:;-?!" +
            "ʘ|" +
            "'\"" +
            "\\/*-+=°^#&@$%" +
            "<>«»" +
            " \n";
    
    // ===================
    // ENCODING / DECODING
    // ===================
    
    /**
     * Returns the string encoded to base64. The code is based on blog post on
     * <code>http://www.wikihow.com/Encode-a-String-to-Base64-With-Java</code>.
     * If the input is <code>null</code>, returns <code>null</code> too.
     * 
     * @param str
     * input string
     * @return input string converted to base64
     */
    public static String getBase64(final String str) {
        if (str == null) {
            return null;
        }
        
        String encoded = "";
        byte[] stringArray;
        
        // get properly encoded bytes
        
        try {
            stringArray = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException x) {
            throw new IllegalSelectorException();
        }
        
        // determine how many padding bytes to add to the output
        
        final int paddingCount = (3 - (stringArray.length % 3)) % 3;
        
        // add any necessary padding to the input
        
        stringArray = SimpleStringUtils.zeroPad(stringArray.length + paddingCount, stringArray);
        
        // process 3 bytes at a time, churning out 4 output bytes
        
        for (int i = 0; i < stringArray.length; i += 3) {
            final int j = ((stringArray[i] & 0xff) << 16) + ((stringArray[i + 1] & 0xff) << 8) + (stringArray[i + 2] & 0xff);
            encoded = encoded
                    + SimpleStringUtils.BASE64_ALPHA.charAt((j >> 18) & 0x3f)
                    + SimpleStringUtils.BASE64_ALPHA.charAt((j >> 12) & 0x3f)
                    + SimpleStringUtils.BASE64_ALPHA.charAt((j >> 6) & 0x3f)
                    + SimpleStringUtils.BASE64_ALPHA.charAt(j & 0x3f);
        }
        
        // replace encoded padding nulls with "="
        
        return encoded.substring(0, encoded.length() - paddingCount) + "==".substring(0, paddingCount);
    }
    
    /**
     * Returns the hash of the given string. If the input is <code>null</code>,
     * returns <code>null</code> too.
     * 
     * @param str
     * input string to compute hash
     * @return hash of the given string
     */
    public static String getHash(final String str) {
        if (str == null) {
            return null;
        }
        
        try {
            final MessageDigest encoder = MessageDigest.getInstance("SHA-1");
            final byte[] bytesOfMessage = str.getBytes("utf-8");
            final byte[] theDigest = encoder.digest(bytesOfMessage);
            return SimpleStringUtils.getBase64(new String(theDigest, "utf-8"));
        } catch (final Exception x) {
            throw new IllegalStateException(x);
        }
    }
    
    /**
     * Encodes a value to use in URL. If the input is <code>null</code>, returns
     * <code>null</code> too.
     * 
     * @param str
     * input string value to encode
     * @return encoded value safe to use in the URL
     */
    public static String encodeForUrl(final String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 1) {
            return "";
        }
        
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (final UnsupportedEncodingException x) {
            throw new IllegalStateException(x);
        }
    }
    
    /**
     * Decodes a value from URL to use in the system. If the input is
     * <code>null</code>, returns <code>null</code> too.
     * 
     * @param str
     * encoded value from URL
     * @return decoded value safe to use in the system
     */
    public static String decodeFromUrl(final String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 1) {
            return "";
        }
        
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (final Exception x) {
            throw new IllegalStateException(x);
        }
    }
    
    /**
     * Translates the input string to ASCII so it can be used as a filename, URL
     * address, etc. The current version just replaces spaces to underscores and
     * removes everything except letters, numbers and following characters:
     * 
     * <pre>
     * - _ = ( ) [ ] ~
     * </pre>
     * 
     * @param str
     * input string with possible accents
     * @return input string translated to ASCII
     */
    public static String normalizeToAscii(final String str) {
        final StringBuilder builder = new StringBuilder(str.length());
        
        final String acc1 = " _-=()[]~áäčćďéěëłĺíµňôóöŕřšśťúůüýžÁÄČĆĎÉĚËŁĹÍĄŇÓÖÔŘŔŠŚŤÚŮÜÝŽ";
        final String acc0 = "__-=()[]~aaccdeeeLlilnooorrsstuuuyzAACCDEEELLIANOOORRSSTUUUYZ";
        
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            
            if ((c >= 48) && (c <= 57)) {
                // numbers
                
                builder.append(c);
            } else if ((c >= 65) && (c <= 90)) {
                // upper case letters
                
                builder.append(c);
            } else if ((c >= 97) && (c <= 122)) {
                // lower case letters
                
                builder.append(c);
            } else {
                final int index = acc1.indexOf(c);
                
                if (index != -1) {
                    // add the character without the accent
                    
                    builder.append(acc0.charAt(index));
                } else {
                    // the rest will not be added at all
                }
            }
        }
        
        return builder.toString();
    }
    
    // ============
    // WEB ORIENTED
    // ============
    
    /**
     * Returns a random string of the given length. The resulting string is
     * composed of alphanumeric characters only.
     * 
     * @param length
     * desired string length
     * @return a random alphanumeric string
     */
    public static String getRandomString(final int length) {
        final StringBuilder b = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            final int r = (int) (Math.random() * SimpleStringUtils.BASIC_ALPHA.length());
            b.append(SimpleStringUtils.BASIC_ALPHA.charAt(r));
        }
        
        return b.toString().trim();
    }
    
    /**
     * Checks if the query is valid and not "dangerous" for the system. The
     * rules are basically the same for all roles:
     * <ul>
     * <li>query cannot be <code>null</code> or empty</li>
     * <li>query must contain at least one character other than asterisk (
     * <code>*</code>), question mark (<code>?</code>), ampersand (
     * <code>&amp;</code>) and vertical line (<code>|</code>)</li>
     * </ul>
     * 
     * @param query
     * the query
     * @param userRole
     * user role
     * @return <code>true</code> if the query is valid and not dangerous,
     * <code>false</code> otherwise
     */
    public static boolean isAcceptableQuery(final String query, final UserRole userRole) {
        if ((query == null) || (query.trim().length() < 1)) {
            // query is empty
            return false;
        }
        
        String temp = query;
        temp = temp.replace('*', ' ');
        temp = temp.replace('?', ' ');
        temp = temp.replace('&', ' ');
        temp = temp.replace('|', ' ');
        temp = temp.trim();
        
        if (temp.length() < 1) {
            // query does not contain any reasonable characters
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns the input string with all newlines converted to HTML line breaks
     * (&lt;br&gt; tags). The method is platform independent, it handles
     * newlines from MacOS (CR), Linux (LF) and Windows (CRLF).
     * 
     * @param str
     * source string with newlines
     * @return source string with newlines converted to HTML line breaks
     */
    public static String nl2br(final String str) {
        return SimpleStringUtils.fixNewLines(str, "<br />");
    }
    
    /**
     * Fixes all newlines (CR, CR LF and LF) by replacing them by the given
     * replacement string.
     * 
     * @param str
     * string with newlines
     * @param replacement
     * replacement string
     * @return string with newlines replaced by the replacement string
     */
    protected static String fixNewLines(final String str, final String replacement) {
        String result = str;
        result = result.replace("\r\n", replacement);
        result = result.replace("\r", replacement);
        result = result.replace("\n", replacement);
        return result;
    }
    
    /**
     * Removes all whitespace from a string.
     * 
     * @param str
     * source string
     * @return source string without whitespace
     */
    public static String stripWhitespace(final String str) { // NO_UCD
        return str.replaceAll("\\s", "");
    }
    
    /**
     * This method fixes the following issues in the text:
     * <ul>
     * <li>punctuation <code>! ? : ; . ,</code> with no space after it</li>
     * <li>sequence of whitespace sequence</li>
     * <li>punctuation <code>; : ,</code> with a space before it</li>
     * <li>combination of the previous scenarios</li>
     * <li>various other rules for brackets, punctuation and space</li>
     * </ul>
     * 
     * @param str
     * input string
     * @return the input string with the spaces fixed
     */
    public static String fixTypographicSpaces(final String str) {
        String result = str;
        result = result.replace("!", "! ");
        result = result.replace("?", "? ");
        result = result.replace(":", ": ");
        result = result.replace(";", "; ");
        result = result.replace(".", ". ");
        result = result.replace(",", ", ");
        result = SimpleStringUtils.fixWhitespace(result);
        result = result.replace(". ]", ".]");
        result = result.replace(". )", ".)");
        result = result.replace(". /", "./");
        result = result.replace("? ]", "?]");
        result = result.replace("? )", "?)");
        result = result.replace("? /", "?/");
        result = result.replace(" :", ":");
        result = result.replace(" ;", ";");
        result = result.replace(" .", ".");
        result = result.replace(" ,", ",");
        result = result.replace(". -", ".-");
        result = result.replace("? ]", "?]");
        result = result.replace("[ =", "[=");
        result = result.replace("= ?", "=?");
        return result;
    }
    
    /**
     * Removes all unwanted characters from the given string. Generally
     * speaking, this method removes everything except letters (both lowercase
     * and uppercase), digits and some defined special characters, including
     * newline and space.
     * 
     * @param str
     * source string
     * @return source string without all unwanted symbols
     */
    public static String fixSpecialChars(final String str) {
        final StringBuilder sb = new StringBuilder(str.length());
        
        for (int i = 0; i < str.length(); i++) {
            final int c = str.charAt(i);
            
            if (Character.isLetterOrDigit(c)) {
                // allowed: letter or digit (both lowercase and uppercase)
                sb.appendCodePoint(c);
                continue;
            }
            
            if (SimpleStringUtils.ALLOWED_SYMBOLS.indexOf(c) != -1) {
                // allowed: symbols (brackets, quotes, etc.)
                sb.appendCodePoint(c);
                continue;
            }
        }
        
        final int diff = str.length() - sb.length();
        
        if (diff != 0) {
            SimpleStringUtils.LOG.debug("Unallowed characters removed from string: " + diff);
        }
        
        return sb.toString();
    }
    
    /**
     * Replaces all whitespace in a string by a single space. For example: four
     * spaces, two newlines, newline, newline and space, etc. are replaced by
     * one space only.
     * 
     * @param str
     * source string
     * @return source string without whitespace
     */
    public static String fixWhitespace(final String str) {
        return str.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Aligns the given string on right by spaces if it is shorter than the row
     * width provided. Usable only for fixed-width texts with non-proportional
     * fonts. Examples for width 10:
     * <ul>
     * <li>HELLO - <code>'     HELLO'</code></li>
     * <li>Mine - <code>'      Mine'</code></li>
     * </ul>
     * 
     * @param str
     * input string
     * @param width
     * row width (column count)
     * @return string padded by spaces on the left if shorted
     */
    public static String alignRightIfPossible(final String str, final int width) {
        final String str2 = str.trim();
        
        if (str2.length() < width) {
            return StringUtil.join(Collections.nCopies(width - str2.length(), " "), "") + str2;
        }
        
        return str2;
    }
    
    /**
     * Does a word inflection - returns the correct word for the given count of
     * items. The word can be specified for 1 / 2, 3, 4 / more items.
     * 
     * @param count
     * count of items
     * @param w1
     * word in singular (1 item)
     * @param w234
     * word in plural (2 - 4 items)
     * @param w5
     * word in plural (no items or more than 5 items)
     * @return word with a count in format <code>NUMBER [space] WORD</code>
     */
    public static String inflect(final int count, final String w1, final String w234, final String w5) {
        switch (Math.abs(count)) {
            case 1:
                return count + " " + w1;
            case 2:
            case 3:
            case 4:
                return count + " " + w234;
            default:
                return count + " " + w5;
        }
    }
    
    // =============
    // WORD WRAPPING
    // =============
    
    /**
     * Returns the input string wrapped to the given maximum width (number of
     * characters). The wrapping algorithm is smart and splits a word only if it
     * is necessary to do so. All existing new lines are preserved.
     * 
     * @param str
     * the input string
     * @param width
     * maximal string width (wrap width = number of columns)
     * @param newLine
     * new line symbol used to wrap
     * @return input string wrapped
     */
    public static String wordwrap(final String str, final int width, final String newLine) {
        final StringBuilder buffer = new StringBuilder(256);
        final String str2 = SimpleStringUtils.fixNewLines(str.trim(), Settings.LINE_END);
        final String[] lines = str2.split(Pattern.quote(Settings.LINE_END));
        int currentLine = 0;
        
        for (final String line : lines) {
            SimpleStringUtils.wordwrapRecursive(line, width, newLine, buffer);
            currentLine++;
            
            if (currentLine < lines.length) {
                buffer.append(newLine);
            }
        }
        
        return buffer.toString().trim();
    }
    
    /**
     * Recursive function for word wrapping. The result will be written to the
     * provided string builder object.
     * 
     * @param str
     * the input string (must be trimmed)
     * @param width
     * maximal string width (without the new line separator)
     * @param lineSeparator
     * new line symbol used to wrap
     * @param buffer
     * target string buffer
     */
    private static void wordwrapRecursive(final String str, final int width, final String lineSeparator, final StringBuilder buffer) {
        if (str.length() <= width) {
            // string is short enough to fit
            // ...and this is the end of the wrap algorithm!
            
            buffer.append(str);
        } else {
            // string is too long and must be wrapped
            
            // first, find a window
            // the window length here equals at least (width + 1)
            
            final String window = str.substring(0, width + 1);
            
            // find the best wrap position
            
            int wrapPosition = width;
            
            // try to locate some whitespace in the right half of the window
            
            for (int p = wrapPosition; p > width / 2; p--) {
                if (Character.isWhitespace(str.charAt(p))) {
                    wrapPosition = p;
                    break;
                }
            }
            
            // get adjusted window
            
            final String betterWindow = window.substring(0, wrapPosition);
            
            // add window with a wrap to the buffer
            
            buffer.append(betterWindow.trim());
            buffer.append(lineSeparator);
            
            // repeat recursively
            
            final String rest = str.substring(wrapPosition).trim();
            SimpleStringUtils.wordwrapRecursive(rest, width, lineSeparator, buffer);
        }
    }
    
    // =======
    // SORTING
    // =======
    
    /**
     * Creates a natural string comparator. This comparator uses different
     * behavior on numeric and string parts of the compared string. The string
     * parts are compared using the default Czech alphabet collator (see the
     * {@link CzechAlphabet} class).
     * 
     * @return a natural string comparator
     */
    public static Comparator<String> getNaturalStringComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                return SimpleStringUtils.compareStringsNaturalInsensitive(CzechAlphabet.getInstance(), o1, o2);
            }
        };
    }
    
    /**
     * Compares two strings naturally. This code is based on a code fragment by
     * Stephen Kelvin Friedrich - <code>s.friedrich@eekboom.com</code>.<br>
     * <br>
     * Copyright (c) 2006, Stephen Kelvin Friedrich, All rights reserved.<br>
     * <br>
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are
     * met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright
     * notice, this list of conditions and the following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright
     * notice, this list of conditions and the following disclaimer in the
     * documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of the "Stephen Kelvin Friedrich" nor the names of
     * its contributors may be used to endorse or promote products derived from
     * this software without specific prior written permission.</li>
     * </ul>
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
     * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
     * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
     * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
     * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
     * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
     * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
     * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
     * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
     * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     * 
     * @param defaultComparator
     * the default comparator to use on words
     * @param s1
     * first string
     * @param s2
     * second string
     * @return -1 if the first string is less than the second string, 1 if the
     * first string is greater than the second string, 0 if the strings are
     * equal
     */
    private static int compareStringsNaturalInsensitive(final Comparator<String> defaultComparator, final String s1, final String s2) {
        int sIndex = 0;
        int tIndex = 0;
        
        final int sLength = s1.length();
        final int tLength = s2.length();
        
        while (true) {
            if ((sIndex == sLength) && (tIndex == tLength)) {
                return 0;
            }
            
            if (sIndex == sLength) {
                return -1;
            }
            
            if (tIndex == tLength) {
                return 1;
            }
            
            char sChar = s1.charAt(sIndex);
            char tChar = s2.charAt(tIndex);
            
            boolean sCharIsDigit = Character.isDigit(sChar);
            boolean tCharIsDigit = Character.isDigit(tChar);
            
            if (sCharIsDigit && tCharIsDigit) {
                int sLeadingZeroCount = 0;
                
                while (sChar == '0') {
                    ++sLeadingZeroCount;
                    ++sIndex;
                    
                    if (sIndex == sLength) {
                        break;
                    }
                    
                    sChar = s1.charAt(sIndex);
                }
                
                int tLeadingZeroCount = 0;
                
                while (tChar == '0') {
                    ++tLeadingZeroCount;
                    ++tIndex;
                    
                    if (tIndex == tLength) {
                        break;
                    }
                    
                    tChar = s2.charAt(tIndex);
                }
                
                final boolean sAllZero = (sIndex == sLength) || !Character.isDigit(sChar);
                final boolean tAllZero = (tIndex == tLength) || !Character.isDigit(tChar);
                
                if (sAllZero && tAllZero) {
                    continue;
                }
                
                if (sAllZero && !tAllZero) {
                    return -1;
                }
                
                if (tAllZero) {
                    return 1;
                }
                
                int diff = 0;
                
                do {
                    if (diff == 0) {
                        diff = sChar - tChar;
                    }
                    
                    ++sIndex;
                    ++tIndex;
                    
                    if ((sIndex == sLength) && (tIndex == tLength)) {
                        return diff != 0 ? diff : sLeadingZeroCount - tLeadingZeroCount;
                    }
                    
                    if (sIndex == sLength) {
                        if (diff == 0) {
                            return -1;
                        }
                        
                        return Character.isDigit(s2.charAt(tIndex)) ? -1 : diff;
                    }
                    
                    if (tIndex == tLength) {
                        if (diff == 0) {
                            return 1;
                        }
                        
                        return Character.isDigit(s1.charAt(sIndex)) ? 1 : diff;
                    }
                    
                    sChar = s1.charAt(sIndex);
                    tChar = s2.charAt(tIndex);
                    sCharIsDigit = Character.isDigit(sChar);
                    tCharIsDigit = Character.isDigit(tChar);
                    
                    if (!sCharIsDigit && !tCharIsDigit) {
                        if (diff != 0) {
                            return diff;
                        }
                        
                        break;
                    }
                    
                    if (!sCharIsDigit) {
                        return -1;
                    }
                    
                    if (!tCharIsDigit) {
                        return 1;
                    }
                } while (true);
            } else {
                final int aw = sIndex;
                final int bw = tIndex;
                
                do {
                    ++sIndex;
                } while ((sIndex < sLength) && !Character.isDigit(s1.charAt(sIndex)));
                
                do {
                    ++tIndex;
                } while ((tIndex < tLength) && !Character.isDigit(s2.charAt(tIndex)));
                
                final String as = s1.substring(aw, sIndex);
                final String bs = s2.substring(bw, tIndex);
                
                final int subwordResult = defaultComparator.compare(as, bs);
                
                if (subwordResult != 0) {
                    return subwordResult;
                }
            }
        }
    }
    
    // ====
    // JSON
    // ====
    
    /**
     * Converts an object to JSON (unformatted).
     * 
     * @param object
     * object to convert
     * @return resulting JSON
     */
    public static String toJson(final Object object) {
        return SimpleStringUtils.toJson(object, false);
    }
    
    /**
     * Converts an object to JSON.
     * 
     * @param object
     * object to convert
     * @param format
     * format the output by using some whitespace
     * @return resulting JSON
     */
    public static String toJson(final Object object, final boolean format) {
        final JSON encoder = JSON.defaultJSON();
        encoder.setEscapeUnicodeChars(false);
        
        if (format) {
            return encoder.dumpObjectFormatted(object);
        }
        
        return encoder.forValue(object);
    }
    
    /**
     * Converts a JSON to a typed object.
     * 
     * @param <T>
     * static type of the object
     * @param str
     * JSON string to convert
     * @param type
     * runtime type of the object
     * @return resulting object
     */
    public static <T> T fromJson(final String str, final Class<T> type) {
        final JSONParser decoder = new JSONParser();
        return decoder.parse(type, str);
    }
    
    // =======
    // UTILITY
    // =======
    
    /**
     * Escapes columns for use in a CSV file. Escapes each column value and
     * separates the values by a CSV column separator.
     * 
     * @param addRowSeparator
     * end the row by adding the CSV row separator on the end
     * @param colValues
     * column values
     * @return escaped columns
     */
    public static String escapeColsForCSV(final boolean addRowSeparator, final String... colValues) {
        if ((colValues == null) || (colValues.length < 1)) {
            throw new IllegalArgumentException("Alespoň jedna hodnota sloupce musí být zadána.");
        }
        
        final StringBuilder buffer = new StringBuilder(200);
        
        for (final String value : colValues) {
            if (buffer.length() != 0) {
                buffer.append(Settings.CSV_COLUMN);
            }
            
            buffer.append(SimpleStringUtils.escapeForCSV(value));
        }
        
        if (addRowSeparator) {
            buffer.append(Settings.CSV_ROW);
        }
        
        return buffer.toString();
    }
    
    /**
     * Escapes a string value for use in a CSV file. Adds a quotes on both sides
     * and escapes quotes inside the string. The <code>null</code> value is
     * handled as an empty string.
     * 
     * @param value
     * value to be escaped (may be <code>null</code>)
     * @return escaped value
     */
    public static String escapeForCSV(final String value) {
        if (value == null) {
            return Settings.CSV_QUOTE + Settings.CSV_QUOTE;
        }
        
        return Settings.CSV_QUOTE + value.replace(Settings.CSV_QUOTE, Settings.CSV_QUOTE_ESCAPE).trim() + Settings.CSV_QUOTE;
    }
    
    /**
     * Checks if the given string is empty - that means, if it is
     * <code>null</code> or its trimmed length is less or equal zero.
     * 
     * @param str
     * input string
     * @return <code>true</code> if the input string is <code>null</code> or is
     * empty (trimmed), <code>false</code> otherwise
     */
    public static boolean isEmpty(final String str) {
        if ((str != null) && (str.trim().length() > 0)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns the trimmed input string if it is not <code>null</code> and it is
     * not empty. Returns <code>null</code> in all other cases.
     * 
     * @param str
     * input string
     * @return the input string if not <code>null</code> and not empty,
     * <code>null</code> otherwise
     */
    public static String emptyToNull(final String str) {
        if ((str != null) && (str.trim().length() > 0)) {
            return str.trim();
        }
        
        return null;
    }
    
    /**
     * Trims the CSV value and converts <code>null</code> to empty string.
     * 
     * @param str
     * input string
     * @return CSV value (always not <code>null</code>)
     */
    public static String nullToEmpty(final String str) {
        return str == null ? "" : str.trim();
    }
    
    /**
     * Ensures that the output is never empty. If the input value (converted to
     * a string by a <code>toString()</code> method) is empty or
     * <code>null</code>, returns a subsidiary character (-).
     * 
     * @param str
     * input string (or <code>null</code>
     * @return output string (never empty)
     */
    public static String neverEmpty(final Object str) {
        if ((str == null) || (str.toString().trim().length() < 1)) {
            return "-";
        }
        
        return str.toString();
    }
    
    /**
     * Returns the array of bytes padded by zeros to the given length.
     * 
     * @param length
     * target length
     * @param bytes
     * input byte array
     * @return array of bytes padded by zeroes
     */
    private static byte[] zeroPad(final int length, final byte[] bytes) {
        final byte[] padded = new byte[length];
        
        for (int i = 0; i < padded.length; i++) {
            padded[i] = 0;
        }
        
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        
        return padded;
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleStringUtils() {
        throw new UnsupportedOperationException();
    }
}
