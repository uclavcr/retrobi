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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cz.insophy.retrobi.database.entity.type.ImageFlag;

/**
 * Image view mode.
 * 
 * @author Vojtěch Hordějčuk
 */
public enum ImageViewMode {
    /**
     * cropped images (including artificial ones)
     */
    CROPPED(true) {
        @SuppressWarnings("unchecked")
        @Override
        public List<String> filterImageNames(final List<String> imageNames, final boolean detailedView) {
            return ImageViewMode.getFirstNotEmpty(
                    ImageViewMode.getSynthesizedImageNames(imageNames),
                    ImageViewMode.getOriginalImageNames(imageNames),
                    imageNames);
        }
    },
    /**
     * full images (including artificial ones)
     */
    FULL(false) {
        @Override
        public List<String> filterImageNames(final List<String> imageNames, final boolean detailedView) {
            return CROPPED.filterImageNames(imageNames, detailedView);
        }
    },
    /**
     * cropped images (only original ones)
     */
    CROPPED_ORIGINAL(true) {
        @SuppressWarnings("unchecked")
        @Override
        public List<String> filterImageNames(final List<String> imageNames, final boolean detailedView) {
            return ImageViewMode.getFirstNotEmpty(
                    ImageViewMode.getOriginalImageNames(imageNames),
                    imageNames);
        }
    },
    /**
     * full images (only original ones)
     */
    FULL_ORIGINAL(false) {
        @Override
        public List<String> filterImageNames(final List<String> imageNames, final boolean detailedView) {
            return CROPPED_ORIGINAL.filterImageNames(imageNames, detailedView);
        }
    },
    /**
     * text representation
     */
    TEXT(false) {
        @Override
        public List<String> filterImageNames(final List<String> imageNames, final boolean detailedView) {
            if (detailedView) {
                // text mode in detail must return images
                return FULL.filterImageNames(imageNames, detailedView);
            }
            
            return Collections.emptyList();
        }
    },
    /**
     * simple table representation
     */
    TABLE(false) {
        @Override
        public List<String> filterImageNames(final List<String> imageNames, final boolean detailedView) {
            return TEXT.filterImageNames(imageNames, detailedView);
        }
    };
    
    /**
     * image crop flag
     */
    private final boolean crop;
    
    /**
     * Creates a new instance.
     * 
     * @param crop
     * image crop flag
     */
    private ImageViewMode(final boolean crop) {
        this.crop = crop;
    }
    
    /**
     * Returns the crop flag.
     * 
     * @return crop flag
     */
    public boolean isCrop() {
        return this.crop;
    }
    
    /**
     * Returns a subset of the given image names that passes the view mode.
     * 
     * @param imageNames
     * original list of all image names
     * @param detailedView
     * detailed view enabled (if <code>true</code>, returns the image set
     * feasible for the detail view; if <code>false</code>, the default behavior
     * is used)
     * @return a subset of image names
     */
    abstract public List<String> filterImageNames(List<String> imageNames, boolean detailedView);
    
    /**
     * Returns synthesized image names from the given list.
     * 
     * @param imageNames
     * input image name list
     * @return synthesized image name list
     */
    private static List<String> getSynthesizedImageNames(final List<String> imageNames) {
        final List<String> result = new LinkedList<String>();
        
        for (final String imageName : imageNames) {
            if (ImageFlag.SYNTHESIZED.inImageName(imageName)) {
                result.add(imageName);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Returns original image names from the given list.
     * 
     * @param imageNames
     * input image name list
     * @return original image name list
     */
    private static List<String> getOriginalImageNames(final List<String> imageNames) {
        final List<String> result = new LinkedList<String>();
        
        for (final String imageName : imageNames) {
            if (ImageFlag.ORIGINAL.inImageName(imageName)) {
                result.add(imageName);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Returns the first non-empty list given are an argument.
     * 
     * @param <E>
     * list content type
     * @param lists
     * any number of lists
     * @return the first non-empty list from arguments
     */
    private static <E> List<E> getFirstNotEmpty(final List<E>... lists) {
        for (final List<E> list : lists) {
            if (!list.isEmpty()) {
                return list;
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "Náhledy lístků";
            case 1:
                return "Celé lístky";
            case 2:
                return "Náhledy lístků (originály)";
            case 3:
                return "Celé lístky (originály)";
            case 4:
                return "Textový přepis";
            case 5:
                return "Tabulka";
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
