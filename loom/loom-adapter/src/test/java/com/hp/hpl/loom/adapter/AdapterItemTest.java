/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom.adapter;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class AdapterItemTest {
    private static final Log LOG = LogFactory.getLog(AdapterItemTest.class);

    ItemTypeA itA = new ItemTypeA();
    ItemTypeB itB = new ItemTypeB();
    ItemTypeC itC = new ItemTypeC();
    TestItemCollector ic = new TestItemCollector();
    TestItem itemB1;
    TestItem itemB2;
    TestItem itemB3;
    TestItem itemC1;
    TestItem itemC2;
    TestItem itemA;

    Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        itA.setId("os-" + itA.getLocalId());
        itB.setId("os-" + itB.getLocalId());
        itC.setId("os-" + itC.getLocalId());
        itemB1 = new TestItem(ic.getLogicalId(itB.getLocalId(), "b1"), itB);
        ic.registerAdapterItem(itemB1);
        itemB2 = new TestItem(ic.getLogicalId(itB.getLocalId(), "b2"), itB);
        ic.registerAdapterItem(itemB2);
        itemB3 = new TestItem(ic.getLogicalId(itB.getLocalId(), "b3"), itB);
        ic.registerAdapterItem(itemB3);
        itemC1 = new TestItem(ic.getLogicalId(itC.getLocalId(), "c1"), itC);
        ic.registerAdapterItem(itemC1);
        itemC2 = new TestItem(ic.getLogicalId(itC.getLocalId(), "c2"), itC);
        ic.registerAdapterItem(itemC2);
        itemA = new TestItem(ic.getLogicalId(itA.getLocalId(), "a1"), itA);
        ic.registerAdapterItem(itemA);
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
    }

    private void removeItem(final TestItem item) {
        ic.deregisterAdapterItem(item);
        item.setUpdateStatus(AdapterItem.DELETED);
    }

    private void setAllRelationships() {
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b1");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b2");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b3");
        itemA.setRelationship(provider, ItemTypeC.TYPE_LOCAL_ID, "c1");
        itemA.setRelationship(provider, ItemTypeC.TYPE_LOCAL_ID, "c2");
    }

    private void setTwoABOneAC() {
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b1");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b2");
        itemA.setRelationship(provider, ItemTypeC.TYPE_LOCAL_ID, "c1");
    }

    private void setTwoABTwoAC() {
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b1");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b3");
        itemA.setRelationship(provider, ItemTypeC.TYPE_LOCAL_ID, "c1");
        itemA.setRelationship(provider, ItemTypeC.TYPE_LOCAL_ID, "c2");

    }

    private void setB1B2() {
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b1");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b2");
    }

    private void setB1B3() {
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b1");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b3");
    }

    private void setThreeAB() {
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b1");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b2");
        itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, "b3");
    }

    private void setAFromC1() {
        itemC1.setRelationship(provider, ItemTypeA.TYPE_LOCAL_ID, "a1");
    }

    private void fillUpOthers(final ItemRelationsDelta relationsDelta) {
        ArrayList<Item> newOthers = new ArrayList<>();
        RelationsDeltaType deltaType = relationsDelta.getType();
        if (deltaType.equals(RelationsDeltaType.Clear)) {
            return;
        }
        for (String lid : relationsDelta.getOtherIds()) {
            newOthers.add(ic.getAdapterItem(null, lid));
        }
        relationsDelta.setOthers(newOthers);
    }

    private void applyDeltas(final Collection<ItemRelationsDelta> deltas) {
        for (ItemRelationsDelta relationsDelta : deltas) {
            RelationsDeltaType deltaType = relationsDelta.getType();
            Item item = relationsDelta.getItem();
            // Item other = relationsDelta.getOther();
            fillUpOthers(relationsDelta);
            Collection<Item> others = relationsDelta.getOthers();
            String relationsName = relationsDelta.getRelationsName();
            switch (deltaType) {
                case Add:
                    if (others != null) {
                        for (Item otherItem : others) {
                            addConnectedRelationshipsOnItem(item, otherItem, relationsName);
                        }
                    }
                    break;
                case Remove:
                    if (others != null) {
                        for (Item otherItem : others) {
                            removeConnectedRelationshipsOnItem(item, otherItem, relationsName);
                        }
                    }
                    break;
                case Clear:
                    if (relationsName == null) {
                        item.removeAllConnectedRelationships();
                    } else {
                        item.removeAllConnectedRelationshipsWithRelationshipName(relationsName);
                    }
                    break;
            }
        }
    }

    // private void addConnectedRelationshipsOnItem(final Item item, final Item other, final String
    // relationsName) {
    // if (relationsName == null) {
    // item.addConnectedRelationships(other);
    // } else {
    // item.addConnectedRelationshipsWithName(relationsName, other);
    // }
    // }
    //
    // private void removeConnectedRelationshipsOnItem(final Item item, final Item other, final
    // String relationsName) {
    // if (relationsName == null) {
    // item.removeConnectedRelationships(other);
    // } else {
    // item.removeConnectedRelationshipsWithName(relationsName, other);
    // }
    // }

    private void addConnectedRelationshipsOnItem(final Item item, final Item other, final String relationsName) {
        item.addConnectedRelationshipsWithName(relationsName, other);
    }

    private void removeConnectedRelationshipsOnItem(final Item item, final Item other, final String relationsName) {
        item.removeConnectedRelationshipsWithNameAndType(relationsName, other);
    }

    @Test
    public void testRelationshipSetting() {
        setAllRelationships();
        Map<String, Collection<String>> idsMap = itemA.getRelationshipsIds();
        assertTrue("Expected 2 relationships names: " + idsMap.size(), idsMap.size() == 2);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 3 A_B rels: " + idsMap.get(relNameAB).size(), idsMap.get(relNameAB).size() == 3);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 2 A_C rels: " + idsMap.get(relNameAC).size(), idsMap.get(relNameAC).size() == 2);
    }

    @Test
    public void testRelationshipListSetting() {
        List<String> bList = Arrays.asList("b1", "b2", "b3");
        List<String> cList = Arrays.asList("c1", "c2");
        for (String connectedResourceId : bList) {
            itemA.setRelationship(provider, ItemTypeB.TYPE_LOCAL_ID, connectedResourceId);
        }
        for (String connectedResourceId : cList) {
            itemA.setRelationship(provider, ItemTypeC.TYPE_LOCAL_ID, connectedResourceId);
        }
        Map<String, Collection<String>> idsMap = itemA.getRelationshipsIds();
        assertTrue("Expected 2 relationships names: " + idsMap.size(), idsMap.size() == 2);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 3 A_B rels: " + idsMap.get(relNameAB).size(), idsMap.get(relNameAB).size() == 3);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 2 A_C rels: " + idsMap.get(relNameAC).size(), idsMap.get(relNameAC).size() == 2);
    }

    @Test
    public void testInitialDeltas() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 2 rel deltas: " + deltas.size(), deltas.size() == 2);
        int add_cnt = 0;
        for (ItemRelationsDelta ird : deltas) {
            if (ird.getType().equals(RelationsDeltaType.Add)) {
                add_cnt += ird.getOtherIds().size();
            }
        }
        assertTrue("Expected 5 additions: " + add_cnt, add_cnt == 5);
        applyDeltas(deltas);
        Map<String, Map<String, Item>> connectedRels = itemA.getConnectedRelationships();
        assertTrue("Expected 2 relationships names: " + connectedRels.size(), connectedRels.size() == 2);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 3 A_B rels: " + connectedRels.get(relNameAB).size(),
                connectedRels.get(relNameAB).size() == 3);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 2 A_C rels: " + connectedRels.get(relNameAC).size(),
                connectedRels.get(relNameAC).size() == 2);
    }

    @Test
    public void testLooseOneAB() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        setTwoABTwoAC();
        Map<String, Collection<String>> idsMap = itemA.getRelationshipsIds();
        assertTrue("Expected 2 relationships names: " + idsMap.size(), idsMap.size() == 2);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 2 A_B rels: " + idsMap.get(relNameAB).size(), idsMap.get(relNameAB).size() == 2);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 2 A_C rels: " + idsMap.get(relNameAC).size(), idsMap.get(relNameAC).size() == 2);
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 1 rel deltas: " + deltas.size(), deltas.size() == 1);
        ItemRelationsDelta ird = deltas.iterator().next();
        assertTrue("Expected a remove delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Remove));
    }

    @Test
    public void testChangeOneABAddOneAC() {
        // b1, b2, c1
        setTwoABOneAC();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        // -b2, +b3, +c2
        setTwoABTwoAC();
        Map<String, Collection<String>> idsMap = itemA.getRelationshipsIds();
        assertTrue("Expected 2 relationships names: " + idsMap.size(), idsMap.size() == 2);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 2 A_B rels: " + idsMap.get(relNameAB).size(), idsMap.get(relNameAB).size() == 2);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 2 A_C rels: " + idsMap.get(relNameAC).size(), idsMap.get(relNameAC).size() == 2);
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 3 rel deltas: " + deltas.size(), deltas.size() == 3);
        for (ItemRelationsDelta ird : deltas) {
            if (ird.getType().equals(RelationsDeltaType.Remove)) {
                assertTrue("Expected AB relname for remove: " + ird.getRelationsName(),
                        ird.getRelationsName().equals(relNameAB));
            } else if (ird.getType().equals(RelationsDeltaType.Add)) {
                assertTrue("Expected 1 add for each relname: " + ird.getOtherIds().size(),
                        ird.getOtherIds().size() == 1);
                String addId = ird.getOtherIds().iterator().next();
                if (ird.getRelationsName().equals(relNameAB)) {
                    assertTrue("Expected b3 to be added: " + addId, addId.equals(itemB3.getLogicalId()));
                } else if (ird.getRelationsName().equals(relNameAC)) {
                    assertTrue("Expected c2 to be added: " + addId, addId.equals(itemC2.getLogicalId()));
                }
            }
        }
    }

    @Test
    public void testLooseAllAC() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        setThreeAB();
        Map<String, Collection<String>> idsMap = itemA.getRelationshipsIds();
        assertTrue("Expected 1 relationships names: " + idsMap.size(), idsMap.size() == 1);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 3 A_B rels: " + idsMap.get(relNameAB).size(), idsMap.get(relNameAB).size() == 3);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected NO A_C rels: " + idsMap.get(relNameAC), idsMap.get(relNameAC) == null);
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 1 rel deltas: " + deltas.size(), deltas.size() == 1);
        ItemRelationsDelta ird = deltas.iterator().next();
        assertTrue("Expected a clear delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Clear));
        assertTrue("Expected a clear delta on AC rel: " + ird.getRelationsName(),
                ird.getRelationsName().equals(relNameAC));
        applyDeltas(deltas);
        Map<String, Map<String, Item>> connectedRels = itemA.getConnectedRelationships();
        assertTrue("Expected 1 relationships names: " + connectedRels.size(), connectedRels.size() == 1);
        assertTrue("Expected 3 A_B rels: " + connectedRels.get(relNameAB).size(),
                connectedRels.get(relNameAB).size() == 3);
        assertTrue("Expected No A_C rels: " + connectedRels.get(relNameAC), connectedRels.get(relNameAC) == null);
    }

    @Test
    public void testLooseAll() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 1 rel deltas: " + deltas.size(), deltas.size() == 1);
        ItemRelationsDelta ird = deltas.iterator().next();
        assertTrue("Expected a clear delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Clear));
        assertTrue("Expected a clear delta with no rel: " + ird.getRelationsName(), ird.getRelationsName() == null);
    }

    @Test
    public void testLooseAllThroughRemoval() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        removeItem(itemB1);
        removeItem(itemB2);
        removeItem(itemB3);
        removeItem(itemC1);
        removeItem(itemC2);
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 1 rel deltas: " + deltas.size(), deltas.size() == 1);
        ItemRelationsDelta ird = deltas.iterator().next();
        assertTrue("Expected a clear delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Clear));
        assertTrue("Expected a clear delta with no rel: " + ird.getRelationsName(), ird.getRelationsName() == null);
    }

    @Test
    public void testLooseAllACThroughRemoval() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        setThreeAB();
        removeItem(itemC1);
        removeItem(itemC2);
        Map<String, Collection<String>> idsMap = itemA.getRelationshipsIds();
        assertTrue("Expected 1 relationships names: " + idsMap.size(), idsMap.size() == 1);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 3 A_B rels: " + idsMap.get(relNameAB).size(), idsMap.get(relNameAB).size() == 3);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected NO A_C rels: " + idsMap.get(relNameAC), idsMap.get(relNameAC) == null);
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected 1 rel deltas: " + deltas.size(), deltas.size() == 1);
        ItemRelationsDelta ird = deltas.iterator().next();
        assertTrue("Expected a clear delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Clear));
        assertTrue("Expected a clear delta on AC rel: " + ird.getRelationsName(),
                ird.getRelationsName().equals(relNameAC));
        applyDeltas(deltas);
        Map<String, Map<String, Item>> connectedRels = itemA.getConnectedRelationships();
        assertTrue("Expected 1 relationships names: " + connectedRels.size(), connectedRels.size() == 1);
        assertTrue("Expected 3 A_B rels: " + connectedRels.get(relNameAB).size(),
                connectedRels.get(relNameAB).size() == 3);
        assertTrue("Expected No A_C rels: " + connectedRels.get(relNameAC), connectedRels.get(relNameAC) == null);
    }

    @Test
    public void testRemoveItem() {
        setAllRelationships();
        Collection<ItemRelationsDelta> deltas = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltas);
        itemA.clearRelationshipsIds();
        // -b2
        setTwoABTwoAC();
        removeItem(itemB2);
        deltas = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected one rel deltas: " + deltas.size(), deltas.size() == 1);
        ItemRelationsDelta ird = deltas.iterator().next();
        assertTrue("Expected a remove delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Remove));
    }


    // tests now include item based relationships added through another Item
    @Test
    public void testInitialSettingWithExternalRel() {
        setThreeAB();
        setAFromC1();
        Collection<ItemRelationsDelta> deltasA1 = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltasA1);
        Collection<ItemRelationsDelta> deltasC1 = itemC1.createRelationshipsDeltasIDs();
        applyDeltas(deltasC1);
        Map<String, Map<String, Item>> connectedRels = itemA.getConnectedRelationships();
        assertTrue("Expected 2 relationships names: " + connectedRels.size(), connectedRels.size() == 2);
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected 3 A_B rels: " + connectedRels.get(relNameAB).size(),
                connectedRels.get(relNameAB).size() == 3);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 1 A_C rels: " + connectedRels.get(relNameAC).size(),
                connectedRels.get(relNameAC).size() == 1);
    }

    @Test
    public void testDeletionWithExternalRel() {
        // tick 1
        setThreeAB();
        setAFromC1();
        Collection<ItemRelationsDelta> deltasA1 = itemA.createRelationshipsDeltasIDs();
        applyDeltas(deltasA1);
        Collection<ItemRelationsDelta> deltasC1 = itemC1.createRelationshipsDeltasIDs();
        applyDeltas(deltasC1);
        itemA.clearRelationshipsIds();
        // tick1: no rel detected + no change on itemC1
        itemA.updateRelnameSet();
        deltasA1 = itemA.createRelationshipsDeltasIDs();
        // 1 delta with clear on relName
        assertTrue("Expected 1 rel deltas: " + deltasA1.size(), deltasA1.size() == 1);
        ItemRelationsDelta ird = deltasA1.iterator().next();
        assertTrue("Expected a clear delta: " + ird.getType(), ird.getType().equals(RelationsDeltaType.Clear));
        String relNameAB = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itB, null);
        assertTrue("Expected a clear delta on relAB: " + relNameAB + " vs " + ird.getRelationsName(),
                relNameAB.equals(ird.getRelationsName()));
        applyDeltas(deltasA1);
        Map<String, Map<String, Item>> connectedRels = itemA.getConnectedRelationships();
        assertTrue("Expected 1 relationships names: " + connectedRels.size(), connectedRels.size() == 1);
        String relNameAC = RelationshipUtil.getRelationshipNameBetweenTypesWithRelType(itA, itC, null);
        assertTrue("Expected 1 A_C rels: " + connectedRels.get(relNameAC).size(),
                connectedRels.get(relNameAC).size() == 1);
        itemA.clearRelationshipsIds();
        // tick2: no rel detected + no change on itemC1
        itemA.updateRelnameSet();
        deltasA1 = itemA.createRelationshipsDeltasIDs();
        assertTrue("Expected NO rel deltas: " + deltasA1, deltasA1 == null);
        assertTrue("Expected 1 A_C rels: " + connectedRels.get(relNameAC).size(),
                connectedRels.get(relNameAC).size() == 1);
    }
}
