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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Returns a list of relationship types.
 */
@JsonAutoDetect
public class RelationshipTypeSet {
    private Map<String, RelationshipType> relationshipTypes;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public RelationshipTypeSet() {
        relationshipTypes = new HashMap<String, RelationshipType>();
    }

    /**
     * Get the list of RelationshipTypes.
     *
     * @return the RelationshipType list
     */
    public Collection<RelationshipType> getRelationshipTypes() {
        return relationshipTypes.values();
    }

    /**
     * @param relationType the relationType
     * @param name the name
     * @param item the item
     */
    public void addItemId(String relationType, String name, String item) {
        RelationshipType relationshipType = relationshipTypes.get(relationType);
        if (relationshipType == null) {
            relationshipType = new RelationshipType(relationType, name);
            relationshipTypes.put(relationType, relationshipType);
        }
        relationshipType.getItems().add(item);
    }
}
