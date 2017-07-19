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

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.loom.model.Aggregation;

/**
 * Collection of all indexed aggregations for a specific session.
 */
class LoomModel {

    private String sessionId;

    // Map of providerId of Grounded Aggregations, indexed by logicalId.
    private Map<String, String> groundedAggregationProviderIds = new HashMap<String, String>();

    // Map of Grounded Aggregations containing the core entities, indexed by logicalId.
    private Map<String, Aggregation> groundedAggregations = new HashMap<String, Aggregation>();

    // Map of Derived Aggregations containing the core entities, indexed by logicalId.
    private Map<String, Aggregation> derivedAggregations = new HashMap<String, Aggregation>();

    private AggregationMapper aggregationMapper = new AggregationMapperImpl();

    // private Map<String, List<String>> thrownExceptions = new HashMap<>(4);


    // Map of all Aggregations, indexed by logicalId.
    private Map<String, Aggregation> aggregations = new HashMap<String, Aggregation>();

    LoomModel(final String sessionId) {
        setSessionId(sessionId);
    }

    String getSessionId() {
        return sessionId;
    }

    void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    AggregationMapper getAggregationMapper() {
        return aggregationMapper;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Grounded Aggregations
    // ////////////////////////////////////////////////////////////////////////////////////////

    Aggregation putGroundedAggregation(final String logicalId, final Aggregation aggregation, final String providerId) {
        aggregations.put(logicalId, aggregation);
        groundedAggregationProviderIds.put(logicalId, providerId);
        return groundedAggregations.put(logicalId, aggregation);
    }

    Map<String, Aggregation> getGroundedAggregations() {
        return groundedAggregations;
    }

    Aggregation getGroundedAggregation(final String logicalId) {
        return groundedAggregations.get(logicalId);
    }

    String getGroundedAggregationProviderId(final String logicalId) {
        return groundedAggregationProviderIds.get(logicalId);
    }

    boolean groundedAggregationExists(final String logicalId) {
        return groundedAggregations.containsKey(logicalId);
    }


    // ////////////////////////////////////////////////////////////////////////////////////////
    // Derived Aggregations
    // ////////////////////////////////////////////////////////////////////////////////////////

    Aggregation putDerivedAggregation(final String logicalId, final Aggregation aggregation) {
        aggregations.put(logicalId, aggregation);
        return derivedAggregations.put(logicalId, aggregation);
    }

    Map<String, Aggregation> getDerivedAggregations() {
        return derivedAggregations;
    }

    boolean derivedAggregationExists(final String logicalId) {
        return derivedAggregations.containsKey(logicalId);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Aggregations
    // ////////////////////////////////////////////////////////////////////////////////////////

    Map<String, Aggregation> getAggregations() {
        return aggregations;
    }

    Aggregation getAggregation(final String logicalId) {
        return aggregations.get(logicalId);
    }

    boolean aggregationExists(final String logicalId) {
        if (logicalId == null) {
            throw new IllegalArgumentException("Logical ID cannot be null");
        }
        return aggregations.containsKey(logicalId);
    }

    Aggregation removeAggregation(final String logicalId) {
        Aggregation aggregation = aggregations.remove(logicalId);
        if (aggregation != null) {
            if (aggregation.isGrounded()) {
                groundedAggregations.remove(logicalId);
                groundedAggregationProviderIds.remove(logicalId);
            } else {
                derivedAggregations.remove(logicalId);
            }
            if (aggregation.isGrounded()) {
                getAggregationMapper().deleteFromMaps(aggregation);
            }
        }
        return aggregation;
    }

    void deleteAllAggregations() {
        aggregations.clear();
        groundedAggregations.clear();
        getAggregationMapper().clear();
    }

}
