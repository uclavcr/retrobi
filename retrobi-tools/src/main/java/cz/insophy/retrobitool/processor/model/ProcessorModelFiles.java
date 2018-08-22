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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.utils.library.SimpleFileUtils;
import cz.insophy.retrobi.utils.library.SimpleFrameUtils;
import cz.insophy.retrobitool.AbstractModelFiles;
import cz.insophy.retrobitool.ProcessorFileMetaInfo;

/**
 * Helper class that contains source directories with files.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ProcessorModelFiles extends AbstractModelFiles<ProcessorFileMetaInfo> {
    /**
     * logger instance
     */
    public static final Logger LOG = LoggerFactory.getLogger(ProcessorModelFiles.class);
    /**
     * selected catalog
     */
    private Catalog catalog;
    
    /**
     * Creates a new instance.
     */
    public ProcessorModelFiles() {
        super();
        
        this.catalog = null;
    }
    
    /**
     * Resets the empty flags of all loaded files.
     */
    protected void resetEmptyFlags() {
        for (final ProcessorFileMetaInfo file : this.getLoadedFilesInfo()) {
            file.resetEmptyCheck();
        }
    }
    
    /**
     * Sets the catalog for all loaded files.
     * 
     * @param catalog
     * catalog
     */
    protected void setCatalog(final Catalog catalog) {
        this.catalog = catalog;
        
        for (final ProcessorFileMetaInfo file : this.getLoadedFilesInfo()) {
            file.setCatalog(this.catalog);
        }
    }
    
    @Override
    protected void prepareMetaInfo(final File targetSubDir, final List<File> sourceSubDirFiles) {
        // --------------------
        // INITIALIZE VARIABLES
        // --------------------
        
        // first card number = number of the last card in target (if any)
        // card number = order of a card in the drawer starting from 1
        // page number = number of a page
        // last paper = total count of card papers (each paper has two pages)
        
        final int firstCardNumber = SimpleFileUtils.getLastCardNumberInBackupDir(targetSubDir);
        int cardNumber = firstCardNumber + 1;
        int pageNumber = 1;
        int lastPageNumber = -1;
        
        ProcessorModelFiles.LOG.debug("First card number: " + firstCardNumber);
        
        // walk all files and extract all meta information
        
        for (final File file : sourceSubDirFiles) {
            // get the paper count from the current file
            // page count = 2 * paper count (odd and even pages)
            
            final int newLastPage = 2 * SimpleFileUtils.extractPaperCountFromFile(file);
            
            if (lastPageNumber == -1) {
                // last page number is undefined
                // new page count must be saved right here
                
                lastPageNumber = newLastPage;
            } else {
                // the last page number is defined from the previous file
                // must check their equivalence
                
                if (newLastPage != lastPageNumber) {
                    throw new IllegalStateException(String.format("Chybný počet listů poblíž souboru '%s'." + Settings.LINE_END + Settings.LINE_END + "Cesta: %s", file.getName(), file.getAbsolutePath()));
                }
            }
            
            // create the file meta information
            
            this.putFileInfo(file, new ProcessorFileMetaInfo(
                    file,
                    this.catalog,
                    file.getParentFile().getName(),
                    firstCardNumber,
                    cardNumber,
                    pageNumber,
                    lastPageNumber,
                    "tif"));
            
            // check if the maximum number of pages was reached
            
            if (pageNumber == lastPageNumber) {
                // -----------------------
                // THE LAST PAGE OF A CARD
                // -----------------------
                
                // increment the card number
                // reset the page number
                // undefine the last page number
                
                cardNumber++;
                pageNumber = 1;
                lastPageNumber = -1;
            } else {
                // ---------------------------
                // NOT THE LAST PAGE OF A CARD
                // ---------------------------
                
                // check the range
                
                if (pageNumber > lastPageNumber) {
                    ProcessorModelFiles.LOG.warn("Invalid paper count near: " + file.getName());
                    throw new IllegalStateException(String.format("Chybný počet listů poblíž souboru '%s'." + Settings.LINE_END + Settings.LINE_END + "Cesta: %s", file.getName(), file.getAbsolutePath()));
                }
                
                // increment the page number only
                
                pageNumber++;
            }
        }
    }
    
    @Override
    protected void fillListOfFiles(final File directory, final List<File> targetList) {
        // check catalog
        
        if (this.catalog == null) {
            throw new IllegalStateException("Není vybrán žádný katalog.");
        }
        
        // process files
        
        for (final File subfile : directory.listFiles()) {
            // skip all hidden files
            
            if (subfile.isHidden()) {
                continue;
            }
            
            if (SimpleFileUtils.isValidSourceFile(subfile)) {
                // add file to the list
                
                ProcessorModelFiles.LOG.debug("Adding file: " + subfile.getName());
                targetList.add(subfile);
            } else {
                // invalid file
                
                ProcessorModelFiles.LOG.warn("Illegal file: " + subfile.getName());
                throw new IllegalArgumentException("Neplatný soubor: " + subfile.getAbsolutePath());
            }
        }
        
        // check the count of source files
        // the count must be even (2N = N papers from both sides)
        
        if (targetList.size() % 2 != 0) {
            throw new IllegalStateException(String.format(
                    "Počet obrázků v adresáři '%s' nesmí být lichý." + Settings.LINE_END + Settings.LINE_END + "Cesta: %s",
                    directory.getName(),
                    directory.getAbsolutePath()));
        }
    }
    
    @Override
    protected void sortListOfFiles(final List<File> targetList) {
        // sort image files according to the number they contain
        
        Collections.sort(targetList, new Comparator<File>() {
            @Override
            public int compare(final File o1, final File o2) {
                final BigDecimal n1 = SimpleFileUtils.extractNumberFromFile(o1);
                final BigDecimal n2 = SimpleFileUtils.extractNumberFromFile(o2);
                return n1.compareTo(n2);
            }
        });
    }
    
    @Override
    protected void checkListOfAllFiles(final List<File> allFiles) {
        // flag for checking the number count
        
        boolean hasValidDigitCount = true;
        
        // list of all numbers for continuity check and relevant files
        
        final List<BigDecimal> numbers = new LinkedList<BigDecimal>();
        final List<BigDecimal> invalidNumbers = new LinkedList<BigDecimal>();
        
        // walk all files and extract the information needed
        
        for (final File file : allFiles) {
            // check digit count
            
            if (SimpleFileUtils.extractDigitCount(file) != Settings.SCAN_DIGIT_COUNT) {
                ProcessorModelFiles.LOG.debug(String.format("Invalid digit count in file '%s'.", file.getName()));
                hasValidDigitCount = false;
            }
            
            // extract number
            
            numbers.add(SimpleFileUtils.extractNumberFromFile(file));
        }
        
        if (!hasValidDigitCount) {
            SimpleFrameUtils.showInformation("V seznamu je alespoň jeden soubor s nesprávným počtem číslic.");
        }
        
        // check number range continuity
        
        Collections.sort(numbers);
        
        BigDecimal nextNr = null;
        
        for (final BigDecimal number : numbers) {
            if (nextNr != null) {
                if (!number.equals(nextNr)) {
                    invalidNumbers.add(number);
                }
            }
            
            nextNr = number.add(BigDecimal.ONE);
        }
        
        if (invalidNumbers.size() > 5) {
            SimpleFrameUtils.showInformation(String.format("Nesouvislosti v číselné řadě: %s, atd.", invalidNumbers.subList(0, 10).toString()));
        } else if (invalidNumbers.size() > 0) {
            SimpleFrameUtils.showInformation(String.format("Nesouvislosti v číselné řadě: %s", invalidNumbers.toString()));
        }
    }
    
    /**
     * Writes a backup file (if it does not exist).
     * 
     * @param file
     * file meta information
     * @throws IOException
     * I/O exception
     */
    protected void prepareBackupFile(final ProcessorFileMetaInfo file) throws IOException {
        ProcessorModelFiles.LOG.debug(String.format("Preparing backup file '%s'.", file.getFile().getName()));
        
        if (SimpleFileUtils.isValidBackupFile(file.getFile())) {
            // file is already processed, just return it
            
            ProcessorModelFiles.LOG.debug("Already processed.");
            return;
        }
        
        // create backup file
        // the directory structure must be the same as source
        // (only the root is different)
        
        final File targetDir = SimpleFileUtils.replaceRootDir(
                file.getFile().getParentFile(),
                this.getSourceDirectory(),
                this.getTargetDirectory());
        
        // prepare target directory structure
        
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IllegalStateException(String.format("Chyba při vytváření cest:" + Settings.LINE_END + "%s", targetDir.getAbsolutePath()));
        }
        
        // create target file
        
        final File backupImageFile = new File(
                targetDir,
                SimpleFileUtils.produceImageFileName(
                        this.catalog,
                        file.getBatch(),
                        file.getNumber(),
                        file.getPage(),
                        "tif",
                        file.isEmpty()));
        
        if (backupImageFile.exists()) {
            ProcessorModelFiles.LOG.debug(String.format("Backup file '%s' already exists.", backupImageFile.getAbsolutePath()));
            throw new IllegalStateException("Záložní soubor se stejným názvem nesmí existovat.");
        }
        
        // file must be copied
        
        SimpleFileUtils.copyFile(file.getFile(), backupImageFile, true);
    }
}
