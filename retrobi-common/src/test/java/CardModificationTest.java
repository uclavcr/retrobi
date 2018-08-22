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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.insophy.retrobi.database.entity.Card;
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.database.entity.type.CardState;
import cz.insophy.retrobi.database.entity.type.Catalog;
import cz.insophy.retrobi.exception.AlreadyModifiedException;
import cz.insophy.retrobi.longtask.AddAttributeModification;
import cz.insophy.retrobi.longtask.RemoveAttributeModification;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class CardModificationTest {
    /**
     * test
     * 
     * @throws AlreadyModifiedException
     * -
     */
    @Test
    public final void testAdd() throws AlreadyModifiedException {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode phone = (AtomicAttributeNode) person.getChildren().get(2);
        final AtomicAttributeNode street = (AtomicAttributeNode) address.getChildren().get(0);
        
        Assert.assertEquals(9, root.getSize());
        
        final Card testCard = CardModificationTest.getTestCard(root);
        
        // add 3 streets
        
        Assert.assertTrue(new AddAttributeModification(proto, street.getPrototype(), "street1").modify(testCard));
        Assert.assertTrue(new AddAttributeModification(proto, street.getPrototype(), "street2").modify(testCard));
        Assert.assertTrue(new AddAttributeModification(proto, street.getPrototype(), "street3").modify(testCard));
        final AttributeNode rootAfter1 = SimpleAttributeUtils.fromDocument(testCard, proto);
        
        Assert.assertEquals(15, rootAfter1.getSize());
        final List<AttributeNode> streets = rootAfter1.find(street.getPrototype());
        Assert.assertEquals(3, streets.size());
        Assert.assertEquals("street1", ((AtomicAttributeNode) streets.get(0)).getValue());
        Assert.assertEquals("street2", ((AtomicAttributeNode) streets.get(1)).getValue());
        Assert.assertEquals("street3", ((AtomicAttributeNode) streets.get(2)).getValue());
        
        // add 2 phones
        
        Assert.assertTrue(new AddAttributeModification(proto, phone.getPrototype(), "phone1").modify(testCard));
        Assert.assertTrue(new AddAttributeModification(proto, phone.getPrototype(), "phone2").modify(testCard));
        final AttributeNode rootAfter2 = SimpleAttributeUtils.fromDocument(testCard, proto);
        
        Assert.assertEquals(16, rootAfter2.getSize());
        final List<AttributeNode> phones = rootAfter2.find(phone.getPrototype());
        Assert.assertEquals(2, phones.size());
        Assert.assertEquals("phone1", ((AtomicAttributeNode) phones.get(0)).getValue());
        Assert.assertEquals("phone2", ((AtomicAttributeNode) phones.get(1)).getValue());
        
        // add address (does nothing, address is a composed node)
        
        Assert.assertFalse(new AddAttributeModification(proto, address.getPrototype(), "address1").modify(testCard));
        final AttributeNode rootAfter3 = SimpleAttributeUtils.fromDocument(testCard, proto);
        Assert.assertEquals(16, rootAfter3.getSize());
        
        // add a value already present
        
        try {
            new AddAttributeModification(proto, street.getPrototype(), "street1").modify(testCard);
            Assert.fail();
        } catch (final AlreadyModifiedException x) {
            // OK
        }
        
        try {
            new AddAttributeModification(proto, phone.getPrototype(), "phone2").modify(testCard);
            Assert.fail();
        } catch (final AlreadyModifiedException x) {
            // OK
        }
    }
    
    /**
     * test
     * 
     * @throws AlreadyModifiedException
     * -
     */
    @Test
    public final void testRemove() throws AlreadyModifiedException {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode phone = (AtomicAttributeNode) person.getChildren().get(2);
        final AtomicAttributeNode street = (AtomicAttributeNode) address.getChildren().get(0);
        
        Assert.assertEquals(9, root.getSize());
        
        final Card testCard = CardModificationTest.getTestCard(root);
        
        // first, add some testing values
        
        Assert.assertTrue(new AddAttributeModification(proto, street.getPrototype(), "street1").modify(testCard));
        Assert.assertTrue(new AddAttributeModification(proto, street.getPrototype(), "street2").modify(testCard));
        Assert.assertTrue(new AddAttributeModification(proto, phone.getPrototype(), "phone1").modify(testCard));
        Assert.assertTrue(new AddAttributeModification(proto, phone.getPrototype(), "phone2").modify(testCard));
        
        final AttributeNode rootAfter1 = SimpleAttributeUtils.fromDocument(testCard, proto);
        
        Assert.assertEquals(13, rootAfter1.getSize());
        
        // remove all streets
        
        final List<AttributeNode> streetNodesAfter1 = rootAfter1.find(street.getPrototype());
        Assert.assertEquals(2, streetNodesAfter1.size());
        Assert.assertFalse(streetNodesAfter1.get(0).isEmpty());
        Assert.assertFalse(streetNodesAfter1.get(1).isEmpty());
        
        Assert.assertTrue(new RemoveAttributeModification(proto, street.getPrototype(), null).modify(testCard));
        final AttributeNode rootAfter2 = SimpleAttributeUtils.fromDocument(testCard, proto);
        Assert.assertEquals(10, rootAfter2.getSize());
        
        final List<AttributeNode> streetNodesAfter2 = rootAfter2.find(street.getPrototype());
        Assert.assertEquals(1, streetNodesAfter2.size());
        Assert.assertTrue(streetNodesAfter2.get(0).isEmpty());
        
        // remove one phone
        
        Assert.assertTrue(new RemoveAttributeModification(proto, phone.getPrototype(), "phone2").modify(testCard));
        final AttributeNode rootAfter3 = SimpleAttributeUtils.fromDocument(testCard, proto);
        Assert.assertEquals(9, rootAfter3.getSize());
        
        // remove non-existing value
        
        try {
            new RemoveAttributeModification(proto, phone.getPrototype(), "phoneNOT").modify(testCard);
            Assert.fail();
        } catch (final AlreadyModifiedException x) {
            // OK
        }
        
        try {
            new RemoveAttributeModification(proto, street.getPrototype(), "streetNOT").modify(testCard);
            Assert.fail();
        } catch (final AlreadyModifiedException x) {
            // OK
        }
    }
    
    /**
     * test
     * 
     * @throws AlreadyModifiedException
     * -
     */
    @Test
    public final void testRemoveValue() throws AlreadyModifiedException {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final AtomicAttributeNode note = (AtomicAttributeNode) root.getChildren().get(0);
        note.setValue("note");
        
        final Card card1 = CardModificationTest.getTestCard(root);
        final Card card2 = CardModificationTest.getTestCard(root);
        final Card card3 = CardModificationTest.getTestCard(root);
        
        Assert.assertEquals(9, SimpleAttributeUtils.fromDocument(card1, proto).getSize());
        Assert.assertEquals(9, SimpleAttributeUtils.fromDocument(card2, proto).getSize());
        Assert.assertEquals(9, SimpleAttributeUtils.fromDocument(card3, proto).getSize());
        
        Assert.assertTrue(new RemoveAttributeModification(proto, note.getPrototype(), null).modify(card1));
        Assert.assertEquals(9, SimpleAttributeUtils.fromDocument(card1, proto).getSize());
        
        try {
            Assert.assertFalse(new RemoveAttributeModification(proto, note.getPrototype(), "NOTHING").modify(card2));
            Assert.assertEquals(9, SimpleAttributeUtils.fromDocument(card2, proto).getSize());
            Assert.fail();
        } catch (final AlreadyModifiedException x) {
            // OK
        }
        
        Assert.assertTrue(new RemoveAttributeModification(proto, note.getPrototype(), "note").modify(card3));
        Assert.assertEquals(9, SimpleAttributeUtils.fromDocument(card3, proto).getSize());
    }
    
    /**
     * test
     * 
     * @throws AlreadyModifiedException
     * -
     */
    @Test
    public final void testCleanup1() throws AlreadyModifiedException {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final AtomicAttributeNode note = (AtomicAttributeNode) root.getChildren().get(0);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        
        // does not remove anything
        
        Assert.assertEquals(9, root.getSize());
        SimpleAttributeUtils.removeEmptyChildren(root);
        Assert.assertEquals(9, root.getSize());
        
        // repeat the note twice
        
        note.createSibling();
        Assert.assertEquals(10, root.getSize());
        note.createSibling();
        Assert.assertEquals(11, root.getSize());
        SimpleAttributeUtils.removeEmptyChildren(root);
        Assert.assertEquals(9, root.getSize());
        
        // repeat the note and address twice
        
        final AtomicAttributeNode note2 = (AtomicAttributeNode) root.find(note.getPrototype()).get(0);
        final ComposedAttributeNode address2 = (ComposedAttributeNode) root.find(address.getPrototype()).get(0);
        note2.createSibling();
        Assert.assertEquals(10, root.getSize());
        note2.createSibling();
        Assert.assertEquals(11, root.getSize());
        address2.createSibling();
        Assert.assertEquals(14, root.getSize());
        address2.createSibling();
        Assert.assertEquals(17, root.getSize());
        SimpleAttributeUtils.removeEmptyChildren(root);
        Assert.assertEquals(9, root.getSize());
        
        // repeat the note and address twice
        // put value into one NOTE and two STREET nodes
        // this will prevent them from being removed
        
        final AtomicAttributeNode note3 = (AtomicAttributeNode) root.find(note.getPrototype()).get(0);
        note3.setValue("value:NOTE");
        note3.createSibling();
        Assert.assertEquals(10, root.getSize());
        note3.createSibling();
        Assert.assertEquals(11, root.getSize());
        
        final ComposedAttributeNode address31 = (ComposedAttributeNode) root.find(address.getPrototype()).get(0);
        final AtomicAttributeNode street31 = (AtomicAttributeNode) address31.getChildren().get(0);
        street31.setValue("value:STREET1");
        address31.createSibling();
        Assert.assertEquals(14, root.getSize());
        final ComposedAttributeNode address32 = (ComposedAttributeNode) root.find(address.getPrototype()).get(1);
        final AtomicAttributeNode street32 = (AtomicAttributeNode) address32.getChildren().get(0);
        street32.setValue("value:STREET2");
        address31.createSibling();
        Assert.assertEquals(17, root.getSize());
        
        SimpleAttributeUtils.removeEmptyChildren(root);
        Assert.assertEquals(12, root.getSize());
    }
    
    /**
     * test
     */
    @Test
    public final void testHigherLowerState() {
        for (final CardState cardState : CardState.values()) {
            final Card card = new Card();
            card.setState(cardState);
            
            for (final CardState otherState : CardState.values()) {
                final boolean islower;
                final boolean ishigher;
                
                if (otherState.ordinal() < card.getState().ordinal()) {
                    islower = false;
                    ishigher = true;
                } else if (otherState.ordinal() > card.getState().ordinal()) {
                    islower = true;
                    ishigher = false;
                } else {
                    islower = false;
                    ishigher = false;
                }
                
                Assert.assertEquals(islower, card.hasLowerState(otherState));
                Assert.assertEquals(islower, card.getState().isLowerThan(otherState));
                Assert.assertEquals(ishigher, card.getState().isHigherThan(otherState));
            }
        }
    }
    
    /**
     * -
     * 
     * @param root
     * -
     * @return -
     */
    public static Card getTestCard(final AttributeNode root) {
        final Card card = new Card();
        card.setBatch("test");
        card.setCatalog(Catalog.A);
        card.setNumberInBatch(1);
        SimpleAttributeUtils.toDocument(card, root);
        return card;
    }
}
