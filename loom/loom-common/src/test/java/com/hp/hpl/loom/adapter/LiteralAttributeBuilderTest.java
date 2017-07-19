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
package com.hp.hpl.loom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.hp.hpl.loom.exceptions.AttributeException;

public class LiteralAttributeBuilderTest {

    /**
     * Test tries to call the build having not set any info
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithNoDetails() throws AttributeException {
        LiteralAttribute.Builder builder = new LiteralAttribute.Builder("Test");
        LiteralAttribute attribute = builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Test", attribute.getName());
        assertEquals("literal", attribute.getType());
        assertFalse(attribute.getMappable());
        assertFalse(attribute.getPlottable());
        assertFalse(attribute.getVisible());
        assertNotNull(attribute.getRange());
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(8, attribute.displayMap.size());
    }

    /**
     * Test sets all the variables and confirms they are represented correctly by the Attribute
     * created on the build
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithAllParams() throws AttributeException {
        Map<String, String> allowedValues = new HashMap<>();
        allowedValues.put("key1", "val");
        allowedValues.put("key2", "val2");
        allowedValues.put("key3", "val3");

        LiteralAttribute.Builder builder = new LiteralAttribute.Builder("Test").mappable(true).name("Name")
                .plottable(true).type(Attribute.TYPE_LITERAL).visible(true).allowedValues(allowedValues);
        LiteralAttribute attribute = builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Name", attribute.getName());
        assertEquals("literal", attribute.getType());
        assertTrue(attribute.getMappable());
        assertTrue(attribute.getPlottable());
        assertTrue(attribute.getVisible());
        assertNotNull(attribute.getRange());
        assertEquals(3, attribute.getRange().size());
        assertTrue(attribute.getRange().containsKey("key1"));
        assertTrue(attribute.getRange().containsKey("key2"));
        assertTrue(attribute.getRange().containsKey("key3"));
        assertNotNull(attribute.getRange().get("key1"));
        assertNotNull(attribute.getRange().get("key2"));
        assertNotNull(attribute.getRange().get("key3"));
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(8, attribute.displayMap.size());
    }

    /**
     * Tests what happens if we have null allowed values created on the build
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithNullAllowedValues() throws AttributeException {
        LiteralAttribute.Builder builder = new LiteralAttribute.Builder("Test").mappable(true).name("Name")
                .plottable(true).type(Attribute.TYPE_LITERAL).visible(true).allowedValues(null);
        LiteralAttribute attribute = builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Name", attribute.getName());
        assertEquals("literal", attribute.getType());
        assertTrue(attribute.getMappable());
        assertTrue(attribute.getPlottable());
        assertTrue(attribute.getVisible());
        assertNotNull(attribute.getRange());
        assertEquals(0, attribute.getRange().size());
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(8, attribute.displayMap.size());
    }
}
