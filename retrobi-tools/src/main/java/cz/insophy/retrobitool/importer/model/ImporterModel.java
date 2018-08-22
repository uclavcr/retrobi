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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleFileUtils;
import cz.insophy.retrobi.utils.library.SimpleFrameUtils;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;
import cz.insophy.retrobitool.ImporterFileMetaInfo;

/**
 * Main importer model. It realizes all the functionality of the import tool and
 * provides interface for listeners. Its main purpose can be described as a
 * smart conversion from files to cards, setting their values based on user
 * input and correct upload to the database. Several validations and checks must
 * be done during the process.
 * 
 * @author Vojtěch Hordějčuk
 */
public class ImporterModel {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImporterModel.class);
    /**
     * list of listeners
     */
    private final List<ImporterModelListener> listeners;
    /**
     * loaded files
     */
    private final ImporterModelFiles files;
    /**
     * loaded cards
     */
    private final ImporterModelCards cards;
    /**
     * log output directory
     */
    private File logDirectory;
    /**
     * cancel flag
     */
    private Boolean cancel;
    /**
     * pause flag
     */
    private Boolean pause;
    /**
     * cancel flag lock
     */
    private final Object cancelLock;
    /**
     * pause flag lock
     */
    private final Object pauseLock;
    
    /**
     * Creates a new instance.
     * 
     * @throws GeneralRepositoryException
     * exception
     */
    public ImporterModel() throws GeneralRepositoryException {
        this.listeners = new LinkedList<ImporterModelListener>();
        this.files = new ImporterModelFiles();
        this.cards = new ImporterModelCards();
        this.logDirectory = null;
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
    public void addListener(final ImporterModelListener listener) {
        this.listeners.add(listener);
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the list of all loaded files.
     * 
     * @return file list
     */
    public List<File> getFiles() {
        return Collections.unmodifiableList(this.files.getLoadedFiles());
    }
    
    /**
     * Returns the list of cards.
     * 
     * @return list of cards
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(this.cards.getLoadedCards());
    }
    
    /**
     * Returns the file meta information.
     * 
     * @param file
     * file
     * @return file meta information
     */
    public ImporterFileMetaInfo getFileMetaInfo(final File file) {
        return this.files.getFileInfo(file);
    }
    
    /**
     * Returns the log directory.
     * 
     * @return log directory
     */
    public File getLogDirectory() {
        if (this.logDirectory == null) {
            throw new IllegalStateException("Výstupní adresář pro logy není specifikován.");
        }
        
        return this.logDirectory;
    }
    
    // =======
    // ACTIONS
    // =======
    
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
     * Sets the log output directory.
     * 
     * @param dir
     * log output directory
     */
    public void setLogDirectory(final File dir) {
        // check the target directory
        
        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException("Neplatná složka pro zálohu: " + dir.getAbsolutePath());
        }
        
        this.logDirectory = dir;
    }
    
    /**
     * Opens a source directory and loads all files in it.
     */
    public void loadFiles() {
        // clear old files
        
        this.clear();
        
        // load new files
        
        this.files.loadFiles();
        
        // notify listeners
        
        this.fireFilesChanged();
        
        // regenerate the card list
        
        this.cards.createFromFiles(this.files);
        
        // update cards properties
        
        this.cards.updateProperties();
        
        // notify listeners
        
        this.fireCardsChanged();
        
        // check files for missing OCRs
        
        final List<ImporterFileMetaInfo> missing = this.files.getLoadedFilesWithoutOcr();
        
        if (!missing.isEmpty()) {
            if (missing.size() <= 12) {
                // reasonably small list of files, display
                
                final StringBuilder temp = new StringBuilder(500);
                
                for (final ImporterFileMetaInfo fileinf : missing) {
                    temp.append(fileinf.getFile().getName() + Settings.LINE_END);
                }
                
                SimpleFrameUtils.showInformation("U těchto souborů chybí OCR:" + Settings.LINE_END + Settings.LINE_END + temp.toString().trim());
            } else {
                // too many files, do not display
                
                SimpleFrameUtils.showInformation("U " + missing.size() + " souborů chybí OCR.");
            }
        }
    }
    
    /**
     * Uploads the card to the database server.
     * 
     * @throws GeneralRepositoryException
     * repository error
     */
    public void uploadCards() throws GeneralRepositoryException {
        if (this.cards.isEmpty()) {
            SimpleFrameUtils.showInformation("Není co odeslat.");
            return;
        }
        
        synchronized (this.cancelLock) {
            this.cancel = false;
        }
        
        synchronized (this.pauseLock) {
            this.pause = false;
        }
        
        // prepare log output directory
        
        if (this.logDirectory == null) {
            throw new IllegalStateException("Není určen výstupní adresář pro logy.");
        }
        
        // start upload
        
        this.fireUploadStarted();
        
        // upload all cards
        
        final int total = this.cards.getLoadedCards().size();
        int walked = 0;
        int skipped = 0;
        int errors = 0;
        int done = 0;
        
        this.fireUploadStatusChanged("Lístků ke zpracování: " + total, null, null, done, total);
        
        for (final Card card : this.cards.getLoadedCards()) {
            this.fireUploadStatusChanged(String.format("Zpracovávám '%s'...", card.toString()), card, null);
            
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
                    ImporterModel.LOG.debug("Processing cancelled.");
                    break;
                }
            }
            
            try {
                // check if there is at least one card image available
                
                if ((this.cards.getFiles(card) == null) || this.cards.getFiles(card).isEmpty()) {
                    ImporterModel.LOG.warn("No image for card: " + card);
                    this.fireUploadStatusChanged("U lístku chybí obrázek, proces bude přerušen.", card, null);
                    throw new IllegalStateException("No image for card: " + card);
                }
                
                // check if card file are already remote
                
                if (this.mustBeUploaded(card)) {
                    // save card if it passed
                    
                    ImporterModel.LOG.debug("Saving card...");
                    this.fireUploadStatusChanged("Vytvářím nový lístek v databázi...", card, null);
                    RetrobiApplication.db().getCardRepository().addCard(card);
                    this.fireUploadStatusChanged(String.format("Lístek byl uložen do databáze s ID '%s'.", card.getId()), card, null);
                    
                    // prepare (if necessary) and upload card files
                    
                    ImporterModel.LOG.debug(String.format("Uploading files of card '%s'...", card.getId()));
                    
                    try {
                        for (final ImporterFileMetaInfo file : this.cards.getFiles(card)) {
                            this.fireUploadStatusChanged(String.format("Zpracování obrázku '%s'...", file.getFile().getAbsolutePath()), card, file.getFile());
                            
                            RetrobiApplication.db().getCardImageRepository().addImageToCard(
                                    card,
                                    this.prepareImageFile(file),
                                    ImageFlag.produceImageName(file.getPage(), ImageFlag.ORIGINAL),
                                    "image/png");
                            
                            this.fireUploadStatusChanged("OK: Obrázek odeslán.", card, file.getFile(), walked, total);
                        }
                        
                        ImporterModel.LOG.debug("Card and its files uploaded.");
                        this.fireUploadStatusChanged("OK: Zpracování lístku dokončeno.", card, null, walked, total);
                        done++;
                    } catch (final Exception x) {
                        // delete invalid card
                        
                        ImporterModel.LOG.error("Error during image upload, removing card...");
                        RetrobiApplication.db().getCardRepository().deleteCard(card);
                        
                        // re-throw exception
                        
                        throw x;
                    }
                } else {
                    // do not save card, just process the files
                    
                    this.fireUploadStatusChanged("Lístek nebude odeslán. Zpracovávám obrázky...", card, null);
                    
                    for (final ImporterFileMetaInfo file : this.cards.getFiles(card)) {
                        this.fireUploadStatusChanged(String.format("Připravuji obrázek '%s'...", file.getFile().getAbsolutePath()), card, file.getFile());
                        this.prepareImageFile(file);
                        this.fireUploadStatusChanged("Obrázek připraven.", card, file.getFile());
                    }
                    
                    ImporterModel.LOG.debug("Card was already uploaded, skipping.");
                    this.fireUploadStatusChanged("OK: Lístek přeskočen.", card, null, walked, total);
                    skipped++;
                }
            } catch (final Exception x) {
                // some exception occurred during the process
                
                x.printStackTrace();
                
                ImporterModel.LOG.warn("Error during upload.");
                ImporterModel.LOG.warn("Error class: " + x.getClass().getName());
                ImporterModel.LOG.warn("Error message: " + x.getMessage());
                ImporterModel.this.fireUploadStatusChanged(String.format("CHYBA: %s (%s)", x.getMessage(), x.getClass().getName()), null, null, walked, total);
                errors++;
            } finally {
                walked++;
            }
        }
        
        // show overall statistics
        
        this.fireUploadStatusChanged("Celkem lístků: " + walked, null, null);
        this.fireUploadStatusChanged("Odesláno: " + done, null, null);
        this.fireUploadStatusChanged("Přeskočeno: " + skipped, null, null);
        this.fireUploadStatusChanged("Chyb: " + errors, null, null);
        
        this.fireUploadFinished();
        
        SimpleFrameUtils.showInformation(String.format(
                "Celkem lístků: %d" + Settings.LINE_END + Settings.LINE_END + "- odesláno: %d" + Settings.LINE_END + "- přeskočeno: %d" + Settings.LINE_END + "- chyb: %d",
                walked,
                done,
                skipped,
                errors));
        
        // clear all
        
        this.clear();
    }
    
    /**
     * Prepares a target file. The target file is to be saved in the database as
     * an attachment of a card. The target image should be about 50 kB in size.
     * If the image is already processed (based on the file extension), the
     * method simply returns the input file.
     * 
     * @param file
     * input image file
     * @return target file
     * @throws IOException
     * IO error
     */
    private File prepareImageFile(final ImporterFileMetaInfo file) throws IOException {
        ImporterModel.LOG.debug(String.format("Preparing target file '%s'.", file.getFile().getName()));
        
        if (SimpleFileUtils.isValidOutputFile(file.getFile())) {
            // file is already processed, just return it
            
            this.fireUploadStatusChanged(String.format("Soubor '%s' je výstupní, není nutné jej konvertovat.", file.getFile().getAbsolutePath()), null, file.getFile());
            ImporterModel.LOG.debug("Already processed [source].");
            return file.getFile();
        }
        
        // create target file
        // the directory structure must be the same as source
        // (only the root is different)
        
        final File targetDir = SimpleFileUtils.replaceRootDir(
                file.getFile().getParentFile(),
                this.files.getSourceDirectory(),
                this.files.getTargetDirectory());
        
        // prepare target directory structure
        
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new IllegalStateException(String.format("Chyba při vytváření cest:" + Settings.LINE_END + "%s", targetDir.getAbsolutePath()));
            }
        }
        
        // ------------------
        // PREPARE IMAGE FILE
        // ------------------
        
        // create target file path
        // target file has the same name as the original file but extension
        
        final File targetImageFile = new File(targetDir, SimpleFileUtils.changeExtension(file.getFile(), "png").getName());
        
        if (!targetImageFile.exists()) {
            // file must be processed first
            // resize it and convert to PNG
            // then save the file to the target directory
            
            // load image
            
            this.fireUploadStatusChanged("Načítám a konvertuji TIFF...", null, file.getFile());
            ImporterModel.LOG.debug("Loading image from TIFF...");
            BufferedImage image = SimpleImageUtils.loadImageFromFile(file.getFile());
            
            // resize image
            
            ImporterModel.LOG.debug("Resizing...");
            this.fireUploadStatusChanged("Vytvářím náhled obrázku TIFF...", null, file.getFile());
            image = SimpleImageUtils.makeThumbnailImage(image, Settings.TARGET_IMAGE_WIDTH, false);
            
            // save as PNG
            
            ImporterModel.LOG.debug("Saving final PNG...");
            this.fireUploadStatusChanged("Ukládám výsledné PNG...", null, targetImageFile);
            SimpleImageUtils.saveImageToPngFile(image, targetImageFile);
            
            // fire event
            
            this.fireUploadStatusChanged(String.format("Konverze '%s' na '%s' proběhla úspěšně.", file.getFile().getAbsolutePath(), targetImageFile.getAbsolutePath()), null, file.getFile());
            ImporterModel.LOG.debug(String.format("Converted to file '%s'.", targetImageFile.getAbsolutePath()));
        } else {
            // target image already exists
            
            this.fireUploadStatusChanged(String.format("Výstupní soubor '%s' existuje, byl již zkonvertován.", targetImageFile.getAbsolutePath()), null, file.getFile());
            ImporterModel.LOG.debug("Already processed [target].");
        }
        
        // -------------
        // COPY OCR FILE
        // -------------
        
        // create OCR file paths
        
        final File sourceTextFile = SimpleFileUtils.changeExtension(file.getFile(), "txt");
        final File targetTextFile = new File(targetImageFile.getParentFile(), sourceTextFile.getName());
        
        if (!targetTextFile.exists()) {
            // OCR file must be copied to the new location
            
            ImporterModel.LOG.debug("Copying OCR file....");
            ImporterModel.LOG.debug("Source OCR file: " + sourceTextFile);
            ImporterModel.LOG.debug("Target OCR file: " + targetTextFile);
            
            if (SimpleFileUtils.isValidOCRFile(sourceTextFile)) {
                // OCR valid
                
                ImporterModel.LOG.debug(String.format("Copying OCR file '%s' to '%s'...", sourceTextFile.getAbsolutePath(), targetTextFile.getAbsolutePath()));
                this.fireUploadStatusChanged(String.format("Kopírování souboru OCR '%s' do '%s'...", sourceTextFile.getAbsolutePath(), targetTextFile.getAbsolutePath()), null, sourceTextFile);
                SimpleFileUtils.copyFile(sourceTextFile, targetTextFile, true);
                
            } else {
                // OCR not found or invalid
                
                ImporterModel.LOG.debug("OCR file was not found, nothing to copy.");
            }
        } else {
            // target OCR already exists
            
            ImporterModel.LOG.debug("OCR already exists.");
        }
        
        // return the output file
        
        return targetImageFile;
    }
    
    /**
     * Checks if the card must be added and its files uploaded. This is a
     * failsafe that no two identical files are uploaded in the database twice.
     * The identity of files means that the files have the same names. Extension
     * is ignored. This method can interact with user.
     * 
     * @param card
     * a card to check
     * @return <code>false</code> if the card is in the database and not to be
     * uploaded again, <code>true</code> otherwise
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    private boolean mustBeUploaded(final Card card) throws GeneralRepositoryException, NotFoundRepositoryException {
        // check if any of the card files is already remote
        // (both possible extensions must be checked)
        
        final Set<String> dangerousCardIds = new HashSet<String>();
        
        // get local card files
        
        final List<ImporterFileMetaInfo> localCardFiles = this.cards.getFiles(card);
        
        // find all possible conflicting cards
        
        for (final ImporterFileMetaInfo localCardFile : localCardFiles) {
            ImporterModel.LOG.debug(String.format("Checking file '%s'...", localCardFile.getFile().getName()));
            
            // generate file names
            
            final String fTif = SimpleFileUtils.changeExtension(localCardFile.getFile(), "tif").getName();
            final String fPng = SimpleFileUtils.changeExtension(localCardFile.getFile(), "png").getName();
            
            ImporterModel.LOG.debug(String.format("Files: %s / %s", fTif, fPng));
            
            // get cards containing at least one of these file names
            
            try {
                // add cards with the TIF file
                final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsWithFile(fTif);
                dangerousCardIds.addAll(cardIds);
            } catch (final NotFoundRepositoryException e) {
                ImporterModel.LOG.error("Error during card fetching (for TIF file): " + e.getMessage());
            }
            
            try {
                // add cards with the PNG file
                final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsWithFile(fPng);
                dangerousCardIds.addAll(cardIds);
            } catch (final NotFoundRepositoryException e) {
                ImporterModel.LOG.error("Error during card fetching (for PNG file): " + e.getMessage());
            }
        }
        
        switch (dangerousCardIds.size()) {
            case 0:
                // no conflicts at all
                
                ImporterModel.LOG.debug("No conflicts at all, card will be uploaded.");
                this.fireUploadStatusChanged("Lístek bude odeslán, nebyly nalezeny žádné duplikáty.", card, null);
                return true;
            case 1:
                // just one card
                
                ImporterModel.LOG.debug("Just one conflicting card.");
                final Card dangerousCard = RetrobiApplication.db().getCardRepository().getCard(dangerousCardIds.iterator().next());
                ImporterModel.LOG.debug("Conflicting card: " + dangerousCard.toString());
                
                if (dangerousCard.getAttachmentCount() != localCardFiles.size()) {
                    ImporterModel.LOG.debug("Conflicting card has different number of attachments. Will be removed");
                    this.fireUploadStatusChanged("VAROVÁNÍ! Lístek v databázi má jiný počet příloh než lístek na disku. Lístek bude smazán a nahrán znovu.", dangerousCard, null);
                    this.fireUploadStatusChanged(String.format("Mazání nekompletního lístku '%s'...", dangerousCard.toString()), dangerousCard, null);
                    RetrobiApplication.db().getCardRepository().deleteCard(dangerousCard);
                    ImporterModel.LOG.debug("Conflicting card removed, card will be uploaded.");
                    this.fireUploadStatusChanged("Nekompletní lístek byl smazán a bude nahrán znovu.", dangerousCard, null);
                    return true;
                }
                
                ImporterModel.LOG.debug("Conflicting card has the same number of attachments and will be kept. Do not upload.");
                this.fireUploadStatusChanged("Lístek v databázi má stejný počet příloh jako nahrávaný, bude tedy přeskočen.", dangerousCard, null);
                return false;
            default:
                // more cards
                
                ImporterModel.LOG.debug("More conflicting card. Requires confirmation.");
                this.fireUploadStatusChanged(String.format("Bylo nalezeno více duplikátních lístků (%d). Uživatel požádán o potvrzení.", dangerousCardIds.size()), null, null);
                
                final boolean skip = SimpleFrameUtils.showConfirm(String.format(
                        "Některý z obrázků se v databázi vyskytuje, a to u %d lístků. Chcete aktuální lístek přeskočit (tzn. neukládat)?",
                        dangerousCardIds.size()));
                
                if (skip) {
                    ImporterModel.LOG.debug("User decided to SKIP the card.");
                    this.fireUploadStatusChanged("Uživatel se rozhodl lístek přeskočit.", card, null);
                    return false;
                }
                
                ImporterModel.LOG.debug("User decided to UPLOAD the card.");
                this.fireUploadStatusChanged("Uživatel se rozhodl lístek nahrát.", card, null);
                return true;
        }
    }
    
    /**
     * Clears all the loaded cached objects.
     */
    private void clear() {
        synchronized (this.cancelLock) {
            this.cancel = false;
        }
        
        synchronized (this.pauseLock) {
            this.pause = false;
        }
        
        this.files.clear();
        this.cards.clear();
        this.fireFilesChanged();
        this.fireCardsChanged();
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
     * Fire event "files changed" to all listeners.
     */
    private void fireFilesChanged() {
        for (final ImporterModelListener listener : this.listeners) {
            listener.filesChanged();
        }
    }
    
    /**
     * Fire event "cards changed" to all listeners.
     */
    private void fireCardsChanged() {
        for (final ImporterModelListener listener : this.listeners) {
            listener.cardsChanged();
        }
    }
    
    /**
     * Fire event "upload started" to all listeners.
     */
    private void fireUploadStarted() {
        for (final ImporterModelListener listener : this.listeners) {
            listener.uploadStarted();
        }
    }
    
    /**
     * Fire event "upload finished" to all listeners.
     */
    private void fireUploadFinished() {
        for (final ImporterModelListener listener : this.listeners) {
            listener.uploadFinished();
        }
    }
    
    /**
     * Fire event "upload status changed" to all listeners.
     * 
     * @param message
     * message text
     * @param card
     * relevant card (or <code>null</code>)
     * @param file
     * relevant file (or <code>null</code>)
     */
    private void fireUploadStatusChanged(final String message, final Card card, final File file) {
        for (final ImporterModelListener listener : this.listeners) {
            listener.uploadStatusChanged(message, card, file);
        }
    }
    
    /**
     * Fire event "upload status changed" (with statistics) to all listeners.
     * 
     * @param message
     * message text
     * @param card
     * relevant card (or <code>null</code>)
     * @param file
     * relevant file (or <code>null</code>)
     * @param done
     * done cards
     * @param total
     * total cards
     */
    private void fireUploadStatusChanged(final String message, final Card card, final File file, final int done, final int total) {
        for (final ImporterModelListener listener : this.listeners) {
            listener.uploadStatusChanged(message, card, file, done, total);
        }
    }
}
