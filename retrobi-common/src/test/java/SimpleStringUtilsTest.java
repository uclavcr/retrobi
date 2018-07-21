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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.database.document.BasicDocument;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleStringUtilsTest {
    /**
     * test
     */
    @Test
    public final void testFixDoubleSpace() {
        Assert.assertEquals("1 2 3", SimpleStringUtils.fixWhitespace("1    2         3"));
        Assert.assertEquals("1 2 3", SimpleStringUtils.fixWhitespace("1   \n2\n 3"));
        Assert.assertEquals("1 2 3", SimpleStringUtils.fixWhitespace("1   2\r\n\r\n3"));
    }
    
    /**
     * test
     */
    @Test
    public final void testFixTypograhy() {
        Assert.assertEquals("", SimpleStringUtils.fixTypographicSpaces(""));
        Assert.assertEquals("", SimpleStringUtils.fixTypographicSpaces("    "));
        Assert.assertEquals("A: B", SimpleStringUtils.fixTypographicSpaces("A:B"));
        Assert.assertEquals("A: B", SimpleStringUtils.fixTypographicSpaces("A: B"));
        Assert.assertEquals("A: B", SimpleStringUtils.fixTypographicSpaces("A:    B"));
        Assert.assertEquals("A: B", SimpleStringUtils.fixTypographicSpaces("    A:     B   "));
        Assert.assertEquals("A: B", SimpleStringUtils.fixTypographicSpaces("    A    :     B   "));
        Assert.assertEquals("A; B", SimpleStringUtils.fixTypographicSpaces("A;B"));
        Assert.assertEquals("A, B", SimpleStringUtils.fixTypographicSpaces("A,B"));
        Assert.assertEquals("A. B", SimpleStringUtils.fixTypographicSpaces("A.B"));
        Assert.assertEquals("A! B", SimpleStringUtils.fixTypographicSpaces("A!B"));
        Assert.assertEquals("A? B", SimpleStringUtils.fixTypographicSpaces("A?B"));
        Assert.assertEquals(".,", SimpleStringUtils.fixTypographicSpaces(".,"));
        Assert.assertEquals(".;", SimpleStringUtils.fixTypographicSpaces(".;"));
        Assert.assertEquals("5. 3., 6. 3.", SimpleStringUtils.fixTypographicSpaces("5.3.,6.3."));
        Assert.assertEquals("5. 3., 6. 3.", SimpleStringUtils.fixTypographicSpaces("5. 3. , 6. 3."));
        Assert.assertEquals("aaa.; aaa.", SimpleStringUtils.fixTypographicSpaces("aaa.;aaa."));
        Assert.assertEquals("bbb.; bbb.", SimpleStringUtils.fixTypographicSpaces("bbb. ; bbb."));
        Assert.assertEquals("(1. 5.)", SimpleStringUtils.fixTypographicSpaces("  (1 .    5    .   )  "));
        Assert.assertEquals("[1. 5.]", SimpleStringUtils.fixTypographicSpaces("  [1   .  5  .    ]   "));
        Assert.assertEquals("/1. 5./", SimpleStringUtils.fixTypographicSpaces(" /1 .5.    /   "));
        Assert.assertEquals("a ?)", SimpleStringUtils.fixTypographicSpaces(" a ?  )  "));
        Assert.assertEquals("a ?]", SimpleStringUtils.fixTypographicSpaces("   a      ?    ] "));
        Assert.assertEquals("a ?/", SimpleStringUtils.fixTypographicSpaces(" a    ?  /   "));
        Assert.assertEquals("a.-", SimpleStringUtils.fixTypographicSpaces(" a   .   - "));
        Assert.assertEquals("hello?]", SimpleStringUtils.fixTypographicSpaces(" hello?   ] "));
        Assert.assertEquals("[=hello]", SimpleStringUtils.fixTypographicSpaces(" [    =hello]   "));
        Assert.assertEquals("[=? hello]", SimpleStringUtils.fixTypographicSpaces(" [    =    ?hello] "));
        Assert.assertEquals("A.-B", SimpleStringUtils.fixTypographicSpaces(" A.   -B  "));
        Assert.assertEquals("„Hello“", SimpleStringUtils.fixTypographicSpaces("„Hello“"));
        Assert.assertEquals("\"Hello\"", SimpleStringUtils.fixTypographicSpaces("\"Hello\""));
    }
    
    /**
     * test
     */
    @Test
    public final void testFixSpecialChars() {
        // good characters only
        
        Assert.assertEquals("The first line.\nThe second line.", SimpleStringUtils.fixSpecialChars("The first line.\nThe second line."));
        Assert.assertEquals("The first line.\nThe second line.", SimpleStringUtils.fixSpecialChars("The first line.\r\nThe second line."));
        Assert.assertEquals("The first line.The second line.", SimpleStringUtils.fixSpecialChars("The first line.\rThe second line."));
        Assert.assertEquals("1 + {2 * (34-5678)} / [90]", SimpleStringUtils.fixSpecialChars("1 + {2 * (34-5678)} / [90]"));
        Assert.assertEquals("He said: 'HEY!' Well; Think-tank;", SimpleStringUtils.fixSpecialChars("He said: 'HEY!' Well; Think-tank;"));
        Assert.assertEquals("/= a /(= b)[= c]", SimpleStringUtils.fixSpecialChars("/= a /(= b)[= c]"));
        Assert.assertEquals("Firstʘ|Secondʘ|Thirdʘ", SimpleStringUtils.fixSpecialChars("Firstʘ|Secondʘ|Thirdʘ"));
        Assert.assertEquals("ěščřžýáíéďťňůú1234567890", SimpleStringUtils.fixSpecialChars("ěščřžýáíéďťňůú1234567890"));
        Assert.assertEquals("<special> «special»", SimpleStringUtils.fixSpecialChars("<special> «special»"));
        
        // including bad characters
        
        Assert.assertEquals("aaa", SimpleStringUtils.fixSpecialChars("__a___a__a__"));
        Assert.assertEquals("aaa", SimpleStringUtils.fixSpecialChars("_\ta__\r\t_a_\ra\r\r\r"));
        
        // NOTE: contains 'ZERO WIDTH NO-BREAK SPACE' (codepoint = 65279)
        
        final String adamekBad = "A﻿dámek";
        final String adamekGood = "Adámek";
        Assert.assertEquals(adamekGood, SimpleStringUtils.fixSpecialChars(adamekBad));
        
        final StringBuilder sb = new StringBuilder();
        
        for (char i = 0; i <= 'ž'; i++) {
            sb.appendCodePoint(i);
        }
        
        final String sb2s = sb.toString();
        Assert.assertEquals(383, sb2s.length());
        Assert.assertEquals(288, SimpleStringUtils.fixSpecialChars(sb2s).length());
    }
    
    /**
     * test
     */
    @Test
    public final void testNl2Br() {
        Assert.assertEquals("1<br />2<br />3<br />4", SimpleStringUtils.nl2br("1\n2\r\n3\r4"));
    }
    
    /**
     * test
     */
    @Test
    public final void testCSV() {
        Assert.assertEquals("\"\"", SimpleStringUtils.escapeForCSV(null));
        Assert.assertEquals("\"\"", SimpleStringUtils.escapeForCSV(""));
        Assert.assertEquals("\"test\"", SimpleStringUtils.escapeForCSV("test"));
        Assert.assertEquals("\"test\"", SimpleStringUtils.escapeForCSV("   test   "));
        Assert.assertEquals("\"a řekl: 'test'\"", SimpleStringUtils.escapeForCSV("a řekl: 'test'"));
        Assert.assertEquals("\"a řekl: \\\"test\\\"\"", SimpleStringUtils.escapeForCSV("a řekl: \"test\""));
        Assert.assertEquals("\"first\nsecond\nthird\"", SimpleStringUtils.escapeForCSV("first\nsecond\nthird\n"));
        
        Assert.assertEquals("\"\"", SimpleStringUtils.escapeColsForCSV(false, (String) null));
        Assert.assertEquals("\"\"", SimpleStringUtils.escapeColsForCSV(false, ""));
        Assert.assertEquals("\"test\"", SimpleStringUtils.escapeColsForCSV(false, "test"));
        Assert.assertEquals("\"test\";\"test\"", SimpleStringUtils.escapeColsForCSV(false, "test", "  test    "));
        Assert.assertEquals("\"a řekl: \\\"test\\\"\";\"test\"", SimpleStringUtils.escapeColsForCSV(false, "a řekl: \"test\"", "  test    "));
        
        Assert.assertEquals("\"\"\n", SimpleStringUtils.escapeColsForCSV(true, (String) null));
        Assert.assertEquals("\"\"\n", SimpleStringUtils.escapeColsForCSV(true, ""));
        Assert.assertEquals("\"test\"\n", SimpleStringUtils.escapeColsForCSV(true, "test"));
        Assert.assertEquals("\"test\";\"test\"\n", SimpleStringUtils.escapeColsForCSV(true, "test", "  test    "));
        Assert.assertEquals("\"a řekl: \\\"test\\\"\";\"test\"\n", SimpleStringUtils.escapeColsForCSV(true, "a řekl: \"test\"", "  test    "));
    }
    
    /**
     * test
     */
    @Test
    public final void testEmpty() {
        Assert.assertFalse(SimpleStringUtils.isEmpty("A"));
        Assert.assertFalse(SimpleStringUtils.isEmpty("   b "));
        Assert.assertFalse(SimpleStringUtils.isEmpty(" _"));
        Assert.assertFalse(SimpleStringUtils.isEmpty(" 01 248 4 8498 \n 498 4 654 654 65   "));
        Assert.assertTrue(SimpleStringUtils.isEmpty(null));
        Assert.assertTrue(SimpleStringUtils.isEmpty(""));
        Assert.assertTrue(SimpleStringUtils.isEmpty("    "));
        Assert.assertTrue(SimpleStringUtils.isEmpty("\n"));
        Assert.assertTrue(SimpleStringUtils.isEmpty("\n\n\n\r\n"));
        
        Assert.assertNull(SimpleStringUtils.emptyToNull(null));
        Assert.assertNull(SimpleStringUtils.emptyToNull(""));
        Assert.assertNull(SimpleStringUtils.emptyToNull("    "));
        Assert.assertEquals("a", SimpleStringUtils.emptyToNull("     a    "));
        Assert.assertEquals("Q", SimpleStringUtils.emptyToNull("Q"));
        Assert.assertEquals("9", SimpleStringUtils.emptyToNull("                         9"));
        
        Assert.assertEquals("", SimpleStringUtils.nullToEmpty(null));
        Assert.assertEquals("", SimpleStringUtils.nullToEmpty(""));
        Assert.assertEquals("test", SimpleStringUtils.nullToEmpty("      test   "));
    }
    
    /**
     * test
     */
    @Test
    public final void testAlignRight() {
        Assert.assertEquals("     Hello", SimpleStringUtils.alignRightIfPossible("Hello", 10));
        Assert.assertEquals("Hello", SimpleStringUtils.alignRightIfPossible("Hello", 1));
        Assert.assertEquals("Hello", SimpleStringUtils.alignRightIfPossible("Hello", 5));
        Assert.assertEquals(" Hello", SimpleStringUtils.alignRightIfPossible("Hello", 6));
        Assert.assertEquals("Hello", SimpleStringUtils.alignRightIfPossible("Hello", 0));
        Assert.assertEquals("Hello", SimpleStringUtils.alignRightIfPossible("Hello", -1));
        Assert.assertEquals("Hello", SimpleStringUtils.alignRightIfPossible("Hello", -10));
        Assert.assertEquals("My little pony", SimpleStringUtils.alignRightIfPossible("My little pony", 6));
        Assert.assertEquals("My little pony", SimpleStringUtils.alignRightIfPossible("My little pony", 14));
        Assert.assertEquals(" My little pony", SimpleStringUtils.alignRightIfPossible("My little pony", 15));
        Assert.assertEquals("  My little pony", SimpleStringUtils.alignRightIfPossible("My little pony", 16));
        Assert.assertEquals("      My little pony", SimpleStringUtils.alignRightIfPossible(" My little pony", 20));
        Assert.assertEquals("      My little pony", SimpleStringUtils.alignRightIfPossible("    My little pony", 20));
        Assert.assertEquals("      My little pony", SimpleStringUtils.alignRightIfPossible("    My little pony    ", 20));
        Assert.assertEquals("      My little pony", SimpleStringUtils.alignRightIfPossible("    My little pony     ", 20));
        Assert.assertEquals("My little pony", SimpleStringUtils.alignRightIfPossible("    My little pony     ", 0));
        Assert.assertEquals("My little pony", SimpleStringUtils.alignRightIfPossible("    My little pony     ", -1));
    }
    
    /**
     * test
     */
    @Test
    public final void testJson() {
        Assert.assertEquals("\"hello\"", SimpleStringUtils.stripWhitespace(SimpleStringUtils.toJson("hello")));
        Assert.assertEquals("-153", SimpleStringUtils.stripWhitespace(SimpleStringUtils.toJson(-153)));
        Assert.assertEquals("[\"a\",\"b\",\"c\"]", SimpleStringUtils.stripWhitespace(SimpleStringUtils.toJson(new String[] { "a", "b", "c" })));
        
        Assert.assertEquals(Collections.singletonMap("a", "b"), SimpleStringUtils.fromJson("{\"a\":\"b\"}", Map.class));
    }
    
    /**
     * test
     */
    @Test
    public final void testHash() {
        this.testHash(null, null);
        this.testHash("admin", "77+9M++/vSrvv71I77+977+9Zg/vv70UCu+/vTXvv70MTe+/ve+/vQ==");
        this.testHash("nimda", "77+9y7Lvv708UBbvv71+77+977+9E1rvv73vv73vv71n77+9YQ==");
        this.testHash("Zkouška s diakritikou, znaky, čísly (1234567890).", "xYFeHjB+OlHvv73vv712Ee+/ve+/vXDvv73vv71G77+977+9");
    }
    
    /**
     * test
     */
    @Test
    public final void testEncode() {
        this.testEncode(null, null);
        this.testEncode("", "");
        this.testEncode("ahoj", "YWhvag==");
        this.testEncode("Ahoj!", "QWhvaiE=");
        this.testEncode("1234567890", "MTIzNDU2Nzg5MA==");
        this.testEncode("zkouska base64", "emtvdXNrYSBiYXNlNjQ=");
        this.testEncode("Zkouška s diakritikou, znaky, čísly (1234567890).", "WmtvdcWha2EgcyBkaWFrcml0aWtvdSwgem5ha3ksIMSNw61zbHkgKDEyMzQ1Njc4OTApLg==");
    }
    
    /**
     * test
     */
    @Test
    public final void testUrlEncode() {
        this.testUrlEncode("abc123", "abc123");
        this.testUrlEncode(" ", "+");
        this.testUrlEncode("\"", "%22");
        this.testUrlEncode("\",\"", "%22%2C%22");
        this.testUrlEncode("4ae8rg74a erg .,D_fgasC", "4ae8rg74a+erg+.%2CD_fgasC");
        this.testUrlEncode("<>\"{}|\\^~[]` /?&", "%3C%3E%22%7B%7D%7C%5C%5E%7E%5B%5D%60+%2F%3F%26");
        this.testUrlEncode("+a -b", "%2Ba+-b");
        this.testUrlEncode("+default_lc:a\\[*", "%2Bdefault_lc%3Aa%5C%5B*");
        this.testUrlEncode("[1900 TO 1945]", "%5B1900+TO+1945%5D");
    }
    
    /**
     * test
     */
    @Test
    public final void testUrlDecode() {
        this.testUrlDecode("abc123", "abc123");
        this.testUrlDecode("%20", " ");
        this.testUrlDecode("+", " ");
        this.testUrlDecode("%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%2F%3F%25%23%5B%5D", "!*'();:@&=+$,/?%#[]");
        this.testUrlDecode("4ae8rg74a%20erg%20.,D_fgasC", "4ae8rg74a erg .,D_fgasC");
        this.testUrlDecode("%3C%3E%22%7B%7D%7C%5C%5E%7E%5B%5D%60%20%2F%3F%26", "<>\"{}|\\^~[]` /?&");
        this.testUrlDecode("%2Ba+%2Db", "+a -b");
        this.testUrlDecode("%2Ba+-b", "+a -b");
        this.testUrlDecode("%2Bdefault_lc%3Aa%5C%5B*", "+default_lc:a\\[*");
        this.testUrlDecode("%5B1900%20TO%201945%5D", "[1900 TO 1945]");
    }
    
    /**
     * test
     */
    @Test
    public final void testUrlBothways() {
        this.testUrlBothways(" ");
        this.testUrlBothways("hello, world");
        this.testUrlBothways("+-/*:) _ $ 123");
        this.testUrlBothways("+ěščřžýáíé=ďťň");
        this.testUrlBothways("autorská část katalogu - tohle je český text - zkouška :)");
        this.testUrlBothways(" + ě š č ř ž ý á í é = ď ť ň");
        this.testUrlBothways(" (*,*1910 TO 1950*)~ \ref 123");
        this.testUrlBothways("!*'();:@&=+$,/?%#[]");
        this.testUrlBothways("\"<>.#{}|\\^~[]`+/?&");
        this.testUrlBothways("+default_lc:(a\\[*)");
        this.testUrlBothways("[1900 TO 1945]");
        this.testUrlBothways("{1900 TO 1945}");
    }
    
    /**
     * test
     */
    @Test
    public void testAcceptableQuery() {
        for (final UserRole role : UserRole.values()) {
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*ab*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*AB*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*AB", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*A*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("A*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*A***", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("AB", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*A*B*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("+a -b", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("+QA* -BA*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("+*Ah* -*Nezo* -*Kare* +*Mezzo*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("+aho* +le* *kříž*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*a*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("*@*", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery("?&&&|a", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery(" * ||||| ??b &|", role));
            Assert.assertTrue(SimpleStringUtils.isAcceptableQuery(" * 2||||| ??8 &|", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery(null, role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("*", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("     ", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("**", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("***", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("*  *  *", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery("  *  *  *", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery(" ? *  * ?? *", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery(" ? * && * | ?? *", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery(" * |||||", role));
            Assert.assertFalse(SimpleStringUtils.isAcceptableQuery(" * ||||| ?? &|", role));
        }
    }
    
    /**
     * test
     */
    @Test
    public void testNaturalSort() {
        final List<String> a1 = Arrays.asList("Z", "B", "DEDE", "C", "AAA", "aaa", "154", "15 4");
        final List<String> b1 = Arrays.asList("AAA", "aaa", "B", "C", "DEDE", "Z", "15 4", "154");
        Collections.sort(a1, SimpleStringUtils.getNaturalStringComparator());
        Assert.assertEquals(b1, a1);
        
        final List<String> a2 = Arrays.asList("4x", "5", "5o", "4ox", "10o", "5ox", "11", "11o", "1o", "1", "2o", "something", "~");
        final List<String> b2 = Arrays.asList("~", "something", "1", "1o", "2o", "4ox", "4x", "5", "5o", "5ox", "10o", "11", "11o");
        Collections.sort(a2, SimpleStringUtils.getNaturalStringComparator());
        Assert.assertEquals(b2, a2);
        
        final List<String> a3 = Arrays.asList("2o", "3o", "4ox", "4xo", "10o", "1o", "5o");
        final List<String> b3 = Arrays.asList("1o", "2o", "3o", "4ox", "4xo", "5o", "10o");
        Collections.sort(a3, SimpleStringUtils.getNaturalStringComparator());
        Assert.assertEquals(b3, a3);
        
        final List<String> a4 = Arrays.asList("1", "2", "10", "3", "4", "5");
        final List<String> b4 = Arrays.asList("1", "2", "3", "4", "5", "10");
        Collections.sort(a4, SimpleStringUtils.getNaturalStringComparator());
        Assert.assertEquals(b4, a4);
        
        final List<String> a5 = Arrays.asList("2o", "2O", "3O", "3o", "4xo", "10o", "10O", "5O", "4XO", "1o", "5o", "1O");
        final List<String> b5 = Arrays.asList("1O", "1o", "2O", "2o", "3O", "3o", "4XO", "4xo", "5O", "5o", "10O", "10o");
        Collections.sort(a5, SimpleStringUtils.getNaturalStringComparator());
        Assert.assertEquals(b5, a5);
        
        final List<String> a6 = Arrays.asList("1a1", "1a4", "1a10", "1a2", "1a5", "1a3");
        final List<String> b6 = Arrays.asList("1a1", "1a2", "1a3", "1a4", "1a5", "1a10");
        Collections.sort(a6, SimpleStringUtils.getNaturalStringComparator());
        Assert.assertEquals(b6, a6);
    }
    
    /**
     * test
     */
    @Test
    public void testWordWrap1() {
        Assert.assertEquals("single short row - NO WRAP", SimpleStringUtils.wordwrap("single short row - NO WRAP", 100, "\n"));
        Assert.assertEquals("12345678901234567890", SimpleStringUtils.wordwrap("12345678901234567890", 20, "*|*"));
        Assert.assertEquals("first word*|*second*|*word third*|*word", SimpleStringUtils.wordwrap("first word second word third word", 10, "*|*"));
        Assert.assertEquals("1234567890*|*1234567890", SimpleStringUtils.wordwrap("12345678901234567890", 10, "*|*"));
        Assert.assertEquals("1234567890*|*1234567890", SimpleStringUtils.wordwrap("1234567890 1234567890", 10, "*|*"));
        Assert.assertEquals("1234567890*|*ABCDE", SimpleStringUtils.wordwrap("     1234567890      ABCDE", 10, "*|*"));
        Assert.assertEquals("1234567890      ABCDE", SimpleStringUtils.wordwrap("     1234567890      ABCDE", 100, "*|*"));
        Assert.assertEquals("1 2 3 4 5*|*6 7 8 9 0*|*1 2 3 4 5*|*6 7 8 9 0", SimpleStringUtils.wordwrap("1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 ", 10, "*|*"));
        Assert.assertEquals("FIRSTLONGWORD*|*SHORT", SimpleStringUtils.wordwrap("FIRSTLONGWORD SHORT", 17, "*|*"));
        Assert.assertEquals("FIRSTLONGWORD\n|\nSHORT SHORT\n|\nNEXTLONGWORD\n|\nSHORT A B C D E\n|\nF SHORT LONG\n|\nSOMEWORD\n|\nLONGWORDETCETC", SimpleStringUtils.wordwrap("FIRSTLONGWORD SHORT SHORT NEXTLONGWORD SHORT A B C D E F SHORT LONG SOMEWORD LONGWORDETCETC", 15, "\n|\n"));
    }
    
    /**
     * test
     */
    @Test
    public void testWordWrap2() {
        final String sw1 = "Tady je testovací OCR. Tady je testovací\r\n" +
                "OCR. Tady je testovací OCR. Tady je\r\n" +
                "testovací OCR. Tady je testovací OCR. Tady\r\n" +
                "je testovací OCR. Tady je testovací OCR.\r\n" +
                "Tady je testovací OCR. Tady je testovací\r\n" +
                "OCR. Tady je testovací OCR. Tady je\r\n" +
                "testovací OCR. Tady je testovací OCR. Tady\r\n" +
                "je testovací OCR. Tady je testovací OCR.\r\n" +
                "Tady je testovací OCR. Tady je testovací\r\n" +
                "OCR. Tady je testovací OCR. Tady je\r\n" +
                "testovací OCR. Tady je testovací OCR. Tady\r\n" +
                "je testovací OCR. Tady je testovací OCR.\r\n" +
                "Tady je testovací OCR.";
        
        final String ss1 = "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR. " +
                "Tady je testovací OCR. Tady je testovací OCR.";
        
        Assert.assertEquals(sw1, SimpleStringUtils.wordwrap(ss1, 42, "\r\n"));
        
        final String sw2 = "Jeppe Aakjaer: Srdce klid! [Báseň] Srdce klid, jde\r\nslunce spát.";
        final String ss2 = "Jeppe Aakjaer: Srdce klid! [Báseň] Srdce klid, jde slunce spát.";
        
        Assert.assertEquals(sw2, SimpleStringUtils.wordwrap(ss2, 50, "\r\n"));
        
        final String sw3 = "1234567890\n1234567890\n1234567890";
        final String ss3 = "123456789012345678901234567890";
        
        Assert.assertEquals(sw3, SimpleStringUtils.wordwrap(ss3, 10, "\n"));
        
        final String sw4 = "1234567890\n1234567890\n1234567890";
        final String ss4 = "1234567890 1234567890 1234567890";
        
        Assert.assertEquals(sw4, SimpleStringUtils.wordwrap(ss4, 10, "\n"));
        
        final String sw5 = "1234567890+1234567890+1234567890";
        final String ss5 = "1234567890 1234567890 1234567890";
        
        Assert.assertEquals(sw5, SimpleStringUtils.wordwrap(ss5, 10, "+"));
        
        final String sw6 = "Jiří Brandes: Dánská literatura po r. 1870 [Essay]";
        final String ss6 = "Jiří Brandes: Dánská literatura po r. 1870 [Essay]";
        
        Assert.assertEquals(sw6, SimpleStringUtils.wordwrap(ss6, 50, "+"));
        
        final String sw7 = "12345\r\n12345\r\n12345";
        final String ss7 = "12345\r\n12345\r\n12345\r\n";
        
        Assert.assertEquals(sw7, SimpleStringUtils.wordwrap(ss7, 5, "\r\n"));
        
        final String sw8 = "12345\r\n6\r\n12345\r\n6";
        final String ss8 = "123456\r\n123456";
        
        Assert.assertEquals(sw8, SimpleStringUtils.wordwrap(ss8, 5, "\r\n"));
    }
    
    /**
     * test
     */
    @Test
    public void testFromJson() {
        final String json = "{\"rows\":[" +
                "{\"key\":[\"O\",\"HOFFMEISTER, ADOLF 1920\",\"Hoffmeister, Adolf 1920\"],\"value\":{\"sum\":21528,\"count\":207,\"min\":1,\"max\":207,\"sumsqr\":2978040}}," +
                "{\"key\":[\"O\",\"HOFFMEISTER, ADOLF 1933\",\"Hoffmeister, Adolf 1933\"],\"value\":{\"sum\":29890,\"count\":244,\"min\":1,\"max\":244,\"sumsqr\":4872070}}," +
                "{\"key\":[\"O\",\"HOF\u00cdREK\",\"Hof\u00edrek\"],\"value\":{\"sum\":33670,\"count\":259,\"min\":1,\"max\":259,\"sumsqr\":5824910}}," +
                "{\"key\":[\"O\",\"HOFMANNSTHAL\",\"Hofmannsthal\"],\"value\":{\"sum\":10011,\"count\":141,\"min\":1,\"max\":141,\"sumsqr\":944371}}," +
                "{\"key\":[\"O\",\"HOFMEISTER\",\"Hofmeister\"],\"value\":{\"sum\":20910,\"count\":204,\"min\":1,\"max\":204,\"sumsqr\":2850730}}," +
                "{\"key\":[\"O\",\"HOL\u00c1\",\"Hol\u00e1\"],\"value\":{\"sum\":57630,\"count\":339,\"min\":1,\"max\":339,\"sumsqr\":13043590}}," +
                "{\"key\":[\"O\",\"HOLAN\",\"Holan\"],\"value\":{\"sum\":25651,\"count\":226,\"min\":1,\"max\":226,\"sumsqr\":3873301}}," +
                "{\"key\":[\"O\",\"H\u00d6LDERIN\",\"H\u00f6lderin\"],\"value\":{\"sum\":8001,\"count\":126,\"min\":1,\"max\":126,\"sumsqr\":674751}}," +
                "{\"key\":[\"O\",\"HOLEC\",\"Holec\"],\"value\":{\"sum\":5778,\"count\":107,\"min\":1,\"max\":107,\"sumsqr\":414090}}" +
                "]}";
        
        final BasicDocument doc = SimpleStringUtils.fromJson(json, BasicDocument.class);
        final List<?> list = (List<?>) doc.getProperty("rows");
        Assert.assertEquals(9, list.size());
        final Map<?, ?> map = (Map<?, ?>) list.get(5);
        final List<?> list2 = (List<?>) map.get("key");
        Assert.assertEquals("HOLÁ", list2.get(1));
        Assert.assertEquals("Holá", list2.get(2));
    }
    
    /**
     * test
     */
    @Test
    public void testNeverEmpty() {
        Assert.assertEquals("-", SimpleStringUtils.neverEmpty(null));
        Assert.assertEquals("-", SimpleStringUtils.neverEmpty(""));
        Assert.assertEquals("-", SimpleStringUtils.neverEmpty("-"));
        Assert.assertEquals("a", SimpleStringUtils.neverEmpty("a"));
    }
    
    /**
     * test
     */
    @Test
    public void testInflect() {
        Assert.assertEquals("0 w5", SimpleStringUtils.inflect(0, "w1", "w234", "w5"));
        
        Assert.assertEquals("1 w1", SimpleStringUtils.inflect(1, "w1", "w234", "w5"));
        Assert.assertEquals("2 w234", SimpleStringUtils.inflect(2, "w1", "w234", "w5"));
        Assert.assertEquals("3 w234", SimpleStringUtils.inflect(3, "w1", "w234", "w5"));
        Assert.assertEquals("4 w234", SimpleStringUtils.inflect(4, "w1", "w234", "w5"));
        Assert.assertEquals("5 w5", SimpleStringUtils.inflect(5, "w1", "w234", "w5"));
        
        Assert.assertEquals("-1 w1", SimpleStringUtils.inflect(-1, "w1", "w234", "w5"));
        Assert.assertEquals("-2 w234", SimpleStringUtils.inflect(-2, "w1", "w234", "w5"));
        Assert.assertEquals("-3 w234", SimpleStringUtils.inflect(-3, "w1", "w234", "w5"));
        Assert.assertEquals("-4 w234", SimpleStringUtils.inflect(-4, "w1", "w234", "w5"));
        Assert.assertEquals("-5 w5", SimpleStringUtils.inflect(-5, "w1", "w234", "w5"));
    }
    
    /**
     * test
     */
    @Test
    public void testRandomString() {
        for (int l = 1; l <= 10; l++) {
            for (int i = 0; i < 1000; i++) {
                Assert.assertEquals(l, SimpleStringUtils.getRandomString(l).length());
            }
        }
    }
    
    /**
     * test
     */
    @Test
    public void testNormalize() {
        Assert.assertEquals("1234567890", SimpleStringUtils.normalizeToAscii("1234567890"));
        Assert.assertEquals("hello_WORLD", SimpleStringUtils.normalizeToAscii("hello WORLD"));
        Assert.assertEquals("hello_-_[world]_-_vezicka", SimpleStringUtils.normalizeToAscii("<hello> - [world] - {věžička}"));
        Assert.assertEquals("12-)-cerstvy-unor-zezulicka-WITA", SimpleStringUtils.normalizeToAscii("12-:)-čerstvý-únor-žežulička-WÍŤA"));
    }
    
    /**
     * test
     * 
     * @param input
     * input
     */
    private final void testUrlBothways(final String input) {
        Assert.assertEquals(input, SimpleStringUtils.decodeFromUrl((SimpleStringUtils.encodeForUrl(input))));
    }
    
    /**
     * test
     * 
     * @param input
     * input
     * @param output
     * output
     */
    private final void testUrlEncode(final String input, final String output) {
        Assert.assertEquals(output, SimpleStringUtils.encodeForUrl(input));
    }
    
    /**
     * test
     * 
     * @param input
     * input
     * @param output
     * output
     */
    private final void testUrlDecode(final String input, final String output) {
        Assert.assertEquals(output, SimpleStringUtils.decodeFromUrl(input));
    }
    
    /**
     * test
     * 
     * @param input
     * input
     * @param output
     * output
     */
    private final void testEncode(final String input, final String output) {
        Assert.assertEquals(output, SimpleStringUtils.getBase64(input));
    }
    
    /**
     * test
     * 
     * @param input
     * input
     * @param output
     * output
     */
    private final void testHash(final String input, final String output) {
        Assert.assertEquals(output, SimpleStringUtils.getHash(input));
    }
}
