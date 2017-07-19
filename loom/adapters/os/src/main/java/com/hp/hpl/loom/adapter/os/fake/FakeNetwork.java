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
package com.hp.hpl.loom.adapter.os.fake;

import java.util.ArrayList;

import com.hp.hpl.loom.adapter.os.OsNetworkAttributes;

public class FakeNetwork extends OsNetworkAttributes {

    private ArrayList<String> subnetIds;

    public FakeNetwork(final String name, final String id) {
        setItemName(name);
        setItemId(id);
        setAdminStateUp(false);
        setShared(false);
        setStatus("ACTIVE");
        subnetIds = new ArrayList<>();
    }

    public ArrayList<String> getSubnetIds() {
        return subnetIds;
    }

    public void addSubnetId(final String id) {
        subnetIds.add(id);
    }
}
