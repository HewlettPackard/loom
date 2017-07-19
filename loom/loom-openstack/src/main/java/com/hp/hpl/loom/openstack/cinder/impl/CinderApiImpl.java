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
package com.hp.hpl.loom.openstack.cinder.impl;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.cinder.CinderApi;
import com.hp.hpl.loom.openstack.cinder.CinderVolume;
import com.hp.hpl.loom.openstack.cinder.CinderVolumeType;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * The cinder API impl.
 */
public class CinderApiImpl implements CinderApi {
    private CinderVolume cinderVolume;
    private CinderVolumeType cinderVolumeType;
    private CinderVolumeQuotaImpl cinderVolumeQuota;
    private String version;

    /**
     * An implementation of the CinderApi.
     *
     * @param version the version this API is supporting
     * @param openstackService the openstackApi for looking up tokens
     * @param jsonEndpoint the end point to access
     */
    public CinderApiImpl(final String version, final OpenstackApi openstackService, final JsonEndpoint jsonEndpoint) {
        this.version = version;
        cinderVolume = new CinderVolumeImpl(openstackService, jsonEndpoint);
        cinderVolumeType = new CinderVolumeTypeImpl(openstackService, jsonEndpoint);
        cinderVolumeQuota = new CinderVolumeQuotaImpl(openstackService, jsonEndpoint);
    }

    @Override
    public CinderVolume getCinderVolume() {
        return cinderVolume;
    }

    @Override
    public CinderVolumeType getCinderVolumeType() {
        return cinderVolumeType;
    }

    @Override
    public CinderVolumeQuotaImpl getCinderVolumeQuota() {
        return cinderVolumeQuota;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
