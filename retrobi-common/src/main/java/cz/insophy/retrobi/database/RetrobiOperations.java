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

package cz.insophy.retrobi.database;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Cardset;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.exception.GeneralOperationException;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.utils.CSVHistoryLogger;
import cz.insophy.retrobi.utils.CzechAlphabet;
import cz.insophy.retrobi.utils.Triple;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;
import cz.insophy.retrobi.utils.library.SimpleImageUtils;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Class that contains complex business operations.
 * 
 * @author Vojtěch Hordějčuk
 */
final public class RetrobiOperations {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(RetrobiOperations.class);
    
    // =====
    // USERS
    // =====
    
    /**
     * Registers a new user using the given e-mail and password. After a
     * successful registration, an e-mail is sent to the user with the details.
     * 
     * @param newUser
     * user to register
     * @param password
     * user password
     * @param autologinUrl
     * auto-login page URL (external form)
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void registerUser(final User newUser, final String password, final String autologinUrl, final CSVHistoryLogger logger) throws GeneralOperationException {
        try {
            // check if the login chosen is free
            
            if (!RetrobiApplication.db().getUserRepository().isUserLoginFree(newUser.getLogin())) {
                throw new GeneralOperationException("Bohužel, tento login je již obsazen.");
            }
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během ověřování jedinečnosti loginu.", x);
        }
        
        try {
            // check if the e-mail chosen is free
            
            if (!RetrobiApplication.db().getUserRepository().isUserEmailFree(newUser.getEmail())) {
                throw new GeneralOperationException("Bohužel, tento e-mail je již obsazen.");
            }
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během ověřování jedinečnosti e-mailu.", x);
        }
        
        try {
            // save the user into the database
            
            RetrobiApplication.db().getUserRepository().addUser(newUser);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během ukládání nového uživatele.", x);
        }
        
        try {
            // send an e-mail with information
            
            RetrobiApplication.db().getMessageRepository().sendEmail(
                    newUser.getEmail(),
                    "Informace o registraci",
                    String.format("" +
                            "<p>Vaše registrace na web Retrospektivní bibliografie proběhla v pořádku. Zde jsou Vaše registrační údaje:</p>" +
                            "<ul>" +
                            "<li><b>Login:</b> %s</li>" +
                            "<li><b>Heslo:</b> %s</li>" +
                            "</ul>" +
                            "<p>Ihned se <a href='%s'>můžete přihlásit</a> a změnit své heslo.</p>",
                            newUser.getLogin(),
                            password,
                            autologinUrl));
        } catch (final AddressException x) {
            throw new GeneralOperationException("Chyba v cílové e-mailové adrese.", x);
        } catch (final MessagingException x) {
            throw new GeneralOperationException("Chyba při transportu zprávy.", x);
        }
        
        // log the change
        
        try {
            RetrobiApplication.db().getMessageRepository().eventUserRegistered(logger, newUser);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
        }
    }
    
    /**
     * Removes an existing user from the database.
     * 
     * @param userToRemove
     * user to remove
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void removeUser(final User userToRemove, final CSVHistoryLogger logger) throws GeneralOperationException {
        try {
            // remove user cardsets
            
            final List<Triple<String, String, String>> cardsets = RetrobiApplication.db().getCardsetRepository().getCardsetIds(userToRemove.getId());
            
            for (final Triple<String, String, String> cardset : cardsets) {
                final Cardset cardsetToRemove = RetrobiApplication.db().getCardsetRepository().getCardset(cardset.getFirst());
                RetrobiApplication.db().getCardsetRepository().deleteCardset(cardsetToRemove);
            }
            
            // remove user comments
            
            final List<String> commentIds = RetrobiApplication.db().getCommentRepository().getCommentIdsForUser(userToRemove.getId());
            final List<Comment> commentsToRemove = RetrobiApplication.db().getCommentRepository().getComments(commentIds);
            
            for (final Comment commentToRemove : commentsToRemove) {
                RetrobiApplication.db().getCommentRepository().deleteComment(commentToRemove);
            }
            
            // remove the user from the database
            
            RetrobiApplication.db().getUserRepository().deleteUser(userToRemove);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během mazání uživatele.", x);
        } catch (final NotFoundRepositoryException x) {
            throw new GeneralOperationException("Chyba během mazání uživatele.", x);
        }
        
        // log the change
        
        try {
            RetrobiApplication.db().getMessageRepository().eventUserRemoved(logger, userToRemove);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
        }
        
        try {
            // send an e-mail with information
            
            RetrobiApplication.db().getMessageRepository().sendEmail(
                    userToRemove.getEmail(),
                    "Informace o zrušení registrace",
                    "<p>Váš profil byl smazán. Děkujeme Vám za použití našeho systému.</p>");
        } catch (final AddressException x) {
            throw new GeneralOperationException("Chyba v cílové adrese.", x);
        } catch (final MessagingException x) {
            throw new GeneralOperationException("Chyba při transportu zprávy.", x);
        }
    }
    
    // =====
    // CARDS
    // =====
    
    /**
     * Does the automatic segmentation using a simple algorithm. If the
     * segmentation is approved, saves the card.
     * 
     * @param cardId
     * ID of card to modify
     * @param user
     * user that did the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void doAutomaticSegments(final String cardId, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // load the card
        
        final Card card = RetrobiOperations.loadCard(cardId);
        
        // check card state
        
        if (card.getState().equals(CardState.CLOSED)) {
            throw new GeneralOperationException("Segmentaci nelze uložit, lístek je již uzavřený.");
        }
        
        // get the old value
        
        final String oldValue = SimpleSegmentUtils.segmentsToString(card, Settings.SYMBOL_SEGMENT);
        
        // extract card segments
        
        if (!SimpleSegmentUtils.segment(Settings.SYMBOL_SEGMENT, card)) {
            throw new GeneralOperationException("Segmentace nebyla provedena. Opravte prosím přepis OCR.");
        }
        
        // save the card and log the event
        
        RetrobiOperations.saveCardSegments(card, oldValue, user, logger);
    }
    
    /**
     * Does the manual segmentation.
     * 
     * @param cardId
     * ID of a card to modify
     * @param textH
     * head segment
     * @param textT
     * title segment
     * @param textB
     * bibliography segment
     * @param textA
     * annotation segment
     * @param textE
     * excerpter segment
     * @param user
     * user that did the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void doManualSegments(final String cardId, final String textH, final String textT, final String textB, final String textA, final String textE, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // load the card
        
        final Card card = RetrobiOperations.loadCard(cardId);
        
        // check card state
        
        if (card.getState().equals(CardState.CLOSED)) {
            throw new GeneralOperationException("Segmentaci nelze uložit, lístek je již uzavřený.");
        }
        
        // get the old value
        
        final String oldValue = SimpleSegmentUtils.segmentsToString(card, Settings.SYMBOL_SEGMENT);
        
        // assign the segments manually
        
        card.setSegmentHead(SimpleStringUtils.nullToEmpty(textH).trim());
        card.setSegmentTitle(SimpleStringUtils.nullToEmpty(textT).trim());
        card.setSegmentBibliography(SimpleStringUtils.nullToEmpty(textB).trim());
        card.setSegmentAnnotation(SimpleStringUtils.nullToEmpty(textA).trim());
        card.setSegmentExcerpter(SimpleStringUtils.nullToEmpty(textE).trim());
        
        // save the card and log the event
        
        RetrobiOperations.saveCardSegments(card, oldValue, user, logger);
    }
    
    /**
     * Internal method for saving a card after the segmentation change.
     * 
     * @param card
     * card to be saved
     * @param oldValue
     * old segments value encoded in a string
     * @param user
     * user that did the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    private static void saveCardSegments(final Card card, final String oldValue, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        try {
            // update card
            
            RetrobiApplication.db().getCardRepository().updateCard(card);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při aktualizaci lístku.", x);
        }
        
        // get the new value also
        
        final String newValue = SimpleSegmentUtils.segmentsToString(card, Settings.SYMBOL_SEGMENT);
        
        // log the change
        // (close if the card state is segmented and higher)
        
        final boolean closeEvent = card.getState().isHigherThan(CardState.REWRITTEN);
        
        try {
            RetrobiApplication.db().getMessageRepository().eventSegmentsUpdated(logger, card, oldValue, newValue, user, closeEvent);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
        }
        
        // set card state to "segmented" and do associated operations
        // (this does administrator only)
        
        if (user.hasRoleAtLeast(UserRole.ADMIN)) {
            RetrobiOperations.changeCardState(card.getId(), CardState.SEGMENTED, user, logger);
        }
    }
    
    /**
     * Changes the fixed OCR of the given card.
     * 
     * @param cardId
     * ID of a card to modify
     * @param newOcr
     * new OCR
     * @param user
     * logged user
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void changeCardOcr(final String cardId, final String newOcr, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // load the card
        
        final Card card = RetrobiOperations.loadCard(cardId);
        
        // check OCR
        
        if (SimpleStringUtils.isEmpty(newOcr)) {
            throw new GeneralOperationException("Navrhované OCR nesmí být prázdné.");
        }
        
        // check card state
        
        if (card.getState().equals(CardState.CLOSED)) {
            throw new GeneralOperationException("Změnu OCR nelze uložit, lístek je již uzavřený.");
        }
        
        // fix the OCR (by using the typographic rules)
        
        final String newOcrFixed = SimpleStringUtils.fixTypographicSpaces(newOcr);
        
        if (SimpleGeneralUtils.wasChangedAsString(card.getOcrFix(), newOcrFixed)) {
            // set the new fixed OCR to the card
            
            card.setOcrFix(newOcrFixed);
            
            // remember the last user who fixed the card
            // (change user only if the current state is rewritten or lower)
            // we must prevent editors and admins to get too much points
            
            if (!card.getState().isHigherThan(CardState.REWRITTEN)) {
                card.setOcrFixUserId(user.getId());
            }
            
            try {
                // update card
                
                RetrobiApplication.db().getCardRepository().updateCard(card);
            } catch (final GeneralRepositoryException x) {
                throw new GeneralOperationException("Chyba při aktualizaci lístku.", x);
            }
            
            // log the change
            
            try {
                RetrobiApplication.db().getMessageRepository().eventOcrUpdated(logger, card, newOcrFixed, user);
            } catch (final GeneralRepositoryException x) {
                throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
            }
        }
        
        // set card state to "rewritten"
        
        RetrobiOperations.changeCardState(cardId, CardState.REWRITTEN, user, logger);
    }
    
    /**
     * Updates the basic card information.
     * 
     * @param cardId
     * ID of a card to be updated
     * @param newNote
     * new card note
     * @param newUrl
     * new URL address
     * @param user
     * logged user who did the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void updateCard(final String cardId, final String newUrl, final String newNote, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // load the card
        
        final Card card = RetrobiOperations.loadCard(cardId);
        
        // change card properties
        
        final String oldUrl = card.getUrl();
        card.setUrl(newUrl);
        final String oldNote = card.getNote();
        card.setNote(newNote);
        
        try {
            // update the card in the database
            
            RetrobiApplication.db().getCardRepository().updateCard(card);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při uzavírání lístku.", x);
        }
        
        // log the changes
        
        if (SimpleGeneralUtils.wasChangedAsString(oldUrl, newUrl)) {
            try {
                RetrobiApplication.db().getMessageRepository().eventCardUrlUpdated(logger, card, oldUrl, newUrl, user);
            } catch (final GeneralRepositoryException x) {
                throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
            }
        }
        
        if (SimpleGeneralUtils.wasChangedAsString(oldNote, newNote)) {
            try {
                RetrobiApplication.db().getMessageRepository().eventCardNoteUpdated(logger, card, oldNote, newNote, user);
            } catch (final GeneralRepositoryException x) {
                throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
            }
        }
    }
    
    /**
     * Updates the card state (if different) and performs the state transitions.
     * Currently, two additional operations are done: confirming all relevant
     * messages (from any lower state to CLOSED) and synthesized image
     * generation (from any lower state to SEGMENTED).
     * 
     * @param cardId
     * ID of a card to be updated
     * @param newState
     * new state to be set
     * @param user
     * user who does the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void changeCardState(final String cardId, final CardState newState, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // load the card
        
        final Card card = RetrobiOperations.loadCard(cardId);
        
        final CardState oldState = card.getState();
        
        if (!oldState.equals(newState)) {
            // -----------------
            // CARD STATE CHANGE
            // -----------------
            
            // set the new card state
            // (only if the new state differs)
            
            card.setState(newState);
            
            try {
                // update the card in the database
                
                RetrobiApplication.db().getCardRepository().updateCard(card);
            } catch (final GeneralRepositoryException x) {
                throw new GeneralOperationException("Chyba při změně stavu lístku.", x);
            }
            
            // ------------------
            // MESSAGE CONFIRMING
            // ------------------
            
            final boolean makeEventConfirmed;
            
            // confirm all messages for this card
            // (that will happen only if the new state is higher than previous)
            
            if (newState.isHigherThan(oldState)) {
                try {
                    RetrobiApplication.db().getMessageRepository().confirmMessagesForCard(user.getId(), card.getId());
                } catch (final GeneralRepositoryException x) {
                    throw new GeneralOperationException("Chyba při potvrzování hlášení k uzavíranému lístku.", x);
                } catch (final NotFoundRepositoryException x) {
                    throw new GeneralOperationException("Chyba při potvrzování hlášení k uzavíranému lístku.", x);
                }
                
                // confirm the event only if the new state is higher and by
                // editor
                
                makeEventConfirmed = user.hasRoleAtLeast(UserRole.EDITOR);
            } else {
                makeEventConfirmed = false;
            }
            
            // ------------
            // EVENT ADDING
            // ------------
            
            try {
                RetrobiApplication.db().getMessageRepository().eventCardStateChanged(logger, oldState, card, user, makeEventConfirmed);
            } catch (final GeneralRepositoryException x) {
                throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
            }
        }
        
        // -------------------
        // SYNTH IMAGES UPDATE
        // -------------------
        
        RetrobiOperations.updateSynthesizedImages(card, oldState, newState);
        
        // ---------------------
        // USER COUNTER UPDATING
        // ---------------------
        
        // this should be last operation because it can throw an exception
        // (when the last OCR fixing user is not found)
        
        RetrobiOperations.updateUserEditCounter(card.getOcrFixUserId(), oldState, newState);
    }
    
    /**
     * Updates the user edit counter based on the previous and current card
     * state. If no fixing user is assigned to a card, nothing will happened.
     * 
     * @param fixingUserId
     * fixing user id or <code>null</code> (OCR fix author)
     * @param oldState
     * old card state
     * @param newState
     * new card state
     * @throws GeneralOperationException
     * an exception during the operation
     */
    private static void updateUserEditCounter(final String fixingUserId, final CardState oldState, final CardState newState) throws GeneralOperationException {
        if (fixingUserId == null) {
            return;
        }
        
        // is the old state segmented or higher?
        final boolean oldSegmentedOrMore = CardState.SEGMENTED.isLowerThan(oldState) || CardState.SEGMENTED.equals(oldState);
        // is the new state segmented or higher?
        final boolean newSegmentedOrMore = CardState.SEGMENTED.isLowerThan(newState) || CardState.SEGMENTED.equals(newState);
        
        try {
            if (!oldSegmentedOrMore && newSegmentedOrMore) {
                // non-segmented to segmented
                // increase user edit count
                
                RetrobiApplication.db().getUserRepository().changeUserEditCount(fixingUserId, 1);
            } else if (oldSegmentedOrMore && !newSegmentedOrMore) {
                // segmented to non-segmented
                // decrease user edit count
                
                RetrobiApplication.db().getUserRepository().changeUserEditCount(fixingUserId, -1);
            }
        } catch (final NotFoundRepositoryException x) {
            throw new GeneralOperationException("Poslední autor přepisu nebyl nalezen v databázi.", x);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při změně počtu přepisů uživatele.", x);
        }
    }
    
    /**
     * Updates the synthesized images of a card. Based on the previous and
     * current card state, an operation is chosen and done (nothing / image
     * removing / both image removing and image recreating).
     * 
     * @param card
     * card whose images should be update
     * @param oldState
     * old card state
     * @param newState
     * new card state
     * @throws GeneralOperationException
     * an exception during the operation
     */
    private static void updateSynthesizedImages(final Card card, final CardState oldState, final CardState newState) throws GeneralOperationException {
        boolean removeSynImages = false;
        boolean createSynImages = false;
        
        if (oldState.isHigherThan(newState)) {
            // decreasing state means removing synthesized images
            
            removeSynImages = true;
        }
        
        if (CardState.SEGMENTED.equals(newState) || CardState.SEGMENTED.isLowerThan(newState)) {
            // setting the segmented state (or higher) causes image synthesis
            
            removeSynImages = true;
            createSynImages = true;
        }
        
        // execute the selected operations
        
        if (removeSynImages) {
            RetrobiOperations.removeSynthesizedCardImages(card);
            
            if (createSynImages) {
                RetrobiOperations.synthesizeCardImages(card);
            }
        }
    }
    
    /**
     * Creates a new card in the database. The card will have a number 1.
     * 
     * @param catalog
     * target catalog
     * @param batch
     * target batch
     * @param number
     * target card number
     * @param user
     * user that did the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @return a new card created
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static Card createCard(final Catalog catalog, final String batch, final int number, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // create a card
        
        final Card newCard = new Card();
        newCard.setAdded(Time.now());
        newCard.setUpdated(Time.now());
        newCard.setBatch(batch);
        newCard.setBatchForSort(CzechAlphabet.getDefaultBatchForSort(newCard.getBatch()));
        newCard.setCatalog(catalog);
        newCard.setNumberInBatch(number);
        
        // save a card
        
        try {
            RetrobiApplication.db().getCardRepository().addCard(newCard);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při ukládání nového lístku.", x);
        }
        
        // log the change
        
        try {
            RetrobiApplication.db().getMessageRepository().eventCardCreated(logger, newCard, user);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
        }
        
        return newCard;
    }
    
    /**
     * Deletes a card and all relevant information.
     * 
     * @param cardId
     * ID of a card to be removed
     * @param user
     * user that did the change
     * @param logger
     * CSV logger (or <code>null</code>)
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void deleteCard(final String cardId, final User user, final CSVHistoryLogger logger) throws GeneralOperationException {
        // load the card
        
        final Card card = RetrobiOperations.loadCard(cardId);
        
        try {
            // remove card comments
            
            for (final String id : RetrobiApplication.db().getCommentRepository().getCommentIdsForCard(card.getId())) {
                final Comment comment = RetrobiApplication.db().getCommentRepository().getComment(id);
                RetrobiApplication.db().getCommentRepository().deleteComment(comment);
            }
            
            // remove the card
            
            RetrobiApplication.db().getCardRepository().deleteCard(card);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během mazání lístku: " + x.getMessage(), x);
        } catch (final NotFoundRepositoryException x) {
            throw new GeneralOperationException("Chyba během mazání lístku: " + x.getMessage(), x);
        }
        
        // log the change
        
        try {
            RetrobiApplication.db().getMessageRepository().eventCardRemoved(logger, card, user);
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při ukládání hlášení.", x);
        }
        
        // reorder the batch
        
        try {
            RetrobiApplication.db().getCardRepository().renumberBatch(card.getCatalog(), card.getBatch());
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při přečíslování skupiny: " + x.getMessage(), x);
        } catch (final NotFoundRepositoryException x) {
            throw new GeneralOperationException("Chyba při přečíslování skupiny: " + x.getMessage(), x);
        }
    }
    
    /**
     * Resets all OCRs fixed by the given user and resets his counter.
     * 
     * @param userId
     * user ID
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void resetRewritesByUser(final String userId) throws GeneralOperationException {
        int changeCounter = 0;
        
        try {
            // find all cards rewritten by the given user
            
            final List<String> cardIds = RetrobiApplication.db().getCardRepository().getCardIdsFixedByUser(userId);
            
            for (final String cardId : cardIds) {
                // load the card
                
                final Card card = RetrobiOperations.loadCard(cardId);
                
                if (CardState.REWRITTEN.equals(card.getState())) {
                    // reset the last OCR rewrite
                    
                    card.resetRewrite();
                    
                    try {
                        // save the card
                        
                        RetrobiApplication.db().getCardRepository().updateCard(card);
                        
                        // increment change count
                        
                        changeCounter++;
                    } catch (final GeneralRepositoryException x) {
                        RetrobiOperations.LOG.warn(x.getMessage(), x);
                    }
                }
            }
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při mazání uživatelského přepisu: " + x.getMessage(), x);
        }
    }
    
    /**
     * Loads the card by its ID and wraps the exceptions.
     * 
     * @param cardId
     * card ID
     * @return card
     * @throws GeneralOperationException
     * an exception during the loading
     */
    private static Card loadCard(final String cardId) throws GeneralOperationException {
        try {
            return RetrobiApplication.db().getCardRepository().getCard(cardId);
        } catch (final NotFoundRepositoryException x) {
            throw new GeneralOperationException(x.getMessage());
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException(x.getMessage());
        }
    }
    
    /**
     * Removes all existing synthesized images of the given card.
     * 
     * @param card
     * card
     * @throws GeneralOperationException
     * an exception during the operation
     */
    private static void removeSynthesizedCardImages(final Card card) throws GeneralOperationException {
        // get all existing synthesized images
        
        final List<String> imageNames = card.getAttachmentNamesSorted();
        
        try {
            // remove all the images
            
            for (final String imageName : imageNames) {
                if (ImageFlag.SYNTHESIZED.inImageName(imageName)) {
                    RetrobiOperations.LOG.debug(String.format("Odstraňuji syntetický obrázek '%s'...", imageName));
                    RetrobiApplication.db().getCardImageRepository().removeImageFromCard(card, imageName);
                }
            }
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba při odstraňování obrázku.", x);
        } catch (final NotFoundRepositoryException x) {
            throw new GeneralOperationException("Obrázek nebyl nalezen.", x);
        }
    }
    
    /**
     * Creates synthesized images for the given card. All already existing
     * synthesized images are first deleted and then replaced. The remaining
     * images are untouched.<br>
     * <br>
     * <b>Note:</b> The card attachments are invalid after this operation and
     * the document must be updated.
     * 
     * @param card
     * card
     * @throws GeneralOperationException
     * an exception during the operation
     */
    private static void synthesizeCardImages(final Card card) throws GeneralOperationException {
        // generate new synthetic images
        
        final List<BufferedImage> newImages = SimpleImageUtils.synthesizeCardImages(card);
        
        // save new images
        
        int page = 1;
        
        try {
            for (final BufferedImage newImage : newImages) {
                // create a name for the new image
                
                final String newImageName = ImageFlag.produceImageName(
                        page,
                        ImageFlag.SYNTHESIZED);
                
                // save the new image to the card
                
                RetrobiApplication.db().getCardImageRepository().addImageToCard(
                        card,
                        newImage,
                        newImageName,
                        "png",
                        "image/png");
                
                // increment the page number
                
                page++;
            }
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během přidávání obrázku.", x);
        }
    }
    
    // ========
    // MESSAGES
    // ========
    
    /**
     * Confirms or un-confirms the user message (or system event).
     * 
     * @param message
     * the message to confirm
     * @param user
     * logged user that does the confirmation
     * @throws GeneralOperationException
     * an exception during the operation
     */
    public static void confirmOrUnconfirmMessage(final Message message, final User user) throws GeneralOperationException {
        try {
            if (message.getConfirmedByUserId() == null) {
                // confirm message
                
                RetrobiApplication.db().getMessageRepository().confirmMessage(message, user.getId());
            } else {
                // un-confirm message
                
                RetrobiApplication.db().getMessageRepository().unconfirmMessage(message);
            }
        } catch (final GeneralRepositoryException x) {
            throw new GeneralOperationException("Chyba během změny stavu hlášení.", x);
        }
    }
    
    /**
     * Cannot make instances of this class.
     */
    private RetrobiOperations() {
        throw new UnsupportedOperationException();
    }
}
