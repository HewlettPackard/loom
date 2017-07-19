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

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

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
 * REST implementation of LoomClient.
 */
public class RestClient implements LoomClient {

    protected static Log log = LogFactory.getLog(RestClient.class);

    private String sessionId = null; // "test" + UUID.randomUUID().toString();

    protected String baseUrl = "http://overrideme:8080/warp";

    /**
     * Private constructor for the CookieRestTemplate
     *
     * This is responsible for adding the Cookie header to each request.
     *
     */
    private class CookieRestTemplate extends RestTemplate {
        @Override
        protected ClientHttpRequest createRequest(final URI url, final HttpMethod method) throws IOException {
            ClientHttpRequest request = super.createRequest(url, method);
            if (sessionId != null) {
                request.getHeaders().add("Cookie", LoomClient.SESSION_COOKIE + "=" + sessionId);
            }
            return request;
        }
    }

    /**
     * Default constructor.
     */
    public RestClient() {}

    protected RestTemplate restTemplate = new CookieRestTemplate();
    private static final int TIME_WARNING_DEFAULT = 1000;
    protected static int timewarning = TIME_WARNING_DEFAULT;

    private boolean setErrorHandler = false;

    private ErrorResponseErrorHandler errorHandler = new ErrorResponseErrorHandler();

    /**
     * This class is a custom Error Handler for the responses it catches the exceptions and
     * re-throws a LoomClientException.
     */
    private class ErrorResponseErrorHandler extends DefaultResponseErrorHandler {
        public ErrorResponseErrorHandler() {
            super();
        }

        @Override
        public void handleError(final ClientHttpResponse response) throws IOException {
            HttpStatus statusCode = response.getStatusCode();
            String statusText = response.getStatusText();
            //

            log.error("Handle error " + statusCode + " " + statusText);
            switch (statusCode.series()) {
                case CLIENT_ERROR:
                    throw new LoomClientException(statusText, statusCode.value());
                case SERVER_ERROR:
                    String body = IOUtils.toString(response.getBody());
                    log.error("Server error " + statusCode + " body " + body);
                    throw new LoomClientException(statusText + " body: " + body, statusCode.value());
                default:
                    throw new LoomClientException("Unknown status code [" + statusCode + "]", statusCode.value());
            }
        }
    }

    private void setErrorHandler() {
        if (!setErrorHandler) {
            restTemplate.setErrorHandler(errorHandler);
            setErrorHandler = true;
        }
    }

    private <T> T post(final String url, final Object bodyObj, final java.lang.Class<T> tClass,
            final Object... uriVariables) {
        setErrorHandler();
        StopWatch watch = new StopWatch();
        watch.start();
        // T object = restTemplate.postForObject(url, bodyObj, tClass, uriVariables);
        ResponseEntity<T> response = restTemplate.postForEntity(url, bodyObj, tClass, uriVariables);
        HttpHeaders headers = response.getHeaders();

        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null && cookies.size() > 0) {
            for (String cookie : cookies) {
                if (cookie.substring(0, cookie.indexOf("=")).equals(LoomClient.SESSION_COOKIE)) {
                    sessionId = cookie.substring(cookie.indexOf("=") + 1, cookie.indexOf(";"));
                    if (sessionId.equals("")) {
                        sessionId = null;
                    }
                    break;
                }
            }
        }

        watch.stop();
        if (log.isTraceEnabled()) {
            // if (watch.getTime() > timeWarning) {
            // bodyStr += " WARNING:";
            // }
            String args = "";
            for (Object arg : uriVariables) {
                args += " " + arg;
            }
            log.trace("POST " + url + " " + args + " " + bodyObj + " Operation took " + watch);
        }

        return response.getBody();
        // return object;
    }

    private <T> void put(final String url, final Object bodyObj, final java.lang.Class<T> tClass,
            final Object... uriVariables) {
        setErrorHandler();
        StopWatch watch = new StopWatch();
        watch.start();
        restTemplate.put(url, bodyObj, tClass, uriVariables);
        watch.stop();
        if (log.isTraceEnabled()) {
            // if (watch.getTime() > timeWarning) {
            // bodyStr += " WARNING:";
            // }
            String args = "";
            for (Object arg : uriVariables) {
                args += " " + arg;
            }
            log.trace("PUT " + url + " " + args + " " + bodyObj + " Operation took " + watch);
        }
    }

    private <T> void delete(final String url, final Object... uriVariables) {
        setErrorHandler();
        StopWatch watch = new StopWatch();
        watch.start();
        restTemplate.delete(url, uriVariables);
        watch.stop();
        if (log.isTraceEnabled()) {
            String args = "";
            for (Object arg : uriVariables) {
                args += " " + arg;
            }
            log.trace("DELETE " + url + " " + args + " Operation took " + watch);
        }
    }

    private <T> T get(final String url, final java.lang.Class<T> tClass, final Object... uriVariables) {
        setErrorHandler();
        StopWatch watch = new StopWatch();
        watch.start();
        // T object = restTemplate.getForObject(url, tClass, uriVariables);

        ResponseEntity<T> response = restTemplate.getForEntity(url, tClass, uriVariables);
        HttpHeaders headers = response.getHeaders();

        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null && cookies.size() > 0) {
            for (String cookie : cookies) {
                if (cookie.substring(0, cookie.indexOf("=")).equals(LoomClient.SESSION_COOKIE)) {
                    sessionId = cookie.substring(cookie.indexOf("=") + 1, cookie.indexOf(";"));
                    if (sessionId.equals("")) {
                        sessionId = null;
                    }
                    break;
                }
            }
        }

        watch.stop();
        if (log.isTraceEnabled()) {
            String args = "";
            for (Object arg : uriVariables) {
                args += " " + arg;
            }
            if (watch.getTime() > timewarning) {
                log.trace("GET " + url + " " + args + " WARNING: Operation took " + watch);
            } else {
                log.trace("GET " + url + " " + args + " Operation took " + watch);
            }
        }

        return response.getBody();
        // return object;
    }

    /**
     * Set the base url for the RestClient.
     *
     * @param url Url to connect to
     */
    public void setBaseURL(final String url) {
        baseUrl = url;
    }

    @Override
    public PatternDefinitionList getPatterns() {
        String restURL = baseUrl + ApiConfig.PATTERNS_BASE;
        PatternDefinitionList patternDefinitionList = get(restURL, PatternDefinitionList.class);
        return patternDefinitionList;
    }

    @Override
    public PatternDefinition getPattern(final String patternId) {
        String restURL = baseUrl + ApiConfig.PATTERNS_BASE + "/" + patternId;
        PatternDefinition patternDefinition = get(restURL, PatternDefinition.class);
        return patternDefinition;
    }

    @Override
    public TapestryDefinition createTapestryDefinition(final TapestryDefinition tapestryDefinition) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE;
        TapestryDefinition createdTapestryDefinition = post(restURL, tapestryDefinition, TapestryDefinition.class);
        return createdTapestryDefinition;
    }

    @Override
    public TapestryDefinition getTapestry(final String tapId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapId;
        return get(restURL, TapestryDefinition.class);
    }

    @Override
    public void updateTapestryDefinition(final String tapestryId, final TapestryDefinition tapestryDefinition) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId;
        put(restURL, tapestryDefinition, Void.class);
    }

    @Override
    public QueryResult getAggregation(final String tapestryId, final String threadId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId + "/results";
        return get(restURL, QueryResult.class);
    }

    @Override
    public QueryResultList getAggregations(final String tapestryId, final List<String> threadIds) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/results";
        return post(restURL, threadIds, QueryResultList.class);
    }

    @Override
    public TapestryDefinitionList getTapestryDefinitions() {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE;
        return get(restURL, TapestryDefinitionList.class);
    }

    @Override
    public void deleteTapestryDefinition(final String tapestryId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId;
        delete(restURL);
    }

    @Override
    public void deleteTapestryDefinitions() {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE;
        delete(restURL);
    }

    @Override
    public void createThreadDefinition(final String tapestryId, final ThreadDefinition threadDefinition) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        post(restURL, threadDefinition, Void.class);
    }

    @Override
    public void updateThreadDefinition(final String tapestryId, final String threadId,
            final ThreadDefinition threadDefinition) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId;
        put(restURL, threadDefinition, Void.class);
    }

    @Override
    public ThreadDefinition getThreadDefinition(final String tapestryId, final String threadId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId;
        return get(restURL, ThreadDefinition.class);
    }

    @Override
    public void deleteThreadDefinition(final String tapestryId, final String threadId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads/" + threadId;
        delete(restURL);
    }

    @Override
    public ThreadDefinitionList getThreadDefinitions(final String tapestryId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        return get(restURL, ThreadDefinitionList.class);
    }

    @Override
    public void deleteThreadDefinitions(final String tapestryId) {
        String restURL = baseUrl + ApiConfig.TAPESTRY_BASE + "/" + tapestryId + "/threads";
        delete(restURL);
    }

    @Override
    public PatternDefinitionList loginProvider(final String providerType, final String providerId,
            final Credentials credentials) {
        String restURL =
                baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=login";
        return post(restURL, credentials, PatternDefinitionList.class);
    }

    @Override
    public PatternDefinitionList loginProviderBadOp(final String providerType, final String providerId,
            final Credentials credentials, final String op) {
        String restURL =
                baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=" + op;
        return post(restURL, credentials, PatternDefinitionList.class);
    }

    @Override
    public void logoutProvider(final String providerType, final String providerId) {
        String restURL =
                baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId + "?operation=logout";
        post(restURL, "", Void.class);
    }

    @Override
    public void logoutAllProviders() {
        String restURL = baseUrl + ApiConfig.PROVIDERS_BASE + "?operation=logout";
        post(restURL, "", Void.class);
    }

    @Override
    public QueryResultElement getItem(final String logicalId) {
        String restURL = baseUrl + ApiConfig.ITEM;
        return post(restURL, logicalId, QueryResultElement.class);
    }


    @Override
    public Status getStatus() {
        String restURL = baseUrl + ApiConfig.API_STATUS;
        return get(restURL, Status.class);
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public ProviderList getProviders() {
        String restURL = baseUrl + ApiConfig.PROVIDERS_BASE;
        ProviderList providerList = get(restURL, ProviderList.class);
        return providerList;
    }

    @Override
    public ProviderList getProviders(final String providerType) {
        String restURL = baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType;
        ProviderList providerList = get(restURL, ProviderList.class);
        return providerList;
    }

    @Override
    public Provider getProvider(final String providerType, final String providerId) {
        String restURL = baseUrl + ApiConfig.PROVIDERS_BASE + "/" + providerType + "/" + providerId;
        Provider provider = get(restURL, Provider.class);
        return provider;
    }

    @Override
    public ActionResult executeAction(final Action action) {
        String restURL = baseUrl + ApiConfig.ACTIONS_BASE;
        return post(restURL, action, ActionResult.class);
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType, final String providerId) {
        String restURL = baseUrl + ApiConfig.ITEM_TYPE + "/" + providerType + "/" + providerId;
        return get(restURL, ItemTypeList.class);
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType) {
        String restURL = baseUrl + ApiConfig.ITEM_TYPE + "/" + providerType;
        return get(restURL, ItemTypeList.class);
    }

    @Override
    public ItemTypeList getItemTypes() {
        String restURL = baseUrl + ApiConfig.ITEM_TYPE;
        return get(restURL, ItemTypeList.class);
    }

    /**
     * @return the timeWarning
     */
    public int getTimeWarning() {
        return timewarning;
    }

    /**
     * @param timeWarning the timeWarning to set
     */
    public void setTimeWarning(final int timeWarning) {
        timewarning = timeWarning;
    }

    @Override
    public OperationList getOperations(final String providerType, final String declaredBy) {
        String restURL = baseUrl + ApiConfig.OPERATION;
        if (providerType != null && declaredBy == null) {
            restURL += "?providerType=" + providerType;
        } else if (providerType == null && declaredBy != null) {
            restURL += "?declaredBy=" + declaredBy;
        } else {
            restURL += "?providerType=" + providerType + "&declaredBy=" + declaredBy;
        }
        return get(restURL, OperationList.class);
    }

    @Override
    public ProviderList getProviders(final boolean noSession) {
        return getProviders();
    }

    @Override
    public PatternDefinitionList getPatterns(final boolean noSession) {
        return getPatterns();
    }

    @Override
    public PatternDefinition getPattern(final String patternId, final boolean noSession) {
        return getPattern(patternId);
    }

    @Override
    public TapestryDefinition createTapestryDefinition(final TapestryDefinition tapestryDefinition,
            final boolean noSession) {
        return createTapestryDefinition(tapestryDefinition);
    }

    @Override
    public ActionResult executeAction(final Action action, final boolean noSession) {
        return executeAction(action);
    }

    @Override
    public TapestryDefinition getTapestry(final String tapId, final boolean noSession) {
        return getTapestry(tapId);
    }

    @Override
    public void updateTapestryDefinition(final String tapestryId, final TapestryDefinition tapestryDefinition,
            final boolean noSession) {
        updateTapestryDefinition(tapestryId, tapestryDefinition);

    }

    @Override
    public QueryResult getAggregation(final String tapestryId, final String threadId, final boolean noSession) {
        // TODO Auto-generated method stub
        return getAggregation(tapestryId, threadId);
    }

    @Override
    public QueryResultList getAggregations(final String tapestryId, final List<String> threadIds,
            final boolean noSession) {
        return getAggregations(tapestryId, threadIds);
    }

    @Override
    public TapestryDefinitionList getTapestryDefinitions(final boolean noSession) {
        return getTapestryDefinitions();
    }

    @Override
    public void deleteTapestryDefinition(final String tapestryId, final boolean noSession) {
        deleteTapestryDefinition(tapestryId);
    }

    @Override
    public void deleteTapestryDefinitions(final boolean noSession) {
        deleteTapestryDefinitions();
    }

    @Override
    public void createThreadDefinition(final String tapestryId, final ThreadDefinition threadDefinition,
            final boolean noSession) {
        createThreadDefinition(tapestryId, threadDefinition);
    }

    @Override
    public void updateThreadDefinition(final String tapestryId, final String threadId,
            final ThreadDefinition threadDefinition, final boolean noSession) {
        updateThreadDefinition(tapestryId, threadId, threadDefinition);
    }

    @Override
    public ThreadDefinition getThreadDefinition(final String tapestryId, final String threadId,
            final boolean noSession) {
        return getThreadDefinition(tapestryId, threadId);
    }

    @Override
    public void deleteThreadDefinition(final String tapestryId, final String threadId, final boolean noSession) {
        deleteThreadDefinition(tapestryId, threadId);
    }

    @Override
    public ThreadDefinitionList getThreadDefinitions(final String tapestryId, final boolean noSession) {
        return getThreadDefinitions(tapestryId);
    }

    @Override
    public void deleteThreadDefinitions(final String tapestryId, final boolean noSession) {
        deleteThreadDefinitions(tapestryId);
    }

    @Override
    public PatternDefinitionList loginProvider(final String providerType, final String providerId,
            final Credentials credentials, final boolean noSession) {
        return loginProvider(providerType, providerId, credentials);
    }

    @Override
    public void logoutProvider(final String providerType, final String providerId, final boolean noSession) {
        logoutProvider(providerType, providerId);
    }

    @Override
    public void logoutAllProviders(final boolean noSession) {
        logoutAllProviders();
    }

    @Override
    public QueryResultElement getItem(final String logicalId, final boolean noSession) {
        return getItem(logicalId);
    }

    @Override
    public ProviderList getProviders(final String providerType, final boolean noSession) {
        return getProviders(providerType);
    }

    @Override
    public Provider getProvider(final String providerType, final String providerId, final boolean noSession) {
        return getProvider(providerType, providerId);
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType, final String providerId, final boolean noSession) {
        return getItemTypes(providerType, providerId);
    }

    @Override
    public ItemTypeList getItemTypes(final String providerType, final boolean noSession) {
        return getItemTypes(providerType);
    }

    @Override
    public OperationList getOperations(final String providerType, final String declaredBy, final boolean noSession) {
        return getOperations(providerType, declaredBy);
    }

    @Override
    public ActionResult actionResultStatus(final String actionResultId) {
        String restURL = baseUrl + ApiConfig.ACTION_RESULTS_BASE + "/" + actionResultId;
        return get(restURL, ActionResult.class);
    }

    @Override
    public ActionResult actionResultStatus(final String actionResultId, final boolean noSession) {
        return actionResultStatus(actionResultId);
    }

}
