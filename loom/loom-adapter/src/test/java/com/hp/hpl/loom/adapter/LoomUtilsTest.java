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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.tapestry.PatternDefinition;

public class LoomUtilsTest {



    private Provider provider = new ProviderImpl("providerType", "providerId", "authEndpoint", "providerName", "com");
    private ItemType itemType = new ItemType("localId");
    private Aggregation aggregation = new Aggregation("logicalId", "typeId", "name", "description", 10);
    private PatternDefinition patternDefinition = new PatternDefinition("anId");

    private static final Log LOG = LogFactory.getLog(LoomUtilsTest.class);


    /**
     * This test confirms that you can't construct the Utils.
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void checkConstructor() throws InstantiationException, IllegalAccessException {
        // the LoomUtils is private but you can still try and construct it using reflection so test
        // for that.
        try {
            Constructor<?> constructor = LoomUtils.class.getDeclaredConstructor();
            // constructor private? no problem... just make it not private!
            constructor.setAccessible(true); // muhahahaha
            constructor.newInstance();
            fail("It should have throw a InvocationTargetException");
        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
            fail("The exception is wrong - it should have been InvocationTargetException");
        } catch (InvocationTargetException ex) {
        }
    }


    /**
     * This test confirms that it sets the pattern id properly
     */
    @Test
    public void setPatternId() {
        try {
            LoomUtils.setPatternId(provider, patternDefinition);
            assertNotNull(patternDefinition.getId());
            assertEquals("providerType-anId", patternDefinition.getId());
        } catch (NullPatternIdException e) {
            fail("Throw in error");
        }
    }

    /**
     * This test confirms that it sets the pattern id with null provider / pattern / pattern id
     */
    @Test
    public void setPatternIdError() {
        try {
            LoomUtils.setPatternId(null, null);
            fail("Should have thrown an error");
        } catch (IllegalArgumentException e) {
        } catch (NullPatternIdException e) {
            fail("Should have thrown this error");
        }
        try {
            LoomUtils.setPatternId(provider, null);
            fail("Should have thrown an error");
        } catch (IllegalArgumentException e) {
        } catch (NullPatternIdException e) {
            fail("It should have throw a NullItemTypeIdException");
        }

        try {
            PatternDefinition patternDefinitionNullId = new PatternDefinition();
            LoomUtils.setPatternId(provider, patternDefinitionNullId);
            fail("Should have thrown an error");
        } catch (NullPatternIdException e) {
        }
    }

    /**
     * This test confirms that it unsets the pattern id properly
     */
    @Test
    public void unsetPatternId() {
        setPatternId();
        LoomUtils.unsetPatternId(provider, patternDefinition);
        LoomUtils.unsetPatternId(provider, patternDefinition); // double check the unset method
                                                               // doesn't break when run a second
                                                               // time
        assertEquals("anId", patternDefinition.getId());
    }

    /**
     * This test confirms that it unsets the pattern id with null provider / pattern / pattern id
     */
    @Test
    public void unsetPatternIdError() {
        setPatternId();
        try {
            LoomUtils.unsetPatternId(null, null);
            fail("Should have thrown an error");
        } catch (IllegalArgumentException e) {
        }
        try {
            LoomUtils.setPatternId(provider, null);
            fail("Should have thrown an error");
        } catch (IllegalArgumentException e) {
        } catch (NullPatternIdException e) {
            fail("It should have throw a NullItemTypeIdException");
        }

        try {
            PatternDefinition patternDefinitionNullId = new PatternDefinition();
            LoomUtils.setPatternId(provider, patternDefinitionNullId);
            fail("Should have thrown an error");
        } catch (NullPatternIdException e) {
        }
    }

    /**
     * This test confirms that it sets the itemType id properly
     */
    @Test
    public void setId() {
        try {
            LoomUtils.setId(provider, itemType);
            assertNotNull(itemType.getId());
            assertEquals("providerType-localId", itemType.getId());
        } catch (NullItemTypeIdException e) {
            fail("Throw in error");
        }
    }

    /**
     * This test confirms that is errors correctly with null provider / itemType / itemtype id
     */
    @Test
    public void setIdError() {
        try {
            LoomUtils.setId(null, null);
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (NullItemTypeIdException e) {
            fail("It should have throw a different error");
        }

        try {
            LoomUtils.setId(provider, null);
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (NullItemTypeIdException e) {
            fail("Throw in error");
        }

        try {
            ItemType itemTypeNullId = new ItemType();
            LoomUtils.setId(provider, itemTypeNullId);
            fail("It should have throw a NullItemTypeIdException");
        } catch (NullItemTypeIdException e) {
        }
    }

    /**
     * This test checks the itemType id helper
     */
    @Test
    public void getItemTypeId() {
        String id = LoomUtils.getItemTypeId(provider, "itemTypeLocalId");
        assertEquals("providerType-itemTypeLocalId", id);
    }

    /**
     * This test checks the itemType id with a null provider / itemTypeLocalId
     */
    @Test
    public void getItemTypeIdError() {
        try {
            LoomUtils.getItemTypeId(null, "itemTypeLocalId");
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * This test checks the MergedLogicalId helper
     */
    @Test
    public void getMergedLogicalIdFromItemType() {
        String id = LoomUtils.getMergedLogicalIdFromItemType(provider, "itemTypeLocalId", "localId");
        assertEquals("providerType/itemTypeLocalIds/localId", id);

        String id2 = LoomUtils.getMergedLogicalIdFromItemType(provider, "itemTypeLocalId", null);
        assertEquals("providerType/itemTypeLocalIds", id2);
    }

    /**
     * This test checks the MergedLogicalId with a null provider / itemTypeLocalId / localId
     */
    @Test
    public void getMergedLogicalIdFromItemTypeError() {
        try {
            LoomUtils.getMergedLogicalIdFromItemType(null, "itemTypeLocalId", "localId");
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            LoomUtils.getMergedLogicalIdFromItemType(provider, null, "localId");
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * This test checks the ItemLogicalId helper
     */
    @Test
    public void getItemLogicalId() {
        String id = LoomUtils.getItemLogicalId(aggregation, "itemId");
        assertEquals("logicalId/itemId", id);
    }

    /**
     * This test checks the ItemLogicalId with a null aggregation / itemId
     */
    @Test
    public void getItemLogicalIdError() {
        try {
            LoomUtils.getItemLogicalId(null, "itemId");
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            LoomUtils.getItemLogicalId(aggregation, null);
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * This test checks the deriveAggregation LogicalId helper
     */
    @Test
    public void deriveAggregationLogicalIdFromItemLogicalId() {
        String itemLogicalId = LoomUtils.getItemLogicalId(aggregation, "itemId");
        String id = LoomUtils.deriveAggregationLogicalIdFromItemLogicalId(itemLogicalId);
        assertEquals("logicalId", id);
    }

    /**
     * This test checks the deriveAggregation LogicalId with a null itemLogicalId
     */
    @Test
    public void deriveAggregationLogicalIdFromItemLogicalIdError() {
        try {
            LoomUtils.getItemLogicalId(null, "itemId");
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            LoomUtils.getItemLogicalId(aggregation, null);
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * This test checks the AggregationLogicalId helper
     */
    @Test
    public void getAggregationLogicalIdFromItemType() {
        String itemLogicalId = LoomUtils.getAggregationLogicalIdFromItemType(provider, "itemTypeLocalId", null);
        assertEquals("providerType/providerId/itemTypeLocalIds", itemLogicalId);

        String itemLogicalId2 = LoomUtils.getAggregationLogicalIdFromItemType(provider, "itemTypeLocalId", "localId");
        assertEquals("providerType/providerId/itemTypeLocalIds/localId", itemLogicalId2);
    }

    /**
     * This test checks the AggregationLogicalId with a null itemLogicalId
     */
    @Test
    public void getAggregationLogicalIdFromItemTypeError() {
        try {
            LoomUtils.getAggregationLogicalIdFromItemType(null, "itemTypeLocalId", null);
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            LoomUtils.getAggregationLogicalIdFromItemType(provider, null, null);
            fail("It should have throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
