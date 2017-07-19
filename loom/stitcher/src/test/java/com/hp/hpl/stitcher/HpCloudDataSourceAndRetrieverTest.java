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
package com.hp.hpl.stitcher;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import no.priv.garshol.duke.RecordIterator;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItemAttributes;
import com.hp.hpl.loom.adapter.hpcloud.itemtype.HpCloudItemType;

public class HpCloudDataSourceAndRetrieverTest {

    private static Collection<HpCloudItem<HpCloudItemAttributes>> elements; // To be mocked
    private static Map<String, String> propertiesInfo; // To be mocked
    private static HpCloudItem<HpCloudItemAttributes> e1, e2, e3, e4;
    private static HpCloudItemAttributes coreE1, coreE2, coreE3, coreE4;
    private static HpCloudItemType type;

    @BeforeClass
    public static void beforeClass() {
        coreE1 = mock(HpCloudItemAttributes.class);
        coreE2 = mock(HpCloudItemAttributes.class);
        coreE3 = mock(HpCloudItemAttributes.class);
        coreE4 = mock(HpCloudItemAttributes.class);

        when(coreE1.getAttributeAsString("_id")).thenReturn("e1");
        when(coreE2.getAttributeAsString("_id")).thenReturn("e2");
        when(coreE3.getAttributeAsString("_id")).thenReturn("e3");
        when(coreE4.getAttributeAsString("_id")).thenReturn("e4");

        when(coreE1.getHpCloudId()).thenReturn("e1");
        when(coreE2.getHpCloudId()).thenReturn("e2");
        when(coreE3.getHpCloudId()).thenReturn("e3");
        when(coreE4.getHpCloudId()).thenReturn("e4");

        type = mock(HpCloudItemType.class);
        when(type.getId()).thenReturn("test"); // NECESSARY TO PREVENT ERRORS!
        when(type.getLocalId()).thenReturn("test"); // NECESSARY TO PREVENT ERRORS!

        e1 = new HpCloudItem<HpCloudItemAttributes>("e1", type);
        e1.setCore(coreE1);
        e2 = new HpCloudItem<HpCloudItemAttributes>("e2", type);
        e2.setCore(coreE2);
        e3 = new HpCloudItem<HpCloudItemAttributes>("e3", type);
        e3.setCore(coreE3);
        e4 = new HpCloudItem<HpCloudItemAttributes>("e4", type);
        e4.setCore(coreE4);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructor() {
        elements = mock(Collection.class);
        propertiesInfo = mock(Map.class);
        when(propertiesInfo.containsKey("ID")).thenReturn(true);
        when(propertiesInfo.get("ID")).thenReturn("_id");

        new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructorNullArgs() {
        elements = mock(Collection.class);
        propertiesInfo = mock(Map.class);

        try {
            new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(null, propertiesInfo);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructorInvalidProperties() {
        elements = mock(Collection.class);
        propertiesInfo = mock(Map.class);
        when(propertiesInfo.containsKey("ID")).thenReturn(false);

        try {
            new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        when(propertiesInfo.containsKey("ID")).thenReturn(true);
        when(propertiesInfo.get("ID")).thenReturn("Wrong string!"); // This should make it fail
        try {
            new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        when(propertiesInfo.get("ID")).thenReturn("_id");
        // This should finally work
        new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRecordsEmpty() {
        elements = Collections.emptyList();
        propertiesInfo = mock(Map.class);
        when(propertiesInfo.containsKey("ID")).thenReturn(true);
        when(propertiesInfo.get("ID")).thenReturn("_id");

        HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>> hpCloudDataSource =
                new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);
        RecordIterator it = hpCloudDataSource.getRecords();
        assertFalse(it.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRecords() {
        elements = Arrays.asList(e1, e2, e3, e4);
        Collection<String> elementIds =
                Arrays.asList(e1.getCore().getAttributeAsString("_id"), e2.getCore().getAttributeAsString("_id"),
                        e3.getCore().getAttributeAsString("_id"), e4.getCore().getAttributeAsString("_id"));

        propertiesInfo = mock(Map.class);
        when(propertiesInfo.containsKey("ID")).thenReturn(true);
        when(propertiesInfo.get("ID")).thenReturn("_id");
        when(propertiesInfo.keySet()).thenReturn(new HashSet<String>(Arrays.asList("ID")));

        HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>> hpCloudDataSource =
                new HpCloudDataSource<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);
        RecordIterator it = hpCloudDataSource.getRecords();

        // Check iterated elements are among the original ones
        for (int i = 0; i < elements.size(); i++) {
            assertTrue(it.hasNext());
            assertTrue(elementIds.contains(it.next().getValue("ID")));
        }

        // Reached this point, there should be no more elements left
        assertFalse(it.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieve() {
        elements = Arrays.asList(e1, e2, e3, e4);

        propertiesInfo = mock(Map.class);
        when(propertiesInfo.containsKey("ID")).thenReturn(true);
        when(propertiesInfo.get("ID")).thenReturn("_id");
        when(propertiesInfo.keySet()).thenReturn(new HashSet<String>(Arrays.asList("ID")));

        HpCloudDataSourceAndRetriever<HpCloudItem<HpCloudItemAttributes>> hpCloudDataSourceAndRetriever =
                new HpCloudDataSourceAndRetriever<HpCloudItem<HpCloudItemAttributes>>(elements, propertiesInfo);


        for (HpCloudItem<HpCloudItemAttributes> e : elements) {
            assertEquals(e, hpCloudDataSourceAndRetriever.retrieve(e.getCore().getAttributeAsString("_id")));
        }
    }
}
