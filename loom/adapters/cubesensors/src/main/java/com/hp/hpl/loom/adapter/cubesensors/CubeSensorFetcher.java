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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import com.w3asel.cubesensors.api.v1.CubeSensorsApiV1;
import com.w3asel.cubesensors.api.v1.Device;
import com.w3asel.cubesensors.api.v1.State;

public class CubeSensorFetcher implements Runnable {
    Map<String, DeviceAndState> cache = new HashMap<>();
    boolean stop = false;

    private static OAuthService service;
    private static CubeSensorsApiV1 api;

    public CubeSensorFetcher() {
        String accessToken = "qPBv5J5Dnm4C";
        String accessSecret = "CaKliMgDF8TWVXXw";
        Token token = new Token(accessToken, accessSecret);
        api = new CubeSensorsApiV1(token);
    }

    public DeviceAndState getLatestDeviceState(final String uid) {
        return cache.get(uid);
    }

    public Iterator<DeviceAndState> getDevices() {
        return cache.values().iterator();
    }

    @Override
    public void run() {
        while (!stop) {
            List<Device> devices = api.getDevices();
            for (Device device : devices) {
                DeviceAndState deviceAndState = cache.get(device.getUid());
                if (deviceAndState == null) {
                    deviceAndState = new DeviceAndState(device);
                    cache.put(device.getUid(), deviceAndState);
                } else {
                    deviceAndState.setDevice(device);
                }
                State state = api.getCurrent(device.getUid());
                if (state != null) {
                    deviceAndState.setLastState(state);
                }
            }


            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }


}
