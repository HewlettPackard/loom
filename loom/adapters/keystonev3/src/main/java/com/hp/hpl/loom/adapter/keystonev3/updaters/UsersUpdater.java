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
import com.hp.hpl.loom.adapter.keystonev3.items.DomainType;
import com.hp.hpl.loom.adapter.keystonev3.items.User;
import com.hp.hpl.loom.adapter.keystonev3.items.UserItemAttributes;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonUser;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonUsers;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class UsersUpdater extends KeystoneUpdater<User, UserItemAttributes, JsonUser> {
    private static final Log LOG = LogFactory.getLog(UsersUpdater.class);

    public UsersUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final KeystoneCollector keystoneCollector) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, keystoneCollector);
    }


    @Override
    protected String getItemId(final JsonUser resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<JsonUser> getResourceIterator() {
        return this.getJsonResources("user").iterator();
    }

    @Override
    protected List<JsonUser> getResourcesFromGet(final RestTemplate rt, final URI targetURI)
            throws HttpStatusCodeException, UpdaterHttpException {
        ResponseEntity<JsonUsers> resp = rt.getForEntity(targetURI, JsonUsers.class);
        if (resp != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("response is not null: " + resp.getStatusCode());
            }
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("response is OK");
                }
                return resp.getBody().getUsers();
            } else {
                throw new UpdaterHttpException(
                        "unable to collect users - status code: " + resp.getStatusCode().toString());
            }
        } else {
            throw new UpdaterHttpException("unable to collect users - HTTP response was null");
        }
    }

    @Override
    protected User createEmptyItem(final String logicalId) {
        return new User(logicalId, itemType);
    }

    @Override
    protected UserItemAttributes createItemAttributes(final JsonUser resource) {
        UserItemAttributes uia = new UserItemAttributes();
        uia.setEnabled(resource.isEnabled());
        uia.setEmail(resource.getEmail());
        uia.setItemId(resource.getId());
        uia.setItemName(resource.getName());
        return uia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final UserItemAttributes uia, final JsonUser resource) {
        if (uia.isEnabled() != resource.isEnabled()) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final JsonUser resource) {
        // this data is a subset of what is obtained from role_assignments so not needed
        // if (resource.getDefaultProjectId() != null) {
        // item.setRelationship(ProjectType.TYPE_LOCAL_ID, resource.getDefaultProjectId());
        // }
        if (resource.getDomainId() != null) {
            item.setRelationship(adapter.getProvider(), DomainType.TYPE_LOCAL_ID, resource.getDomainId());
        }
    }

}
