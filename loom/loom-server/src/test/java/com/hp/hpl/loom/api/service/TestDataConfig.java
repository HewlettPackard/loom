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
package com.hp.hpl.loom.api.service;

public interface TestDataConfig {

    boolean getSoak();

    int getSoakTimeMins();

    int getBraidTest();

    int getBraidClient();

    String getAggregatorUri();

    String getUsername();

    String getPassword();

    String getProviderType(int adapterIdx);

    String getProviderId(int adapterIdx);

    int getExpectedInstanceNbr(int adapterIdx);

    int getExpectedVolumeNbr(int adapterIdx);

    int getExpectedImageNbr(int adapterIdx);

    int getExpectedNetworkNbr(int adapterIdx);

    int getExpectedSubnetNbr(int adapterIdx);
}
