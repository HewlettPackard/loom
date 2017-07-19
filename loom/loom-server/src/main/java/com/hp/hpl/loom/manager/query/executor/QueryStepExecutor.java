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
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.manager.query.IntermediateAggregationHandler;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.OperationManager;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.Operation;

public class QueryStepExecutor {

    private static final Log LOG = LogFactory.getLog(QueryStepExecutor.class);

    public final ImmutableList<Operation> steps;
    public final PipeLink<Fibre> inputs;
    private final OperationManager opManager;
    private final OperationContext context;
    private final StringBuilder opsDoneResultingInDa;
    private final Session session;
    private final String derivedLogicalId;
    private final IntermediateAggregationHandler queryExecutorImpl;
    private ImmutableList<String> inputIds;

    private boolean includeExcludedItems;

    public QueryStepExecutor(final Session session, final ImmutableList<Operation> steps, final PipeLink<Fibre> inputs,
            final List<String> inputIds, final OperationManager opManager, final OperationContext context,
            final String threadId, final String derivedLogicalId,
            final IntermediateAggregationHandler queryExecutorImpl, boolean includeExcludedItems) {
        this.steps = steps;
        this.inputs = inputs;
        this.opManager = opManager;
        this.context = context;
        opsDoneResultingInDa = new StringBuilder(threadId);
        this.session = session;
        this.derivedLogicalId = derivedLogicalId;
        this.queryExecutorImpl = queryExecutorImpl;
        this.inputIds = ImmutableList.copyOf(inputIds);
        this.includeExcludedItems = includeExcludedItems;
    }

    public QueryExecutorResult[] exec() throws NoSuchSessionException, OperationException, InvalidQueryInputException,
            NoSuchAggregationException, ItemPropertyNotFound {
        QueryExecutorResult[] execs = null;
        String lastOperator = steps.get(steps.size() - 1).getOperator();
        boolean crossThread = false;
        boolean needToCreateIntermediateDAs = false;
        PipeLink<Fibre> rawOperationResult = null;
        List<Fibre> preparedOpResult = new ArrayList<>(0);
        List<Operation> opsDone = new ArrayList<>();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Processing ops " + steps + " with inputs: " + inputs.size());
        }

        Map<OperationErrorCode, String> errors = new HashMap<>();

        String oppositeDerivedLogicalId = derivedLogicalId + "!";

        if (inputs.size() == 0) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Empty inputs; stop processing pipeline");
            }
            // (String derivedLogicalId, List<Fibre> preparedOpResult, boolean crossThread, boolean
            // needToCreateIntermediateDAs) {
            if (includeExcludedItems) {
                execs = new QueryExecutorResult[2];
                execs[0] = new QueryExecutorResult(derivedLogicalId, preparedOpResult, false, false);
                execs[1] = new QueryExecutorResult(oppositeDerivedLogicalId, new ArrayList<Fibre>(), false, false);
            } else {
                execs = new QueryExecutorResult[1];
                execs[0] = new QueryExecutorResult(derivedLogicalId, preparedOpResult, false, false);
            }
            return execs;
        }

        HashMap<String, Fibre> remainingItems = new HashMap<>();
        if (includeExcludedItems) {// && !excludeItemsQuery) {
            List<Fibre> all = LoomQueryUtils.getFirstInput(inputs, errors);
            for (Fibre fibre : all) {
                remainingItems.put(fibre.getLogicalId(), fibre);
            }

        }

        for (Operation op : steps) {
            QueryOperation qOp = opManager.getOperation(op.getOperator()).getQueryOperation();

            // may affect other threads in the tapestry
            if (qOp != null && !crossThread && qOp.isInterItemType()) {
                crossThread = true;
            }
            // if there aren't any input then skip the operation
            // if (inputs.size() != 0) {
            rawOperationResult = executeOperation(op, inputs, context);
            // }

            // prepare for next iteration
            opsDone.add(op);

            needToCreateIntermediateDAs = rawOperationResult.size() > 1 || rawOperationResult.size() == 1
                    && evaluatePredicateList(qOp.getOneLengthOutputPredicate(), rawOperationResult);

            if (needToCreateIntermediateDAs) {
                opsDoneResultingInDa.append("/");
                opsDoneResultingInDa.append(op.hashCode());
            }

            preparedOpResult = prepareDataForNextIteration(session, rawOperationResult, lastOperator, opsDone,
                    opsDoneResultingInDa, needToCreateIntermediateDAs, derivedLogicalId, inputIds, context.getType());

            if (includeExcludedItems) {
                for (Object key : rawOperationResult.keySet()) {
                    List<Fibre> items = rawOperationResult.get(key);
                    for (Fibre fibre : items) {
                        remainingItems.remove(fibre.getLogicalId());
                    }
                }
            }

            if (preparedOpResult.size() == 0) {
                break;
            } else {
                inputs.clear();
                inputs.put("0", preparedOpResult);
            }
        }
        if (includeExcludedItems) {
            execs = new QueryExecutorResult[2];
            execs[0] = new QueryExecutorResult(derivedLogicalId, preparedOpResult, crossThread,
                    needToCreateIntermediateDAs);

            // generate the DA for the opposite items
            PipeLink<Fibre> pipeLink = new PipeLink<>(0);
            Optional<Object> inputElement = rawOperationResult.keySet().stream().findFirst();
            Object key = null;
            if (!inputElement.isPresent()) {
                key = "!";
            } else {
                key = inputElement.get();
            }
            List<Fibre> rItems = new ArrayList<>(remainingItems.values());
            pipeLink.put(key, rItems);
            rItems = queryExecutorImpl.createIntermediateDAs(session, steps, opsDoneResultingInDa, lastOperator,
                    oppositeDerivedLogicalId, inputIds, pipeLink, true, context.getType());


            execs[1] =
                    new QueryExecutorResult(oppositeDerivedLogicalId, rItems, crossThread, needToCreateIntermediateDAs);
        } else {
            execs = new QueryExecutorResult[1];
            execs[0] = new QueryExecutorResult(derivedLogicalId, preparedOpResult, crossThread,
                    needToCreateIntermediateDAs);
        }
        return execs;
    }

    private PipeLink<Fibre> executeOperation(final Operation op, final PipeLink<Fibre> inputs,
            final OperationContext context)
            throws NoSuchSessionException, OperationException, InvalidQueryInputException {

        if (inputs == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Null input into " + op + "while processing " + op);
            }
            throw new InvalidQueryInputException("Null input into " + op);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Processing operation " + op + " with inputs " + inputs.size());
        }

        @SuppressWarnings("checkstyle:linelength")
        QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> function =
                opManager.getFunction(op.getOperator());

        if (function == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unsupported Operation " + op);
            }
            throw new UnsupportedOperationException(op.toString());
        }

        PipeLink<Fibre> executedResult = null;
        Map<OperationErrorCode, String> errors = new HashMap<>();

        try {
            executedResult = function.apply(inputs, op.getParameters(), errors, context);
        } catch (RuntimeException ex) {
            LOG.error("Runtime exception in operation " + op, ex);
        }
        checkErrorCodes(errors, op);

        if (executedResult == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Problem while executing operation " + op);
            }
            throw new OperationException("Operation did not process correctly. Null result", op);
        }
        return executedResult;
    }

    private void checkErrorCodes(final Map<OperationErrorCode, String> errors, final Operation op)
            throws OperationException {
        if (errors.size() != 0) {
            StringBuilder errorStrings = new StringBuilder();
            String errorString = null;
            int errorCount = 0;
            for (OperationErrorCode code : errors.keySet()) {
                errorString = "Event in execution of " + op.getOperator() + ". Error code " + code + "; stack trace: "
                        + errors.get(code);

                errorStrings.append(errorString);
                if (errorString.contains("ERROR")) {
                    LOG.error(errorString);
                    errorCount++;
                } else {
                    LOG.warn(errorString);
                }
            }
            if (errorCount > 0) {
                throw new OperationException(errorStrings.toString(), op);
            }
        }
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private List<Fibre> prepareDataForNextIteration(final Session session, final PipeLink<Fibre> operationResult,
            final String lastOperatorInPipeline, final List<Operation> previousOperations,
            final StringBuilder semanticId, final Boolean needToCreateIntermediateDAs, final String derivedLogicalId,
            final List<String> inputIds, final ItemType itemType)
            throws NoSuchAggregationException, NoSuchSessionException, ItemPropertyNotFound {
        List<Fibre> singleOpOutput;
        if (needToCreateIntermediateDAs) {
            singleOpOutput = queryExecutorImpl.createIntermediateDAs(session, previousOperations, semanticId,
                    lastOperatorInPipeline, derivedLogicalId, inputIds, operationResult, needToCreateIntermediateDAs,
                    itemType);
        } else {
            if (operationResult.values().size() != 0) {
                singleOpOutput = operationResult.values().iterator().next();
            } else {
                singleOpOutput = new ArrayList<>(0);
            }
        }
        return singleOpOutput;
    }

    private boolean evaluatePredicateList(final List<Predicate<PipeLink<Fibre>>> predList,
            final PipeLink<Fibre> pipeLink) {
        boolean result = true;
        for (Predicate<PipeLink<Fibre>> pred : predList) {
            result = result && pred.test(pipeLink);
            if (!result) { // encourage early termination for large concatenations of predicates
                return result;
            }
        }
        return result;
    }
}
