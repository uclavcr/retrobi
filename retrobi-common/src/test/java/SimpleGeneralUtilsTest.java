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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.Date;
import cz.insophy.retrobi.database.entity.Time;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.utils.Triple;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.SimpleGeneralUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleGeneralUtilsTest {
    /**
     * test
     */
    @Test
    public void testMoveDefault() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("d", "e");
        final String p = "b";
        
        Assert.assertEquals(
                Arrays.asList("a", "b", "d", "e", "c"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList("a", "d", "e", "b", "c"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMovePivotMoving() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("a", "c", "e");
        final String p = "c";
        
        Assert.assertEquals(
                Arrays.asList("b", "a", "c", "e", "d"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList("b", "a", "c", "e", "d"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveLeftBoundary() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("e", "b", "d");
        final String p = "a";
        
        Assert.assertEquals(
                Arrays.asList("a", "e", "b", "d", "c"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList("e", "b", "d", "a", "c"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveRightBoundary() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("c", "a", "b");
        final String p = "e";
        
        Assert.assertEquals(
                Arrays.asList("d", "e", "c", "a", "b"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList("d", "c", "a", "b", "e"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveBoundary2() {
        final List<Integer> s = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        final List<Integer> m1 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        final Integer p1 = 12;
        final List<Integer> m2 = Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        final Integer p2 = 1;
        
        Assert.assertEquals(
                Arrays.asList(12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                SimpleGeneralUtils.moveItems(s, m1, p1, true));
        
        Assert.assertEquals(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                SimpleGeneralUtils.moveItems(s, m1, p1, false));
        
        Assert.assertEquals(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                SimpleGeneralUtils.moveItems(s, m2, p2, true));
        
        Assert.assertEquals(
                Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1),
                SimpleGeneralUtils.moveItems(s, m2, p2, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveEmpty() {
        final List<String> s = Arrays.asList();
        final List<String> m = Arrays.asList();
        final String p = "c";
        
        Assert.assertEquals(
                Arrays.asList(),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList(),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveRepeat() {
        final List<String> s = Arrays.asList("a", "b", "c", "d");
        final List<String> m = Arrays.asList("c", "c", "d", "c", "d", "d");
        final String p = "b";
        
        Assert.assertEquals(
                Arrays.asList("a", "b", "c", "c", "d", "c", "d", "d"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList("a", "c", "c", "d", "c", "d", "d", "b"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveForeighers() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("X", "Y");
        final String p = "b";
        
        Assert.assertEquals(
                Arrays.asList("a", "b", "X", "Y", "c", "d", "e"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
        
        Assert.assertEquals(
                Arrays.asList("a", "X", "Y", "b", "c", "d", "e"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveForeighersFirst() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("X", "Y");
        final String p = "a";
        
        Assert.assertEquals(
                Arrays.asList("X", "Y", "a", "b", "c", "d", "e"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveForeighersLast() {
        final List<String> s = Arrays.asList("a", "b", "c", "d", "e");
        final List<String> m = Arrays.asList("X", "Y");
        final String p = "e";
        
        Assert.assertEquals(
                Arrays.asList("a", "b", "c", "d", "e", "X", "Y"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveOneFirst() {
        final List<String> s = Arrays.asList("b", "c", "d");
        final List<String> m = Arrays.asList("a");
        final String p = "b";
        
        Assert.assertEquals(
                Arrays.asList("a", "b", "c", "d"),
                SimpleGeneralUtils.moveItems(s, m, p, false));
    }
    
    /**
     * test
     */
    @Test
    public void testMoveOneLast() {
        final List<String> s = Arrays.asList("a", "b", "c");
        final List<String> m = Arrays.asList("d");
        final String p = "c";
        
        Assert.assertEquals(
                Arrays.asList("a", "b", "c", "d"),
                SimpleGeneralUtils.moveItems(s, m, p, true));
    }
    
    /**
     * test
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCoalesce() {
        Assert.assertEquals(Integer.valueOf(1), SimpleGeneralUtils.coalesce(1, 2, 3));
        Assert.assertEquals(Integer.valueOf(1), SimpleGeneralUtils.coalesce(null, 1, 2, 3));
        Assert.assertEquals("A", SimpleGeneralUtils.coalesce(null, "A", 2, 3));
        Assert.assertEquals(null, SimpleGeneralUtils.coalesce());
        Assert.assertEquals(null, SimpleGeneralUtils.coalesce());
        Assert.assertEquals(null, SimpleGeneralUtils.coalesce(null, null, null));
    }
    
    /**
     * test
     */
    @Test
    public void testLimit() {
        Assert.assertEquals(100, SimpleGeneralUtils.limit(999, 1, 100));
        Assert.assertEquals(100, SimpleGeneralUtils.limit(101, 1, 100));
        
        Assert.assertEquals(100, SimpleGeneralUtils.limit(100, 1, 100));
        Assert.assertEquals(42, SimpleGeneralUtils.limit(42, 1, 100));
        Assert.assertEquals(1, SimpleGeneralUtils.limit(1, 1, 100));
        
        Assert.assertEquals(1, SimpleGeneralUtils.limit(0, 1, 100));
        Assert.assertEquals(1, SimpleGeneralUtils.limit(-1, 1, 100));
        Assert.assertEquals(1, SimpleGeneralUtils.limit(-85, 1, 100));
    }
    
    /**
     * test
     */
    @Test
    public void testWasChanged() {
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(null, "a"));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged("a", null));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged("a", "b"));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(true, false));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(false, true));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(150, 151));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(150l, -18654l));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(1544.15d, 1544.16d));
        Assert.assertTrue(SimpleGeneralUtils.wasChanged(1544.15f, 1544.16f));
        
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(null, null));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged("a", "a"));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged("150", "150"));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(true, true));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(false, false));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(150, 150));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(7834545l, 7834545l));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(-42.4823d, -42.4823d));
        Assert.assertFalse(SimpleGeneralUtils.wasChanged(-42.4823f, -42.4823f));
    }
    
    /**
     * test
     */
    @Test
    public void testWasChangedString() {
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString("", "a"));
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString("a", ""));
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString(null, "a"));
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString("a", null));
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString("a", "A"));
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString("A", "a"));
        Assert.assertTrue(SimpleGeneralUtils.wasChangedAsString("a", "b"));
        
        Assert.assertFalse(SimpleGeneralUtils.wasChangedAsString(null, null));
        Assert.assertFalse(SimpleGeneralUtils.wasChangedAsString(null, ""));
        Assert.assertFalse(SimpleGeneralUtils.wasChangedAsString("", null));
        Assert.assertFalse(SimpleGeneralUtils.wasChangedAsString("", ""));
        Assert.assertFalse(SimpleGeneralUtils.wasChangedAsString("a", "a"));
        Assert.assertFalse(SimpleGeneralUtils.wasChangedAsString("A B C", "A B C"));
    }
    
    /**
     * test
     */
    @Test
    public void testTuple() {
        final Tuple<String, Integer> t1a = Tuple.of("a", 154);
        final Tuple<String, Integer> t1b = Tuple.of("a", 154);
        final Tuple<String, Integer> t2 = Tuple.of("a", 153);
        final Tuple<String, Integer> t3 = Tuple.of("b", 154);
        
        Assert.assertFalse(t1a.equals(null));
        Assert.assertFalse(t1a.equals(157));
        Assert.assertFalse(t1a.equals("(a, 154)"));
        
        Assert.assertEquals("(a, 154)", t1a.toString());
        Assert.assertEquals(t1a, t1a);
        Assert.assertEquals(t1b, t1b);
        Assert.assertEquals(t1a, t1b);
        Assert.assertEquals(t1b, t1a);
        Assert.assertFalse(t1a.equals(t2));
        Assert.assertFalse(t1a.equals(t3));
        Assert.assertFalse(t2.equals(t1a));
        Assert.assertFalse(t3.equals(t1a));
        
        final Set<Tuple<?, ?>> set = new HashSet<Tuple<?, ?>>();
        
        for (int i = 0; i < 100000; i++) {
            final Tuple<?, ?> t = Tuple.of(i % 10, i % 2);
            set.add(t);
        }
        
        Assert.assertEquals(10, set.size());
        Assert.assertTrue(set.contains(Tuple.of(0, 0)));
        Assert.assertTrue(set.contains(Tuple.of(1, 1)));
        Assert.assertTrue(set.contains(Tuple.of(2, 0)));
        Assert.assertTrue(set.contains(Tuple.of(3, 1)));
        Assert.assertTrue(set.contains(Tuple.of(4, 0)));
        Assert.assertTrue(set.contains(Tuple.of(5, 1)));
        Assert.assertTrue(set.contains(Tuple.of(6, 0)));
        Assert.assertTrue(set.contains(Tuple.of(7, 1)));
        Assert.assertTrue(set.contains(Tuple.of(8, 0)));
        Assert.assertTrue(set.contains(Tuple.of(9, 1)));
    }
    
    /**
     * test
     */
    @Test
    public void testTriple() {
        final Triple<String, Integer, Integer> t1a = Triple.of("a", 154, -21);
        final Triple<String, Integer, Integer> t1b = Triple.of("a", 154, -21);
        final Triple<String, Integer, Integer> t2 = Triple.of("a", 153, -21);
        final Triple<String, Integer, Integer> t3 = Triple.of("b", 154, -21);
        
        Assert.assertFalse(t1a.equals(null));
        Assert.assertFalse(t1a.equals(157));
        Assert.assertFalse(t1a.equals("(a, 154)"));
        
        Assert.assertEquals("(a, 154, -21)", t1a.toString());
        Assert.assertEquals(t1a, t1a);
        Assert.assertEquals(t1b, t1b);
        Assert.assertEquals(t1a, t1b);
        Assert.assertEquals(t1b, t1a);
        Assert.assertFalse(t1a.equals(null));
        Assert.assertFalse(t1a.equals(157));
        Assert.assertFalse(t1a.equals(t2));
        Assert.assertFalse(t1a.equals(t3));
        Assert.assertFalse(t2.equals(t1a));
        Assert.assertFalse(t3.equals(t1a));
        
        final Set<Triple<?, ?, ?>> set = new HashSet<Triple<?, ?, ?>>();
        
        for (int i = 0; i < 100000; i++) {
            final Triple<?, ?, ?> t = Triple.of(i % 3, i % 3, i % 2);
            set.add(t);
        }
        
        Assert.assertEquals(6, set.size());
        Assert.assertTrue(set.contains(Triple.of(0, 0, 0)));
        Assert.assertTrue(set.contains(Triple.of(0, 0, 1)));
        Assert.assertTrue(set.contains(Triple.of(1, 1, 0)));
        Assert.assertTrue(set.contains(Triple.of(1, 1, 1)));
        Assert.assertTrue(set.contains(Triple.of(2, 2, 0)));
        Assert.assertTrue(set.contains(Triple.of(2, 2, 1)));
    }
    
    /**
     * test
     */
    @Test
    public void testSortDate() {
        final Date a1 = new Date(1, 2, 3);
        final Date b1 = new Date(1, 2, 3);
        Assert.assertEquals(0, Date.compare(null, null));
        Assert.assertTrue(Date.compare(null, a1) < 0);
        Assert.assertTrue(Date.compare(a1, null) > 0);
        Assert.assertTrue(Date.compare(null, b1) < 0);
        Assert.assertTrue(Date.compare(b1, null) > 0);
        Assert.assertEquals(0, Date.compare(a1, b1));
        Assert.assertEquals(0, Date.compare(a1, a1));
        Assert.assertEquals(0, Date.compare(b1, b1));
        
        final Date a2 = new Date(1, 2, 3);
        final Date b2 = new Date(1, 2, 4);
        Assert.assertTrue(Date.compare(a2, b2) < 0);
        Assert.assertTrue(Date.compare(b2, a2) > 0);
        
        final Date c2 = new Date(1, 3, 3);
        Assert.assertTrue(Date.compare(a2, c2) < 0);
        Assert.assertTrue(Date.compare(c2, a2) > 0);
        
        final Date d2 = new Date(2, 2, 3);
        Assert.assertTrue(Date.compare(a2, d2) < 0);
        Assert.assertTrue(Date.compare(d2, a2) > 0);
        
        final Date e2 = new Date(0, 2, 3);
        Assert.assertTrue(Date.compare(a2, e2) > 0);
        Assert.assertTrue(Date.compare(e2, a2) < 0);
        
        final Time a3 = new Time(a2, 0, 0, 0);
        final Time b3 = new Time(a2, 1, 0, 0);
        final Time c3 = new Time(a2, 0, 1, 0);
        final Time d3 = new Time(a2, 0, 1, 1);
        Assert.assertEquals(0, Time.compare(a3, a3));
        Assert.assertTrue(Time.compare(a3, b3) < 0);
        Assert.assertTrue(Time.compare(b3, a3) > 0);
        Assert.assertTrue(Time.compare(a3, c3) < 0);
        Assert.assertTrue(Time.compare(c3, a3) > 0);
        Assert.assertEquals(0, Date.compare(a3, a3));
        Assert.assertEquals(0, Date.compare(a3, b3));
        Assert.assertEquals(0, Date.compare(b3, a3));
        Assert.assertEquals(0, Date.compare(a3, c3));
        Assert.assertEquals(0, Date.compare(c3, a3));
        Assert.assertTrue(Time.compare(c3, d3) < 0);
        Assert.assertTrue(Time.compare(d3, c3) > 0);
    }
    
    /**
     * test
     */
    @Test
    public void testSortDates() {
        final List<Date> datesOK = Arrays.asList(
                new Date(1, 1, 2001),
                new Date(1, 2, 2001),
                new Date(1, 1, 2002),
                new Date(1, 2, 2002),
                new Time(new Date(3, 2, 2002), 0, 0, 0),
                new Time(new Date(4, 2, 2002), 0, 1, 0),
                new Date(1, 1, 2003),
                new Date(1, 2, 2003),
                new Date(1, 3, 2003),
                new Date(2, 3, 2003),
                new Time(new Date(3, 3, 2003), 1, 5, 0),
                new Date(1, 4, 2003),
                new Date(31, 12, 2012),
                new Date(5, 4, 2013),
                new Date(6, 4, 2013));
        
        final List<Date> dates = new ArrayList<Date>(datesOK);
        
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(dates);
            Collections.sort(dates, new Comparator<Date>() {
                @Override
                public int compare(final Date o1, final Date o2) {
                    return Date.compare(o1, o2);
                }
            });
            Assert.assertEquals(datesOK, dates);
        }
    }
    
    /**
     * test
     */
    @Test
    public void testSortTimes() {
        final List<Time> datesOK = Arrays.asList(
                new Time(new Date(1, 1, 2001), 1, 5, 0),
                new Time(new Date(1, 1, 2001), 1, 5, 32),
                new Time(new Date(1, 1, 2001), 1, 5, 58),
                new Time(new Date(1, 2, 2001), 0, 0, 0),
                new Time(new Date(1, 1, 2002), 9, 3, 0),
                new Time(new Date(1, 1, 2002), 9, 3, 13),
                new Time(new Date(1, 1, 2002), 9, 3, 37),
                new Time(new Date(1, 2, 2002), 3, 7, 0),
                new Time(new Date(3, 2, 2002), 2, 7, 0),
                new Time(new Date(4, 2, 2002), 0, 1, 0),
                new Time(new Date(1, 1, 2003), 1, 5, 0),
                new Time(new Date(1, 2, 2003), 6, 5, 0),
                new Time(new Date(1, 3, 2003), 8, 35, 0),
                new Time(new Date(2, 3, 2003), 7, 30, 5),
                new Time(new Date(2, 3, 2003), 7, 30, 14),
                new Time(new Date(2, 3, 2003), 7, 30, 15),
                new Time(new Date(3, 3, 2003), 8, 8, 0),
                new Time(new Date(1, 4, 2003), 22, 51, 0),
                new Time(new Date(31, 12, 2012), 23, 55, 0),
                new Time(new Date(5, 4, 2013), 1, 5, 0),
                new Time(new Date(5, 4, 2013), 1, 5, 55),
                new Time(new Date(6, 4, 2013), 10, 5, 0));
        
        final List<Time> dates = new ArrayList<Time>(datesOK);
        
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(dates);
            Collections.sort(dates, new Comparator<Time>() {
                @Override
                public int compare(final Time o1, final Time o2) {
                    return Time.compare(o1, o2);
                }
            });
            Assert.assertEquals(datesOK, dates);
        }
    }
    
    /**
     * test
     */
    @Test
    public void testSortCards() {
        final List<Card> cardsOK = new LinkedList<Card>();
        
        for (int i = 0; i < 50; i++) {
            cardsOK.add(SimpleGeneralUtilsTest.createTestCard(i, new Time(new Date(i + 1, 3, 2001), i + 1, i + 3, i + 3)));
            cardsOK.add(SimpleGeneralUtilsTest.createTestCard(i, new Time(new Date(i + 2, 2, 2001), i + 2, i + 2, i + 2)));
            cardsOK.add(SimpleGeneralUtilsTest.createTestCard(i, new Time(new Date(i + 3, 1, 2001), i + 3, i + 1, i + 1)));
        }
        
        final List<Card> cards = new ArrayList<Card>(cardsOK);
        
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(cards);
            SimpleGeneralUtils.sortByNumberAndTime(cards);
            Assert.assertEquals(cardsOK, cards);
        }
    }
    
    /**
     * test
     * 
     * @param number
     * -
     * @param time
     * -
     * @return -
     */
    private static Card createTestCard(final int number, final Time time) {
        final Card card = new Card();
        card.setAdded(time);
        card.setUpdated(time);
        card.setCatalog(Catalog.A);
        card.setBatch("test");
        card.setBatchForSort("test");
        card.setNumberInBatch(number);
        return card;
    }
}
