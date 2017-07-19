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
package com.hp.hpl.loom.adapter.os.real;

import com.hp.hpl.loom.openstack.nova.NovaServers;

public class InstanceActionTask implements Runnable {

    private NovaServers novaServers;
    private String serverId;

    private String action;

    public InstanceActionTask(final NovaServers novaServers, final String serverId, final String action) {
        this.novaServers = novaServers;
        this.serverId = serverId;
        this.action = action;
    }

    @Override
    public void run() {
        if ("softReboot".equals(action)) {
            novaServers.reboot(serverId, "SOFT");
        } else if ("hardReboot".equals(action)) {
            novaServers.reboot(serverId, "HARD");
        } else if ("start".equals(action)) {
            novaServers.start(serverId);
        } else if ("stop".equals(action)) {
            novaServers.stop(serverId);
        }
    }
}
