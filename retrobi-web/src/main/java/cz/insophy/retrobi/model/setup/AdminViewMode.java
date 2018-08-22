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

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.link.BookmarkableAdminCardsLink;
import cz.insophy.retrobi.link.BookmarkableAdminMessagesLink;
import cz.insophy.retrobi.link.BookmarkableAdminReportsPageLink;
import cz.insophy.retrobi.link.BookmarkableAdminTextsPageLink;
import cz.insophy.retrobi.link.BookmarkableAdminToolsPageLink;
import cz.insophy.retrobi.link.BookmarkableAdminUsersPageLink;

/**
 * Administration mode.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum AdminViewMode {
    /**
     * message manager
     */
    MESSAGES("Hlášení", UserRole.ADMIN) {
        @Override
        public BookmarkablePageLink<?> createLink(final String id) {
            return new BookmarkableAdminMessagesLink(id);
        }
    },
    /**
     * card batch editor
     */
    CARDS("Hromadná editace", UserRole.EDITOR) {
        @Override
        public BookmarkablePageLink<?> createLink(final String id) {
            return new BookmarkableAdminCardsLink(id);
        }
    },
    /**
     * user manager
     */
    USERS("Uživatelé", UserRole.ADMIN) {
        @Override
        public BookmarkablePageLink<?> createLink(final String id) {
            return new BookmarkableAdminUsersPageLink(id);
        }
    },
    /**
     * text manager
     */
    TEXTS("Texty", UserRole.ADMIN) {
        @Override
        public BookmarkablePageLink<?> createLink(final String id) {
            return new BookmarkableAdminTextsPageLink(id);
        }
    },
    /**
     * reporting tool
     */
    REPORTS("Analýzy", UserRole.EDITOR) {
        @Override
        public BookmarkablePageLink<?> createLink(final String id) {
            return new BookmarkableAdminReportsPageLink(id);
        }
    },
    /**
     * tools for download
     */
    TOOLS("Nástroje", UserRole.ADMIN) {
        @Override
        public BookmarkablePageLink<?> createLink(final String id) {
            return new BookmarkableAdminToolsPageLink(id);
        }
    };
    
    /**
     * mode title
     */
    private final String title;
    /**
     * minimal role for displaying
     */
    private final UserRole minRole;
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * title
     * @param minRole
     * minimal role for displaying
     */
    private AdminViewMode(final String title, final UserRole minRole) {
        this.title = title;
        this.minRole = minRole;
    }
    
    /**
     * Returns the minimal role for using this mode.
     * 
     * @return the minimal user role for using this mode
     */
    public UserRole getMinRole() {
        return this.minRole;
    }
    
    /**
     * Creates a page link for the given enumeration constant.
     * 
     * @param id
     * component ID
     * @return link for the given constant
     */
    abstract public BookmarkablePageLink<?> createLink(String id);
    
    @Override
    public String toString() {
        return this.title;
    }
    
    /**
     * Returns all values available for the specified user role.
     * 
     * @param userRole
     * user role
     * @return all values available for the user role
     */
    public static List<AdminViewMode> valuesForRole(final UserRole userRole) {
        final List<AdminViewMode> result = new ArrayList<AdminViewMode>(AdminViewMode.values().length);
        
        for (final AdminViewMode mode : AdminViewMode.values()) {
            if (userRole.ordinal() >= mode.getMinRole().ordinal()) {
                result.add(mode);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
}
