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
package com.hp.hpl.loom.adapter.cubesensors;

import com.w3asel.cubesensors.api.v1.Device;
import com.w3asel.cubesensors.api.v1.State;

public class DeviceAndState {
    private Device device;
    private State lastState;

    public DeviceAndState(final Device device) {
        this.device = device;
    }

    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * @param device the device to set
     */
    public void setDevice(final Device device) {
        this.device = device;
    }

    /**
     * @return the lastState
     */
    public State getLastState() {
        return lastState;
    }

    /**
     * @param lastState the lastState to set
     */
    public void setLastState(final State lastState) {
        this.lastState = lastState;
    }

}
