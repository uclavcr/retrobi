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

package cz.insophy.retrobi.utils.library;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.type.CardIndexInfo;
import cz.insophy.retrobi.database.entity.type.UserRole;

/**
 * Default attribute prototype tree provider.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class DefaultAttributePrototyper {
    /**
     * author index name
     */
    private static final String INDEX_PERSON_AUTHOR = "osoba_autor";
    /**
     * referred index name
     */
    private static final String INDEX_PERSON_REFERRED = "osoba_odkaz";
    /**
     * person index name
     */
    private static final String INDEX_PERSON_ALL = "osoba";
    /**
     * source name index name
     */
    private static final String INDEX_SOURCE_NAME = "zdroj";
    /**
     * date published index name
     */
    private static final String INDEX_DATE_PUBLISHED = "rok";
    /**
     * description (or characteristics) index name
     */
    private static final String INDEX_DESCRIPTION = "charakteristika";
    
    /**
     * Creates the default index information.
     * 
     * @return the index naming information
     */
    public static Set<CardIndexInfo> info() {
        final Set<CardIndexInfo> set = new HashSet<CardIndexInfo>();
        
        set.add(new CardIndexInfo(DefaultAttributePrototyper.INDEX_PERSON_ALL, "Osoby (autorské + odkazované)", UserRole.GUEST, 1));
        set.add(new CardIndexInfo(DefaultAttributePrototyper.INDEX_PERSON_AUTHOR, "Osoby autorské", UserRole.GUEST, 2));
        set.add(new CardIndexInfo(DefaultAttributePrototyper.INDEX_PERSON_REFERRED, "Osoby odkazované", UserRole.GUEST, 3));
        set.add(new CardIndexInfo(DefaultAttributePrototyper.INDEX_SOURCE_NAME, "Název zdroje", UserRole.GUEST, 4));
        set.add(new CardIndexInfo(DefaultAttributePrototyper.INDEX_DATE_PUBLISHED, "Rok vydání", UserRole.GUEST, 5));
        set.add(new CardIndexInfo(DefaultAttributePrototyper.INDEX_DESCRIPTION, "Charakteristika", UserRole.GUEST, 6));
        
        return Collections.unmodifiableSet(set);
    }
    
    /**
     * Creates the default attribute tree.
     * 
     * @return the attribute tree root
     */
    public static AttributePrototype root() {
        final AttributePrototype root = new AttributePrototype("root", "Položkový rozpis", false,
                DefaultAttributePrototyper.head(),
                DefaultAttributePrototyper.title(),
                DefaultAttributePrototyper.bibliography(),
                DefaultAttributePrototyper.annotation(),
                DefaultAttributePrototyper.excerptor(),
                new AttributePrototype("poznamka", "Poznámka k lístku", true));
        
        return root;
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @return an attribute subtree root
     */
    private static AttributePrototype head() {
        return new AttributePrototype("zahlavi", "Záhlaví", false);
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @return an attribute subtree root
     */
    private static AttributePrototype title() {
        return new AttributePrototype("nazvova_cast", "Názvový údaj", false,
                DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("autor", "Autor", true, "AUT")),
                new AttributePrototype("nazev", "Název", false),
                new AttributePrototype("soubezny_nazev", "Souběžný název", false),
                new AttributePrototype("podnazev", "Podnázev", false),
                new AttributePrototype("incipit", "Incipit", false),
                new AttributePrototype("preklad", "Překlad z jazyka", true),
                DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("prekladatel", "Autor překladu", true, "TRL")),
                DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("ilustrator", "Autor ilustrace", true, "ILL")),
                DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("jina_role", "Jiná personální role", true, "")),
                DefaultAttributePrototyper.indexDescription(new AttributePrototype("charakteristika", "Charakteristika", true)),
                new AttributePrototype("poznamka", "Poznámka k názvu", true),
                new AttributePrototype("cast", "Část", true,
                        DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("author", "Autor", true, "AUT")),
                        new AttributePrototype("nazev", "Název", false),
                        new AttributePrototype("soubezny_nazev", "Souběžný název", false),
                        new AttributePrototype("podnazev", "Podnázev", false),
                        new AttributePrototype("incipit", "Incipit", false),
                        new AttributePrototype("preklad", "Překlad z jazyka", true),
                        DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("prekladatel", "Autor překladu", true, "TRL")),
                        DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("ilustrator", "Autor ilustrace", true, "ILL")),
                        DefaultAttributePrototyper.indexAuthor(DefaultAttributePrototyper.person("jina_role", "Jiná personální role", true, "")),
                        DefaultAttributePrototyper.indexDescription(new AttributePrototype("charakteristika", "Charakteristika", true)),
                        new AttributePrototype("poznamka", "Poznámka k části", true)));
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @return an attribute subtree root
     */
    private static AttributePrototype bibliography() {
        return new AttributePrototype("bibliograficka_cast", "Bibliografický údaj", false,
                new AttributePrototype("zdroj", "Zdroj", false,
                        DefaultAttributePrototyper.indexSourceName(new AttributePrototype("nazev", "Název zdroje", false)),
                        new AttributePrototype("cislo_cnb", "Číslo ČNB", false),
                        new AttributePrototype("rada_zdroje", "Označení a název řady zdroje", false),
                        new AttributePrototype("url", "URL adresa", true),
                        new AttributePrototype("rocnik", "Ročník", true,
                                new AttributePrototype("rocnik", "Ročník", false),
                                new AttributePrototype("oznaceni", "Chronologické označení ročníku", false),
                                new AttributePrototype("svazek", "Svazek", false),
                                new AttributePrototype("cislo", "Číslo", true,
                                        new AttributePrototype("cislo", "Číslo", false),
                                        new AttributePrototype("datum_vydani", "Datum vydání", false),
                                        new AttributePrototype("priloha", "Příloha", false),
                                        new AttributePrototype("cislo_prilohy", "Číslo přílohy", false),
                                        new AttributePrototype("strana", "Strana", false),
                                        new AttributePrototype("oznaceni_vydani", "Označení vydání", false))),
                        new AttributePrototype("poznamka", "Poznámka", true),
                        DefaultAttributePrototyper.indexYearPublished(new AttributePrototype("rok", "Rok", false))));
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @return an attribute subtree root
     */
    private static AttributePrototype annotation() {
        return new AttributePrototype("anotacni_cast", "Anotační část", false,
                new AttributePrototype("jazyk", "Jazyk", true),
                new AttributePrototype("anotace", "Anotace", false),
                DefaultAttributePrototyper.indexReferred(DefaultAttributePrototyper.person("odkazovana_osoba", "Odkazovaná osoba", true, "")),
                DefaultAttributePrototyper.work("odkazovane_dilo", "Odkazované dílo", true),
                new AttributePrototype("odkazovana_korporace", "Odkazovaná korporace", true,
                        new AttributePrototype("jmeno", "Jméno", false),
                        new AttributePrototype("poznamka", "Poznámka", true)),
                new AttributePrototype("odkazovana_akce", "Odkazovaná akce", true,
                        new AttributePrototype("nazev", "Název", false),
                        new AttributePrototype("druh", "Druh", true),
                        DefaultAttributePrototyper.indexReferred(DefaultAttributePrototyper.person("osoba", "Osoba", true, "")),
                        new AttributePrototype("korporace", "Korporace", true,
                                new AttributePrototype("jmeno", "Jméno", false),
                                new AttributePrototype("poznamka", "Poznámka", true)),
                        new AttributePrototype("misto", "Místo", true),
                        new AttributePrototype("zeme", "Země", true),
                        new AttributePrototype("datum_zahajeni", "Datum zahájení", false),
                        new AttributePrototype("datum_ukonceni", "Datum ukončení", false),
                        new AttributePrototype("poznamka", "Poznámka", true)
                ),
                new AttributePrototype("klicove_slovo", "Klíčové slovo", true,
                        new AttributePrototype("nazev", "Název", false),
                        new AttributePrototype("poznamka", "Poznámka", true)),
                new AttributePrototype("chronologicky_udaj", "Chronologický údaj", true,
                        new AttributePrototype("nazev", "Název", false),
                        new AttributePrototype("poznamka", "Poznámka", true)),
                new AttributePrototype("geograficky_udaj", "Geografický údaj", true,
                        new AttributePrototype("nazev", "Název", false),
                        new AttributePrototype("poznamka", "Poznámka", true)),
                new AttributePrototype("mdt", "MDT", true),
                new AttributePrototype("konspekt", "Konspekt", true));
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @return an attribute subtree root
     */
    private static AttributePrototype excerptor() {
        return new AttributePrototype("excerptor", "Excerptor", false);
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @param key
     * key
     * @param title
     * title
     * @param repeat
     * repeatable
     * @return an attribute subtree root
     */
    private static AttributePrototype work(final String key, final String title, final boolean repeat) {
        return new AttributePrototype(key, title, repeat,
                new AttributePrototype("cislo_cnb", "Číslo ČNB", false),
                DefaultAttributePrototyper.indexReferred(DefaultAttributePrototyper.person("autor", "Autor", true, "AUT")),
                new AttributePrototype("nazev", "Název", false),
                DefaultAttributePrototyper.indexReferred(DefaultAttributePrototyper.person("editor", "Editor", true, "EDT")),
                new AttributePrototype("jazyk_prekladu", "Jazyk překladu", false),
                DefaultAttributePrototyper.indexReferred(DefaultAttributePrototyper.person("prekladatel", "Překladatel", true, "TRL")),
                DefaultAttributePrototyper.indexReferred(DefaultAttributePrototyper.person("jina_role", "Jiná role", true, "")),
                new AttributePrototype("udaje", "Nakladatelské údaje", true,
                        new AttributePrototype("misto", "Místo vydání", true),
                        new AttributePrototype("nakladatel", "Nakladatel", true)),
                new AttributePrototype("rok", "Rok vydání", true),
                new AttributePrototype("edice", "Edice", true));
    }
    
    /**
     * Creates an attribute subtree.
     * 
     * @param key
     * key
     * @param title
     * title
     * @param repeat
     * repeatable
     * @param defaultCode
     * default person role code
     * @return an attribute subtree root
     */
    private static AttributePrototype person(final String key, final String title, final boolean repeat, final String defaultCode) {
        return new AttributePrototype(key, title, repeat,
                new AttributePrototype("jmeno", "Jméno", false),
                new AttributePrototype("sifra", "Šifra/Podpis", false),
                new AttributePrototype("kod", "Kód role", false, defaultCode),
                new AttributePrototype("poznamka", "Poznámka", true),
                new AttributePrototype("id", "ID", false));
    }
    
    /**
     * Adds the attribute to the author index and person index.
     * 
     * @param attribute
     * target attribute
     * @return the same target attribute
     */
    private static AttributePrototype indexAuthor(final AttributePrototype attribute) {
        return DefaultAttributePrototyper.index(attribute, DefaultAttributePrototyper.INDEX_PERSON_AUTHOR, DefaultAttributePrototyper.INDEX_PERSON_ALL);
    }
    
    /**
     * Adds the attribute to the referred person index and person index.
     * 
     * @param attribute
     * target attribute
     * @return the same target attribute
     */
    private static AttributePrototype indexReferred(final AttributePrototype attribute) {
        return DefaultAttributePrototyper.index(attribute, DefaultAttributePrototyper.INDEX_PERSON_REFERRED, DefaultAttributePrototyper.INDEX_PERSON_ALL);
    }
    
    /**
     * Adds the attribute to the source name index.
     * 
     * @param attribute
     * target attribute
     * @return the same target attribute
     */
    private static AttributePrototype indexSourceName(final AttributePrototype attribute) {
        return DefaultAttributePrototyper.index(attribute, DefaultAttributePrototyper.INDEX_SOURCE_NAME);
    }
    
    /**
     * Adds the attribute to the date published index.
     * 
     * @param attribute
     * target attribute
     * @return the same target attribute
     */
    private static AttributePrototype indexYearPublished(final AttributePrototype attribute) {
        return DefaultAttributePrototyper.index(attribute, DefaultAttributePrototyper.INDEX_DATE_PUBLISHED);
    }
    
    /**
     * Adds the attribute to the description index.
     * 
     * @param attribute
     * target attribute
     * @return the same target attribute
     */
    private static AttributePrototype indexDescription(final AttributePrototype attribute) {
        return DefaultAttributePrototyper.index(attribute, DefaultAttributePrototyper.INDEX_DESCRIPTION);
    }
    
    /**
     * Adds indexes to the given attribute and returns the same attribute back.
     * This is a convenience method.
     * 
     * @param attribute
     * target attribute
     * @param indexes
     * indexes to add
     * @return the same target attribute
     */
    private static AttributePrototype index(final AttributePrototype attribute, final String... indexes) {
        for (final String index : indexes) {
            attribute.addIndex(index);
        }
        
        return attribute;
    }
    
    /**
     * Cannot make instances of this class.
     */
    private DefaultAttributePrototyper() {
        throw new UnsupportedOperationException();
    }
}
