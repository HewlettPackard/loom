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
package com.hp.hpl.loom.adapter.hpcloud.records;

import java.util.HashMap;
import java.util.Map;

public class Payload {

    // VARIABLES --------------------------------------------------------------
    private Map<String, Object> attributes;

    // VARIABLES - END --------------------------------------------------------

    // CONSTRUCTORS -----------------------------------------------------------

    public Payload() {
        // Saul: DO NOT USE Hashtable! It doesn't allow null values
        // attributes = new Hashtable<String, Object>(); // Old implementation. Avoid!

        // Saul: Use HashMap instead. :-)
        attributes = new HashMap<String, Object>();
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object o) {
        attributes.put(key, o);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    // METHODS - END ----------------------------------------------------------

}
