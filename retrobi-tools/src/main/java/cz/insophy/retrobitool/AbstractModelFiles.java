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

package cz.insophy.retrobitool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.utils.library.SimpleFileUtils;
import cz.insophy.retrobi.utils.library.SimpleFrameUtils;
import cz.insophy.retrobitool.importer.model.ImporterModelFiles;
import cz.insophy.retrobitool.processor.model.ProcessorModelFiles;

/**
 * Abstract basic file holder to build more specific classes for use in the
 * processor and importer tools. The class has an <code>source</code> and
 * <code>target</code> directory. Source directory can be opened and all its
 * contents is sorted and fetched. During the process, meta information are
 * extracted.
 * 
 * @author Vojtěch Hordějčuk
 * @param <T>
 * exact class of the file meta information
 */
public abstract class AbstractModelFiles<T extends FileMetaInfo> {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImporterModelFiles.class);
    /**
     * source directory
     */
    private File sourceDir;
    /**
     * target directory
     */
    private File targetDir;
    /**
     * all loaded source files grouped by their source directories
     */
    private final Map<File, List<File>> loadedFiles;
    /**
     * map for file meta information
     */
    private final Map<File, T> loadedFilesInfo;
    
    /**
     * Creates a new instance.
     */
    protected AbstractModelFiles() {
        this.loadedFiles = new HashMap<File, List<File>>();
        this.loadedFilesInfo = new HashMap<File, T>();
        this.sourceDir = null;
        this.targetDir = null;
    }
    
    /**
     * Clears all the loaded files.
     */
    public void clear() {
        AbstractModelFiles.LOG.debug("Clearing all loaded files...");
        
        // clear the file map
        
        this.loadedFiles.clear();
        this.loadedFilesInfo.clear();
    }
    
    /**
     * Returns the source directory or NULL.
     * 
     * @return target directory or NULL
     */
    public File getSourceDirectory() {
        if (this.sourceDir == null) {
            throw new IllegalStateException();
        }
        
        return this.sourceDir;
    }
    
    /**
     * Returns the target directory or NULL.
     * 
     * @return target directory or NULL
     */
    public File getTargetDirectory() {
        if (this.targetDir == null) {
            throw new IllegalStateException();
        }
        
        return this.targetDir;
    }
    
    /**
     * Checks and sets the source directory.
     * 
     * @param dir
     * source directory
     */
    public void setSourceDirectory(final File dir) {
        // check the source directory
        
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            throw new IllegalArgumentException("Neplatná vstupní složka.");
        }
        
        if ((dir.listFiles() == null) || (dir.listFiles().length < 1)) {
            throw new IllegalArgumentException("Vstupní složka neobsahuje žádné přístupné soubory.");
        }
        
        AbstractModelFiles.LOG.debug(String.format("Setting source directory to '%s'...", dir.getAbsolutePath()));
        this.sourceDir = dir;
    }
    
    /**
     * Checks and sets the target directory.
     * 
     * @param dir
     * target directory
     */
    public void setTargetDirectory(final File dir) {
        // check the target directory
        
        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException("Neplatná výstupní složka: " + dir.getAbsolutePath());
        }
        
        AbstractModelFiles.LOG.debug(String.format("Setting target directory to '%s'...", dir.getAbsolutePath()));
        this.targetDir = dir;
    }
    
    /**
     * The main loading method. Opens a source directory and processes all
     * sub-directories it contains, recursively (by DFS). Only these
     * sub-directories are opened, that contains some files (not directories).
     * All hidden files are skipped.
     */
    public void loadFiles() {
        AbstractModelFiles.LOG.debug("Loading files...");
        
        if ((this.targetDir == null) || (this.sourceDir == null)) {
            throw new IllegalStateException();
        }
        
        // clear existing files
        
        this.clear();
        
        // the source directory is now ensured to be valid
        // load all images in the deepest sub-directories into the model
        
        AbstractModelFiles.LOG.debug("Loading all images...");
        
        int sourceFileCounter = 0;
        int nonEmptyBackupDirCounter = 0;
        
        // walk the deepest sub directories
        
        final List<File> allFiles = new LinkedList<File>();
        final List<File> deepestDirs = this.getDeepestSubDirectories(this.sourceDir);
        
        for (final File sourceSubDir : deepestDirs) {
            AbstractModelFiles.LOG.debug("Opening source sub-directory: " + sourceSubDir.getName());
            
            // skip empty files
            
            if (sourceSubDir.isHidden()) {
                AbstractModelFiles.LOG.debug("Skipping hidden file: " + sourceSubDir.getName());
                continue;
            }
            
            // skip empty sub-directories
            
            if ((sourceSubDir.listFiles() == null) || (sourceSubDir.listFiles().length < 1)) {
                AbstractModelFiles.LOG.debug("Skipping empty source sub-directory: " + sourceSubDir.getName());
                continue;
            }
            
            // create new file list and fill it with sorted files
            
            final List<File> sourceSubDirFiles = new ArrayList<File>(500);
            
            this.fillListOfFiles(sourceSubDir, sourceSubDirFiles);
            this.sortListOfFiles(sourceSubDirFiles);
            
            // remember all files
            
            allFiles.addAll(sourceSubDirFiles);
            
            // assign the file list to the current sub-directory
            
            this.putDirectoryFiles(sourceSubDir, sourceSubDirFiles);
            
            // create correct target sub directory
            // (the whole relative path structure from the source preserved)
            
            final File targetSubDir = SimpleFileUtils.replaceRootDir(sourceSubDir, this.sourceDir, this.targetDir);
            
            // prepare files meta info
            
            this.prepareMetaInfo(targetSubDir, sourceSubDirFiles);
            
            // check if there is a non empty backup directory
            
            if (SimpleFileUtils.getLastCardNumberInBackupDir(targetSubDir) != 0) {
                // increment non empty backup directory counter
                
                nonEmptyBackupDirCounter++;
            }
            
            // increment the file count
            
            sourceFileCounter += sourceSubDirFiles.size();
        }
        
        AbstractModelFiles.LOG.debug("Loaded " + sourceFileCounter + " file(s).");
        AbstractModelFiles.LOG.debug("There are " + nonEmptyBackupDirCounter + " non empty backup directories.");
        
        // validate all loaded files
        
        this.checkListOfAllFiles(allFiles);
        
        // validate the consistency
        
        if (sourceFileCounter != this.getFileCount()) {
            AbstractModelFiles.LOG.warn("File inconsistency: counter = " + sourceFileCounter + ", file count = " + this.getFileCount());
            this.clear();
            throw new IllegalStateException("Nekonzistentní informace o souborech. Proběhlo vyprázdnění paměti.");
        }
        
        // check the non empty backup directory counter
        
        if (nonEmptyBackupDirCounter != 0) {
            SimpleFrameUtils.showInformation(String.format("Pozor! Byly nalezeny neprázdné zálohové adresáře (počet: %d).", nonEmptyBackupDirCounter));
        }
    }
    
    /**
     * Searches for the deepest sub-directories in the provided root directory.
     * Deepest sub-directory is a directory that contains files only. The search
     * method is DFS (depth-first search) and is implemented using stack.
     * 
     * @param rootDir
     * root directory where to start search from
     * @return list of the deepest sub-directories in the provided directory
     */
    private List<File> getDeepestSubDirectories(final File rootDir) {
        final List<File> deepestDirs = new LinkedList<File>();
        final Stack<File> dirStack = new Stack<File>();
        
        AbstractModelFiles.LOG.debug("Starting DFS from: " + rootDir.getAbsolutePath());
        
        // initialize stack
        
        dirStack.push(rootDir);
        
        while (!dirStack.isEmpty()) {
            // take one file from the stack
            
            final File openedDir = dirStack.pop();
            
            // walk all files in the directory
            
            if (openedDir.isDirectory()) {
                boolean filesOnly = true;
                
                // for each file it contains, check if it is a directory
                
                for (final File openedDirSubDir : openedDir.listFiles()) {
                    // if so, push it into the stack
                    
                    if (openedDirSubDir.isDirectory()) {
                        filesOnly = false;
                        dirStack.push(openedDirSubDir);
                        AbstractModelFiles.LOG.debug(String.format("Found sub-directory '%s'.", openedDirSubDir.getName()));
                    }
                }
                
                // a directory with files only is a deepest one
                
                if (filesOnly) {
                    deepestDirs.add(openedDir);
                    AbstractModelFiles.LOG.debug(String.format("Found deepest sub-directory '%s'.", openedDir.getName()));
                }
            } else {
                AbstractModelFiles.LOG.warn(String.format("Non-directory '%s' was popped from the stack.", openedDir.getName()));
                throw new IllegalArgumentException("Procházet se mohou pouze adresáře.");
            }
        }
        
        AbstractModelFiles.LOG.debug("Count of deepest sub-directories: " + deepestDirs.size());
        return Collections.unmodifiableList(deepestDirs);
    }
    
    /**
     * Prepares meta information for provided list of files. This method adds
     * items to <code>filesInfo</code> variable. If the meta information of a
     * file is already set, the method throws an exception.
     * 
     * @param targetSubDir
     * target sub-directory
     * @param sourceSubDirFiles
     * source sub-directory files
     */
    protected abstract void prepareMetaInfo(File targetSubDir, List<File> sourceSubDirFiles);
    
    /**
     * Fills the target file list with all valid files from the provided image
     * directory. This directory should contain valid source image files only.
     * The order of added files is undefined.
     * 
     * @param directory
     * input directory with image files
     * @param targetList
     * target file list
     */
    protected abstract void fillListOfFiles(final File directory, final List<File> targetList);
    
    /**
     * Sorts the provided file list according to the number extracted from each
     * file. This number represents the card order in the drawer.
     * 
     * @param targetList
     * target file list
     */
    protected abstract void sortListOfFiles(final List<File> targetList);
    
    /**
     * Checks the provided list of all files loaded in the model. In the case
     * any error is found, it throws a runtime exception. The file list is
     * already sorted.
     * 
     * @param allFiles
     * list of all files loaded in the model
     */
    protected abstract void checkListOfAllFiles(List<File> allFiles);
    
    /**
     * Check whether the file list is empty.
     * 
     * @return <code>true</code> if the file list is empty, <code>false</code>
     * otherwise
     */
    public boolean isEmpty() {
        return this.getFileCount() == 0;
    }
    
    /**
     * Returns the file count.
     * 
     * @return file count
     */
    public int getFileCount() {
        return this.loadedFilesInfo.size();
    }
    
    /**
     * Returns a list of all loaded files. The list is ordered by a directory
     * and image file card number.
     * 
     * @return a list of all loaded files
     */
    public List<File> getLoadedFiles() {
        final List<File> list = new ArrayList<File>(500);
        
        for (final File dir : this.getLoadedDirectories()) {
            list.addAll(this.loadedFiles.get(dir));
        }
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Returns a list of meta information of all loaded files. The list order is
     * based on the order of <code>getLoadedFiles()</code> method.
     * 
     * @return a list of all file meta information
     */
    public List<T> getLoadedFilesInfo() {
        final List<T> list = new ArrayList<T>(500);
        
        for (final File file : this.getLoadedFiles()) {
            list.add(this.getFileInfo(file));
        }
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Returns the list of loaded directories.
     * 
     * @return list of loaded directories
     */
    public List<File> getLoadedDirectories() {
        final List<File> list = new LinkedList<File>(this.loadedFiles.keySet());
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Returns the list of loaded files for the given directory. The list is
     * ordered according to the extracted card number.
     * 
     * @param directory
     * parent directory
     * @return list of loaded files
     */
    public List<File> getLoadedFiles(final File directory) {
        if (!this.loadedFiles.containsKey(directory)) {
            AbstractModelFiles.LOG.error("Unknown directory: " + directory.getName());
            throw new NoSuchElementException("Záznam pro adresář nenalezen: " + directory.getName());
        }
        
        return Collections.unmodifiableList(this.loadedFiles.get(directory));
    }
    
    /**
     * Returns a meta information for the given file. If the meta information is
     * not available, an exception will be thrown.
     * 
     * @param file
     * file
     * @return file meta information
     */
    public T getFileInfo(final File file) {
        if (!this.loadedFilesInfo.containsKey(file)) {
            AbstractModelFiles.LOG.error("Unknown file: " + file.getName());
            throw new NoSuchElementException("Záznam pro soubor nenalezen: " + file.getName());
        }
        
        return this.loadedFilesInfo.get(file);
    }
    
    /**
     * Assigns a list of files to the given directory. If the directory was
     * already opened, the method will throw an exception.
     * 
     * @param directory
     * directory
     * @param files
     * file list
     */
    private void putDirectoryFiles(final File directory, final List<File> files) {
        if (this.loadedFiles.containsKey(directory)) {
            AbstractModelFiles.LOG.error("Directory already processed: " + directory.getName());
            throw new IllegalStateException("Složka je již zpracována: " + directory.getName());
        }
        
        this.loadedFiles.put(directory, files);
    }
    
    /**
     * Saves a meta information about the file. If the file already has some
     * info, the method will throw an exception.
     * 
     * @param file
     * file
     * @param info
     * file meta information
     */
    protected void putFileInfo(final File file, final T info) {
        if (this.loadedFilesInfo.containsKey(file)) {
            AbstractModelFiles.LOG.error("File already processed: " + file.getName());
            throw new IllegalStateException("Soubor je již zpracován: " + file.getName());
        }
        
        this.loadedFilesInfo.put(file, info);
    }
    
    /**
     * Robust method for renaming a file. If the rename fails, program asks to
     * do it again and again until the user chooses to end.
     * 
     * @param from
     * source file (must exist)
     * @param to
     * target file (must not exist)
     * @throws IOException
     * IO exception
     */
    public void safeRenameFile(final File from, final File to) throws IOException {
        AbstractModelFiles.LOG.debug(String.format("Safe renaming from '%s' to '%s'...", from.getAbsolutePath(), to.getAbsolutePath()));
        
        int attempt = 0;
        
        while (true) {
            attempt++;
            
            AbstractModelFiles.LOG.debug("Attempt " + attempt + "...");
            
            try {
                // validate files
                
                if (!from.exists()) {
                    ProcessorModelFiles.LOG.error(String.format("File '%s' cannot be renamed, because it does not exist.", from.getAbsolutePath()));
                    throw new IOException(String.format("Soubor '%s' nelze přejmenovat, protože neexistuje.", from.getAbsolutePath()));
                }
                
                if (to.exists()) {
                    ProcessorModelFiles.LOG.error(String.format("File '%s' cannot be renamed, because target file '%s' exists.", from.getAbsolutePath(), to.getAbsolutePath()));
                    throw new IOException(String.format("Soubor '%s' nelze přejmenovat, protože cílový soubor '%s' existuje.", from.getAbsolutePath(), to.getAbsolutePath()));
                }
                
                // rename files
                
                if (!from.renameTo(to)) {
                    ProcessorModelFiles.LOG.error(String.format("File rename from '%s' to '%s' failed.", from.getAbsolutePath(), to.getAbsolutePath()));
                    throw new IOException(String.format("Přejmenovaní souboru '%s' na '%s' selhalo.", from.getAbsolutePath(), to.getAbsolutePath()));
                }
                
                // success
                
                break;
            } catch (final IOException x) {
                if (attempt < 10) {
                    // less than 10 attempts: REPEAT automatically
                    
                    try {
                        Thread.sleep(attempt * 300l);
                    } catch (final InterruptedException e) {
                        // NOP
                    }
                } else {
                    // 10 attempts: ASK USER
                    
                    if (!SimpleFrameUtils.showConfirm(String.format(
                            "Přejmenování složky či souboru se nezdařilo. Možná je zdroj otevřený v jiném programu. " +
                                    "Chcete to zkusit znovu?" + Settings.LINE_END + Settings.LINE_END +
                                    "Zdroj: %s" + Settings.LINE_END +
                                    "Cíl: %s" + Settings.LINE_END + Settings.LINE_END +
                                    "Chybová hláška: %s",
                            from.getAbsolutePath(),
                            to.getAbsolutePath(),
                            x.getMessage()))) {
                        throw x;
                    }
                    
                    attempt = 0;
                }
            }
        }
    }
    
    /**
     * Robust method for text file loading. Returns an empty text in the case of
     * any error or an user cancels the process.
     * 
     * @param file
     * input file
     * @return the text content read from the file
     */
    public String safeLoadText(final File file) {
        AbstractModelFiles.LOG.debug(String.format("Safe reading text from '%s'...", file.getAbsolutePath()));
        
        int attempt = 0;
        
        while (true) {
            attempt++;
            
            AbstractModelFiles.LOG.debug("Attempt " + attempt + "...");
            
            if (file.exists() && file.canRead()) {
                try {
                    return SimpleFileUtils.readFileToString(file);
                } catch (final IOException x) {
                    AbstractModelFiles.LOG.warn("Error during file read: " + x.getMessage());
                }
            }
            
            if (attempt < 10) {
                // less than 10 attempts: REPEAT automatically
                
                try {
                    Thread.sleep(attempt * 300l);
                } catch (final InterruptedException e) {
                    // NOP
                }
            } else {
                // 10 attempts: ASK USER
                
                final String msg = String.format(
                        "Načtení textového souboru se nezdařilo. Možná je zdroj otevřený v jiném programu. " +
                                "Chcete to zkusit znovu?" + Settings.LINE_END + Settings.LINE_END +
                                "Zdroj: %s",
                        file.getAbsolutePath());
                
                if (!SimpleFrameUtils.showConfirm(msg)) {
                    return null;
                }
                
                attempt = 0;
            }
        }
    }
    
    /**
     * Robust method for cleaning a directory. Cleaning a directory means to
     * remove all its files (recursively) and remove the cleaned folder using
     * the two strings provided as parameters (start and end boundaries).
     * 
     * @param directory
     * directory to clean
     * @param firstProcessedCard
     * first processed card number
     * @param lastProcessedCard
     * last processed card number
     * @throws IOException
     * IO exception
     */
    public void safeCleanDirectory(final File directory, final String firstProcessedCard, final String lastProcessedCard) throws IOException {
        AbstractModelFiles.LOG.debug(String.format("Safe cleaning directory '%s' (%s/%s)...", directory.getAbsolutePath(), firstProcessedCard, lastProcessedCard));
        
        if ((firstProcessedCard == null) || (lastProcessedCard == null)) {
            throw new IllegalArgumentException("Nebyl zpracován ani jeden lístek, složku nelze finalizovat.");
        }
        
        if (directory.exists()) {
            // check directory
            
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException(String.format("Soubor '%s' není platná složka určená k finalizaci.", directory.getAbsolutePath()));
            }
            
            // only if the directory exists
            
            ProcessorModelFiles.LOG.debug("Finalizing directory: " + directory.getAbsolutePath());
            
            // clear directory contents
            
            FileUtils.cleanDirectory(directory);
            
            // rename directory to indicate its completion
            
            final String newName = String.format(
                    "%s%s_%s-%s",
                    SimpleFileUtils.PROCESSED_IMAGE_PREFIX,
                    directory.getName(),
                    firstProcessedCard,
                    lastProcessedCard);
            
            this.safeRenameFile(directory, new File(directory.getParentFile(), newName));
        }
    }
}
