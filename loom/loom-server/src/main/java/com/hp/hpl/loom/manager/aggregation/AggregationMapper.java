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
package com.hp.hpl.loom.manager.aggregation;

import java.util.Set;

import com.hp.hpl.loom.model.Aggregation;

interface AggregationMapper {

    /**
     * Associate GA to mapped aggregation for providers of the same type (e.g. with the same item.
     * types for different GAs)
     *
     * @param logicalId Id to be added to the map
     * @param mergedLogicalId Key of the map
     */
    void mapGroundedAggregation(String logicalId, String mergedLogicalId);

    /**
     * Get Set of Id mapped to the specified mappedLogicalId.
     *
     * @param mappedName mapped logical Id
     * @return Set of mapped logical Ids
     */
    Set<String> getMap(String mappedName);

    /**
     * Let the Query manager know about any of the mapped GAs being dirty.
     *
     * @param mergedLogicalId
     * @param dirty
     */
    void setDirty(String mergedLogicalId, boolean dirty);

    /**
     * delete logicalId from existent map.
     *
     * @param deleted Aggregation which logicalId is to be removed from the map
     */
    void deleteFromMaps(Aggregation deleted);

    /**
     * empty all maps.
     */
    void clear();

}
