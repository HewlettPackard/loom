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

import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;

/**
 * Utility class for constructing relationship names between items.
 */
public class RelationshipUtil {

    private static final int TYPE_ID_ELEMENTS_3 = 3;
    /**
     * The relation name separator.
     */
    public static final String RELATION_NAME_SEPARATOR = ":";

    /**
     * Protected constructor as this is a utility class.
     */
    protected RelationshipUtil() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Given a type id string, either local or global, return the localId.
     *
     * @param typeId the type id
     * @return the local type id
     */
    public static String getLocalTypeId(final String typeId) {
        if (typeId == null) {
            throw new IllegalArgumentException("Null type specified");
        }
        int hyphenIndex = typeId.indexOf('-');
        return hyphenIndex > 0 ? typeId.substring(hyphenIndex + 1) : typeId;
    }

    /**
     * @param item1 item1
     * @param item2 item2
     * @param type the relationship type
     * @return the relationship name between two items
     */
    public static String getRelationshipNameBetweenItems(final Item item1, final Item item2, final String type) {
        if (item1 == null || item2 == null) {
            throw new IllegalArgumentException("Null Item specified");
        }
        if (item1.getItemType() == null || item2.getItemType() == null) {
            throw new IllegalArgumentException("Item did not have an ItemType");
        }

        return getRelationshipNameBetweenTypesWithRelType(item1.getItemType(), item2.getItemType(), type);
    }

    /**
     * Build the relationship with name and type. It doesn't add the type it is null or empty.
     *
     * @param relationshipName the relationshipName
     * @param type the relationship type
     * @return the combination of the two details.
     */
    public static String buildRelationshipWithType(final String relationshipName, final String type) {
        if (type != null && !type.equals("")) {
            return relationshipName + RELATION_NAME_SEPARATOR + type;
        } else {
            return relationshipName;
        }
    }


    /**
     * @param type1 type1
     * @param type2 type2
     * @param relType the relation type
     * @return the relationship name between two types and the relation type
     */
    public static String getRelationshipNameBetweenTypesWithRelType(final ItemType type1, final ItemType type2,
            final String relType) {
        if (type1 == null || type1 == null) {
            throw new IllegalArgumentException("Null ItemType specified");
        }

        String id1 = type1.getId();
        String id2 = type2.getId();
        String name =
                id1.compareTo(id2) <= 0 ? id1 + RELATION_NAME_SEPARATOR + id2 : id2 + RELATION_NAME_SEPARATOR + id1;

        return buildRelationshipWithType(name, relType);

    }

    /**
     * @param providerType providerType
     * @param typeId1 typeId1
     * @param typeId2 typeId2
     * @return the relationship name between two types
     */
    public static String getRelationshipNameBetweenTypeIds(final String providerType, final String typeId1,
            final String typeId2) {
        return getRelationshipNameBetweenTypeIdsWithRelType(providerType, typeId1, typeId2, "");
    }

    /**
     * @param providerType providerType
     * @param typeId1 typeId1
     * @param typeId2 typeId2
     * @param relType the relation type
     * @return the relationship name between two types id and the relation type
     */
    public static String getRelationshipNameBetweenTypeIdsWithRelType(final String providerType, final String typeId1,
            final String typeId2, final String relType) {
        if (typeId1 == null || typeId2 == null) {
            throw new IllegalArgumentException("Null type specified");
        }
        return getRelationshipNameBetweenLocalTypeIdsWithRelType(providerType, getLocalTypeId(typeId1),
                getLocalTypeId(typeId2), relType);
    }

    /**
     * @param providerType providerType
     * @param typeId1 typeId1
     * @param typeId2 typeId2
     * @return the relationship name between two types id
     */
    public static String getRelationshipNameBetweenLocalTypeIds(final String providerType, final String typeId1,
            final String typeId2) {
        return getRelationshipNameBetweenLocalTypeIdsWithRelType(providerType, typeId1, typeId2, "");
    }

    /**
     * @param providerType providerType
     * @param typeId1 local types id 1
     * @param typeId2 local types id 2
     * @param relType relType
     * @return the relationship name between two local types id
     */
    public static String getRelationshipNameBetweenLocalTypeIdsWithRelType(final String providerType,
            final String typeId1, final String typeId2, final String relType) {
        if (typeId1 == null || typeId2 == null) {
            throw new IllegalArgumentException("Null type specified");
        }
        String id1 = providerType + "-" + typeId1;
        String id2 = providerType + "-" + typeId2;
        if (providerType == null || providerType.equals("")) {
            id1 = typeId1;
            id2 = typeId2;
        } else {
            id1 = providerType + "-" + typeId1;
            id2 = providerType + "-" + typeId2;
        }

        String name =
                id1.compareTo(id2) <= 0 ? id1 + RELATION_NAME_SEPARATOR + id2 : id2 + RELATION_NAME_SEPARATOR + id1;

        return buildRelationshipWithType(name, relType);
    }

    /**
     * @param relationshipName relationshipName
     * @param typeId typeId
     * @return other local type id
     */
    public static String getOtherLocalTypeId(final String relationshipName, final String typeId) {
        if (relationshipName == null) {
            throw new IllegalArgumentException("Null relationshipName specified");
        }
        if (typeId == null) {
            throw new IllegalArgumentException("Null localTypeId specified");
        }
        String[] comps = relationshipName.split("[" + RELATION_NAME_SEPARATOR + "]");
        if (comps.length == 2 || comps.length == TYPE_ID_ELEMENTS_3) {
            if (typeId.equals(comps[0])) {
                return comps[1].substring(comps[1].indexOf("-") + 1, comps[1].length());
            } else {
                return comps[0].substring(comps[0].indexOf("-") + 1, comps[0].length());
            }
        } else {
            return null;
        }
    }
}
