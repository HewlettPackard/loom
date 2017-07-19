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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;

public class GroupingCalculator {

    static public Collection<Fibre> GroupEntities(final Session session, final AggregationManager aggregationManager,
            final Aggregation aggregation, final String groupBy)
            throws NoSuchAggregationException, NoSuchSessionException, LogicalIdAlreadyExistsException {

        if (groupBy == null || groupBy.length() == 0) {
            // No need to group.
            return aggregation.getElements();
        }
        // Do the grouping into a hash map
        String[] dependsOnLogicalIdArray = aggregation.isGrounded() ? new String[] {aggregation.getLogicalId()} : null;
        String typeId = aggregation.getTypeId();
        String groupByDescription = "GroupBy " + groupBy;
        HashMap<String, Fibre> groups = new HashMap<String, Fibre>();
        try {
            for (Fibre entity : aggregation.getElements()) {
                String value = FibreIntrospectionUtils.introspectPropertyStrict(groupBy, entity, null);
                Aggregation itemGroup = (Aggregation) groups.get(value);
                if (itemGroup == null) {
                    itemGroup = aggregationManager.createDerivedAggregation(session, typeId,
                            "/da" + aggregation.getLogicalId() + "/id/" + value, Fibre.Type.Item, value,
                            groupByDescription, dependsOnLogicalIdArray, aggregation.getSize());

                    groups.put(value, itemGroup);
                }
                itemGroup.add(entity);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            return null;
        }
        return groups.values();
    }
}
