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

import java.util.List;

public class DrillDownThreads {
    private String itemThreadId; // ID of thread containing items
    private List<String> newThreadIds; // IDs of any new threads created to reach items
    private String lastOpThreadLogicalId;

    DrillDownThreads(final String itemThreadId, final List<String> newThreadIds) {
        this.itemThreadId = itemThreadId;
        this.newThreadIds = newThreadIds;
    }

    DrillDownThreads(final String itemThreadId, final List<String> newThreadIds, final String lastOpItemThread) {
        this.itemThreadId = itemThreadId;
        this.newThreadIds = newThreadIds;
        lastOpThreadLogicalId = lastOpItemThread;
    }

    public String getItemThreadId() {
        return itemThreadId;
    }

    public List<String> getNewThreadIds() {
        return newThreadIds;
    }

    public void addThreadId(final String itemThreadId) {
        newThreadIds.add(itemThreadId);
    }

    public String getLastOpThreadLogicalId() {
        return lastOpThreadLogicalId;
    }

    public void setLastOpThreadLogicalId(final String lastOpThreadLogicalId) {
        this.lastOpThreadLogicalId = lastOpThreadLogicalId;
    }
}
