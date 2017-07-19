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

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.keystonev3.KeystoneCollector;
import com.hp.hpl.loom.adapter.keystonev3.items.ProjectType;
import com.hp.hpl.loom.adapter.keystonev3.items.Role;
import com.hp.hpl.loom.adapter.keystonev3.items.User;
import com.hp.hpl.loom.adapter.keystonev3.items.UserType;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonRoleAssignment;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonRoleAssignments;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class RoleAssignmentsUpdater
        extends KeystoneUpdater<AdapterItem<CoreItemAttributes>, CoreItemAttributes, JsonRoleAssignment> {
    private static final Log LOG = LogFactory.getLog(RoleAssignmentsUpdater.class);

    public RoleAssignmentsUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final String itemTypeLocalId, final KeystoneCollector keystoneCollector) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, keystoneCollector);
    }


    @Override
    protected String getItemId(final JsonRoleAssignment resource) {
        if ((resource.getRole() != null) && (resource.getUser() != null)) {
            return resource.getRole().getId() + "#" + resource.getUser().getId();
        } else {
            return ("unknownRoleAssignment");
        }
    }

    @Override
    protected Iterator<JsonRoleAssignment> getResourceIterator() {
        return this.getJsonResources("role_assignment").iterator();
        // return getJsonDomains().getDomains().iterator();
    }

    @Override
    protected List<JsonRoleAssignment> getResourcesFromGet(final RestTemplate rt, final URI targetURI)
            throws HttpStatusCodeException, UpdaterHttpException {
        ResponseEntity<JsonRoleAssignments> resp = rt.getForEntity(targetURI, JsonRoleAssignments.class);
        if (resp != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("response is not null: " + resp.getStatusCode());
            }
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("response is OK");
                }
                return resp.getBody().getRoleAssignments();
            } else {
                throw new UpdaterHttpException(
                        "unable to collect roleAssigments - status code: " + resp.getStatusCode().toString());
            }
        } else {
            throw new UpdaterHttpException("unable to collect roleAssignments - HTTP response was null");
        }
    }

    @Override
    protected AdapterItem createEmptyItem(final String logicalId) {
        return null;
    }

    @Override
    protected CoreItemAttributes createItemAttributes(final JsonRoleAssignment resource) {
        return null;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final CoreItemAttributes dia,
            final JsonRoleAssignment resource) {
        return ChangeStatus.UNCHANGED;
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final JsonRoleAssignment resource) {
        if (resource.getRole() != null) {
            Role role = kc.getRole(resource.getRole().getId());
            if (role != null) {
                if (resource.getUser() != null) {
                    role.setRelationship(adapter.getProvider(), UserType.TYPE_LOCAL_ID, resource.getUser().getId());
                }
                if ((resource.getScope() != null) && (resource.getScope().getProject() != null)) {
                    role.setRelationship(adapter.getProvider(), ProjectType.TYPE_LOCAL_ID,
                            resource.getScope().getProject().getId());
                }
            }
        }
        // users and projects are not linked anymore through roles (@root breaks the link) so
        // explicit
        // relationship setting is needed here.
        if (resource.getUser() != null) {
            User user = kc.getUser(resource.getUser().getId());
            if (user != null) {
                if ((resource.getScope() != null) && (resource.getScope().getProject() != null)) {
                    user.setRelationship(adapter.getProvider(), ProjectType.TYPE_LOCAL_ID,
                            resource.getScope().getProject().getId());
                }
            }
        }
    }
}
