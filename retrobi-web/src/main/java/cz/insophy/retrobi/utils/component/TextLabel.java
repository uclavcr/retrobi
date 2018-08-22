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

package cz.insophy.retrobi.utils.component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;

import cz.insophy.retrobi.RetrobiApplication;
import cz.insophy.retrobi.database.entity.type.TextType;
import cz.insophy.retrobi.link.BookmarkableAboutLink;
import cz.insophy.retrobi.link.BookmarkableBasketLink;
import cz.insophy.retrobi.link.BookmarkableCatalogLink;
import cz.insophy.retrobi.link.BookmarkableHelpLink;
import cz.insophy.retrobi.link.BookmarkableRegisterLink;
import cz.insophy.retrobi.link.BookmarkableSearchLink;
import cz.insophy.retrobi.link.BookmarkableStatsLink;
import cz.insophy.retrobi.utils.Tuple;

/**
 * Label containing a dynamic text (both plain text and HTML). The content is
 * loaded from the database by the text key provided and tag replacement is
 * done. If no such text exists, a dummy text will be used.
 * 
 * @author Vojtěch Hordějčuk
 */
public class TextLabel extends Label {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * text type displayed
     */
    private final TextType type;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param key
     * text key
     */
    public TextLabel(final String id, final TextType key) {
        super(id);
        
        this.type = key;
        final String text = RetrobiApplication.db().getTextRepository().getText(key);
        
        if (this.type.isHtml()) {
            this.setEscapeModelStrings(false);
            this.setDefaultModel(Model.of(this.replaceTags(text)));
        } else {
            this.setEscapeModelStrings(true);
            this.setDefaultModel(Model.of(text));
        }
    }
    
    /**
     * Replaces special tags in the text.
     * 
     * @param text
     * text to do the replacement in
     * @return resulting text
     */
    private String replaceTags(final String text) {
        String result = text;
        
        for (final Tuple<String, BookmarkablePageLink<?>> tag : TextLabel.getTags()) {
            result = this.replace(result, tag.getFirst(), tag.getSecond());
        }
        
        return result;
    }
    
    /**
     * Replaces the special tag in the given text by a provided link.
     * 
     * @param str
     * string to do the replacement in
     * @param tag
     * a tag to replace
     * @param link
     * link to extract the page class and parameters from
     * @return resulting text
     */
    private String replace(final String str, final String tag, final BookmarkablePageLink<?> link) {
        final String url = RequestCycle.get().urlFor(link.getPageClass(), link.getPageParameters()).toString();
        return str.replace(TextLabel.getTagKey(tag), HtmlLabel.escapeHtml(url));
    }
    
    /**
     * Returns a list of available tag keys.
     * 
     * @return list of tags
     */
    public static List<String> getTagKeys() {
        final List<String> tags = new LinkedList<String>();
        
        for (final Tuple<String, BookmarkablePageLink<?>> tag : TextLabel.getTags()) {
            tags.add(TextLabel.getTagKey(tag.getFirst()));
        }
        
        return Collections.unmodifiableList(tags);
    }
    
    /**
     * Returns the tag key.
     * 
     * @param tag
     * tag
     * @return tag key (<code>{tag}</code>)
     */
    private static String getTagKey(final String tag) {
        return "{" + tag + "}";
    }
    
    /**
     * Returns the lazy loaded map of page tags.
     * 
     * @return a list of page tags
     */
    private static List<Tuple<String, BookmarkablePageLink<?>>> getTags() {
        final List<Tuple<String, BookmarkablePageLink<?>>> tags = new LinkedList<Tuple<String, BookmarkablePageLink<?>>>();
        
        tags.add(TextLabel.createTag("page:about", new BookmarkableAboutLink("TEMP")));
        tags.add(TextLabel.createTag("page:basket", new BookmarkableBasketLink("TEMP")));
        tags.add(TextLabel.createTag("page:catalog", new BookmarkableCatalogLink("TEMP")));
        tags.add(TextLabel.createTag("page:help", new BookmarkableHelpLink("TEMP")));
        tags.add(TextLabel.createTag("page:register", new BookmarkableRegisterLink("TEMP")));
        tags.add(TextLabel.createTag("page:search", new BookmarkableSearchLink("TEMP")));
        tags.add(TextLabel.createTag("page:stats", new BookmarkableStatsLink("TEMP")));
        
        for (final TextType t : TextType.values()) {
            if (t.isHelp()) {
                tags.add(TextLabel.createTag(t.getKey(), new BookmarkableHelpLink("TEMP", t)));
            }
        }
        
        return Collections.unmodifiableList(tags);
    }
    
    /**
     * Creates a tag.
     * 
     * @param key
     * tag key
     * @param link
     * page link
     * @return tuple (tag key, page link) for use in replacement method
     */
    @SuppressWarnings("unchecked")
    private static Tuple<String, BookmarkablePageLink<?>> createTag(final String key, final BookmarkablePageLink<?> link) {
        final Tuple<String, ?> tuple = Tuple.of(key, link);
        return (Tuple<String, BookmarkablePageLink<?>>) tuple;
    }
}
