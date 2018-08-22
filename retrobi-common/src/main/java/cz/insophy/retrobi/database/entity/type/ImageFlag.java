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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Card image flag enumeration. Each card image can have multiple flags. The
 * flags are hidden in the image name and the absolute or relative order of the
 * flag code is unimportant.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum ImageFlag {
    /**
     * original image
     */
    ORIGINAL('o'),
    /**
     * synthesized image
     */
    SYNTHESIZED('s'),
    /**
     * crossed-out (stroked-out)
     */
    CROSSOUT('x');
    
    /**
     * code (part of the image name)
     */
    private final char code;
    
    /**
     * Creates a new instance.
     * 
     * @param code
     * flag code
     */
    private ImageFlag(final char code) {
        this.code = code;
    }
    
    /**
     * Adds this image flag to the image name specified.
     * 
     * @param imageName
     * original image name
     * @return a new image name (with this flag)
     */
    public String addToImageName(final String imageName) {
        return this.removeFromImageName(imageName) + this.code;
    }
    
    /**
     * Removes this image flag from the image name specified.
     * 
     * @param imageName
     * original image name
     * @return a new image name (without this flag)
     */
    public String removeFromImageName(final String imageName) {
        return imageName.replace(String.valueOf(this.code), "");
    }
    
    /**
     * Produces an image name by the page number, using the flags provided. The
     * resulting image name can look for example like this: <code>1o</code>,
     * <code>3s</code>, <code>5sx</code>, <code>3</code>, etc. The flags given
     * are sorted (by ordinal) and ensured to be unique, that is, each distinct
     * flag is at most once in the resulting image name.
     * 
     * @param page
     * page number
     * @param flags
     * flags (can be empty)
     * @return the name of the image
     */
    public static String produceImageName(final int page, final ImageFlag... flags) {
        if (flags.length < 1) {
            return String.valueOf(page);
        }
        
        // create a list of sorted unique flags
        
        final List<ImageFlag> temp = new LinkedList<ImageFlag>();
        
        for (final ImageFlag flag : flags) {
            if (!temp.contains(flag)) {
                temp.add(flag);
            }
        }
        
        Collections.sort(temp);
        
        // build the image name
        
        final StringBuilder buffer = new StringBuilder(10);
        buffer.append(String.valueOf(page));
        
        for (final ImageFlag flag : temp) {
            buffer.append(flag.code);
        }
        
        return buffer.toString();
    }
    
    /**
     * Checks if the image contains this flag.
     * 
     * @param imageName
     * image name
     * @return <code>true</code> if the image contains the flag,
     * <code>false</code> otherwise
     */
    public boolean inImageName(final String imageName) {
        return imageName.contains(String.valueOf(this.code));
    }
    
    /**
     * Utility method for getting image names with the flags specified.
     * 
     * @param imageNames
     * a source collection of image names
     * @param flag
     * desired flag
     * @return sorted list of card image names
     */
    public static List<String> filterImageNames(final Collection<String> imageNames, final ImageFlag flag) {
        final List<String> filteredNames = new LinkedList<String>();
        
        for (final String imageName : imageNames) {
            if (flag.inImageName(imageName)) {
                filteredNames.add(imageName);
            }
        }
        
        return Collections.unmodifiableList(filteredNames);
    }
    
    /**
     * Returns a list of all flag codes. The order is undefined.
     * 
     * @return all flag codes (no spaces, random order)
     */
    public static String codesForRegexp() {
        final StringBuffer b = new StringBuffer(ImageFlag.values().length);
        
        for (final ImageFlag f : ImageFlag.values()) {
            b.append(f.code);
        }
        
        return b.toString();
    }
    
    /**
     * Returns the flag code.
     * 
     * @return the code
     */
    public char getCode() {
        return this.code;
    }
    
    @Override
    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "Původní";
            case 1:
                return "Umělý";
            case 2:
                return "Škrtnutý";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
