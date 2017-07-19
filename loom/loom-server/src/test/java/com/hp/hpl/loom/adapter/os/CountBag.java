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
package com.hp.hpl.loom.adapter.os;

import java.util.Date;

public class CountBag {

    private long all = 0;
    private long created = 0;
    private long updated = 0;
    private long deleted = 0;
    private Date lastUpdateSeen = null;

    public CountBag(final long all, final long created, final long updated, final long deleted,
            final Date lastUpdateSeen) {
        this.all = all;
        this.created = created;
        this.deleted = deleted;
        this.updated = updated;
        this.lastUpdateSeen = lastUpdateSeen;
    }

    public long getAll() {
        return all;
    }

    public long getCreated() {
        return created;
    }

    public long getDeleted() {
        return deleted;
    }

    public long getUpdated() {
        return updated;
    }

    public Date getLastUpdateSeen() {
        return lastUpdateSeen;
    }

}
