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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OperationTest {

    Operation groupBy, sameGroupBy, differentParamsGroupBy, sortBy, combined;

    @Before
    public void setUp() throws Exception {
        String groupByOp = new String("GROUP_BY");
        String sameGroupByOp = new String("GROup_BY");
        String sortByOp = new String("SORT_BY");
        String combinedOp = new String("MERGE");

        String in1 = "/da/1";
        String in2 = "/da/2";

        Map<String, Object> groupByParams1 = new HashMap(1);
        Map<String, Object> groupByParams2 = new HashMap(1);
        Map<String, Object> sortByParams = new HashMap(1);

        groupByParams1.put("property", "rootName");
        groupByParams1.put("value", "Armstrong");
        groupByParams2.put("MAC", "DEADBEEF");
        sortByParams.put("rootUserName", "");
        List<String> sources = new ArrayList(2);
        sources.add(in1);
        sources.add(in2);
        sortByParams.put("sources", sources);

        groupBy = new Operation(groupByOp, groupByParams1);
        sameGroupBy = new Operation(sameGroupByOp, groupByParams1);

        differentParamsGroupBy = new Operation(groupByOp, groupByParams2);

        sortBy = new Operation(sortByOp, sortByParams);
        combined = new Operation(combinedOp, new HashMap<String, Object>());
    }

    @After
    public void shutDown() throws Exception {}

    @Test
    public void testConstructNoArgs() {
        new Operation();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullOperator() {
        new Operation(null, new HashMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullParameters() {
        new Operation(new String("TEST"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullOperator() {
        Operation op = new Operation();
        op.setOperator(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSetNullParameters() {
        Operation op = new Operation();
        op.setParameters(null);
    }

    @Test
    public void testInspection() {
        assertEquals("group_by", groupBy.getOperator().toLowerCase());
        assertEquals(true, groupBy.getParameters().containsKey("property"));
        assertEquals(true, groupBy.getParameters().containsValue("Armstrong"));

    }

    @Test
    public void testEquality() {

        assertEquals(true, groupBy.equals(groupBy));
        assertEquals(false, groupBy.equals(sameGroupBy));

        assertEquals(false, groupBy.equals(sortBy));
        assertEquals(false, sameGroupBy.equals(sortBy));

        assertEquals(false, groupBy.equals(differentParamsGroupBy));
        assertEquals(false, sameGroupBy.equals(differentParamsGroupBy));

        assertEquals(false, groupBy.equals(combined));
        assertEquals(false, sameGroupBy.equals(combined));

        assertEquals(false, differentParamsGroupBy.equals(sortBy));
        assertEquals(false, differentParamsGroupBy.equals(combined));

        assertEquals(false, sortBy.equals(combined));
    }

    @Test
    public void testSerialisationFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(groupBy);
        mapper.writeValueAsString(combined);
    }
}
