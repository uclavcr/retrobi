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

package cz.insophy.retrobitool.importer.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultListModel;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.utils.library.SimpleFileUtils;

/**
 * Default list model extended by file logging features. It writes a CSV file
 * with comma as field separator and Unix newline as a line separator. For each
 * log file, just one instance of this class can be created. If you try to
 * append to already closed log, an exception will be thrown. Therefore, for
 * each logged run, use the new instance of this class.<br>
 * <br>
 * This list model shows only a part of log, as the big log slows down the
 * algorithm run significantly.
 * 
 * @author Vojtěch Hordějčuk
 */
public class LoggingListModel extends DefaultListModel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * maximum number of lines displayed
     */
    private static final int MAX_LINES = 20;
    /**
     * output file
     */
    private File outputFile;
    /**
     * output file writer
     */
    private FileWriter outputFileWriter;
    /**
     * line counter
     */
    private int counter;
    
    /**
     * Creates a new instance.
     * 
     * @param dir
     * output directory where the log will be saved
     * @throws IOException
     * IO exception
     */
    public LoggingListModel(final File dir) throws IOException {
        super();
        
        // reset counter
        
        this.counter = 0;
        
        // create output parent directory
        
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException(String.format("Nepodařilo se vytvořit adresář pro výstup logu:" + Settings.LINE_END + Settings.LINE_END + "%s", dir.getAbsolutePath()));
            }
        }
        
        // get the current time
        
        final Time time = Time.now();
        final long ms = System.currentTimeMillis();
        
        // prepare the output filename
        
        this.outputFile = new File(dir, String.format(
                "%04d-%02d-%02d__%02d-%02d__%d.csv",
                time.getYear(),
                time.getMonth(),
                time.getDay(),
                time.getHour(),
                time.getMinute(),
                ms));
        
        // check existing file
        
        if (this.outputFile.exists()) {
            throw new IOException(String.format("Soubor se stejným názvem '%s' již existuje.", this.outputFile.getAbsolutePath()));
        }
        
        // create a file writer and write the first greeting line
        
        this.outputFileWriter = new FileWriter(this.outputFile);
        
        this.addElementWithWrite("RETROBI IMPORT", null, null);
        this.addElementWithWrite(String.format("Přihlášený uživatel: %s", System.getProperty("user.name")), null, null);
    }
    
    /**
     * Adds an element on the end of the list and writes into the log.
     * 
     * @param message
     * message
     * @param card
     * relevant card (or <code>null</code>)
     * @param file
     * relevant file (or <code>null</code>)
     * @throws IOException
     * IO exception
     */
    public void addElementWithWrite(final String message, final Card card, final File file) throws IOException {
        // increment the counter
        
        this.counter++;
        
        // add element to the model
        
        super.addElement(message);
        
        if (this.size() > LoggingListModel.MAX_LINES) {
            // too many lines
            // remove the first one and display replacement message
            
            super.remove(0);
            super.set(0, String.format("(%d dalších zpráv skryto)", this.counter - LoggingListModel.MAX_LINES));
        }
        
        this.write(
                message,
                file == null ? "" : file.getAbsolutePath(),
                (card == null) || (card.getId() == null) ? "" : card.getId());
    }
    
    /**
     * Closes the log. The same class can not be used anymore. New list model
     * must be created.
     * 
     * @throws IOException
     * IO exception
     */
    public void close() throws IOException {
        this.outputFileWriter.close();
        this.outputFile.renameTo(new File(this.outputFile.getParentFile(), SimpleFileUtils.PROCESSED_IMAGE_PREFIX + this.outputFile.getName()));
        this.outputFileWriter = null;
        this.outputFile = null;
    }
    
    /**
     * Writes a line to the resulting CSV file.
     * 
     * @param s1
     * first field value (message)
     * @param s2
     * second field value (card ID)
     * @param s3
     * third field value (file absolute path)
     * @throws IOException
     * IO exception
     */
    private void write(final String s1, final String s2, final String s3) throws IOException {
        if ((s1 == null) || (s2 == null) || (s3 == null)) {
            throw new NullPointerException();
        }
        
        if (this.outputFileWriter == null) {
            throw new IllegalStateException("Výstupní soubor je již uzavřený a nelze do něj zapisovat.");
        }
        
        final String fstr =
                "" +
                        Settings.CSV_QUOTE +
                        Time.now().toString() +
                        Settings.CSV_QUOTE +
                        Settings.CSV_COLUMN +
                        Settings.CSV_QUOTE +
                        s1.replace(Settings.CSV_QUOTE, Settings.CSV_QUOTE_ESCAPE) +
                        Settings.CSV_QUOTE +
                        Settings.CSV_COLUMN +
                        Settings.CSV_QUOTE +
                        s2.replace(Settings.CSV_QUOTE, Settings.CSV_QUOTE_ESCAPE) +
                        Settings.CSV_QUOTE +
                        Settings.CSV_COLUMN +
                        Settings.CSV_QUOTE +
                        s3.replace(Settings.CSV_QUOTE, Settings.CSV_QUOTE_ESCAPE) +
                        Settings.CSV_QUOTE +
                        Settings.CSV_ROW;
        
        this.outputFileWriter.write(fstr);
        this.outputFileWriter.flush();
    }
}
