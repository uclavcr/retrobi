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

import junit.framework.Assert;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;
import org.junit.Test;

import cz.insophy.retrobi.database.entity.SearchResult;
import cz.insophy.retrobi.database.entity.SearchResultRow;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.exception.GeneralRepositoryException;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleSearchUtilsTest {
    /**
     * test
     */
    @Test
    public void testLowercase() {
        Assert.assertEquals("123", SimpleSearchUtils.queryToLowercase("123"));
        Assert.assertEquals("a a b ěšč", SimpleSearchUtils.queryToLowercase("a A b ěšč"));
        Assert.assertEquals("a běbů +154", SimpleSearchUtils.queryToLowercase("a BĚBŮ +154"));
        
        Assert.assertEquals("[15487 8484]", SimpleSearchUtils.queryToLowercase("[15487 8484]"));
        Assert.assertEquals("[15487 TO 8484]", SimpleSearchUtils.queryToLowercase("[15487 TO 8484]"));
        Assert.assertEquals("[15487 TO aaa]", SimpleSearchUtils.queryToLowercase("[15487 TO AAA]"));
        Assert.assertEquals("[15487 to 1054", SimpleSearchUtils.queryToLowercase("[15487 TO 1054"));
        Assert.assertEquals("123 to 456", SimpleSearchUtils.queryToLowercase("123 TO 456"));
        Assert.assertEquals("aaa to bbb", SimpleSearchUtils.queryToLowercase("AAA TO BBB"));
        Assert.assertEquals("[aaa TO bbb]", SimpleSearchUtils.queryToLowercase("[AAA TO BBB]"));
        Assert.assertEquals("[12a TO 34b]", SimpleSearchUtils.queryToLowercase("[12A TO 34B]"));
        Assert.assertEquals("+[123 TO 456] -[9999 TO 10000]", SimpleSearchUtils.queryToLowercase("+[123 TO 456] -[9999 TO 10000]"));
        Assert.assertEquals("+[123 TO 456] a to 1557 to qwr to 1 to 21 -[9999 TO 10000]", SimpleSearchUtils.queryToLowercase("+[123 TO 456] A TO 1557 TO QWR TO 1 TO 21 -[9999 TO 10000]"));
        Assert.assertEquals("+[123 TO 456] -[9999 TO 10000] +[154 to 12 to 47]", SimpleSearchUtils.queryToLowercase("+[123 TO 456] -[9999 TO 10000] +[154 TO 12 TO 47]"));
        Assert.assertEquals("[1 TO 2]", SimpleSearchUtils.queryToLowercase("[1 TO 2]"));
        Assert.assertEquals("+default:*ab*e* +default_lc:124*wfa", SimpleSearchUtils.queryToLowercase("+default:*AB*E* +default_lc:124*Wfa"));
        Assert.assertEquals("+default:*ab*e* +default_lc:124*wfa +default:[13 TO 54]", SimpleSearchUtils.queryToLowercase("+default:*AB*E* +default_lc:124*Wfa +default:[13 TO 54]"));
        Assert.assertEquals("+default:*ab*e* +default:[aaa TO bbb] +default:[ccc TO ddd]", SimpleSearchUtils.queryToLowercase("+default:*AB*E* +default:[AAA TO BBB] +default:[CCC TO DDD]"));
        
        Assert.assertEquals("{15487 8484}", SimpleSearchUtils.queryToLowercase("{15487 8484}"));
        Assert.assertEquals("{15487 TO 8484}", SimpleSearchUtils.queryToLowercase("{15487 TO 8484}"));
        Assert.assertEquals("{15487 TO aaa}", SimpleSearchUtils.queryToLowercase("{15487 TO AAA}"));
        Assert.assertEquals("{15487 to 1054", SimpleSearchUtils.queryToLowercase("{15487 TO 1054"));
        Assert.assertEquals("123 to 456", SimpleSearchUtils.queryToLowercase("123 TO 456"));
        Assert.assertEquals("aaa to bbb", SimpleSearchUtils.queryToLowercase("AAA TO BBB"));
        Assert.assertEquals("{aaa TO bbb}", SimpleSearchUtils.queryToLowercase("{AAA TO BBB}"));
        Assert.assertEquals("{12a TO 34b}", SimpleSearchUtils.queryToLowercase("{12A TO 34B}"));
        Assert.assertEquals("+{123 TO 456} -{9999 TO 10000}", SimpleSearchUtils.queryToLowercase("+{123 TO 456} -{9999 TO 10000}"));
        Assert.assertEquals("+{123 TO 456} a to 1557 to qwr to 1 to 21 -{9999 TO 10000}", SimpleSearchUtils.queryToLowercase("+{123 TO 456} A TO 1557 TO QWR TO 1 TO 21 -{9999 TO 10000}"));
        Assert.assertEquals("+{123 TO 456} -{9999 TO 10000} +{154 to 12 to 47}", SimpleSearchUtils.queryToLowercase("+{123 TO 456} -{9999 TO 10000} +{154 TO 12 TO 47}"));
        Assert.assertEquals("{1 TO 2}", SimpleSearchUtils.queryToLowercase("{1 TO 2}"));
        Assert.assertEquals("+default:*ab*e* +default_lc:124*wfa", SimpleSearchUtils.queryToLowercase("+default:*AB*E* +default_lc:124*Wfa"));
        Assert.assertEquals("+default:*ab*e* +default_lc:124*wfa +default:{13 TO 54}", SimpleSearchUtils.queryToLowercase("+default:*AB*E* +default_lc:124*Wfa +default:{13 TO 54}"));
        Assert.assertEquals("+default:*ab*e* +default:{aaa TO bbb} +default:{ccc TO ddd}", SimpleSearchUtils.queryToLowercase("+default:*AB*E* +default:{AAA TO BBB} +default:{CCC TO DDD}"));
        
        Assert.assertEquals("+default:{aaa1b TO bbb54e54} +default:[ccc1d TO ddd54e54]", SimpleSearchUtils.queryToLowercase("+default:{AaA1B TO bbb54E54} +default:[CCC1d TO DDD54e54]"));
        
        Assert.assertEquals("+default:aaa1a && bbb1b -default:*ccc1c* || *ddd1d* ! *eee1e*", SimpleSearchUtils.queryToLowercase("+default:AAA1a AND Bbb1B -default:*ccc1C* OR *DdD1d* NOT *EeE1e*"));
        Assert.assertEquals("(+default:aaa1a && bbb1b -default:*ccc1c*) || *ddd1d* ! *eee1e*", SimpleSearchUtils.queryToLowercase("(+default:AAA1a AND Bbb1B -default:*ccc1C*) OR *DdD1d* NOT *EeE1e*"));
        Assert.assertEquals("a && b && c || d && e || (f && g)", SimpleSearchUtils.queryToLowercase("A AND B AND C OR D AND E OR (F AND G)"));
        Assert.assertEquals("a && ! (b || c) && ! d ! e", SimpleSearchUtils.queryToLowercase("A AND NOT (B OR C) AND NOT D NOT e"));
        Assert.assertEquals("a && c && ! (b || c) && ! d ! e", SimpleSearchUtils.queryToLowercase("A AND C && NOT (B OR C) AND NOT D NOT e"));
        Assert.assertEquals("\"and\" && \"and\"", SimpleSearchUtils.queryToLowercase("\"AND\" AND \"AND\""));
    }
    
    /**
     * test
     * 
     * @throws GeneralRepositoryException
     * -
     */
    @Test
    public void testHighlight() throws GeneralRepositoryException {
        final Formatter formatter = new Formatter() {
            @Override
            public String highlightTerm(final String originalText, final TokenGroup tokenGroup) {
                if (tokenGroup.getTotalScore() <= 0) {
                    return originalText;
                }
                
                return "<strong>" + originalText + "</strong>";
            }
        };
        
        Assert.assertEquals("<strong>1</strong> 2 3 4 5 6 7 8 9", SimpleSearchUtils.highlight(formatter, "1 2 3 4 5 6 7 8 9", "1", false));
        Assert.assertEquals("<strong>1</strong> <strong>2</strong> 3 4 5 6 7 <strong>8</strong> 9", SimpleSearchUtils.highlight(formatter, "1 2 3 4 5 6 7 8 9", "1 2 8", false));
        Assert.assertEquals("A <strong>B</strong> <strong>C</strong>", SimpleSearchUtils.highlight(formatter, "A B C", "B C", true));
        Assert.assertEquals("a <strong>b</strong> <strong>c</strong>", SimpleSearchUtils.highlight(formatter, "A B C", "B C", false));
        Assert.assertEquals(null, SimpleSearchUtils.highlight(formatter, "A B C", "b c", true));
        Assert.assertEquals("a <strong>b</strong> <strong>c</strong>", SimpleSearchUtils.highlight(formatter, "A B C", "b c", false));
        Assert.assertEquals("a <strong>b</strong> <strong>c</strong>", SimpleSearchUtils.highlight(formatter, "a b c", "b c", true));
        Assert.assertEquals("a <strong>b</strong> <strong>c</strong>", SimpleSearchUtils.highlight(formatter, "a b c", "b c", false));
        Assert.assertEquals("1 2 <strong>3</strong> <strong>4</strong> <strong>5</strong> 6 7 8 9", SimpleSearchUtils.highlight(formatter, "1 2 3 4 5 6 7 8 9", "[3 TO 5]", true));
        Assert.assertEquals("1 2 <strong>3</strong> <strong>4</strong> <strong>5</strong> 6 7 8 9", SimpleSearchUtils.highlight(formatter, "1 2 3 4 5 6 7 8 9", "[3 TO 5]", false));
        Assert.assertEquals("1 2 3 <strong>4</strong> 5 6 7 8 9", SimpleSearchUtils.highlight(formatter, "1 2 3 4 5 6 7 8 9", "{3 TO 5}", false));
        Assert.assertEquals("1 2 3 <strong>4</strong> 5 6 7 8 9", SimpleSearchUtils.highlight(formatter, "1 2 3 4 5 6 7 8 9", "{3 TO 5}", true));
    }
    
    /**
     * test
     */
    @Test
    public void testConversion() {
        final AttributePrototype p11 = new AttributePrototype("p11", "p11", true);
        final AttributePrototype p12 = new AttributePrototype("p12", "p12", true);
        final AttributePrototype p13 = new AttributePrototype("p13", "p13", true);
        final AttributePrototype p1 = new AttributePrototype("p1", "p1", true, p11, p12, p13);
        
        final AttributeNode n1 = SimpleAttributeUtils.createFromPrototype(p1);
        ((AtomicAttributeNode) n1.find(p11).get(0)).setValue("p11v");
        ((AtomicAttributeNode) n1.find(p12).get(0)).setValue("p12v");
        ((AtomicAttributeNode) n1.find(p13).get(0)).setValue("p13v");
        
        Assert.assertEquals("p11v", SimpleSearchUtils.toHighlightData(n1, p11, "-"));
        Assert.assertEquals("p12v", SimpleSearchUtils.toHighlightData(n1, p12, "-"));
        Assert.assertEquals("p13v", SimpleSearchUtils.toHighlightData(n1, p13, "-"));
        Assert.assertEquals("p11v-p12v-p13v", SimpleSearchUtils.toHighlightData(n1, p1, "-"));
        
        Assert.assertEquals("p11v|p12v|p13v", SimpleSearchUtils.toHighlightData(n1, "|"));
    }
    
    /**
     * test
     */
    @Test
    public void testToString() {
        Assert.assertEquals("", SimpleSearchUtils.objectToString(null, "|"));
        Assert.assertEquals("false", SimpleSearchUtils.objectToString(false, "|"));
        Assert.assertEquals("true", SimpleSearchUtils.objectToString(true, "|"));
        Assert.assertEquals("42", SimpleSearchUtils.objectToString(42, "|"));
        Assert.assertEquals("42.187", SimpleSearchUtils.objectToString(42.187d, "|"));
        Assert.assertEquals("test", SimpleSearchUtils.objectToString("test", "|"));
        Assert.assertEquals("a|b|c", SimpleSearchUtils.objectToString(Arrays.asList("a", "b", "c"), "|"));
        Assert.assertEquals("b", SimpleSearchUtils.objectToString(Collections.singletonMap("a", "b"), "|"));
    }
    
    /**
     * test
     */
    @Test
    public void testExtendQuery() {
        Assert.assertEquals("+default_lc:(a || b)", SimpleSearchUtils.extendQuery("A || b", false, null, null));
        Assert.assertEquals("+default:(A || b)", SimpleSearchUtils.extendQuery("A || b", true, null, null));
    }
    
    /**
     * test
     */
    @Test
    public void testSearchResultToId() {
        final SearchResult sr = new SearchResult();
        final SearchResultRow srr1 = new SearchResultRow();
        srr1.setId("id1");
        final SearchResultRow srr2 = new SearchResultRow();
        srr2.setId("id2");
        sr.setRows(Arrays.asList(srr1, srr2));
        
        Assert.assertEquals(Arrays.asList("id1", "id2"), SimpleSearchUtils.extractCardIds(sr));
    }
}
