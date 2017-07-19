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

public class NoSuchAggregationExceptionTest {
    @Test
    public void testNoSuchAggregationExceptionString() {
        String id = "anid";
        NoSuchAggregationException e = new NoSuchAggregationException(id);

        assertEquals(id, e.getLogicalId());
        assertNotNull(e.getMessage());
    }

    @Test
    public void testNoSuchAggregationExceptionStringThrowable() {
        String id = "anid";
        String message = "my message";
        NoSuchAggregationException e = new NoSuchAggregationException(id, new Throwable(message));

        assertEquals(id, e.getLogicalId());
        assertNotNull(e.getMessage());
        assertEquals(message, e.getCause().getMessage());
    }
}
