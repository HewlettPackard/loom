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
package com.hp.hpl.loom.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;
import org.springframework.beans.InvalidPropertyException;

import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;
import com.hp.hpl.loom.relationships.RelationshipUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

public class BaseModelTest {
    Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");

    private class MyFibreType extends ItemType {
        static final String TYPE_LOCAL_ID = "myfibre";
        static final String PROVIDER_ID = "my";
        static final String TYPE_ID = PROVIDER_ID + "-" + TYPE_LOCAL_ID;

        public MyFibreType() {
            super(TYPE_LOCAL_ID);
            setId(PROVIDER_ID + "-" + getLocalId());
        }
    }

    private class MyFibre extends Item {
        MyFibre(final String logicalId, final ItemType type) {
            super(logicalId, type);
        }

        MyFibre(final String logicalId, final ItemType type, final String name, final String description) {
            super(logicalId, type, name, description);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Fibre that = (Fibre) o;

            return Objects.equals(getFibreCreated(), that.getFibreCreated())
                    && Objects.equals(getFibreDeleted(), that.getFibreDeleted())
                    && Objects.equals(getDescription(), that.getDescription())
                    && Objects.equals(getFibreType(), that.getFibreType())
                    && Objects.equals(getLogicalId(), that.getLogicalId()) && Objects.equals(getName(), that.getName())
                    && Objects.equals(getTypeId(), that.getTypeId())
                    && Objects.equals(getFibreUpdated(), that.getFibreUpdated());

        }


        @Override
        public int hashCode() {
            return Objects.hash(getFibreType(), getLogicalId(), getTypeId(), getName(), getDescription(),
                    getFibreCreated(), getFibreUpdated(), getFibreDeleted());
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    @Test
    public void testFoo() {
        List<MyFibre> fibres = new ArrayList<>();
        List<MyFibre> fibres2 = new ArrayList<>();
        MyFibreType myFibreType = new MyFibreType();


        for (int i = 0; i < 4000000; i++) {
            MyFibre fib = new MyFibre("LogicalId", myFibreType);
            fibres.add(fib);
            fibres2.add(fib);
        }
        fibres.removeAll(fibres2);
    }

    @Test
    public void testFibreConstruction() {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();

        {
            MyFibre myFibre = new MyFibre(logicalId, myFibreType);
            assertEquals("Logical ID incorrect", logicalId, myFibre.getLogicalId());
            assertEquals("Type ID incorrect", MyFibreType.TYPE_ID, myFibre.getTypeId());
        }

        {
            String name = "myname";
            String description = "my description";
            MyFibre myFibre = new MyFibre(logicalId, myFibreType, name, description);
            assertEquals("Logical ID incorrect", logicalId, myFibre.getLogicalId());
            assertEquals("Type ID not correct", MyFibreType.TYPE_ID, myFibre.getTypeId());
            assertEquals("Name incorrect", name, myFibre.getName());
            assertEquals("Description incorrect", description, myFibre.getDescription());
        }
    }

    @Test
    public void testFibreMemberOf() {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        MyFibre myFibre = new MyFibre(logicalId, myFibreType);
        assertNotNull("MemberOf collection is null", myFibre.getMemberOf());
        assertEquals("Initial MemberOf size incorrect", 0, myFibre.getMemberOf().size());

        String aggLogicalId = "/da/myfibre";
        Aggregation agg = new Aggregation(aggLogicalId, MyFibreType.TYPE_ID, "myfibreagg", "myfibre aggregation", 1);

        myFibre.addMemberOf(agg);
        assertEquals("Added MemberOf size incorrect", 1, myFibre.getMemberOf().size());
        assertEquals("MemberOf incorrect", agg, myFibre.getMemberOf().iterator().next());

        myFibre.removeMemberOf(agg);
        assertEquals("Added MemberOf size incorrect", 0, myFibre.getMemberOf().size());

        myFibre.addMemberOf(agg);
        assertEquals("Added MemberOf size incorrect", 1, myFibre.getMemberOf().size());
        assertEquals("MemberOf incorrect", agg, myFibre.getMemberOf().iterator().next());

        myFibre.clearMemberOf();
        assertEquals("Added MemberOf size incorrect", 0, myFibre.getMemberOf().size());
    }

    @Test
    public void testFibreEquals() {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        Date now = new Date();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        Date later = new Date();
        MyFibre myFibre = new MyFibre(logicalId, myFibreType, "myname", "My description");
        MyFibre myFibreEqual = new MyFibre(logicalId, myFibreType, "myname", "My description");
        MyFibre myFibreNotEqual = new MyFibre(logicalId + "1", myFibreType, "myname", "My description");
        myFibre.setFibreCreated(now);
        myFibre.setFibreUpdated(now);
        myFibre.setFibreDeleted(now);
        myFibreEqual.setFibreCreated(now);
        myFibreEqual.setFibreUpdated(now);
        myFibreEqual.setFibreDeleted(now);
        myFibreNotEqual.setFibreCreated(now);
        myFibreNotEqual.setFibreUpdated(later);
        myFibreNotEqual.setFibreDeleted(later);

        assertTrue("Fibre not equal to itself", myFibre.equals(myFibre));
        assertFalse("Fibre equal to null", myFibre.equals(null));
        assertTrue("Fibre not equal to equivalenet", myFibre.equals(myFibreEqual));
        assertFalse("Fibre equal to mon equivalenet", myFibre.equals(myFibreNotEqual));

        assertTrue("Fibre hash not equal to equivalent", myFibre.hashCode() == myFibreEqual.hashCode());
        assertFalse("Fibre hash equal to non equivalent", myFibre.hashCode() == myFibreNotEqual.hashCode());

        String aggLogicalId = "/da/myfibre";
        Aggregation agg = new Aggregation(aggLogicalId, MyFibreType.TYPE_ID, "myfibreagg", "myfibre aggregation", 1);
        assertFalse("Fibre equal to Aggregation", myFibre.equals(agg));
    }

    @Test
    public void testIntrospectPropertyErrorUnknown() {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        MyFibre myFibre = new MyFibre(logicalId, myFibreType);

        Map<OperationErrorCode, String> errors = new HashMap<>();
        Object value = FibreIntrospectionUtils.introspectProperty("bad", myFibre, errors, null);
        assertNull("introspectProperty not null for unknown property", value);
    }

    @Test
    public void testIntrospectPropertyErrorReserved() {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        MyFibre myFibre = new MyFibre(logicalId, myFibreType);

        Map<OperationErrorCode, String> errors = new HashMap<>();
        Object value = FibreIntrospectionUtils.introspectProperty("memberOf", myFibre, errors, null);
        assertNull("introspectProperty not null", value);
        assertTrue("Reserved property not repotrted", errors.containsKey(OperationErrorCode.NotReadableField));
    }

    @Test(expected = IllegalAccessException.class)
    public void testIntrospectPropertyReserved()
            throws InvalidPropertyException, InvocationTargetException, IllegalAccessException {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        MyFibre myFibre = new MyFibre(logicalId, myFibreType);

        FibreIntrospectionUtils.introspectPropertyStrict("memberOf", myFibre, null);
    }

    @Test(expected = IllegalAccessException.class)
    public void testIntrospectPropertyForFibresReserved()
            throws InvalidPropertyException, InvocationTargetException, IllegalAccessException {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        MyFibre myFibre = new MyFibre(logicalId, myFibreType);
        MyFibre[] myFibreArray = {myFibre};
        List<Fibre> fibres = Arrays.asList(myFibreArray);

        FibreIntrospectionUtils.introspectPropertyForFibres("memberOf", fibres, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFibreConstructorInvalidTypeId() {
        String logicalId = "/myfibre/myid";
        MyFibreType myFibreType = new MyFibreType();
        myFibreType.setId(null);
        new MyFibre(logicalId, myFibreType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFibreConstructorInvalidType() {
        String logicalId = "/myfibre/myid";
        new MyFibre(logicalId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFibreConstructorInvalidLogicalId() {
        String logicalId = "";
        MyFibreType myFibreType = new MyFibreType();
        new MyFibre(logicalId, myFibreType);
    }


    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Item
    // //////////////////////////////////////////////////////////////////////////////////////

    private class MyItemType extends ItemType {
        static final String TYPE_LOCAL_ID = "myitem";
        static final String PROVIDER_ID = "my";
        static final String TYPE_ID = PROVIDER_ID + "-" + TYPE_LOCAL_ID;

        public MyItemType() {
            super(TYPE_LOCAL_ID);
            setId(PROVIDER_ID + "-" + getLocalId());
        }
    }

    private class MyItem extends Item {
        MyItem(final String logicalId, final ItemType type) {
            super(logicalId, type);
        }

        MyItem(final String logicalId, final ItemType type, final String name, final String description) {
            super(logicalId, type, name, description);
        }

    }

    private class MyOtherItemType extends ItemType {
        static final String TYPE_LOCAL_ID = "myotheritem";
        static final String PROVIDER_ID = "my";
        static final String TYPE_ID = PROVIDER_ID + "-" + TYPE_LOCAL_ID;

        public MyOtherItemType() {
            super(TYPE_LOCAL_ID);
            setId(PROVIDER_ID + "-" + getLocalId());
        }
    }

    private class MyOtherItem extends Item {
        MyOtherItem(final String logicalId, final ItemType type) {
            super(logicalId, type);
        }

        MyOtherItem(final String logicalId, final ItemType type, final String name, final String description) {
            super(logicalId, type, name, description);
        }
    }

    @Test
    public void testItemConstruction() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();

        {
            MyItem myItem = new MyItem(logicalId, myItemType);
            assertEquals("Logical ID incorrect", logicalId, myItem.getLogicalId());
            assertEquals("Type ID incorrect", MyItemType.TYPE_ID, myItem.getTypeId());
        }

        {
            String name = "myname";
            String description = "my description";
            MyItem myItem = new MyItem(logicalId, myItemType, name, description);
            assertEquals("Logical ID incorrect", logicalId, myItem.getLogicalId());
            assertEquals("Type ID not correct", MyItemType.TYPE_ID, myItem.getTypeId());
            assertEquals("Name incorrect", name, myItem.getName());
            assertEquals("Description incorrect", description, myItem.getDescription());
        }

    }

    @Test
    public void testItemEquals() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem = new MyItem(logicalId, myItemType);
        MyItem myItemEqual = new MyItem(logicalId, myItemType);
        MyItem myItemNotEqual = new MyItem(logicalId + "1", myItemType);

        assertTrue("Item not equal to itself", myItem.equals(myItem));
        assertFalse("Item equal to null", myItem.equals(null));

        assertFalse("Item not equal to equivalent", myItem.equals(myItemEqual)); // UUID different
        assertFalse("Item equal to non equivalent", myItem.equals(myItemNotEqual));

        assertFalse("Item hash not equal to equivalent", myItem.hashCode() == myItemEqual.hashCode()); // UUID
                                                                                                       // different
        assertFalse("Item hash equal to non equivalent", myItem.hashCode() == myItemNotEqual.hashCode());

        String aggLogicalId = "/da/myitem";
        Aggregation agg = new Aggregation(aggLogicalId, MyItemType.TYPE_ID, "myitemagg", "myitem aggregation", 1);
        assertFalse("Item equal to Aggregation", myItem.equals(agg));
    }

    @Test
    public void testItemQualifiedName() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem = new MyItem(logicalId, myItemType);
        assertNull("QualifiedName not null", myItem.getQualifiedName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testItemNullItemTypeConnectedTo() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem1 = new MyItem(logicalId + "1", myItemType);
        MyItem myItem2 = new MyItem(logicalId + "2", myItemType);
        // So far so good ... now break rules and attempt to connect
        myItemType.setLocalId(null);
        myItem1.addConnectedRelationships(myItem2, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testItemNullConnectedTo() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem1 = new MyItem(logicalId, myItemType);
        myItem1.addConnectedRelationships(null, "");
    }

    @Test
    public void testItemCheckConnectedRelationshipsDifferent() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem1 = new MyItem(logicalId + "1", myItemType);
        MyItem myItem2 = new MyItem(logicalId + "2", myItemType);

        // Connect two items
        myItem1.addConnectedRelationships(myItem2, "");

        assertFalse("Item connected relationships should be same as self",
                myItem1.checkConnectedRelationshipsDifferent(myItem1));

        assertTrue("Item 1 connected relationships should be same as equivalent 2",
                myItem1.checkConnectedRelationshipsDifferent(myItem2));
        assertTrue("Item 2 connected relationships should be same as equivalent 1",
                myItem2.checkConnectedRelationshipsDifferent(myItem1));

        // Modify relationships
        MyItem myItem3 = new MyItem(logicalId + "3", myItemType);
        myItem1.addConnectedRelationships(myItem3, "");

        assertTrue("Item 1 connected relationships should now be different from 3",
                myItem1.checkConnectedRelationshipsDifferent(myItem3));
        assertTrue("Item 3 connected relationships should now be different from 1",
                myItem3.checkConnectedRelationshipsDifferent(myItem1));

        assertTrue("Item 1 connected relationships should now be different from 2",
                myItem1.checkConnectedRelationshipsDifferent(myItem2));
        assertTrue("Item 2 connected relationships should now be different from 1",
                myItem2.checkConnectedRelationshipsDifferent(myItem1));

        // Check comparison with empty relationships
        MyItem myItemDisconnected = new MyItem(logicalId + "Discon", myItemType);
        assertTrue("Item 1 connected relationships should now be different from same type empty",
                myItem1.checkConnectedRelationshipsDifferent(myItemDisconnected));
        assertTrue("Item same type empty connected relationships should now be different from 1",
                myItemDisconnected.checkConnectedRelationshipsDifferent(myItem1));


        // Check comparison with empty relationships with different type
        String otherLogicalId = "/myotheritem/myid";
        MyOtherItemType myOtherItemType = new MyOtherItemType();
        MyOtherItem myOtherItem1 = new MyOtherItem(otherLogicalId + "1", myOtherItemType);
        MyOtherItem myOtherItem2 = new MyOtherItem(otherLogicalId + "2", myOtherItemType);
        myOtherItem1.addConnectedRelationships(myOtherItem2, "");
        assertTrue("Item 1 connected relationships should now be different from item of different type",
                myItem1.checkConnectedRelationshipsDifferent(myOtherItem1));
        assertTrue("Item of different type connected relationships should now be different from 1",
                myOtherItem1.checkConnectedRelationshipsDifferent(myItem1));
    }

    @Test
    public void testItemNullFirstConnectedTo() {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem1 = new MyItem(logicalId + "1", myItemType);

        assertNull("Foo relation should not exist", myItem1.getFirstConnectedItemWithRelationshipName("foo"));

        // Connect two items
        MyItem myItem2 = new MyItem(logicalId + "2", myItemType);
        myItem1.addConnectedRelationships(myItem2, "");

        // Check relations exist
        String relationName = RelationshipUtil.getRelationshipNameBetweenItems(myItem1, myItem2, "");
        Item item2 = myItem1.getFirstConnectedItemWithRelationshipName(relationName);
        assertNotNull(relationName + " relation should exist for 1", item2);
        assertEquals(relationName + " first item incorrect for 1", myItem2, item2);

        Item item1 = myItem2.getFirstConnectedItemWithRelationshipName(relationName);
        assertNotNull(relationName + " relation should exist for 2", item1);
        assertEquals(relationName + " first item incorrect for 2", myItem1, item1);

        // Disconnect two related items
        myItem1.removeConnectedRelationships(myItem2, "");
        assertNull(relationName + " relation for 1 should not exist after removal",
                myItem1.getFirstConnectedItemWithRelationshipName(relationName));
        assertNull(relationName + " relation for 2 should not exist after removal",
                myItem2.getFirstConnectedItemWithRelationshipName(relationName));

        // Disconnect two unrelated items
        MyItem myItem3 = new MyItem(logicalId + "3", myItemType);
        myItem1.removeConnectedRelationships(myItem3, "");
    }


    @Test
    public void testIntrospectPropertyForItemsConnectedTo()
            throws InvalidPropertyException, InvocationTargetException, IllegalAccessException {
        String logicalId = "/myitem/myid";
        MyItemType myItemType = new MyItemType();
        String logicalId1 = logicalId + "1";
        String logicalId2 = logicalId + "2";
        MyItem myItem1 = new MyItem(logicalId1, myItemType);
        MyItem myItem2 = new MyItem(logicalId2, myItemType);
        myItem1.addConnectedRelationships(myItem2, "");
        MyItem[] myItemArray = {myItem1, myItem2};
        List<Fibre> items = Arrays.asList(myItemArray);

        String propertyName = RelationshipUtil.getRelationshipNameBetweenItems(myItem1, myItem2, "");
        List<Object> properties = FibreIntrospectionUtils.introspectPropertyForFibres(propertyName, items, null);

        assertNotNull("Returned properties was null", properties);
        assertEquals("Returned properties incorrect size", 2, properties.size());
        for (int count = 0; count < properties.size(); count++) {
            assertNotNull("Returned property was null at " + count, properties.get(count));
        }
        assertEquals("First property incorrect", myItem2, properties.get(0));
        assertEquals("Second property incorrect", myItem1, properties.get(1));
    }


    // //////////////////////////////////////////////////////////////////////////////////////
    // Tests for Aggregation
    // //////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testAggregationEquals() {
        String aggLogicalId = "/da/myitem";
        Aggregation agg = new Aggregation(aggLogicalId, MyItemType.TYPE_ID, "myitemagg", "myitem aggregation", 1);
        Aggregation aggEqual = new Aggregation(aggLogicalId, MyItemType.TYPE_ID, "myitemagg", "myitem aggregation", 1);
        Aggregation aggSimilar =
                new Aggregation(aggLogicalId, MyItemType.TYPE_ID, "myitemaggdiff", "myitem aggregation", 1);
        Aggregation aggNotEqual =
                new Aggregation(aggLogicalId, MyItemType.TYPE_ID, "myitemagg", "myitem aggregation", 1);


        aggNotEqual.setDirty(true);
        aggNotEqual.setGrounded(true);

        assertTrue("Aggregation not equal to itself", agg.equals(agg));
        assertFalse("Aggregation equal to null", agg.equals(null));

        assertTrue("Aggregation not equal to equivalent", agg.equals(aggEqual));
        assertFalse("Aggregation equal to non equivalent", agg.equals(aggNotEqual));
        assertFalse("Aggregation equal to similar", agg.equals(aggSimilar));

        assertTrue("Aggregation hash not equal to equivalent", agg.hashCode() == aggEqual.hashCode());
        assertFalse("Aggregation hash equal to non equivalent", agg.hashCode() == aggNotEqual.hashCode());

        String logicalId = "/myfibre/myid";
        MyItemType myItemType = new MyItemType();
        MyItem myItem = new MyItem(logicalId, myItemType);
        assertFalse("Aggregation equal to Item", agg.equals(myItem));
    }

    @Test
    public void testAggregationElements() {
        int size = 10;
        String aggLogicalId = "/da/myitem";
        Aggregation agg = new Aggregation(aggLogicalId, MyItemType.TYPE_ID, "myitemagg", "myitem aggregation", size);

        String logicalId = "/myfibre/myid";
        MyItemType myItemType = new MyItemType();
        for (int count = 0; count < size; count++) {
            MyItem myItem = new MyItem(logicalId + count, myItemType);
            agg.add(myItem);
            assertEquals("Aggregation getter did not work", myItem, agg.get(count));
            if (count == 0) {
                assertEquals("Aggregation first did not work", myItem, agg.first());
            }
        }

        int count = 0;
        for (Iterator<Fibre> iterator = agg.getIterator(); iterator.hasNext();) {
            Fibre fibre = iterator.next();
            assertNotNull("Aggregation iterator returned null value", fibre);
            count++;
        }
        assertEquals("Aggregation iterator returned wronf number of elements", size, count);
    }
}
