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
package com.hp.hpl.loom.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.RateLimiter;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.api.exceptions.ApiThrottlingException;
import com.hp.hpl.loom.api.exceptions.BadRequestException;
import com.hp.hpl.loom.api.util.ModelValidator;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.AccessExpiredException;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchUserException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDefinitionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.action.ActionManager;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.adapter.GenerateSchema;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.query.OperationManager;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.query.QueryManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.ItemTypeList;
import com.hp.hpl.loom.model.Operation;
import com.hp.hpl.loom.model.OperationList;
import com.hp.hpl.loom.model.Parameter;
import com.hp.hpl.loom.model.ParameterEnum;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.QueryResultList;
import com.hp.hpl.loom.model.RelationshipType;
import com.hp.hpl.loom.model.RelationshipTypeSet;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;
import com.hp.hpl.loom.model.Status;
import com.hp.hpl.loom.relationships.ConnectedRelationships;
import com.hp.hpl.loom.relationships.ConnectedToRelationship;
import com.hp.hpl.loom.relationships.RelationshipsModelImpl;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinitionList;
import com.hp.hpl.loom.tapestry.ThreadDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinitionList;

@Controller
public class LoomServiceImpl implements LoomService, ActionService, PatternDefinitionService, ProviderService,
        TapestryDefinitionService, QueryService, ServletContextAware, ItemTypeService, OperationService {

    private static final String PROVIDER_ANY = "any";
    private static final Log LOG = LogFactory.getLog(LoomServiceImpl.class);
    private static Status status;

    @Autowired
    private AdapterManager adapterManager;
    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    private Tacker stitcher;
    @Autowired
    private ActionManager actionManager;
    @Autowired
    private QueryManager queryManager;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private TapestryManager tapestryManager;
    @Autowired
    private ItemTypeManager itemTypeManager;
    @Autowired
    private ModelValidator modelValidator;
    @Autowired
    private OperationManager operationManager;

    @Autowired
    private AdapterLoader adapterLoader;

    private ServletContext servletContext;
    private RateLimiter rateLimiter;

    @Value("${api.rate.limit}")
    protected Integer maxReqsPerSecond;

    public LoomServiceImpl() {}

    @PostConstruct
    public void init() {
        rateLimiter = RateLimiter.create(maxReqsPerSecond);
    }

    @Override
    @RequestMapping(value = ApiConfig.PROVIDERS_BASE + "/{providerType}/{providerId}", method = RequestMethod.POST,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public PatternDefinitionList logProvider(@PathVariable final String providerType,
            @PathVariable final String providerId, @RequestParam final String operation,
            @RequestBody(required = false) final Credentials creds,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchProviderException, SessionAlreadyExistsException,
            NoSuchSessionException, UserAlreadyConnectedException, NoSuchUserException {

        if (rateLimiter.tryAcquire()) {
            if (StringUtils.isBlank(providerType)) {
                throw new BadRequestException("Empty provider type parameter");
            }

            if (StringUtils.isBlank(providerId)) {
                throw new BadRequestException("Empty provider id parameter");
            }

            if (StringUtils.isBlank(operation)) {
                throw new BadRequestException("Empty operation parameter");
            }

            Provider provider = getProvider(providerType, providerId);
            if (provider == null) {
                throw new NoSuchProviderException(providerType, providerId);
            }

            Session session = sessionManager.getSessionWithSessionId(sessionId, response);
            synchronized (session) {
                return handleLogOperation(provider, session, operation, creds, response);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    private PatternDefinitionList handleLogOperation(final Provider provider, final Session session,
            final String operation, final Credentials creds, final HttpServletResponse response)
            throws NoSuchUserException, SessionAlreadyExistsException, UserAlreadyConnectedException,
            NoSuchProviderException, NoSuchSessionException {
        if (rateLimiter.tryAcquire()) {
            if (operation.equals("login")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logging into " + provider.getProviderType() + "/" + provider.getProviderId()
                            + " with session " + session.getId());
                }

                if (!provider.authenticate(creds)) {
                    throw new NoSuchUserException("Access denied", null);
                }

                return validateLogin(provider, session, response, creds);
            } else if (operation.equals("logout")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logging out from " + provider.getProviderType() + "/" + provider.getProviderId());
                }
                logoutProvider(provider, session.getId(), response);
                return null;
            } else {
                throw new BadRequestException("Invalid login/logout operation: " + operation);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    // Suppressing the warning for the session being unused as we will make use of it later.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private PatternDefinitionList validateLogin(final Provider provider, final Session session,
            final HttpServletResponse response, final Credentials creds) throws NoSuchSessionException,
            SessionAlreadyExistsException, NoSuchProviderException, UserAlreadyConnectedException {
        if (rateLimiter.tryAcquire()) {
            if (getProvider(provider.getProviderType(), provider.getProviderId()) != null) {
                aggregationManager.createSession(session);
                stitcher.createSession(session);

                try {
                    adapterManager.userConnected(session, provider, creds);
                    session.addProvider(provider, false);
                } catch (UserAlreadyConnectedException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("UserAlreadyConnected for provider " + provider.getProviderType() + "/"
                                + provider.getProviderId());
                    }
                } catch (NoSuchProviderException | NoSuchSessionException e) {
                    throw e;
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logged into " + provider.getProviderType() + "/" + provider.getProviderId());
                }
                return getPatternsProvider(session, provider);
            } else {
                throw new NoSuchProviderException(provider);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    private void logoutProvider(final Provider provider, final String sessionId, final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchProviderException, NoSuchUserException {
        if (rateLimiter.tryAcquire()) {
            if (sessionId == null) {
                throw new NoSuchSessionException(sessionId);
            } else {
                Session session = sessionManager.getSession(sessionId, response);
                if (session == null) {
                    throw new NoSuchSessionException(new SessionImpl(sessionId, sessionManager.getInterval()));
                }
                adapterManager.userDisconnected(session, provider, null);
                queryManager.clear(session);
                session.removeProvider(provider);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logged out from " + provider.getProviderType() + "/" + provider.getProviderId());
                }

                if (session.getProviders().isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deleting session, not connected to any provider");
                    }
                    stitcher.deleteSession(session);
                    aggregationManager.deleteSession(session);
                    sessionManager.releaseSession(session, response);
                }
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.PROVIDERS_BASE, method = RequestMethod.GET, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ProviderList getProviders(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) {
        if (rateLimiter.tryAcquire()) {
            sessionManager.getSession(sessionId, response);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all providers");
            }

            ProviderList providerList = new ProviderList();
            providerList.getProviders().addAll(adapterManager.getProviders());
            return providerList;
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.PROVIDERS_BASE + "/{providerType}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ProviderList getProviders(@PathVariable final String providerType,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) {
        if (rateLimiter.tryAcquire()) {

            if (providerType == null || StringUtils.isBlank(providerType) || providerType.equals("null")) {
                throw new BadRequestException("Empty provider type parameter");
            }

            sessionManager.getSession(sessionId, response);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all providers for type " + providerType);
            }

            ProviderList providerList = new ProviderList();
            providerList.getProviders().addAll(adapterManager.getProviders(providerType));
            return providerList;
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.PROVIDERS_BASE + "/{providerType}/{providerId}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public Provider getProvider(@PathVariable final String providerType, @PathVariable final String providerId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchProviderException {
        if (rateLimiter.tryAcquire()) {
            sessionManager.getSession(sessionId, response);

            modelValidator.validateProviderType(providerType);
            modelValidator.validateProviderId(providerId);


            if (LOG.isDebugEnabled()) {
                LOG.debug("Get provider " + providerType + Provider.PROV_SEPARATOR + providerId);
            }
            return getProvider(providerType, providerId);
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }


    @Override
    @RequestMapping(value = ApiConfig.PROVIDERS_BASE, method = RequestMethod.POST, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    public void logoutAllProviders(@RequestParam final String operation,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchUserException, NoSuchProviderException {
        if (rateLimiter.tryAcquire()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Logout from all providers");
            }
            if (StringUtils.isBlank(operation)) {
                throw new BadRequestException("Null operation parameter");
            }
            if (!operation.equals("logout")) {
                throw new BadRequestException("Invalid operation: " + operation);
            }
            if (sessionId == null) {
                throw new NoSuchSessionException();
            }

            Session session = modelValidator.validateSession(sessionId, response);

            Map<Provider, Boolean> providers = session.getProviders();
            for (Provider provider : providers.keySet()) {
                adapterManager.userDisconnected(session, provider, null);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Logged out from " + provider.getProviderType() + "/" + provider.getProviderId());
                }
            }
            queryManager.clear(session);
            providers.clear();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Deleting session, not connected to any provider");
            }

            stitcher.deleteSession(session);
            aggregationManager.deleteSession(session);
            sessionManager.releaseSession(session, response);

        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    private Provider getProvider(final String providerType, final String providerId) throws NoSuchProviderException {
        return adapterManager.getProvider(providerType, providerId);
    }

    // Suppressing the warning for the session being unused as we will make use of it later.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private PatternDefinitionList getPatternsProvider(final Session session, final Provider provider)
            throws NoSuchProviderException {
        PatternDefinitionList patternDefinitionList = new PatternDefinitionList();
        List<PatternDefinition> patterns = patternDefinitionList.getPatterns();
        patterns.addAll(tapestryManager.getPatterns(provider));
        return patternDefinitionList;
    }

    @Override
    @RequestMapping(value = ApiConfig.PATTERNS_BASE, method = RequestMethod.GET, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public PatternDefinitionList getPatterns(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all patterns for all providers");
            }

            synchronized (session) {
                PatternDefinitionList patternDefinitionList = new PatternDefinitionList();
                List<PatternDefinition> patterns = patternDefinitionList.getPatterns();
                patterns.addAll(tapestryManager.getAllPatterns());
                return patternDefinitionList;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.PATTERNS_BASE + "/{patternId}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public PatternDefinition getPattern(@PathVariable final String patternId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws InterruptedException, NoSuchSessionException, NoSuchPatternException {
        if (rateLimiter.tryAcquire()) {
            if (StringUtils.isBlank(patternId)) {
                throw new BadRequestException("Empty pattern id parameter");
            }

            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get pattern with id: " + patternId);
            }

            synchronized (session) {
                PatternDefinition patternDefinition = tapestryManager.getPattern(patternId);
                return patternDefinition;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    private TapestryDefinition setOrUpdateTapestryDefinition(final Session session,
            final TapestryDefinition tapestryDefinition)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        return tapestryManager.setTapestryDefinition(session, tapestryDefinition);
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE, method = RequestMethod.POST, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public TapestryDefinition createTapestryDefinition(@RequestBody final TapestryDefinition tapestryDefinition,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, LogicalIdAlreadyExistsException, OperationException,
            NoSuchThreadDefinitionException, InvalidQueryInputException, InvalidQueryParametersException,
            NoSuchItemTypeException, ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Create tapestry definition " + tapestryDefinition + " with session " + sessionId);
            }

            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryDefinition, true);
                return setOrUpdateTapestryDefinition(session, tapestryDefinition);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}", method = RequestMethod.PUT,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void updateTapestryDefinition(@PathVariable final String tapestryId,
            @RequestBody final TapestryDefinition tapestryDefinition,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, LogicalIdAlreadyExistsException, OperationException,
            NoSuchThreadDefinitionException, InvalidQueryInputException, InvalidQueryParametersException,
            NoSuchItemTypeException, ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Update tapestry definition: " + tapestryDefinition + " with session ID " + sessionId);
            }

            LOG.info("Update tapestry definition: " + tapestryDefinition + " with session ID " + sessionId);
            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("updateTapestryDefinition start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, true);
                setOrUpdateTapestryDefinition(session, tapestryDefinition);

                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("updateTapestryDefinition end time=" + watch);
                }
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public TapestryDefinition getTapestryDefinition(@PathVariable final String tapestryId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get tapestry definition with id: " + tapestryId);
            }

            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryId);
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                // querying with any tapestryId would work as well as the tapestryManager works
                // based on session, not id.
                return tapestryDefinition;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE, method = RequestMethod.GET, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public TapestryDefinitionList getTapestryDefinitions(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all tapestry definitions");
            }

            synchronized (session) {
                // Ultimately, this needs to be changed based on user rather than session.
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                ArrayList<TapestryDefinition> tapestryDefinitions = new ArrayList<>(1);
                if (tapestryDefinition != null) {
                    tapestryDefinitions.add(tapestryDefinition);
                }
                return new TapestryDefinitionList(tapestryDefinitions);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}", method = RequestMethod.DELETE,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void deleteTapestryDefinition(@PathVariable final String tapestryId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Delete tapestry definition with id: " + tapestryId);
            }

            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryId);
                // Ultimately, this needs to be a specific tapestry not based on sesssion.
                tapestryManager.clearTapestryDefinition(session);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE, method = RequestMethod.DELETE, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void deleteTapestryDefinitions(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Delete all tapestry definitions");
            }

            synchronized (session) {
                // Ultimately, this needs to invoke a method in tapestry manager to remove all
                // tapestries rather than the only one bound with a session.
                tapestryManager.clearTapestryDefinition(session);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads", method = RequestMethod.POST,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void createThreadDefinition(@PathVariable final String tapestryId,
            @RequestBody final ThreadDefinition threadDefinition,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException,
            NoSuchAggregationException, NoSuchQueryDefinitionException, OperationException,
            LogicalIdAlreadyExistsException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDefinitionAlreadyExistsException, NoSuchThreadDefinitionException, ThreadDeletedByDynAdapterUnload {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Create thread definition: " + threadDefinition.getId() + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("createThreadDefinition start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                // should get tapestry based on tapestryId instead
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, false);
                modelValidator.validateThreadDefinition(threadDefinition);

                // using exceptions for a common case is not the best approach, replaced by code
                // after the TODO below

                // try {
                // tapestryDefinition.getThreadDefinition(threadDefinition.getId());
                // throw new ThreadDefinitionAlreadyExistsException(tapestryId,
                // threadDefinition.getId());
                // } catch (NoSuchThreadDefinitionException e) {
                // if (LOG.isDebugEnabled()) {
                // LOG.debug("ThreadDefinition does not exist, create one from the given
                // threadDefinition");
                // }
                // }

                // shouldn't this be the responsibility of the tapestryDefinition data
                // structure?
                boolean found = tapestryDefinition.getThreads().stream().map(t -> t.getId())
                        .filter(id -> id.equals(threadDefinition.getId())).count() == 1f;

                if (!found) {
                    tapestryDefinition.addThreadDefinition(threadDefinition);
                    queryManager.preCreateQueryResponses(session, tapestryDefinition);
                } else {
                    throw new ThreadDefinitionAlreadyExistsException(tapestryId, threadDefinition.getId());
                }

                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("createThreadDefinition end time=" + watch);
                }
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads/{threadId}", method = RequestMethod.PUT,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void updateThreadDefinition(@PathVariable final String tapestryId, @PathVariable final String threadId,
            @RequestBody(required = false) final ThreadDefinition threadDefinition,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            NoSuchThreadDefinitionException, NoSuchItemTypeException, InvalidQueryInputException,
            InvalidQueryParametersException, NoSuchAggregationException, NoSuchQueryDefinitionException,
            OperationException, LogicalIdAlreadyExistsException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Update thread definition: " + threadId + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("updateThreadDefinition start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                // should get tapestry based on tapestryId instead
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, false);
                modelValidator.validateThreadDefinition(threadId, threadDefinition);

                tapestryDefinition.updateThreadDefinition(threadId, threadDefinition);
                queryManager.preCreateQueryResponses(session, tapestryDefinition);


                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("updateThreadDefinition end time=" + watch);
                }
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads/{threadId}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ThreadDefinition getThreadDefinition(@PathVariable final String tapestryId,
            @PathVariable final String threadId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchThreadDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get thread definition: " + threadId + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("getThreadDefinition start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                // should get tapestry based on tapestryId instead
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, false);
                modelValidator.validateThreadDefinition(threadId);
                ThreadDefinition threadDefinition = tapestryDefinition.getThreadDefinition(threadId);

                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("getThreadDefinition end time=" + watch);
                }

                return threadDefinition;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads/{threadId}", method = RequestMethod.DELETE,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void deleteThreadDefinition(@PathVariable final String tapestryId, @PathVariable final String threadId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchThreadDefinitionException,
            NoSuchAggregationException, NoSuchQueryDefinitionException, OperationException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, InvalidQueryParametersException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload, NoSuchItemTypeException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Delete thread definition: " + threadId + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleteThreadDefinition start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                // should get tapestry based on tapestryId instead
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, false);
                modelValidator.validateThreadDefinition(threadId);
                tapestryDefinition.removeThreadDefinition(threadId);
                queryManager.preCreateQueryResponses(session, tapestryDefinition);
                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("deleteThreadDefinition end time=" + watch);
                }
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ThreadDefinitionList getThreadDefinitions(@PathVariable final String tapestryId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get thread definitions from tapestry: " + tapestryId + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("getThreadDefinitions start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                // should get tapestry based on tapestryId instead
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, false);
                ThreadDefinitionList threads = new ThreadDefinitionList(tapestryDefinition.getThreads());

                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("getThreadDefinition end time=" + watch);
                }

                return threads;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads", method = RequestMethod.DELETE,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public void deleteThreadDefinitions(@PathVariable final String tapestryId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload, NoSuchItemTypeException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Delete all thread definitions from tapestry: " + tapestryId + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleteThreadDefinitions start");
                watch = new StopWatch();
                watch.start();
            }
            synchronized (session) {
                // should get tapestry based on tapestryId instead
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);
                modelValidator.validateTapestryDefinition(tapestryId, tapestryDefinition, false);

                tapestryDefinition.clearThreads();
                queryManager.preCreateQueryResponses(session, tapestryDefinition);
                if (LOG.isDebugEnabled()) {
                    watch.stop();
                    LOG.debug("deleteThreadDefinitions end time=" + watch);
                }
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads/{threadId}/results",
            method = RequestMethod.GET, headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public QueryResult getQueryResult(@PathVariable final String tapestryId, @PathVariable final String threadId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchSessionException, NoSuchThreadDefinitionException,
            NoSuchTapestryDefinitionException, NoSuchQueryDefinitionException, NoSuchAggregationException,
            LogicalIdAlreadyExistsException, InvalidQueryInputException, OperationException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload, NoSuchItemTypeException,
            InvalidQueryParametersException, AccessExpiredException, JsonProcessingException, NoSuchProviderException {

        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSessionAndAccess(sessionId, response, tapestryId, threadId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Query a thread with tapestryId: " + tapestryId + " and threadId: " + threadId
                        + " with session ID " + sessionId);
            }
            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryId);
                modelValidator.validateThreadDefinition(threadId);
                return queryManager.getThread(session, threadId, false);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/threads/results", method = RequestMethod.POST,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public QueryResultList getQueryResults(@PathVariable final String tapestryId,
            @RequestBody final List<String> threadIds,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchThreadDefinitionException, NoSuchTapestryDefinitionException,
            NoSuchQueryDefinitionException, NoSuchAggregationException, LogicalIdAlreadyExistsException,
            InvalidQueryInputException, OperationException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload, AccessExpiredException, JsonProcessingException, NoSuchProviderException {

        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSessionAndAccess(sessionId, response, tapestryId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Query a set of threads with tapestryId " + tapestryId + " and threadIds in: "
                        + threadIds.toString() + " with session ID " + sessionId);
            }
            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryId);
                List<QueryResult> queryResults = new ArrayList<>(threadIds.size());
                for (String threadId : threadIds) {
                    queryResults.add(queryManager.getThread(session, threadId, false));
                }
                return new QueryResultList(queryResults);
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.ACTIONS_BASE, method = RequestMethod.POST, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ActionResult doAction(@RequestBody final Action action,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws InvalidActionSpecificationException, NoSuchProviderException,
            NoSuchSessionException, NoSuchItemTypeException {
        if (rateLimiter.tryAcquire()) {
            if (action == null) {
                throw new BadRequestException("Action object cannot be null");
            }
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Execute action " + action + " for session " + sessionId);
            }
            synchronized (session) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Assigned session " + session.getId());
                }
                ActionResult actionResult = actionManager.doAction(session, action);
                return actionResult;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.ACTION_RESULTS_BASE + "/{actionResultId}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ActionResult getActionResult(@PathVariable final String actionResultId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws InvalidActionSpecificationException, NoSuchProviderException,
            NoSuchSessionException, NoSuchItemTypeException {
        if (rateLimiter.tryAcquire()) {
            if (actionResultId == null) {
                throw new BadRequestException("actionResultId cannot be null");
            }
            UUID uuid = UUID.fromString(actionResultId);
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Lookup action result " + actionResultId + " for session " + sessionId);
            }
            synchronized (session) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Assigned session " + session.getId());
                }
                ActionResult actionResult = actionManager.getActionResult(session, uuid);
                return actionResult;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }


    @Override
    @RequestMapping(value = ApiConfig.ITEM, method = RequestMethod.POST, headers = "Accept=*/*",
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public QueryResultElement getItem(@RequestBody final String logicalId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchProviderException, NoSuchSessionException, NoSuchItemTypeException, NoSuchItemException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get individual item " + logicalId + " for session " + sessionId);
            }

            synchronized (session) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Assigned session " + session.getId());
                }

                if (StringUtils.isBlank(logicalId)) {
                    throw new BadRequestException("Empty logical id parameter");
                }

                Aggregation containing = adapterManager.getAggregationForItem(session, logicalId);
                if (containing == null) {
                    LOG.error("Could not find aggregation for item " + logicalId);
                    throw new NoSuchItemException("Could not find item " + logicalId);
                }

                Item resultingItem = null;
                if (!containing.containsAggregations()) {
                    for (Item item : containing.getContainedItems()) {
                        if (item.getLogicalId().equals(logicalId)) {
                            resultingItem = item;
                            break;
                        }
                    }
                }

                if (resultingItem == null) {
                    LOG.error("Could not find item " + logicalId + " in aggregation.");
                    throw new NoSuchItemException("Could not find item " + logicalId);
                }

                QueryResultElement qre = new QueryResultElement();
                qre.setEntity(resultingItem);
                return qre;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.API_STATUS, method = RequestMethod.GET, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public Status getStatus(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) {
        if (!rateLimiter.tryAcquire()) {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }

        // cached to save having to make the call each time
        // read the manifest from the WAR file and build up a JSON response
        try {
            modelValidator.validateSession(sessionId, response);
        } catch (NoSuchSessionException e) {
            LOG.info("Guest user checks for server status.");
        }

        if (status == null) {
            status = new Status();
            InputStream is = null;
            try {
                // manifest isn't available during mocking
                is = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
                if (is != null) {
                    final Manifest manifest = new Manifest(is);
                    final Attributes attr = manifest.getMainAttributes();

                    status.setBuild(attr.getValue("Implementation-Title"));
                    status.setVersion(attr.getValue("Implementation-Version"));
                } else {
                    status.setBuild("Problem accessing build");
                    status.setVersion("Problem accessing version");
                }
            } catch (IOException e) {
                status.setBuild("Problem accessing build");
                status.setVersion("Problem accessing version");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // build the adapter info

        status.setAdapters(adapterLoader.getAdapterDetails());
        status.setStatusEvents(adapterLoader.getStatusEvents());

        return status;
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    @RequestMapping(value = ApiConfig.ITEM_TYPE + "/{providerType}/{providerId}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ItemTypeList getItemTypes(@PathVariable final String providerType, @PathVariable final String providerId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchProviderException {
        if (rateLimiter.tryAcquire()) {
            sessionManager.getSession(sessionId, response);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all itemTypes for a provider");
            }

            modelValidator.validateProviderType(providerType);
            modelValidator.validateProviderId(providerId);

            Provider provider = getProvider(providerType, providerId);
            return new ItemTypeList(new HashSet<>(itemTypeManager.getItemTypes(provider)));
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.ITEM_TYPE + "/{providerType}", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ItemTypeList getItemTypes(@PathVariable final String providerType,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) {
        if (rateLimiter.tryAcquire()) {
            modelValidator.validateProviderType(providerType);
            sessionManager.getSession(sessionId, response);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all itemTypes for a provider type");
            }

            return new ItemTypeList(new HashSet<>(itemTypeManager.getItemTypes(providerType)));
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.ITEM_TYPE, method = RequestMethod.GET, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public ItemTypeList getItemTypes(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            @RequestParam(value = "current", required = false) final boolean current,
            final HttpServletResponse response) throws NoSuchSessionException {
        if (rateLimiter.tryAcquire()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get all itemTypes for all provider types");
            }
            Set<ItemType> itemTypes = new HashSet<>();
            ItemTypeList itemTypeList = new ItemTypeList();
            Session session = modelValidator.validateSession(sessionId, response);
            synchronized (session) {
                if (current) {
                    for (Provider provider : session.getProviders().keySet()) {
                        try {
                            itemTypes.addAll(itemTypeManager.getItemTypes(provider));
                        } catch (NoSuchProviderException e) {
                        }
                    }
                } else {
                    itemTypes.addAll(itemTypeManager.getItemTypes());
                }
                itemTypeList.setItemTypes(itemTypes);
                return itemTypeList;
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.OPERATION, method = RequestMethod.GET, headers = ApiConfig.API_HEADERS,
            produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public OperationList getOperations(@RequestParam(value = "itemType", required = false) final String itemType,
            @RequestParam(value = "providerType", required = false) final String providerType,
            @RequestParam(value = "declaredBy", required = false) final String declaredBy,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) {
        if (rateLimiter.tryAcquire()) {
            sessionManager.getSession(sessionId, response);

            if (!StringUtils.isBlank(providerType) && declaredBy == null && itemType == null) {
                return getOperationsOperateOn(providerType);
            } else if (providerType == null && !StringUtils.isBlank(declaredBy) && itemType == null) {
                return getOperationsDeclaredBy(declaredBy);
            } else if (!StringUtils.isBlank(itemType) && providerType == null && declaredBy == null) {
                return getOperationsByItemType(itemType);
            } else if (itemType == null && providerType == null && declaredBy == null) {
                return getOperationsOperateOn(PROVIDER_ANY);
            } else {
                throw new BadRequestException(
                        "A request parameter required, either providerType or declaredBy or itemType");
            }
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    private List<String> providerOperationId(final ItemType itemType, final String operationId) {
        List<String> ids = new ArrayList<>();
        List<Provider> providers = adapterManager.getProviders();
        for (Provider p : providers) {
            Collection<ItemType> providerIds;
            try {
                providerIds = itemTypeManager.getItemTypes(p);
                if (providerIds.contains(itemType)) {
                    String id = (p.getProviderTypeAndId() + Provider.PROV_SEPARATOR + operationId);
                    ids.add(id);
                }
            } catch (NoSuchProviderException e) {

                e.printStackTrace();
            }
        }
        return ids;
    }

    private OperationList getOperationsOperateOn(final String providerType) {
        List<String> defaultOperationIds = operationManager.getDefault();
        if (providerType.toLowerCase().equals(PROVIDER_ANY)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get info on all operations that can operate on any ItemType");
            }

            List<Operation> operations = new ArrayList<>(0);
            Map<String, Operation> operationsMap = new HashMap<>();
            Collection<ItemType> itemTypes = itemTypeManager.getItemTypes();
            for (ItemType itemType : itemTypes) {
                Set<String> operationNames = itemType.getOperations().keySet();

                for (String operationId : operationNames) {
                    QuadFunctionMeta quadFunctionMeta = operationManager.getOperation(operationId);
                    if (quadFunctionMeta == null) {
                        List<String> ids = providerOperationId(itemType, operationId);
                        for (String id : ids) {
                            quadFunctionMeta = operationManager.getOperation(id);
                            if (quadFunctionMeta != null) {
                                Operation op = createOperation(id, quadFunctionMeta);
                                operationsMap.put(id, op);
                            }
                        }
                    } else {
                        Operation op = createOperation(operationId, quadFunctionMeta);
                        operationsMap.put(operationId, op);
                    }
                }
            }
            for (Operation operation : operationsMap.values()) {
                operations.add(operation);
            }

            getOperationList(itemTypes, operations, defaultOperationIds, Inclusion.INCLUDE);
            return new OperationList(operations);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get info on all operations that can operate on ItemType declared by " + providerType);
            }

            Map<String, Operation> operationsMap = new HashMap<>();
            Collection<ItemType> itemTypes = itemTypeManager.getItemTypes(providerType);
            for (ItemType itemType : itemTypes) {
                Set<String> operationNames = itemType.getOperations().keySet();

                for (String operationId : operationNames) {
                    QuadFunctionMeta quadFunctionMeta = operationManager.getOperation(operationId);
                    if (quadFunctionMeta == null) {
                        List<String> ids = providerOperationId(itemType, operationId);
                        for (String id : ids) {
                            quadFunctionMeta = operationManager.getOperation(id);
                            if (quadFunctionMeta != null) {
                                Operation op = createOperation(id, quadFunctionMeta);
                                operationsMap.put(id, op);
                            }
                        }
                    } else {
                        Operation op = createOperation(operationId, quadFunctionMeta);
                        operationsMap.put(operationId, op);
                    }
                }
            }
            List<Operation> operations = new ArrayList<>(0);
            for (Operation operation : operationsMap.values()) {
                operations.add(operation);
            }

            getOperationList(itemTypes, operations, defaultOperationIds, Inclusion.INCLUDE);
            return new OperationList(operations);
        }
    }

    private OperationList getOperationsByItemType(final String itemTypeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get info on all operations that can operate on this ItemType: " + itemTypeId);
        }

        List<Operation> operations = new ArrayList<>(0);
        Map<String, Operation> operationsMap = new HashMap<>();

        ItemType itemType = itemTypeManager.getItemType(itemTypeId);
        if (itemType != null) {
            Collection<ItemType> items = new ArrayList<>();
            Set<String> operationNames = itemType.getOperations().keySet();
            items.add(itemType);

            for (String operationId : operationNames) {
                QuadFunctionMeta quadFunctionMeta = operationManager.getOperation(operationId);
                if (quadFunctionMeta == null) {
                    List<String> ids = providerOperationId(itemType, operationId);
                    for (String id : ids) {
                        quadFunctionMeta = operationManager.getOperation(id);
                        if (quadFunctionMeta != null) {
                            Operation op = createOperation(id, quadFunctionMeta);
                            operationsMap.put(id, op);
                        }
                    }
                } else {
                    Operation op = createOperation(operationId, quadFunctionMeta);
                    operationsMap.put(operationId, op);
                }
            }

            for (Operation operation : operationsMap.values()) {
                operations.add(operation);
            }
            List<String> defaultOperationIds = operationManager.getDefault();
            getOperationList(items, operations, defaultOperationIds, Inclusion.INCLUDE);
        }
        return new OperationList(operations);
    }

    private OperationList getOperationsDeclaredBy(final String declaredBy) {
        List<String> defaultOperationIds = operationManager.getDefault();
        if (declaredBy.toLowerCase().equals("loom")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get info on all operations declared by Loom");
            }

            List<Operation> operations = new ArrayList<>(defaultOperationIds.size());

            for (String defaultOperationId : defaultOperationIds) {
                QuadFunctionMeta quadFunctionMeta = operationManager.getOperation(defaultOperationId);
                Operation op = createOperation(defaultOperationId, quadFunctionMeta);
                operations.add(op);
            }

            Set<ItemType> itemTypes = getAllItemTypes();

            getOperationList(itemTypes, operations, defaultOperationIds, Inclusion.INCLUDE);
            return new OperationList(operations);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get info on all operations declared by " + declaredBy);
            }

            List<Operation> operations = new ArrayList<>();
            Collection<ItemType> itemTypes = itemTypeManager.getItemTypes(declaredBy);
            for (ItemType itemType : itemTypes) {
                Set<String> keys = itemType.getOperations().keySet();
                for (String key : keys) {
                    List<String> ids = providerOperationId(itemType, key);
                    for (String id : ids) {
                        QuadFunctionMeta quadFunctionMeta = operationManager.getOperation(id);
                        if (quadFunctionMeta != null) {
                            Operation op = createOperation(id, quadFunctionMeta);
                            operations.add(op);
                        }
                    }
                }
            }


            getOperationList(itemTypes, operations, defaultOperationIds, Inclusion.EXCLUDE);
            return new OperationList(operations);
        }
    }

    private Operation createOperation(final String defaultOperationId, final QuadFunctionMeta quadFunctionMeta) {
        Operation op = new Operation(defaultOperationId, quadFunctionMeta.getName());
        op.setIcon(quadFunctionMeta.getIcon());
        op.setDisplayParameters(quadFunctionMeta.getDisplayParameters());
        op.setCanExcludeItems(quadFunctionMeta.isCanExcludeItems());
        if (quadFunctionMeta.getParams() != null) {
            op.getParams().addAll(quadFunctionMeta.getParams());
        }
        return op;
    }

    private enum Inclusion {
        INCLUDE(true), EXCLUDE(false), ALL(null);
        private final Boolean include;

        Inclusion(final Boolean include) {
            this.include = include;
        }

        public Boolean getInclude() {
            return include;
        }
    }

    private void getOperationList(final Collection<ItemType> itemTypes, final List<Operation> operations,
            final Collection<String> defaultOperationIds, final Inclusion inclusion) {
        this.getOperationList(null, itemTypes, operations, defaultOperationIds, inclusion);
    }

    private void getOperationList(final String itemTypeFilter, final Collection<ItemType> itemTypes,
            final List<Operation> operations, final Collection<String> defaultOperationIds, final Inclusion inclusion) {
        for (ItemType itemType : itemTypes) {
            String itemTypeId = itemType.getId();
            if (itemTypeFilter != null && !itemTypeId.equals(itemTypeFilter)) {
                continue;
            }
            Map<String, Set<OrderedString>> itemTypeOperations = itemType.getOperations();
            for (String operationId : itemTypeOperations.keySet()) {
                if (inclusion.getInclude() == null
                        || defaultOperationIds.contains(operationId) && inclusion.getInclude()) {
                    List<Operation> operation = operations.stream().filter(op -> op.getId().equals(operationId))
                            .collect(Collectors.toList());
                    if (operation.size() > 1) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("There should not be any duplicate operations");
                        }
                    } else if (operation.size() != 0) {
                        buildOperation(itemTypeId, itemTypeOperations, operationId, operation);
                    }
                } else if (!defaultOperationIds.contains(operationId)) {

                    List<Operation> operation = operations.stream()
                            .filter(op -> op.getId().toLowerCase().endsWith(operationId.toLowerCase()))
                            .collect(Collectors.toList());
                    if (operation.size() != 0) {
                        buildOperation(itemTypeId, itemTypeOperations, operationId, operation);
                    }
                }

            }
        }
    }

    private void buildOperation(final String itemTypeId, final Map<String, Set<OrderedString>> itemTypeOperations,
            final String operationId, final List<Operation> operation) {
        for (Operation op : operation) {
            op.getItemTypes().add(itemTypeId);
            Iterator<Parameter> params = op.getParams().iterator();
            while (params.hasNext()) {
                Parameter param = params.next();
                if (param.getType() == ParameterEnum.ATTRIBUTE_LIST) {
                    Set<OrderedString> items = param.getAttributes().get(itemTypeId);
                    if (items == null) {
                        items = new HashSet<>();
                    }
                    items.addAll(itemTypeOperations.get(operationId));
                    param.getAttributes().put(itemTypeId, items);
                }
            }
        }
    }


    private Set<ItemType> getAllItemTypes() {
        Set<ItemType> itemTypes = new HashSet<>();
        for (Provider provider : adapterManager.getProviders()) {
            try {
                itemTypes.addAll(new HashSet<ItemType>(itemTypeManager.getItemTypes(provider)));
            } catch (NoSuchProviderException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("NoSuchProviderException, this should not happen!");
                }
            }
        }
        return itemTypes;
    }



    @Override
    @RequestMapping(value = ApiConfig.TAPESTRY_BASE + "/{tapestryId}/relation", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public RelationshipTypeSet getThreadRelations(@PathVariable final String tapestryId,
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchThreadDefinitionException,
            NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        if (rateLimiter.tryAcquire()) {
            Session session = modelValidator.validateSession(sessionId, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get thread definition: " + tapestryId + " with session ID " + sessionId);
            }

            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("getThreadDefinition start");
                watch = new StopWatch();
                watch.start();
            }
            RelationshipTypeSet results = new RelationshipTypeSet();
            Set<RelationshipType> relationshipTypes = new HashSet<>();
            synchronized (session) {
                modelValidator.validateTapestryDefinition(tapestryId);
                TapestryDefinition tapestryDefinition = tapestryManager.getTapestryDefinition(session);

                RelationshipsModelImpl model = new RelationshipsModelImpl();
                List<Aggregation> gas = aggregationManager.listGroundedAggregations(session);
                model.calculateClassRelationships(gas);
                for (ThreadDefinition td : tapestryDefinition.getThreads()) {

                    ConnectedRelationships connectedRelationship = model.getItemRelationshipsByItemTypeId(
                            itemTypeManager.getItemType(td.getItemType()).getLocalId());
                    if (connectedRelationship != null) {
                        List<ConnectedToRelationship> list = connectedRelationship.getConnectedTos();
                        for (ConnectedToRelationship connectedToRelationship : list) {
                            // TODO - BROKEN!!!!
                            String id = connectedToRelationship.getRelationName("BROKEN!");
                            if (connectedToRelationship.getType() != null
                                    && !connectedToRelationship.getType().equals("")) {
                                results.addItemId(connectedToRelationship.getType(),
                                        connectedToRelationship.getTypeName(), id);
                                RelationshipType r = new RelationshipType(id, connectedToRelationship.getTypeName());
                                relationshipTypes.add(r);
                            }
                        }
                    }
                }
            }
            return results;
        } else {
            throw new ApiThrottlingException("Exceeded max number of requests per second");
        }
    }

    @Override
    @RequestMapping(value = ApiConfig.PROVIDERS_BASE + "/schema", method = RequestMethod.GET,
            headers = ApiConfig.API_HEADERS, produces = {ApiConfig.API_PRODUCES})
    @ResponseBody
    public String getRelationships(
            @CookieValue(value = SessionManager.SESSION_COOKIE, required = false) final String sessionId,
            final HttpServletResponse response) throws NoSuchProviderException, NoSuchSessionException {
        GenerateSchema drawer = new GenerateSchema();
        List<Class<? extends Item>> items = new ArrayList<>();
        if (rateLimiter.tryAcquire()) {
            Reflections reflections = new Reflections("com");
            Set<Class<? extends Item>> subTypes = reflections.getSubTypesOf(Item.class);
            for (Class<? extends Item> class1 : subTypes) {
                items.add(class1);
            }
        }

        Session session = modelValidator.validateSession(sessionId, response);

        return drawer.process(stitcher, items, session.getProviders().keySet());
    }
}
