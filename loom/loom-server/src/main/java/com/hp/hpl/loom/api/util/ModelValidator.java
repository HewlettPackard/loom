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
package com.hp.hpl.loom.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.hpl.loom.api.exceptions.BadRequestException;
import com.hp.hpl.loom.exceptions.AccessExpiredException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@Component
public class ModelValidator {
    private static final Log LOG = LogFactory.getLog(ModelValidator.class);

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private TapestryManager tapestryManager;

    @Autowired
    private ItemTypeManager itemTypeManager;

    public ModelValidator() {}

    public Session validateSession(final String sessionId, final HttpServletResponse response)
            throws NoSuchSessionException {
        if (StringUtils.isBlank(sessionId)) {
            throw new NoSuchSessionException();
        }

        Session session = sessionManager.getSession(sessionId, response);
        if (session == null) {
            throw new NoSuchSessionException(sessionId);
        }
        return session;
    }

    public Session validateSessionAndAccess(final String sessionId, final HttpServletResponse response,
            final String tapestryId) throws NoSuchSessionException, AccessExpiredException, JsonProcessingException,
            NoSuchThreadDefinitionException, NoSuchProviderException {
        Session session = validateSession(sessionId, response);
        TapestryDefinition td = tapestryManager.getTapestryDefinition(session);
        List<ThreadDefinition> threads = td.getThreads();
        for (ThreadDefinition thread : threads) {
            String itemTypeId = thread.getItemType();
            validateAccess(itemTypeId, session);
        }
        return session;
    }

    private void validateAccess(final String itemTypeId, final Session session)
            throws NoSuchProviderException, AccessExpiredException, JsonProcessingException {
        Map<Provider, Boolean> providers = session.getProviders();
        ArrayList<Provider> providersRequireReAuth = new ArrayList<>();
        for (Provider provider : providers.keySet()) {
            provider.getProviderType();
            if (providers.get(provider)) {
                Collection<ItemType> itemTypes = itemTypeManager.getItemTypes(provider);
                for (ItemType itemType : itemTypes) {
                    if (itemType.getId().equals(itemTypeId)) {
                        providersRequireReAuth.add(provider);
                        break;
                    }
                }
            }
        }
        if (providersRequireReAuth.size() > 0) {
            ProviderList providerList = new ProviderList();
            providerList.setProviders(providersRequireReAuth);
            throw new AccessExpiredException(session, providerList);
        }
    }

    public Session validateSessionAndAccess(final String sessionId, final HttpServletResponse response,
            final String tapestryId, final String threadId) throws NoSuchSessionException, AccessExpiredException,
            JsonProcessingException, NoSuchThreadDefinitionException, NoSuchProviderException {
        Session session = validateSession(sessionId, response);
        TapestryDefinition td = tapestryManager.getTapestryDefinition(session);
        if (td != null) {
            ThreadDefinition thread = td.getThreadDefinition(threadId);
            String itemTypeId = thread.getItemType();
            validateAccess(itemTypeId, session);
        }
        return session;
    }

    public void validateTapestryDefinition(final String tapestryId, final TapestryDefinition tapestryDefinition,
            final boolean checkThreads)
            throws NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        validateTapestryDefinition(tapestryId);
        validateTapestryDefinition(tapestryDefinition, checkThreads);

        if (!tapestryDefinition.getId().equals(tapestryId)) {
            throw new BadRequestException("Provided Id does not match session tapestry");
        }
    }

    public void validateTapestryDefinition(final String tapestryId) {

        if (StringUtils.isBlank(tapestryId) || tapestryId.equals("null")) {
            throw new BadRequestException("Empty tapestry id parameter");
        }
    }

    public void validateTapestryDefinition(final TapestryDefinition tapestryDefinition, final boolean checkThreads)
            throws NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        if (tapestryDefinition == null) {
            throw new BadRequestException("Null tapestry definition parameter");
        }
        if (tapestryDefinition.getThreads() == null || tapestryDefinition.getThreads().size() == 0) {
            throw new BadRequestException("Null threads in tapestry definition parameter");
        }

        if (checkThreads) {
            for (ThreadDefinition thread : tapestryDefinition.getThreads()) {
                validateThreadDefinition(thread);
            }
        }
    }

    public void validateThreadDefinition(final String threadId)
            throws NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        if (StringUtils.isBlank(threadId)) {
            throw new BadRequestException("Empty thread id parameter");
        }
    }

    public void validateThreadDefinition(final String threadId, final ThreadDefinition threadDefinition)
            throws NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {

        validateThreadDefinition(threadId);

        if (!threadDefinition.getId().equals(threadId)) {
            throw new BadRequestException(
                    "Path variable threadId does not correspond to id in the provided ThreadDefinition");
        }

        validateThreadDefinition(threadDefinition);

    }

    public void validateThreadDefinition(final ThreadDefinition thread)
            throws NoSuchItemTypeException, InvalidQueryInputException, InvalidQueryParametersException {
        if (thread == null) {
            throw new BadRequestException("Null thread definition parameter");
        }
        if (StringUtils.isBlank(thread.getId()) || StringUtils.isBlank(thread.getItemType())) {
            LOG.error("Thread definition is malformed: empty threadId or item type");
            throw new BadRequestException("Thread definition is malformed: empty threadId or item type");
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("ThreadID " + thread.getId() + "; query " + thread.getQuery() + "; type "
                        + thread.getItemType());
            }
        }

        ItemType itemType = itemTypeManager.getItemType(thread.getItemType());
        if (itemType == null) {
            throw new NoSuchItemTypeException(thread.getItemType());
        }

        validateQueryDefinition(thread.getQuery());
    }

    private void validateQueryDefinition(final QueryDefinition query)
            throws InvalidQueryInputException, InvalidQueryParametersException {
        if (query == null) {
            throw new BadRequestException("Null query definition parameter");
        }

        validateQueryInputs(query.getInputs());
        validateQueryOperationPipeline(query.getOperationPipeline());
    }

    private void validateQueryInputs(final List<String> inputIds) throws InvalidQueryInputException {
        if (inputIds == null || inputIds.size() == 0) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Invalid Query Input: The query requires at least one input logical ID to be processed.");
            }
            throw new InvalidQueryInputException("Invalid Query Input: The query requires at least one input.");
        }

        for (String inputId : inputIds) {
            validateSingleInput(inputId);
        }
    }

    private void validateSingleInput(final String inputId) throws InvalidQueryInputException {
        if (StringUtils.isBlank(inputId)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Invalid Query Input: one of the inputs to the query was empty.");
            }
            throw new InvalidQueryInputException("Invalid Query Input: one of the inputs to the query was empty.");
        }
    }

    private void validateQueryOperationPipeline(final List<Operation> operationPipeline)
            throws InvalidQueryParametersException {
        if (operationPipeline == null) {
            throw new InvalidQueryParametersException("Empty operation pipeline in query definition parameter");
        }

        for (Operation operation : operationPipeline) {
            validateQueryOperation(operation);
        }
    }

    private void validateQueryOperation(final Operation operation) throws InvalidQueryParametersException {
        if (operation == null) {
            throw new InvalidQueryParametersException(
                    "Invalid Operation: one of the operations in the pipeline was empty");
        }
        if (StringUtils.isBlank(operation.getOperator())) {
            throw new InvalidQueryParametersException("Invalid Query Operation: operation operator was empty");
        }

    }

    public void validateProviderType(final String providerType) {
        if (providerType == null || StringUtils.isBlank(providerType) || providerType.equals("null")) {
            throw new BadRequestException("Empty provider type parameter");
        }
    }

    public void validateProviderId(final String providerId) {
        if (providerId == null || StringUtils.isBlank(providerId) || providerId.equals("null")) {
            throw new BadRequestException("Empty provider type parameter");
        }
    }

}
