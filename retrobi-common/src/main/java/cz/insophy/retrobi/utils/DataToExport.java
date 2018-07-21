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

package cz.insophy.retrobi.utils;

import java.util.Collections;
import java.util.List;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Comment;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.utils.type.DownloadImageQuality;
import cz.insophy.retrobi.utils.type.DownloadTextSource;

/**
 * Data related to export.
 * 
 * @author Vojtěch Hordějčuk
 */
public class DataToExport {
    /**
     * card to export
     */
    private final Card card;
    /**
     * attribute tree root
     */
    private final AttributeNode tree;
    /**
     * list of image names
     */
    private final List<String> imageNames;
    /**
     * list of comments
     */
    private final List<Comment> comments;
    /**
     * image quality
     */
    private final DownloadImageQuality imageQuality;
    /**
     * text source
     */
    private final DownloadTextSource textSource;
    
    /**
     * Creates a new instance.
     * 
     * @param card
     * card
     * @param tree
     * attribute tree or <code>null</code>
     * @param imageNames
     * list of image names
     * @param comments
     * list of comments
     * @param imageQuality
     * image quality
     * @param textSource
     * text source
     */
    public DataToExport(final Card card, final AttributeNode tree, final List<String> imageNames, final List<Comment> comments, final DownloadImageQuality imageQuality, final DownloadTextSource textSource) {
        this.card = card;
        this.tree = tree;
        this.imageNames = imageNames;
        this.comments = comments;
        this.imageQuality = imageQuality;
        this.textSource = textSource;
    }
    
    /**
     * Returns the card.
     * 
     * @return the card
     */
    public Card getCard() {
        return this.card;
    }
    
    /**
     * Returns the attribute tree root or <code>null</code>.
     * 
     * @return the attribute tree root
     */
    public AttributeNode getTree() {
        return this.tree;
    }
    
    /**
     * Returns the list of image names.
     * 
     * @return list of image names
     */
    public List<String> getImageNames() {
        return Collections.unmodifiableList(this.imageNames);
    }
    
    /**
     * Returns the list of comments.
     * 
     * @return list of comments
     */
    public List<Comment> getComments() {
        return Collections.unmodifiableList(this.comments);
    }
    
    /**
     * Returns the image quality.
     * 
     * @return the image quality
     */
    public DownloadImageQuality getImageQuality() {
        return this.imageQuality;
    }
    
    /**
     * Returns the text source.
     * 
     * @return the text source
     */
    public DownloadTextSource getTextSource() {
        return this.textSource;
    }
}
