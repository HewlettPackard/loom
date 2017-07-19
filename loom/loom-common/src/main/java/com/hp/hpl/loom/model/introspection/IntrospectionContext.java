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
package com.hp.hpl.loom.model.introspection;

import java.util.List;

/**
 * Hold context in which to perform introspection.
 */
public class IntrospectionContext {
    private String property;
    private Object object;
    private List<Object> allPropertyObjects;

    /**
     * Constructor to be used for introspection of a specific property on a specific object.
     *
     * @param object the object
     * @param property the property
     */
    public IntrospectionContext(final Object object, final String property) {
        this.object = object;
        this.property = property;
    }

    /**
     * Constructor to be used for introspection of all properties on a list of objects.
     *
     * @param allPropertyObjects the property objects
     */
    public IntrospectionContext(final List<Object> allPropertyObjects) {
        this.allPropertyObjects = allPropertyObjects;
    }

    /**
     * Specific property to be introspected, corresponding to getObject.
     *
     * @return the string
     */
    public String getProperty() {
        return property;
    }

    /**
     * Specific object to be introspected, corresponding to property.
     *
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * List of objects that have properties that can be introspected.
     *
     * @return the list of objects
     */
    public List<Object> getAllPropertyObjects() {
        return allPropertyObjects;
    }
}
