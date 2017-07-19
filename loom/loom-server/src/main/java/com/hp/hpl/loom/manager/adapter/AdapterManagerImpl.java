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
package com.hp.hpl.loom.manager.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.adapter.AggregationUpdate;
import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.GeoAttribute;
import com.hp.hpl.loom.adapter.ItemRelationsDelta;
import com.hp.hpl.loom.adapter.LiteralAttribute;
import com.hp.hpl.loom.adapter.LoomUtils;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.adapter.ProviderItem;
import com.hp.hpl.loom.adapter.ProviderType;
import com.hp.hpl.loom.adapter.TimeAttribute;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionParameter;
import com.hp.hpl.loom.adapter.annotations.ActionRange;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LiteralRange;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.api.exceptions.BadRequestException;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.action.ActionExecutor;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.OperationManager;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.ActionResult.Status;
import com.hp.hpl.loom.model.AdminSessionImpl;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.GeoLocationAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.RelationshipUtil;
import com.hp.hpl.loom.stitcher.StitcherRulePair;
import com.hp.hpl.loom.tapestry.Meta;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;


/**
 * Implementation of the AdapterManager.
 *
 */
@Component
public class AdapterManagerImpl extends AdapterManager {
    private static final int PROVIDER_AGG_INITIAL_SIZE = 5;

    private static final String ITEM_PROVIDER_RELATIONSHIP = "item:provider";

    private static final String STITCHER_RULE_ID_SEPARATOR = ":";

    private static final Log LOG = LogFactory.getLog(AdapterManagerImpl.class);

    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    protected TapestryManager tapestryManager;
    @Autowired
    protected ItemTypeManager itemTypeManager;
    @Autowired
    protected OperationManager opManager;
    @Autowired
    protected Tacker tacker;

    @Autowired
    private SessionManager sessionManager;


    private Map<String, Adapter> adapterMap = new HashMap<>();
    private Map<String, Provider> providerMap = new HashMap<>();
    private Map<Provider, Map<String, ItemType>> providerToItemTypeMap = new HashMap<>();

    private StitcherRuleManager getStitcherRuleManager() {
        return tacker == null ? null : tacker.getStitcherRuleManager();
    }

    @Autowired
    private ActionExecutor actionExecutor;

    private Map<UUID, ActionResult> actionResults = new HashMap<>();

    private Map<Provider, ProviderType> providersMap = new HashMap<>();

    private ProviderType getProviderType(final Provider provider) {
        ProviderType type = providersMap.get(provider);
        if (type == null) {
            type = new ProviderType();
            providersMap.put(provider, type);
        }
        final String[] defaultLayer = {ItemTypeInfo.DEFAULT_LAYER};
        type.setLayers(defaultLayer);
        return type;
    }

    @Override
    public List<Provider> getProviders() {
        List<Provider> providers = new ArrayList<>();
        providers.addAll(providerMap.values());
        Collections.sort(providers, new Comparator<Provider>() {
            @Override
            public int compare(final Provider p, final Provider p2) {
                return p.getProviderName().compareTo(p2.getProviderName());
            }
        });

        return providers;
    }

    @Override
    public List<Provider> getProviders(final String pType) {
        if (StringUtils.isBlank(pType)) {
            throw new BadRequestException("provider type must not be empty");
        }
        List<Provider> providers = new ArrayList<>();
        for (Provider provider : getProviders()) {
            if (provider.getProviderType().equals(pType)) {
                providers.add(provider);
            }
        }
        return providers;
    }

    @Override
    public Provider getProvider(final String pType, final String providerId) throws NoSuchProviderException {
        if (StringUtils.isBlank(pType) || StringUtils.isBlank(providerId)) {
            throw new BadRequestException("provider type and provider id must not be empty");
        }
        Provider provider = providerMap.get(Provider.getProviderTypeAndId(pType, providerId));
        if (provider == null) {
            throw new NoSuchProviderException("cannot find a provider for : " + pType + "/" + providerId);
        }
        return provider;
    }

    @Override
    public Provider getProvider(final String itemLogicalId) throws NoSuchProviderException {
        String[] comp = itemLogicalId.split("[/]");
        if (comp.length < 2) {
            throw new NoSuchProviderException(
                    "parsing logicalId " + itemLogicalId + " is wrong - cannot find a provider: " + comp.length);
        }

        Provider provider = providerMap.get(Provider.getProviderTypeAndId(comp[0], comp[1]));

        if (provider == null) {
            throw new NoSuchProviderException("cannot find a provider for logicalId: " + itemLogicalId);
        }
        return provider;
    }

    @Override
    @SuppressWarnings({"checkstyle:emptyblock", "PMD.EmptyCatchBlock"})
    public void userConnected(final Session session, final Provider provider, final Credentials creds)
            throws NoSuchProviderException, NoSuchSessionException, UserAlreadyConnectedException {
        validateNotNull("session", session);
        validateNotNull("provider", provider);
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        if (adapter == null) {
            throw new NoSuchProviderException(provider);
        }
        adapter.userConnected(session, creds);

        try {
            String aggName = LoomUtils.getAggregationLogicalIdFromItemType(provider, ProviderType.TYPE_LOCAL_ID, null);
            Aggregation aggregation = aggregationManager.getAggregation(session, aggName);


            ProviderType providerType = getProviderType(provider);
            // add the provider aggregation if don't already have one in this session
            if (aggregation == null) {
                aggregation = aggregationManager.createGroundedAggregation(session, provider, providerType, aggName,
                        providerType.getLocalId(), "provider", "provider", PROVIDER_AGG_INITIAL_SIZE);
            }

            String itemId = provider.getProviderName() + "-" + provider.getProviderId();
            ProviderItem item = new ProviderItem(LoomUtils.getItemLogicalId(aggregation, itemId), providerType);
            item.setProviderName(provider.getProviderName());
            item.setProviderType(provider.getProviderType());
            item.setProviderId(provider.getProviderId());
            String name = provider.getProviderName() + ":" + provider.getProviderId();
            item.setName(name);
            item.setUuid(name);
            item.setGroundedAggregation(aggregation);
            ArrayList<Item> newItems = new ArrayList<Item>();
            newItems.add(item);
            item.update();

            // add in all the exist items as well
            ArrayList<Item> allItems = new ArrayList<Item>();
            allItems.add(item);
            allItems.addAll(aggregation.getContainedItems());

            UpdateResult updateResult =
                    new UpdateResult(allItems, newItems, new ArrayList<Item>(), new ArrayList<Item>());

            try {
                aggregationManager.updateGroundedAggregation(session, aggregation, updateResult);
            } catch (NoSuchAggregationException e) {
            }
        } catch (LogicalIdAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    private void validateNotEmpty(final String type, final Collection<?> item) {
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("cannot parse a null " + type);
        }
    }

    private void validateNotNull(final String type, final Object item) {
        if (item == null) {
            throw new IllegalArgumentException("cannot parse a null " + type);
        }
    }


    @Override
    public void userDisconnected(final Session session, final Provider provider, final Credentials creds)
            throws NoSuchProviderException, NoSuchSessionException {
        validateNotNull("session", session);
        validateNotNull("provider", provider);
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        if (adapter == null) {
            throw new NoSuchProviderException(provider);
        }
        adapter.userDisconnected(session, creds);

        String aggName = LoomUtils.getAggregationLogicalIdFromItemType(provider, ProviderType.TYPE_LOCAL_ID, null);
        // remove the provider aggregation if it is present
        if (aggregationManager.getAggregation(session, aggName) != null) {
            Aggregation aggregation = aggregationManager.getAggregation(session, aggName);
            String itemId = provider.getProviderName() + "-" + provider.getProviderId();
            ProviderType providerType = getProviderType(provider);
            ProviderItem item = new ProviderItem(LoomUtils.getItemLogicalId(aggregation, itemId), providerType);
            item.setProviderName(provider.getProviderName());
            item.setProviderType(provider.getProviderType());
            item.setProviderId(provider.getProviderId());
            String name = provider.getProviderName() + ":" + provider.getProviderId();
            item.setName(name);
            item.setUuid(name);
            item.setGroundedAggregation(aggregation);
            ArrayList<Item> items = new ArrayList<Item>();
            items.add(item);
            item.update();

            UpdateResult updateResult =
                    new UpdateResult(new ArrayList<Item>(), new ArrayList<Item>(), new ArrayList<Item>(), items);

            try {
                aggregationManager.updateGroundedAggregation(session, aggregation, updateResult);
            } catch (NoSuchAggregationException e) {
            }
        }
    }

    @Override
    public ActionResult doAction(final Session session, final Provider provider, final Action action,
            final String itemTypeId) throws NoSuchProviderException, NoSuchSessionException {
        validateNotNull("session", session);
        validateNotNull("provider", provider);
        validateNotNull("action", action);
        validateNotNull("itemTypeId", itemTypeId);

        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        if (adapter == null) {
            throw new NoSuchProviderException(provider);
        }
        return adapter.doAction(session, action, itemTypeId);
        // return queueAction(session, new ActionTarget(session, adapter, action, itemTypeId));
    }

    @Override
    public ActionResult doAction(final Session session, final Provider provider, final Action action,
            final Collection<Item> items) throws NoSuchProviderException, NoSuchSessionException {
        validateNotNull("session", session);
        validateNotNull("provider", provider);
        validateNotNull("action", action);
        validateNotEmpty("items", items);

        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        if (adapter == null) {
            throw new NoSuchProviderException(provider);
        }
        // return queueAction(session, new ActionTarget(session, adapter, action, items));
        return adapter.doAction(session, action, items);
    }

    // @Override
    // public ActionResult doAction(final Session session, final Provider provider, final Action
    // action,
    // final List<String> logicalIds) throws NoSuchProviderException, NoSuchSessionException {
    // validateNotNull("session", session);
    // validateNotNull("provider", provider);
    // validateNotNull("action", provider);
    // validateNotEmpty("logicalIds", logicalIds);
    //
    // Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
    // if (adapter == null) {
    // throw new NoSuchProviderException(provider);
    // }
    // return queueAction(session, new ActionTarget(session, adapter, action, logicalIds));
    // // return adapter.doAction(session, action, logicalIds);
    // }

    @Override
    public ActionResult getActionResult(final Session session, final UUID actionResultId)
            throws NoSuchSessionException {
        validateNotNull("session", session);
        // find and return action result
        return actionResults.get(actionResultId);
    }



    private ActionResult queueAction(final Session session, final ActionTarget actionTarget) {
        ActionResult actionResult = new ActionResult();
        actionResult.setId(UUID.randomUUID());
        actionResult.setOverallStatus(Status.pending);
        actionTarget.setActionResult(actionResult);

        actionResults.put(actionResult.getId(), actionResult);

        // send to thread to be processed
        actionExecutor.processAction(actionTarget);
        return actionResult;
    }

    @Override
    public void registerAdapter(final Adapter adapter) throws DuplicateAdapterException, NoSuchProviderException,
            DuplicateItemTypeException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException {
        validateAdapter(adapter);
        Provider prov = adapter.getProvider();
        if (prov == null) {
            LOG.error("null provider for non null adapter: " + adapter);
            throw new IllegalArgumentException("null provider for non null adapter");
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("registering adapter for provider " + prov);
        }
        Provider alreadyProv =
                providerMap.get(Provider.getProviderTypeAndId(prov.getProviderType(), prov.getProviderId()));
        if (alreadyProv != null) {
            throw new DuplicateAdapterException(
                    "Provider " + prov.toString() + " is duplicate: already registered type & id");
        }
        adapterMap.put(prov.getProviderTypeAndId(), adapter);
        providerMap.put(prov.getProviderTypeAndId(), prov);
        if (LOG.isInfoEnabled()) {
            LOG.info("adapter asked to register new operations");
        }
        Map<String, QuadFunctionMeta> newOps =
                adapter.registerQueryOperations(opManager.getAllFunctions(adapter.getProvider()));
        if (newOps != null) {
            for (String opId : newOps.keySet()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Registering " + opId);
                }
                opManager.registerOperation4Provider(opId, newOps.get(opId), adapter.getProvider());
            }
        }

        // Register stitching rules
        StitcherRuleManager ruleManager = getStitcherRuleManager();
        if (ruleManager != null) {
            Collection<StitcherRulePair<?, ?>> stitchRules = adapter.getStitchingRules();
            if (stitchRules != null) {
                for (StitcherRulePair<?, ?> rulePair : stitchRules) {
                    ruleManager.addStitcherRulePair(
                            prov.getProviderTypeAndId() + STITCHER_RULE_ID_SEPARATOR + rulePair.getId(), rulePair);
                }
            }
        }

        Set<ItemType> itemTypes = new HashSet<ItemType>(adapter.getItemTypes());


        // create a map
        Map<String, ItemType> itemTypeMap =
                itemTypes.stream().collect(Collectors.toMap(ItemType::getLocalId, item -> item));

        for (Class<?> itemClass : adapter.getAnnotatedItemsClasses()) {
            try {
                ItemType it = addAttributesFromItemAnnotations(adapter.getProvider(), itemClass, itemTypeMap);
                itemTypes.add(it);
            } catch (AttributeException ex) {
                LOG.error("Problem processing annotations", ex);
            }
        }

        ProviderType providerType = getProviderType(adapter.getProvider());
        itemTypeMap.put(providerType.getLocalId(), providerType);

        ItemType it;
        try {
            it = addAttributesFromItemAnnotations(adapter.getProvider(), ProviderItem.class, itemTypeMap);
            itemTypes.add(it);
        } catch (AttributeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // if (StringUtils.isEmpty(providerType.getId())) {
        // addItemType(adapter.getProvider(), providerType);
        // addToProviderItemTypeMap(adapter.getProvider(), providerType);
        // providerToItemTypeMap.get(adapter.getProvider()).put(providerType.getLocalId(),
        // providerType);
        // }


        // register the default operations for each itemType
        List<String> defaultOperationIds = opManager.getDefault();
        for (String opId : defaultOperationIds) {
            QuadFunctionMeta meta = opManager.getOperation(opId);
            if (meta.isApplyToAllItems()) {
                for (ItemType itemType : itemTypes) {
                    if (!itemType.getOperations().containsKey(opId)) {
                        Set orderedAttributes = new HashSet<OrderedString>();
                        orderedAttributes.addAll(itemType.getOrderedAttributes());
                        itemType.getOperations().put(opId, orderedAttributes);
                    }
                }
            }
        }

        registerDefinitions(adapter, itemTypes);
        registerPatterns(adapter);

        AdapterConfig config = adapter.getAdapterConfig();
        if (config != null) {
            String globalUser = config.getPropertiesConfiguration().getString("globalUser");
            String globalPassword = config.getPropertiesConfiguration().getString("globalPassword");
            if (LOG.isInfoEnabled()) {
                LOG.info("Found global user/password " + globalUser);
            }
            if (globalUser != null && globalPassword != null) {
                Credentials creds = new Credentials(globalUser, globalPassword);
                boolean auth = adapter.getProvider().authenticate(creds);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Authed = " + auth);
                }
                if (auth) {
                    Session session = sessionManager.createSuperSession(null);
                    ((AdminSessionImpl) session).setManager(creds, this);
                    try {
                        aggregationManager.createSession(session);
                        tacker.createSession(session);
                        this.userConnected(session, adapter.getProvider(), creds);
                        session.addProvider(adapter.getProvider(), false);
                    } catch (NoSuchSessionException e) {
                        LOG.warn("Problem no session found whilst creating global user account");
                    } catch (UserAlreadyConnectedException e) {
                        LOG.warn("Global user already connected");
                    } catch (SessionAlreadyExistsException e) {
                        LOG.warn("Session already exists");
                    }
                }
            }
        }
    }

    private void validateAdapter(final Adapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter is null");
        }
    }

    /**
     * Calls the adapter requesting the pattern definitions than registers them with itself.
     *
     * @param adapter The adapter to register the patterns for
     * @throws UnsupportedOperationException
     * @throws NullPatternIdException
     * @throws DuplicatePatternException
     * @throws NoSuchProviderException
     */
    @SuppressWarnings({"checkstyle:emptyblock", "PMD.EmptyCatchBlock"})
    private void registerPatterns(final Adapter adapter)
            throws NoSuchProviderException, DuplicatePatternException, NullPatternIdException {
        if (LOG.isInfoEnabled()) {
            LOG.info("adapter.getPatternDefinitions() " + adapter.getPatternDefinitions());
        }
        // register Patterns
        for (PatternDefinition pd : adapter.getPatternDefinitions()) {
            addPatternDefinition(adapter.getProvider(), pd);
        }

        String providerPattern = "Providers";
        PatternDefinition existing = null;
        try {
            existing = tapestryManager.getPattern(providerPattern);
        } catch (NoSuchPatternException e) {
        }
        if (existing == null) {
            List<ItemType> providerTypeList = new ArrayList<>();
            ProviderType providerType = getProviderType(adapter.getProvider());

            providerTypeList.add(providerType);
            PatternDefinition providerDef = createProviderPattern(providerType, providerPattern);
            addGlobalDefinition(providerDef);
        }
    }

    private PatternDefinition createProviderPattern(final ProviderType providerType, final String id) {
        List<ThreadDefinition> threadDefs = new ArrayList<>(1);
        Map<String, ItemType> itMap = new HashMap<>(1);
        int threadIdx = 0;

        List<String> ins = new ArrayList<String>(1);
        ins.add(providerType.getLocalId());
        QueryDefinition query = new QueryDefinition(ins);

        ThreadDefinition threadDefinition =
                new ThreadDefinition(id + "-" + Integer.toString(threadIdx++), providerType.getId(), query, id);
        threadDefs.add(threadDefinition);
        itMap.put(providerType.getId(), providerType);


        PatternDefinition pd = new PatternDefinition(id, threadDefs, "global", new Meta(itMap), "Provider");

        return pd;
    }


    /**
     * Registers the adapters definitions with the types derived from either adapter itself or via
     * the annotations on the items.
     *
     * @param adapter
     * @param itemTypes
     * @throws NoSuchProviderException
     * @throws DuplicateItemTypeException
     * @throws NullItemTypeIdException
     */
    private void registerDefinitions(final Adapter adapter, final Collection<ItemType> itemTypes)
            throws NoSuchProviderException, DuplicateItemTypeException, NullItemTypeIdException {
        if (LOG.isInfoEnabled()) {
            LOG.info("adapter asked to register definitions");
        }
        for (ItemType it : itemTypes) {
            addItemType(adapter.getProvider(), it);
            addToProviderItemTypeMap(adapter.getProvider(), it);
            providerToItemTypeMap.get(adapter.getProvider()).put(it.getLocalId(), it);
        }

        // if (StringUtils.isEmpty(providerType.getId())) {
        // addItemType(adapter.getProvider(), providerType);
        // addToProviderItemTypeMap(adapter.getProvider(), providerType);
        // providerToItemTypeMap.get(adapter.getProvider()).put(providerType.getLocalId(),
        // providerType);
        // }
    }

    private int getSortOrder(final Collection<Sort> orders, final DefaultOperations operation, final String element) {
        Optional<String[]> data =
                orders.stream().filter(p -> p.operation().equals(operation)).map(Sort::fieldOrder).findFirst();
        if (data.isPresent() && data.get() != null) {
            int result = Arrays.asList(data.get()).indexOf(element);
            if (result == -1) {
                result = Integer.MAX_VALUE;
            }
            return result;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private Class<?> findCoreItemAttributesType(final Class<?> item) {
        Type ciaType = null;
        Class<?> ciaClass = null;
        Type currentType = item.getGenericSuperclass();
        boolean found = false;
        // null when class is Object
        while (!found && currentType != null) {
            if (currentType instanceof Class) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found class: " + currentType.toString());
                }
                currentType = ((Class<?>) currentType).getGenericSuperclass();
            } else if (currentType instanceof ParameterizedType) {
                ciaType = ((ParameterizedType) currentType).getActualTypeArguments()[0];
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found ParameterizedType: " + ciaType);
                }
                if (ciaType instanceof Class && isSubclassOfItemAttributes((Class<?>) ciaType)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found Matching Class: " + ciaType);
                    }
                    ciaClass = (Class<?>) ciaType;
                    found = true;
                } else {
                    currentType = ((Class<?>) (((ParameterizedType) currentType).getRawType())).getGenericSuperclass();
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found unknown: " + currentType.toString());
                }
            }

        }
        return ciaClass;
    }

    private boolean isSubclassOfItemAttributes(final Class<?> item) {
        Type currentType = item.getGenericSuperclass();
        // null when class is Object
        while (currentType != null) {
            if (currentType instanceof Class) {
                if (((Class<?>) currentType).equals(CoreItemAttributes.class)) {
                    return true;
                } else {
                    currentType = ((Class<?>) currentType).getGenericSuperclass();
                }
            } else if (currentType instanceof ParameterizedType) {
                currentType = ((Class<?>) (((ParameterizedType) currentType).getRawType())).getGenericSuperclass();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found unknown: " + currentType.toString());
                }
            }

        }
        return false;
    }

    private List<Class<?>> getSuperclasses(final Class<?> ciaClass) {
        List<Class<?>> classList = new ArrayList<>();
        classList.add(ciaClass);
        Type currentType = ciaClass.getGenericSuperclass();
        // null when class is Object
        while (currentType != null) {
            if (currentType instanceof Class) {
                classList.add((Class<?>) currentType);
                if (((Class<?>) currentType).equals(CoreItemAttributes.class)) {
                    break;
                } else {
                    currentType = ((Class<?>) currentType).getGenericSuperclass();
                }
            } else if (currentType instanceof ParameterizedType) {
                classList.add((Class<?>) (((ParameterizedType) currentType).getRawType()));
                currentType = ((Class<?>) (((ParameterizedType) currentType).getRawType())).getGenericSuperclass();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found unknown: " + currentType.toString());
                }
            }
        }
        return classList;
    }

    private void setItemTypeAttributes(final Class<?> item, final ItemType itemType, final List<Sort> sorting,
            final String corePrefix) throws AttributeException {

        Map<String, List<GeoAttribute>> allGeoAttributes = new HashMap<>();

        for (Field f : item.getDeclaredFields()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Processing field " + f.getName());
            }

            if (f.isAnnotationPresent(LoomAttribute.class)) {
                LoomAttribute params = f.getAnnotation(LoomAttribute.class);
                String key = params.key();
                if (key == null) {
                    key = f.getName();
                }

                String attrName = f.getName();
                if (corePrefix != null) {
                    attrName = corePrefix + f.getName();
                }

                for (DefaultOperations op : params.supportedOperations()) {
                    addOperations(itemType, op.toString(), attrName, getSortOrder(sorting, op, key));
                }

                for (String additionalOperation : params.supportedAdditionalOperations()) {
                    addOperations(itemType, additionalOperation, attrName, Integer.MAX_VALUE);
                }

                Attribute attribute = null;
                if (params.type().equals(Attribute.class)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("add Attribute field " + f.getName() + " with name: " + attrName);
                    }
                    attribute = new Attribute.Builder(attrName).name(key).visible(params.visible())
                            .plottable(params.plottable()).mappable(params.mappable())
                            .ignoreUpdate(params.ignoreUpdate()).collectionType(params.collectionType()).build();
                    itemType.addAttributes(attribute);
                } else if (params.type().equals(NumericAttribute.class)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("add NumericAttribute field " + f.getName() + " with name: " + attrName);
                    }
                    attribute = new NumericAttribute.Builder(attrName).min(convertNULL(params.min()))
                            .max(convertNULL(params.max())).unit(convertNULL(params.unit())).name(key)
                            .visible(params.visible()).plottable(params.plottable()).mappable(params.mappable())
                            .ignoreUpdate(params.ignoreUpdate()).collectionType(params.collectionType()).build();
                    itemType.addAttributes(attribute);
                } else if (params.type().equals(GeoAttribute.class)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("add GeoAttribute field " + f.getName() + " with name: " + attrName);
                    }
                    attribute = new GeoAttribute.Builder(attrName).group(params.group()).latitude(params.latitude())
                            .longitude(params.longitude()).country(params.country()).name(key).visible(params.visible())
                            .mappable(params.mappable()).ignoreUpdate(params.ignoreUpdate())
                            .collectionType(params.collectionType()).build();
                    itemType.addAttributes(attribute);
                    List<GeoAttribute> geoAttribs = allGeoAttributes.get(params.group());
                    if (geoAttribs == null) {
                        geoAttribs = new ArrayList<>();
                    }
                    geoAttribs.add((GeoAttribute) attribute);
                    allGeoAttributes.put(params.group(), geoAttribs);
                } else if (params.type().equals(TimeAttribute.class)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("add TimeAttribute field " + f.getName() + " with name: " + attrName);
                    }
                    attribute = new TimeAttribute.Builder(attrName).name(key).shortFormat(params.shortFormat())
                            .format(params.format()).visible(params.visible()).mappable(params.mappable())
                            .ignoreUpdate(params.ignoreUpdate()).collectionType(params.collectionType()).build();
                    itemType.addAttributes(attribute);
                } else if (params.type().equals(LiteralAttribute.class)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("add LiteralAttribute field " + f.getName() + " with name: " + attrName);
                    }

                    Map<String, String> allowedValues = new HashMap<>();
                    LiteralRange[] range = params.range();
                    for (LiteralRange literalRange : range) {
                        allowedValues.put(literalRange.key(), literalRange.name());
                    }

                    attribute = new LiteralAttribute.Builder(attrName).name(key).visible(params.visible())
                            .plottable(params.plottable())
                            // .supportedOperations(supportedOperations)
                            .mappable(params.mappable()).allowedValues(allowedValues)
                            .collectionType(params.collectionType()).ignoreUpdate(params.ignoreUpdate()).build();
                    itemType.addAttributes(attribute);
                }
            }
        }

        for (String geoGroup : allGeoAttributes.keySet()) {
            List<GeoAttribute> geoPair = allGeoAttributes.get(geoGroup);
            if (geoPair.size() == 3) {
                GeoAttribute latitude = null;
                GeoAttribute longitude = null;
                GeoAttribute country = null;
                for (GeoAttribute geoAttribute : geoPair) {
                    if (geoAttribute.isLatitude()) {
                        latitude = geoAttribute;
                    } else if (geoAttribute.isLongitude()) {
                        longitude = geoAttribute;
                    } else if (geoAttribute.isCountry()) {
                        country = geoAttribute;
                    }
                }

                GeoLocationAttributes foo = new GeoLocationAttributes();
                foo.addGeoAttributes(latitude.getFieldName(), longitude.getFieldName(), country.getFieldName());
                itemType.setGeoAttributes((foo));
            }
        }

    }

    /**
     * This method creates an ItemType based on the annotations on the Item class supplied.
     *
     * @param item - Item to examine for Annotations
     * @param builderTypes - Map of the localId to type for types built with builder
     * @return Returns the ItemType corresponding to the Item's annotations
     */
    private ItemType addAttributesFromItemAnnotations(final Provider provider, final Class<?> item,
            final Map<String, ItemType> builderTypes) throws AttributeException {
        ItemTypeInfo itemTypeInfo = item.getAnnotation(ItemTypeInfo.class);
        // overlay the provided itemType with the annotation details
        String localId = itemTypeInfo.value();
        ItemType itemType = builderTypes.get(localId);
        if (itemType == null) {
            itemType = new ItemType();
            itemType.setLocalId(localId);
            itemType.setLayers(itemTypeInfo.layers());
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Found itemTypeInfo " + itemTypeInfo.value());
        }

        List<Sort> sorting = Arrays.asList(itemTypeInfo.sorting());

        // find coreItemAttributes
        Class<?> ciaClass = findCoreItemAttributesType(item);
        if (ciaClass != null) {
            for (Class<?> eachClass : getSuperclasses(ciaClass)) {
                setItemTypeAttributes(eachClass, itemType, sorting, ItemType.CORE_NAME);
            }
        }

        setItemTypeAttributes(item, itemType, sorting, null);

        List<String> supportedAdditionalOperations = Arrays.asList(itemTypeInfo.supportedAdditionalOperations());
        for (String opId : supportedAdditionalOperations) {
            itemType.addOperations(opId, new HashSet<>(0));
        }


        // process the relationships
        ConnectedTo[] relationships = item.getAnnotationsByType(ConnectedTo.class);
        for (ConnectedTo connectedTo : relationships) {

            Class<?> item2 = connectedTo.toClass();
            ItemType itemType2 = new ItemType();
            ItemTypeInfo itemTypeInfo2 = item2.getAnnotation(ItemTypeInfo.class);
            itemType2.setLocalId(itemTypeInfo2.value());
            if (LOG.isInfoEnabled()) {
                LOG.info(" connecting " + itemType.getLocalId() + " to " + itemType2.getLocalId());
            }

            LoomAttribute params = connectedTo.relationshipDetails();
            if (params != null && !params.key().equals(LoomAttribute.NULL)) {
                for (DefaultOperations op : params.supportedOperations()) {
                    addOperations(itemType, op.toString(),
                            RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType(
                                    provider.getProviderType(), itemType.getLocalId(), itemType2.getLocalId(),
                                    connectedTo.type()),
                            getSortOrder(sorting, op, params.key()));
                }
                Attribute attribute = new Attribute.Builder(
                        RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType(provider.getProviderType(),
                                itemType.getLocalId(), itemType2.getLocalId(), connectedTo.type())).name(params.key())
                                        .visible(params.visible()).plottable(params.plottable())
                                        .mappable(params.mappable()).ignoreUpdate(params.ignoreUpdate())
                                        .collectionType(params.collectionType()).build();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Rel attribute: " + attribute.getFieldName() + ", " + attribute.getName() + ", "
                            + attribute.getType());
                }
                itemType.addAttributes(attribute);
                // keyToAttribute.put(params.key(), attribute);
            } else {
                Attribute attribute = new Attribute.Builder(
                        RelationshipUtil.getRelationshipNameBetweenLocalTypeIdsWithRelType(provider.getProviderType(),
                                itemType.getLocalId(), itemType2.getLocalId(), connectedTo.type())).build();
                itemType.addAttributes(attribute);
            }
        }

        // setup the actions
        ActionDefinition[] actionDefinitions = item.getAnnotationsByType(ActionDefinition.class);
        for (ActionDefinition actionDefinition : actionDefinitions) {

            ActionParameters actionParameters = new ActionParameters();
            try {
                ActionParameter[] params = actionDefinition.parameters();
                for (ActionParameter actionParameter : params) {
                    ActionRange[] ranges = actionParameter.ranges();
                    Map<String, String> range = new HashMap<String, String>();
                    for (ActionRange actionRange : ranges) {
                        range.put(actionRange.id(), actionRange.name());
                    }

                    actionParameters.add(new com.hp.hpl.loom.model.ActionParameter(actionParameter.id(),
                            actionParameter.type(), actionParameter.name(), range));
                }


                Action action = new Action(actionDefinition.id(), actionDefinition.name(),
                        actionDefinition.description(), actionDefinition.icon(), actionParameters);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("New Action: " + action.toString());
                }

                addActionToType(itemType, actionDefinition.type(), action);
            } catch (InvalidActionSpecificationException e) {
                LOG.error("Problem setting up action for annotation: " + actionDefinition.name(), e);
            }
        }

        return itemType;
    }

    /**
     * Adds the action to the ItemType.
     *
     * @param itemType
     * @param type
     * @param action
     */
    private void addActionToType(final ItemType itemType, final ActionTypes type, final Action action) {
        Map<String, Map<String, Action>> allActions = itemType.getActions();
        String key = type.toString().toLowerCase();
        Map<String, Action> actionMap = allActions.get(key);
        if (actionMap == null) {
            actionMap = new HashMap<>();
            allActions.put(key, actionMap);
        }
        actionMap.put(action.getId(), action);
    }

    /**
     * Adds the operations to the ItemType respecting the order they are defined in the sort
     * annotation or placing them at the bottom of the list.
     *
     * @param itemType
     * @param operationType
     * @param key
     * @param order
     */
    private void addOperations(final ItemType itemType, final String operationType, final String fieldName,
            final int order) {
        Set<OrderedString> opSet = itemType.getOperations().get(operationType);

        if (opSet == null) {
            opSet = new TreeSet<OrderedString>();
            itemType.getOperations().put(operationType, opSet);
        }
        OrderedString orderedString = new OrderedString(fieldName, order);
        opSet.add(orderedString);
    }

    private String convertNULL(final String value) {
        if (value.equals(LoomAttribute.NULL)) {
            return null;
        }
        return value;
    }


    private void addToProviderItemTypeMap(final Provider provider, final ItemType it) {
        Map<String, ItemType> itemTypes = providerToItemTypeMap.get(provider);
        if (itemTypes == null) {
            itemTypes = new HashMap<>();
            providerToItemTypeMap.put(provider, itemTypes);
        }
        itemTypes.put(it.getLocalId(), it);
    }

    private void removeFromProviderItemTypeMap(final Provider provider, final ItemType it) {
        Map<String, ItemType> itemTypes = providerToItemTypeMap.get(provider);
        if (itemTypes != null) {
            itemTypes.remove(it.getLocalId());
        }
    }

    @Override
    public ItemType getItemTypeLocalId(final Provider provider, final String itemLocalId) {
        return providerToItemTypeMap.get(provider).get(itemLocalId);
    }

    @Override
    public void deregisterAdapter(final Adapter adapter, final Set<Session> sessions) throws NoSuchProviderException {
        this.validateAdapter(adapter);

        Provider prov = adapter.getProvider();
        if (prov == null) {
            throw new IllegalArgumentException("null provider for non null adapter");
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("deregistering adapter for provider " + prov);
        }

        Adapter regAdapter = adapterMap.get(prov.getProviderTypeAndId());
        if (regAdapter == null) {
            throw new NoSuchProviderException(prov);
        }

        // delete DAs and GAs associated to the adapter
        removeAggregations(sessions, prov.getProviderTypeAndId());


        // Deregister stitching rules
        StitcherRuleManager ruleManager = getStitcherRuleManager();
        if (ruleManager != null) {
            Collection<StitcherRulePair<?, ?>> stitchRules = adapter.getStitchingRules();
            if (stitchRules != null) {
                for (StitcherRulePair<?, ?> rulePair : stitchRules) {
                    ruleManager.removeStitcherRulePair(
                            prov.getProviderTypeAndId() + STITCHER_RULE_ID_SEPARATOR + rulePair.getId());
                }
            }
        }

        // remove patterns

        for (ItemType it : adapter.getItemTypes()) {
            try {
                this.removeItemType(prov, it.getId());
                removeFromProviderItemTypeMap(adapter.getProvider(), it);
            } catch (NoSuchProviderException e) {
                LOG.error("Provider is missing, during adapter deregister " + prov.getProviderName());
            } catch (NoSuchItemTypeException e) {
                LOG.error("ItemType is missing, during adapter deregister " + it.getLocalId());
            }
        }

        // deregister Patterns
        for (PatternDefinition pd : adapter.getPatternDefinitions()) {
            try {
                this.removePatternDefinition(adapter.getProvider(), pd);
            } catch (NoSuchProviderException e) {
                LOG.error("Provider is missing, during adapter deregister " + prov.getProviderName());
            } catch (NoSuchPatternException e) {
                LOG.error("Pattern is missing, during adapter deregister " + pd.getName());
            }
        }

        // regAdapter.deregisterDefinitions();


        // remove operations
        opManager.deleteOperationsForProvider(adapter.getProvider());

        // now remove adapter
        adapterMap.remove(prov.getProviderTypeAndId());
        providerMap.remove(prov.getProviderTypeAndId());
    }

    private void removeAggregations(final Set<Session> sessions, final String providerTypeAndId) {

        for (Session session : sessions) {
            try {
                List<Aggregation> gas = aggregationManager.listGroundedAggregations(session);
                for (Aggregation ga : gas) {
                    if (ga.getLogicalId().contains(providerTypeAndId)) {
                        try {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Deleting " + ga.getLogicalId() + " and children DAs");
                            }
                            aggregationManager.deleteAggregationAndChildren(session, ga.getLogicalId(), true);
                        } catch (NoSuchAggregationException e) {
                            LOG.error("Could not finish adapter deregistration for provider: " + providerTypeAndId
                                    + " -> Failed cleaning of GAs, aggregation " + ga.getLogicalId()
                                    + " does not exist");
                        }
                    }
                }
            } catch (NoSuchSessionException e) {
                LOG.error("Could not finish adapter deregistration for provider: " + providerTypeAndId
                        + " -> Failed cleaning of GAs, session " + session + " does not exist");
            }

        }
    }

    @Override
    public Aggregation getAggregationForItem(final Session session, final String itemLogicalId)
            throws NoSuchSessionException {
        return aggregationManager.getAggregation(session,
                LoomUtils.deriveAggregationLogicalIdFromItemLogicalId(itemLogicalId));
    }

    @Override
    public String getItemTypeLocalIdFromLogicalId(final Provider provider, final String logicalId)
            throws NoSuchProviderException {
        validateNotNull("provider", provider);
        validateNotNull("logicalId", provider);

        if (adapterMap.get(provider.getProviderTypeAndId()) == null) {
            throw new NoSuchProviderException(provider);
        }
        String subId = logicalId.substring(provider.getProviderTypeAndId().length() + 1);
        int markerIdx = -1;
        String key = null;
        while (true) {
            // check if removing the last character matches a typeId
            key = subId.substring(0, subId.length() - 1);
            if (itemTypeManager.getItemType(LoomUtils.getItemTypeId(provider, key)) != null) {
                break;
            }
            markerIdx = subId.lastIndexOf("/");
            if (markerIdx == -1) {
                key = null;
                break;
            } else {
                subId = subId.substring(0, markerIdx);
            }
        }
        return key;
    }

    @Override
    @SuppressWarnings("checkstyle:parameternumber")
    public Aggregation createGroundedAggregation(final Session session, final Provider provider,
            final ItemType itemType, final String localId, final boolean mapped, final String name,
            final String description, final int expectedSize)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchProviderException {
        validateNotNull("session", session);
        validateNotNull("provider", provider);

        if (itemType == null || name == null) {
            throw new IllegalArgumentException("NULL argument " + itemType + " - " + name);
        }
        String logicalId = LoomUtils.getAggregationLogicalIdFromItemType(provider, itemType.getLocalId(), localId);
        String mappedLogicalId = null;
        if (mapped) {
            mappedLogicalId = LoomUtils.getMergedLogicalIdFromItemType(provider, itemType.getLocalId(), localId);
        }
        return aggregationManager.createGroundedAggregation(session, provider, itemType, logicalId, mappedLogicalId,
                name, description, expectedSize);
    }

    @Override
    public void updateGroundedAggregation(final Session session, final Aggregation aggregation,
            final UpdateResult updateResult) throws NoSuchSessionException, NoSuchAggregationException {
        validateNotNull("session", session);
        validateNotNull("aggregation", aggregation);

        processUpdateResult(session, updateResult);

        aggregationManager.updateGroundedAggregation(session, aggregation, updateResult);
    }

    private void processUpdateResult(final Session session, final UpdateResult updateResult)
            throws NoSuchSessionException {
        Map<String, ProviderItem> pMap = new HashMap<>();
        final List<Item> items = updateResult.getNewItems();

        Collection<ItemRelationsDelta> itemRelationsDeltas = new ArrayList<>();
        for (Item item : items) {
            String providerIdTypeFromLogicalId = LoomUtils.getProviderIdFromItemLogicalId(item);

            ProviderItem providerItem = pMap.get(providerIdTypeFromLogicalId);
            if (providerItem == null) {
                providerItem = getProviderItem(session, providerIdTypeFromLogicalId);
                pMap.put(providerIdTypeFromLogicalId, providerItem);
            }
            if (providerItem != null) {
                if (updateResult.isDeltaMode()) {
                    ItemRelationsDelta delta =
                            ItemRelationsDelta.addNamedRelationToOther(providerItem, item, ITEM_PROVIDER_RELATIONSHIP);
                    itemRelationsDeltas.add(delta);
                } else {
                    providerItem.addConnectedRelationshipsWithName(ITEM_PROVIDER_RELATIONSHIP, item);
                }
            }
        }
        if (itemRelationsDeltas.size() != 0) {
            updateResult.getUpdateDelta().addItemRelationsDeltas(itemRelationsDeltas);
        }
    }

    @Override
    public void updateGroundedAggregations(final Session session,
            final Collection<AggregationUpdate> aggregationUpdates)
            throws NoSuchSessionException, NoSuchAggregationException {
        validateNotNull("session", session);
        validateNotNull("aggregationUpdates", aggregationUpdates);

        for (AggregationUpdate aggregationUpdate : aggregationUpdates) {
            UpdateResult updateResult = aggregationUpdate.getUpdateResult();

            processUpdateResult(session, updateResult);
        }
        aggregationManager.updateGroundedAggregations(session, aggregationUpdates);
    }

    private ProviderItem getProviderItem(final Session session, final String providerIdTypeFromLogicalId)
            throws NoSuchSessionException {

        Aggregation agg = aggregationManager.getAggregation(session, providerIdTypeFromLogicalId);
        ProviderItem providerItem = null;

        if (agg != null && agg.getNumberOfFibres() == 1) {
            providerItem = (ProviderItem) agg.get(0);
        }
        return providerItem;
    }



    @Override
    public void deleteGroundedAggregation(final Session session, final String logicalId)
            throws NoSuchSessionException, NoSuchAggregationException {
        validateNotNull("session", session);
        validateNotNull("logicalId", logicalId);

        aggregationManager.deleteGroundedAggregation(session, logicalId);
    }

    // /////////////////////// Pattern definition

    @Override
    public Collection<String> addPatternDefinitions(final Provider provider,
            final Collection<PatternDefinition> patterns)
            throws NoSuchProviderException, DuplicatePatternException, NullPatternIdException {
        validateNotNull("provider", provider);
        validateNotEmpty("patterns", patterns);
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        if (adapter == null) {
            throw new NoSuchProviderException(provider);
        }
        return setIdAndForward(provider, patterns);
    }

    private Collection<String> setIdAndForward(final Provider provider, final Collection<PatternDefinition> patterns)
            throws DuplicatePatternException, NoSuchProviderException, NullPatternIdException {
        for (PatternDefinition pattern : patterns) {
            LoomUtils.setPatternId(provider, pattern);
        }
        return tapestryManager.addPatternDefinitions(provider, patterns);
    }

    @Override
    public String addPatternDefinition(final Provider provider, final PatternDefinition pattern)
            throws NoSuchProviderException, DuplicatePatternException, NullPatternIdException {
        validateNotNull("provider", provider);
        validateNotNull("pattern", pattern);

        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());

        validateAdapter(adapter);
        LoomUtils.setPatternId(provider, pattern);
        return tapestryManager.addPatternDefinition(provider, pattern);
    }

    @Override
    public String addGlobalDefinition(final PatternDefinition pattern)
            throws DuplicatePatternException, NullPatternIdException {
        validateNotNull("pattern", pattern);

        return tapestryManager.addGlobalDefinition(pattern);
    }

    @Override
    public String removePatternDefinition(final Provider provider, final PatternDefinition pattern)
            throws NoSuchProviderException, NoSuchPatternException {
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        validateAdapter(adapter);
        String removedId = tapestryManager.removePatternDefinition(provider, pattern);
        LoomUtils.unsetPatternId(provider, pattern);
        return removedId;
    }

    @Override
    public Collection<String> removePatternDefinitions(final Provider provider)
            throws NoSuchProviderException, NoSuchPatternException {
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        validateAdapter(adapter);
        return tapestryManager.removePatternDefinitions(provider);
    }

    @Override
    public String addItemType(final Provider provider, final ItemType itemType)
            throws NoSuchProviderException, DuplicateItemTypeException, NullItemTypeIdException {

        if (itemType == null) {
            throw new IllegalArgumentException("itemType should not be null");
        }
        validateNotNull("provider", provider);
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        validateAdapter(adapter);
        String typeId = setIdAndForward(provider, itemType);
        addToProviderItemTypeMap(provider, itemType);

        return typeId;
    }

    private String setIdAndForward(final Provider provider, final ItemType itemType)
            throws DuplicateItemTypeException, NullItemTypeIdException, NoSuchProviderException {
        LoomUtils.setId(provider, itemType);
        return itemTypeManager.addItemType(provider, itemType);
    }

    @Override
    public String removeItemType(final Provider provider, final String itemTypeId)
            throws NoSuchProviderException, NoSuchItemTypeException {
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        validateAdapter(adapter);
        return itemTypeManager.removeItemType(provider, itemTypeId);
    }

    @Override
    public boolean removeAllItemTypes(final Provider provider) throws NoSuchProviderException, NoSuchItemTypeException {
        Adapter adapter = adapterMap.get(provider.getProviderTypeAndId());
        validateAdapter(adapter);
        return itemTypeManager.removeAllItemTypes(provider);
    }
}


/**
 * This orders the orderedStrings based on the getOrder within them.
 */
class OrderedStringComp implements Comparator<OrderedString> {

    @Override
    public int compare(final OrderedString e1, final OrderedString e2) {
        return e1.getOrder() - e2.getOrder();
    }
}
