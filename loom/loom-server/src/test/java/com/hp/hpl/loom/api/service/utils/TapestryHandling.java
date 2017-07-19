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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public interface TapestryHandling {
    static final Log log = LogFactory.getLog(TapestryHandling.class);

    static PatternDefinitionList clientGetPatterns(final LoomClient client) {
        StopWatch watch = new StopWatch();
        watch.start();
        PatternDefinitionList patternDefinitionList = client.getPatterns();
        watch.stop();
        if (log.isDebugEnabled()) {
            log.debug("Get patterns time=" + watch);
        }
        return patternDefinitionList;
    }

    static boolean predictableOrdering = false;

    static void convertToPredictableOrdering(final TapestryDefinition tapestryDefinition) {
        for (ThreadDefinition thread : tapestryDefinition.getThreads()) {
            QueryDefinition query = thread.getQuery();
            if (!query.getInputs().get(0).contains("instance")) {
                continue;
            }
            boolean containsPredictiveSort = false;
            List<Operation> pipeline = query.getOperationPipeline();
            if (!pipeline.isEmpty()) {
                Operation op = pipeline.get(0);
                String sortOperator = DefaultOperations.SORT_BY.toString();
                if (sortOperator.equals(op.getOperator())) {
                    if (op.getParameters().containsKey(QueryOperation.PROPERTY)
                            && op.getParameters().get(QueryOperation.PROPERTY).equals(Fibre.ATTR_LOGICAL_ID)) {
                        containsPredictiveSort = true;
                    }
                }
            }
            if (!containsPredictiveSort) {
                Map<String, Object> sortParams = new HashMap<>(4);
                sortParams.put(QueryOperation.PROPERTY, Fibre.ATTR_LOGICAL_ID);
                sortParams.put(QueryOperation.ORDER, QueryOperation.ASC_ORDER);
                Operation sort = new Operation((DefaultOperations.SORT_BY.toString()), sortParams);
                pipeline.add(0, sort);
                query.setOperationPipeline(pipeline);
            }
        }
    }


    static String clientCreateTapestryDefinition(final LoomClient client, final TapestryDefinition tapestryDefinition) {
        if (predictableOrdering) {
            convertToPredictableOrdering(tapestryDefinition);
        }
        StopWatch watch = new StopWatch();
        watch.start();
        // String id = client.createTapestryDefinition(tapestryDefinition);
        String id = client.createTapestryDefinition(tapestryDefinition).getId();
        watch.stop();
        if (log.isDebugEnabled()) {
            log.debug("Create tapestry time=" + watch + " nthreads=" + tapestryDefinition.getThreads().size() + " id "
                    + id);
        }

        return id;
    }


    static String createTapestryDefinition(final LoomClient client) {
        PatternDefinitionList patternDefinitionList = clientGetPatterns(client);
        List<PatternDefinition> patternDefinitions = patternDefinitionList.getPatterns();
        assertTrue(!patternDefinitions.isEmpty());
        PatternDefinition patternDefinition = patternDefinitions.get(0);
        List<ThreadDefinition> threads = patternDefinition.getThreads();
        TapestryDefinition tapestryDefinition = new TapestryDefinition();
        if (threads.size() > 0) {
            tapestryDefinition.setThreads(new ArrayList<ThreadDefinition>(threads));
        }
        String tapestryId = clientCreateTapestryDefinition(client, tapestryDefinition);
        return tapestryId;
    }

    static TapestryDefinition createFullTapestryFromPatternsWithIds(final LoomClient client, final int maxFibres,
            final Set<String> requiredPatternIds) {
        PatternDefinitionList patternDefinitionList = clientGetPatterns(client);
        List<PatternDefinition> patternDefinitions = patternDefinitionList.getPatterns();
        Set<String> foundPatternIds = new HashSet<>(requiredPatternIds);
        List<ThreadDefinition> threads = new ArrayList<>();
        for (String patternId : requiredPatternIds) {
            String foundPatternId = null;
            for (PatternDefinition pd : patternDefinitions) {
                if (pd.getId().endsWith(patternId)) {
                    foundPatternId = patternId;
                    log.info("Found the pattern - " + patternId);
                    assertTrue("Pattern " + patternId + " did not contain any threads", pd.getThreads().size() > 0);
                    threads.addAll(pd.getThreads());
                    break;
                }
            }
            assertNotNull("Could not find a pattern in list " + patternId, foundPatternId);
        }
        // Construct braided queries for all threads
        List<ThreadDefinition> myThreads = new ArrayList<ThreadDefinition>(threads.size());
        for (ThreadDefinition thread : threads) {
            QueryDefinition query =
                    maxFibres > 0 ? QueryFormatting.createBraidQuery(thread.getQuery().getInputs(), maxFibres)
                            : QueryFormatting.createSimpleQuery(thread.getQuery().getInputs());
            ThreadDefinition myThread = new ThreadDefinition(thread.getItemType(), thread.getItemType(), query);
            myThreads.add(myThread);
        }
        // Create a tapestry on client from this combined pattern
        TapestryDefinition tapestryDefinition = createTapestryFromThreadDefinitions(client, myThreads);
        return tapestryDefinition;
    }

    static TapestryDefinition createFullTapestryFromPatterns(final LoomClient client, final int maxFibres) {
        Set<String> patternIds =
                new HashSet<String>(Arrays.asList(BaseOsAdapter.ALL_FIVE_PATTERN, BaseOsAdapter.REG_PROJ_PATTERN));
        return createFullTapestryFromPatternsWithIds(client, maxFibres, patternIds);
    }

    static PatternDefinition getPatternDefinitionMatchingId(final LoomClient client, final String targetPattern) {
        PatternDefinitionList patternDefinitionList = clientGetPatterns(client);
        List<PatternDefinition> patternDefinitions = patternDefinitionList.getPatterns();
        for (PatternDefinition pd : patternDefinitions) {
            if (pd.getId().endsWith(targetPattern)) {
                log.info("Found the pattern matching " + targetPattern);
                assertNotNull("Threads should not be null", pd.getThreads());
                return pd;
            }
        }
        return null;
    }

    static TapestryDefinition createTapestryDefinitionFromPatternDefinition(final PatternDefinition pd) {
        return createTapestryDefinitionFromThreadDefinitions(pd.getThreads());
    }

    static TapestryDefinition createTapestryDefinitionFromThreadDefinition(final ThreadDefinition thread) {
        List<ThreadDefinition> threads = new ArrayList<ThreadDefinition>(1);
        threads.add(thread);
        return createTapestryDefinitionFromThreadDefinitions(threads);
    }

    /**
     * Simply create a TapestryDefinition data structure from specified thread definitions.
     *
     * @param threads
     * @return
     */
    static TapestryDefinition createTapestryDefinitionFromThreadDefinitions(final List<ThreadDefinition> threads) {
        TapestryDefinition tapestryDefinition = new TapestryDefinition();
        tapestryDefinition.setThreads(new ArrayList<ThreadDefinition>(threads));
        return tapestryDefinition;
    }

    /**
     * Create a new tapestry on client from a pattern that matches specified id.
     *
     * @param targetPattern
     * @return
     */
    static TapestryDefinition createTapestryFromPatternId(final LoomClient client, final String targetPattern) {
        PatternDefinition pd = getPatternDefinitionMatchingId(client, targetPattern);
        assertNotNull("Could not find pattern " + targetPattern, pd);
        return createTapestryFromPattern(client, pd);
    }

    /**
     * Create a new tapestry on client from a pattern.
     *
     * @param pattern
     * @return
     */
    static TapestryDefinition createTapestryFromPattern(final LoomClient client, final PatternDefinition pattern) {
        TapestryDefinition tapestryDefinition = createTapestryDefinitionFromPatternDefinition(pattern);
        return createTapestryFromTapestryDefinition(client, tapestryDefinition);
    }

    /**
     * Create a new tapestry on client from set of thread definitions.
     *
     * @param threads
     * @return
     */
    static TapestryDefinition createTapestryFromThreadDefinitions(final LoomClient client,
            final List<ThreadDefinition> threads) {
        TapestryDefinition tapestryDefinition = createTapestryDefinitionFromThreadDefinitions(threads);
        return createTapestryFromTapestryDefinition(client, tapestryDefinition);
    }

    /**
     * Create a new tapestry on client from a TapestryDefinition.
     *
     * @param tapestryDefinition
     * @return
     */
    static TapestryDefinition createTapestryFromTapestryDefinition(final LoomClient client,
            final TapestryDefinition tapestryDefinition) {
        String tapestryId = clientCreateTapestryDefinition(client, tapestryDefinition);
        // Must set it otherwise tapestry definition has a null id
        tapestryDefinition.setId(tapestryId);
        return tapestryDefinition;
    }

    static void clientUpdateTapestryFromTapestryDefinition(final LoomClient client,
            final TapestryDefinition tapestryDefinition) {
        if (predictableOrdering) {
            convertToPredictableOrdering(tapestryDefinition);
        }
        StopWatch watch = new StopWatch();
        watch.start();
        client.updateTapestryDefinition(tapestryDefinition.getId(), tapestryDefinition);
        watch.stop();
        log.info("Update tapestry time=" + watch + " nthreads=" + tapestryDefinition.getThreads().size());
    }

    static void removeThreadsFromTapestry(final LoomClient client, final TapestryDefinition tapestryDefinition,
            final List<String> removeThreadIds) {
        if (log.isDebugEnabled()) {
            log.debug("Remove threads from tapestry " + removeThreadIds);
        }
        List<ThreadDefinition> newThreads = new ArrayList<ThreadDefinition>(tapestryDefinition.getThreads().size());
        for (ThreadDefinition thread : tapestryDefinition.getThreads()) {
            if (!removeThreadIds.contains(thread.getId())) {
                newThreads.add(thread);
            }
        }
        tapestryDefinition.setThreads(newThreads);
        clientUpdateTapestryFromTapestryDefinition(client, tapestryDefinition);
    }

    static ThreadDefinition findThreadDefinitionWithItemType(final List<ThreadDefinition> threads,
            final String itemType) {
        for (ThreadDefinition thread : threads) {
            if (thread.getItemType().endsWith(itemType)) {
                return thread;
            }
        }
        assertTrue("Could not find thread of type " + itemType, false);
        return null;
    }

    // public static String verifyAndGetJsonId(String jsonId) {
    // assertTrue(jsonId.contains("{"));
    // assertTrue(jsonId.contains("}"));
    // assertTrue(jsonId.contains("\"id\":"));
    // assertNotNull("id was null", jsonId);
    // jsonId = jsonId.replace("{", "");
    // jsonId = jsonId.replace("}", "");
    // jsonId = jsonId.replace("\"", "");
    // String tokens[] = jsonId.split("\\s+");
    // return tokens[tokens.length - 1];
    // }
}
