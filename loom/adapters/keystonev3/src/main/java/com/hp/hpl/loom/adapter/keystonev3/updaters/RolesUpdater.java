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
package com.hp.hpl.loom.adapter.keystonev3.updaters;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.keystonev3.KeystoneCollector;
import com.hp.hpl.loom.adapter.keystonev3.items.Role;
import com.hp.hpl.loom.adapter.keystonev3.items.RoleItemAttributes;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonRole;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonRoles;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class RolesUpdater extends KeystoneUpdater<Role, RoleItemAttributes, JsonRole> {
    private static final Log LOG = LogFactory.getLog(RolesUpdater.class);

    public RolesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final KeystoneCollector keystoneCollector) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, keystoneCollector);
    }


    @Override
    protected String getItemId(final JsonRole resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<JsonRole> getResourceIterator() {
        return this.getJsonResources("role").iterator();
    }

    @Override
    protected List<JsonRole> getResourcesFromGet(final RestTemplate rt, final URI targetURI)
            throws HttpStatusCodeException, UpdaterHttpException {
        ResponseEntity<JsonRoles> resp = rt.getForEntity(targetURI, JsonRoles.class);
        if (resp != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("response is not null: " + resp.getStatusCode());
            }
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("response is OK");
                }
                return resp.getBody().getRoles();
            } else {
                throw new UpdaterHttpException(
                        "unable to collect roles - status code: " + resp.getStatusCode().toString());
            }
        } else {
            throw new UpdaterHttpException("unable to collect roles - HTTP response was null");
        }
    }

    @Override
    protected Role createEmptyItem(final String logicalId) {
        return new Role(logicalId, itemType);
    }

    @Override
    protected RoleItemAttributes createItemAttributes(final JsonRole resource) {
        RoleItemAttributes ria = new RoleItemAttributes();
        ria.setItemId(resource.getId());
        ria.setItemName(resource.getName());
        return ria;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final RoleItemAttributes dia, final JsonRole resource) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final JsonRole resource) {
        // no rels
    }

}
