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

package cz.insophy.retrobi.database.entity.type;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Message type.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum MessageType {
    /**
     * an OCR updated
     */
    EVENT_OCR_UPDATED("Změna OCR", "Přepis OCR byl změněn.", true),
    /**
     * segments updated
     */
    EVENT_SEGMENTS_UPDATED("Změna segmentace", "Segmentace byla změněna.", true),
    /**
     * card note updated
     */
    EVENT_NOTE_UPDATED("Změna poznámky", "Poznámka byla změněna.", true),
    /**
     * card URL updated
     */
    EVENT_URL_UPDATED("Změna URL", "Adresa URL byla změněna.", true),
    /**
     * a card state was upgraded
     */
    EVENT_STATE_UPDATED("Změna stavu lístku", "Lístek byl převeden do vyššího stavu.", true),
    /**
     * a card created
     */
    EVENT_CARD_CREATED("Lístek založen", "Byl založen nový lístek.", true),
    /**
     * a card removed
     */
    EVENT_CARD_REMOVED("Lístek smazán", "Lístek byl odstraněn.", true),
    /**
     * a card modified by batch operation
     */
    EVENT_CARD_MODIFIED("Hromadná operace", "Nad lístkem byla provedena hromadná operace.", false),
    /**
     * multiple cards modified by batch operation
     */
    EVENT_MULTIPLE_CARDS_MODIFIED("Hromadná operace (více lístků)", "Byla provedena hromadná operace nad více lístky.", true),
    /**
     * a card moved in catalog
     */
    EVENT_CARD_MOVED("Hromadný přesun", "Lístek byl přesunut.", false),
    /**
     * multiple cards moved in catalog
     */
    EVENT_MULTIPLE_CARDS_MOVED("Hromadný přesun (více lístků)", "Byl proveden hromadný přesun lístků.", true),
    /**
     * a new user has registered
     */
    EVENT_USER_REGISTERED("Registrace uživatele", "Uživatel se registroval.", true),
    /**
     * an existing user was removed
     */
    EVENT_USER_REMOVED("Uživatel smazán", "Uživatel byl odstraněn.", true),
    /**
     * a card image was crossed-out
     */
    EVENT_IMAGE_CROSS_ON("Zaškrtnutí lístku", "Lístek byl zaškrtnut.", true),
    /**
     * a card image crossing was canceled
     */
    EVENT_IMAGE_CROSS_OFF("Zrušeno zaškrtnutí lístku", "Zaškrtnutí lístku bylo zrušeno.", true),
    /**
     * wrong card order
     */
    PROBLEM_CARD_ORDER("Chybné zařazení lístku", "Lístek je chybně zařazen. Měl by být zařazen v { __________ }.", true),
    /**
     * wrong card edit (OCR fix, segmentation)
     */
    PROBLEM_CARD_EDIT("Chybný přepis lístku", "V přepisu lístku je chyba v { __________ }.", true),
    /**
     * a card image is empty or invalid
     */
    PROBLEM_IMAGE_EMPTY("Prázdná stránka lístku / makulatura", "Obrázek je prázdný nebo se jedná o makulaturu.", true),
    /**
     * invalid image rotation
     */
    PROBLEM_IMAGE_ROTATION("Špatně otočený lístek", "Lístek je otočený nebo jinak nesprávně orientovaný.", true),
    /**
     * general card problem
     */
    PROBLEM_CARD_GENERAL("Jiná chyba na lístku", "Na lístku je chyba. Podrobnější popis: { __________ }", true),
    /**
     * general problem (bug, suggestion, etc.)
     */
    PROBLEM_GENERAL("Chyba aplikace", "Popiště prosím chybu co nejpodrobněji. Děkujeme.", true);
    
    /**
     * message title
     */
    private final String title;
    /**
     * message text (template)
     */
    private final String template;
    /**
     * save message in database
     */
    private final boolean saveInDatabase;
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * message title
     * @param template
     * message text template
     * @param saveInDatabase
     * save message in the database
     */
    private MessageType(final String title, final String template, final boolean saveInDatabase) {
        this.title = title;
        this.template = template;
        this.saveInDatabase = saveInDatabase;
    }
    
    /**
     * Checks if the constant is an event, not error report.
     * 
     * @return <code>true</code> if the constant is an event, <code>false</code>
     * otherwise
     */
    public boolean isEvent() {
        return this.name().startsWith("EVENT_");
    }
    
    /**
     * Checks if the message is saved in the database.
     * 
     * @return <code>true</code> if the message should be saved in database,
     * <code>false</code> if not
     */
    public boolean isSavingToDatabase() {
        return this.saveInDatabase;
    }
    
    /**
     * Returns the message template.
     * 
     * @return the template
     */
    public String getTemplate() {
        return this.template;
    }
    
    @Override
    public String toString() {
        return this.title;
    }
    
    /**
     * Returns a list of enumeration constants that are saved in database.
     * 
     * @return a list of constants that are saved in database
     */
    public static List<MessageType> valuesSavingInDatabase() {
        final List<MessageType> result = new LinkedList<MessageType>();
        
        for (final MessageType type : MessageType.values()) {
            if (type.isSavingToDatabase()) {
                result.add(type);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
}
