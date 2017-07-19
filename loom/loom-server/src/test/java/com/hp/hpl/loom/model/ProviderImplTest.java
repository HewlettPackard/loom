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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProviderImplTest {
    @Before
    public void setUp() throws Exception {}

    @After
    public void shutDown() throws Exception {}

    @Test
    public void testConstructSuccess() {
        new ProviderImpl("type", "uniqueId", "uri", "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullType() {
        new ProviderImpl(null, "uniqueId", "uri", "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructInvalidType() {
        new ProviderImpl("", "uniqueId", "uri", "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullId() {
        new ProviderImpl("type", null, "uri", "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructInvalidId() {
        new ProviderImpl("type", "", "uri", "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullEndpoint() {
        new ProviderImpl("type", "uniqueId", null, "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructInvalidEndpoint() {
        new ProviderImpl("type", "uniqueId", "", "name", "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructNullName() {
        new ProviderImpl("type", "uniqueId", "uri", null, "com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructInvalidName() {
        new ProviderImpl("type", "uniqueId", "uri", "", "com");
    }

    @Test
    public void testInspection() {
        Provider provider = new ProviderImpl("type", "uniqueId", "uri", "name", "com");

        assertEquals("type", provider.getProviderType());
        assertEquals("uniqueId", provider.getProviderId());
        assertEquals("uri", provider.getAuthEndpoint());
    }

    @Test
    public void testToString() {
        String output = new ProviderImpl("type", "uniqueId", "uri", "name", "com").toString();

        assertTrue(output.contains("type"));
        assertTrue(output.contains("uniqueId"));
        assertTrue(output.contains("uri"));
        assertTrue(output.contains("name"));
    }

    @Test
    public void testEquality() {
        Provider provider = new ProviderImpl("type", "uniqueId", "uri", "name", "com");
        Provider differentType = new ProviderImpl("type2", "uniqueId", "uri", "name", "com");
        Provider differentId = new ProviderImpl("type", "uniqueId2", "uri", "name", "com");
        Provider differentEndpoint = new ProviderImpl("type", "uniqueId", "uri2", "name", "com");
        Provider differentName = new ProviderImpl("type", "uniqueId", "uri", "name2", "com");

        assertTrue(provider.equals(provider));
        assertFalse(provider.equals(differentType));
        assertFalse(provider.equals(differentId));
        assertFalse(provider.equals(differentEndpoint));
        assertFalse(provider.equals(differentName));

        assertFalse(provider.equals(null));
        assertFalse(provider.equals(new String()));
    }

    @Test
    public void testHash() {
        Provider p1 = new ProviderImpl("type", "uniqueId", "uri", "name", "com");
        Provider p2 = new ProviderImpl("type", "uniqueId", "uri", "name", "com");
        Provider pType = new ProviderImpl("type1", "uniqueId", "uri", "name", "com");
        Provider pId = new ProviderImpl("type", "uniqueId1", "uri", "name", "com");
        Provider pEndpoint = new ProviderImpl("type", "uniqueId", "uri1", "name", "com");
        Provider pName = new ProviderImpl("type", "uniqueId", "uri", "name1", "com");

        assertEquals(p1.hashCode(), p1.hashCode());
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1.hashCode(), pType.hashCode());
        assertNotEquals(p1.hashCode(), pId.hashCode());
        assertNotEquals(p1.hashCode(), pEndpoint.hashCode());
        assertNotEquals(p1.hashCode(), pName.hashCode());
    }
}
