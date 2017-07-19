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
package com.hp.hpl.loom.openstack.nova.model;

/**
 * Object to model the RebootAction.
 */
public class JsonRebootAction extends JsonAction {
    private JsonRebootType reboot = null;

    /**
     * @return the reboot
     */
    public JsonRebootType getReboot() {
        return reboot;
    }

    /**
     * @param reboot the reboot to set
     */
    public void setReboot(final JsonRebootType reboot) {
        this.reboot = reboot;
    }
}
