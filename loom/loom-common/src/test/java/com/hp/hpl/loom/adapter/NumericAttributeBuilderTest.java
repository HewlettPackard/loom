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
import static org.junit.Assert.fail;

import org.junit.Test;

import com.hp.hpl.loom.exceptions.AttributeException;

public class NumericAttributeBuilderTest {

    /**
     * Test tries to call the build having not set any info
     */
    @Test
    public void buildWithNoDetails() {
        try {
            NumericAttribute.Builder builder = new NumericAttribute.Builder("Test");
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
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").min("0").max("100").unit("%")
                .mappable(true).name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        NumericAttribute attribute = builder.build();
        assertEquals("Test", attribute.getFieldName());
        assertEquals("Name", attribute.getName());
        assertEquals("numeric", attribute.getType());
        assertTrue(attribute.getMappable());
        assertTrue(attribute.getPlottable());
        assertTrue(attribute.getVisible());
        assertNotNull(attribute.getMin());
        assertNotNull(attribute.getMax());
        assertNotNull(attribute.getUnit());
        assertEquals("NONE", attribute.getCollectionType());
        assertFalse(attribute.getIgnoreUpdate());
        assertEquals(10, attribute.displayMap.size());
    }

    /**
     * Tests what happens if we have null/Empty for the max
     */
    @Test
    public void buildWithNullEmptyMax() {
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").min("0").unit("%").mappable(true)
                .name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        try {
            builder.build();
            fail("it should have failed as max isn't set");
        } catch (AttributeException ex) {
        }
    }

    /**
     * Tests what happens if we have -Inf and Inf
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithInfForMinAndMax() throws AttributeException {
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").min("-Inf").max("Inf").unit("%")
                .mappable(true).name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        builder.build();

    }


    /**
     * Tests what happens if we have null/Empty for the min
     */
    @Test
    public void buildWithNullEmptyMin() {
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").max("100").unit("%").mappable(true)
                .name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        try {
            builder.build();
            fail("it should have failed as min isn't set");
        } catch (AttributeException ex) {
        }
    }

    /**
     * Tests what happens if we have null/Empty for the unit
     *
     * @throws AttributeException
     */
    @Test
    public void buildWithNullEmptyUnit() throws AttributeException {
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").min("0").max("100").mappable(true)
                .name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);

        builder.build();
    }

    /**
     * Tests what happens if we have non numeric for max/min
     */
    @Test
    public void buildWithNonNumericForMaxMin() {
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").min("Test").max("100").unit("%")
                .mappable(true).name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        try {
            builder.build();
            fail("it should have failed as min has a non number");
        } catch (AttributeException ex) {
        }

        builder = new NumericAttribute.Builder("Test").min("0").max("Test").unit("%").mappable(true).name("Name")
                .plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        try {
            builder.build();
            fail("it should have failed as max has a non number");
        } catch (AttributeException ex) {
        }
    }

    /**
     * Tests what happens if we have min greater than max
     */
    @Test
    public void buildWithMinGreaterMax() {
        NumericAttribute.Builder builder = new NumericAttribute.Builder("Test").min("101").max("0").unit("%")
                .mappable(true).name("Name").plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        try {
            builder.build();
            fail("it should have failed as min is greater than max");
        } catch (AttributeException ex) {
        }

        builder = new NumericAttribute.Builder("Test").min("101.02").max("-1.10").unit("%").mappable(true).name("Name")
                .plottable(true).type(Attribute.TYPE_NUMERIC).visible(true);
        try {
            builder.build();
            fail("it should have failed as min is greater than max");
        } catch (AttributeException ex) {
        }
    }
}
