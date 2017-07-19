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
package com.hp.hpl.loom.manager.adapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.os.DoNothingAdapter;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.query.OperationManager;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

/**
 * Tests that this adapter is registered to the adapterManager successfully
 */
public class ItemAttributesAnnotationsTest {

    private static final Log LOG = LogFactory.getLog(ItemAttributesAnnotationsTest.class);

    OperationManager operationManager;
    ItemTypeManager itemTypeManager;
    TapestryManager tapestryManager;
    Provider provider;
    DoNothingAdapter adapter;
    ArrayList<String> attrNames;


    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        operationManager = mock(OperationManager.class);
        itemTypeManager = mock(ItemTypeManager.class);
        tapestryManager = mock(TapestryManager.class);
        provider = mock(Provider.class);
        when(provider.getProviderType()).thenReturn("donothing");
        adapter = mock(DoNothingAdapter.class);
        when(adapter.getProvider()).thenReturn(provider);
        when(adapter.registerQueryOperations(null)).thenReturn(new HashMap<String, QuadFunctionMeta>());
        attrNames = new ArrayList<>();
        attrNames.add("core.attributeName");
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
    }

    @Test
    public void testItemOne() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        testTypes(TestItemOne.class, "testItemOne", attrNames, false);
    }

    @Test
    public void testItemTwo() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        testTypes(TestItemTwo.class, "testItemTwo", attrNames, false);
    }

    @Test
    public void testItemThree() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        testTypes(TestItemThree.class, "testItemThree", attrNames, false);
    }

    @Test
    public void testItemFour() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        testTypes(TestItemFour.class, "testItemFour", attrNames, false);
    }

    @Test
    public void testItemFive() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        attrNames.add("core.attributeNameTwo");
        testTypes(TestItemFive.class, "testItemFive", attrNames, false);
    }

    @Test
    public void testItemSix() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        attrNames.add("core.attributeNameTwo");
        attrNames.add("core.attributeNameThree");
        testTypes(TestItemSix.class, "testItemSix", attrNames, false);
    }

    @Test
    public void testItemSeven() throws DuplicateAdapterException, DuplicateItemTypeException, JsonProcessingException,
            NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        testTypes(TestItemSeven.class, "testItemSeven", attrNames, true);
    }

    private void testTypes(final Class<?> itemTypeClass, final String itemTypeLocalId, final List<String> attrNames,
            final boolean displayJson) throws DuplicateAdapterException, DuplicateItemTypeException,
            JsonProcessingException, NoSuchProviderException, NullItemTypeIdException, DuplicatePatternException,
            NullPatternIdException, UnsupportedOperationException {
        List<Class> items = new ArrayList<Class>();
        items.add(itemTypeClass);

        when(adapter.getAnnotatedItemsClasses()).thenReturn(items);

        AdapterManagerImpl adapterManager = new AdapterManagerImpl();
        adapterManager.opManager = operationManager;
        adapterManager.itemTypeManager = itemTypeManager;
        adapterManager.tapestryManager = tapestryManager;
        adapterManager.registerAdapter(adapter);

        ArgumentCaptor<ItemType> argument = ArgumentCaptor.forClass(ItemType.class);
        Mockito.verify(itemTypeManager, times(2)).addItemType(Matchers.any(), argument.capture());
        List<ItemType> capturedTypes = argument.getAllValues();
        boolean found = false;
        for (ItemType itemType : capturedTypes) {
            if (("donothing-" + itemTypeLocalId).equals(itemType.getId())) {
                for (String attrName : attrNames) {
                    assertNotNull(itemType.getAttributes().get(attrName));
                    found = true;
                }

                if (displayJson) {
                    LOG.debug(toJson(itemType));
                }
            }
        }
        if (!found) {
            fail("Expecting: donothing-" + itemTypeLocalId);
        }
        // assertEquals("donothing-" + itemTypeLocalId, testType.getId());

    }

    // test json serialization
    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }
}
