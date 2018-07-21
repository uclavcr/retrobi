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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.database.entity.type.ImageFlag;

/**
 * @author Vojtěch Hordějčuk
 */
public class ImageNameTest {
    /**
     * test
     */
    @Test
    public final void testAddRemove() {
        Assert.assertEquals("1x", ImageFlag.CROSSOUT.addToImageName("1"));
        Assert.assertEquals("2sx", ImageFlag.CROSSOUT.addToImageName("2s"));
        Assert.assertEquals("3dx", ImageFlag.CROSSOUT.addToImageName("3dx"));
        
        Assert.assertEquals("1", ImageFlag.CROSSOUT.removeFromImageName("1"));
        Assert.assertEquals("2s", ImageFlag.CROSSOUT.removeFromImageName("2s"));
        Assert.assertEquals("3d", ImageFlag.CROSSOUT.removeFromImageName("3dx"));
    }
    
    /**
     * test
     */
    @Test
    public final void testProduceName() {
        Assert.assertEquals("1", ImageFlag.produceImageName(1));
        Assert.assertEquals("5o", ImageFlag.produceImageName(5, ImageFlag.ORIGINAL));
        Assert.assertEquals("10x", ImageFlag.produceImageName(10, ImageFlag.CROSSOUT));
        Assert.assertEquals("1ox", ImageFlag.produceImageName(1, ImageFlag.CROSSOUT, ImageFlag.ORIGINAL));
        Assert.assertEquals("9os", ImageFlag.produceImageName(9, ImageFlag.SYNTHESIZED, ImageFlag.ORIGINAL, ImageFlag.SYNTHESIZED));
        Assert.assertEquals("5o", ImageFlag.produceImageName(5, ImageFlag.ORIGINAL, ImageFlag.ORIGINAL, ImageFlag.ORIGINAL));
        Assert.assertEquals("624osx", ImageFlag.produceImageName(624, ImageFlag.ORIGINAL, ImageFlag.CROSSOUT, ImageFlag.SYNTHESIZED, ImageFlag.CROSSOUT, ImageFlag.SYNTHESIZED));
    }
    
    /**
     * test
     */
    @Test
    public final void testFilter() {
        Assert.assertEquals(
                Arrays.asList("1o", "3o"),
                ImageFlag.filterImageNames(Arrays.asList("1o", "2", "3o", "4", "5"), ImageFlag.ORIGINAL));
        
        Assert.assertEquals(
                Arrays.asList("5xo", "9o", "3o", "5ooo"),
                ImageFlag.filterImageNames(Arrays.asList("5xo", "9o", "2", "3o", "4", "5ooo", "6xs"), ImageFlag.ORIGINAL));
        
        Assert.assertEquals(
                Arrays.asList("3osx"),
                ImageFlag.filterImageNames(Arrays.asList("1o", "2", "3osx", "4", "5"), ImageFlag.SYNTHESIZED));
    }
}
