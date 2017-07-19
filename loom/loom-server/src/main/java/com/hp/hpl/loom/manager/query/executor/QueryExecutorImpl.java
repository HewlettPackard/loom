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
package com.hp.hpl.loom.manager.query.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.IntermediateAggregationHandler;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationContextImpl;
import com.hp.hpl.loom.manager.query.OperationManager;
import com.hp.hpl.loom.manager.query.OperationManagerImpl;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QueryResultCache;
import com.hp.hpl.loom.manager.query.QueryResultFormattingHelper;
import com.hp.hpl.loom.manager.query.utils.StatUtils;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.relationships.RelationshipCalculator;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@Component
public class QueryExecutorImpl implements QueryExecutor, IntermediateAggregationHandler {
    private static final int USER_GROUP_DEFAULT = 45;

    private static final int QUERY_RESULT_CACHE_SIZE = 600;

    private static final Log LOG = LogFactory.getLog(QueryExecutorImpl.class);

    public static final String DA = "/da/";

    @Autowired
    private OperationManager opManager;

    @Autowired
    private AdapterManager adapterManager;

    @Autowired
    private AggregationManager aggregationManager;

    @Autowired
    private RelationshipCalculator relationshipCalculator;

    @Autowired
    private Tacker stitcher;

    // @Autowired
    // private TapestryManager tapestryManager;

    @Value("${include.timing}")
    private boolean timing = false;

    @Autowired
    private ItemTypeManager itemTypeManager;

    private QueryResultCache cache;

    // private QueryResultFormattingHelper formatHelper;

    @PostConstruct
    public void init() {

        cache = new QueryResultCache(QUERY_RESULT_CACHE_SIZE); // 600 concurrent sessions as a
                                                               // target
    }

    @Override
    public boolean isSupportedOperation(final String opId) {
        return opManager.listOperations(OperationManagerImpl.LOOM_UUID).contains(opId);
    }

    @Override
    public QueryResult processQuery(final Session session, final ThreadDefinition threadDef)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException,
            InvalidQueryInputException, OperationException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload {
        return processQuery(session, threadDef, new ArrayList<ThreadDefinition>(0), false);
    }

    // used for tests only
    @Override
    public QueryResult processQuery(final Session session, final ThreadDefinition threadDef,
            final String derivedAgreggLogicalId) throws NoSuchSessionException, LogicalIdAlreadyExistsException,
            NoSuchAggregationException, InvalidQueryInputException, OperationException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        if (getQueryResult(session, DA + derivedAgreggLogicalId, threadDef, null)) {
            updateCachedRelationships(session); // need to update previously calculated and cached
                                                // data for consistency
        }
        QueryResult result = cache.getCachedResult(session, DA + derivedAgreggLogicalId);
        return result;
    }

    @Override
    public QueryResult processQuery(final Session session, final ThreadDefinition threadDef,
            final List<ThreadDefinition> threads, final boolean forceRelationsRecalculation)
            throws NoSuchSessionException, NoSuchAggregationException, InvalidQueryInputException, OperationException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Processing thread " + threadDef);

        }

        // check thread has not been deleted by adapter deletion
        if (threadDef.isDeleted()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(threadDef + " has been deleted by a dynamic removal of its associated adapter");
            }

            // clean cache
            cache.cleanCachedResult(session, DA + threadDef.getQuery().hashCode());

            throw new ThreadDeletedByDynAdapterUnload("Thread deleted by a dynamic removal of its associated adapter",
                    threadDef);
        }



        return updateThreads(session, threadDef, threads, forceRelationsRecalculation);
    }

    private QueryResult updateThreads(final Session session, final ThreadDefinition threadDef,
            final List<ThreadDefinition> threads, boolean forceRelationsRecalculation)
            throws NoSuchAggregationException, NoSuchSessionException, InvalidQueryInputException, OperationException,
            ItemPropertyNotFound, RelationPropertyNotFound {
        StopWatch watch = null;
        if (timing) {
            watch = new StopWatch();
            watch.start();
        }
        QueryResult result = null;
        /**
         * Need to proactively update related DAs so that the relationship calculator gets a right
         * result right away (as opposed to letting the client complete a full polling cycle)
         */
        if (threads != null && threads.size() > 0) {
            for (ThreadDefinition relatedThread : threads) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Thread " + DA + relatedThread.getQuery().hashCode() + "; forced: "
                            + forceRelationsRecalculation);
                }
                boolean needRecalculation = false;
                if (!relatedThread.equals(threadDef)) {
                    needRecalculation = updateOtherThreadInTapestry(session, relatedThread);
                } else {
                    needRecalculation = getQueryResult(session, threadDef, threads);
                }
                forceRelationsRecalculation = forceRelationsRecalculation || needRecalculation;
            }
        } else {
            forceRelationsRecalculation = forceRelationsRecalculation || getQueryResult(session, threadDef, null);
        }

        if (forceRelationsRecalculation) {
            updateCachedRelationships(session); // need to update previously calculated and ached
                                                // data for consistency
        }
        result = cache.getCachedResult(session, DA + threadDef.getQuery().hashCode());

        StopWatch aggStatsTiming = null;
        if (timing) {
            aggStatsTiming = new StopWatch();
            aggStatsTiming.start();
        }

        ItemType it = result.getItemType();
        for (QueryResultElement qre : result.getElements()) {
            if (qre.getEntity().isAggregation()) {
                Aggregation agg = (Aggregation) qre.getEntity();
                StatUtils.populateAggregateStats(agg, it, agg.getElements(), null);
            }
        }

        if (timing) {
            aggStatsTiming.stop();
            result.addTiming("aggStatsTime", aggStatsTiming.getTime());
            if (result != null) {
                watch.stop();
                result.setTotalTiming(watch.getTime());
            }
        }

        QueryResult oppositeResult = cache.getCachedResult(session, DA + threadDef.getQuery().hashCode() + "!");
        if (oppositeResult != null && oppositeResult.getElements().size() > 0) {
            for (QueryResultElement qre : oppositeResult.getElements()) {
                if (qre.getEntity().isAggregation()) {
                    Aggregation agg = (Aggregation) qre.getEntity();
                    it = oppositeResult.getItemType();
                    StatUtils.populateAggregateStats(agg, it, agg.getElements(), null);
                }
            }
            result.setExcludedItems(oppositeResult.getElements().get(0));
        }

        if (LOG.isDebugEnabled() && watch != null) {
            watch.stop();
            LOG.debug("updateThreads end time=" + watch);
        }
        return result;
    }

    /*
     * swallow exceptions on other threads in tapestry toa void confusing clients
     */
    private boolean updateOtherThreadInTapestry(final Session session, final ThreadDefinition relatedThread) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Processing other thread in tapestry " + relatedThread);
        }
        try {
            return getQueryResult(session, relatedThread, null);
        } catch (NoSuchSessionException e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        } catch (NoSuchAggregationException e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        } catch (OperationException e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        } catch (IllegalArgumentException e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        } catch (InvalidQueryInputException e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        } catch (ItemPropertyNotFound e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        } catch (NullPointerException e) {
            LOG.error("Error processing other thread in the tapestry: " + relatedThread
                    + ". Exception caught but not propagated. " + e);
        }
        return false;
    }

    private boolean getQueryResult(final Session session, final String derivedLogicalId,
            final ThreadDefinition threadDef, final List<ThreadDefinition> threads) throws NoSuchSessionException,
            NoSuchAggregationException, OperationException, InvalidQueryInputException, ItemPropertyNotFound {

        ItemType itemType = itemTypeManager.getItemType(threadDef.getItemType());
        List<String> inputIds = threadDef.getQuery().getInputs();
        validateInputs(inputIds);
        List<Operation> processPipe =
                detectAndFormatDefaultIdentityQueries(threadDef.getQuery().getOperationPipeline());
        Aggregation derived = aggregationManager.getAggregation(session, derivedLogicalId);

        String oppositeDerivedLogicalId = derivedLogicalId + "!";

        Aggregation oppositeDerived = aggregationManager.getAggregation(session, oppositeDerivedLogicalId);
        String threadId = threadDef.getId();

        boolean forceRelRecalc = false;
        /*****************************/
        // check cached
        /*****************************/
        if (derived != null && !derived.isDirty()) {
            if (cache.getCachedResult(session, derivedLogicalId) == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Reusing prior aggregation: no need to recalculate, just build QR " + derivedLogicalId);
                }
                // aggregation exists, use it to build and cache QR
                buildAndCacheQueryResult(session, 0, null, derived, itemType);
                if (oppositeDerived != null) {
                    buildAndCacheQueryResult(session, 0, null, oppositeDerived, itemType);
                }
                forceRelRecalc = true;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Reusing prior result: no need to recalculate " + derivedLogicalId);
                    // result = cache.getCachedResult(session, derivedLogicalId);
                }
            }
        } else {

            /*****************************/
            // re-compute if first time or not cached
            /*****************************/
            QueryResult previousQueryResult = cache.getCachedResult(session, derivedLogicalId);
            long lastQuery = 0;
            if (derived != null) {
                lastQuery = derived.getFibreCreated().getTime();
                if (derived.isDirty()) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Cleaning pre-existing dirty DA & children " + derivedLogicalId);
                    }
                    aggregationManager.deleteAggregationAndChildren(session, derivedLogicalId, false);
                    cache.cleanCachedResult(session, derivedLogicalId);
                }
            }

            StopWatch watch = null;
            if (timing) {
                watch = new StopWatch();
                watch.start();
            }

            Map<Integer, Aggregation> aggs = executePipeline(session, inputIds, processPipe, null, derivedLogicalId,
                    itemType, threads, threadId, threadDef.getQuery().isIncludeExcludedItems());
            long aggTime = 0;
            if (timing) {
                aggTime = watch.getTime();
                watch.reset();
                watch.start();
            }
            QueryResult result =
                    buildAndCacheQueryResult(session, lastQuery, previousQueryResult, aggs.get(0), itemType);
            long buildQueryTime = 0;
            if (timing) {
                watch.stop();
                buildQueryTime = watch.getTime();
            }

            forceRelRecalc = true;

            if (timing) {
                result.addTiming("aggTime", aggTime);
                result.addTiming("queryTime", buildQueryTime);
                buildQueryTime = 0;
                watch.reset();
                watch.start();
            }

            QueryResult oppositePreviousQueryResult = cache.getCachedResult(session, oppositeDerivedLogicalId);
            lastQuery = 0;
            if (oppositeDerived != null) {
                lastQuery = oppositeDerived.getFibreCreated().getTime();
                if (oppositeDerived.isDirty()) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Cleaning pre-existing dirty DA & children " + oppositeDerivedLogicalId);
                    }
                    aggregationManager.deleteAggregationAndChildren(session, oppositeDerivedLogicalId, false);
                    cache.cleanCachedResult(session, oppositeDerivedLogicalId);
                }
            }
            buildAndCacheQueryResult(session, lastQuery, oppositePreviousQueryResult, aggs.get(1), itemType);

            if (timing) {
                watch.stop();
                result.addTiming("oppositeQueryTime", buildQueryTime);
            }
        }
        if (derived != null) {
            derived.setDirty(false);
        }
        if (oppositeDerived != null) {
            oppositeDerived.setDirty(false);
        }
        return forceRelRecalc;
    }

    private boolean getQueryResult(final Session session, final ThreadDefinition threadDef,
            final List<ThreadDefinition> threads) throws NoSuchSessionException, NoSuchAggregationException,
            OperationException, InvalidQueryInputException, ItemPropertyNotFound {
        String derivedLogicalId = DA + threadDef.getQuery().hashCode();
        return getQueryResult(session, derivedLogicalId, threadDef, threads);
    }

    private void updateCachedRelationships(final Session thisSession)
            throws NoSuchSessionException, ItemPropertyNotFound, NoSuchAggregationException, RelationPropertyNotFound {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating relationships for cached threads in session: " + thisSession.getId());
        }
        Map<String, QueryResult> idQueryResultMap = cache.get(thisSession);
        idQueryResultMap = QueryResultFormattingHelper.populateWithRelationshipsMultiple(thisSession, idQueryResultMap,
                aggregationManager.listGroundedAggregations(thisSession), true, relationshipCalculator);
        if (idQueryResultMap != null) {
            for (String key : idQueryResultMap.keySet()) {
                QueryResult qr = idQueryResultMap.get(key);
                cache.cacheResult(thisSession, key, qr);
            }
        }
    }

    @Override
    public void clearUnused(final Session thisSession, final String logicalId)
            throws NoSuchSessionException, NoSuchAggregationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Clear unused DA " + logicalId);
        }
        aggregationManager.deleteAggregationAndChildren(thisSession, logicalId, false);
        cache.cleanCachedResult(thisSession, logicalId);
        // updateCachedRelationships(thisSession);
    }

    @Override
    public void clear(final Session thisSession) {
        cache.clear(thisSession);
        // updateCachedRelationships(thisSession);
    }

    // TODO: this step by step processing is not efficient (some ops can be done
    // at the same time in a single pass of
    // the collection)
    private Map<Integer, Aggregation> executePipeline(final Session session, final List<String> inputIds,
            final List<Operation> pipe, Aggregation derived, final String derivedLogicalId, final ItemType itemType,
            final List<ThreadDefinition> threads, final String threadId, final boolean includeExcludedItems)
            throws NoSuchSessionException, NoSuchAggregationException, OperationException, InvalidQueryInputException,
            ItemPropertyNotFound {

        if (LOG.isTraceEnabled()) {
            LOG.trace("Processing pipeline with " + pipe.size() + " operations");
        }

        // TODO: this semantic Id supports just single inputs in the pipelink => not valid for
        // join operations (LOOM-1441 in jira)
        PipeLink<Fibre> inputs = formatInputsForPipeProcessing(session, inputIds);

        /*****************************/
        // process
        /*****************************/
        // How to validate operations spanning several collections?
        // create exec context for operation and execute it
        OperationContext context = new OperationContextImpl(adapterManager, aggregationManager, relationshipCalculator,
                stitcher, session, itemType);
        QueryStepExecutor exec = new QueryStepExecutor(session, ImmutableList.copyOf(pipe), inputs, inputIds, opManager,
                context, threadId, derivedLogicalId, (this), includeExcludedItems);
        QueryExecutorResult[] execResults = exec.exec();

        Map<Integer, Aggregation> results = new HashMap<>();
        int i = 0;
        for (QueryExecutorResult queryExecutorResult : execResults) {

            if (queryExecutorResult.crossThread && threads != null) {
                for (ThreadDefinition otherThread : threads) {
                    updateOtherThreadInTapestry(session, otherThread);
                }
            }

            /*****************************/
            // postprocess
            /*****************************/
            derived = aggregationManager.getAggregation(session, queryExecutorResult.derivedLogicalId);
            derived = executeCreationOrUpdateOfDA(session, derived, queryExecutorResult.derivedLogicalId,
                    queryExecutorResult.preparedOpResult, inputIds, itemType);
            QueryResultFormattingHelper.setAttributes(queryExecutorResult.needToCreateIntermediateDAs, false, derived,
                    0, 0, derived.getTags());
            QueryResultFormattingHelper.calculateAggregateAlerts(derived);

            results.put(i, derived);
            i++;
        }
        derived = results.get(0);
        return results;
    }



    private QueryResult buildAndCacheQueryResult(final Session session, final long previousDaTime,
            final QueryResult previousQueryResult, final Aggregation derived, final ItemType itemType)
            throws NoSuchSessionException {
        QueryResult result = QueryResultFormattingHelper.convertToQueryResult(previousDaTime, previousQueryResult,
                derived, itemType);


        // ObjectMapper mapper = new ObjectMapper();
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        // try {
        // LOG.info(mapper.writeValueAsString(result));
        // } catch (JsonProcessingException e) {
        // e.printStackTrace();
        // }

        if (derived != null) {
            cache.cacheResult(session, derived.getLogicalId(), result);
        }
        return result;
    }

    @Override
    public List<Fibre> createIntermediateDAs(final Session session, final List<Operation> opsDone,
            final StringBuilder semanticId, final String lastOperator, final String derivedLogicalId,
            final List<String> inputAggregationLogicalIds, final PipeLink<Fibre> fullOut, final boolean aggregated,
            final ItemType itemType) throws NoSuchSessionException, NoSuchAggregationException, ItemPropertyNotFound {

        Operation currentOp = opsDone.get(opsDone.size() - 1);
        boolean isLast = currentOp.getOperator().equalsIgnoreCase(lastOperator);
        List<Fibre> container = new ArrayList<>(fullOut.keySet().size());
        String key;
        String groupedLogicalId;
        int resultSize = fullOut.size();

        // TODO: Need a different naming mechanism to make it independent of the operation name (see
        // LOOM-1440 in jira)
        boolean isBraid =
                currentOp.getOperator().equalsIgnoreCase(DefaultOperations.BRAID.toString()) && resultSize > 1;
        boolean isGroup = currentOp.getOperator().equalsIgnoreCase(DefaultOperations.GROUP_BY.toString());

        // Assuming that we're creating a DA, we set the current operation as last operation that
        // has created a DA.
        String opTag = currentOp.getOperator();

        int i = 0;
        int aggIndex = 0;
        Map<String, Integer> usedGroupedIds = new HashMap<String, Integer>(USER_GROUP_DEFAULT);
        for (Object obj : fullOut.keySet()) {
            key = String.valueOf(obj);
            // different naming schemas if is last or not
            groupedLogicalId = QueryResultFormattingHelper.getNameForDa(derivedLogicalId, isLast, isBraid, opsDone, key,
                    i++, usedGroupedIds);

            Aggregation contained = executeCreationOrUpdateOfDA(session, null, groupedLogicalId, fullOut.get(obj),
                    inputAggregationLogicalIds, itemType);

            QueryResultFormattingHelper.setAttributes(isBraid, isGroup, contained, fullOut.get(obj).size(), aggIndex,
                    opTag); // tag for intermediate DA, therefore is not the
            // last DA to be created: false as a param

            contained.setSemanticPrefix(semanticId.toString());

            QueryResultFormattingHelper.calculateAggregateAlerts(contained);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Contained " + contained.getLogicalId() + " tag is: " + contained.getTags() + " aggregated? "
                        + aggregated);
            }
            container.add(contained);
            aggIndex += fullOut.get(obj).size();
        }
        return container;
    }

    private Aggregation executeCreationOrUpdateOfDA(final Session session, Aggregation derived,
            final String derivedLogicalId, final List<Fibre> newOrUpdatedDA, final List<String> inputIds,
            final ItemType itemType) throws NoSuchAggregationException, NoSuchSessionException, ItemPropertyNotFound {
        String[] inputArray = new String[inputIds.size()];
        for (int i = 0; i < inputIds.size(); i++) {
            inputArray[i] = inputIds.get(i);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Executing actual creation of DA " + derivedLogicalId + " for " + inputArray[i] + " with "
                        + newOrUpdatedDA.size());
            }
        }

        if (derived == null) {
            // check if it exists on agg manager
            derived = aggregationManager.getAggregation(session, derivedLogicalId);
            if (derived == null) {
                try {
                    derived = aggregationManager.createDerivedAggregation(session, itemType.getId(), derivedLogicalId,
                            QueryResultFormattingHelper.getLoomEntityType(newOrUpdatedDA), derivedLogicalId,
                            derivedLogicalId, inputArray, newOrUpdatedDA.size());
                } catch (LogicalIdAlreadyExistsException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Wrong logical ID provided: " + derivedLogicalId);
                    }
                    throw new IllegalArgumentException("Wrong logical ID provided: " + derivedLogicalId);
                }
            }
        }
        aggregationManager.updateDerivedAggregation(session, derived, newOrUpdatedDA);
        StatUtils.populateAggregateStats(derived, itemType, newOrUpdatedDA, null);
        derived.setDirty(false);
        return derived;
    }

    private PipeLink<Fibre> formatInputsForPipeProcessing(final Session session, final List<String> inputIds)
            throws NoSuchSessionException, NoSuchAggregationException {

        PipeLink<Fibre> inputs = new PipeLink<Fibre>(inputIds.size());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Pre processing inputs " + inputIds.size());
        }
        Aggregation inputAggregation = null;
        boolean atLeastOneInputContainElements = false;

        for (String inputLogicalId : inputIds) {
            inputAggregation = aggregationManager.getAggregation(session, inputLogicalId);

            if (inputAggregation == null) {
                LOG.error("Non-existent input logical ID " + inputLogicalId);
                throw new NoSuchAggregationException(inputLogicalId);
            }
            if (inputAggregation.getElements().size() > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding input to: " + inputLogicalId + " with " + inputAggregation.getElements().size()
                            + " elements. Are they items? "
                            + inputAggregation.getElements().stream().findFirst().get().isItem());
                }
                inputs.put(inputLogicalId, inputAggregation.getElements());
                atLeastOneInputContainElements = true;
            }
        }

        if (atLeastOneInputContainElements) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Pre-processing input -> results " + inputs.size());
            }
        } else {
            if (inputAggregation.getFibreUpdated() != null) {
                inputs = new PipeLink<Fibre>(0);
                // in.setPending(false);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Data not populated yet for " + inputAggregation.getMergedLogicalId());
                }
            }
        }
        return inputs;
    }

    private void validateInputs(final List<String> inputIds) throws InvalidQueryInputException {
        if (inputIds == null || inputIds.size() == 0) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Invalid Query Input: The query requires at least one input logical ID to be processed.");
            }
            throw new InvalidQueryInputException(
                    "Invalid Query Input: The query requires at least one input logical ID to be processed.");
        }
        for (String inputId : inputIds) {
            validateSingleInput(inputId);
        }
    }

    private void validateSingleInput(final String inputId) throws InvalidQueryInputException {
        if (inputId == null || inputId.isEmpty()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Invalid Query Input: one of the inputs to the query was empty.");
            }
            throw new InvalidQueryInputException("Invalid Query Input: one of the inputs to the query was empty.");
        }
    }

    private List<Operation> detectAndFormatDefaultIdentityQueries(final List<Operation> pipe) {
        List<Operation> newPipe;
        if (pipe == null || pipe.size() == 0) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Submitted a default identity query");
            }
            newPipe = new ArrayList(1);
            newPipe.add(new Operation());
        } else {
            newPipe = new ArrayList(pipe);
        }
        return newPipe;
    }
}
