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
package com.hp.hpl.loom.manager.adapter;

import java.util.Collection;
import java.util.List;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;

public class ActionTarget {
    private Session session;
    private Adapter adapter;
    private Action action;
    private String itemTypeId;
    private Collection<Item> items;
    private List<String> logicalIds;
    private ActionResult actionResult;

    public ActionTarget(final Session session, final Adapter adapter, final Action action) {
        this.session = session;
        this.adapter = adapter;
        this.action = action;
    }

    public ActionTarget(final Session session, final Adapter adapter, final Action action,
            final Collection<Item> items) {
        this(session, adapter, action);
        this.items = items;
    }

    public ActionTarget(final Session session, final Adapter adapter, final Action action,
            final List<String> logicalIds) {
        this(session, adapter, action);
        this.logicalIds = logicalIds;
    }

    public ActionTarget(final Session session, final Adapter adapter, final Action action, final String itemTypeId) {
        this(session, adapter, action);
        this.itemTypeId = itemTypeId;
    }

    public void doAction() {
        try {
            ActionResult ar = null;
            if (itemTypeId != null) {
                ar = adapter.doAction(session, action, itemTypeId);
            } else if (getItems() == null) {
                ar = adapter.doAction(session, action, logicalIds);
            } else {
                ar = adapter.doAction(session, action, items);
            }
            actionResult.setInterval(ar.getInterval());
            actionResult.setOverallStatus(ar.getOverallStatus());
        } catch (NoSuchSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * @return the items
     */
    public Collection<Item> getItems() {
        return items;
    }

    /**
     * @return the logicalIds
     */
    public List<String> getLogicalIds() {
        return logicalIds;
    }

    /**
     * @return the itemTypeId
     */
    public String getItemTypeId() {
        return itemTypeId;
    }

    /**
     * @return the actionResult
     */
    public ActionResult getActionResult() {
        return actionResult;
    }

    /**
     * @param actionResult the actionResult to set
     */
    public void setActionResult(final ActionResult actionResult) {
        this.actionResult = actionResult;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return the adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }

}
