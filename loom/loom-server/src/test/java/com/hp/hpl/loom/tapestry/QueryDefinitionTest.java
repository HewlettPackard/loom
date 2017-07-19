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
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-test-full.xml")
public class QueryDefinitionTest {

    QueryDefinition query, diffOperation, chained, reverseChain;
    Session session1;
    List<Operation> groupByPipe;
    List<String> sources;

    @Autowired
    private SessionManager sessionManager;

    @Before
    public void setUp() throws Exception {

        session1 = new SessionImpl("testSession1", sessionManager.getInterval());
        new SessionImpl("testSession2", sessionManager.getInterval());

        String groupByOp = "GROUP_BY";
        String filterOp = "FILTER_BIGGER";

        String in1 = "/da/1";
        String in2 = "/da/2";

        Map<String, Object> groupByParams1 = new HashMap(1);
        groupByParams1.put("property", "rootUserName");
        groupByParams1.put("value", "Armstrong");
        sources = new ArrayList(2);
        sources.add(in1);
        sources.add(in2);


        Operation groupByOperation = new Operation(groupByOp, groupByParams1);
        groupByPipe = new ArrayList<Operation>(1);
        groupByPipe.add(groupByOperation);
        query = new QueryDefinition(groupByPipe, sources);

        Operation filterOperation = new Operation(filterOp, groupByParams1);
        List<Operation> filterPipe = new ArrayList<Operation>(1);
        filterPipe.add(filterOperation);
        diffOperation = new QueryDefinition(filterPipe, sources);

        Operation chainedFilterOperation = new Operation(filterOp, groupByParams1);
        List<Operation> chainedPipe = new ArrayList<Operation>(1);
        chainedPipe.add(groupByOperation);
        chainedPipe.add(chainedFilterOperation);
        chained = new QueryDefinition(chainedPipe, sources);

        Operation chainedGroupByOperation = new Operation(groupByOp, groupByParams1);
        List<Operation> reverseChainedPipe = new ArrayList<Operation>(1);
        reverseChainedPipe.add(filterOperation);
        reverseChainedPipe.add(chainedGroupByOperation);
        reverseChain = new QueryDefinition(reverseChainedPipe, sources);
    }

    @Test
    public void testConstructNoParams() {
        new QueryDefinition();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullPipe() {
        new QueryDefinition(null, sources);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructEmptyPipe() {
        new QueryDefinition(new ArrayList<Operation>(0), sources);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullSources() {
        new QueryDefinition(groupByPipe, null);
    }

    @Test
    public void testInspection() {
        assertTrue(query.getOperationPipeline().get(0).getOperator().equalsIgnoreCase("GROUP_BY"));
        assertTrue(query.getInputs().get(0).equalsIgnoreCase("/da/1"));
        assertTrue(chained.getOperationPipeline().get(1).getOperator().equalsIgnoreCase("FILTER_BIGGER"));

    }

    @Test
    public void testEquality() {

        assertFalse(query.equals(diffOperation));
        assertFalse(query.equals(chained));
        assertFalse(query.equals(reverseChain));
        assertFalse(diffOperation.equals(chained));
        assertFalse(diffOperation.equals(reverseChain));
        assertFalse(chained.equals(reverseChain));

    }



    @Test
    public void testHashing() {


        // diff name renders diff hash


        // diff pipe diff hash


        // same pipe, diff input -> diff hash



    }


    @Test
    public void testSerialisationFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writeValueAsString(query);
        mapper.writeValueAsString(diffOperation);
        mapper.writeValueAsString(chained);
        mapper.writeValueAsString(reverseChain);
    }

}
