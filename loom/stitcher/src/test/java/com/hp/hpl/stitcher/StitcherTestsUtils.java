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
package com.hp.hpl.stitcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

public class StitcherTestsUtils {

    public static <B, C> void checkSameStitches(Map<B, Collection<C>> expected, Map<B, Collection<C>> actual) {
        checkSameStitches("", expected, actual);
    }

    public static <B, C> void checkSameStitches(String message, Map<B, Collection<C>> expected,
            Map<B, Collection<C>> actual) {

        // Check that both have the same keys
        Collection<B> expectedKeys = expected.keySet();
        Collection<B> actualKeys = actual.keySet();
        assertEquals(message, expectedKeys, actualKeys);

        // Check each key has the same values
        for (B key : expectedKeys) {
            checkSameElements(message, expected.get(key), actual.get(key));
        }
    }

    public static <T> void checkSameElements(Collection<T> expected, Collection<T> actual) {
        checkSameElements("", expected, actual);
    }

    public static <T> void checkSameElements(String message, Collection<T> expected, Collection<T> actual) {
        // Symmetric comparison
        assertTrue(message, expected.containsAll(actual));
        assertTrue(message, actual.containsAll(expected));
    }

}
