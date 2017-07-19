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
package com.hp.hpl.loom.api.service.utils;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.service.IntegrationTestBase;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public interface ThreadNavigation {
    static final Log log = LogFactory.getLog(ThreadNavigation.class);

    /*
     * Create a new thread to on first thread found with specified type. Return the IDs of the new
     * threads created.
     */
    static QueryResult filterOnRelatedToId(final LoomClient client, final TapestryDefinition tapestryDefinition,
            final String typeId, final String logicalId) {
        ThreadDefinition thread =
                TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), typeId);
        QueryDefinition newQuery = QueryFormatting.createFilterRelatedQuery(thread.getQuery().getInputs(), logicalId);

        String threadId = java.util.UUID.randomUUID().toString();
        log.info(newQuery);
        ThreadDefinition newThread = new ThreadDefinition(threadId, typeId, newQuery);
        tapestryDefinition.addThreadDefinition(newThread);
        TapestryHandling.clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);
        QueryResult qr = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                IntegrationTestBase.greaterThan0);
        return qr;
    }

    /*
     * Create new threads to drill down on first thread found with specified type. Return the IDs of
     * the new threads created.
     */
    static DrillDownThreads drillDownOnTypeId(final LoomClient client, final TapestryDefinition tapestryDefinition,
            final String typeId, final int maxFibres, final boolean randomDrillDown) {
        // Drill down on instances until reach instance Items
        ThreadDefinition thread =
                TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), typeId);
        return drillDownOnThreadId(client, tapestryDefinition, thread.getId(), thread.getItemType(), maxFibres,
                randomDrillDown);
    }

    static DrillDownThreads drillDownOnThreadId(final LoomClient client, final TapestryDefinition tapestryDefinition,
            final String threadId, final String itemType, final int maxFibres, final boolean randomDrillDown) {
        return drillDownOnThreadId(client, tapestryDefinition, threadId, itemType, maxFibres, randomDrillDown, 0);
    }

    static DrillDownThreads drillDownOnThreadId(final LoomClient client, final TapestryDefinition tapestryDefinition,
            final String threadId, final String itemType, final int maxFibres, final boolean randomDrillDown,
            final int indx) {
        List<String> newThreadIds = new ArrayList<String>();
        String itemThreadId = threadId;
        QueryResult qr = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), threadId,
                IntegrationTestBase.greaterThan0);
        for (int count = 0; count < 20; count++) {
            if (!qr.getElements().get(0).getEntity().isAggregation()) {
                // Down to level of items
                break;
            }

            QueryDefinition nextLevelQuery =
                    randomDrillDown ? QueryFormatting.createDrillDownToRandomFibreQuery(qr, maxFibres)
                            : QueryFormatting.indxDrillDown(qr, maxFibres, indx);

            log.info(nextLevelQuery);
            itemThreadId = threadId + count;
            newThreadIds.add(itemThreadId);
            ThreadDefinition nextLevelThread = new ThreadDefinition(itemThreadId, itemType, nextLevelQuery);
            tapestryDefinition.addThreadDefinition(nextLevelThread);
            TapestryHandling.clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);
            qr = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), nextLevelThread.getId(),
                    IntegrationTestBase.greaterThan0);

        }
        DrillDownThreads drilledDown = new DrillDownThreads(itemThreadId, newThreadIds);
        drilledDown.setLastOpThreadLogicalId(qr.getLogicalId());
        return drilledDown;
    }

    static DrillDownThreads execOperationWithRandomParamsOnTypeId(final LoomClient client,
            final TapestryDefinition tapestryDefinition, final String op, final ItemType type, final int maxFibres,
            final boolean randomDrillDown, final DrillDownThreads previousDrill) {
        ThreadDefinition thread =
                TapestryHandling.findThreadDefinitionWithItemType(tapestryDefinition.getThreads(), type.getId());
        DrillDownThreads drilledDown =
                previousDrill == null
                        ? drillDownOnThreadId(client, tapestryDefinition, thread.getId(), thread.getItemType(),
                                maxFibres, randomDrillDown)
                        : previousDrill;
        String logicalIdToOperateOn = drilledDown.getLastOpThreadLogicalId() == null ? drilledDown.getItemThreadId()
                : drilledDown.getLastOpThreadLogicalId();


        QueryDefinition query = null;
        QueryResult qr;
        if (op.equals(DefaultOperations.SORT_BY.toString())) {
            Map<String, Object> sortParams = populateRandomProperty(DefaultOperations.SORT_BY.toString(), type);

            if (Math.random() > 0.5) {
                sortParams.put("order", "DSC");
            } else {
                sortParams.put("order", "ASC");
            }
            if (Math.random() > 0.5) {// no braid
                query = QueryFormatting.createSortByQuery(logicalIdToOperateOn, sortParams, maxFibres);
            } else { // braid
                query = QueryFormatting.createSortByBraidQuery(logicalIdToOperateOn, sortParams, maxFibres);
            }
        }
        if (op.equals(DefaultOperations.GROUP_BY.toString())) {
            Map<String, Object> groupParams = populateRandomProperty(DefaultOperations.GROUP_BY.toString(), type);
            if (Math.random() > 0.5) {// no braid
                QueryFormatting.createGroupByQuery(logicalIdToOperateOn, groupParams);
            } else {
                QueryFormatting.createGroupByBraidQuery(logicalIdToOperateOn, groupParams, maxFibres);
            }
        }
        if (query != null) {
            ThreadDefinition nextLevelThread = new ThreadDefinition(
                    String.valueOf(tapestryDefinition.getThreads().size()), "os-" + type.getId(), query);
            tapestryDefinition.addThreadDefinition(nextLevelThread);
            TapestryHandling.clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);
            qr = BasicQueryOperations.getThreadWithWait(client, tapestryDefinition.getId(), nextLevelThread.getId(),
                    IntegrationTestBase.greaterThan0);
            drilledDown.addThreadId(nextLevelThread.getId());
            drilledDown.setLastOpThreadLogicalId(qr.getLogicalId());
        }

        return drilledDown;
    }

    static List<String> getOpPropertyNames(final String op, final ItemType type) {
        Map<String, Set<OrderedString>> ops = type.getOperations();
        // get fieldNames
        List<String> names = new ArrayList<>();
        Set<OrderedString> fieldNames = ops.get(op);
        if (fieldNames != null) {
            for (OrderedString fieldName : fieldNames) {
                names.add(fieldName.getKey());
            }
        }
        assertTrue("No parameters match op " + op + " for type " + type.getId(), names.size() > 0);
        return names;
    }

    static Map<String, Object> populateRandomProperty(final String op, final ItemType type) {
        List<String> names = getOpPropertyNames(op, type);
        int randomPick = (int) (Math.random() * (names.size()));
        Map<String, Object> paramMap = new HashMap<>(1);
        paramMap.put("property", names.get(randomPick));
        return paramMap;
    }

}
