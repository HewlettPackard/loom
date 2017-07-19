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

import org.junit.Test;

public class NoSuchQueryDefinitionExceptionTest {
    @Test
    public void testNoQueryDefinitionExceptionTestString() {
        String id = "anid";
        NoSuchQueryDefinitionException e = new NoSuchQueryDefinitionException(id);

        assertEquals(id, e.getQuery());
        assertNotNull(e.getMessage());
    }

    @Test
    public void testNoQueryDefinitionExceptionTestStringThrowable() {
        String id = "anid";
        String message = "my message";
        NoSuchQueryDefinitionException e = new NoSuchQueryDefinitionException(id, new Throwable(message));

        assertEquals(id, e.getQuery());
        assertNotNull(e.getMessage());
        assertEquals(message, e.getCause().getMessage());
    }
}
