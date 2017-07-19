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
package com.hp.hpl.loom.openstack.swift;

/**
 * Interface for the swift get containers API call.
 */
public interface SwiftApi {

    /**
     * The service name.
     */
    String SERVICE_NAME = "object-store";

    /**
     * Returns the SwiftAccounts API.
     *
     * @return the SwiftAccount API.
     */
    SwiftAccounts getSwiftAccounts();

    /**
     * Returns the SwiftContainer API.
     *
     * @return the SwiftContainer API.
     */
    SwiftContainer getSwiftContainers();

    /**
     * Returns the SwiftObjects API.
     *
     * @return the SwiftObjects API.
     */
    SwiftObjects getSwiftObjects();

    /**
     * Returns the version this API is using.
     *
     * @return the API version
     */
    String getVersion();
}
