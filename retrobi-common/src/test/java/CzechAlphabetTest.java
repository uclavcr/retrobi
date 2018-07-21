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

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.utils.CzechAlphabet;

/**
 * @author Vojtěch Hordějčuk
 */
public class CzechAlphabetTest {
    /**
     * fallback letter (must be the same as in CzechAlphabet)
     */
    private static final String FALLBACK_LETTER = "?";
    
    /**
     * test
     */
    @Test
    public final void testBelongsToLetter() {
        final CzechAlphabet a = CzechAlphabet.getInstance();
        
        Assert.assertTrue(a.belongsToLetter("Amaranth", "A"));
        Assert.assertTrue(a.belongsToLetter("de Balon", "B"));
        Assert.assertTrue(a.belongsToLetter("citron", "C"));
        Assert.assertTrue(a.belongsToLetter("Ćsabo", "C"));
        Assert.assertTrue(a.belongsToLetter("Česká republika", "Č"));
        Assert.assertTrue(a.belongsToLetter("černý", "Č"));
        Assert.assertTrue(a.belongsToLetter("  děda ", "D"));
        Assert.assertTrue(a.belongsToLetter("von Dittrich", "D"));
        Assert.assertTrue(a.belongsToLetter("123456", CzechAlphabetTest.FALLBACK_LETTER));
        
        Assert.assertFalse(a.belongsToLetter("  děda ", "A"));
        Assert.assertFalse(a.belongsToLetter(" Děda ", "Ď"));
        Assert.assertFalse(a.belongsToLetter("Amaranth", "?"));
        Assert.assertFalse(a.belongsToLetter("Ćsabo", "Ć"));
        Assert.assertFalse(a.belongsToLetter("", "A"));
        Assert.assertFalse(a.belongsToLetter(null, "A"));
        Assert.assertFalse(a.belongsToLetter("A", null));
        Assert.assertFalse(a.belongsToLetter(null, null));
        Assert.assertFalse(a.belongsToLetter("A", ""));
        Assert.assertFalse(a.belongsToLetter(CzechAlphabetTest.FALLBACK_LETTER, "A"));
    }
    
    /**
     * test
     */
    @Test
    public final void testGetFirstAlphabetLetter() {
        final CzechAlphabet a = CzechAlphabet.getInstance();
        
        // default
        
        Assert.assertEquals("A", a.getAlphabetFirstLetter("AHOJ"));
        Assert.assertEquals("A", a.getAlphabetFirstLetter("ahoj"));
        Assert.assertEquals("A", a.getAlphabetFirstLetter("ÁHOJ"));
        Assert.assertEquals("A", a.getAlphabetFirstLetter("áhoj"));
        Assert.assertEquals("A", a.getAlphabetFirstLetter("von Auswitz"));
        Assert.assertEquals("A", a.getAlphabetFirstLetter("..bAb.."));
        
        Assert.assertEquals("B", a.getAlphabetFirstLetter("BABA"));
        Assert.assertEquals("B", a.getAlphabetFirstLetter("baba"));
        
        Assert.assertEquals("C", a.getAlphabetFirstLetter("CITRON"));
        Assert.assertEquals("C", a.getAlphabetFirstLetter("citron"));
        Assert.assertEquals("C", a.getAlphabetFirstLetter("ĆSABO"));
        Assert.assertEquals("C", a.getAlphabetFirstLetter("ćsabo"));
        
        Assert.assertEquals("Č", a.getAlphabetFirstLetter("ČECHY"));
        Assert.assertEquals("Č", a.getAlphabetFirstLetter("čechy"));
        
        Assert.assertEquals("D", a.getAlphabetFirstLetter("DĚDA"));
        Assert.assertEquals("D", a.getAlphabetFirstLetter("děda"));
        
        // aliases
        
        Assert.assertEquals("O", a.getAlphabetFirstLetter("Ö-RE-ME"));
        Assert.assertEquals("O", a.getAlphabetFirstLetter("ómačka"));
        Assert.assertEquals("U", a.getAlphabetFirstLetter("übrigens"));
        Assert.assertEquals("C", a.getAlphabetFirstLetter("Ćsabo"));
        Assert.assertEquals("A", a.getAlphabetFirstLetter("áve césar"));
        
        // invalid
        
        Assert.assertEquals(CzechAlphabetTest.FALLBACK_LETTER, a.getAlphabetFirstLetter("132456"));
        Assert.assertEquals(CzechAlphabetTest.FALLBACK_LETTER, a.getAlphabetFirstLetter(".,..,."));
        Assert.assertEquals(CzechAlphabetTest.FALLBACK_LETTER, a.getAlphabetFirstLetter("  . ,..,.   "));
        Assert.assertEquals(CzechAlphabetTest.FALLBACK_LETTER, a.getAlphabetFirstLetter(null));
    }
    
    /**
     * test
     */
    @Test
    public final void testGetFirstLetter() {
        Assert.assertEquals("N", CzechAlphabet.getFirstLetter("Novák"));
        Assert.assertEquals("B", CzechAlphabet.getFirstLetter("babička"));
        Assert.assertEquals("T", CzechAlphabet.getFirstLetter("TATA"));
        Assert.assertEquals("A", CzechAlphabet.getFirstLetter("123atrěščů////*-AT A"));
        Assert.assertEquals("P", CzechAlphabet.getFirstLetter("de Puzo"));
        Assert.assertEquals("S", CzechAlphabet.getFirstLetter("von Schirach-Neumann"));
        Assert.assertEquals("G", CzechAlphabet.getFirstLetter("van Graff"));
        Assert.assertEquals("A", CzechAlphabet.getFirstLetter("d'Alambert"));
        Assert.assertEquals("H", CzechAlphabet.getFirstLetter("van den Hosen"));
        Assert.assertEquals("H", CzechAlphabet.getFirstLetter("van der Hosen"));
        Assert.assertEquals("A", CzechAlphabet.getFirstLetter("ahoj"));
        Assert.assertEquals("O", CzechAlphabet.getFirstLetter("O'Hara"));
        Assert.assertEquals("H", CzechAlphabet.getFirstLetter("o'Hara"));
        Assert.assertEquals("O", CzechAlphabet.getFirstLetter("o'hara"));
        Assert.assertEquals("B", CzechAlphabet.getFirstLetter("Babička"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("cha-cha"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("? Charakter"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("Charakter"));
        Assert.assertEquals("C", CzechAlphabet.getFirstLetter("testC"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("testChx"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("testCh"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("testCH"));
        Assert.assertEquals("H", CzechAlphabet.getFirstLetter("testcH"));
        Assert.assertEquals("CH", CzechAlphabet.getFirstLetter("-CHarakter"));
        Assert.assertEquals("C", CzechAlphabet.getFirstLetter("Creative"));
        Assert.assertEquals("N", CzechAlphabet.getFirstLetter("    no a co!"));
        Assert.assertEquals("A", CzechAlphabet.getFirstLetter("d'Alambert"));
        
        Assert.assertNull(CzechAlphabet.getFirstLetter("123456"));
        Assert.assertNull(CzechAlphabet.getFirstLetter(""));
        Assert.assertNull(CzechAlphabet.getFirstLetter("   !   : ? :)/  "));
    }
    
    /**
     * test
     */
    @Test
    public final void testGetPrevNextLetter() {
        final CzechAlphabet a = CzechAlphabet.getInstance();
        
        Assert.assertEquals("A", a.getAlphabetPreviousLetter("B"));
        Assert.assertEquals("Č", a.getAlphabetPreviousLetter("D"));
        Assert.assertEquals("Z", a.getAlphabetPreviousLetter("Ž"));
        Assert.assertNull(a.getAlphabetPreviousLetter("A"));
        Assert.assertNull(a.getAlphabetPreviousLetter(""));
        Assert.assertNull(a.getAlphabetPreviousLetter(null));
        Assert.assertNull(a.getAlphabetPreviousLetter("5"));
        
        Assert.assertEquals("B", a.getAlphabetNextLetter("A"));
        Assert.assertEquals("D", a.getAlphabetNextLetter("Č"));
        Assert.assertEquals("Ž", a.getAlphabetNextLetter("Z"));
        Assert.assertNull(a.getAlphabetNextLetter("?"));
        Assert.assertNull(a.getAlphabetNextLetter(""));
        Assert.assertNull(a.getAlphabetNextLetter(null));
        Assert.assertNull(a.getAlphabetNextLetter("5"));
    }
    
    /**
     * test
     */
    @Test
    public final void testGetSortBatch() {
        Assert.assertEquals("", CzechAlphabet.getDefaultBatchForSort(""));
        Assert.assertEquals(",,...,.,.,", CzechAlphabet.getDefaultBatchForSort(",,...,.,.,"));
        Assert.assertEquals("NOVÁK", CzechAlphabet.getDefaultBatchForSort("Novák"));
        Assert.assertEquals("NOVÁK", CzechAlphabet.getDefaultBatchForSort("   NoVák  "));
        Assert.assertEquals("123", CzechAlphabet.getDefaultBatchForSort("   123  "));
        Assert.assertEquals("BABIČKA", CzechAlphabet.getDefaultBatchForSort("babička"));
        Assert.assertEquals("TATA", CzechAlphabet.getDefaultBatchForSort("TATA"));
        Assert.assertEquals("OHARA", CzechAlphabet.getDefaultBatchForSort("O´Hara"));
        Assert.assertEquals("ONEILL", CzechAlphabet.getDefaultBatchForSort("O´Neill"));
        Assert.assertEquals("AT A", CzechAlphabet.getDefaultBatchForSort("123atrěščů////*-AT A"));
        Assert.assertEquals("PUZO", CzechAlphabet.getDefaultBatchForSort("de Puzo"));
        Assert.assertEquals("SCHIRACH-NEUMANN", CzechAlphabet.getDefaultBatchForSort("von Schirach-Neumann"));
        Assert.assertEquals("GRAFF", CzechAlphabet.getDefaultBatchForSort("van Graff"));
        Assert.assertEquals("ALAMBERT", CzechAlphabet.getDefaultBatchForSort("d'Alambert"));
        Assert.assertEquals("HOSEN", CzechAlphabet.getDefaultBatchForSort("van den Hosen"));
        Assert.assertEquals("HOSEN", CzechAlphabet.getDefaultBatchForSort("van der Hosen"));
    }
    
    /**
     * test
     */
    @Test
    public final void testSort() {
        final List<String> list = Arrays.asList(new String[] {
                "BÁ",
                "O´hara",
                "cha",
                "O´Neill",
                "Běhounek",
                "Brücke",
                "Göbl",
                "Bém",
                "Döbler",
                "Bělinskij",
                "Ópletal",
                "B-",
                "Go",
                "Gp",
                "CA",
                "B ",
                "Müller, K",
                "O´Casey",
                "ZA",
                "Dobler",
                "Dada",
                "cÄ",
                "Opletal",
                "Böckh",
                "Běla",
                "CHA",
                "Bröndal",
                "Müller",
                "B–",
                "obr",
                "Bekal",
                "Bejček",
                "Döbler",
                "B",
                "Ćsabo",
                "BA",
                "ca",
                "CA",
                "O´Hara",
                "O´casey",
                "CZ",
                "děda",
        });
        
        final List<String> correct = Arrays.asList(new String[] {
                "B",
                "B ",
                "B-",
                "B–",
                "BA",
                "BÁ",
                "Bejček",
                "Bekal",
                "Bém",
                "Běhounek",
                "Běla",
                "Bělinskij",
                "Böckh",
                "Bröndal",
                "Brücke",
                "CA",
                "CA",
                "ca",
                "cÄ",
                "Ćsabo",
                "CZ",
                "Dada",
                "děda",
                "Dobler",
                "Döbler",
                "Döbler",
                "Go",
                "Göbl",
                "Gp",
                "CHA",
                "cha",
                "Müller",
                "Müller, K",
                "obr",
                "O´Casey",
                "O´casey",
                "O´Hara",
                "O´hara",
                "O´Neill",
                "Opletal",
                "Ópletal",
                "ZA",
        });
        
        Collections.sort(list, CzechAlphabet.getInstance());
        
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals(correct.get(i), list.get(i));
        }
    }
}
