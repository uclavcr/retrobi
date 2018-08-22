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

package cz.insophy.retrobi.database.entity;

import org.svenson.JSONProperty;

import cz.insophy.retrobi.database.document.StandaloneDocument;
import cz.insophy.retrobi.database.entity.type.MessageType;

/**
 * General system message that is added whenever some important data in the
 * database is changed. Each message is either unconfirmed or confirmed (done).
 * 
 * @author Vojtěch Hordějčuk
 */
public class Message extends StandaloneDocument {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * message type
     */
    private MessageType type;
    /**
     * time added
     */
    private Time added;
    /**
     * relevant card ID (can be <code>null</code>)
     */
    private String cardId;
    /**
     * relevant card name (can be <code>null</code>)
     */
    private String cardName;
    /**
     * relevant user ID (can be <code>null</code>)
     */
    private String userId;
    /**
     * relevant user name (can be <code>null</code>)
     */
    private String userName;
    /**
     * relevant image name (can be <code>null</code>)
     */
    private String imageName;
    /**
     * message body
     */
    private String body;
    /**
     * confirmed by user ID (can be <code>null</code>)
     */
    private String confirmedByUserId;
    /**
     * time of confirmation (can be <code>null</code>)
     */
    private Time confirmed;
    
    /**
     * Creates a new instance.
     */
    public Message() {
        super();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param type
     * message type
     * @param refCard
     * relevant card (or <code>null</code>)
     * @param refImage
     * relevant image (or <code>null</code>)
     * @param user
     * relevant user (or <code>null</code>)
     */
    public Message(final MessageType type, final Card refCard, final String refImage, final User user) {
        this();
        
        this.added = Time.now();
        this.body = type.getTemplate();
        this.cardId = (refCard != null) ? refCard.getId() : null;
        this.cardName = (refCard != null) ? refCard.toString() : null;
        this.imageName = (refImage != null) ? refImage : null;
        this.type = type;
        this.userId = (user != null) ? user.getId() : null;
        this.userName = (user != null) ? user.getEmail() : null;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param type
     * message type
     * @param refCard
     * relevant card (or <code>null</code>)
     * @param refImage
     * relevant image (or <code>null</code>)
     * @param user
     * relevant user (or <code>null</code>)
     * @param body
     * message text
     */
    public Message(final MessageType type, final Card refCard, final String refImage, final User user, final String body) {
        this(type, refCard, refImage, user);
        
        this.body = body;
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the message type flag.
     * 
     * @return message type flag
     */
    @JSONProperty(value = "TAG_message", readOnly = true)
    public boolean isMessage() {
        return true;
    }
    
    /**
     * Returns the message type.
     * 
     * @return the message type
     */
    public MessageType getType() {
        return this.type;
    }
    
    /**
     * Returns the event flag.
     * 
     * @return the event flag
     */
    @JSONProperty(value = "event", readOnly = true)
    public boolean isEventType() {
        return this.type.isEvent();
    }
    
    /**
     * Returns the time added.
     * 
     * @return the time added
     */
    public Time getAdded() {
        return this.added;
    }
    
    /**
     * Returns the card ID (or <code>null</code>).
     * 
     * @return the card ID
     */
    @JSONProperty(value = "card_id")
    public String getCardId() {
        return this.cardId;
    }
    
    /**
     * Returns the card name (or <code>null</code>).
     * 
     * @return the card name
     */
    @JSONProperty(value = "card_name")
    public String getCardName() {
        return this.cardName;
    }
    
    /**
     * Returns the user ID (or <code>null</code>).
     * 
     * @return the user ID
     */
    @JSONProperty(value = "user_id")
    public String getUserId() {
        return this.userId;
    }
    
    /**
     * Returns the user name (or <code>null</code>).
     * 
     * @return the user name
     */
    @JSONProperty(value = "user_name")
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * Returns the image name (or <code>null</code>).
     * 
     * @return the image name
     */
    @JSONProperty(value = "image_name")
    public String getImageName() {
        return this.imageName;
    }
    
    /**
     * Returns the message body.
     * 
     * @return the message body
     */
    public String getBody() {
        return this.body;
    }
    
    /**
     * Returns the ID of the user who confirmed this message (or
     * <code>null</code> if the message was not confirmed yet).
     * 
     * @return the user ID (or <code>null</code>)
     */
    @JSONProperty(value = "confirmed_user_id")
    public String getConfirmedByUserId() {
        return this.confirmedByUserId;
    }
    
    /**
     * Returns the confirmation time (or <code>null</code> if the message was
     * not confirmed yet).
     * 
     * @return the confirmation time (or <code>null)
     */
    public Time getConfirmed() {
        return this.confirmed;
    }
    
    // =======
    // SETTERS
    // =======
    
    /**
     * Sets the message type.
     * 
     * @param value
     * the message type
     */
    public void setType(final MessageType value) {
        this.type = value;
    }
    
    /**
     * Sets the time added.
     * 
     * @param value
     * time added
     */
    public void setAdded(final Time value) {
        this.added = value;
    }
    
    /**
     * Sets the card ID.
     * 
     * @param value
     * card ID or <code>null</code>
     */
    public void setCardId(final String value) {
        this.cardId = value;
    }
    
    /**
     * Sets the card name.
     * 
     * @param value
     * card name or <code>null</code>
     */
    public void setCardName(final String value) {
        this.cardName = value;
    }
    
    /**
     * Sets the user ID.
     * 
     * @param value
     * user ID or <code>null</code>
     */
    public void setUserId(final String value) {
        this.userId = value;
    }
    
    /**
     * Sets the user name.
     * 
     * @param value
     * user name or <code>null</code>
     */
    public void setUserName(final String value) {
        this.userName = value;
    }
    
    /**
     * Sets the image name.
     * 
     * @param value
     * image name or <code>null</code>
     */
    public void setImageName(final String value) {
        this.imageName = value;
    }
    
    /**
     * Sets the message body.
     * 
     * @param value
     * message body
     */
    public void setBody(final String value) {
        this.body = value;
    }
    
    /**
     * Sets the ID of a user that confirmed this message. Accepts
     * <code>null</code> if the message was not confirmed yet.
     * 
     * @param value
     * ID of a user or <code>null</code>
     */
    public void setConfirmedByUserId(final String value) {
        this.confirmedByUserId = value;
    }
    
    /**
     * Sets the confirmation time.
     * 
     * @param value
     * the confirmation time or <code>null</code>
     */
    public void setConfirmed(final Time value) {
        this.confirmed = value;
    }
    
    /**
     * Confirms the message.
     * 
     * @param confirmingUserId
     * confirming user
     */
    @JSONProperty(ignore = true)
    public void confirm(final String confirmingUserId) {
        this.confirmedByUserId = confirmingUserId;
        this.confirmed = Time.now();
    }
    
    /**
     * Unconfirms the message.
     */
    @JSONProperty(ignore = true)
    public void unconfirm() {
        this.confirmed = null;
        this.confirmedByUserId = null;
    }
}
