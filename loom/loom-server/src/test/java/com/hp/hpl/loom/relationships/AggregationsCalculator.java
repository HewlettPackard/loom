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

import java.util.ArrayList;
import java.util.Collection;

import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Session;

public class AggregationsCalculator {

    static public Aggregation AggregateEntities(final Session session, final int logicalIdIndex,
            final AggregationManager aggregationManager, final String logicalIdBase, final String typeId,
            final String name, final String description, final Collection<Fibre> input,
            final String[] dependsOnLogicalIdArray, final int aggregateSize)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {
        // = inputAggregation.isGrounded() ? new String[] { inputAggregation.getLogicalId() } :
        // null;
        int size = input.size();
        if (aggregateSize <= 0 || size <= aggregateSize) {
            // No need to aggregate, simply create a DA that is a copy of the original.
            Aggregation derivedAggregation = aggregationManager.createDerivedAggregation(session, typeId,
                    "/da" + logicalIdBase + "/hash" + logicalIdIndex, Fibre.Type.Item, name, description,
                    dependsOnLogicalIdArray, input.size());
            aggregationManager.updateDerivedAggregation(session, derivedAggregation, new ArrayList<Fibre>(input));
            derivedAggregation.setDirty(false);
            return derivedAggregation;
        } else {
            int entitiesPerCluster = size / aggregateSize;
            int remainder = size % aggregateSize;
            Aggregation derivedAggregation = aggregationManager.createDerivedAggregation(session, typeId,
                    "/da" + logicalIdBase + "/hash" + logicalIdIndex, Fibre.Type.Aggregation, "Clusters",
                    "Clustered Entities", dependsOnLogicalIdArray, aggregateSize);

            // Create child aggregations for the clusters
            ArrayList<Fibre> childAggregations = new ArrayList<Fibre>(aggregateSize);
            for (int count = 0; count < aggregateSize; count++) {
                Aggregation childAggregation = aggregationManager.createDerivedAggregation(session, typeId,
                        derivedAggregation.getLogicalId() + "/" + count, input.iterator().next().getFibreType(),
                        "cluster" + count, "Cluster", null, entitiesPerCluster + (remainder > 0 ? 1 : 0));
                childAggregations.add(childAggregation);
            }

            // Distribute the entities from the input aggregation into the clusters
            int aggregateIndex = 0;
            int count = 0;
            boolean addedExtra = false;
            for (Fibre entity : input) {
                if (++count > entitiesPerCluster) {
                    if (remainder > 0) {
                        remainder--;
                        addedExtra = true;
                    }
                }
                Aggregation aggregation = (Aggregation) childAggregations.get(aggregateIndex);
                aggregation.add(entity);

                if ((count >= entitiesPerCluster && remainder <= 0) || addedExtra) {
                    addedExtra = false;
                    count = 0;
                    aggregateIndex++;
                }
            }
            aggregationManager.updateDerivedAggregation(session, derivedAggregation, childAggregations);
            derivedAggregation.setDirty(false);
            return derivedAggregation;
        }
    }
}
