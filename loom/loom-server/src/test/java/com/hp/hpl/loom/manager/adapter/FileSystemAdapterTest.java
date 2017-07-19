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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.adapter.file.FileItem;
import com.hp.hpl.loom.adapter.file.FileSystemAdapter;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManagerImpl;
import com.hp.hpl.loom.manager.query.OperationManager;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

/**
 * Tests that this adapter is registered to the adapterManager successfully
 */
public class FileSystemAdapterTest {

    private static final Log LOG = LogFactory.getLog(FileSystemAdapterTest.class);

    @Test
    public void testAddItemTypeActionValidation() throws DuplicateAdapterException, NoSuchProviderException,
            DuplicateItemTypeException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException {
        ItemType fileType = setupItemType();

        ItemTypeManagerImpl itemTypeManagerImpl = new ItemTypeManagerImpl();
        Provider provider = mock(Provider.class);
        itemTypeManagerImpl.addItemType(provider, fileType);
        // try and register the same itemtype with the same action
        try {
            itemTypeManagerImpl.addItemType(provider, fileType);
            fail("Expecting an error");
        } catch (IllegalArgumentException ex) {

        }
    }

    @Test
    public void testRegisteringAdapterBasedOnAnnotations()
            throws DuplicateAdapterException, DuplicateItemTypeException, NoSuchProviderException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {

        ItemType fileType = setupItemType();

        assertEquals("file-file", fileType.getId());

        try {
            LOG.info("ItemType content: " + toJson(fileType));
        } catch (Exception e) {
            LOG.error(e);
        }

        // check we have all the ones we expect
        assertNotNull(fileType.getAttributes().get("core.path"));
        assertNotNull(fileType.getAttributes().get("core.filename"));
        assertNotNull(fileType.getAttributes().get("core.size"));

        assertTrue((Boolean) fileType.getAttributes().get("core.path").get("visible"));
        assertTrue((Boolean) fileType.getAttributes().get("core.filename").get("visible"));
        assertTrue((Boolean) fileType.getAttributes().get("core.size").get("visible"));

        // extra checks for the Numeric Attribute
        HashMap numAttr = (HashMap) fileType.getAttributes().get("core.size");
        assertEquals("Bytes", numAttr.get("unit"));
        assertEquals("0", numAttr.get("min"));
        assertEquals("1000000000000", numAttr.get("max"));
        assertEquals(true, numAttr.get("plottable"));
        assertEquals("numeric", numAttr.get("type"));

        // extra checks for the Literal Attribute
        // @LoomAttribute(label = "readonly", supportedOperations = {DefaultOperations.SORT_BY},
        // plottable = false,
        // type = LiteralAttribute.class, ranges = {@LiteralRange(key = "true", name = "True"),
        // @LiteralRange(key = "false", name = "False")})

        HashMap readonlyAttr = (HashMap) fileType.getAttributes().get("core.readonly");

        HashMap range = (HashMap) readonlyAttr.get("range");
        assertNotNull(range.get("true"));
        assertNotNull(range.get("false"));


        assertNotNull(fileType.getAttributes().get("core.readonly"));
        assertNotNull(fileType.getAttributes().get("alertLevel"));

        assertNotNull(fileType.getAttributes().get("file-dir:file-file:contains"));
        assertNotNull(fileType.getAttributes().get("file-dir:file-file:ancestor"));
        assertNotNull(fileType.getAttributes().get("fullyQualifiedName"));
        assertNotNull(fileType.getAttributes().get("alertDescription"));

        assertNotNull(fileType.getOperations().get("/loom/loom/GROUP_BY"));
        assertNotNull(fileType.getOperations().get("/loom/loom/SORT_BY"));

        List<String> list = Arrays.asList(new String[] {"fullyQualifiedName", "core.filename", "core.path", "core.size",
                "core.sizes", "core.sizesList", "core.value", "core.readonly", "core.latitude", "core.longitude",
                "core.created", "core.updated", "core.country", "core.itemName", "core.itemDescription", "core.itemId",
                "file-dir:file-file:contains", "file-dir:file-file:ancestor"});

        assertEquals(list, fileType.getOrderedAttributes());

        assertNotNull(fileType.getActions().get("item"));

        // check the order of the operations
        Set<OrderedString> sortBy = fileType.getOperations().get("/loom/loom/SORT_BY");

        Map<String, Integer> keyToOrder = sortBy.stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getOrder()));

        LOG.info("Display keyToOrder: " + keyToOrder);

        assertEquals(0, (int) keyToOrder.get("core.size"));
        assertEquals(1, (int) keyToOrder.get("core.path"));
        assertEquals(2, (int) keyToOrder.get("core.filename"));
        assertEquals(3, (int) keyToOrder.get("file-dir:file-file:contains"));
        assertEquals(2147483647, (int) keyToOrder.get("core.readonly"));
    }

    private ItemType setupItemType() throws DuplicateAdapterException, NoSuchProviderException,
            DuplicateItemTypeException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException {
        OperationManager operationManager = mock(OperationManager.class);
        ItemTypeManager itemTypeManager = mock(ItemTypeManager.class);
        TapestryManager tapestryManager = mock(TapestryManager.class);
        Provider provider = mock(Provider.class);
        when(provider.getProviderType()).thenReturn("file");
        FileSystemAdapter adapter = mock(FileSystemAdapter.class);
        when(adapter.getProvider()).thenReturn(provider);
        when(adapter.registerQueryOperations(null)).thenReturn(new HashMap<String, QuadFunctionMeta>());

        List<Class> items = new ArrayList<Class>();
        items.add(FileItem.class);

        when(adapter.getAnnotatedItemsClasses()).thenReturn(items);

        AdapterManagerImpl adapterManager = new AdapterManagerImpl();
        adapterManager.opManager = operationManager;
        adapterManager.itemTypeManager = itemTypeManager;
        adapterManager.tapestryManager = tapestryManager;
        adapterManager.registerAdapter(adapter);

        ArgumentCaptor<ItemType> argument = ArgumentCaptor.forClass(ItemType.class);
        Mockito.verify(itemTypeManager, times(2)).addItemType(Matchers.any(), argument.capture());
        List<ItemType> capturedPeople = argument.getAllValues();
        ItemType testType = capturedPeople.get(0);



        return testType;
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
