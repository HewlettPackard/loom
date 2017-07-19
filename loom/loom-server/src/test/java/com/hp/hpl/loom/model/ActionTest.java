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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;


public class ActionTest {

    Action action;
    ActionParameter param1;
    ActionParameters params;
    ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        Map<String, String> range = new HashMap<String, String>(1);
        range.put("min", "5");
        range.put("max", "15");
        param1 = new ActionParameter("delay", ActionParameter.Type.NUMBER, "delay", range);
        params = new ActionParameters();
        params.add(param1);
        mapper = new ObjectMapper();
    }

    @Test
    public void testConstructorNoArgs() {
        action = new Action();
    }

    @Test
    public void testBasicConstruction() throws InvalidActionSpecificationException {
        action = new Action("reboot", "reboot", "power cycle a machine", "", params);
    }

    @Test(expected = InvalidActionSpecificationException.class)
    public void testNoAdapterIdForTheAction() throws InvalidActionSpecificationException {
        action = new Action("", "reboot", "power cycle a machine", null, params);
    }

    @Test
    public void testInspection() throws InvalidActionSpecificationException, JsonProcessingException {
        action = new Action("reboot", "", "power cycle a machine", "", params);
        assertEquals("reboot", action.getId());
        action.setId("fake");
        assertEquals("fake", action.getId());
        assertEquals("", action.getName());
        action.setName("test");
        assertEquals("test", action.getName());
        assertEquals("power cycle a machine", action.getDescription());
        assertEquals("", action.getIcon());
    }

    @Test
    public void testSerialisation() throws InvalidActionSpecificationException, JsonProcessingException {

        action = new Action("reboot", "", "power cycle a machine", "", params);

        List<String> ins = new ArrayList<String>(2);
        ins.add("/os/ga1/1");
        ins.add("/os/ga1/30");
        action.setTargets(ins);
        assertNotNull(mapper.writeValueAsString(action));
    }

    @Test
    public void testDeserialisation() throws IOException {
        String format = "{\"name\":\"reboot\",\"targets\":[\"/os/ga1/1\",\"/os/ga1/30\"]}";
        assertNotNull(mapper.readValue(format, Action.class));
    }



}
