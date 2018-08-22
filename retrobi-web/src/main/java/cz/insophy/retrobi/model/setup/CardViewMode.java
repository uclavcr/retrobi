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

package cz.insophy.retrobi.model.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.insophy.retrobi.database.entity.type.UserRole;

/**
 * Card view mode.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum CardViewMode {
    /**
     * basic mode
     */
    BASIC("Atributy", UserRole.GUEST, UserRole.ADMIN),
    /**
     * attribute mode
     */
    ATTRIBUTE("Položkový rozpis", UserRole.EDITOR, UserRole.ADMIN),
    /**
     * OCR mode
     */
    OCR("OCR a přepis", UserRole.USER, UserRole.ADMIN),
    /**
     * message mode
     */
    MESSAGE("Hlášení", UserRole.GUEST, UserRole.ADMIN),
    /**
     * comment mode
     */
    COMMENT("Komentáře", UserRole.USER, UserRole.ADMIN),
    /**
     * image mode
     */
    IMAGE("Obrázky", UserRole.ADMIN, UserRole.ADMIN),
    /**
     * move mode
     */
    MOVE("Přesun", UserRole.ADMIN, UserRole.ADMIN),
    /**
     * message help mode
     */
    HELP_MESSAGE("Nápověda k hlášení", UserRole.GUEST, UserRole.USER),
    /**
     * card help mode
     */
    HELP_CARD("Nápověda k lístku", UserRole.GUEST, UserRole.USER);
    /**
     * mode title
     */
    private final String title;
    /**
     * minimal user role for this mode
     */
    private final UserRole minRole;
    /**
     * maximal user role for this mode
     */
    private final UserRole maxRole;
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * title
     * @param minRole
     * minimal role
     * @param maxRole
     * maximal role
     */
    private CardViewMode(final String title, final UserRole minRole, final UserRole maxRole) {
        this.title = title;
        this.minRole = minRole;
        this.maxRole = maxRole;
    }
    
    /**
     * Returns the title.
     * 
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Returns the minimal role.
     * 
     * @return the minimal role
     */
    public UserRole getMinRole() {
        return this.minRole;
    }
    
    /**
     * Returns the maximal role.
     * 
     * @return the maximal role
     */
    public UserRole getMaxRole() {
        return this.maxRole;
    }
    
    /**
     * Returns all values available for the specified user role.
     * 
     * @param userRole
     * user role
     * @return all values available for the user role
     */
    public static List<CardViewMode> valuesForRole(final UserRole userRole) {
        final List<CardViewMode> result = new ArrayList<CardViewMode>(CardViewMode.values().length);
        
        for (final CardViewMode mode : CardViewMode.values()) {
            final boolean hasMin = userRole.ordinal() >= mode.getMinRole().ordinal();
            final boolean hasMax = userRole.ordinal() <= mode.getMaxRole().ordinal();
            
            if (hasMin && hasMax) {
                result.add(mode);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
}
