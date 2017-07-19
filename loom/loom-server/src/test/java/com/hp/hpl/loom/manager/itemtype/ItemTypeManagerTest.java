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
package com.hp.hpl.loom.manager.itemtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testTapestryManager.xml")
public class ItemTypeManagerTest {
    private static final Log LOG = LogFactory.getLog(ItemTypeManagerTest.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private ItemTypeManagerImpl itemTypeManager;

    @After
    public void shutDown() throws Exception {
        if (itemTypeManager != null) {
            itemTypeManager.removeAllItemTypes();
        }
    }

    // ----------------------------------------------------------------------------------------------------------
    // ItemType Tests
    // ----------------------------------------------------------------------------------------------------------

    @Test
    public void testAddItemType() throws NoSuchProviderException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType("anid");

        String itemTypeId = itemTypeManager.addItemType(provider, itemType);

        assertNotNull(itemTypeId);
        assertEquals("anid", itemType.getId());

        Collection<ItemType> itemTypes = itemTypeManager.getItemTypes(provider);

        assertNotNull(itemTypes);
        assertEquals(1, itemTypes.size());
        assertTrue(itemTypes.contains(itemType));

        ItemType storedItemType = itemTypes.iterator().next();

        assertNotNull(storedItemType.getId());
        assertTrue(storedItemType.equals(itemType));

        assertEquals(itemType, itemTypeManager.getItemType(itemTypeId));

        Provider provider2 = new ProviderImpl("type2", "id2", "uri", "name", "com");
        ItemType itemType2 = createItemType("anid2");
        String itemTypeId2 = itemTypeManager.addItemType(provider2, itemType2);
        assertNotNull(itemTypeId2);
        assertEquals("anid2", itemType2.getId());

        itemTypes = itemTypeManager.getItemTypes(provider);
        assertNotNull(itemTypes);
        assertEquals(1, itemTypes.size());
        itemTypes = itemTypeManager.getItemTypes(provider2);
        assertNotNull(itemTypes);
        assertEquals(1, itemTypes.size());
        itemTypes = itemTypeManager.getItemTypes();
        assertNotNull(itemTypes);
        assertEquals(2, itemTypes.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddItemTypeNullProvider() throws DuplicateItemTypeException, NoSuchProviderException {
        itemTypeManager.addItemType(null, createItemType("anid"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItemType() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        itemTypeManager.addItemType(provider, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddInvalidItemType() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType("");

        itemTypeManager.addItemType(provider, itemType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddInvalidItemType2() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType(null);

        itemTypeManager.addItemType(provider, itemType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddItemTypeWithNullId() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType(null);

        itemTypeManager.addItemType(provider, itemType);
    }

    @Test(expected = DuplicateItemTypeException.class)
    public void testAddDuplicateItemType() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType("anid");

        itemTypeManager.addItemType(provider, itemType);
        itemTypeManager.addItemType(provider, itemType);
    }

    @Test
    public void testAddSameItemTypeFromTwoProviders() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider1 = new ProviderImpl("type", "id1", "uri", "name", "com");
        Provider provider2 = new ProviderImpl("type", "id2", "uri", "name", "com");
        ItemType itemType = createItemType("anid");

        String itemTypeId1 = itemTypeManager.addItemType(provider1, itemType);
        String itemTypeId2 = itemTypeManager.addItemType(provider2, itemType);

        assertEquals("anid", itemTypeId1);
        assertEquals(itemTypeId1, itemTypeId2);
    }

    @Test(expected = DuplicateItemTypeException.class)
    public void testAddSameItemTypeByTwoProvidersTypes() throws DuplicateItemTypeException, NoSuchProviderException {
        Provider provider1 = new ProviderImpl("type1", "id1", "uri", "name", "com");
        Provider provider2 = new ProviderImpl("type2", "id2", "uri", "name", "com");
        ItemType itemType = createItemType("anid");

        itemTypeManager.addItemType(provider1, itemType);
        itemTypeManager.addItemType(provider2, itemType);
    }

    @Test(expected = NoSuchProviderException.class)
    public void testAddOneRemoveOneItemType()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType("anid");

        String itemTypeId = itemTypeManager.addItemType(provider, itemType);

        assertEquals("anid", itemTypeId);

        Collection<ItemType> itemTypes = itemTypeManager.getItemTypes(provider);

        assertEquals(1, itemTypes.size());

        String removedId = itemTypeManager.removeItemType(provider, itemTypeId);

        assertEquals(removedId, itemTypeId);

        // This will throw an exception because the Provider was removed when we
        // removed the only ItemType registered against it.
        itemTypes = itemTypeManager.getItemTypes(provider);
    }

    @Test
    public void testAddTwoRemoveOneItemType()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType("anid");
        ItemType itemType2 = createItemType("anid2");

        itemTypeManager.addItemType(provider, itemType);
        String itemTypeId = itemTypeManager.addItemType(provider, itemType2);

        assertEquals("anid2", itemTypeId);

        LOG.debug("Fetching Item Types for Provider: " + provider);
        Collection<ItemType> itemTypes = itemTypeManager.getItemTypes(provider);

        assertEquals(2, itemTypes.size());

        String removedId = itemTypeManager.removeItemType(provider, itemTypeId);

        assertEquals(removedId, itemTypeId);

        itemTypes = itemTypeManager.getItemTypes(provider);

        assertEquals(1, itemTypes.size());
    }

    @Test
    public void testRemoveSameItemTypeByTwoProviders()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider1 = new ProviderImpl("type", "id1", "uri", "name", "com");
        Provider provider2 = new ProviderImpl("type", "id2", "uri", "name", "com");
        String id = "id1";
        ItemType itemType = createItemType(id);

        itemTypeManager.addItemType(provider1, itemType);
        itemTypeManager.addItemType(provider2, itemType);

        assertEquals(1, itemTypeManager.getItemTypes(provider1).size());
        assertEquals(1, itemTypeManager.getItemTypes(provider2).size());
        assertEquals(id, itemTypeManager.removeItemType(provider1, id));

        try {
            itemTypeManager.getItemTypes(provider1);
            fail("Provider1 should have been removed when all its ItemTypes were removed");
        } catch (NoSuchProviderException e) {
        }

        assertEquals(1, itemTypeManager.getItemTypes(provider2).size());
        assertEquals(id, itemTypeManager.getItemTypes(provider2).iterator().next().getId());
        assertEquals(id, itemTypeManager.removeItemType(provider2, id));

        try {
            itemTypeManager.getItemTypes(provider2);
            fail("Provider2 should have been removed when all its ItemTypes were removed");
        } catch (NoSuchProviderException e) {
        }
    }

    @Test(expected = NoSuchItemTypeException.class)
    public void testRemoveNonExistentItemType()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        ItemType itemType = createItemType("anid");

        itemTypeManager.addItemType(provider, itemType);
        itemTypeManager.removeItemType(provider, "nosuchid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullItemType()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        itemTypeManager.addItemType(provider, createItemType("anid"));
        itemTypeManager.removeItemType(provider, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEmptyItemType()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        itemTypeManager.removeItemType(provider, "");
    }

    @Test(expected = NoSuchProviderException.class)
    public void testRemoveItemTypeFromNonExistentProvider()
            throws NoSuchProviderException, NoSuchItemTypeException, DuplicateItemTypeException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        itemTypeManager.removeItemType(provider, "nosuchid");
    }

    private ItemType createItemType(final String localId) {
        ItemType it = new ItemType(localId);
        it.setId(it.getLocalId());
        return it;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullItemType() throws NoSuchProviderException, DuplicateItemTypeException {
        itemTypeManager.getItemType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEmptyItemType() throws NoSuchProviderException, DuplicateItemTypeException {
        itemTypeManager.getItemType("");
    }

    @Test
    public void testGetUnknownItemType() throws NoSuchProviderException, DuplicateItemTypeException {
        assertEquals(null, itemTypeManager.getItemType("unknown"));
    }
}
