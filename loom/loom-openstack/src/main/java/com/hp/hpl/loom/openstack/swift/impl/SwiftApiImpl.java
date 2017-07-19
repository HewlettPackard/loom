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
package com.hp.hpl.loom.openstack.swift.impl;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.swift.SwiftAccounts;
import com.hp.hpl.loom.openstack.swift.SwiftApi;
import com.hp.hpl.loom.openstack.swift.SwiftContainer;
import com.hp.hpl.loom.openstack.swift.SwiftObjects;

/**
 * The swift API implementation, it constructs the sub implementations.
 */
public class SwiftApiImpl implements SwiftApi {
    private SwiftAccounts swiftAccounts;
    private SwiftContainer swiftContainers;
    private SwiftObjects swiftObjects;
    private String version;

    /**
     * Constructor that takes the version, openstackApi and endpoint.
     *
     * @param version The version of this API
     * @param openstackService the openstack API (used to lookup the rest templates)
     * @param jsonEndpoint the end point to access
     */
    public SwiftApiImpl(final String version, final OpenstackApi openstackService, final JsonEndpoint jsonEndpoint) {
        swiftAccounts = new SwiftAccountsImpl(openstackService, jsonEndpoint);
        swiftContainers = new SwiftContainersImpl(openstackService, jsonEndpoint);
        swiftObjects = new SwiftObjectsImpl(openstackService, jsonEndpoint);
        this.version = version;
    }


    @Override
    public SwiftAccounts getSwiftAccounts() {
        return swiftAccounts;
    }

    @Override
    public SwiftContainer getSwiftContainers() {
        return swiftContainers;
    }

    @Override
    public SwiftObjects getSwiftObjects() {
        return swiftObjects;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
