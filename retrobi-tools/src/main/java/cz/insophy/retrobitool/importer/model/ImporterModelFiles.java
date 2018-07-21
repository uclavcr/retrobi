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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.utils.library.SimpleFileUtils;
import cz.insophy.retrobitool.AbstractModelFiles;
import cz.insophy.retrobitool.ImporterFileMetaInfo;

/**
 * Helper class that contains backup directories with files.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImporterModelFiles extends AbstractModelFiles<ImporterFileMetaInfo> {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImporterModelFiles.class);
    
    /**
     * Creates a new instance.
     */
    public ImporterModelFiles() {
        super();
    }
    
    /**
     * Returns a list of loaded files without OCR loaded.
     * 
     * @return a list of files without OCR
     */
    protected List<ImporterFileMetaInfo> getLoadedFilesWithoutOcr() {
        final List<ImporterFileMetaInfo> files = new LinkedList<ImporterFileMetaInfo>();
        
        for (final ImporterFileMetaInfo fileinf : this.getLoadedFilesInfo()) {
            if (!fileinf.hasOcr()) {
                // OCR missing, add the file to the list
                
                ImporterModelFiles.LOG.warn("OCR missing: " + fileinf.getFile().getName());
                files.add(fileinf);
            }
        }
        
        return Collections.unmodifiableList(files);
    }
    
    @Override
    protected void prepareMetaInfo(final File targetSubDir, final List<File> sourceSubDirFiles) {
        ImporterModelFiles.LOG.debug("Preparing file meta info...");
        ImporterModelFiles.LOG.debug("Target sub-directory: " + targetSubDir);
        ImporterModelFiles.LOG.debug("Source files: " + sourceSubDirFiles.size());
        
        for (final File file : sourceSubDirFiles) {
            // extract and save the file meta information
            
            ImporterModelFiles.LOG.debug("Extracting file info: " + file.getName());
            
            this.putFileInfo(file, new ImporterFileMetaInfo(
                    file.getParentFile(),
                    file,
                    (file.getParentFile().getParentFile() == null)
                            ? null
                            : file.getParentFile().getParentFile().getName(),
                    SimpleFileUtils.extractCatalogFromFile(file),
                    file.getParentFile().getName(),
                    SimpleFileUtils.extractCardNumberFromFile(file),
                    SimpleFileUtils.extractPageNumberFromFile(file),
                    this.getOcrText(file)));
        }
    }
    
    @Override
    protected void fillListOfFiles(final File directory, final List<File> targetList) {
        for (final File subfile : directory.listFiles()) {
            // skip empty backup images
            
            if (SimpleFileUtils.isValidEmptyBackupFile(subfile)) {
                continue;
            }
            
            // skip OCR text files
            
            if (SimpleFileUtils.isValidOCRFile(subfile)) {
                continue;
            }
            
            // skip hidden files (like thumbs.db)
            
            if (subfile.isHidden()) {
                continue;
            }
            
            if (SimpleFileUtils.isValidBackupFile(subfile) || SimpleFileUtils.isValidOutputFile(subfile)) {
                // valid backup or output file
                // add file to the list
                
                ImporterModelFiles.LOG.debug("Adding file: " + subfile.getName());
                targetList.add(subfile);
            } else {
                // invalid file
                
                ImporterModelFiles.LOG.warn("Invalid file: " + subfile.getName());
                throw new IllegalArgumentException("Neplatný soubor: " + subfile.getAbsolutePath());
            }
        }
    }
    
    @Override
    protected void sortListOfFiles(final List<File> targetList) {
        ImporterModelFiles.LOG.debug("Sorting list of files (" + targetList.size() + ")...");
        
        // sort image files according to the card number and page number
        
        Collections.sort(targetList, new Comparator<File>() {
            @Override
            public int compare(final File o1, final File o2) {
                int n1 = SimpleFileUtils.extractCardNumberFromFile(o1);
                int n2 = SimpleFileUtils.extractCardNumberFromFile(o2);
                
                if (n1 != n2)
                {
                    return n1 - n2;
                }
                
                n1 = SimpleFileUtils.extractPageNumberFromFile(o1);
                n2 = SimpleFileUtils.extractPageNumberFromFile(o2);
                
                return n1 - n2;
            }
        });
    }
    
    @Override
    protected void checkListOfAllFiles(final List<File> allFiles) {
        // nothing
    }
    
    /**
     * Returns the OCR file contents (if the file exists and is readable) or
     * <code>null</code> if no OCR file exists. The text file with OCR must have
     * the same name as the image file but with the TXT extension.
     * 
     * @param file
     * card image file
     * @return OCR file contents or <code>null</code>
     */
    private String getOcrText(final File file) {
        // get path to the expected text file
        
        final File textFile = SimpleFileUtils.changeExtension(file, "txt");
        
        ImporterModelFiles.LOG.debug("Trying to load the OCR file: " + textFile.getAbsolutePath());
        
        // check if the text file exists and if so, read it into the string
        
        if (textFile.exists()) {
            final String text = this.safeLoadText(textFile);
            
            if (text != null) {
                ImporterModelFiles.LOG.debug("Loaded " + text.length() + " byte(s) of OCR text.");
                return text;
            }
        }
        
        ImporterModelFiles.LOG.debug("No OCR file found.");
        return null;
    }
}
