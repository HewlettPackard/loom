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
import com.hp.hpl.loom.adapter.keystonev3.items.Project;
import com.hp.hpl.loom.adapter.keystonev3.items.ProjectItemAttributes;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonProject;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonProjects;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

public class ProjectsUpdater extends KeystoneUpdater<Project, ProjectItemAttributes, JsonProject> {
    private static final Log LOG = LogFactory.getLog(ProjectsUpdater.class);

    public ProjectsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final KeystoneCollector keystoneCollector) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, keystoneCollector);
    }


    @Override
    protected String getItemId(final JsonProject resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<JsonProject> getResourceIterator() {
        return this.getJsonResources("project").iterator();
        // return getJsonProjects().getProjects().iterator();
    }

    @Override
    protected List<JsonProject> getResourcesFromGet(final RestTemplate rt, final URI targetURI)
            throws HttpStatusCodeException, UpdaterHttpException {
        ResponseEntity<JsonProjects> resp = rt.getForEntity(targetURI, JsonProjects.class);
        if (resp != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("response is not null: " + resp.getStatusCode());
            }
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("response is OK");
                }
                return resp.getBody().getProjects();
            } else {
                throw new UpdaterHttpException(
                        "unable to collect projects - status code: " + resp.getStatusCode().toString());
            }
        } else {
            throw new UpdaterHttpException("unable to collect projects - HTTP response was null");
        }
    }

    @Override
    protected Project createEmptyItem(final String logicalId) {
        return new Project(logicalId, itemType);
    }

    @Override
    protected ProjectItemAttributes createItemAttributes(final JsonProject resource) {
        ProjectItemAttributes pia = new ProjectItemAttributes();
        pia.setEnabled(resource.isEnabled());
        pia.setItemDescription(resource.getDescription());
        pia.setItemId(resource.getId());
        pia.setItemName(resource.getName());
        return pia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ProjectItemAttributes pia,
            final JsonProject resource) {
        if (pia.isEnabled() != resource.isEnabled()) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final JsonProject resource) {
        item.setRelationship(adapter.getProvider(), DomainType.TYPE_LOCAL_ID, resource.getDomainId());
    }
}
