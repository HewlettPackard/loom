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

import com.hp.hpl.loom.adapter.os.OsVolumeAttributes;

public class FakeVolume extends OsVolumeAttributes {

    private ArrayList<String> attachments;

    public FakeVolume(final String name, final String id, final int size, final String status,
            final String availabilityZone, final String created, final String volumeType, final String snapshotId,
            final String description) {
        setItemName(name);
        setItemId(id);
        setSize(size);
        setStatus(status);
        setAvailabilityZone(availabilityZone);
        setCreated(created);
        setVolumeType(volumeType);
        setSnapshotId(snapshotId);
        setItemDescription(description);
        attachments = new ArrayList<>();
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void addAttachment(final String id) {
        attachments.add(id);
    }

    public void removeAttachment(final String id) {
        attachments.remove(id);
    }

}
