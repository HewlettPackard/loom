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
package com.hp.hpl.loom.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class NoSuchTapestryDefinitionExceptionTest {
    @Test
    public void testNoQueryDefinitionExceptionTestString() {
        String sessionId = "asession";
        NoSuchTapestryDefinitionException e = new NoSuchTapestryDefinitionException(sessionId);

        assertEquals(sessionId, e.getSessionId());
        assertNull(e.getTapestryId());
        assertNotNull(e.getMessage());
    }

    @Test
    public void testNoQueryDefinitionExceptionTestDescription() {
        String sessionId = "asession";
        String description = "description";
        NoSuchTapestryDefinitionException e = new NoSuchTapestryDefinitionException(sessionId, description);

        assertEquals(sessionId, e.getSessionId());
        assertNull(e.getTapestryId());
        assertNotNull(e.getMessage());
        assertEquals(description, e.getDescription());
    }

    @Test
    public void testNoQueryDefinitionExceptionWithTapestryIdTestString() {
        String sessionId = "asession";
        String id = "anid";
        String description = "description";
        NoSuchTapestryDefinitionException e = new NoSuchTapestryDefinitionException(sessionId, id, description);

        assertEquals(sessionId, e.getSessionId());
        assertEquals(id, e.getTapestryId());
        assertNotNull(e.getMessage());
        assertEquals(description, e.getDescription());
    }

    @Test
    public void testNoQueryDefinitionExceptionTestStringThrowable() {
        String sessionId = "asession";
        String id = "anid";
        String message = "my message";
        String description = "description";
        NoSuchTapestryDefinitionException e =
                new NoSuchTapestryDefinitionException(sessionId, id, description, new Throwable(message));

        assertEquals(sessionId, e.getSessionId());
        assertEquals(id, e.getTapestryId());
        assertNotNull(e.getMessage());
        assertEquals(message, e.getCause().getMessage());
        assertEquals(description, e.getDescription());
    }
}
