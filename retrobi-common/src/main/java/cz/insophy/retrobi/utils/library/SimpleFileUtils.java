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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.Catalog;

/**
 * File utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleFileUtils {
    /**
     * suspected empty image prefix
     */
    public static final String EMPTY_IMAGE_PREFIX = "!PRAZDNY_";
    /**
     * finished image prefix
     */
    public static final String PROCESSED_IMAGE_PREFIX = "!HOTOVO_";
    /**
     * valid source image filename pattern
     */
    private static final Pattern SOURCE_IMAGE_PATTERN = Pattern.compile("^[0-9]+(x([0-9]+))?\\.tif$", Pattern.CASE_INSENSITIVE);
    /**
     * valid backup image filename pattern
     */
    private static final Pattern BACKUP_IMAGE_PATTERN = Pattern.compile("^[A-Z]{1,2}-.+-[0-9]+-[0-9]+\\.tif$", Pattern.CASE_INSENSITIVE);
    /**
     * valid empty backup image filename pattern
     */
    private static final Pattern BACKUP_EMPTY_IMAGE_PATTERN = Pattern.compile("^!PRAZDNY_[A-Z]{1,2}-.+-[0-9]+-[0-9]+\\.tif$", Pattern.CASE_INSENSITIVE);
    /**
     * valid backup image OCR file pattern
     */
    private static final Pattern BACKUP_OCR_PATTERN = Pattern.compile("^[A-Z]{1,2}-.+-[0-9]+-[0-9]+\\.txt$", Pattern.CASE_INSENSITIVE);
    /**
     * valid target image file pattern
     */
    private static final Pattern TARGET_PATTERN = Pattern.compile("^^[A-Z]{1,2}-.+-[0-9]+-[0-9]+\\.png$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Changes the file extension.
     * 
     * @param originalFile
     * original file
     * @param newExtension
     * new extension for the file
     * @return file with a new extension
     */
    public static File changeExtension(final File originalFile, final String newExtension) {
        final int lastDotIndex = originalFile.getName().lastIndexOf('.');
        
        if (lastDotIndex != -1) {
            return new File(originalFile.getParentFile(), originalFile.getName().substring(0, lastDotIndex) + "." + newExtension);
        }
        
        return new File(originalFile + "." + newExtension);
    }
    
    /**
     * Reads a text file contents into a string. This method adds line end after
     * the text because of possible merge of several OCR files.
     * 
     * @param textFile
     * input text file
     * @return contents of the text file
     * @throws IOException
     * I/O exception
     */
    public static String readFileToString(final File textFile) throws IOException {
        final String text = FileUtils.readFileToString(textFile, "utf-8");
        return text + Settings.LINE_END;
    }
    
    /**
     * Writes a string contents to a file.
     * 
     * @param data
     * contents to be written
     * @param targetFile
     * target file
     * @throws IOException
     * I/O exception
     */
    protected static void writeStringToFile(final String data, final File targetFile) throws IOException {
        FileUtils.writeStringToFile(targetFile, data, "utf-8");
    }
    
    /**
     * Copies the source file to the target location.
     * 
     * @param source
     * source file (must exist)
     * @param target
     * target file
     * @param overwrite
     * if the target file exists, it will be overwritten
     * @throws IOException
     * I/O exception
     */
    public static void copyFile(final File source, final File target, final boolean overwrite) throws IOException {
        if (!source.isFile() || !source.exists() || !source.canRead()) {
            throw new IOException("Zdrojový soubor neexistuje nebo není čitelný.");
        }
        
        if (!overwrite && target.exists()) {
            throw new IOException("Cílový soubor existuje.");
        }
        
        if (target.exists() && !target.delete()) {
            throw new IOException("Cílový soubor nelze smazat.");
        }
        
        FileUtils.copyFile(source, target);
    }
    
    /**
     * Generates a valid image filename based on provided information.
     * 
     * @param catalog
     * catalog
     * @param batch
     * batch name
     * @param cardNumber
     * card number
     * @param page
     * card image page number
     * @param ext
     * extension (jpg, png, bmp)
     * @param empty
     * image is empty (for prefix)
     * @return valid image filename
     */
    public static String produceImageFileName(final Catalog catalog, final String batch, final int cardNumber, final int page, final String ext, final boolean empty) {
        if (empty) {
            return String.format("%s%s-%s-%d-%d.%s", SimpleFileUtils.EMPTY_IMAGE_PREFIX, catalog.name(), batch, cardNumber, page, ext.toLowerCase());
        }
        
        return String.format("%s-%s-%d-%d.%s", catalog.name(), batch, cardNumber, page, ext.toLowerCase());
    }
    
    /**
     * Validates a filename. Checks whether the given file is a valid OCR text
     * file.
     * 
     * @param file
     * a file
     * @return TRUE if the file is valid
     */
    public static boolean isValidOCRFile(final File file) {
        return SimpleFileUtils.isValidFile(file, SimpleFileUtils.BACKUP_OCR_PATTERN);
    }
    
    /**
     * Validates a filename. Checks whether the given file is a valid source
     * file.
     * 
     * @param file
     * a file
     * @return TRUE if the file is valid
     */
    public static boolean isValidSourceFile(final File file) {
        return SimpleFileUtils.isValidFile(file, SimpleFileUtils.SOURCE_IMAGE_PATTERN);
    }
    
    /**
     * Validates a filename. Checks whether the given file is a valid backup
     * file.
     * 
     * @param file
     * a file
     * @return TRUE if the file is valid
     */
    public static boolean isValidBackupFile(final File file) {
        return SimpleFileUtils.isValidFile(file, SimpleFileUtils.BACKUP_IMAGE_PATTERN);
    }
    
    /**
     * Validates a filename. Checks whether the given file is a valid empty
     * backup file.
     * 
     * @param file
     * a file
     * @return TRUE if the file is valid
     */
    public static boolean isValidEmptyBackupFile(final File file) {
        return SimpleFileUtils.isValidFile(file, SimpleFileUtils.BACKUP_EMPTY_IMAGE_PATTERN);
    }
    
    /**
     * Validates a filename. Checks whether the given file is a valid output
     * file (converted for use in the database).
     * 
     * @param file
     * a file
     * @return TRUE if the file is valid
     */
    public static boolean isValidOutputFile(final File file) {
        return SimpleFileUtils.isValidFile(file, SimpleFileUtils.TARGET_PATTERN);
    }
    
    /**
     * Validates a given file by matching its name against the specified regular
     * expression rule. It also checks, if the file exists, is readable and has
     * non-zero length.
     * 
     * @param file
     * a file
     * @param pattern
     * pattern to validate against
     * @return TRUE if the file matches the criteria
     */
    private static boolean isValidFile(final File file, final Pattern pattern) {
        if (!file.exists() || !file.isFile() || (file.length() < 1)) {
            return false;
        }
        
        if (!pattern.matcher(file.getName()).find()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns the filename without an extension. Example:
     * <code>123456-7.png -> 123456-7</code>
     * 
     * @param file
     * a file
     * @return the filename without an extension
     */
    public static String extractNameFromFile(final File file) {
        final int dotpos = file.getName().lastIndexOf('.');
        
        if (dotpos == -1) {
            return file.getName();
        }
        
        return file.getName().substring(0, dotpos);
    }
    
    /**
     * Returns the number contained in the beginning of the file name. The file
     * name is read character by character from the left until the first
     * non-digit character is encountered. Then the read string (converted to
     * integer) is returned. Example:<br>
     * <code>154254abc.png -> 154254</code>
     * 
     * @param file
     * a file
     * @return the number in the file name
     */
    public static BigDecimal extractNumberFromFile(final File file) {
        final StringBuilder builder = new StringBuilder(20);
        
        for (final char digit : file.getName().toCharArray()) {
            if (Character.isDigit(digit)) {
                builder.append(digit);
            } else {
                break;
            }
        }
        
        try {
            return new BigDecimal(builder.toString());
        } catch (final NumberFormatException x) {
            throw new IllegalArgumentException("Neplatný název souboru (pro získání čísla): " + file.getName());
        }
    }
    
    /**
     * Returns the catalog extracted from the file name. Example:
     * <code>A-156748.png -> Catalog.AUTHOR</code>
     * 
     * @param file
     * a file
     * @return catalog
     */
    public static Catalog extractCatalogFromFile(final File file) {
        final String[] pieces = SimpleFileUtils.extractNameFromFile(file).split("-");
        
        if (pieces.length > 0) {
            try {
                return Catalog.valueOf(pieces[0]);
            } catch (final IllegalArgumentException x) {
                // NOP
            }
        }
        
        throw new IllegalArgumentException("Neplatný název souboru (pro získání katalogu): " + file.getName());
    }
    
    /**
     * Returns the card number from the file. Example:<br>
     * <code>154421-5.png -> 154421</code>,<br>
     * <code>Novak-154421-5.png -> 154421</code>,<br>
     * <code>Novak-Mlynar-8-1.png -> 8</code>
     * 
     * @param file
     * a file
     * @return card number
     */
    public static int extractCardNumberFromFile(final File file) {
        final String[] pieces = SimpleFileUtils.extractNameFromFile(file).split("-");
        
        if (pieces.length > 1) {
            try {
                return Integer.valueOf(pieces[pieces.length - 2]);
            } catch (final NumberFormatException x) {
                // NOP
            }
        }
        
        throw new IllegalArgumentException("Neplatný název souboru (pro získání čísla lístku): " + file.getName());
    }
    
    /**
     * Returns the card page number from the file. Example:<br>
     * <code>154421-5.png -> 5</code>,<br>
     * <code>Novak-154421-5.png -> 5</code>,<br>
     * <code>Novak-Mlynar-8-1.png -> 1</code>
     * 
     * @param file
     * a file
     * @return card page number
     */
    public static int extractPageNumberFromFile(final File file) {
        final String[] pieces = SimpleFileUtils.extractNameFromFile(file).split("-");
        
        if (pieces.length > 1) {
            try {
                return Integer.valueOf(pieces[pieces.length - 1]);
            } catch (final NumberFormatException x) {
                // NOP
            }
        }
        
        throw new IllegalArgumentException("Neplatný název souboru (pro získání čísla stránky): " + file.getName());
    }
    
    /**
     * Returns a first continuous digit group size from the filename. First, it
     * finds the first digit. Then it continues until first non-numeric
     * character found. Example:<br>
     * <code>a123456b.png -> 6</code>,<br>
     * <code>aaa123456bbb123.png -> 6</code>,<br>
     * <code>abcdef.png -> 0</code>
     * 
     * @param file
     * a file
     * @return number of digits in the file name
     */
    public static int extractDigitCount(final File file) {
        int count = 0;
        boolean started = false;
        
        for (final char c : file.getName().toCharArray()) {
            if (Character.isDigit(c)) {
                if (!started) {
                    // first digit group started
                    
                    started = true;
                    count = 1;
                } else {
                    // first digit group continues
                    
                    count++;
                }
            } else {
                if (started) {
                    // first digit group ended
                    
                    break;
                }
            }
        }
        
        return count;
    }
    
    /**
     * Returns the number of papers of the card. The count is extracted from the
     * card file name. Each paper therefore contains two pages: odd and even.
     * 
     * @param file
     * input file
     * @return number of papers of the card
     */
    public static int extractPaperCountFromFile(final File file) {
        final Matcher matcher = SimpleFileUtils.SOURCE_IMAGE_PATTERN.matcher(file.getName().toLowerCase());
        
        if (!matcher.find()) {
            // no match, use the default value (one paper)
            
            return 1;
        }
        
        final String paperCount = matcher.group(2);
        
        if (paperCount == null) {
            // no match, use the default value (one paper)
            
            return 1;
        }
        
        // match, use the given value as a paper count
        
        int count = 1;
        
        try {
            count = Integer.valueOf(paperCount);
        } catch (final NumberFormatException x) {
            throw new IllegalArgumentException("Neplatný název souboru (pro získání počtu listů): " + file.getName());
        }
        
        if ((count < 1) || (count > 9)) {
            throw new IllegalArgumentException("Nesprávný počet listů (povolené rozmezí = 1 až 9).");
        }
        
        return count;
    }
    
    /**
     * Returns the last card number in the given directory. If the directory
     * does not exist or contains no valid backup card image files, default
     * value of 0 will be returned.
     * 
     * @param backupDir
     * backup directory to search in
     * @return number of the last card in the directory or 0
     */
    public static int getLastCardNumberInBackupDir(final File backupDir) {
        if (!backupDir.exists() || !backupDir.isDirectory() || (backupDir.listFiles().length < 1)) {
            return 0;
        }
        
        int lastNumber = 0;
        
        for (final File cardImageFile : backupDir.listFiles()) {
            if (SimpleFileUtils.isValidBackupFile(cardImageFile)) {
                final int thisNumber = SimpleFileUtils.extractCardNumberFromFile(cardImageFile);
                
                if (thisNumber > lastNumber) {
                    lastNumber = thisNumber;
                }
            }
        }
        
        return lastNumber;
    }
    
    /**
     * Utility method for replacing a directory in a file path. It creates a
     * file from the base file by replacing the old root by a new root while
     * preserving the sub-directory structure.<br>
     * For example, let us have path <code>"/R1ROOT/.../R1/C/D"</code>. Now we
     * want to replace <code>R1</code> by <code>R2</code> to produce path
     * <code>"/R2ROOT/.../R2/C/D"</code>. To do this, we must call this method
     * with parameters (<code>"/R1ROOT/.../R1/C/D"</code>,
     * <code>"/R1ROOT/.../R1"</code>, <code>"/R2ROOT/.../R2"</code>).
     * 
     * @param file
     * a base file
     * @param oldRootDir
     * old root directory
     * @param newRootDir
     * new root directory
     * @return a new file constructed from base file by replacing the old root
     * by a new root (the sub-directory structure will be preserved)
     */
    public static File replaceRootDir(final File file, final File oldRootDir, final File newRootDir) {
        // if the roots are the same, the file already is on correct location
        
        final String absOldRoot = oldRootDir.getAbsolutePath();
        final String absNewRoot = newRootDir.getAbsolutePath();
        
        if (absNewRoot.equals(absOldRoot)) {
            return file;
        }
        
        // check that file lies in the old root
        
        final String absFile = file.getAbsolutePath();
        
        if (!absFile.startsWith(absOldRoot)) {
            throw new IllegalArgumentException("Soubor není umístěn ve správném kořenovém adresáři.");
        }
        
        // glue the new root and the rest to create a new file
        
        final String rest = absFile.substring(absOldRoot.length());
        
        return new File(newRootDir, rest);
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleFileUtils() {
        throw new UnsupportedOperationException();
    }
}
