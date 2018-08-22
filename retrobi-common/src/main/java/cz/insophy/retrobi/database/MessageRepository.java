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

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Message;
import cz.insophy.retrobi.database.entity.User;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.database.entity.type.ImageFlag;
import cz.insophy.retrobi.database.entity.type.MessageStateOption;
import cz.insophy.retrobi.database.entity.type.MessageType;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.exception.NotFoundRepositoryException;
import cz.insophy.retrobi.longtask.CardModification;
import cz.insophy.retrobi.utils.CSVHistoryLogger;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;

/**
 * Message repository.
 * 
 * @author Vojtěch Hordějčuk
 */
public class MessageRepository extends AbstractRepository {
    /**
     * logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(MessageRepository.class);
    /**
     * all open event messages by card
     */
    private static final String V_OPEN_EVENTS_BY_CARD = "card_open_events";
    /**
     * all open problem messages by card
     */
    private static final String V_OPEN_PROBLEMS_BY_CARD = "card_open_problems";
    /**
     * old closed messages
     */
    private static final String V_CLOSED_OLD = "closed_old";
    
    /**
     * Creates a new instance.
     * 
     * @param database
     * database object
     */
    protected MessageRepository(final Database database) {
        super(database);
    }
    
    /**
     * Loads messages by their IDs.
     * 
     * @param messageIds
     * message IDs
     * @return list of messages
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<Message> getMessages(final List<String> messageIds) throws NotFoundRepositoryException, GeneralRepositoryException {
        MessageRepository.LOG.debug(String.format("Loading %d message(s)...", messageIds.size()));
        return this.loadDocuments(Message.class, messageIds);
    }
    
    /**
     * Loads a list of unconfirmed EVENT messages for the given card.
     * 
     * @param cardId
     * card ID
     * @return list of message IDs
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<String> getEventMessageIds(final String cardId) throws GeneralRepositoryException { // NO_UCD
        return this.getMessageIds(cardId, true);
    }
    
    /**
     * Loads a list of unconfirmed PROBLEM messages for the given card.
     * 
     * @param cardId
     * card ID
     * @return list of message IDs
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<String> getProblemMessageIds(final String cardId) throws GeneralRepositoryException {
        return this.getMessageIds(cardId, false);
    }
    
    /**
     * Returns IDs of old messages (older than one month).
     * 
     * @param limit
     * message limit (a reasonable number is about 5000)
     * @return old message IDs
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public List<String> getOldMessageIds(final int limit) throws GeneralRepositoryException {
        MessageRepository.LOG.debug("Loading old closed messages...");
        
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH) + 1;
        final Object endKey = (year * 100) + month - 1;
        
        // an example:
        // - started at 3. 5. 2012
        // - year is 2012
        // - month is 5
        // - end key is 2012 * 100 + 5 - 1 = 201204
        
        return this.loadDocumentIds(MessageRepository.V_CLOSED_OLD, new Options().endKey(endKey).limit(limit));
    }
    
    /**
     * Loads a list of unconfirmed messages for the given card. The list is
     * sorted by date ascending. No limit is used.
     * 
     * @param cardId
     * card ID
     * @param events
     * <code>true</code> for events, <code>false</code> for problems
     * @return list of message IDs
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private List<String> getMessageIds(final String cardId, final boolean events) throws GeneralRepositoryException {
        MessageRepository.LOG.debug(String.format("Loading messages for card '%s'...", cardId));
        
        final Object startKey = new Object[] { cardId };
        final Object endKey = new Object[] { cardId, Collections.emptyMap() };
        
        final String viewName = events ? MessageRepository.V_OPEN_EVENTS_BY_CARD : MessageRepository.V_OPEN_PROBLEMS_BY_CARD;
        final Options options = new Options().startKey(startKey).endKey(endKey);
        
        return this.loadDocumentIds(viewName, options);
    }
    
    /**
     * Loads the message count and a list from the database using the filter
     * provided (by type). The list is sorted by date.
     * 
     * @param state
     * state filter (or <code>null</code>)
     * @param type
     * type filter (or <code>null</code>)
     * @param page
     * record page
     * @param limit
     * record limit
     * @return list of message IDs with a paging information
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public Tuple<Integer, List<String>> getMessageIdsByType(final MessageStateOption state, final MessageType type, final int page, final int limit) throws NotFoundRepositoryException, GeneralRepositoryException {
        MessageRepository.LOG.debug(String.format("Loading messages using a filter (state = %s / type = %s)...", state, type));
        
        final String viewName = (type == null) ? MessageRepository.getViewName(state) : MessageRepository.getViewNameByType(state);
        final String countViewName = MessageRepository.getCountViewName(viewName);
        final Object startKey = (type == null) ? null : new Object[] { type.name() };
        final Object endKey = (type == null) ? null : new Object[] { type.name(), Collections.emptyMap() };
        
        return this.getPagedMessageIds(viewName, countViewName, page, limit, startKey, endKey);
    }
    
    /**
     * Loads the message count and a list from the database using the filter
     * provided (by user). The list is sorted by date.
     * 
     * @param state
     * state filter (or <code>null</code>)
     * @param user
     * user filter (or <code>null</code>)
     * @param page
     * record page
     * @param limit
     * record limit
     * @return list of message IDs with a paging information
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public Tuple<Integer, List<String>> getMessageIdsByUser(final MessageStateOption state, final String user, final int page, final int limit) throws NotFoundRepositoryException, GeneralRepositoryException {
        MessageRepository.LOG.debug(String.format("Loading messages using a filter (state = %s / user = %s)...", state, user));
        
        final String viewName = (user == null) ? MessageRepository.getViewName(state) : MessageRepository.getViewNameByUser(state);
        final String countViewName = MessageRepository.getCountViewName(viewName);
        final Object startKey = (user == null) ? null : new Object[] { user };
        final Object endKey = (user == null) ? null : new Object[] { user, Collections.emptyMap() };
        
        return this.getPagedMessageIds(viewName, countViewName, page, limit, startKey, endKey);
    }
    
    /**
     * Helper method for getting a message ID list. Returns messages sorted by
     * the addition date descending (newest first).
     * 
     * @param docView
     * full document view name
     * @param countView
     * count view name
     * @param page
     * page number
     * @param limit
     * limit
     * @param startKey
     * start key (or <code>null</code>)
     * @param endKey
     * end key (or <code>null</code>)
     * @return list of messages and relevant information
     * @throws NotFoundRepositoryException
     * not found exception
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private Tuple<Integer, List<String>> getPagedMessageIds(final String docView, final String countView, final int page, final int limit, final Object startKey, final Object endKey) throws NotFoundRepositoryException, GeneralRepositoryException {
        MessageRepository.LOG.debug("Loading messages (paged)...");
        
        // DESCENDING mode is used = START key and END key are swapped
        
        return this.loadPagedDocumentIds(docView, countView, endKey, startKey, true, page, limit);
    }
    
    /**
     * Saves the new user registered event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param user
     * a new user that has just registered
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventUserRegistered(final CSVHistoryLogger logger, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_USER_REGISTERED, null, null, user),
                null,
                user.getLogin());
    }
    
    /**
     * Saves the user removed event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param user
     * an existing user that was removed
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventUserRemoved(final CSVHistoryLogger logger, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_USER_REMOVED, null, null, user),
                user.getLogin(),
                null);
    }
    
    /**
     * Saves the card added event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that was added
     * @param user
     * user that did the change
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardCreated(final CSVHistoryLogger logger, final Card card, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_CARD_CREATED, card, null, user),
                SimpleSegmentUtils.getCardAsText(card),
                null);
    }
    
    /**
     * Saves the card removed event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that was removed
     * @param user
     * user that did the change
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardRemoved(final CSVHistoryLogger logger, final Card card, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_CARD_REMOVED, card, null, user),
                SimpleSegmentUtils.getCardAsText(card),
                null);
    }
    
    /**
     * Saves the OCR change event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that is to be changed
     * @param newOcr
     * new OCR
     * @param user
     * user that did the change
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventOcrUpdated(final CSVHistoryLogger logger, final Card card, final String newOcr, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_OCR_UPDATED, card, null, user),
                card.getOcrFix(),
                newOcr);
    }
    
    /**
     * Saves the segment change event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that is to be changed
     * @param oldValue
     * old value (segments encoded in a string)
     * @param newValue
     * new value (segments encoded in a string)
     * @param user
     * user that did the change
     * @param makeEventConfirmed
     * make the event confirmed (before save)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventSegmentsUpdated(final CSVHistoryLogger logger, final Card card, final String oldValue, final String newValue, final User user, final boolean makeEventConfirmed) throws GeneralRepositoryException {
        final Message message = new Message(MessageType.EVENT_SEGMENTS_UPDATED, card, null, user);
        
        if (makeEventConfirmed) {
            message.confirm(user.getId());
        }
        
        this.saveAndLog(
                logger,
                message,
                oldValue,
                newValue);
    }
    
    /**
     * Saves the note change event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that is to be changed
     * @param oldValue
     * old value
     * @param newValue
     * new value
     * @param user
     * user that did the change
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardNoteUpdated(final CSVHistoryLogger logger, final Card card, final String oldValue, final String newValue, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_NOTE_UPDATED, card, null, user),
                oldValue,
                newValue);
    }
    
    /**
     * Saves the URL change event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that is to be changed
     * @param oldValue
     * old value
     * @param newValue
     * new value
     * @param user
     * user that did the change
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardUrlUpdated(final CSVHistoryLogger logger, final Card card, final String oldValue, final String newValue, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_URL_UPDATED, card, null, user),
                oldValue,
                newValue);
    }
    
    /**
     * Saves the card upgraded event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param oldState
     * the previous card state
     * @param card
     * card that was updated (with new state)
     * @param user
     * user that did the change
     * @param makeEventConfirmed
     * make the event confirmed (before save)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardStateChanged(final CSVHistoryLogger logger, final CardState oldState, final Card card, final User user, final boolean makeEventConfirmed) throws GeneralRepositoryException {
        final String oldValue = oldState.toString();
        final String newValue = card.getState().toString();
        final String body = String.format("Změna stavu '%s' na '%s'.", oldValue, newValue);
        
        final Message message = new Message(MessageType.EVENT_STATE_UPDATED, card, null, user, body);
        
        if (makeEventConfirmed) {
            message.confirm(user.getId());
        }
        
        this.saveAndLog(
                logger,
                message,
                oldValue,
                newValue);
    }
    
    /**
     * Saves the card image cross-out event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param oldImageName
     * old image name
     * @param newImageName
     * new image name
     * @param card
     * card that was modified
     * @param user
     * user that did the change
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventImageCrossUpdated(final CSVHistoryLogger logger, final String oldImageName, final String newImageName, final Card card, final User user) throws GeneralRepositoryException {
        final MessageType type = ImageFlag.CROSSOUT.inImageName(newImageName)
                ? MessageType.EVENT_IMAGE_CROSS_ON
                : MessageType.EVENT_IMAGE_CROSS_OFF;
        
        this.saveAndLog(
                logger,
                new Message(type, card, oldImageName, user),
                oldImageName,
                newImageName);
    }
    
    /**
     * Saves the card modification event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param card
     * card that was modified
     * @param modification
     * modification that was done to the card
     * @param user
     * user that did the change(s)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardModified(final CSVHistoryLogger logger, final Card card, final CardModification modification, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_CARD_MODIFIED, card, null, user, modification == null ? "-" : modification.getTitle()),
                null,
                null);
    }
    
    /**
     * Saves the multiple card modification event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param count
     * number of cards edited
     * @param modification
     * modification that was done to the card
     * @param user
     * user that did the change(s)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventMultipleCardsModified(final CSVHistoryLogger logger, final int count, final CardModification modification, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_MULTIPLE_CARDS_MODIFIED, null, null, user, String.format("Hromadná úprava (%s, lístků: %d)", modification == null ? "-" : modification.getTitle(), count)),
                null,
                null);
    }
    
    /**
     * Saves the card move event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param oldCatalog
     * old catalog
     * @param oldBatch
     * old batch
     * @param oldNumber
     * old number
     * @param card
     * card (on the new position)
     * @param user
     * user that did the change(s)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventCardMoved(final CSVHistoryLogger logger, final Catalog oldCatalog, final String oldBatch, final int oldNumber, final Card card, final User user) throws GeneralRepositoryException {
        final String posf = "%s / %s # %d";
        final String oldpos = String.format(posf, oldCatalog.toString(), oldBatch, oldNumber);
        final String newpos = String.format(posf, card.getCatalog().toString(), card.getBatch(), card.getNumberInBatch());
        
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_CARD_MOVED, card, null, user),
                oldpos,
                newpos);
    }
    
    /**
     * Saves the multiple card move event.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param count
     * card count
     * @param pivot
     * pivot card
     * @param user
     * user that did the change(s)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void eventMultipleCardsMoved(final CSVHistoryLogger logger, final int count, final Card pivot, final User user) throws GeneralRepositoryException {
        this.saveAndLog(
                logger,
                new Message(MessageType.EVENT_MULTIPLE_CARDS_MOVED, null, null, user, String.format("Hromadný přesun (cíl: %s, lístků: %d)", pivot.toString(), count)),
                null, null);
    }
    
    /**
     * Adds a message into the database and logs it.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param message
     * the message to save
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void addCustomMessage(final CSVHistoryLogger logger, final Message message) throws GeneralRepositoryException {
        MessageRepository.LOG.debug("Adding custom message...");
        this.saveAndLog(logger, message, null, null);
        MessageRepository.LOG.debug("Message added.");
    }
    
    /**
     * Confirms the message.
     * 
     * @param message
     * message to confirm
     * @param userId
     * user that confirmed the message
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void confirmMessage(final Message message, final String userId) throws GeneralRepositoryException {
        MessageRepository.LOG.debug("Confirming message...");
        message.confirm(userId);
        this.updateDocument(message);
        MessageRepository.LOG.debug("Message confirmed.");
    }
    
    /**
     * Un-confirms the message.
     * 
     * @param message
     * message to un-confirm
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void unconfirmMessage(final Message message) throws GeneralRepositoryException {
        MessageRepository.LOG.debug("Unconfirming message...");
        message.unconfirm();
        this.updateDocument(message);
        MessageRepository.LOG.debug("Message unconfirmed.");
    }
    
    /**
     * Confirms all event and problem messages for the given card.
     * 
     * @param userId
     * confirming user ID
     * @param cardId
     * card ID
     * @throws GeneralRepositoryException
     * general repository exception
     * @throws NotFoundRepositoryException
     * not found exception
     */
    public void confirmMessagesForCard(final String userId, final String cardId) throws GeneralRepositoryException, NotFoundRepositoryException {
        MessageRepository.LOG.debug(String.format("Confirming all events for card '%s' by user '%s'...", cardId, userId));
        
        // confirm events
        
        for (final String messageId : this.getEventMessageIds(cardId)) {
            MessageRepository.LOG.debug("Confirming event message: " + messageId);
            final Message message = this.loadDocument(Message.class, messageId);
            this.confirmMessage(message, userId);
            MessageRepository.LOG.debug("Message confirmed and saved.");
        }
        
        MessageRepository.LOG.debug("Confirming done.");
    }
    
    /**
     * Removes a message with the given ID.
     * 
     * @param id
     * message ID
     * @throws NotFoundRepositoryException
     * message not found
     * @throws GeneralRepositoryException
     * general repository exception
     */
    public void removeMessage(final String id) throws NotFoundRepositoryException, GeneralRepositoryException {
        MessageRepository.LOG.debug(String.format("Removing message with ID '%s'...", id));
        final Message message = this.loadDocument(Message.class, id);
        this.deleteDocument(message);
        MessageRepository.LOG.debug("Message removed.");
    }
    
    /**
     * Saves the provided message and records the event into the CSV log file.
     * It only throws exceptions occurred during the message saving. The log is
     * written always, even if the message saving ends with an exception. And
     * all the exceptions during the file writing are ignored.
     * 
     * @param logger
     * logger (or <code>null</code>)
     * @param message
     * message
     * @param oldValue
     * old value (or <code>null</code>)
     * @param newValue
     * new value (or <code>null</code>)
     * @throws GeneralRepositoryException
     * general repository exception
     */
    private void saveAndLog(final CSVHistoryLogger logger, final Message message, final String oldValue, final String newValue) throws GeneralRepositoryException {
        MessageRepository.LOG.debug(String.format("Saving the event as a message: %s", message.getType().name()));
        
        try {
            if (message.getType().isSavingToDatabase()) {
                // save message as document
                
                this.createDocument(message);
            }
        } finally {
            if (logger != null) {
                // save message into the CSV log
                
                MessageRepository.LOG.debug("Saving the event into the CSV log...");
                
                try {
                    logger.append(message, oldValue, newValue);
                } catch (final IOException x) {
                    MessageRepository.LOG.warn("I/O error while appending to CSV log: " + x.getMessage());
                }
            } else {
                MessageRepository.LOG.debug("Logger not available in this context.");
            }
        }
        
        MessageRepository.LOG.debug("Event was logged.");
    }
    
    /**
     * Sends an e-mail. Source address and the subject prefix may be configured
     * in the Settings class. Needs the SMTP server to be up and running and the
     * following system properties to be set:
     * <ul>
     * <li>mail.store.protocol</li>
     * <li>mail.transport.protocol</li>
     * <li>mail.host</li>
     * <li>mail.user</li>
     * <li>mail.protocol.host</li>
     * <li>mail.protocol.user</li>
     * <li>mail.debug</li>
     * </ul>
     * If there is any property missing, a default value is used - and it does
     * not have to be always the best.
     * 
     * @param to
     * e-mail address to send to (recipient)
     * @param subject
     * subject line (common prefix will be added)
     * @param body
     * the main content of the message (message body) in HTML
     * @throws AddressException
     * address exception
     * @throws MessagingException
     * messaging exception
     */
    public void sendEmail(final String to, final String subject, final String body) throws AddressException, MessagingException {
        MessageRepository.LOG.debug(String.format("Sending an e-mail to '%s' with subject '%s'...", to, subject));
        
        // get a session instance
        
        final Session session = Session.getDefaultInstance(System.getProperties(), null);
        
        // create a new message
        
        final javax.mail.Message message = new MimeMessage(session);
        
        // setup message
        
        message.setFrom(new InternetAddress(Settings.SOURCE_EMAIL));
        message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(Settings.RETROBI_SUBJECT_PREFIX + subject);
        message.setText("Tato zprava je ve formatu HTML (znakova sada UTF-8). Pokud ji nemuzete precist, aktualizujte prosim svuj e-mailovy prohlizec.");
        message.setContent(body, "text/html; charset=utf-8");
        message.setHeader("X-Mailer", CommentRepository.class.getSimpleName());
        message.setSentDate(new Date());
        
        // send the message
        
        Transport.send(message);
        
        MessageRepository.LOG.debug("E-mail sent successfully.");
    }
    
    // =============
    // VIEW CREATION
    // =============
    
    @Override
    public Map<String, View> createViews() {
        final Map<String, View> views = new HashMap<String, View>();
        
        // ---------------
        // MESSAGE LISTING
        // ---------------
        
        // list of open messages (one card) - events and problems individually
        
        // key = [card ID, year, month, day, hour, minute]
        // value = message ID
        
        views.put(MessageRepository.V_OPEN_EVENTS_BY_CARD, new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_message && doc.confirmed_user_id == null && doc.event) {\n" +
                "    emit([doc.card_id, doc.added.y, doc.added.m, doc.added.d, doc.added.hh, doc.added.mm], doc._id);\n" +
                "  }\n" +
                "}\n"));
        
        views.put(MessageRepository.V_OPEN_PROBLEMS_BY_CARD, new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_message && doc.confirmed_user_id == null && !doc.event) {\n" +
                "    emit([doc.card_id, doc.added.y, doc.added.m, doc.added.d, doc.added.hh, doc.added.mm], doc._id);\n" +
                "  }\n" +
                "}\n"));
        
        // list of old closed messages (more than month)
        
        // key = year * 100 + month (e.g. 201112, 201203)
        // value = message ID
        
        views.put(MessageRepository.V_CLOSED_OLD, new View("" +
                "function(doc) {\n" +
                "  if (doc.TAG_message && doc.confirmed_user_id != null && doc.confirmed != null) {\n" +
                "    emit(doc.confirmed.y * 100 + doc.confirmed.m, doc._id);\n" +
                "  }\n" +
                "}\n"));
        
        // list of messages (all cards)
        
        for (final MessageStateOption state : MessageStateOption.values()) {
            // all cards
            MessageRepository.addViewAndCount(views, "null", MessageRepository.getViewName(state), state);
            // by type
            MessageRepository.addViewAndCount(views, "doc.type", MessageRepository.getViewNameByType(state), state);
            // by user
            MessageRepository.addViewAndCount(views, "doc.user_name", MessageRepository.getViewNameByUser(state), state);
        }
        
        return Collections.unmodifiableMap(views);
    }
    
    /**
     * Adds two views in the target map: view that returns message document IDs
     * and the second view that returns message count and has the same map
     * function as the previous.
     * 
     * @param views
     * target map
     * @param var
     * variable name (which to add as a value)
     * @param viewName
     * base view name
     * @param state
     * message state to use
     */
    private static void addViewAndCount(final Map<String, View> views, final String var, final String viewName, final MessageStateOption state) {
        final String countViewName = MessageRepository.getCountViewName(viewName);
        final View viewOfUser = MessageRepository.createView(var, state);
        views.put(viewName, viewOfUser);
        views.put(countViewName, AbstractProtoRepository.createCountView(viewOfUser));
    }
    
    /**
     * Just a helper method for creating similar views.
     * 
     * @param vars
     * variables used as a key (e.g. "doc.user_name")
     * @param state
     * message state
     * @return a created view
     */
    private static View createView(final String vars, final MessageStateOption state) {
        // ---------------
        // MESSAGE LISTING
        // ---------------
        
        // key = [VARS, year, month, day, hour, minute]
        // value = message ID
        
        final String map = "" +
                "function(doc) {\n" +
                "  if (doc.TAG_message && %s) {\n" +
                "    emit([%s, doc.added.y, doc.added.m, doc.added.d, doc.added.hh, doc.added.mm], doc._id);\n" +
                "  }\n" +
                "}\n";
        
        return new View(String.format(map, MessageRepository.getViewCondition(state), vars));
    }
    
    /**
     * Returns the view condition.
     * 
     * @param state
     * message state
     * @return view condition (Javascript code fragment)
     */
    private static String getViewCondition(final MessageStateOption state) {
        switch (state) {
            case CONFIRMED_ALL:
                return "doc.confirmed_user_id != null";
            case UNCONFIRMED_ALL:
                return "doc.confirmed_user_id == null";
            case CONFIRMED_EVENTS:
                return "doc.event && doc.confirmed_user_id != null";
            case UNCONFIRMED_EVENTS:
                return "doc.event && doc.confirmed_user_id == null";
            case CONFIRMED_PROBLEMS:
                return "!doc.event && doc.confirmed_user_id != null";
            case UNCONFIRMED_PROBLEMS:
                return "!doc.event && doc.confirmed_user_id == null";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Returns the name of the view with item count.
     * 
     * @param viewName
     * basic view name
     * @return name of the view with item count
     */
    private static String getCountViewName(final String viewName) {
        return viewName + "_count";
    }
    
    /**
     * Returns the view name (by user).
     * 
     * @param state
     * message state
     * @return view name
     */
    private static String getViewNameByUser(final MessageStateOption state) {
        return MessageRepository.getViewPrefix(state) + "by_user";
    }
    
    /**
     * Returns the view name (by type).
     * 
     * @param state
     * message state
     * @return view name
     */
    private static String getViewNameByType(final MessageStateOption state) {
        return MessageRepository.getViewPrefix(state) + "by_type";
    }
    
    /**
     * Returns the view name.
     * 
     * @param state
     * message state
     * @return view name
     */
    private static String getViewName(final MessageStateOption state) {
        return MessageRepository.getViewPrefix(state) + "all";
    }
    
    /**
     * Returns the correct view prefix for the given state.
     * 
     * @param state
     * message state
     * @return view prefix (e.g. <code>closed_all_</code>)
     */
    private static String getViewPrefix(final MessageStateOption state) {
        switch (state) {
            case CONFIRMED_ALL:
                return "closed_";
            case CONFIRMED_EVENTS:
                return "closed_e_";
            case CONFIRMED_PROBLEMS:
                return "closed_p_";
            case UNCONFIRMED_ALL:
                return "open_";
            case UNCONFIRMED_EVENTS:
                return "open_e_";
            case UNCONFIRMED_PROBLEMS:
                return "open_p_";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
