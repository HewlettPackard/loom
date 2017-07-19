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
package com.hp.hpl.loom.tapestry;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.FakeType;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;

public class MetaTest {
    Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");
    Map<String, ItemType> itemSet1;
    Meta meta;
    ItemType itemType = new OsInstanceType(provider);
    ItemType fakeType = new FakeType();

    @Before
    public void setUp() throws Exception {

        itemSet1 = new HashMap<String, ItemType>(1);
        itemSet1.put("/os/instances", itemType);
        meta = new Meta(itemSet1);
    }

    @Test
    public void testConstructNoArgs() {
        new Meta();
    }

    @Test
    public void testInspection() {

        Map<String, ItemType> itemSet2 = new HashMap<String, ItemType>(2);
        itemSet2.put("/os/instances", itemType);
        itemSet2.put("/test/fake", fakeType);

        assertEquals(1, meta.getItemTypes().size());
        assertEquals(itemType, meta.getItemTypes().get("/os/instances"));

        meta.setItemTypes(itemSet2);
        assertEquals(2, meta.getItemTypes().size());
        assertEquals(fakeType, meta.getItemTypes().get("/test/fake"));

    }

}
