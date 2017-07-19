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
package com.hp.hpl.loom.openstack.nova.impl;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.nova.NovaQuotas;
import com.hp.hpl.loom.openstack.nova.model.JsonQuota;
import com.hp.hpl.loom.openstack.nova.model.JsonQuotas;

/**
 * The nova quotas API.
 */
public class NovaQuotasImpl extends NovaBase<JsonQuotas, JsonQuota> implements NovaQuotas {
    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackService the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public NovaQuotasImpl(final OpenstackApi openstackService, final JsonEndpoint jsonEndpoint) {
        super(openstackService, jsonEndpoint);
    }

    @Override
    protected Class<JsonQuotas> getTypeClass() {
        return JsonQuotas.class;
    }

    @Override
    protected String getUriSuffix() {
        return "os-quota-set";
    }

    @Override
    public String getUri() {
        String resourcesUri = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s/" + jsonEndpoint.getProjectId();
        return resourcesUri;

    }

    @Override
    public void addToResult(final JsonQuotas result, final JsonQuotas nextResults) {}

    @Override
    public List<JsonQuota> getResults(final JsonQuotas result) {
        List<JsonQuota> list = new ArrayList<>();
        list.add(result.getQuotaSet());
        return list;
    }

}
