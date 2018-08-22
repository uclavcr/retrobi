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

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.model.CardRange;

/**
 * @author Vojtěch Hordějčuk
 */
public class CardRangeTest {
    /**
     * test
     */
    @Test
    public final void testGetHighestRange() {
        Assert.assertEquals(1, CardRange.getHighestRange(0));
        Assert.assertEquals(10, CardRange.getHighestRange(5));
        Assert.assertEquals(10, CardRange.getHighestRange(10));
        Assert.assertEquals(100, CardRange.getHighestRange(15));
        Assert.assertEquals(100, CardRange.getHighestRange(100));
        Assert.assertEquals(1000, CardRange.getHighestRange(124));
        Assert.assertEquals(1000, CardRange.getHighestRange(1000));
        Assert.assertEquals(10000, CardRange.getHighestRange(3870));
        Assert.assertEquals(10000, CardRange.getHighestRange(10000));
        Assert.assertEquals(100000, CardRange.getHighestRange(99999));
        Assert.assertEquals(100000, CardRange.getHighestRange(100000));
        Assert.assertEquals(1000000, CardRange.getHighestRange(867538));
        Assert.assertEquals(1000000, CardRange.getHighestRange(1000000));
        Assert.assertEquals(10000000, CardRange.getHighestRange(9999999));
    }
    
    /**
     * test
     */
    @Test
    public final void testRoundOffset() {
        Assert.assertEquals(10, CardRange.roundOffset(15, 10));
        Assert.assertEquals(0, CardRange.roundOffset(15, 100));
        Assert.assertEquals(0, CardRange.roundOffset(15, 1000));
        Assert.assertEquals(15, CardRange.roundOffset(15, 1));
        Assert.assertEquals(100, CardRange.roundOffset(153, 100));
        Assert.assertEquals(0, CardRange.roundOffset(153, 1000));
        Assert.assertEquals(20000, CardRange.roundOffset(24871, 10000));
        Assert.assertEquals(30, CardRange.roundOffset(38, 10));
        Assert.assertEquals(0, CardRange.roundOffset(123, 1000));
    }
}
