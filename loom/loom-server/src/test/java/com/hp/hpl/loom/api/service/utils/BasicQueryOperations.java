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
package com.hp.hpl.loom.api.service.utils;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.service.IntegrationTestBase;
import com.hp.hpl.loom.exceptions.QueryResultPendingForTooLongException;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public interface BasicQueryOperations {
    static final Log log = LogFactory.getLog(BasicQueryOperations.class);


    // //////////////////////////////////////////////////////////////////////////////////////////////
    // Basic Query Operations
    // //////////////////////////////////////////////////////////////////////////////////////////////

    static QueryResult clientGetAggregation(final LoomClient client, final String tapestryId, final String threadId) {
        StopWatch watch = new StopWatch();
        if (log.isDebugEnabled()) {
            log.debug("TN: Get " + tapestryId + " " + threadId);
        }
        watch.start();
        QueryResult qr = client.getAggregation(tapestryId, threadId);
        watch.stop();
        if (log.isDebugEnabled()) {
            log.debug("Get " + tapestryId + " " + threadId + " get aggregation time=" + watch);
        }
        return qr;
    }


    static QueryResult waitForAdaptersToPopulateGAs(final LoomClient client, final String tapestryId,
            final String threadId, final Predicate pred)
            throws InterruptedException, QueryResultPendingForTooLongException {
        QueryResult qr = null;
        for (int j = 0; j < IntegrationTestBase.MAX_ATTEMPTS; ++j) {
            qr = clientGetAggregation(client, tapestryId, threadId);
            ArrayList<QueryResultElement> qrElems = qr.getElements();
            if (pred.test(qrElems)) {
                return qr;
            }
            if (j < IntegrationTestBase.MAX_ATTEMPTS) {
                Thread.sleep(IntegrationTestBase.WAIT_TIME);
            }
        }

        if (qr.getStatus().equals(QueryResult.Statuses.PENDING)) {
            throw new QueryResultPendingForTooLongException(
                    "Status Pending for tapestry " + tapestryId + " thread " + threadId, qr);
        }
        throw new QueryResultPendingForTooLongException(
                "Predicate not met for tapestry " + tapestryId + " thread " + threadId, qr);
    }

    static QueryResult getThreadWithWait(final LoomClient client, final String tapestryId, final String threadId,
            final Predicate pred) {
        try {

            QueryResult qr1 = waitForAdaptersToPopulateGAs(client, tapestryId, threadId, pred);
            return qr1;
        } catch (QueryResultPendingForTooLongException e) {
            log.error("Failed to fetch expected amount of data from adapter. Expected !=0 and got: "
                    + e.getQueryResult().getElements().size());
            fail("Failed to fetch expected amount of data. Expected got: " + e.getQueryResult().getElements().size()
                    + " " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("Interrupted exception ", e);
            fail("Interrupted exception " + e);
        }
        return null;
    }


    static QueryResultElement getItem(final LoomClient client, final String logicalId) {

        return client.getItem(logicalId);

    }

    /*
     * Get all of the threads, waiting until they all have results
     */
    static List<QueryResult> queryAllThreads(final LoomClient client, final TapestryDefinition tapestryDefinition) {
        List<QueryResult> queryResults = new ArrayList<QueryResult>(tapestryDefinition.getThreads().size());
        StopWatch watch = new StopWatch();
        watch.start();
        List<ThreadDefinition> threads = tapestryDefinition.getThreads();
        for (ThreadDefinition thread : threads) {
            String threadId = thread.getId();
            QueryResult qr =
                    getThreadWithWait(client, tapestryDefinition.getId(), threadId, IntegrationTestBase.greaterThan0);
            assertFalse("Result of a query did not contain elements for " + threadId, qr.getElements().isEmpty());
            queryResults.add(qr);
        }
        watch.stop();
        log.info("Getting all threads time=" + watch);
        assertFalse("Loom stuck in performance black hole", watch.getTime() > (60 * 1000));
        RelationshipsHandling.checkCrossThreadRelationships(queryResults);
        return queryResults;
    }

}
