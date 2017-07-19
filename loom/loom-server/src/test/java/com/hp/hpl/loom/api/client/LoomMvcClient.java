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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.api.ApiConfig;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ItemTypeList;
import com.hp.hpl.loom.model.OperationList;
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

/**
 * Loom client for Spring testing.
 */
public class LoomMvcClient implements LoomClient {
    private static final Log LOG = LogFactory.getLog(LoomMvcClient.class);

    private String sessionId = null; // UUID.randomUUID().toString();
    private MockMvc mockMvc;

    private int timeWarning = 1000;

    private static class PrintHandler implements ResultHandler {
        private static final PrintHandler handler = new PrintHandler();

        @Override
        public void handle(final MvcResult mvcResult) throws Exception {
            if (LOG.isDebugEnabled()) {
                print();
            }
        }

        public static ResultHandler print() {
            return handler;
        }
    }

    public void changeSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public LoomMvcClient(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private String checkResult(final MvcResult result) throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();
        assertNotNull("Content was null", content);
        assertTrue("Content was empty", content.length() > 0);
        return content;
    }

    private <T> T perform(MockHttpServletRequestBuilder requestBuilder, String bodyStr, final java.lang.Class<T> tClass,
            final boolean noSession) throws Exception {
        if (bodyStr == null) {
            bodyStr = "";
        }
        requestBuilder = requestBuilder.content(bodyStr);

        if (noSession) {
            requestBuilder = requestBuilder.cookie(new Cookie(LoomClient.SESSION_COOKIE, ""));
        } else {
            if (sessionId != null) {
                requestBuilder = requestBuilder.cookie(new Cookie(LoomClient.SESSION_COOKIE, sessionId));
            }
        }


        StopWatch watch = new StopWatch();
        watch.start();
        MvcResult result = tClass == null
                ? mockMvc.perform(requestBuilder).andExpect(status().isOk()).andDo(PrintHandler.print()).andReturn()
                : mockMvc.perform(requestBuilder)
                        // .andExpect(status().isOk())
                        // .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andDo(PrintHandler.print()).andReturn();

        if (result.getResponse().getStatus() == 200) {
            T resource = tClass == null ? null : new ObjectMapper().readValue(checkResult(result), tClass);

            watch.stop();
            if (LOG.isTraceEnabled()) {
                String uri = result.getRequest().getRequestURI();
                String method = result.getRequest().getMethod();
                if (watch.getTime() > timeWarning) {
                    bodyStr += " WARNING:";
                }
                LOG.trace(method + " " + uri + " " + bodyStr + " Operation took " + watch);
            }

            Cookie[] c = result.getResponse().getCookies();
            Cookie loomCookie = null;
            for (Cookie element : c) {
                if (element.getName().equals(LoomClient.SESSION_COOKIE)) {
                    loomCookie = element;
                }
            }

            if (loomCookie != null) {
                if (loomCookie.getMaxAge() != 0) {
                    sessionId = loomCookie.getValue();
                } else {
                    sessionId = null;
                }
            } // TODO: LOOM-622 work around for cookie issue
            return resource;
        } else {
            System.out.println(result);
            System.out.println(result.getResponse().getStatus());
            System.out.println(result.getResponse().getContentAsString());
            throw new LoomClientException("", result.getResponse().getStatus());
        }
    }

    private void performVoid(MockHttpServletRequestBuilder requestBuilder, String bodyStr, final boolean noSession)
            throws Exception {
        if (bodyStr == null) {
            bodyStr = "";
        }
        requestBuilder = requestBuilder.content(bodyStr);


        if (noSession) {
            requestBuilder = requestBuilder.cookie(new Cookie(LoomClient.SESSION_COOKIE, ""));
        } else {
            if (sessionId != null) {
                requestBuilder = requestBuilder.cookie(new Cookie(LoomClient.SESSION_COOKIE, sessionId));
            }
        }

        StopWatch watch = new StopWatch();
        watch.start();
        MvcResult result =
                mockMvc.perform(requestBuilder).andExpect(status().isOk()).andDo(PrintHandler.print()).andReturn();
        watch.stop();

        Cookie[] c = result.getResponse().getCookies();
        Cookie loomCookie = null;
        for (Cookie element : c) {
            if (element.getName().equals(LoomClient.SESSION_COOKIE)) {
                loomCookie = element;
            }
        }

        if (loomCookie != null) {
            if (loomCookie.getMaxAge() != 0) {
                sessionId = loomCookie.getValue();
            } else {
                sessionId = null;
            }
        } // TODO: LOOM-622 work around for cookie issue
    }

    @Override
    public PatternDefinitionList getPatterns() throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.PATTERNS_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, PatternDefinitionList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public PatternDefinition getPattern(final String patternId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.PATTERNS_BASE + "/" + patternId).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, PatternDefinition.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    // @Override
    @Override
    public TapestryDefinition createTapestryDefinition(final TapestryDefinition tapestryDefinition)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.TAPESTRY_BASE).contentType(MediaType.APPLICATION_JSON);
            TapestryDefinition createdTapestryDefinition = perform(requestBuilder,
                    new ObjectMapper().writeValueAsString(tapestryDefinition), TapestryDefinition.class, false);
            return createdTapestryDefinition;
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ActionResult executeAction(final Action action) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.ACTIONS_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, new ObjectMapper().writeValueAsString(action), ActionResult.class, false);

        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public TapestryDefinition getTapestry(final String tapId) throws LoomClientException {
        try {

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.TAPESTRY_BASE + "/" + tapId).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, TapestryDefinition.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void updateTapestryDefinition(final String tapestryId, final TapestryDefinition tapestryDefinition)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .put(ApiConfig.TAPESTRY_BASE + "/" + tapestryId).contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, new ObjectMapper().writeValueAsString(tapestryDefinition), false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    /**
     * Included for testing only: remove as all functionality gets completed
     */
    @Override
    public QueryResult getAggregation(final String tapestryId, final String threadId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId + "/results")
                    .contentType(MediaType.APPLICATION_JSON);

            return perform(requestBuilder, null, QueryResult.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public QueryResultList getAggregations(final String tapestryId, final List<String> threadIds)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/results")
                            .contentType(MediaType.APPLICATION_JSON);

            return perform(requestBuilder, new ObjectMapper().writeValueAsString(threadIds), QueryResultList.class,
                    false);
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public TapestryDefinitionList getTapestryDefinitions() throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.TAPESTRY_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, TapestryDefinitionList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteTapestryDefinition(final String tapestryId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(ApiConfig.TAPESTRY_BASE + "/" + tapestryId).contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteTapestryDefinitions() throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.delete(ApiConfig.TAPESTRY_BASE).contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void createThreadDefinition(final String tapestryId, final ThreadDefinition threadDefinition)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads")
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, new ObjectMapper().writeValueAsString(threadDefinition), false);
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void updateThreadDefinition(final String tapestryId, final String threadId,
            final ThreadDefinition threadDefinition) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.put(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId)
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, new ObjectMapper().writeValueAsString(threadDefinition), false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ThreadDefinition getThreadDefinition(final String tapestryId, final String threadId)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId)
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ThreadDefinition.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteThreadDefinition(final String tapestryId, final String threadId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.delete(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId)
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ThreadDefinitionList getThreadDefinitions(final String tapestryId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads")
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ThreadDefinitionList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteThreadDefinitions(final String tapestryId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.delete(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads")
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public PatternDefinitionList loginProvider(final String providerType, final String providerId,
            final Credentials credentials) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=login")
                    .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, new ObjectMapper().writeValueAsString(credentials),
                    PatternDefinitionList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }


    @Override
    public void logoutProvider(final String providerType, final String providerId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=logout")
                    .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void logoutAllProviders() throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "?operation=logout").contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public QueryResultElement getItem(final String logicalId) {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.ITEM).contentType(MediaType.TEXT_PLAIN);
            return perform(requestBuilder, logicalId, QueryResultElement.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ProviderList getProviders() throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.PROVIDERS_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ProviderList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ProviderList getProviders(final String providerType) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.PROVIDERS_BASE + "/" + providerType).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ProviderList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public Provider getProvider(final String providerType, final String providerId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId)
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, Provider.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }



    @Override
    public ItemTypeList getItemTypes(final String providerType, final String providerId) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.ITEM_TYPE + "/" + providerType + "/" + providerId)
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ItemTypeList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.ITEM_TYPE + "/" + providerType).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ItemTypeList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ItemTypeList getItemTypes() throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.ITEM_TYPE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ItemTypeList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public OperationList getOperations(final String providerType, final String declaredBy) throws LoomClientException {
        try {
            String url = ApiConfig.OPERATION;
            if (providerType != null && declaredBy == null) {
                url += "?providerType=" + providerType;
            } else if (providerType == null && declaredBy != null) {
                url += "?declaredBy=" + declaredBy;
            } else {
                url += "?providerType=" + providerType + "&declaredBy=" + declaredBy;
            }
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, OperationList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public PatternDefinitionList getPatterns(final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.PATTERNS_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, PatternDefinitionList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public PatternDefinition getPattern(final String patternId, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.PATTERNS_BASE + "/" + patternId).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, PatternDefinition.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    // @Override
    @Override
    public TapestryDefinition createTapestryDefinition(final TapestryDefinition tapestryDefinition,
            final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.TAPESTRY_BASE).contentType(MediaType.APPLICATION_JSON);
            TapestryDefinition createdTapestryDefinition = perform(requestBuilder,
                    new ObjectMapper().writeValueAsString(tapestryDefinition), TapestryDefinition.class, noSession);
            return createdTapestryDefinition;
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ActionResult executeAction(final Action action, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.ACTIONS_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, new ObjectMapper().writeValueAsString(action), ActionResult.class,
                    noSession);

        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public TapestryDefinition getTapestry(final String tapId, final boolean noSession) throws LoomClientException {
        try {

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.TAPESTRY_BASE + "/" + tapId).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, TapestryDefinition.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void updateTapestryDefinition(final String tapestryId, final TapestryDefinition tapestryDefinition,
            final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .put(ApiConfig.TAPESTRY_BASE + "/" + tapestryId).contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, new ObjectMapper().writeValueAsString(tapestryDefinition), noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    /**
     * Included for testing only: remove as all functionality gets completed
     */
    @Override
    public QueryResult getAggregation(final String tapestryId, final String threadId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId + "/results")
                    .contentType(MediaType.APPLICATION_JSON);

            return perform(requestBuilder, null, QueryResult.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public QueryResultList getAggregations(final String tapestryId, final List<String> threadIds,
            final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/results")
                            .contentType(MediaType.APPLICATION_JSON);

            return perform(requestBuilder, new ObjectMapper().writeValueAsString(threadIds), QueryResultList.class,
                    noSession);
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public TapestryDefinitionList getTapestryDefinitions(final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.TAPESTRY_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, TapestryDefinitionList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteTapestryDefinition(final String tapestryId, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .delete(ApiConfig.TAPESTRY_BASE + "/" + tapestryId).contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteTapestryDefinitions(final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.delete(ApiConfig.TAPESTRY_BASE).contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void createThreadDefinition(final String tapestryId, final ThreadDefinition threadDefinition,
            final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads")
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, new ObjectMapper().writeValueAsString(threadDefinition), noSession);
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void updateThreadDefinition(final String tapestryId, final String threadId,
            final ThreadDefinition threadDefinition, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.put(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId)
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, new ObjectMapper().writeValueAsString(threadDefinition), noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ThreadDefinition getThreadDefinition(final String tapestryId, final String threadId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId)
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ThreadDefinition.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteThreadDefinition(final String tapestryId, final String threadId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.delete(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId)
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ThreadDefinitionList getThreadDefinitions(final String tapestryId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads")
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ThreadDefinitionList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void deleteThreadDefinitions(final String tapestryId, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.delete(ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads")
                            .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public PatternDefinitionList loginProvider(final String providerType, final String providerId,
            final Credentials credentials, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=login")
                    .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, new ObjectMapper().writeValueAsString(credentials),
                    PatternDefinitionList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }


    @Override
    public PatternDefinitionList loginProviderBadOp(final String providerType, final String providerId,
            final Credentials credentials, final String op) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=" + op)
                    .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, new ObjectMapper().writeValueAsString(credentials),
                    PatternDefinitionList.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void logoutProvider(final String providerType, final String providerId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=logout")
                    .contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public void logoutAllProviders(final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .post(ApiConfig.PROVIDERS_BASE + "?operation=logout").contentType(MediaType.APPLICATION_JSON);
            performVoid(requestBuilder, null, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public QueryResultElement getItem(final String logicalId, final boolean noSession) {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.post(ApiConfig.ITEM).contentType(MediaType.TEXT_PLAIN);
            return perform(requestBuilder, logicalId, QueryResultElement.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public ProviderList getProviders(final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.PROVIDERS_BASE).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ProviderList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ProviderList getProviders(final String providerType, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.PROVIDERS_BASE + "/" + providerType).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ProviderList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public Provider getProvider(final String providerType, final String providerId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId)
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, Provider.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public Status getStatus() {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.API_STATUS).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, Status.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType, final String providerId, final boolean noSession)
            throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(ApiConfig.ITEM_TYPE + "/" + providerType + "/" + providerId)
                            .contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ItemTypeList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType, final boolean noSession) throws LoomClientException {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.ITEM_TYPE + "/" + providerType).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ItemTypeList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public OperationList getOperations(final String providerType, final String declaredBy, final boolean noSession)
            throws LoomClientException {
        try {
            String url = ApiConfig.OPERATION;
            if (providerType != null && declaredBy == null) {
                url += "?providerType=" + providerType;
            } else if (providerType == null && declaredBy != null) {
                url += "?declaredBy=" + declaredBy;
            } else {
                url += "?providerType=" + providerType + "&declaredBy=" + declaredBy;
            }
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, OperationList.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ActionResult actionResultStatus(final String actionResultId) {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.ACTION_RESULTS_BASE + "/" + actionResultId).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ActionResult.class, false);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

    @Override
    public ActionResult actionResultStatus(final String actionResultId, final boolean noSession) {
        try {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                    .get(ApiConfig.ACTION_RESULTS_BASE + "/" + actionResultId).contentType(MediaType.APPLICATION_JSON);
            return perform(requestBuilder, null, ActionResult.class, noSession);
        } catch (LoomClientException e) {
            throw e;
        } catch (Exception e) {
            throw new LoomClientException(e);
        }
    }

}
