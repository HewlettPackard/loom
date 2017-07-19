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

import java.util.Collection;
import java.util.List;

import com.hp.hpl.loom.model.Aggregation;


/**
 * The interface for the relationships model.
 */
public interface RelationshipsModel {

    /**
     * Calculate all relationships for all classes in model -- Create a method that can take a list
     * of classes so do not need to look through GAs.
     *
     * @param groundedAggregations the groundedAggregations
     */
    void calculateClassRelationships(List<Aggregation> groundedAggregations);

    /**
     * Returns the connected relationships for given class name.
     *
     * @param className classname to lookup on
     * @return ConnectedRelatioships
     */
    ConnectedRelationships getItemRelationships(final String className);

    /**
     * Returns all the connected relationships.
     *
     * @return the collection of connected relationships
     */
    Collection<ConnectedRelationships> getAllItemRelationships();
}
