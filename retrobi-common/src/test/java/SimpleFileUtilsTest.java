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

import java.io.File;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.utils.library.SimpleFileUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleFileUtilsTest {
    /**
     * test
     */
    @Test
    public final void testChangeExtension() {
        Assert.assertEquals(new File("test.b"), SimpleFileUtils.changeExtension(new File("test.a"), "b"));
        Assert.assertEquals(new File("/dummy/test.zip"), SimpleFileUtils.changeExtension(new File("/dummy/test.pdf"), "zip"));
        Assert.assertEquals(new File("/dummy/test.zip"), SimpleFileUtils.changeExtension(new File("/dummy/test"), "zip"));
    }
    
    /**
     * test
     */
    @Test
    public void testExtractCatalogFromFile() {
        for (final Catalog catalog : Catalog.values()) {
            Assert.assertEquals(catalog, SimpleFileUtils.extractCatalogFromFile(new File("/dum-my/files/" + catalog.name() + "-Some-Thing.tif")));
        }
    }
    
    /**
     * test
     */
    @Test
    public final void testProduceImageFileName() {
        Assert.assertEquals("A-batch-1-1.png", SimpleFileUtils.produceImageFileName(Catalog.A, "batch", 1, 1, "png", false));
        Assert.assertEquals(SimpleFileUtils.EMPTY_IMAGE_PREFIX + "O-batch-1-1.png", SimpleFileUtils.produceImageFileName(Catalog.O, "batch", 1, 1, "png", true));
        Assert.assertEquals("O-1a1-100-42.tif", SimpleFileUtils.produceImageFileName(Catalog.O, "1a1", 100, 42, "tif", false));
        Assert.assertEquals(SimpleFileUtils.EMPTY_IMAGE_PREFIX + "IO-1a1-42-100.tif", SimpleFileUtils.produceImageFileName(Catalog.IO, "1a1", 42, 100, "tif", true));
    }
    
    /**
     * test
     */
    @Test
    public final void testExtractNameFromFile() {
        Assert.assertEquals("test", SimpleFileUtils.extractNameFromFile(new File("/dum-my/trap.jpg/test.")));
        Assert.assertEquals("this.is.filename.test", SimpleFileUtils.extractNameFromFile(new File("/dum-my/trap.jpg/this.is.filename.test.")));
        Assert.assertEquals("this.is.filename", SimpleFileUtils.extractNameFromFile(new File("/dum-my/this.is.filename.pdf")));
        Assert.assertEquals("123456x5", SimpleFileUtils.extractNameFromFile(new File("123456x5.tif")));
        Assert.assertEquals("123456", SimpleFileUtils.extractNameFromFile(new File("123456")));
    }
    
    /**
     * test
     */
    @Test
    public final void testExtractNumberFromFile() {
        Assert.assertEquals(new BigDecimal("15452154999"), SimpleFileUtils.extractNumberFromFile(new File("/dum-my/15452154999.png")));
        Assert.assertEquals(BigDecimal.ONE, SimpleFileUtils.extractNumberFromFile(new File("/dum-my/1aaa.png")));
        Assert.assertEquals(BigDecimal.ZERO, SimpleFileUtils.extractNumberFromFile(new File("/dum-my/0.png")));
        Assert.assertEquals(new BigDecimal("64"), SimpleFileUtils.extractNumberFromFile(new File("64")));
        Assert.assertEquals(new BigDecimal("64"), SimpleFileUtils.extractNumberFromFile(new File("/neco/00000000000000064")));
    }
    
    /**
     * test
     */
    @Test
    public final void testExtractDigitCount() {
        Assert.assertEquals(4, SimpleFileUtils.extractDigitCount(new File("/dum-my/file1234.png")));
        Assert.assertEquals(6, SimpleFileUtils.extractDigitCount(new File("/dum-my/000001.png")));
        Assert.assertEquals(6, SimpleFileUtils.extractDigitCount(new File("/dum-my/000001.png23")));
        Assert.assertEquals(10, SimpleFileUtils.extractDigitCount(new File("/dum-my/1234512345x4.png")));
        Assert.assertEquals(6, SimpleFileUtils.extractDigitCount(new File("/du1m-m1y/000001.png")));
        Assert.assertEquals(6, SimpleFileUtils.extractDigitCount(new File("/du1m-m1y/file123456cool123456")));
        Assert.assertEquals(6, SimpleFileUtils.extractDigitCount(new File("aaa123456bbb123")));
        Assert.assertEquals(6, SimpleFileUtils.extractDigitCount(new File("/a/b/1/c/5/4/7/d/000005x5.png")));
        Assert.assertEquals(0, SimpleFileUtils.extractDigitCount(new File("/a/b/1/c/5/4/7/d/helloworld")));
        Assert.assertEquals(0, SimpleFileUtils.extractDigitCount(new File("")));
    }
    
    /**
     * test
     */
    @Test
    public final void testExtractCardNumberFromFile() {
        Assert.assertEquals(1542, SimpleFileUtils.extractCardNumberFromFile(new File("/dum-my/batch-1542-547.png")));
        Assert.assertEquals(78474, SimpleFileUtils.extractCardNumberFromFile(new File("/dum-my/O-M. F. Bartoš-78474-55.png")));
        Assert.assertEquals(124, SimpleFileUtils.extractCardNumberFromFile(new File("/dum-my/A-124-1542.png")));
        Assert.assertEquals(42, SimpleFileUtils.extractCardNumberFromFile(new File("A-a-42-1.tif")));
        Assert.assertEquals(42, SimpleFileUtils.extractCardNumberFromFile(new File("O-b-42-2.tif")));
    }
    
    /**
     * test
     */
    @Test
    public final void testExtractPageNumberFromFile() {
        Assert.assertEquals(547, SimpleFileUtils.extractPageNumberFromFile(new File("/dum-my/AO-batch-1542-547.png")));
        Assert.assertEquals(1542, SimpleFileUtils.extractPageNumberFromFile(new File("/dum-my/A-124-1542.png")));
        Assert.assertEquals(55, SimpleFileUtils.extractPageNumberFromFile(new File("/dum-my/O-M. F. Bartoš-78474-55.png")));
        Assert.assertEquals(8, SimpleFileUtils.extractPageNumberFromFile(new File("AO-a-42-8.tif")));
        Assert.assertEquals(9, SimpleFileUtils.extractPageNumberFromFile(new File("AO-b-42-9.tif")));
        Assert.assertEquals(0, SimpleFileUtils.extractPageNumberFromFile(new File("AO-c-42-0.tif")));
    }
    
    /**
     * test
     */
    @Test
    public final void testExtractPaperCountFromFile() {
        Assert.assertEquals(6, SimpleFileUtils.extractPaperCountFromFile(new File("154244x6.tif")));
        Assert.assertEquals(1, SimpleFileUtils.extractPaperCountFromFile(new File("0x1.tif")));
        Assert.assertEquals(9, SimpleFileUtils.extractPaperCountFromFile(new File("1x9.tif")));
        Assert.assertEquals(1, SimpleFileUtils.extractPaperCountFromFile(new File("1.tif")));
        Assert.assertEquals(1, SimpleFileUtils.extractPaperCountFromFile(new File("3.tif")));
    }
    
    /**
     * test
     */
    @Test
    public final void testReplaceRoot() {
        final File r1 = new File("root1"); // "/root1/"
        final File r1a = new File(r1, "a"); // "/root1/a/"
        final File r1b = new File(r1a, "b"); // "/root1/a/b/"
        final File r1c = new File(r1b, "c.txt"); // "/root1/a/b/c.txt"
        
        final File r2 = new File("root2"); // "/root2/"
        final File r2x = new File(r2, "x"); // "/root2/x/"
        final File r2y = new File(r2x, "y"); // "/root2/x/y/"
        
        final File f1 = new File(new File(r2y, "b"), "c.txt"); // "/root2/x/y/b/c.txt"
        Assert.assertEquals(f1, SimpleFileUtils.replaceRootDir(r1c, r1a, r2y));
        
        final File f2 = new File(r1, "c.txt"); // "/root1/c.txt"
        Assert.assertEquals(f2, SimpleFileUtils.replaceRootDir(r1c, r1b, r1));
    }
}
