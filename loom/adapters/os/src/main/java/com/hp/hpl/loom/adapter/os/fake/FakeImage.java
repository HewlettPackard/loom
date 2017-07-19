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

import com.hp.hpl.loom.adapter.os.OsImageAttributes;

public class FakeImage extends OsImageAttributes {

    public FakeImage(final String name, final String id, final String created, final String updated,
            final String status, final int minDisk, final int minRam) {
        setItemName(name);
        setItemId(id);
        setCreated(created);
        setUpdated(updated);
        setStatus(status);
        setMinDisk(minDisk);
        setMinRam(minRam);
    }

}
