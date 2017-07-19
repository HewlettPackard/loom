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
package com.hp.hpl.loom.adapter.os.swift;

import com.hp.hpl.loom.model.CoreItemAttributes;

public class OsAccountAttributes extends CoreItemAttributes {

    protected long bytesUsed;
    protected long containerCount;
    protected long objectCount;

    public OsAccountAttributes() {
        super();
    }

    public long getBytesUsed() {
        return bytesUsed;
    }

    public void setBytesUsed(final long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    public long getContainerCount() {
        return containerCount;
    }

    public void setContainerCount(final long containerCount) {
        this.containerCount = containerCount;
    }

    public long getObjectCount() {
        return objectCount;
    }

    public void setObjectCount(final long objectCount) {
        this.objectCount = objectCount;
    }
}
