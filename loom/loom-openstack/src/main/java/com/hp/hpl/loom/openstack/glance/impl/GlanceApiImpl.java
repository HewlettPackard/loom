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
package com.hp.hpl.loom.openstack.glance.impl;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.glance.GlanceApi;
import com.hp.hpl.loom.openstack.glance.GlanceImage;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

/**
 * The glance API impl.
 */
public class GlanceApiImpl implements GlanceApi {
    private GlanceImage glanceImage;
    private String version;

    /**
     * An implementation of the GlanceApi.
     *
     * @param version the version this API is supporting
     * @param openstackService the openstackApi for looking up tokens
     * @param jsonEndpoint the end point to access
     */
    public GlanceApiImpl(final String version, final OpenstackApi openstackService, final JsonEndpoint jsonEndpoint) {
        this.version = version;
        glanceImage = new GlanceImageImpl(openstackService, jsonEndpoint);
    }

    @Override
    public GlanceImage getGlanceImage() {
        return glanceImage;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
