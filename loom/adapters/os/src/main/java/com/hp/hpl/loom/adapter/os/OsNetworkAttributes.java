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
package com.hp.hpl.loom.adapter.os;

import com.hp.hpl.loom.model.CoreItemAttributes;

public class OsNetworkAttributes extends CoreItemAttributes {

    private boolean adminStateUp;
    private boolean shared;
    private String status;

    public OsNetworkAttributes() {
        super();
    }

    public OsNetworkAttributes(final boolean adminStateUp, final boolean shared, final String status) {
        super();
        this.adminStateUp = adminStateUp;
        this.shared = shared;
        this.status = status;
    }

    /**
     * @return the adminStateUp
     */
    public boolean isAdminStateUp() {
        return adminStateUp;
    }

    /**
     * @param adminStateUp the adminStateUp to set
     */
    public void setAdminStateUp(final boolean adminStateUp) {
        this.adminStateUp = adminStateUp;
    }

    /**
     * @return the shared
     */
    public boolean isShared() {
        return shared;
    }

    /**
     * @param shared the shared to set
     */
    public void setShared(final boolean shared) {
        this.shared = shared;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final String status) {
        this.status = status;
    }
}
