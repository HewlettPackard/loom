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
package com.hp.hpl.loom.manager.action;


import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.ChangedAggregation;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManagementInternal;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.ActionResult.Status;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;

@Component
public class ActionManagerImpl implements ActionManager {

    private static final Log LOG = LogFactory.getLog(ActionManagerImpl.class);

    // Implement support for long-standing actions that take long to compute
    // We will assume that an action is completed once it has been issued by the Adapter into the
    // Provider for the time being
    // private List<UUID> pendingResults = new ArrayList<UUID>(100);
    // private Map<UUID,ActionResult> unQueriedResults = new HashMap<UUID,ActionResult>(100);
    @Autowired
    private ItemTypeManager itemTypeManager;

    @Autowired
    private AdapterManager adapterManager;

    @Autowired
    private AggregationManagementInternal aggregationManager;

    private Map<UUID, ActionResults> aggActionResults = new HashMap<>();

    @Override
    public ActionResult doAction(final Session session, final Action action)
            throws InvalidActionSpecificationException, NoSuchSessionException, NoSuchProviderException {
        validateSession(session);
        validateActionFormat(action);
        List<String> targets = action.getTargets();
        List<ActionResult> results = new ArrayList<>(targets.size());
        ActionResults actionResults = new ActionResults();
        actionResults.setId(UUID.randomUUID());

        try {
            // after validation we know targets is not empty
            if (targetsAreItemTypes(session, targets.get(0))) {
                String itemTypeId = targets.get(0);
                ItemType itemType = itemTypeManager.getItemType(itemTypeId);
                List<Provider> providers =
                        adapterManager.getProviders(itemTypeId.substring(0, itemTypeId.indexOf("-")));
                for (Provider provider : providers) {
                    actionResults.getResults()
                            .add(adapterManager.doAction(session, provider, action, itemType.getLocalId()));
                }
            } else if (targetsAreItems(session, targets.get(0))) {
                // send logicalIds to adapterManager with relevant Provider
                Map<Provider, List<Item>> itemMap = new HashMap<>(1);
                for (String id : targets) {
                    Provider prov = adapterManager.getProvider(id);
                    Aggregation agg = adapterManager.getAggregationForItem(session, id);

                    List<Item> itemsPerProv = itemMap.get(prov);
                    if (itemsPerProv == null) {
                        itemsPerProv = new ArrayList<>();
                        itemMap.put(prov, itemsPerProv);
                    }
                    Iterator<Fibre> fibreItems = agg.getIterator();
                    while (fibreItems.hasNext()) {
                        Item item = (Item) fibreItems.next();
                        if (item.getLogicalId().equals(id)) {
                            itemsPerProv.add(item);
                            break;
                        }
                    }
                }
                // ok now, targets are sorted by providers we can send them to adapter manager
                for (Provider prov : itemMap.keySet()) {
                    actionResults.getResults().add(adapterManager.doAction(session, prov, action, itemMap.get(prov)));
                }
            } else {
                // get all the items and send to relevant providers
                List<Item> items = flattenTargets(session, action.getTargets(), action.getActionIssueTimestamp());
                // send logicalIds to adapterManager with relevant Provider
                Map<Provider, List<Item>> itemMap = new HashMap<>(items.size());
                for (Item item : items) {
                    Provider prov = adapterManager.getProvider(item.getLogicalId());
                    List<Item> itemsPerProv = itemMap.get(prov);
                    if (itemsPerProv == null) {
                        itemsPerProv = new ArrayList<>();
                        itemMap.put(prov, itemsPerProv);
                    }
                    itemsPerProv.add(item);
                }
                // ok now, targets are sorted by providers we can send them to adapter manager
                for (Provider prov : itemMap.keySet()) {
                    actionResults.getResults().add(adapterManager.doAction(session, prov, action, itemMap.get(prov)));
                }
            }
        } catch (ChangedAggregation ex) {
            List<ActionResult> r = new ArrayList<>();
            r.add(new ActionResult(Status.aborted));
            actionResults.setResults(r);

        }
        results.add(actionResults);

        actionResults.finalProcessing();
        // for (ActionResult result : results) {
        // if (result == null || result.getStatus().equals(ActionResult.Status.aborted)) {
        // throw new InvalidActionSpecificationException("Action produced no results/was aborted",
        // action);
        // }
        // }
        aggActionResults.put(actionResults.getId(), actionResults);

        return actionResults;

        /*
         * Leave pending stuff commented out for now if(result == null){ throw new
         * InvalidActionSpecificationException("Could not execute action request"); }else{
         * if(result.getStatus().equals(ActionResult.Status.pending)){
         * if(LOG.isDebugEnabled())LOG.debug("Action "+action+" is pending");
         * LOG.error("This should not happen now as pending actions are not currently supported");
         * UUID actionUuid=UUID.randomUUID(); result.setId(actionUuid);
         * pendingResults.add(actionUuid); } } return result;
         */
    }

    private boolean targetsAreItems(final Session session, final String logicalId) throws NoSuchSessionException {
        return aggregationManager.getAggregation(session, logicalId) == null;
    }


    private boolean targetsAreItemTypes(final Session session, final String itemTypeId) throws NoSuchSessionException {
        return itemTypeManager.getItemType(itemTypeId) != null;
    }

    private List<Item> flattenTargets(final Session session, final List<String> targets, final Long actionTimestamp)
            throws NoSuchSessionException, InvalidActionSpecificationException, ChangedAggregation {

        List<Item> items = new ArrayList<>();
        Aggregation agg;
        for (String logicalID : targets) {
            agg = aggregationManager.getAggregation(session, logicalID);
            if (agg == null) {
                LOG.error("Invalid target for action " + logicalID);
                throw new InvalidActionSpecificationException("Invalid target for action " + logicalID);
            }

            if (actionTimestamp != null) {
                if (agg.getFibreUpdated().getTime() > actionTimestamp) {
                    throw new ChangedAggregation(
                            "Aggregation: " + agg.getLogicalId() + " has changed since action was issues");
                }
            }

            if (!agg.containsAggregations()) {
                items.addAll(agg.getContainedItems());
            } else {
                items.addAll(flattenTargets(session, getTargets(agg.getElements()), actionTimestamp));
            }
        }
        return items;
    }

    private List<String> getTargets(final List<Fibre> elements) {
        // List<String> logicalIds= new ArrayList<String>(elements.size());
        // for(LoomEntity entity: elements){
        // logicalIds.add(entity.getLogicalId());
        // }
        // return logicalIds;
        return elements.stream().map(Fibre::getLogicalId).collect(toList());
    }


    private void validateActionFormat(final Action action) throws InvalidActionSpecificationException {
        if (action == null || action.getId() == null || action.getId().isEmpty()) {
            LOG.error("Invalid Id for action " + action);
            throw new InvalidActionSpecificationException("Invalid ID", action);
        }

        if (action.getId().isEmpty() || action.getTargets() == null || action.getTargets().size() == 0) {
            LOG.error("Invalid Targets for action " + action);
            throw new InvalidActionSpecificationException("Invalid targets", action);
        }

        // validate parameters: they should contain id
        ActionParameters params = action.getParams();
        if (params != null && params.size() != 0) {
            for (ActionParameter param : params) {
                if (param.getId() == null || param.getId().isEmpty()) {
                    LOG.error("Invalid Parameter ID for action " + action);
                    throw new InvalidActionSpecificationException("Invalid Parameter ID", action);
                }
            }
        }
    }

    private void validateAction(final boolean isItem, final ItemType itemType, final Action action)
            throws InvalidActionSpecificationException {
        if (itemType == null) {
            throw new InvalidActionSpecificationException("null itemType for action " + action);
        }
        if (isItem) {
            Map<String, Action> itemActions = itemType.getItemActions();
            if (itemActions == null || itemActions.isEmpty()) {
                throw new InvalidActionSpecificationException("No actions found for typeId " + itemType.getId());
            }
            Action allowedAction = itemActions.get(action.getId());
            if (allowedAction == null) {
                throw new InvalidActionSpecificationException("action not allowed by itemType " + itemType.getId(),
                        action);
            }

        } else {
            Map<String, Action> aggregationActions = itemType.getAggregationActions();
            if (aggregationActions == null || aggregationActions.isEmpty()) {
                throw new InvalidActionSpecificationException("No actions found for typeId " + itemType.getId());
            }

            Action allowedAction = aggregationActions.get(action.getId());
            if (allowedAction == null) {
                throw new InvalidActionSpecificationException("action not allowed by itemType " + itemType.getId(),
                        action);
            }
        }
    }


    private void validateSession(final Session session) throws NoSuchSessionException {
        if (session == null) {
            throw new NoSuchSessionException(session);
        }
    }

    @Override
    public void cancelAction(final Session session, final UUID actionId) throws NoSuchSessionException {
        validateSession(session);
        // Cancel a pending action
    }

    @Override
    public ActionResult getActionResult(final Session session, final UUID actionResultId)
            throws NoSuchSessionException {
        validateSession(session);
        // Get the status of a pending action
        return aggActionResults.get(actionResultId);
    }
}
