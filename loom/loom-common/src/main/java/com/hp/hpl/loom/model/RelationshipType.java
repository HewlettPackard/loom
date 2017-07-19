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

import java.util.HashSet;
import java.util.Set;

/**
 * The relationship type data structure.
 */
public class RelationshipType {

    private String name;
    private String id;
    private Set<String> items = new HashSet<>();

    /**
     * No-arg constructor for JSON serialisation.
     */
    public RelationshipType() {}

    /**
     * @param id id
     * @param name name
     */
    public RelationshipType(String id, String name) {
        this.name = name;
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the list of item relationships this covers.
     *
     * @return relationship items
     */
    public Set<String> getItems() {
        return items;
    }
}
