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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.os.FakeType;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;

public class PatternDefinitionTest {
    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");
    PatternDefinition basic, differentThreads, differentProvider;
    List<ThreadDefinition> threads1, threads2;
    Map<String, ItemType> itemSet1, itemSet2;

    Meta meta1, meta2;

    @Before
    public void setUp() throws Exception {

        threads1 = new ArrayList<ThreadDefinition>(1);
        threads2 = new ArrayList<ThreadDefinition>(2);
        new ThreadDefinition();

        String groupByOp = "GROUP_BY";
        String in1 = "/da/1";

        Map<String, Object> groupByParams1 = new HashMap(1);
        groupByParams1.put("property", "rootUserName");
        groupByParams1.put("value", "Armstrong");
        List<String> sources = new ArrayList<String>(1);
        sources.add(in1);
        Operation groupByOperation = new Operation(groupByOp, groupByParams1);
        List<Operation> groupByPipe = new ArrayList<Operation>(1);
        groupByPipe.add(groupByOperation);
        QueryDefinition query = new QueryDefinition(groupByPipe, sources);
        QueryDefinition query2 = new QueryDefinition(groupByPipe, sources);

        ThreadDefinition threadDefinition = new ThreadDefinition("0", "/os/instances", query);
        ThreadDefinition difIdThreadDef = new ThreadDefinition("1", "/os/instances", query);
        ThreadDefinition diffQuery = new ThreadDefinition("2", "/os/volumes", query2);

        threads1.add(threadDefinition);
        threads2.add(difIdThreadDef);
        threads2.add(diffQuery);

        ItemType itemType = new OsInstanceType(provider);
        ItemType fakeType = new FakeType();

        itemSet1 = new HashMap<String, ItemType>(1);
        itemSet1.put("/os/instances", itemType);

        meta1 = new Meta(itemSet1);

        itemSet2 = new HashMap<String, ItemType>(2);
        itemSet2.put("/os/instances", itemType);
        itemSet2.put("/test/fake", fakeType);
        meta2 = new Meta(itemSet2);

        basic = new PatternDefinition("0", threads1, "os", meta1);
        differentProvider = new PatternDefinition("0", threads1, "hpcs", meta1);
        differentThreads = new PatternDefinition("1", threads2, "os", meta2);

    }

    @After
    public void shutDown() throws Exception {}

    @Test
    public void testConstructNoArgs() {
        new PatternDefinition();
    }

    @Test
    public void testConstructWithId() {
        PatternDefinition pattern = new PatternDefinition("anid");

        assertEquals("anid", pattern.getId());
    }

    @Test
    public void testConstructNamed() {
        new PatternDefinition("0", threads1, "os", meta1, "testName");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConstructNoId() {
        new PatternDefinition("", threads1, "os", meta1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullId() {
        new PatternDefinition(null, threads1, "os", meta1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNoThreads() {
        new PatternDefinition("0", new ArrayList<ThreadDefinition>(), "os", meta1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullThreads() {
        new PatternDefinition("0", null, "os", meta1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNoProvider() {
        new PatternDefinition("0", threads1, "", meta1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullProvider() {
        new PatternDefinition("0", threads1, null, meta1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullSet() {
        new PatternDefinition("0", threads1, "os", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructDifferentSizeThanThreadSet() {
        new PatternDefinition("0", threads1, "os", meta2);
    }

    @Test
    public void testInspection() {
        assertTrue(basic.getId().equals("0"));
        assertTrue(basic.getName() == null);
        assertTrue(basic.getProviderType().equals("os"));
        assertTrue(basic.getThreads().size() == 1);
        assertTrue(basic.get_meta().getItemTypes().size() == 1);

        basic.set_meta(meta2);
        assertTrue(basic.get_meta().getItemTypes().size() == 2);

        basic.setThreads(threads2);
        assertTrue(basic.getThreads().size() == 2);

    }

    @Test
    public void testEquality() {
        assertFalse(basic.equals(differentProvider));
        assertFalse(basic.equals(differentThreads));
        assertFalse(differentProvider.equals(differentThreads));
    }

    @Test
    public void testSerialisationFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writeValueAsString(differentThreads);

    }

}
