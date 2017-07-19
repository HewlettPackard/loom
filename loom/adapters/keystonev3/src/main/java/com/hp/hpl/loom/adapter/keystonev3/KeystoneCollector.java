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
package com.hp.hpl.loom.adapter.keystonev3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.keystonev3.items.DomainType;
import com.hp.hpl.loom.adapter.keystonev3.items.ProjectType;
import com.hp.hpl.loom.adapter.keystonev3.items.Role;
import com.hp.hpl.loom.adapter.keystonev3.items.RoleAssignmentType;
import com.hp.hpl.loom.adapter.keystonev3.items.RoleType;
import com.hp.hpl.loom.adapter.keystonev3.items.User;
import com.hp.hpl.loom.adapter.keystonev3.items.UserType;
import com.hp.hpl.loom.adapter.keystonev3.rest.RestManager;
import com.hp.hpl.loom.adapter.keystonev3.updaters.DomainsUpdater;
import com.hp.hpl.loom.adapter.keystonev3.updaters.ProjectsUpdater;
import com.hp.hpl.loom.adapter.keystonev3.updaters.RoleAssignmentsUpdater;
import com.hp.hpl.loom.adapter.keystonev3.updaters.RolesUpdater;
import com.hp.hpl.loom.adapter.keystonev3.updaters.UsersUpdater;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;

public class KeystoneCollector extends AggregationUpdaterBasedItemCollector implements ClientHttpRequestInterceptor {

    private String userName;
    private RestManager restManager = RestManager.getInstance();
    private TokenManager tokenManager = TokenManager.getInstance();
    private String keystoneUriBase;

    public KeystoneCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager,
            final String userName) {
        super(session, adapter, adapterManager);
        this.userName = userName;
        keystoneUriBase = adapter.getProvider().getAuthEndpoint();
    }

    public String getKeystoneUriBase() {
        return keystoneUriBase;
    }

    public Role getRole(final String itemId) {
        return (Role) updaterMap.get(RoleType.TYPE_LOCAL_ID).getItem(itemId);
    }

    public User getUser(final String itemId) {
        return (User) updaterMap.get(UserType.TYPE_LOCAL_ID).getItem(itemId);
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation aggregation)
            throws NoSuchProviderException, NoSuchItemTypeException {
        try {
            if (aggregationMatchesItemType(aggregation, DomainType.TYPE_LOCAL_ID)) {
                // domains
                return new DomainsUpdater(aggregation, adapter, DomainType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(aggregation, ProjectType.TYPE_LOCAL_ID)) {
                // projects
                return new ProjectsUpdater(aggregation, adapter, ProjectType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(aggregation, UserType.TYPE_LOCAL_ID)) {
                // projects
                return new UsersUpdater(aggregation, adapter, UserType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(aggregation, RoleType.TYPE_LOCAL_ID)) {
                // projects
                return new RolesUpdater(aggregation, adapter, RoleType.TYPE_LOCAL_ID, this);
            } else if (aggregationMatchesItemType(aggregation, RoleAssignmentType.TYPE_LOCAL_ID)) {
                // projects
                return new RoleAssignmentsUpdater(aggregation, adapter, RoleAssignmentType.TYPE_LOCAL_ID, this);
            }
        } catch (RuntimeException ex) {
            throw new NoSuchProviderException("adapter has gone");
        }
        return null;
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(DomainType.TYPE_LOCAL_ID);
        list.add(ProjectType.TYPE_LOCAL_ID);
        list.add(UserType.TYPE_LOCAL_ID);
        list.add(RoleType.TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        Collection<String> newList = new ArrayList<>(getUpdateItemTypeIdList());
        newList.add(RoleAssignmentType.TYPE_LOCAL_ID);
        return newList;
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        return new ActionResult(ActionResult.Status.aborted);

    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add("X-Auth-Token", tokenManager.getTokenHolder(userName).getUnscoped());
        return execution.execute(request, body);
    }

    public RestTemplate getRestTemplateWithToken() {
        RestTemplate rt = restManager.getRestTemplate("keystone-data");
        rt.setInterceptors(Collections.singletonList(this));
        return rt;
    }

}
