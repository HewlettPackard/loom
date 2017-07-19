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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hp.hpl.loom.adapter.Attribute.Builder;
import com.hp.hpl.loom.exceptions.AttributeException;

public class AttributeBuilderTest {

    /**
     * Test tries to call the build having not set any info
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithNoDetails() throws AttributeException {
        Builder builder = new Attribute.Builder("Test");
        Attribute attribute = builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Test", attribute.getName());
        assertEquals("literal", attribute.getType());
        assertFalse(attribute.getMappable());
        assertFalse(attribute.getPlottable());
        assertFalse(attribute.getVisible());
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(7, attribute.displayMap.size());
    }

    /**
     * Test sets all the variables and confirms they are represented correctly by the Attribute
     * created on the build
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithAllParams() throws AttributeException {
        Builder builder = new Attribute.Builder("Test").mappable(true).name("Name").plottable(true)
                .type(Attribute.TYPE_LITERAL).visible(true);
        Attribute attribute = builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Name", attribute.getName());
        assertEquals("literal", attribute.getType());
        assertTrue(attribute.getMappable());
        assertTrue(attribute.getPlottable());
        assertTrue(attribute.getVisible());
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(7, attribute.displayMap.size());
    }
}
