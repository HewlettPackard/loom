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

import com.hp.hpl.loom.exceptions.AttributeException;

public class GeoAttributeBuilderTest {

    /**
     * Test tries to call the build having not set any info
     */
    @Test
    public void buildWithNoDetails() {
        try {
            GeoAttribute.Builder builder = new GeoAttribute.Builder("Test");
            builder.build();
            // fail("it should have failed as min, max and unit aren't set");
        } catch (AttributeException ex) {
        }
    }

    /**
     * Test sets all the variables and confirms they are represented correctly by the Attribute
     * created on the build
     */
    @Test
    public void buildWithAllParams() throws AttributeException {
        Attribute.Builder builder = new GeoAttribute.Builder("Test").mappable(true).name("Name").plottable(true)
                .type(Attribute.TYPE_GEO).visible(true);
        GeoAttribute attribute = (GeoAttribute) builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Name", attribute.getName());
        assertEquals("geo", attribute.getType());
        assertTrue(attribute.getMappable());
        assertTrue(attribute.getPlottable());
        assertTrue(attribute.getVisible());
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(10, attribute.displayMap.size());
    }


}
