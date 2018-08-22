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

import junit.framework.Assert;

import org.junit.Test;

import cz.insophy.retrobi.Settings;
import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.utils.library.SimpleSegmentUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleSegmentUtilsTest {
    /**
     * test
     */
    @Test
    public final void test0() {
        final Card card1 = SimpleSegmentUtilsTest.createCard("QQaerg|    ||aerg|aer| aegr");
        final Card card2 = SimpleSegmentUtilsTest.createCard("a|a|/=a/|a|a|a");
        final Card card3 = SimpleSegmentUtilsTest.createCard("a|a|/=a/|a|a|a|a");
        final Card card4 = SimpleSegmentUtilsTest.createCard("a|a|/=a/|a|a|a|a|a");
        
        SimpleSegmentUtilsTest.assertInvalid(card1);
        SimpleSegmentUtilsTest.assertInvalid(card2);
        SimpleSegmentUtilsTest.assertInvalid(card3);
        SimpleSegmentUtilsTest.assertInvalid(card4);
    }
    
    /**
     * test
     */
    @Test
    public final void test1() {
        final Card card = SimpleSegmentUtilsTest.createCard("" +
                "  Aakjaer,Jeppe   |  " +
                " Jiří Brandes: Dánská literatura po r.1870 [Essay]  |  " +
                "   /=Lumír 3l,l902-03,č.31,373-376/   |  " +
                " [Struč.nástin polit.,kult a liter.dějin Dánska; uvedeni význačněj. autoři s charakter. jejich tvorby mimo jiné zmíněni též Jeppe Aakjaer,Jakob Knudsen, Frederik Poulsen,Jakob Hansen]   |  " +
                "  Sv  ");
        
        SimpleSegmentUtilsTest.assertValid(
                card,
                "Aakjaer, Jeppe",
                "Jiří Brandes: Dánská literatura po r. 1870 [Essay]",
                "In: Lumír 3l, l902-03, č. 31, 373-376.",
                "[Struč. nástin polit., kult a liter. dějin Dánska; uvedeni význačněj. autoři s charakter. jejich tvorby mimo jiné zmíněni též Jeppe Aakjaer, Jakob Knudsen, Frederik Poulsen, Jakob Hansen]",
                "Sv");
    }
    
    /**
     * test
     */
    @Test
    public final void test2() {
        final Card card = SimpleSegmentUtilsTest.createCard("" +
                "   Aakjaer,Jeppe  | " +
                "  Gustav Pallas:Dánská zpráva   |  " +
                " /=Zvon l4,l913-14, č. 23-24, 334-336, 20/3 1914; č. 25-26, 360-361, 27/3 1914   /  |   " +
                "  [Přehled současné dánské literární produkce; z autorú uvedeni mj. též   Otto Gelsted, Sören Kirkegaard, Jeppe Aakjaer, Karl K. Nicolaisen]   | " +
                "  Sv  ");
        
        SimpleSegmentUtilsTest.assertValid(
                card,
                "Aakjaer, Jeppe",
                "Gustav Pallas: Dánská zpráva",
                "In: Zvon l4, l913-14, č. 23-24, 334-336, 20/3 1914; č. 25-26, 360-361, 27/3 1914.",
                "[Přehled současné dánské literární produkce; z autorú uvedeni mj. též Otto Gelsted, Sören Kirkegaard, Jeppe Aakjaer, Karl K. Nicolaisen]",
                "Sv");
    }
    
    /**
     * test
     */
    @Test
    public final void test3() {
        final Card card = SimpleSegmentUtilsTest.createCard("" +
                " ﻿Aakjär, Jeppe  |" +
                "  ba. [= ]: Básník dánského venkova [Zpráva]   |    " +
                "  /=Venkov 25, 1930, č. 107, str. 4, 7/5/  |" +
                "   [0 úmrtí dánského spisovatele Jeppa Aakjära]    |   ");
        
        SimpleSegmentUtilsTest.assertValid(
                card,
                "﻿Aakjär, Jeppe",
                "ba. [= ]: Básník dánského venkova [Zpráva]",
                "In: Venkov 25, 1930, č. 107, str. 4, 7/5.",
                "[0 úmrtí dánského spisovatele Jeppa Aakjära]",
                "");
    }
    
    /**
     * test
     */
    @Test
    public final void test4() {
        final Card card = SimpleSegmentUtilsTest.createCard("" +
                "  Kalista, Zdeněk  |  " +
                "  Zdeněk Kalista : Zápasníci./Básně/ Praha, Petr a Tvrdý 1922 |  " +
                "  Rf: Karel Ron , Zdeněk Kalista .Zápasníci  |" +
                "   /=Studentský časopis 2,1922/23,č.6,str.169-170 /  |   ");
        
        SimpleSegmentUtilsTest.assertValid(
                card,
                "Kalista, Zdeněk",
                "Karel Ron, Zdeněk Kalista. Zápasníci [Referát]",
                "In: Studentský časopis 2, 1922/23, č. 6, str. 169-170.",
                "[Zdeněk Kalista: Zápasníci./Básně/ Praha, Petr a Tvrdý 1922]",
                "");
    }
    
    /**
     * test
     */
    @Test
    public final void test4space() {
        final Card card = SimpleSegmentUtilsTest.createCard("" +
                "  Kal\nista,     Zdeněk \r\n\n\n  |  " +
                "  Zdeněk \r\r  Kalista           : Zápasníci./Básně/    Praha,       Petr a Tvrdý 1922       |  " +
                "  Rf: Karel   \n\n\n     Ron , Zdeněk Kalista      \r\n\r\n     .Zápasníci  |" +
                "   /=Studentský       časopis    \n\n\n    \r\r 2,1922/23,č.6,str.169-170 /  |   ");
        
        SimpleSegmentUtilsTest.assertValid(
                card,
                "Kal ista, Zdeněk",
                "Karel Ron, Zdeněk Kalista. Zápasníci [Referát]",
                "In: Studentský časopis 2, 1922/23, č. 6, str. 169-170.",
                "[Zdeněk Kalista: Zápasníci./Básně/ Praha, Petr a Tvrdý 1922]",
                "");
    }
    
    /**
     * test
     */
    @Test
    public final void test5() {
        final Card card = SimpleSegmentUtilsTest.createCard("" +
                "﻿DOBROWOLSKI, František | " +
                " František Dobrowolski +. | " +
                " /=  Hlas národa, 1896, č.201, s.5, 23.7., nepodepsáno / | " +
                "  /Žurnalista, redaktor Dzienniku Poznaňskiego Dobrowolski František, nar. 1830 v Polsku, zemřel koncem července 1896. O politické činnosti a perzekuci. D. byl přítelem českého národa, v citovaném listě uveřejňoval českou korespondenci z Čech, kterou zasílal Jelínek Edvard./ |" +
                "");
        
        SimpleSegmentUtilsTest.assertValid(
                card,
                "﻿DOBROWOLSKI, František",
                "František Dobrowolski +.",
                "In: Hlas národa, 1896, č. 201, s. 5, 23. 7., nepodepsáno.",
                "[Žurnalista, redaktor Dzienniku Poznaňskiego Dobrowolski František, nar. 1830 v Polsku, zemřel koncem července 1896. O politické činnosti a perzekuci. D. byl přítelem českého národa, v citovaném listě uveřejňoval českou korespondenci z Čech, kterou zasílal Jelínek Edvard.]",
                "");
    }
    
    /**
     * test
     */
    @Test
    public final void testMatchAnnotation() {
        Assert.assertEquals("", SimpleSegmentUtils.segmentReadAnnotation(""));
        Assert.assertEquals("[TEXT]", SimpleSegmentUtils.segmentReadAnnotation("[TEXT]"));
        Assert.assertEquals("(TEXT)", SimpleSegmentUtils.segmentReadAnnotation("(TEXT)"));
        Assert.assertEquals("/TEXT/", SimpleSegmentUtils.segmentReadAnnotation("/TEXT/"));
        Assert.assertEquals("[A]", SimpleSegmentUtils.segmentReadAnnotation("[A]"));
        Assert.assertEquals("(A)", SimpleSegmentUtils.segmentReadAnnotation("(A)"));
        Assert.assertEquals("/A/", SimpleSegmentUtils.segmentReadAnnotation("/A/"));
        Assert.assertEquals("[   TEXT  ]", SimpleSegmentUtils.segmentReadAnnotation("[   TEXT  ]"));
        Assert.assertEquals("(   TEXT  )", SimpleSegmentUtils.segmentReadAnnotation("(   TEXT  )"));
        Assert.assertEquals("/   TEXT  /", SimpleSegmentUtils.segmentReadAnnotation("/   TEXT  /"));
        Assert.assertEquals("AAA: BBB. /CCC/ DDD", SimpleSegmentUtils.segmentReadAnnotation("AAA: BBB. /CCC/ DDD"));
        Assert.assertEquals("RfAAAAAA", SimpleSegmentUtils.segmentReadAnnotation("RfAAAAAA"));
        Assert.assertEquals("RefAAAAAA", SimpleSegmentUtils.segmentReadAnnotation("RefAAAAAA"));
        Assert.assertEquals("TEXT", SimpleSegmentUtils.segmentReadAnnotation("TEXT"));
        
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("=A"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("=AA"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("=AAA"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("(="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("(A="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("(AA="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:A="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:AA="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:AAA="));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Ref. TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf. TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Ref.TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf.TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("[=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("(=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("/=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:AB=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("[AB=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("(AB=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("/AB=TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadAnnotation("Rf:   =TEXT"));
    }
    
    /**
     * test
     */
    @Test
    public final void testMatchBibliography() {
        Assert.assertEquals("[=TEXT]", SimpleSegmentUtils.segmentReadBibliography("[=TEXT]"));
        Assert.assertEquals("(=TEXT)", SimpleSegmentUtils.segmentReadBibliography("(=TEXT)"));
        Assert.assertEquals("/=TEXT/", SimpleSegmentUtils.segmentReadBibliography("/=TEXT/"));
        Assert.assertEquals("[ =TEXT]", SimpleSegmentUtils.segmentReadBibliography("[ =TEXT]"));
        Assert.assertEquals("[  =TEXT]", SimpleSegmentUtils.segmentReadBibliography("[  =TEXT]"));
        Assert.assertEquals("[   =TEXT]", SimpleSegmentUtils.segmentReadBibliography("[   =TEXT]"));
        
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography(""));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("[    =TEXT  ]"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("(    =TEXT  )"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("/    =TEXT  /"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("[    =TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("(    =TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("/    =TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("[TEXT]"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("(TEXT)"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("/TEXT/"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("[TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("(TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadBibliography("/TEXT"));
    }
    
    /**
     * test
     */
    @Test
    public final void testMatchTitle() {
        Assert.assertEquals("TEXT", SimpleSegmentUtils.segmentReadTitle("TEXT"));
        Assert.assertEquals("=TEXT", SimpleSegmentUtils.segmentReadTitle("=TEXT"));
        Assert.assertEquals("Rf:   TEXT", SimpleSegmentUtils.segmentReadTitle("Rf:   TEXT"));
        Assert.assertEquals("Rf:A", SimpleSegmentUtils.segmentReadTitle("Rf:A"));
        Assert.assertEquals("Rf:TEXT", SimpleSegmentUtils.segmentReadTitle("Rf:TEXT"));
        Assert.assertEquals(".. = TEXT = ..", SimpleSegmentUtils.segmentReadTitle(".. = TEXT = .."));
        Assert.assertEquals("TEXT [ TEXT ]", SimpleSegmentUtils.segmentReadTitle("TEXT [ TEXT ]"));
        Assert.assertEquals("Rf:A", SimpleSegmentUtils.segmentReadTitle("Rf:A"));
        Assert.assertEquals("Ref:A", SimpleSegmentUtils.segmentReadTitle("Ref:A"));
        Assert.assertEquals("RF:A", SimpleSegmentUtils.segmentReadTitle("RF:A"));
        Assert.assertEquals("REF:A", SimpleSegmentUtils.segmentReadTitle("REF:A"));
        Assert.assertEquals("Rf.A", SimpleSegmentUtils.segmentReadTitle("Rf.A"));
        Assert.assertEquals("Ref.A", SimpleSegmentUtils.segmentReadTitle("Ref.A"));
        Assert.assertEquals("RF.A", SimpleSegmentUtils.segmentReadTitle("RF.A"));
        Assert.assertEquals("REF.A", SimpleSegmentUtils.segmentReadTitle("REF.A"));
        Assert.assertEquals("Rf A", SimpleSegmentUtils.segmentReadTitle("Rf A"));
        Assert.assertEquals("Ref A", SimpleSegmentUtils.segmentReadTitle("Ref A"));
        Assert.assertEquals("RF A", SimpleSegmentUtils.segmentReadTitle("RF A"));
        Assert.assertEquals("REF A", SimpleSegmentUtils.segmentReadTitle("REF A"));
        Assert.assertEquals("RFA", SimpleSegmentUtils.segmentReadTitle("RFA"));
        Assert.assertEquals("REFA", SimpleSegmentUtils.segmentReadTitle("REFA"));
        Assert.assertEquals("Rf,a", SimpleSegmentUtils.segmentReadTitle("Rf,a"));
        Assert.assertEquals("Ref,a", SimpleSegmentUtils.segmentReadTitle("Ref,a"));
        
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle(""));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("[TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("(TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("/TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("[    TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("(    TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("/    TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("[ =   TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("( =   TEXT"));
        Assert.assertNull(SimpleSegmentUtils.segmentReadTitle("/ =   TEXT"));
    }
    
    /**
     * test
     */
    @Test
    public final void testFinalize() {
        Assert.assertEquals("In: .", SimpleSegmentUtils.finalizeBibliography("/=/"));
        Assert.assertEquals("In: .", SimpleSegmentUtils.finalizeBibliography("(=)"));
        Assert.assertEquals("In: .", SimpleSegmentUtils.finalizeBibliography("[=]"));
        Assert.assertEquals("In: TEXT.", SimpleSegmentUtils.finalizeBibliography("/= TEXT/"));
        Assert.assertEquals("In: TEXT.", SimpleSegmentUtils.finalizeBibliography("/   = TEXT   /"));
        Assert.assertEquals("In: TEXT.", SimpleSegmentUtils.finalizeBibliography("(   = TEXT   )"));
        Assert.assertEquals("In: TEXT.", SimpleSegmentUtils.finalizeBibliography("[   = TEXT   ]"));
        
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Rf: test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Ref: test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Rf. test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Ref. test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Rf test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Ref test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Rf: test"));
        Assert.assertEquals("test [Referát]", SimpleSegmentUtils.finalizeTitle("Ref: test"));
        
        Assert.assertEquals("", SimpleSegmentUtils.finalizeAnnotation(""));
        Assert.assertEquals("[]", SimpleSegmentUtils.finalizeAnnotation("[]"));
        Assert.assertEquals("[]", SimpleSegmentUtils.finalizeAnnotation("["));
        Assert.assertEquals("[]", SimpleSegmentUtils.finalizeAnnotation("]"));
        Assert.assertEquals("[test]", SimpleSegmentUtils.finalizeAnnotation("test"));
        Assert.assertEquals("[= test]", SimpleSegmentUtils.finalizeAnnotation("[= test]"));
        Assert.assertEquals("[= test]", SimpleSegmentUtils.finalizeAnnotation("[= test"));
        Assert.assertEquals("[= test]", SimpleSegmentUtils.finalizeAnnotation("= test]"));
        Assert.assertEquals("[  test  ]", SimpleSegmentUtils.finalizeAnnotation("(  test  "));
        Assert.assertEquals("[  test  ]", SimpleSegmentUtils.finalizeAnnotation("  test  )"));
        Assert.assertEquals("[  test  ]", SimpleSegmentUtils.finalizeAnnotation("(  test  )"));
        Assert.assertEquals("[  test  ]", SimpleSegmentUtils.finalizeAnnotation("/  test  "));
        Assert.assertEquals("[  test  ]", SimpleSegmentUtils.finalizeAnnotation("  test  /"));
        Assert.assertEquals("[  test  ]", SimpleSegmentUtils.finalizeAnnotation("/  test  /"));
    }
    
    /**
     * test
     */
    @Test
    public final void testToString() {
        final Card card = new Card();
        
        card.setSegmentAnnotation("a");
        card.setSegmentBibliography("b");
        card.setSegmentExcerpter("e");
        card.setSegmentHead("h");
        card.setSegmentTitle("t");
        
        Assert.assertEquals("H|t|b|a||e", SimpleSegmentUtils.segmentsToString(card, "|"));
        
        card.setSegmentAnnotation(null);
        card.setSegmentBibliography(null);
        card.setSegmentExcerpter(null);
        card.setSegmentHead(null);
        card.setSegmentTitle(null);
        
        Assert.assertEquals("-|-|-|-||-", SimpleSegmentUtils.segmentsToString(card, "|"));
        
        card.setSegmentAnnotation(null);
        card.setSegmentBibliography("b");
        card.setSegmentExcerpter("e");
        card.setSegmentHead("h");
        card.setSegmentTitle(null);
        
        Assert.assertEquals("H|-|b|-||e", SimpleSegmentUtils.segmentsToString(card, "|"));
        
        card.setSegmentAnnotation("a");
        card.setSegmentBibliography(null);
        card.setSegmentExcerpter(null);
        card.setSegmentHead(null);
        card.setSegmentTitle("t");
        
        Assert.assertEquals("-|t|-|a||-", SimpleSegmentUtils.segmentsToString(card, "|"));
    }
    
    /**
     * @param card
     * card to be asserted
     */
    private static void assertInvalid(final Card card) {
        final boolean result = SimpleSegmentUtils.segment(Settings.SYMBOL_SEGMENT, card);
        
        Assert.assertFalse(result);
        Assert.assertNull(card.getSegmentHead());
        Assert.assertNull(card.getSegmentTitle());
        Assert.assertNull(card.getSegmentBibliography());
        Assert.assertNull(card.getSegmentAnnotation());
        Assert.assertNull(card.getSegmentExcerpter());
    }
    
    /**
     * @param card
     * card
     * @param expectedH
     * expected head segment
     * @param expectedT
     * expected title segment
     * @param expectedB
     * expected bibliography segment
     * @param expectedA
     * expected annotation segment
     * @param expectedE
     * expected excerpter segment
     */
    private static void assertValid(final Card card, final String expectedH, final String expectedT, final String expectedB, final String expectedA, final String expectedE) {
        final boolean result = SimpleSegmentUtils.segment(Settings.SYMBOL_SEGMENT, card);
        
        Assert.assertTrue(result);
        Assert.assertEquals("head", expectedH, card.getSegmentHead());
        Assert.assertEquals("title", expectedT, card.getSegmentTitle());
        Assert.assertEquals("bibliography", expectedB, card.getSegmentBibliography());
        Assert.assertEquals("annotation", expectedA, card.getSegmentAnnotation());
        Assert.assertEquals("excerpter", expectedE, card.getSegmentExcerpter());
    }
    
    /**
     * @param ocr
     * OCR to be set
     * @return a new card
     */
    private static Card createCard(final String ocr) {
        final Card card = new Card();
        card.setCatalog(Catalog.O);
        card.setOcrFix(ocr);
        return card;
    }
}
