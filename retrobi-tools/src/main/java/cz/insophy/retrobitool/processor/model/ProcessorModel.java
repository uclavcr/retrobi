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

package cz.insophy.retrobitool.processor.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.utils.library.SimpleFileUtils;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;
import cz.insophy.retrobitool.ProcessorFileMetaInfo;

/**
 * Image processor model. It is able to load files from certain directory
 * recursively, extract some information about the files and prepare them for
 * use in other application parts. During the process, empty pages are searched
 * for and marked. After the process ends, the processed files and directories
 * are marked to avoid unwanted duplicity and finally, removed. Exact details
 * about the process can be found in the documentation.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ProcessorModel {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorModel.class);
    /**
     * list of listeners
     */
    private final List<ProcessorModelListener> listeners;
    /**
     * model containing loaded files
     */
    private final ProcessorModelFiles files;
    /**
     * name of the first processed card
     */
    private String firstProcessedCard;
    /**
     * name of the last processed card
     */
    private String lastProcessedCard;
    /**
     * cancel flag
     */
    private Boolean cancel;
    /**
     * pause flag
     */
    private Boolean pause;
    /**
     * cancel lock object
     */
    private final Object cancelLock;
    /**
     * pause lock object
     */
    private final Object pauseLock;
    
    /**
     * Creates a new instance.
     */
    public ProcessorModel() {
        this.listeners = new LinkedList<ProcessorModelListener>();
        this.files = new ProcessorModelFiles();
        this.firstProcessedCard = null;
        this.lastProcessedCard = null;
        this.cancel = false;
        this.pause = false;
        this.cancelLock = new Object();
        this.pauseLock = new Object();
    }
    
    /**
     * Adds a listener of this model.
     * 
     * @param listener
     * a listener
     */
    public void addListener(final ProcessorModelListener listener) {
        this.listeners.add(listener);
    }
    
    // =========
    // PREPARING
    // =========
    
    /**
     * Returns the log output directory.
     * 
     * @return the log output directory
     */
    public File getLogDirectory() {
        return this.files.getTargetDirectory();
    }
    
    /**
     * Sets the catalog.
     * 
     * @param catalog
     * catalog
     */
    public void setCatalog(final Catalog catalog) {
        this.files.setCatalog(catalog);
        this.fireFilesUpdated();
    }
    
    /**
     * Sets the source directory.
     * 
     * @param dir
     * source directory
     */
    public void setSourceDirectory(final File dir) {
        this.files.setSourceDirectory(dir);
    }
    
    /**
     * Sets the target directory.
     * 
     * @param dir
     * target directory
     */
    public void setTargetDirectory(final File dir) {
        this.files.setTargetDirectory(dir);
    }
    
    /**
     * Opens a source directory and loads all files in it.
     */
    public void loadFiles() {
        // clear old files
        
        this.clear();
        
        try {
            // load files
            
            this.files.loadFiles();
        } catch (final RuntimeException x) {
            // clear files in the case of error
            
            this.clear();
            
            throw x;
        } finally {
            // notify listeners
            
            this.fireFilesUpdated();
        }
    }
    
    /**
     * Returns the list of all loaded files.
     * 
     * @return file list
     */
    public List<File> getFiles() {
        return this.files.getLoadedFiles();
    }
    
    /**
     * Returns the file meta information.
     * 
     * @param file
     * file
     * @return file meta information
     */
    public ProcessorFileMetaInfo getFileMetaInfo(final File file) {
        return this.files.getFileInfo(file);
    }
    
    /**
     * Clears the file list.
     */
    private void clear() {
        synchronized (this.cancelLock) {
            this.cancel = false;
        }
        
        synchronized (this.pauseLock) {
            this.pause = false;
        }
        
        this.files.clear();
        this.resetProcessedCards();
        this.fireFilesUpdated();
    }
    
    /**
     * Resets the numbers of processed cards.
     */
    private void resetProcessedCards() {
        this.firstProcessedCard = null;
        this.lastProcessedCard = null;
    }
    
    // ==========
    // PROCESSING
    // ==========
    
    /**
     * Starts the process.
     * 
     * @param threshold
     * threshold (relative value from 0 to 1 inclusive)
     * @param tolerance
     * tolerance (relative value from 0 to 1 inclusive)
     * @param shave
     * shave (relative value from 0 to 1 inclusive)
     * @param readOnly
     * do not allow file manipulation (moving, deleting...)
     */
    public void process(final double threshold, final double tolerance, final double shave, final boolean readOnly) {
        if (this.files.isEmpty()) {
            ProcessorModel.LOG.debug("No items to process.");
            return;
        }
        
        // reset file empty flags
        
        this.files.resetEmptyFlags();
        
        // prepare to process
        
        ProcessorModel.LOG.debug("Starting the process...");
        ProcessorModel.LOG.debug("Read only: " + readOnly);
        
        int filesDone = 0;
        final int filesTotal = this.files.getFileCount();
        
        synchronized (this.cancelLock) {
            this.cancel = false;
        }
        
        synchronized (this.pauseLock) {
            this.pause = false;
        }
        
        this.fireProcessStarted();
        
        try {
            // process all loaded directories
            
            for (final File dir : this.files.getLoadedDirectories()) {
                // reset first and last card
                
                this.resetProcessedCards();
                
                // process all loaded files
                
                for (final File file : this.files.getLoadedFiles(dir)) {
                    // check pause
                    
                    boolean pauseCopy;
                    
                    synchronized (this.pauseLock) {
                        pauseCopy = this.pause;
                    }
                    
                    while (pauseCopy) {
                        try {
                            Thread.sleep(500);
                        } catch (final Exception x) {
                            // NOP
                        }
                        
                        synchronized (this.pauseLock) {
                            pauseCopy = this.pause;
                        }
                    }
                    
                    // check cancel
                    
                    synchronized (this.cancelLock) {
                        if (this.cancel) {
                            ProcessorModel.LOG.debug("Processing cancelled.");
                            break;
                        }
                    }
                    
                    // ----------------
                    // ANALYZE THE FILE
                    // ----------------
                    
                    ProcessorModel.LOG.debug("Analyzing file: " + file.getAbsolutePath());
                    
                    // load image
                    
                    final BufferedImage image = SimpleImageUtils.loadImageFromFile(file);
                    
                    if (image == null) {
                        ProcessorModel.LOG.debug("Could not read the input image file.");
                        throw new IllegalStateException("Zdrojový soubor se nepodařilo načíst.");
                    }
                    
                    // get the image file meta-information
                    
                    final ProcessorFileMetaInfo fileinf = this.files.getFileInfo(file);
                    
                    // check whether the image is empty
                    // (only the last page of the card is checked)
                    
                    if (!fileinf.wasCheckedEmpty()) {
                        ProcessorModel.LOG.debug("Checking the last page emptiness first...");
                        
                        if (fileinf.getPage() == fileinf.getLastPage()) {
                            fileinf.setEmpty(this.isImageEmpty(image, threshold, tolerance, shave));
                        } else {
                            fileinf.setEmpty(false);
                        }
                    }
                    
                    // -------------
                    // MOVE THE FILE
                    // -------------
                    
                    if (!readOnly) {
                        ProcessorModel.LOG.debug("Renaming file: " + file.getAbsolutePath());
                        
                        // prepare backup file
                        
                        this.files.prepareBackupFile(fileinf);
                        
                        // update processed cards
                        // (useful for finalizing directory)
                        
                        final String fileNumber = String.valueOf(SimpleFileUtils.extractNumberFromFile(fileinf.getFile()));
                        
                        if (this.firstProcessedCard == null) {
                            this.firstProcessedCard = fileNumber;
                        }
                        
                        this.lastProcessedCard = fileNumber;
                        
                        // image is complete, rename it
                        
                        synchronized (this.cancelLock) {
                            if (!readOnly && !this.cancel) {
                                this.files.safeRenameFile(file, new File(file.getParentFile(), SimpleFileUtils.PROCESSED_IMAGE_PREFIX + file.getName()));
                            }
                        }
                    }
                    
                    // increment file counter
                    
                    ProcessorModel.LOG.debug("File finished.");
                    filesDone++;
                    this.fireProcessStatusChanged(filesTotal, filesDone);
                }
                
                // finalize the finished directory
                
                synchronized (this.cancelLock) {
                    if (!readOnly && !this.cancel) {
                        this.files.safeCleanDirectory(dir, this.firstProcessedCard, this.lastProcessedCard);
                    }
                }
            }
        } catch (final Exception x) {
            this.fireProcessFailed();
            ProcessorModel.LOG.error(x.getMessage());
            throw new IllegalStateException(x.getMessage(), x);
        }
        
        // finished
        
        this.fireProcessFinished();
        ProcessorModel.LOG.debug("Finished.");
    }
    
    // ===============
    // UTILITY METHODS
    // ===============
    
    /**
     * Check whether the image is likely to be empty.
     * 
     * @param inputImage
     * input image
     * @param threshold
     * threshold
     * @param tolerance
     * tolerance
     * @param shave
     * shave
     * @return <code>true</code> if the image is likely to be empty,
     * <code>false</code> otherwise
     * @throws IOException
     * IO exception
     */
    private boolean isImageEmpty(final BufferedImage inputImage, final double threshold, final double tolerance, final double shave) throws IOException {
        if (SimpleImageUtils.isImageEmpty(inputImage, threshold, tolerance, shave)) {
            ProcessorModel.LOG.debug("Image is empty.");
            return true;
        }
        
        ProcessorModel.LOG.debug("Not empty.");
        return false;
    }
    
    // =============
    // CANCEL, PAUSE
    // =============
    
    /**
     * Thread-safe method for raising the cancel flag.
     */
    public void cancel() {
        synchronized (this.cancelLock) {
            this.cancel = true;
        }
        
        synchronized (this.pauseLock) {
            this.pause = false;
        }
    }
    
    /**
     * Thread-safe method for toggling the cancel flag.
     */
    public void togglePause() {
        synchronized (this.cancelLock) {
            if (this.cancel) {
                // do nothing
                
                return;
            }
        }
        
        synchronized (this.pauseLock) {
            if (this.pause) {
                // resume
                
                this.pause = false;
            } else {
                // pause
                
                this.pause = true;
            }
        }
    }
    
    // ======
    // EVENTS
    // ======
    
    /**
     * Fire event "files updated" to all listeners.
     */
    private void fireFilesUpdated() {
        for (final ProcessorModelListener listener : this.listeners) {
            listener.filesUpdated();
        }
    }
    
    /**
     * Fire event "process started" to all listeners.
     */
    private void fireProcessStarted() {
        for (final ProcessorModelListener listener : this.listeners) {
            listener.processStarted();
        }
    }
    
    /**
     * Fire event "process status changed" to all listeners.
     * 
     * @param total
     * total files
     * @param done
     * completed files
     */
    private void fireProcessStatusChanged(final int total, final int done) {
        for (final ProcessorModelListener listener : this.listeners) {
            listener.processStatusUpdated(total, done);
        }
    }
    
    /**
     * Fire event "process finished" to all listeners.
     */
    private void fireProcessFinished() {
        for (final ProcessorModelListener listener : this.listeners) {
            listener.processFinished();
        }
    }
    
    /**
     * Fire event "process failed" to all listeners.
     */
    private void fireProcessFailed() {
        for (final ProcessorModelListener listener : this.listeners) {
            listener.processFailed();
        }
    }
}
