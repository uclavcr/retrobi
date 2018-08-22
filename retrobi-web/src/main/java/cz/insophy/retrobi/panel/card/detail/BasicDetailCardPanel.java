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

package cz.insophy.retrobi.panel.card.detail;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.database.entity.type.UserRole;
import cz.insophy.retrobi.link.BookmarkableCardLink;
import cz.insophy.retrobi.model.CardPropertyListItem;
import cz.insophy.retrobi.model.RetrobiWebConfiguration;
import cz.insophy.retrobi.model.setup.CardViewMode;
import cz.insophy.retrobi.panel.card.CardPropertyListView;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * Basic card panel.
 * 
 * @author Vojtěch Hordějčuk
 */
public class BasicDetailCardPanel extends AbstractDetailCardPanel {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance.
     * 
     * @param id
     * component ID
     * @param card
     * card to be shown
     */
    public BasicDetailCardPanel(final String id, final IModel<Card> card) {
        super(id, CardViewMode.BASIC);
        
        // create components
        
        final CardPropertyListView listBasic = new CardPropertyListView("list.basic", BasicDetailCardPanel.createBasicItems(card.getObject()));
        final CardPropertyListView listAttribute = new CardPropertyListView("list.attribute", BasicDetailCardPanel.createAttributeItems(card.getObject()));
        final CardPropertyListView listSystem = new CardPropertyListView("list.system", BasicDetailCardPanel.createSystemItems(card.getObject()));
        final BookmarkablePageLink<?> permalink = new BookmarkableCardLink("link.card", card.getObject().getId());
        
        // place components
        
        this.add(permalink);
        this.add(listBasic);
        this.add(listAttribute);
        this.add(listSystem);
    }
    
    /**
     * Creates a list of basic properties.
     * 
     * @param card
     * a source card
     * @return a list of properties
     */
    private static List<CardPropertyListItem> createBasicItems(final Card card) {
        return Arrays.asList(
                new CardPropertyListItem("OCR", UserRole.GUEST, card.getOcr(), false),
                new CardPropertyListItem("Přepis OCR", UserRole.GUEST, card.getOcrFix(), false),
                new CardPropertyListItem("Segmentace: Záhlaví", UserRole.GUEST, card.getSegmentHead(), false),
                new CardPropertyListItem("Segmentace: Názvová část", UserRole.GUEST, card.getSegmentTitle(), false),
                new CardPropertyListItem("Segmentace: Bibliografická část", UserRole.GUEST, card.getSegmentBibliography(), false),
                new CardPropertyListItem("Segmentace: Anotační část", UserRole.GUEST, card.getSegmentAnnotation(), false),
                new CardPropertyListItem("Segmentace: Excerptor", UserRole.GUEST, card.getSegmentExcerpter(), false),
                new CardPropertyListItem("Poznámka", UserRole.GUEST, card.getNote(), SimpleStringUtils.isEmpty(card.getNote())),
                new CardPropertyListItem("WWW", UserRole.GUEST, card.getUrl(), SimpleStringUtils.isEmpty(card.getUrl())));
    }
    
    /**
     * Creates a list of system properties.
     * 
     * @param card
     * a source card
     * @return a list of properties
     */
    private static List<CardPropertyListItem> createSystemItems(final Card card) {
        return Arrays.asList(
                new CardPropertyListItem("ID", UserRole.GUEST, card.getId(), false),
                new CardPropertyListItem("Stav", UserRole.GUEST, card.getState().toString(), false),
                new CardPropertyListItem("Katalog", UserRole.GUEST, card.getCatalog().toString(), false),
                new CardPropertyListItem("Šuplík", UserRole.EDITOR, card.getDrawer(), false),
                new CardPropertyListItem("Skupina", UserRole.GUEST, card.getBatch(), false),
                new CardPropertyListItem("Skupina pro řazení", UserRole.EDITOR, card.getBatchForSort(), false),
                new CardPropertyListItem("Pořadí", UserRole.GUEST, card.getNumberInBatch(), false),
                new CardPropertyListItem("Přidáno", UserRole.EDITOR, card.getAdded().toString(), false),
                new CardPropertyListItem("Upraveno", UserRole.EDITOR, card.getUpdated().toString(), false),
                new CardPropertyListItem("Autor přepisu OCR (ID)", UserRole.ADMIN, card.getOcrFixUserId(), SimpleStringUtils.isEmpty(card.getOcrFixUserId())),
                new CardPropertyListItem("Soubory", UserRole.GUEST, card.getFiles().toString(), false));
    }
    
    /**
     * Creates a list of attribute properties.
     * 
     * @param card
     * a source card
     * @return a list of properties
     */
    private static List<CardPropertyListItem> createAttributeItems(final Card card) {
        final AttributeNode root = SimpleAttributeUtils.fromDocument(card, RetrobiWebConfiguration.getInstance().getAttributeRoot());
        final List<CardPropertyListItem> list = new LinkedList<CardPropertyListItem>();
        list.add(new CardPropertyListItem("Položkový rozpis", UserRole.GUEST, "(prázdný)", (root != null) && !root.isEmpty()));
        BasicDetailCardPanel.collectAtomicAttributes(null, root, list);
        return Collections.unmodifiableList(list);
    }
    
    /**
     * A recursive method for collecting atomic card attributes and converting
     * them to standard properties.
     * 
     * @param title
     * attribute title
     * @param root
     * root attribute node (or a sub-root)
     * @param list
     * target list of properties which will be changed
     */
    private static void collectAtomicAttributes(final String title, final AttributeNode root, final List<CardPropertyListItem> list) {
        if (root instanceof AtomicAttributeNode) {
            if (!SimpleStringUtils.isEmpty(((AtomicAttributeNode) root).getValue())) {
                final AtomicAttributeNode node = (AtomicAttributeNode) root;
                list.add(new CardPropertyListItem(title, node.getPrototype().getRole(), node.getValue(), node.isEmpty()));
            }
        } else if (root instanceof ComposedAttributeNode) {
            for (final AttributeNode node : ((ComposedAttributeNode) root).getChildren()) {
                final String newTitle = (title == null) ? node.getTitle() : title + " / " + node.getTitle();
                BasicDetailCardPanel.collectAtomicAttributes(newTitle, node, list);
            }
        }
    }
}
