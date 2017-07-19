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
package com.hp.hpl.loom.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.api.ApiConfig;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ItemTypeList;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.QueryResultList;
import com.hp.hpl.loom.model.Status;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinitionList;
import com.hp.hpl.loom.tapestry.ThreadDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinitionList;

public class RestClientTest {
    RestClient restClient = new RestClient();
    RestTemplate restTemplate = null;

    private String sessionId = "s%3ALQ1DnT2WKKrh-RJnzhykvqi0I";

    @Before
    public void setup() {
        restTemplate = mock(RestTemplate.class);
        restClient.restTemplate = restTemplate;
        getProviders();
    }

    /**
     * Confirm that we have the session set (it is setup by the getProviders call in setup correctly
     */
    @Test
    public void getSessionId() {
        assertEquals(sessionId, restClient.getSessionId());
    }

    /**
     * Tests the status call
     */
    @Test
    public void getStatus() {
        Status status = new Status();
        ResponseEntity<Status> response = new ResponseEntity<Status>(status, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.API_STATUS;
        when(restTemplate.getForEntity(url, Status.class)).thenReturn(response);
        assertNotNull(restClient.getStatus());

        Mockito.verify(restTemplate).getForEntity(url, Status.class);

        assertEquals(sessionId, restClient.getSessionId());
    }


    /**
     * Test the get provider call
     */
    @Test
    public void getProviders() {
        Mockito.reset(restTemplate);
        ArrayList<Provider> providers = new ArrayList<>();
        providers.add(new TestingProvider());

        ProviderList providerList = new ProviderList();
        providerList.setProviders(providers);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Set-Cookie", "loom=" + sessionId + "; sdfsdf");
        ResponseEntity<ProviderList> response =
                new ResponseEntity<ProviderList>(providerList, headers, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.PROVIDERS_BASE;

        when(restTemplate.getForEntity(url, ProviderList.class)).thenReturn(response);
        ProviderList list = restClient.getProviders();

        assertNotNull(list.getProviders());

        Mockito.verify(restTemplate).getForEntity(url, ProviderList.class);
        assertEquals(sessionId, restClient.getSessionId());
    }

    @Test
    public void getPatterns() {
        PatternDefinitionList patternDefinitionList = new PatternDefinitionList();
        ResponseEntity<PatternDefinitionList> response =
                new ResponseEntity<PatternDefinitionList>(patternDefinitionList, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.PATTERNS_BASE;
        when(restTemplate.getForEntity(url, PatternDefinitionList.class)).thenReturn(response);

        assertNotNull(restClient.getPatterns());

        Mockito.verify(restTemplate).getForEntity(url, PatternDefinitionList.class);

        assertEquals(sessionId, restClient.getSessionId());
    }

    // PatternDefinition getPattern(String patternId) throws LoomClientException;
    @Test
    public void getPattern() {
        PatternDefinition patternDefinition = new PatternDefinition();
        ResponseEntity<PatternDefinition> response =
                new ResponseEntity<PatternDefinition>(patternDefinition, HttpStatus.ACCEPTED);
        String id = "123";
        String url = restClient.baseUrl + ApiConfig.PATTERNS_BASE + "/" + id;
        when(restTemplate.getForEntity(url, PatternDefinition.class)).thenReturn(response);

        assertNotNull(restClient.getPattern(id));

        Mockito.verify(restTemplate).getForEntity(url, PatternDefinition.class);

        assertEquals(sessionId, restClient.getSessionId());
    }


    // TapestryDefinition createTapestryDefinition(TapestryDefinition tapestryDefinition) throws
    // LoomClientException;
    @Test
    public void createTapestryDefinition() {
        TapestryDefinition tapestryDefinition = new TapestryDefinition();
        ResponseEntity<TapestryDefinition> response =
                new ResponseEntity<TapestryDefinition>(tapestryDefinition, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE;
        when(restTemplate.postForEntity(url, tapestryDefinition, TapestryDefinition.class)).thenReturn(response);

        restClient.createTapestryDefinition(tapestryDefinition); // test with value as well

        Mockito.verify(restTemplate).postForEntity(url, tapestryDefinition, TapestryDefinition.class);

        assertEquals(sessionId, restClient.getSessionId());

    }


    // TapestryDefinition getTapestry(String tapId) throws LoomClientException;
    @Test
    public void getTapestry() {
        TapestryDefinition tapestryDefinition = new TapestryDefinition();
        ResponseEntity<TapestryDefinition> response =
                new ResponseEntity<TapestryDefinition>(tapestryDefinition, HttpStatus.ACCEPTED);
        String tapestryId = "123";
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId;
        when(restTemplate.getForEntity(url, TapestryDefinition.class)).thenReturn(response);

        assertNotNull(restClient.getTapestry(tapestryId));

        Mockito.verify(restTemplate).getForEntity(url, TapestryDefinition.class);

        assertEquals(sessionId, restClient.getSessionId());

    }


    // void updateTapestryDefinition(String tapestryId, TapestryDefinition tapestryDefinition)
    // throws LoomClientException;
    @Test
    public void updateTapestryDefinition() {

        TapestryDefinition tapestryDefinition = new TapestryDefinition();
        String tapestryId = "123";
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        restTemplate.put(url, tapestryDefinition);

        restClient.updateTapestryDefinition(tapestryId, tapestryDefinition); // test with value as
                                                                             // well

        Mockito.verify(restTemplate).put(url, tapestryDefinition);

        assertEquals(sessionId, restClient.getSessionId());
    }

    // TapestryDefinitionList getTapestryDefinitions() throws LoomClientException;
    @Test
    public void getTapestryDefinitions() {

        TapestryDefinitionList tapestryDefinitionList = new TapestryDefinitionList();
        ResponseEntity<TapestryDefinitionList> response =
                new ResponseEntity<TapestryDefinitionList>(tapestryDefinitionList, HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE;
        when(restTemplate.getForEntity(url, TapestryDefinitionList.class)).thenReturn(response);

        assertNotNull(restClient.getTapestryDefinitions());

        Mockito.verify(restTemplate).getForEntity(url, TapestryDefinitionList.class);

        assertEquals(sessionId, restClient.getSessionId());

    }


    // void deleteTapestryDefinition(String tapestryId) throws LoomClientException;
    @Test
    public void deleteTapestryDefinition() {
        String tapestryId = "tapestryId";
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId;
        restClient.deleteTapestryDefinition(tapestryId);
        Mockito.verify(restTemplate).delete(url);
        assertEquals(sessionId, restClient.getSessionId());
    }


    // void deleteTapestryDefinitions() throws LoomClientException;
    @Test
    public void deleteTapestryDefinitions() {
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE;
        restClient.deleteTapestryDefinitions();
        Mockito.verify(restTemplate).delete(url);
        assertEquals(sessionId, restClient.getSessionId());
    }


    // void updateThreadDefinition(String tapestryId, String threadId, ThreadDefinition
    // threadDefinition) throws LoomClientException;
    @Test
    public void updateThreadDefinition() {

        String tapestryId = "tapestryId";
        String threadId = "threadId";
        ThreadDefinition threadDefiniton = new ThreadDefinition();
        new ResponseEntity<Void>(HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId;

        restTemplate.put(url, threadDefiniton);

        restClient.updateThreadDefinition(tapestryId, threadId, threadDefiniton);
        Mockito.verify(restTemplate).put(url, threadDefiniton);

        assertEquals(sessionId, restClient.getSessionId());
    }

    // ThreadDefinition getThreadDefinition(String tapestryId, String threadId) throws
    // LoomClientException;
    @Test
    public void getThreadDefinition() {
        String tapestryId = "tapestryId";
        String threadId = "threadId";
        ThreadDefinition threadDefinition = new ThreadDefinition();
        ResponseEntity<ThreadDefinition> response =
                new ResponseEntity<ThreadDefinition>(threadDefinition, HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId;
        when(restTemplate.getForEntity(url, ThreadDefinition.class)).thenReturn(response);
        assertNotNull(restClient.getThreadDefinition(tapestryId, threadId));
        Mockito.verify(restTemplate).getForEntity(url, ThreadDefinition.class);
        assertEquals(sessionId, restClient.getSessionId());
    }

    // ThreadDefinitionList getThreadDefinitions(String tapestryId) throws LoomClientException;
    @Test
    public void getThreadDefinitions() {
        String tapestryId = "tapestryId";
        ThreadDefinitionList threadDefinitionList = new ThreadDefinitionList();
        ResponseEntity<ThreadDefinitionList> response =
                new ResponseEntity<ThreadDefinitionList>(threadDefinitionList, HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        when(restTemplate.getForEntity(url, ThreadDefinitionList.class)).thenReturn(response);
        assertNotNull(restClient.getThreadDefinitions(tapestryId));
        Mockito.verify(restTemplate).getForEntity(url, ThreadDefinitionList.class);
        assertEquals(sessionId, restClient.getSessionId());

    }

    // void deleteThreadDefinition(String tapestryId, String threadId) throws LoomClientException;
    @Test
    public void deleteThreadDefinition() {
        String tapestryId = "tapestryId";
        String threadId = "threadId";
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId;

        restClient.deleteThreadDefinition(tapestryId, threadId);
        Mockito.verify(restTemplate).delete(url);
        assertEquals(sessionId, restClient.getSessionId());
    }

    // void deleteThreadDefinitions(String tapestryId) throws LoomClientException;
    @Test
    public void deleteThreadDefinitions() {
        String tapestryId = "tapestryId";
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        restClient.deleteThreadDefinitions(tapestryId);
        Mockito.verify(restTemplate).delete(url);
        assertEquals(sessionId, restClient.getSessionId());
    }

    // void createThreadDefinition(String tapestryId, ThreadDefinition threadDefinition) throws
    // LoomClientException;
    @Test
    public void createThreadDefinition() {
        String tapestryId = "tapestryId";
        ThreadDefinition threadDefinition = new ThreadDefinition();

        ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        when(restTemplate.postForEntity(url, threadDefinition, Void.class)).thenReturn(response);
        restClient.createThreadDefinition(tapestryId, threadDefinition);
        Mockito.verify(restTemplate).postForEntity(url, threadDefinition, Void.class);
        assertEquals(sessionId, restClient.getSessionId());
    }

    // QueryResultList getAggregations(String tapestryId, List<String> threadIds) throws
    // LoomClientException;
    @Test
    public void getAggregations() {
        String tapestryId = "tapestryId";
        List<String> threadIds = new ArrayList<>();

        QueryResultList queryResultList = new QueryResultList();
        ResponseEntity<QueryResultList> response =
                new ResponseEntity<QueryResultList>(queryResultList, HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/results";
        when(restTemplate.postForEntity(url, threadIds, QueryResultList.class)).thenReturn(response);
        assertNotNull(restClient.getAggregations(tapestryId, threadIds));
        Mockito.verify(restTemplate).postForEntity(url, threadIds, QueryResultList.class);
        assertEquals(sessionId, restClient.getSessionId());
    }

    // public PatternDefinitionList loginProvider(final String providerType, final String
    // providerId, final Credentials credentials) throws LoomClientException;
    @Test
    public void loginProvider() {

        String providerType = "providerType";
        String providerId = "providerId";
        Credentials credentials = new Credentials("test", "test");

        PatternDefinitionList patternDefinitionList = new PatternDefinitionList();

        ResponseEntity<PatternDefinitionList> response =
                new ResponseEntity<PatternDefinitionList>(patternDefinitionList, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId
                + "?operation=login";

        when(restTemplate.postForEntity(url, credentials, PatternDefinitionList.class)).thenReturn(response);

        restClient.loginProvider(providerType, providerId, credentials);

        Mockito.verify(restTemplate).postForEntity(url, credentials, PatternDefinitionList.class);

    }

    // void logoutProvider(String providerType, String providerId) throws LoomClientException;
    @Test
    public void logoutProvider() {
        String providerType = "providerType";
        String providerId = "providerId";

        ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId
                + "?operation=logout";

        when(restTemplate.postForEntity(url, "", Void.class)).thenReturn(response);

        restClient.logoutProvider(providerType, providerId);

        Mockito.verify(restTemplate).postForEntity(url, "", Void.class);

        assertEquals(sessionId, restClient.getSessionId());

    }

    // void logoutAllProviders() throws LoomClientException;
    @Test
    public void logoutAllProviders() {
        ResponseEntity<Void> response = new ResponseEntity<Void>(HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.PROVIDERS_BASE + "?operation=logout";
        when(restTemplate.postForEntity(url, "", Void.class)).thenReturn(response);
        restClient.logoutAllProviders();
        Mockito.verify(restTemplate).postForEntity(url, "", Void.class);
    }

    // ProviderList getProviders(String providerType) throws LoomClientException;
    @Test
    public void getProvidersByType() {
        String providerType = "providerType";
        ProviderList providerList = new ProviderList();
        ResponseEntity<ProviderList> response = new ResponseEntity<ProviderList>(providerList, HttpStatus.ACCEPTED);
        String url = restClient.baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType;
        when(restTemplate.getForEntity(url, ProviderList.class)).thenReturn(response);
        assertNotNull(restClient.getProviders(providerType));
        Mockito.verify(restTemplate).getForEntity(url, ProviderList.class);
        assertEquals(sessionId, restClient.getSessionId());

    }

    // Provider getProvider(String providerType, String providerId) throws LoomClientException;
    @Test
    public void getProvider() {
        String providerType = "providerType";
        String providerId = "providerId";

        Provider provider = new TestingProvider();
        ResponseEntity<Provider> response = new ResponseEntity<Provider>(provider, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId;
        when(restTemplate.getForEntity(url, Provider.class)).thenReturn(response);

        assertNotNull(restClient.getProvider(providerType, providerId));

        Mockito.verify(restTemplate).getForEntity(url, Provider.class);

        assertEquals(sessionId, restClient.getSessionId());

    }

    // QueryResult getAggregation(String tapestryId, String threadId) throws LoomClientException;
    @Test
    public void getAggregation() {
        String tapestryId = "tapestryId";
        String threadId = "threadId";

        QueryResult queryResult = new QueryResult();
        ResponseEntity<QueryResult> response = new ResponseEntity<QueryResult>(queryResult, HttpStatus.ACCEPTED);

        String url =
                restClient.baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId + "/results";
        when(restTemplate.getForEntity(url, QueryResult.class)).thenReturn(response);

        assertNotNull(restClient.getAggregation(tapestryId, threadId));

        Mockito.verify(restTemplate).getForEntity(url, QueryResult.class);

        assertEquals(sessionId, restClient.getSessionId());

    }

    // QueryResultElement getItem(String logicalId);
    @Test
    public void getItem() {
        String logicalId = "logicalId";

        QueryResultElement queryResultElement = new QueryResultElement();
        ResponseEntity<QueryResultElement> response =
                new ResponseEntity<QueryResultElement>(queryResultElement, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.ITEM;
        when(restTemplate.postForEntity(url, logicalId, QueryResultElement.class)).thenReturn(response);

        assertNotNull(restClient.getItem(logicalId));

        Mockito.verify(restTemplate).postForEntity(url, logicalId, QueryResultElement.class);

        assertEquals(sessionId, restClient.getSessionId());
    }

    // ActionResult executeAction(Action action) throws LoomClientException;
    @Test
    public void executeAction() {

        Action action = new Action();
        ActionResult actionResult = new ActionResult();
        ResponseEntity<ActionResult> response = new ResponseEntity<ActionResult>(actionResult, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.ACTIONS_BASE;
        when(restTemplate.postForEntity(url, action, ActionResult.class)).thenReturn(response);

        assertNotNull(restClient.executeAction(action));

        Mockito.verify(restTemplate).postForEntity(url, action, ActionResult.class);

        assertEquals(sessionId, restClient.getSessionId());


    }

    // ItemTypeList getItemTypes(String providerType, String providerId) throws LoomClientException;
    @Test
    public void getItemTypesByProviderTypeAndId() {
        String providerType = "providerType";
        String providerId = "providerId";

        ItemTypeList itemTypeList = new ItemTypeList();
        ResponseEntity<ItemTypeList> response = new ResponseEntity<ItemTypeList>(itemTypeList, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.ITEM_TYPE + "/" + providerType + "/" + providerId;
        when(restTemplate.getForEntity(url, ItemTypeList.class)).thenReturn(response);

        assertNotNull(restClient.getItemTypes(providerType, providerId));

        Mockito.verify(restTemplate).getForEntity(url, ItemTypeList.class);

        assertEquals(sessionId, restClient.getSessionId());

    }

    // ItemTypeList getItemTypes(String providerType) throws LoomClientException;
    @Test
    public void getItemTypesByProviderType() {

        String providerType = "providerType";

        ItemTypeList itemTypeList = new ItemTypeList();
        ResponseEntity<ItemTypeList> response = new ResponseEntity<ItemTypeList>(itemTypeList, HttpStatus.ACCEPTED);

        String url = restClient.baseUrl + ApiConfig.ITEM_TYPE + "/" + providerType;
        when(restTemplate.getForEntity(url, ItemTypeList.class)).thenReturn(response);

        assertNotNull(restClient.getItemTypes(providerType));

        Mockito.verify(restTemplate).getForEntity(url, ItemTypeList.class);

        assertEquals(sessionId, restClient.getSessionId());

    }

}
