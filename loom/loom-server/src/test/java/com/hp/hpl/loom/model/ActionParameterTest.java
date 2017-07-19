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


import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;

public class ActionParameterTest {

    ActionParameter param1;

    ObjectMapper mapper;

    Map<String, String> range;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void testEmptyConstructor() {
        param1 = new ActionParameter();
    }

    @Test
    public void testConstructParam() throws InvalidActionSpecificationException {
        range = new HashMap<String, String>(1);
        range.put("min", "5");
        range.put("max", "15");
        param1 = new ActionParameter("delay", ActionParameter.Type.NUMBER, "delay", range);
    }

    @Test(expected = InvalidActionSpecificationException.class)
    public void testInvalidId() throws InvalidActionSpecificationException {
        range = new HashMap<String, String>(1);
        range.put("min", "5");
        range.put("max", "15");
        param1 = new ActionParameter("", ActionParameter.Type.NUMBER, "delay", range);
    }

    @Test
    public void testInspection() throws InvalidActionSpecificationException {
        range = new HashMap<String, String>(1);
        range.put("min", "5");
        range.put("max", "15");
        param1 = new ActionParameter("delay", ActionParameter.Type.NUMBER, "delay", range);

        assertEquals("delay", param1.getId());
        param1.setId("changed");
        assertEquals("changed", param1.getId());
        assertEquals("delay", param1.getName());
        param1.setName("changed");
        assertEquals("changed", param1.getName());

        assertEquals(ActionParameter.Type.NUMBER, param1.getType());
        param1.setType(ActionParameter.Type.STRING);
        assertEquals(ActionParameter.Type.STRING, param1.getType());

    }

    @Test
    public void testSerialisation() throws InvalidActionSpecificationException, JsonProcessingException {

        range = new HashMap<String, String>(1);
        range.put("min", "5");
        range.put("max", "15");
        param1 = new ActionParameter("delay", ActionParameter.Type.NUMBER, "delay", range);
        assertNotNull(mapper.writeValueAsString(param1));

    }

    @Test
    public void testDeserialisation() throws IOException {
        String format = "{\"type\":\"NUMBER\",\"id\":\"delay\",\"name\":\"delay\",\"value\":25}";
        ActionParameter par = mapper.readValue(format, ActionParameter.class);
        assertEquals("25", par.getValue());
    }

}
