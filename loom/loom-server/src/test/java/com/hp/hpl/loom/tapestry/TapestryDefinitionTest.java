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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;

public class TapestryDefinitionTest {

    List<ThreadDefinition> threads1, threads2;

    TapestryDefinition tap1, tap2;

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

        tap1 = new TapestryDefinition("1", threads1);
        tap2 = new TapestryDefinition("2", threads2);
    }

    @Test
    public void testConstructNoArgs() {
        new TapestryDefinition();
    }

    @Test
    public void testConstructNoId() {
        new TapestryDefinition("", threads1);
    }

    @Test
    public void testConstructNoThreads() {
        new TapestryDefinition("1", new ArrayList<ThreadDefinition>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullThreads() {
        new TapestryDefinition("1", null);
    }

    @Test
    public void testException() {
        try {
            tap1.getThreadDefinition("2");
        } catch (NoSuchThreadDefinitionException e) {
            assertTrue(true);
            return;
        }
        fail();
    }

    @Test
    public void testInspection() {
        assertTrue(tap1.getId().equals("1"));
        List<ThreadDefinition> threads = tap1.getThreads();
        assertTrue(threads.equals(threads1));

        tap1.setId("0");
        assertTrue(tap1.getId().equals("0"));

        tap1.setThreads(threads2);
        threads = tap1.getThreads();
        assertTrue(threads.equals(threads2));

    }

    @Test
    public void testEquality() {
        assertFalse(tap1.equals(tap2));

        tap1.setThreads(threads2);
        tap1.setId("2");

        assertTrue(tap1.equals(tap2));
    }

    @Test
    public void testSerialisationFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writeValueAsString(tap2);
    }

}
