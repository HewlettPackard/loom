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
package com.hp.hpl.loom.relationships;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;

/**
 * Capture relevant information for a ConnectedTo annotation.
 */
public class ConnectedToRelationship {
    private String relationName; // Qualified relationship name, e.g. "instance-volume"
    private ConnectedRelationships toRelationships; // Calculated relationships for class of members
                                                    // of referenced Set.

    private String typeName;
    private String type;

    ConnectedToRelationship(final ItemTypeInfo fromItemTypeInfo, final ConnectedTo connectedTo,
            final ConnectedRelationships toRelationships, final String type, final String typeName) {
        this.type = type;
        this.typeName = typeName;
        this.toRelationships = toRelationships;

        String fromTypeId = fromItemTypeInfo.value();
        String toTypeId = toRelationships.getItemTypeInfo().value();
        relationName = StringUtils.isBlank(connectedTo.value())
                ? RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType("", fromTypeId, toTypeId, type)
                : connectedTo.value();
    }

    /**
     * Get the relation name based using providerType.
     *
     * @param providerType the providerType
     * @return returns the relationname
     */
    public String getRelationName(final String providerType) {
        String rName = providerType + "-" + relationName.substring(0, relationName.indexOf(":")) + ":" + providerType
                + "-" + relationName.substring(relationName.indexOf(":") + 1, relationName.length());
        return rName;
    }

    ConnectedRelationships getToRelationships() {
        return toRelationships;
    }

    /**
     * @return returns the relation type
     */
    public String getType() {
        return type;
    }

    /**
     * @return returns the relation type name
     */
    public String getTypeName() {
        return typeName;
    }
}
