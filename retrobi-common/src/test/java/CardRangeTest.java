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
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.utils.CardRange;
import cz.insophy.retrobi.utils.DataLoader;

/**
 * test
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardRangeTest {
    /**
     * testing data loader
     */
    private static final DataLoader<Integer> test = new DataLoader<Integer>() {
        @Override
        public List<Integer> loadData(final int offset, final int limit) {
            if ((offset < 0) || (limit <= 0)) {
                Assert.fail();
            }
            
            final List<Integer> l = new LinkedList<Integer>();
            
            for (int i = 0; i < limit; i++) {
                l.add(offset + i);
            }
            
            return l;
        }
    };
    
    /**
     * test
     */
    @Test
    public final void testLower1() {
        final CardRange range = new CardRange(0, 10, 10);
        
        Assert.assertTrue(range.hasLower(0));
        Assert.assertTrue(range.hasLower(1));
        Assert.assertTrue(range.hasLower(2));
        Assert.assertTrue(range.hasLower(3));
        Assert.assertTrue(range.hasLower(4));
        Assert.assertTrue(range.hasLower(5));
        Assert.assertTrue(range.hasLower(6));
        Assert.assertTrue(range.hasLower(7));
        Assert.assertTrue(range.hasLower(8));
        Assert.assertTrue(range.hasLower(9));
        Assert.assertFalse(range.hasLower(10));
        Assert.assertFalse(range.hasLower(-1));
        Assert.assertFalse(range.hasLower(50));
    }
    
    /**
     * test
     */
    @Test
    public final void testLower2() {
        final CardRange range = new CardRange(0, 86, 10);
        
        Assert.assertEquals(86, range.getCount());
        Assert.assertEquals(0, range.getFirstOffset());
        Assert.assertEquals(85, range.getLastOffset());
        Assert.assertEquals(10, range.getLimit());
        Assert.assertEquals(100, range.getRange());
        Assert.assertEquals(10, range.getStep());
        
        Assert.assertTrue(range.hasLower(0));
        Assert.assertTrue(range.hasLower(1));
        Assert.assertTrue(range.hasLower(2));
        Assert.assertTrue(range.hasLower(3));
        Assert.assertTrue(range.hasLower(4));
        Assert.assertTrue(range.hasLower(5));
        Assert.assertTrue(range.hasLower(6));
        Assert.assertTrue(range.hasLower(7));
        Assert.assertTrue(range.hasLower(8));
        
        Assert.assertFalse(range.hasLower(9));
        Assert.assertFalse(range.hasLower(10));
        Assert.assertFalse(range.hasLower(-1));
        Assert.assertFalse(range.hasLower(50));
        
        final CardRange r0 = range.createLower(0);
        Assert.assertEquals(0, r0.getFirstOffset());
        Assert.assertEquals(9, r0.getLastOffset());
        Assert.assertEquals(86, r0.getCount());
        Assert.assertEquals(10, r0.getRange());
        Assert.assertEquals(1, r0.getStep());
        
        Assert.assertTrue(r0.hasLower(0));
        Assert.assertTrue(r0.hasLower(5));
        Assert.assertTrue(r0.hasLower(9));
        Assert.assertFalse(r0.hasLower(10));
        
        final CardRange r3 = range.createLower(3);
        Assert.assertEquals(30, r3.getFirstOffset());
        Assert.assertEquals(39, r3.getLastOffset());
        Assert.assertEquals(86, r3.getCount());
        Assert.assertEquals(10, r3.getRange());
        Assert.assertEquals(1, r3.getStep());
        
        final CardRange r8 = range.createLower(8);
        Assert.assertEquals(80, r8.getFirstOffset());
        Assert.assertEquals(85, r8.getLastOffset());
        Assert.assertEquals(86, r8.getCount());
        Assert.assertEquals(10, r8.getRange());
        Assert.assertEquals(1, r8.getStep());
        
        Assert.assertTrue(r8.hasLower(0));
        Assert.assertTrue(r8.hasLower(1));
        Assert.assertTrue(r8.hasLower(2));
        Assert.assertTrue(r8.hasLower(3));
        Assert.assertTrue(r8.hasLower(4));
        Assert.assertTrue(r8.hasLower(5));
        Assert.assertFalse(r8.hasLower(6));
        Assert.assertFalse(r8.hasLower(7));
        Assert.assertFalse(r8.hasLower(8));
        Assert.assertFalse(r8.hasLower(9));
        Assert.assertFalse(r8.hasLower(10));
        Assert.assertFalse(r8.hasLower(-5));
        Assert.assertFalse(r8.hasLower(100));
    }
    
    /**
     * test
     */
    @Test
    public final void testFlat() {
        final CardRange cr10 = new CardRange(0, 10, 10, true);
        Assert.assertEquals(10, cr10.getRange());
        Assert.assertTrue(cr10.isFlat());
        Assert.assertFalse(cr10.hasUpper());
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(cr10.hasLower(i));
        }
        
        final CardRange cr100 = new CardRange(0, 100, 10, true);
        Assert.assertEquals(10, cr100.getRange());
        Assert.assertTrue(cr100.isFlat());
        Assert.assertFalse(cr100.hasUpper());
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(cr100.hasLower(i));
        }
        
        final CardRange cr1000 = new CardRange(0, 1000, 10, true);
        Assert.assertEquals(10, cr1000.getRange());
        Assert.assertTrue(cr1000.isFlat());
        Assert.assertFalse(cr1000.hasUpper());
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(cr1000.hasLower(i));
        }
    }
    
    /**
     * test
     */
    @Test
    public final void testSingleStep() {
        final CardRange cr0 = new CardRange(0, 0, 10);
        Assert.assertEquals(Arrays.asList(), cr0.useForPick(CardRangeTest.test));
        Assert.assertTrue(cr0.isSingle());
        Assert.assertTrue(cr0.isSingleStep());
        Assert.assertEquals(0, cr0.getStep());
        
        final CardRange cr1 = new CardRange(0, 1, 10);
        Assert.assertEquals(Arrays.asList(0), cr1.useForPick(CardRangeTest.test));
        Assert.assertTrue(cr1.isSingle());
        Assert.assertTrue(cr1.isSingleStep());
        Assert.assertEquals(0, cr1.getStep());
        
        final CardRange cr10 = new CardRange(0, 10, 10);
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cr10.useForPick(CardRangeTest.test));
        Assert.assertFalse(cr10.isSingle());
        Assert.assertTrue(cr10.isSingleStep());
        Assert.assertEquals(1, cr10.getStep());
        
        final CardRange cr100 = new CardRange(0, 100, 10);
        Assert.assertEquals(Arrays.asList(0, 10, 20, 30, 40, 50, 60, 70, 80, 90), cr100.useForPick(CardRangeTest.test));
        Assert.assertFalse(cr100.isSingle());
        Assert.assertFalse(cr100.isSingleStep());
        Assert.assertEquals(10, cr100.getStep());
        
        final CardRange cr100b = new CardRange(30, 100, 10);
        Assert.assertEquals(Arrays.asList(30, 40, 50, 60, 70, 80, 90), cr100b.useForPick(CardRangeTest.test));
        Assert.assertFalse(cr100b.isSingle());
        Assert.assertFalse(cr100b.isSingleStep());
        Assert.assertEquals(10, cr100b.getStep());
    }
    
    /**
     * test
     */
    @Test
    public final void testHighest() {
        Assert.assertEquals(1, new CardRange(0, 0, 10).getRange());
        
        int range = 1;
        int count = 1;
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < count - 1; j += range / 10) {
                Assert.assertEquals(range, new CardRange(j, count, 10).getRange());
            }
            range = range * 10;
            count = count * 10;
        }
    }
    
    /**
     * test
     */
    @Test
    public final void testCount() {
        Assert.assertEquals(Arrays.asList(), new CardRange(0, 0, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0), new CardRange(0, 1, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 1), new CardRange(0, 2, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 1, 2), new CardRange(0, 3, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3), new CardRange(0, 4, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4), new CardRange(0, 5, 10).useForPick(CardRangeTest.test));
        
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), new CardRange(0, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 10), new CardRange(0, 19, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 10), new CardRange(0, 20, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 10, 20), new CardRange(0, 21, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 10, 20, 30, 40, 50, 60, 70, 80), new CardRange(0, 84, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(0, 10, 20, 30, 40, 50, 60, 70, 80, 90), new CardRange(0, 94, 10).useForPick(CardRangeTest.test));
        
        Assert.assertEquals(Arrays.asList(131, 132, 133, 134, 135, 136, 137, 138), new CardRange(131, 139, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 132, 133, 134, 135, 136, 137, 138, 139), new CardRange(131, 140, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 132, 133, 134, 135, 136, 137, 138, 139, 140), new CardRange(131, 141, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 132, 133, 134, 135, 136, 137, 138, 139, 140), new CardRange(131, 142, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 132, 133, 134, 135, 136, 137, 138, 139, 140), new CardRange(131, 144, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 132, 133, 134, 135, 136, 137, 138, 139, 140), new CardRange(131, 154, 10, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 141), new CardRange(131, 149, 100, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 141), new CardRange(131, 150, 100, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 141), new CardRange(131, 151, 100, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(131, 141, 151), new CardRange(131, 152, 100, 10).useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(151), new CardRange(151, 152, 100, 10).useForPick(CardRangeTest.test));
    }
    
    /**
     * test
     */
    @Test
    public final void testSurrounding() {
        final CardRange cr = new CardRange(300, 1000, 100, 10);
        
        Assert.assertTrue(cr.hasMoreCards());
        Assert.assertTrue(cr.hasPrevious());
        Assert.assertTrue(cr.hasNext());
        Assert.assertTrue(cr.hasUpper());
        Assert.assertTrue(cr.hasLower(0));
        Assert.assertTrue(cr.hasLower(1));
        Assert.assertTrue(cr.hasLower(2));
        Assert.assertTrue(cr.hasLower(3));
        Assert.assertTrue(cr.hasLower(4));
        Assert.assertTrue(cr.hasLower(5));
        Assert.assertTrue(cr.hasLower(6));
        Assert.assertTrue(cr.hasLower(7));
        Assert.assertTrue(cr.hasLower(8));
        Assert.assertTrue(cr.hasLower(9));
        Assert.assertFalse(cr.hasLower(10));
        Assert.assertFalse(cr.hasLower(-1));
        
        final CardRange crFirst = cr.createFirst();
        final CardRange crLast = cr.createLast();
        
        Assert.assertEquals(Arrays.asList(0), crFirst.useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(999), crLast.useForPick(CardRangeTest.test));
        
        Assert.assertEquals(Arrays.asList(200, 210, 220, 230, 240, 250, 260, 270, 280, 290), cr.createPrevious().useForPick(CardRangeTest.test));
        Assert.assertEquals(Arrays.asList(400, 410, 420, 430, 440, 450, 460, 470, 480, 490), cr.createNext().useForPick(CardRangeTest.test));
        
        for (int i = 0; i < 10; i++) {
            final int range = 10 * i;
            
            Assert.assertEquals(
                    Arrays.asList(300 + range, 301 + range, 302 + range, 303 + range, 304 + range, 305 + range, 306 + range, 307 + range, 308 + range, 309 + range),
                    cr.createLower(i).useForPick(CardRangeTest.test));
            
            for (int j = 0; j < 10; j++) {
                final CardRange lower = cr.createLower(i).createLower(j);
                Assert.assertEquals(Arrays.asList(300 + range + j), lower.useForPick(CardRangeTest.test));
                Assert.assertTrue(lower.isSingle());
            }
            
            Assert.assertTrue(cr.createLower(i).hasUpper());
            Assert.assertEquals(cr.useForPick(CardRangeTest.test), cr.createLower(i).createUpper().useForPick(CardRangeTest.test));
        }
        
        final CardRange cr1 = new CardRange(0, 1000, 100, 10);
        Assert.assertFalse(cr1.hasPrevious());
        Assert.assertTrue(cr1.hasNext());
        
        final CardRange cr2 = new CardRange(100, 1000, 100, 10);
        Assert.assertTrue(cr2.hasPrevious());
        Assert.assertTrue(cr2.hasNext());
        
        final CardRange cr3 = new CardRange(900, 1000, 100, 10);
        Assert.assertTrue(cr3.hasPrevious());
        Assert.assertFalse(cr3.hasNext());
        
        final CardRange cr4 = new CardRange(999, 1000, 100, 10);
        Assert.assertTrue(cr4.hasPrevious());
        Assert.assertFalse(cr4.hasNext());
    }
    
    /**
     * test
     */
    @Test
    public final void testUseForPick1() {
        // 1 to 10 step 1
        
        final CardRange f1t10s1 = new CardRange(0, 10, 10, 10);
        final List<Integer> l_f1t10s1 = new LinkedList<Integer>();
        
        for (int i = 0; i < 10; i++) {
            l_f1t10s1.add(i);
        }
        
        Assert.assertEquals(l_f1t10s1, f1t10s1.useForPick(CardRangeTest.test));
        
        // 1 to 100 step 10
        
        final CardRange f1t100s10 = new CardRange(0, 1000, 100, 10);
        final List<Integer> l_f1t100s10 = new LinkedList<Integer>();
        
        for (int i = 0; i < 10; i++) {
            l_f1t100s10.add(10 * i);
        }
        
        Assert.assertEquals(l_f1t100s10, f1t100s10.useForPick(CardRangeTest.test));
        
        // 5 to 105 step 10
        
        final CardRange f5t105s10 = new CardRange(5, 1000, 100, 10);
        final List<Integer> l_f5t105s10 = new LinkedList<Integer>();
        
        for (int i = 0; i < 10; i++) {
            l_f5t105s10.add(5 + 10 * i);
        }
        
        Assert.assertEquals(l_f5t105s10, f5t105s10.useForPick(CardRangeTest.test));
    }
    
    /**
     * test
     */
    @Test
    public final void testUseForPick2() {
        // 1 to 10
        
        final CardRange f1t10s2 = new CardRange(0, 100, 10, 5);
        final List<Integer> l_f1t10s2 = Arrays.asList(0, 2, 4, 6, 8);
        
        Assert.assertEquals(l_f1t10s2, f1t10s2.useForPick(CardRangeTest.test));
        
        // 7 to 17
        
        final CardRange f1t10s2b = new CardRange(7, 100, 10, 5);
        final List<Integer> l_f1t10s2b = Arrays.asList(7, 9, 11, 13, 15);
        
        Assert.assertEquals(l_f1t10s2b, f1t10s2b.useForPick(CardRangeTest.test));
        
        // 0 to 100
        
        final CardRange f1t100s2 = new CardRange(0, 100, 100, 5);
        final List<Integer> l_f1t100s2 = Arrays.asList(0, 20, 40, 60, 80);
        
        Assert.assertEquals(l_f1t100s2, f1t100s2.useForPick(CardRangeTest.test));
        
        // 5 to 105
        
        final CardRange f5t105s2 = new CardRange(5, 100, 100, 5);
        final List<Integer> l_f5t105s2 = Arrays.asList(5, 25, 45, 65, 85);
        
        Assert.assertEquals(l_f5t105s2, f5t105s2.useForPick(CardRangeTest.test));
    }
}
