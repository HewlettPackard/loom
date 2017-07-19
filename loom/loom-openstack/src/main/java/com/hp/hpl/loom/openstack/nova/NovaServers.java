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
package com.hp.hpl.loom.openstack.nova;

import java.util.Iterator;

import com.hp.hpl.loom.openstack.nova.model.JsonServer;
import com.hp.hpl.loom.openstack.nova.model.JsonServers;


/**
 * The servers api interface.
 */
public interface NovaServers {
    /**
     * @return the servers
     */
    Iterator<JsonServer> getIterator();

    /**
     * Reboot command.
     *
     * @param serverId server id to reboot
     * @param type the reboot type soft/hard
     */
    void reboot(String serverId, String type);

    /**
     * Start command.
     *
     * @param serverId the server id to start
     */
    void start(String serverId);

    /**
     * Stop command.
     *
     * @param serverId the server id to stop
     */
    void stop(String serverId);

    JsonServers createInstance(JsonServers server);

}
