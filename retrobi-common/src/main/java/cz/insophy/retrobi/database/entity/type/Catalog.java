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

/**
 * Card catalog.
 */
public enum Catalog {
    /**
     * catalog #0
     */
    A("Autorská část", "Autorská"),
    /**
     * catalog #1
     */
    O("Odkazová část", "Odkazová"),
    /**
     * catalog #2
     */
    P("Předmětová část", "Předmětová"),
    /**
     * catalog #3
     */
    DE("Dešifrační část", "Dešifrační"),
    /**
     * catalog #4
     */
    DS("Dešifrátová část", "Dešifrátová"),
    /**
     * catalog #5
     */
    IA("Ikonoautorská část", "Ikonoautorská"),
    /**
     * catalog #6
     */
    IO("Ikonoodkazová část", "Ikonoodkazová"),
    /**
     * catalog #7
     */
    IN("Ikonoanonymní část", "Ikonoanonymní"),
    /**
     * catalog #8
     */
    ID("Ikonodešifrační část", "Ikonodešifrační"),
    /**
     * catalog #9
     */
    IP("Ikonopředmětová část", "Ikonopředmětová");
    
    /**
     * long catalog title
     */
    private final String longTitle;
    /**
     * short catalog title
     */
    private final String shortTitle;
    
    /**
     * Creates a new instance.
     * 
     * @param longTitle
     * long title
     * @param shortTitle
     * short title
     */
    private Catalog(final String longTitle, final String shortTitle) {
        this.longTitle = longTitle;
        this.shortTitle = shortTitle;
    }
    
    /**
     * Returns the long title.
     * 
     * @return the long title
     */
    public String getLongTitle() {
        return this.longTitle;
    }
    
    /**
     * Returns the short title.
     * 
     * @return the short title
     */
    public String getShortTitle() {
        return this.shortTitle;
    }
    
    @Override
    public String toString() {
        return this.getLongTitle();
    }
}
