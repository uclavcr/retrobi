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
 * Enumeration of page text fragments. These fragments should be editable by
 * user. Each text key is either <b>SHORT</b> (prefix S) or <b>LONG</b> (prefix
 * L). The <b>SHORT</b> texts are in plain text, <b>LONG</b> texts are in
 * <code>XHTML 1.0 Strict</code>.
 */
public enum TextType {
    /**
     * support e-mail
     */
    S_EMAIL("E-mail", "retrobi@ucl.cas.cz"),
    /**
     * rules to be accepted by system users
     */
    S_RULES("Pravidla užívání", "Zde budou pravidla pro užívání systému."),
    /**
     * home page content (in XHTML)
     */
    L_HOMEPAGE("Úvodní stránka", "<p>Zde bude text úvodní stránky.</p>"),
    /**
     * about page content (in XHTML)
     */
    L_ABOUT("O katalogu", "<p>Zde bude popis projektu.</p>"),
    /**
     * statistics page content under card count table (in XHTML)
     */
    L_STATS_CARD_COUNT("Statistiky - počet lístků", "<p>Zde bude text stránky systémových statistik (pod počtem lístků).</p>"),
    /**
     * statistics page content under image count table (in XHTML)
     */
    L_STATS_IMAGE_COUNT("Statistiky - počet obrázků", "<p>Zde bude text stránky systémových statistik (pod počtem obrázků).</p>"),
    /**
     * statistics page content under values table (in XHTML)
     */
    L_STATS_VALUES("Statistiky - rejstřík", "<p>Zde bude text stránky systémových statistik (pod rejstříkem).</p>"),
    /**
     * lost password page content (in XHTML)
     */
    L_LOST_PASSWORD("Zapomenuté heslo", "<p>Zde bude text stránky pro zapomenuté heslo.</p>"),
    /**
     * catalog help content (in XHTML)
     */
    L_HELP_CATALOG("Nápověda k průchodu katalogem", "<p>Zde bude nápověda ke katalogu.</p>"),
    /**
     * card help content (in XHTML)
     */
    L_HELP_CARD("Nápověda k lístku", "<p>Zde bude nápověda k lístku.</p>"),
    /**
     * comment help content (in XHTML)
     */
    L_HELP_COMMENT("Nápověda ke komentářům", "<p>Zde bude nápověda ke komentářům.</p>"),
    /**
     * user help content (in XHTML)
     */
    L_HELP_USER("Nápověda k registraci", "<p>Zde bude nápověda k registraci uživatelů.</p>"),
    /**
     * message help content (in XHTML)
     */
    L_HELP_MESSAGE("Nápověda k uživatelským hlášením", "<p>Zde bude nápověda k uživatelským hlášením.</p>"),
    /**
     * OCR / segmentation help text (in XHTML)
     */
    L_HELP_OCR("Nápověda k přepisu OCR a segmentaci", "<p>Zde bude popis přepisu OCR a segmentace.</p>"),
    /**
     * OCR / segmentation help text (summary) (in XHTML)
     */
    L_HELP_OCR_SUMMARY("Nápověda k přepisu OCR a segmentaci (shrnutí)", "<p>Zde bude zkrácený popis přepisu OCR a segmentace.</p>"),
    /**
     * basket help content (in XHTML)
     */
    L_HELP_BASKET("Nápověda ke schránce", "<p>Zde bude nápověda ke schránce.</p>"),
    /**
     * search help page content (in XHTML)
     */
    L_HELP_SEARCH("Nápověda k vyhledávání", "<p>Zde bude nápověda k vyhledávání.</p>"),
    /**
     * search help text (summary) (in XHTML)
     */
    L_HELP_SEARCH_SUMMARY("Nápověda k vyhledávání (shrnutí)", "<p>Zde bude zkrácená nápověda k vyhledávání.</p>"),
    /**
     * application help text (in XHTML)
     */
    L_HELP_APPLICATION("Nápověda k aplikaci", "<p>Zde bude nápověda k aplikaci.</p>");
    
    /**
     * constant title
     */
    private final String title;
    /**
     * default text value (if not found in the database)
     */
    private final String defaultValue;
    
    /**
     * Returns a list of help text types.
     * 
     * @return help text types
     */
    public static List<TextType> helpValues() {
        final List<TextType> list = new LinkedList<TextType>();
        
        for (final TextType type : TextType.values()) {
            if (type.isHelp()) {
                list.add(type);
            }
        }
        
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param title
     * text title
     * @param defaultValue
     * default text value
     */
    private TextType(final String title, final String defaultValue) {
        this.title = title;
        this.defaultValue = defaultValue;
    }
    
    /**
     * Returns the default value.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }
    
    /**
     * Returns the key - lowercase substring of the enumeration constant name.
     * For example, if the name is <code>L_HELP_SOMETHING</code>, the result is
     * <code>help_something</code>.
     * 
     * @return the key
     */
    public String getKey() {
        return this.name().substring(2).toLowerCase();
    }
    
    /**
     * Checks if the text is in HTML.
     * 
     * @return <code>true</code> if the text is in HTML, <code>false</code>
     * otherwise
     */
    public boolean isHtml() {
        return this.name().charAt(0) == 'L';
    }
    
    /**
     * Checks if the text is a kind of help text.
     * 
     * @return <code>true</code> if the text is a kind of help text,
     * <code>false</code> otherwise
     */
    public boolean isHelp() {
        return this.name().startsWith("L_HELP_") && !this.name().endsWith("_SUMMARY");
    }
    
    @Override
    public String toString() {
        return this.title;
    }
}
