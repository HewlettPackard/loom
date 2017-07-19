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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-test-full.xml")
public class ThreadDefinitionTest {

    QueryDefinition query, query2;

    String itemType;
    ThreadDefinition threadDefinition, difIdThreadDef, diffQuery;

    @Autowired
    private SessionManager sessionManager;

    @Before
    public void setUp() throws Exception {
        new SessionImpl("testSession1", sessionManager.getInterval());
        new SessionImpl("testSession2", sessionManager.getInterval());
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
        query = new QueryDefinition(groupByPipe, sources);
        query2 = new QueryDefinition(groupByPipe, sources);
        itemType = "/os/instances";
        threadDefinition = new ThreadDefinition("0", itemType, query);
        difIdThreadDef = new ThreadDefinition("1", itemType, query);
        diffQuery = new ThreadDefinition("0", itemType, query2);
    }

    @Test
    public void testConstructNoParams() {
        new ThreadDefinition();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullThreadId() {
        new ThreadDefinition(null, itemType, query);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullItemType() {
        new ThreadDefinition("0", null, query);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullQueryDef() {
        new ThreadDefinition("0", itemType, null);
    }

    @Test
    public void testInspection() {
        assertTrue(threadDefinition.getAggregation().equals("/da/1"));
        assertTrue(threadDefinition.getId().equals("0"));
        threadDefinition.setId("1");
        assertTrue(threadDefinition.getId().equals("1"));
        assertTrue(threadDefinition.getItemType().equals(itemType));
        assertTrue(threadDefinition.getQuery().equals(query));
    }

    @Test
    public void testEquality() {
        assertTrue(threadDefinition.equals(diffQuery));
        assertFalse(threadDefinition.equals(difIdThreadDef));
        assertFalse(difIdThreadDef.equals(diffQuery));
    }

    @Test
    public void testSerialisationFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(threadDefinition);
    }

}
