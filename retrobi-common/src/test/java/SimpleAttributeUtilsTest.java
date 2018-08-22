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
import cz.insophy.retrobi.database.entity.attribute.AtomicAttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributeNode;
import cz.insophy.retrobi.database.entity.attribute.AttributePrototype;
import cz.insophy.retrobi.database.entity.attribute.ComposedAttributeNode;
import cz.insophy.retrobi.utils.Tuple;
import cz.insophy.retrobi.utils.library.DefaultAttributePrototyper;
import cz.insophy.retrobi.utils.library.SimpleAttributeUtils;
import cz.insophy.retrobi.utils.library.SimpleSearchUtils;
import cz.insophy.retrobi.utils.library.SimpleStringUtils;

/**
 * @author Vojtěch Hordějčuk
 */
public class SimpleAttributeUtilsTest {
    /**
     * test
     */
    @Test
    public final void testDefaultPrototype() {
        final AttributePrototype root = DefaultAttributePrototyper.root();
        final AttributeNode tree = SimpleAttributeUtils.createFromPrototype(root);
        Assert.assertEquals(160, tree.getSize());
    }
    
    /**
     * test
     */
    @Test
    public final void testFromPrototype() {
        final AttributePrototype pAtomic1 = new AttributePrototype("c1", "c1", false);
        final AttributePrototype pAtomic2 = new AttributePrototype("c2", "c2", false);
        final AttributePrototype pAtomic3 = new AttributePrototype("c3", "c3", false);
        final AttributePrototype pComposed1 = new AttributePrototype("a", "a", false, pAtomic1, pAtomic2, pAtomic3);
        final AttributePrototype pComposed2 = new AttributePrototype("a", "a", false, pComposed1);
        
        final AttributeNode nAtomic1 = SimpleAttributeUtils.createFromPrototype(pAtomic1);
        final AttributeNode nAtomic2 = SimpleAttributeUtils.createFromPrototype(pAtomic2);
        final AttributeNode nAtomic3 = SimpleAttributeUtils.createFromPrototype(pAtomic3);
        final AttributeNode nComposed1 = SimpleAttributeUtils.createFromPrototype(pComposed1);
        final AttributeNode nComposed2 = SimpleAttributeUtils.createFromPrototype(pComposed2);
        
        Assert.assertTrue(nAtomic1 instanceof AtomicAttributeNode);
        Assert.assertTrue(nAtomic2 instanceof AtomicAttributeNode);
        Assert.assertTrue(nAtomic3 instanceof AtomicAttributeNode);
        Assert.assertTrue(nComposed1 instanceof ComposedAttributeNode);
        Assert.assertTrue(nComposed2 instanceof ComposedAttributeNode);
        
        Assert.assertEquals(1, nAtomic1.getSize());
        Assert.assertEquals(1, nAtomic2.getSize());
        Assert.assertEquals(1, nAtomic3.getSize());
        Assert.assertEquals(4, nComposed1.getSize());
        Assert.assertEquals(5, nComposed2.getSize());
        
        final AttributeNode nAtomic1P1 = SimpleAttributeUtils.createFromPrototype(pAtomic1, nComposed1);
        final AttributeNode nAtomic1P2 = SimpleAttributeUtils.createFromPrototype(pAtomic1, nComposed2);
        
        Assert.assertTrue(nAtomic1P1 instanceof AtomicAttributeNode);
        Assert.assertTrue(nAtomic1P2 instanceof AtomicAttributeNode);
        Assert.assertEquals(1, nAtomic1P1.getSize());
        Assert.assertEquals(1, nAtomic1P2.getSize());
        Assert.assertSame(nComposed1, nAtomic1P1.getParent());
        Assert.assertSame(nComposed2, nAtomic1P2.getParent());
    }
    
    /**
     * test
     */
    @Test
    public final void testParent() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        final AtomicAttributeNode note = (AtomicAttributeNode) person.getChildren().get(1);
        final AtomicAttributeNode phone = (AtomicAttributeNode) person.getChildren().get(2);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode street = (AtomicAttributeNode) address.getChildren().get(0);
        final AtomicAttributeNode city = (AtomicAttributeNode) address.getChildren().get(1);
        
        final Map<AttributePrototype, AttributePrototype> parent = SimpleAttributeUtils.findParents(proto);
        
        Assert.assertEquals(root.getPrototype(), parent.get(person.getPrototype()));
        Assert.assertEquals(person.getPrototype(), parent.get(name.getPrototype()));
        Assert.assertEquals(person.getPrototype(), parent.get(note.getPrototype()));
        Assert.assertEquals(person.getPrototype(), parent.get(phone.getPrototype()));
        Assert.assertEquals(person.getPrototype(), parent.get(address.getPrototype()));
        Assert.assertEquals(address.getPrototype(), parent.get(street.getPrototype()));
        Assert.assertEquals(address.getPrototype(), parent.get(city.getPrototype()));
        
        Assert.assertNull(root.getParent());
        Assert.assertEquals(root, person.getParent());
        Assert.assertEquals(person, name.getParent());
        Assert.assertEquals(person, note.getParent());
        Assert.assertEquals(person, phone.getParent());
        Assert.assertEquals(person, address.getParent());
        Assert.assertEquals(address, street.getParent());
        Assert.assertEquals(address, city.getParent());
    }
    
    /**
     * test
     */
    @Test
    public final void testPath() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode street = (AtomicAttributeNode) address.getChildren().get(0);
        final AtomicAttributeNode city = (AtomicAttributeNode) address.getChildren().get(1);
        
        final Map<AttributePrototype, AttributePrototype> parent = SimpleAttributeUtils.findParents(proto);
        
        Assert.assertEquals(Collections.emptyList(), SimpleAttributeUtils.getPath(root.getPrototype(), parent));
        Assert.assertEquals(Arrays.asList("person"), SimpleAttributeUtils.getPath(person.getPrototype(), parent));
        Assert.assertEquals(Arrays.asList("person", "name"), SimpleAttributeUtils.getPath(name.getPrototype(), parent));
        Assert.assertEquals(Arrays.asList("person", "address"), SimpleAttributeUtils.getPath(address.getPrototype(), parent));
        Assert.assertEquals(Arrays.asList("person", "address", "street"), SimpleAttributeUtils.getPath(street.getPrototype(), parent));
        Assert.assertEquals(Arrays.asList("person", "address", "city"), SimpleAttributeUtils.getPath(city.getPrototype(), parent));
    }
    
    /**
     * test
     */
    @Test
    public final void testFind() {
        // simple
        
        final AttributePrototype sp1 = new AttributePrototype("key", "title", false);
        final AttributePrototype sp2 = new AttributePrototype("key", "title", false);
        
        final AttributeNode node = new AtomicAttributeNode(null, sp1);
        Assert.assertEquals(Collections.singletonList(node), node.find(sp1));
        Assert.assertEquals(Collections.emptyList(), node.find(sp2));
        
        // complex
        
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        
        Assert.assertEquals(1, root.find(person.getPrototype()).size());
        Assert.assertEquals(1, root.find(address.getPrototype()).size());
        Assert.assertEquals(1, root.find(name.getPrototype()).size());
        
        // most complex
        
        Assert.assertNotNull(address.createSibling());
        Assert.assertNotNull(address.createSibling());
        Assert.assertNotNull(address.createSibling());
        Assert.assertNotNull(address.createSibling());
        
        Assert.assertEquals(5, root.find(address.getPrototype()).size());
        
        Assert.assertNotNull(person.createSibling());
        Assert.assertNotNull(person.createSibling());
        Assert.assertNotNull(person.createSibling());
        
        Assert.assertEquals(4, root.find(person.getPrototype()).size());
        Assert.assertEquals(8, root.find(address.getPrototype()).size());
    }
    
    /**
     * test
     */
    @Test
    public final void testNormalize() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        
        final Object name2 = name.normalize();
        final Object address2 = address.normalize();
        
        Assert.assertTrue(name2 instanceof String);
        Assert.assertTrue(address2 instanceof Map);
        Assert.assertEquals(2, ((Map<?, ?>) address2).size());
        Assert.assertTrue(((Map<?, ?>) address2).containsKey("street"));
        Assert.assertTrue(((Map<?, ?>) address2).containsKey("city"));
        Assert.assertTrue(((Map<?, ?>) address2).get("street") instanceof List);
        Assert.assertTrue(((Map<?, ?>) address2).get("city") instanceof List);
    }
    
    /**
     * test
     */
    @Test
    public final void testDenormalizeSimple() {
        final AttributePrototype proto = new AttributePrototype("a", "A", true,
                new AttributePrototype("aa", "AA", true,
                        new AttributePrototype("aaa", "AAA", true),
                        new AttributePrototype("aab", "AAB", true),
                        new AttributePrototype("aac", "AAC", true),
                        new AttributePrototype("aad", "AAD", true),
                        new AttributePrototype("aae", "AAE", true)),
                new AttributePrototype("ab", "AB", true),
                new AttributePrototype("ac", "AC", true),
                new AttributePrototype("ad", "AD", true));
        
        final AttributeNode root = SimpleAttributeUtils.createFromPrototype(proto);
        Assert.assertEquals(10, root.getSize());
        
        final AttributeNode root2 = SimpleAttributeUtils.denormalize(root.normalize(), proto);
        Assert.assertEquals(root.getSize(), root2.getSize());
    }
    
    /**
     * test
     */
    @Test
    public final void testDenormalizeDocument() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        final AtomicAttributeNode street = (AtomicAttributeNode) address.getChildren().get(0);
        
        final BasicDocument document = new BasicDocument();
        SimpleAttributeUtils.toDocument(document, root);
        Assert.assertEquals(112, SimpleStringUtils.toJson(document, false).length());
        
        final ComposedAttributeNode root2 = (ComposedAttributeNode) SimpleAttributeUtils.fromDocument(document, proto);
        final ComposedAttributeNode person2 = (ComposedAttributeNode) root2.getChildren().get(1);
        final AtomicAttributeNode name2 = (AtomicAttributeNode) person2.getChildren().get(0);
        final ComposedAttributeNode address2 = (ComposedAttributeNode) person2.getChildren().get(3);
        final AtomicAttributeNode street2 = (AtomicAttributeNode) address2.getChildren().get(0);
        
        Assert.assertEquals(root.getSize(), root2.getSize());
        Assert.assertEquals(person.getSize(), person2.getSize());
        Assert.assertEquals(name.getSize(), name2.getSize());
        Assert.assertEquals(address.getSize(), address2.getSize());
        Assert.assertEquals(street.getSize(), street2.getSize());
    }
    
    /**
     * test
     */
    @Test
    public final void testDenormalizeCloned() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        
        // transformation to JSON and back
        
        final BasicDocument documentBefore = new BasicDocument();
        SimpleAttributeUtils.toDocument(documentBefore, root);
        final String json = SimpleStringUtils.toJson(documentBefore);
        Assert.assertEquals(112, json.length());
        final BasicDocument documentAfter = SimpleStringUtils.fromJson(json, BasicDocument.class);
        
        // checking
        
        final ComposedAttributeNode root2 = (ComposedAttributeNode) SimpleAttributeUtils.fromDocument(documentAfter, proto);
        final ComposedAttributeNode person2 = (ComposedAttributeNode) root2.getChildren().get(1);
        
        Assert.assertEquals(root.getSize(), root2.getSize());
        Assert.assertEquals(person.getSize(), person2.getSize());
    }
    
    /**
     * test
     */
    @Test
    public final void testCollectAtomic() {
        final AttributePrototype proto = new AttributePrototype("key", "title", true);
        final AtomicAttributeNode root = (AtomicAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        
        Assert.assertEquals(1, root.getSize());
        
        root.setValue("value");
        
        final List<AttributeNode> nodes = SimpleAttributeUtils.collectAtomic(root.normalize(), null, root.getPrototype());
        
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(proto, nodes.get(0).getPrototype());
        Assert.assertEquals("value", ((AtomicAttributeNode) nodes.get(0)).getValue());
    }
    
    /**
     * test
     */
    @Test
    public final void testCollectComposed() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        
        Assert.assertEquals(3, address.getSize());
        
        // result: {address -> [{street -> [], city -> []}]}
        // value: [{street -> [], city -> []}]
        
        final List<AttributeNode> nodes = SimpleAttributeUtils.collectComposed(address.normalize(), null, address.getPrototype());
        
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals(3, nodes.get(0).getSize());
        Assert.assertSame(address.getPrototype(), nodes.get(0).getPrototype());
    }
    
    /**
     * test
     */
    @Test
    public final void testRemove() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        
        Assert.assertEquals(9, root.getSize());
        Assert.assertEquals(7, person.getSize());
        
        // now: 2 names
        
        Assert.assertTrue(root.removeFromChildren(name));
        Assert.assertTrue(root.removeFromChildren(address));
        
        // check the NOP behavior
        
        Assert.assertFalse(address.removeFromChildren(root));
        Assert.assertFalse(address.removeFromChildren(person));
        Assert.assertFalse(person.removeFromChildren(root));
        Assert.assertFalse(name.removeFromChildren(root));
        Assert.assertFalse(name.removeFromChildren(person));
    }
    
    /**
     * test
     */
    @Test
    public final void testClone() {
        final AttributePrototype pAtomic1 = new AttributePrototype("c1", "c1", false);
        final AttributePrototype pAtomic2 = new AttributePrototype("c2", "c2", false);
        final AttributePrototype pAtomic3 = new AttributePrototype("c3", "c3", false);
        final AttributePrototype pComposed1 = new AttributePrototype("a", "a", false, pAtomic1, pAtomic2, pAtomic3);
        final ComposedAttributeNode parent = new ComposedAttributeNode(null, pComposed1);
        
        // atomic
        
        final AtomicAttributeNode atomic = new AtomicAttributeNode(parent, pAtomic1);
        final AtomicAttributeNode atomicC = (AtomicAttributeNode) atomic.createClone(atomic.getParent());
        
        Assert.assertEquals(1, atomic.getSize());
        Assert.assertEquals(1, atomicC.getSize());
        Assert.assertEquals(atomic.getPrototype(), atomicC.getPrototype());
        Assert.assertEquals(atomic.getParent(), atomicC.getParent());
        Assert.assertEquals(parent, atomic.getParent());
        Assert.assertEquals(parent, atomicC.getParent());
        
        // composed
        
        final ComposedAttributeNode composed = new ComposedAttributeNode(parent, pComposed1);
        composed.addChild(new AtomicAttributeNode(composed, pComposed1));
        composed.addChild(new AtomicAttributeNode(composed, pComposed1));
        composed.addChild(new AtomicAttributeNode(composed, pComposed1));
        final ComposedAttributeNode composedC = (ComposedAttributeNode) composed.createClone(composed.getParent());
        
        Assert.assertEquals(4, composed.getSize());
        Assert.assertEquals(4, composedC.getSize());
        Assert.assertEquals(composed.getPrototype(), composedC.getPrototype());
        Assert.assertEquals(composed.getParent(), composedC.getParent());
        Assert.assertEquals(parent, composed.getParent());
        Assert.assertEquals(parent, composedC.getParent());
        Assert.assertEquals(composed.getChildren().size(), composedC.getChildren().size());
        Assert.assertEquals(3, composed.getChildren().size());
        Assert.assertEquals(3, composedC.getChildren().size());
    }
    
    /**
     * test
     */
    @Test
    public final void testSibling() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        final AtomicAttributeNode name = (AtomicAttributeNode) person.getChildren().get(0);
        final ComposedAttributeNode address = (ComposedAttributeNode) person.getChildren().get(3);
        
        Assert.assertEquals(9, root.getSize());
        
        final AttributeNode addressS = address.createSibling();
        Assert.assertTrue(addressS instanceof ComposedAttributeNode);
        Assert.assertEquals(addressS.getParent(), address.getParent());
        Assert.assertTrue(addressS.isEmpty());
        
        Assert.assertEquals(12, root.getSize());
        
        final AttributeNode nameS = name.createSibling();
        Assert.assertTrue(nameS instanceof AtomicAttributeNode);
        Assert.assertEquals(nameS.getParent(), name.getParent());
        Assert.assertTrue(nameS.isEmpty());
        
        Assert.assertEquals(13, root.getSize());
        
        final AttributeNode nameSS = nameS.createSibling();
        Assert.assertTrue(nameSS instanceof AtomicAttributeNode);
        Assert.assertEquals(nameSS.getParent(), nameS.getParent());
        Assert.assertTrue(nameSS.isEmpty());
        
        Assert.assertEquals(14, root.getSize());
        
        final AttributeNode addressS2 = address.createSibling();
        Assert.assertTrue(addressS2 instanceof ComposedAttributeNode);
        Assert.assertEquals(addressS2.getParent(), address.getParent());
        Assert.assertTrue(addressS2.isEmpty());
        
        Assert.assertEquals(17, root.getSize());
    }
    
    /**
     * test
     */
    @Test
    public final void testToHighlightData() {
        final AttributePrototype proto = SimpleAttributeUtilsTest.createTestRoot();
        
        final ComposedAttributeNode root = (ComposedAttributeNode) SimpleAttributeUtils.createFromPrototype(proto);
        final ComposedAttributeNode person = (ComposedAttributeNode) root.getChildren().get(1);
        
        final AtomicAttributeNode n1 = (AtomicAttributeNode) person.getChildren().get(0);
        
        n1.setValue("N1");
        
        Assert.assertEquals("N1", SimpleSearchUtils.toHighlightData(root, "="));
        Assert.assertEquals("N1", SimpleSearchUtils.toHighlightData(person, "="));
        
        Assert.assertEquals("AAA", SimpleSearchUtils.objectToString("AAA", "+"));
        Assert.assertEquals("1+2+3+4+5", SimpleSearchUtils.objectToString(Arrays.asList(1, 2, 3, 4, 5), "+"));
        Assert.assertEquals("148.54", SimpleSearchUtils.objectToString(148.54, "+"));
        Assert.assertEquals("-148", SimpleSearchUtils.objectToString(-148, "+"));
        Assert.assertEquals("false", SimpleSearchUtils.objectToString(false, "+"));
        Assert.assertEquals("", SimpleSearchUtils.objectToString(Collections.emptySet(), "+"));
        Assert.assertEquals("", SimpleSearchUtils.objectToString(null, "+"));
    }
    
    /**
     * test
     */
    @Test
    public final void testJson() {
        // to JSON
        
        final AttributePrototype root = SimpleAttributeUtilsTest.createTestRoot();
        final String json = SimpleStringUtils.toJson(root, false);
        Assert.assertEquals(687, json.length());
        
        // from JSON
        
        final AttributePrototype entity = SimpleStringUtils.fromJson(json, AttributePrototype.class);
        Assert.assertEquals("root", entity.getKey());
        Assert.assertEquals("Root", entity.getTitle());
        Assert.assertEquals(false, entity.isRepeat());
        Assert.assertEquals(false, entity.isAtomic());
    }
    
    /**
     * test
     */
    @Test
    public final void testGather() {
        // all attributes
        
        final List<Tuple<String, AttributePrototype>> list = SimpleAttributeUtils.gatherToList(SimpleAttributeUtilsTest.createTestRoot(), false);
        Assert.assertEquals(8, list.size());
        
        final Tuple<String, AttributePrototype> l0 = list.get(0);
        Assert.assertEquals("Note", l0.getFirst());
        Assert.assertEquals("Note", l0.getSecond().getTitle());
        
        final Tuple<String, AttributePrototype> l5 = list.get(5);
        Assert.assertEquals("Person / Address*", l5.getFirst());
        Assert.assertEquals("Address", l5.getSecond().getTitle());
        
        final Tuple<String, AttributePrototype> l7 = list.get(7);
        Assert.assertEquals("Person / Address* / City", l7.getFirst());
        Assert.assertEquals("City", l7.getSecond().getTitle());
        
        // atomic attributes only
        
        final List<Tuple<String, AttributePrototype>> list2 = SimpleAttributeUtils.gatherToList(SimpleAttributeUtilsTest.createTestRoot(), true);
        Assert.assertEquals(6, list2.size());
        
        final Tuple<String, AttributePrototype> l20 = list2.get(0);
        Assert.assertEquals("Note", l20.getFirst());
        Assert.assertEquals("Note", l20.getSecond().getTitle());
        
        final Tuple<String, AttributePrototype> l25 = list2.get(5);
        Assert.assertEquals("Person / Address* / City", l25.getFirst());
        Assert.assertEquals("City", l25.getSecond().getTitle());
    }
    
    /**
     * Creates a testing attribute prototype tree.
     * 
     * @return test the root of the testing tree
     */
    public static AttributePrototype createTestRoot() {
        return new AttributePrototype("root", "Root", false,
                new AttributePrototype("note", "Note", true),
                new AttributePrototype("person", "Person", true,
                        new AttributePrototype("name", "Name", true),
                        new AttributePrototype("note", "Note", true),
                        new AttributePrototype("phone", "Phone", true),
                        new AttributePrototype("address", "Address", true,
                                new AttributePrototype("street", "Street", false),
                                new AttributePrototype("city", "City", false))));
    }
}
