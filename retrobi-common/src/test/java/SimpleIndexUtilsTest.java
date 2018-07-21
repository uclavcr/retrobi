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
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.type.AbstractCardIndex;
import cz.insophy.retrobi.utils.library.SimpleIndexUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleIndexUtilsTest {
    /**
     * test
     */
    @Test
    public void testMoveDefault() {
        final AttributePrototype c1 = new AttributePrototype("c1", "c1", false);
        c1.addIndex("i1");
        c1.addIndex("i123");
        final AttributePrototype c2 = new AttributePrototype("c2", "c2", false);
        c2.addIndex("i2");
        c2.addIndex("i123");
        final AttributePrototype c3 = new AttributePrototype("c3", "c3", false);
        c3.addIndex("i3");
        c3.addIndex("i123");
        final AttributePrototype p1 = new AttributePrototype("p1", "p1", false, c1, c2, c3);
        p1.addIndex("p1");
        
        Assert.assertEquals(2, c1.getIndexes().size());
        Assert.assertEquals(2, c2.getIndexes().size());
        Assert.assertEquals(2, c3.getIndexes().size());
        Assert.assertEquals(1, p1.getIndexes().size());
        
        final List<AbstractCardIndex> ic1 = SimpleIndexUtils.getIndexes(c1);
        Assert.assertEquals(11, ic1.size());
        
        final List<AbstractCardIndex> ic2 = SimpleIndexUtils.getIndexes(c2);
        Assert.assertEquals(11, ic2.size());
        
        final List<AbstractCardIndex> ic3 = SimpleIndexUtils.getIndexes(c3);
        Assert.assertEquals(11, ic3.size());
        
        final List<AbstractCardIndex> ip1 = SimpleIndexUtils.getIndexes(p1);
        Assert.assertEquals(14, ip1.size());
        
        final List<String> allowedIndexes = Arrays.asList(
                "basic_everything",
                "basic_batch",
                "basic_ocr_best",
                "basic_segment_all",
                "basic_ocr_fixed",
                "basic_ocr_original",
                "basic_segment_title",
                "basic_segment_bibliography",
                "basic_segment_annotation",
                "i1", "i2", "i3", "i123", "p1");
        
        for (final AbstractCardIndex ip1temp : ip1) {
            Assert.assertTrue(allowedIndexes.contains(ip1temp.getName()));
            Assert.assertTrue(ip1temp.getCode().startsWith("function (doc) {\n"));
            Assert.assertTrue(ip1temp.getCode().endsWith("}\n"));
        }
    }
}
