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
import java.util.Collection;

import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;

public class FakeInstance extends OsInstanceAttributes {

    private String imageId;
    private int flavourNbr;

    private int rebootCount;

    private Collection<FakeVolume> vols = new ArrayList<>();
    private Collection<FakeSubnet> subs = new ArrayList<>();

    public FakeInstance(final String name, final String id, final String imageId, final int flavourNbr,
            final String accessIPv4, final String accessIPv6, final String created, final String updated,
            final String status, final String hostId) {
        setItemName(name);
        setItemId(id);
        this.imageId = imageId;
        this.flavourNbr = flavourNbr;
        setAccessIPv4(accessIPv4);
        setAccessIPv6(accessIPv6);
        setCreated(created);
        setUpdated(updated);
        setStatus(status);
        setHostId(hostId);
    }

    public String getImageId() {
        return imageId;
    }

    public int getFlavourNbr() {
        return flavourNbr;
    }

    public void addVolume(final FakeVolume fv) {
        vols.add(fv);
    }

    public void removeVolume(final FakeVolume fv) {
        vols.remove(fv);
    }

    public Collection<FakeVolume> getVolumes() {
        return vols;
    }

    public void addSubnet(final FakeSubnet fs) {
        subs.add(fs);
    }

    public Collection<FakeSubnet> getSubnets() {
        return subs;
    }

    public boolean start() {
        if (getStatus().equals("SHUTOFF")) {
            setStatus("ACTIVE");
            return true;
        } else {
            return false;
        }
    }

    public boolean stop() {
        if (getStatus().equals("ACTIVE")) {
            setStatus("SHUTOFF");
            return true;
        } else {
            return false;
        }
    }

    public void setRebootCount(final int rebootCount) {
        this.rebootCount = rebootCount;
    }

    public boolean decRebootCount() {
        --rebootCount;
        return rebootCount == 0;
    }
}
