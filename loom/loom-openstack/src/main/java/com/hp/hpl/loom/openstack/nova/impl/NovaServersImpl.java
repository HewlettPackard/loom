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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.common.RestUtils;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;
import com.hp.hpl.loom.openstack.nova.NovaServers;
import com.hp.hpl.loom.openstack.nova.model.JsonAction;
import com.hp.hpl.loom.openstack.nova.model.JsonRebootAction;
import com.hp.hpl.loom.openstack.nova.model.JsonRebootType;
import com.hp.hpl.loom.openstack.nova.model.JsonServer;
import com.hp.hpl.loom.openstack.nova.model.JsonServers;
import com.hp.hpl.loom.openstack.nova.model.JsonStartAction;
import com.hp.hpl.loom.openstack.nova.model.JsonStopAction;

/**
 * The nova servers API.
 */
public class NovaServersImpl extends NovaBase<JsonServers, JsonServer> implements NovaServers {
    private static final Log LOG = LogFactory.getLog(NovaServersImpl.class);

    /**
     * Constructor that takes the end point to use and an openstackApp to lookup the RestTemplate.
     *
     * @param openstackApp the openstackApp used to access the rest templates and token holder
     * @param jsonEndpoint the end point to access
     */
    public NovaServersImpl(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    @Override
    protected Class<JsonServers> getTypeClass() {
        return JsonServers.class;
    }

    @Override
    protected String getUriSuffix() {
        return "server";
    }

    @Override
    public String getUri() {
        String resourcesUri = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s/detail";
        return resourcesUri;
    }

    @Override
    public void reboot(final String serverId, final String type) {
        if (LOG.isInfoEnabled()) {
            LOG.info("REBOOT --> " + serverId + " " + type);
        }
        JsonRebootAction jsonRebootAction = new JsonRebootAction();
        JsonRebootType jsonRebootType = new JsonRebootType();
        jsonRebootType.setType(type);
        jsonRebootAction.setReboot(jsonRebootType);
        performAction(serverId, jsonRebootAction);
    }

    @Override
    public void start(final String serverId) {
        if (LOG.isInfoEnabled()) {
            LOG.info("START --> " + serverId);
        }
        JsonAction jsonAction = new JsonStartAction();
        performAction(serverId, jsonAction);

    }

    @Override
    public void stop(final String serverId) {
        if (LOG.isInfoEnabled()) {
            LOG.info("STOP -->   " + serverId);
        }
        JsonAction jsonAction = new JsonStopAction();
        performAction(serverId, jsonAction);
    }

    private void performAction(final String serverId, final JsonAction jsonAction) {
        String resourcesUri = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s/" + serverId + "/action";
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());

        try {
            rt.postForEntity(resourcesUri, jsonAction, String.class);
        } catch (HttpStatusCodeException ex) {
            // check if it's a 401
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthenticationFailureException(ex);
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void addToResult(final JsonServers result, final JsonServers nextResults) {
        result.getServers().addAll(nextResults.getServers());
        result.setLinks(null);
    }

    @Override
    public List<JsonServer> getResults(final JsonServers result) {
        return result.getServers();
    }

    @Override
    public JsonServers createInstance(final JsonServers server) {
        System.out.println(RestUtils.toJson(server));
        String url = jsonEndpoint.getUrl() + "/" + getUriSuffix() + "s";
        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
        ResponseEntity<JsonServers> result = rt.postForEntity(url, server, JsonServers.class);

        return result.getBody();
    }

}
