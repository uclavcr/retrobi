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

package cz.insophy.retrobi.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Universal date-based CSV logger. This class is thread safe.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class CSVHistoryLogger {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(CSVHistoryLogger.class);
    /**
     * singleton instance
     */
    private static CSVHistoryLogger instance = null;
    /**
     * output directory
     */
    private final File outputDir;
    
    /**
     * Returns the singleton instance.
     * 
     * @return the singleton instance
     */
    public synchronized static CSVHistoryLogger getInstance() {
        if (CSVHistoryLogger.instance == null) {
            try {
                CSVHistoryLogger.instance = new CSVHistoryLogger(Settings.CSV_LOG_DIRECTORY);
            } catch (final IOException x) {
                CSVHistoryLogger.LOG.error(x.getMessage());
            }
        }
        
        return CSVHistoryLogger.instance;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param outputDir
     * output directory (will be checked)
     * @throws IOException
     * I/O exception
     */
    private CSVHistoryLogger(final File outputDir) throws IOException {
        if (!outputDir.exists() || !outputDir.isDirectory() || !outputDir.canWrite()) {
            throw new IOException(String.format("Neplatný adresář pro výstup CSV: " + outputDir.getAbsolutePath()));
        }
        
        this.outputDir = outputDir;
    }
    
    /**
     * Appends a log message. The order of columns is as follows:
     * <ol>
     * <li>date added</li>
     * <li>type</li>
     * <li>subject</li>
     * <li>body</li>
     * <li>card name</li>
     * <li>image name</li>
     * <li>card ID</li>
     * <li>user name</li>
     * <li>user ID</li>
     * <li>old value</li>
     * <li>new value</li>
     * </ol>
     * 
     * @param message
     * log message
     * @param oldValue
     * old value (or <code>null</null>)
     * @param newValue
     * new value (or <code>null</null>)
     * @throws IOException
     * I/O exception
     */
    public void append(final Message message, final String oldValue, final String newValue) throws IOException {
        this.append(
                SimpleStringUtils.nullToEmpty(message.getAdded().toString()),
                SimpleStringUtils.nullToEmpty(message.getType().name()),
                SimpleStringUtils.nullToEmpty(message.getType().toString()),
                SimpleStringUtils.nullToEmpty(message.getBody()),
                SimpleStringUtils.nullToEmpty(message.getCardName()),
                SimpleStringUtils.nullToEmpty(message.getImageName()),
                SimpleStringUtils.nullToEmpty(message.getCardId()),
                SimpleStringUtils.nullToEmpty(message.getUserName()),
                SimpleStringUtils.nullToEmpty(message.getUserId()),
                SimpleStringUtils.nullToEmpty(oldValue),
                SimpleStringUtils.nullToEmpty(newValue));
    }
    
    /**
     * Appends a values to the log file. Each value will be treated as a string
     * column value and therefore quoted.
     * 
     * @param values
     * an array of values
     * @throws IOException
     * I/O exception
     */
    private void append(final String... values) throws IOException {
        synchronized (this.outputDir) {
            final String escapedValue = SimpleStringUtils.escapeColsForCSV(true, values);
            CSVHistoryLogger.appendToFile(this.outputDir, escapedValue);
        }
    }
    
    /**
     * Appends a string to the log file.
     * 
     * @param outputDir
     * output directory
     * @param value
     * a string value to append
     * @throws IOException
     * I/O exception
     */
    private static void appendToFile(final File outputDir, final String value) throws IOException {
        // get the correct output file
        
        final File outputFile = CSVHistoryLogger.getFileForToday(outputDir);
        
        // decide whether to write the CSV file header
        
        final boolean writeHeader = !outputFile.exists();
        
        // create writer, append the string value and flush
        
        OutputStream outputStream = null;
        Writer outputWriter = null;
        
        try {
            outputStream = new FileOutputStream(outputFile, true);
            outputWriter = new OutputStreamWriter(outputStream, Settings.CSV_ENCODING);
            
            if (writeHeader) {
                outputWriter.write(SimpleStringUtils.escapeColsForCSV(
                        true,
                        "Datum a čas",
                        "Typ (systémový)",
                        "Typ",
                        "Událost",
                        "Lístek",
                        "Obrázek",
                        "ID lístku",
                        "Uživatel",
                        "ID uživatele",
                        "Stará hodnota",
                        "Nová hodnota"));
            }
            
            outputWriter.write(value);
        } finally {
            if (outputWriter != null) {
                // flush and close
                
                outputWriter.flush();
                outputWriter.close();
            }
            
            if (outputStream != null) {
                // flush and close
                
                outputStream.flush();
                outputStream.close();
            }
        }
    }
    
    /**
     * Returns the filename for today. The filename is only date based (for
     * example <code>2005-12-24.csv</code> for the 24th December 2005).
     * 
     * @param outputDir
     * output directory
     * @return filename for today
     */
    private static File getFileForToday(final File outputDir) {
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH) + 1;
        final int day = c.get(Calendar.DATE);
        final String name = String.format("%04d-%02d-%02d.csv", year, month, day);
        CSVHistoryLogger.LOG.debug("Log name for today: " + name);
        return new File(outputDir, name);
    }
}
