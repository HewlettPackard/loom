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


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.model.Aggregation;


class AggregationMapperImpl implements AggregationMapper {
    private static final int INITIAL_MAPPED_IDS = 3;
    private static final Log LOG = LogFactory.getLog(AggregationMapperImpl.class);

    // Map of Mapped Aggregation logical IDs, indexed by mapping name
    private Map<String, Set<String>> mappedAggregations = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, Boolean> mappedAggregationsDirty = new ConcurrentHashMap<String, Boolean>();

    @Override
    public void mapGroundedAggregation(final String logicalId, final String mergedLogicalId) {
        if (mappedAggregations.containsKey(mergedLogicalId)) { // update map
            if (LOG.isDebugEnabled()) {
                LOG.debug("Updating map for " + mergedLogicalId);
            }
            Set<String> mappedIds = mappedAggregations.get(mergedLogicalId);
            mappedIds.add(logicalId);
            mappedAggregations.put(mergedLogicalId, mappedIds);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Mapped " + mappedAggregations.get(mergedLogicalId));
            }
        } else { // create first element of the map
            if (LOG.isDebugEnabled()) {
                LOG.debug("New map for " + mergedLogicalId);
            }
            Set<String> mappedIds = new HashSet<String>(INITIAL_MAPPED_IDS);
            mappedIds.add(logicalId);
            mappedAggregations.put(mergedLogicalId, mappedIds);
        }
        mappedAggregationsDirty.put(mergedLogicalId, true);
    }

    @Override
    public void deleteFromMaps(final Aggregation deleted) {
        if (mappedAggregations.containsKey(deleted.getMergedLogicalId())) {
            Set<String> mappedIds = mappedAggregations.get(deleted.getMergedLogicalId());
            mappedIds.remove(deleted.getLogicalId());
            mappedAggregations.put(deleted.getMergedLogicalId(), mappedIds);
            mappedAggregationsDirty.put(deleted.getMergedLogicalId(), true);
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleted " + deleted.getLogicalId() + " from map for " + deleted.getMergedLogicalId());
            }
        } else {
            LOG.warn("Attempt to delete non-existent map for aggregation -> " + deleted);
        }
    }

    @Override
    public Set<String> getMap(final String mappedName) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Mapped entity " + mappedAggregations.get(mappedName));
        }
        return mappedAggregations.get(mappedName);
    }

    @Override
    public void setDirty(final String mergedLogicalId, final boolean dirty) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting " + mergedLogicalId + " to dirty? " + dirty);
        }
        mappedAggregationsDirty.put(mergedLogicalId, dirty);
    }

    @Override
    public void clear() {
        mappedAggregations.clear();
        mappedAggregationsDirty.clear();
    }

}
