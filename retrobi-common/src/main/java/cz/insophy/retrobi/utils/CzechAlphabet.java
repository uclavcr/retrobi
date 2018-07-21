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

package cz.insophy.retrobi.utils;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for holding the czech alphabet and comparator.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CzechAlphabet implements Comparator<String> {
    /**
     * singleton instance
     */
    private static CzechAlphabet instance = null;
    /**
     * the fallback letter (note: if changed, update unit tests)
     */
    private static final String FALLBACK_LETTER = "?";
    /**
     * the alphabet (note: if changed, update unit tests) - missing letters
     * (e.g. Ň) will be filed under the fallback letter (?)
     */
    private static final List<String> ALPHABET = Collections.unmodifiableList(Arrays.asList(
            ("A,B,C,Č,D,Ď,E,F,G,H,CH,I,J,K,L,M,N,O,P,Q,R,Ř,S,Š,T,Ť,U,V,W,X,Y,Z,Ž," + CzechAlphabet.FALLBACK_LETTER).split(",")));
    /**
     * the collation rules (note: if changed, update unit tests)
     */
    private static final String RULES = "" +
            "< ' ' < '.' < ',' < ';' < '?' < '¿' < '!' < '¡' < ':' < '\"' < '\'' < '«' < '»' " +
            "< '-' < '–' < '|' < '/' < '\\' < '(' < ')' < '[' < ']' < '<' < '>' < '{' < '}' " +
            "< '&' < '¢' < '£' < '¤' < '¥' < '§' < '©' < '®' < '%' < '‰' < '$' " +
            "< '=' < '+' < '×' < '*' < '÷' < '~' " +
            "< A,a;Á,á;À,à;Â,â;Ą,ą < Ä,ä < B,b < C,c;Ç,ç;Ć,ć < Č,č < D,d < Ď,ď < E,e;É,é;È,è;Ê,ê;Ë,ë;Ę,ę < Ě,ě " +
            "< F,f < G,g < H,h < CH,Ch,cH,ch < I,i;Í,í;Ï,ï < J,j < K,k < L,l;Ľ,ľ;Ł,ł < M,m < N,n < Ň,ň " +
            "< O,o;Ó,ó;Ô,ô < Ö,ö < P,p < Q,q < R,r;Ŕ,ŕ < Ř,ř < S,s;Ś,ś < Š,š < T,t < Ť,ť " +
            "< U,u;Ú,ú;Ů,ů < Ü,ü < V,v < W,w < X,x < Y,y;Ý,ý < Z,z;Ż,ż;Ź,ź < Ž,ž " +
            "< 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9";
    /**
     * the rules of letter aliases (note: if changed, update unit tests)
     */
    private static final String ALIAS = "" +
            "A:Á,À,Â,Ą,Ä;" +
            "C:Ç,Ć;" +
            "E:É,È,Ê,Ë,Ę,Ě;" +
            "I:Í,Ï;" +
            "L:Ľ,Ł;" +
            "O:Ó,Ô,Ö,ö;" +
            "R:Ŕ;" +
            "S:Ś;" +
            "U:Ú,Ů,Ü;" +
            "Y:Ý;" +
            "Z:Ż,ż,Ź,ź";
    /**
     * the comparator instance
     */
    private final RuleBasedCollator comparator;
    /**
     * the mapping from letters to their alphabet representant
     */
    private final Map<String, String> letterToAlphabet;
    
    /**
     * Returns the singleton instance of this class.
     * 
     * @return the singleton instance
     */
    public synchronized static CzechAlphabet getInstance() {
        if (CzechAlphabet.instance == null) {
            CzechAlphabet.instance = new CzechAlphabet();
        }
        
        return CzechAlphabet.instance;
    }
    
    /**
     * Creates a new instance.
     */
    private CzechAlphabet() {
        // initialize
        
        try {
            this.comparator = new RuleBasedCollator(CzechAlphabet.RULES);
        } catch (final ParseException x) {
            throw new IllegalStateException("Chyba při zpracování českých pravidel.", x);
        }
        
        this.comparator.setDecomposition(Collator.NO_DECOMPOSITION);
        this.comparator.setStrength(Collator.IDENTICAL);
        this.letterToAlphabet = new HashMap<String, String>();
        
        // process aliases
        
        for (final String group : CzechAlphabet.ALIAS.split(";")) {
            // group = A:A,Á,À,Â,Ą,Ä
            
            final String part[] = group.split(":");
            
            if (part.length != 2) {
                throw new IllegalStateException("Nesprávná délka pole s aliasy: " + group);
            }
            
            // part[0] = A
            // part[1] = A,Á,À,Â,Ą,Ä
            
            for (final String alias : part[1].split(",")) {
                final String sub = alias.toUpperCase();
                final String main = part[0].toUpperCase();
                
                if (!CzechAlphabet.ALPHABET.contains(main)) {
                    throw new IllegalStateException(String.format("Abeceda musí obsahovat %s.", main));
                }
                
                if (CzechAlphabet.ALPHABET.contains(sub)) {
                    throw new IllegalStateException(String.format("Abeceda nesmí obsahovat %s.", sub));
                }
                
                if (sub.equals(main)) {
                    throw new IllegalStateException(String.format("Hlavní písmeno %s nesmí být shodné s aliasem.", main));
                }
                
                this.letterToAlphabet.put(sub, main);
            }
        }
    }
    
    // ==========
    // COMPARSION
    // ==========
    
    @Override
    public int compare(final String o1, final String o2) {
        return this.comparator.compare(
                CzechAlphabet.removeUnwantedSymbols(o1),
                CzechAlphabet.removeUnwantedSymbols(o2));
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the alphabet. No fallback letter is not included.
     * 
     * @return the alphabet without the fallback letter
     */
    public List<String> getAlphabet() {
        return CzechAlphabet.ALPHABET;
    }
    
    /**
     * Returns the first letter of this string which is also in the alphabet. If
     * the first letter is not in the alphabet, the fallback letter is returned.
     * 
     * @param string
     * the input string
     * @return the first letter of the input string (alphabet preferred)
     */
    public String getAlphabetFirstLetter(final String string) {
        // get the first letter of the input string
        
        final String letter = CzechAlphabet.getFirstLetter(string);
        
        if (letter == null) {
            return CzechAlphabet.FALLBACK_LETTER;
        }
        
        // find it in alphabet
        
        if (CzechAlphabet.ALPHABET.contains(letter)) {
            return letter;
        }
        
        // find it in aliases
        
        if (this.letterToAlphabet.containsKey(letter)) {
            return this.letterToAlphabet.get(letter);
        }
        
        return CzechAlphabet.FALLBACK_LETTER;
    }
    
    /**
     * Returns the previous alphabet letter. If the letter is not in the
     * alphabet or there is no previous letter, returns <code>null</code>.
     * 
     * @param letter
     * a letter
     * @return the previous letter from the alphabet or <code>null</code>
     */
    public String getAlphabetPreviousLetter(final String letter) {
        final int index = CzechAlphabet.ALPHABET.indexOf(letter);
        
        if ((index != -1) && (index > 0)) {
            return CzechAlphabet.ALPHABET.get(index - 1);
        }
        
        return null;
    }
    
    /**
     * Returns the next alphabet letter. If the letter is not in the alphabet or
     * there is no next letter, returns <code>null</code>.
     * 
     * @param letter
     * a letter
     * @return the next letter from the alphabet or <code>null</code>
     */
    public String getAlphabetNextLetter(final String letter) {
        final int index = CzechAlphabet.ALPHABET.indexOf(letter);
        
        if ((index != -1) && (index < CzechAlphabet.ALPHABET.size() - 1)) {
            return CzechAlphabet.ALPHABET.get(index + 1);
        }
        
        return null;
    }
    
    /**
     * Checks if the input string belongs to the given letter.
     * 
     * @param str
     * the input string
     * @param letter
     * a letter
     * @return <code>true</code> if the given input string belongs to the given
     * letter, <code>false</code> otherwise
     */
    public boolean belongsToLetter(final String str, final String letter) {
        return this.getAlphabetFirstLetter(str).equalsIgnoreCase(letter);
    }
    
    // =========
    // UTILITIES
    // =========
    
    /**
     * Returns the first letter of a string, preferably a capital (if any). The
     * letter returned is always in upper case. If no letter character is found,
     * returns <code>null</code>. This method respect the Czech 'CH' letter.
     * Examples:
     * 
     * <pre>
     * novak &gt; N
     * d'Alambert &gt; D
     * !!?!!?Some &gt; S
     * cha-cha &gt; CH
     * 123456 &gt; null
     * achCha &gt; CH
     * achCa &gt; C
     * </pre>
     * 
     * @param str
     * the input string
     * @return first letter (in upper case) or <code>null</code>
     */
    public static String getFirstLetter(final String str) {
        if (str == null) {
            return null;
        }
        
        // try to find the indexes
        
        int finalLetterIndex = -1;
        
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            
            if (Character.isLetter(c)) {
                if (finalLetterIndex == -1) {
                    finalLetterIndex = i;
                }
                
                if (Character.isUpperCase(c)) {
                    finalLetterIndex = i;
                    break;
                }
            }
        }
        
        if (finalLetterIndex == -1) {
            // no letter found at all
            
            return null;
        }
        
        final char letter = Character.toUpperCase(str.charAt(finalLetterIndex));
        
        // check possibility of CH
        
        if ((letter == 'C') && (finalLetterIndex < str.length() - 1)) {
            if (Character.toUpperCase(str.charAt(finalLetterIndex + 1)) == 'H') {
                return "CH";
            }
        }
        
        return String.valueOf(letter);
    }
    
    /**
     * Returns the batch name normalized to be used in sorting. It simply
     * returns the substring starting from the first capital letter (inclusive)
     * in upper case. If no such letter is found, returns just the input in
     * upper case.
     * 
     * @param str
     * original batch name
     * @return batch default batch name for sort (in upper case)
     */
    public static String getDefaultBatchForSort(final String str) {
        String result = str;
        
        // remove special symbols
        
        result = CzechAlphabet.removeUnwantedSymbols(result);
        
        // find first upper case letter
        
        int ucpos = -1;
        
        for (int i = 0; i < result.length(); i++) {
            if (Character.isUpperCase(result.charAt(i))) {
                // first upper case letter
                
                ucpos = i;
                break;
            }
        }
        
        if (ucpos != -1) {
            // take the substring starting from the upper case letter
            
            result = result.substring(ucpos);
        }
        
        // always normalize to upper case
        
        return result.trim().toUpperCase();
    }
    
    /**
     * Removes unwanted symbols from a string. Currently, only an apostrophe is
     * removed (´). This one is common in Irish and Scottish names.
     * 
     * @param str
     * input string
     * @return output string with unwanted characters removed
     */
    private static String removeUnwantedSymbols(final String str) {
        return str.replace("´", "");
    }
}
