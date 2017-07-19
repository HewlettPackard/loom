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
package com.hp.hpl.loom.manager.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.manager.query.executor.QueryExecutor;
import com.hp.hpl.loom.manager.query.executor.QueryExecutorImpl;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

@Component
public class QueryManagerImpl implements QueryManager {
    private static final Log LOG = LogFactory.getLog(QueryManagerImpl.class);

    @Autowired
    private TapestryManager tapestryManager;

    @Autowired
    private QueryExecutor queryExecutor;


    @Override
    public synchronized QueryResult getThread(final Session session, final String threadId,
            final boolean forceRelationsRecalculation)
            throws NoSuchSessionException, NoSuchThreadDefinitionException, NoSuchTapestryDefinitionException,
            NoSuchQueryDefinitionException, NoSuchAggregationException, InvalidQueryInputException, OperationException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {

        validateSession(session);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting thread for session " + session + " and thread " + threadId);
        }

        TapestryDefinition tapestry = tapestryManager.getTapestryDefinition(session);
        validateTapestry(tapestry, session);
        List<ThreadDefinition> threads = tapestry.getThreads();

        ThreadDefinition threadDef = tapestry.getThreadDefinition(threadId);
        validateThread(threadDef, threadId, tapestry);

        QueryDefinition queryDef = threadDef.getQuery();
        validateQuery(queryDef, threadId, tapestry);

        // ItemType itemType = tapestryManager.getItemType(threadDef.getItemType());
        // TODO need to create dynamic itemtypes for aggregations before item type validation can be
        // enabled
        // validateQueryAgainstItemType(queryDef, itemType);


        return queryExecutor.processQuery(session, threadDef, threads, forceRelationsRecalculation);

    }

    // private <T> boolean containsIgnoreCase(final Set<T> strings, final String property) {
    //
    // if (LOG.isTraceEnabled()) {
    // LOG.trace("Set " + strings + " contains? " + property);
    // }
    //
    // boolean contains = false;
    // if (property == null || property.isEmpty() || strings == null) {
    // return contains;
    // }
    // LOG.info("Contains pre " + contains);
    // for (T valid : strings) {
    // if (valid.toString().equalsIgnoreCase(property)) {
    // return true;
    // }
    // }
    //
    // LOG.info("Contains mid " + contains);
    // return contains;
    // }

    private void validateQuery(final QueryDefinition queryDef, final String threadId, final TapestryDefinition tapestry)
            throws NoSuchQueryDefinitionException {
        if (queryDef == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Thread " + threadId + " cannot be found in tapestry: " + tapestry.getId());
            }
            throw new NoSuchQueryDefinitionException(threadId);
        }
    }

    private void validateThread(final ThreadDefinition threadDef, final String threadId,
            final TapestryDefinition tapDef) throws NoSuchThreadDefinitionException {
        if (threadDef == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Thread " + threadId + " cannot be found in tapestry: " + tapDef.getId());
            }
            throw new NoSuchThreadDefinitionException(threadId);
        }

    }

    private void validateTapestry(final TapestryDefinition tapestry, final Session session)
            throws NoSuchTapestryDefinitionException {
        if (tapestry == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Tapestry for session " + session.getId() + " cannot be found");
            }
            throw new NoSuchTapestryDefinitionException(session.getId());
        }
    }


    private void validateSession(final Session session) {
        if (session == null || session.getId().isEmpty()) {
            throw new IllegalArgumentException("Session id not specified");
        }
    }

    @Override
    public void tapestryDefinitionChanged(final Session session, final TapestryDefinition oldTapestry,
            final TapestryDefinition newTapestry) throws NoSuchQueryDefinitionException, OperationException,
            NoSuchThreadDefinitionException, NoSuchAggregationException, NoSuchSessionException,
            NoSuchTapestryDefinitionException, InvalidQueryInputException, ItemPropertyNotFound,
            RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {
        StopWatch watch = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("tapestryDefinitionChanged start");
            watch = new StopWatch();
            watch.start();
        }
        // delete DAs associated to a thread, if needed
        deleteGarbageDAs(session, oldTapestry, newTapestry);
        if (LOG.isDebugEnabled()) {
            watch.stop();
            LOG.debug("deleted GarbageDAs time=" + watch);
            watch.reset();
            watch.start();
        }
        preCreateQueryResponses(session, newTapestry);
        if (LOG.isDebugEnabled()) {
            watch.stop();
            LOG.debug("preCreateQueryResponses time=" + watch);
        }
    }


    private void deleteGarbageDAs(final Session session, final TapestryDefinition oldTap,
            final TapestryDefinition newTap)
            throws NoSuchSessionException, NoSuchAggregationException, ItemPropertyNotFound {

        if (oldTap != null) {
            Set<ThreadDefinition> oldThreads = new HashSet<>(oldTap.getThreads());
            Set<ThreadDefinition> newThreads = new HashSet<>(newTap.getThreads());

            // detect deleted threads
            oldThreads.removeAll(newThreads);

            if (LOG.isDebugEnabled()) {
                LOG.debug(session.getId() + ", tap# " + oldTap.getId() + ": " + oldThreads.size()
                        + " threads are to be deleted");
            }

            // clean recursively on agg manager
            for (ThreadDefinition thread : oldThreads) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(session.getId() + ", tap# " + oldTap.getId() + ": deleting " + "/da/"
                            + thread.getQuery().hashCode());
                }

                clearUnused(session, QueryExecutorImpl.DA + thread.getQuery().hashCode());
                if (thread.getQuery().isIncludeExcludedItems()) {
                    clearUnused(session, QueryExecutorImpl.DA + thread.getQuery().hashCode() + "!");
                }
            }
        }
    }

    @Override
    public void preCreateQueryResponses(final Session session, final TapestryDefinition newTap)
            throws NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            NoSuchAggregationException, NoSuchSessionException, NoSuchTapestryDefinitionException,
            InvalidQueryInputException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload {
        if (newTap.getThreads().size() > 0) {
            ThreadDefinition threadDefinition = newTap.getThreads().get(0);

            if (LOG.isDebugEnabled()) {
                LOG.debug(session.getId() + ", tap# " + newTap.getId() + ": creating DA");
            }

            preCalculateResponses(session, threadDefinition.getId());
        }

    }

    private void clearUnused(final Session session, final String logicalId)
            throws ItemPropertyNotFound, NoSuchSessionException, NoSuchAggregationException {
        queryExecutor.clearUnused(session, logicalId);
    }

    @Override
    public void clear(final Session session) {
        queryExecutor.clear(session);
    }

    private void preCalculateResponses(final Session session, final String threadId) throws ItemPropertyNotFound,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            NoSuchAggregationException, NoSuchSessionException, NoSuchTapestryDefinitionException,
            InvalidQueryInputException, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Precalculate responses to query ");
        }

        getThread(session, threadId, true);
    }
}
