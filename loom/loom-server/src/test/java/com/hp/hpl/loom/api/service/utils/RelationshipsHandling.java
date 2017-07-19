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
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.api.client.LoomClient;
import com.hp.hpl.loom.api.service.IntegrationTestBase;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.QueryResultElement;

public interface RelationshipsHandling {
    static final Log log = LogFactory.getLog(RelationshipsHandling.class);

    static void checkAllElementsContainRelationships(final String message, final QueryResult qr) {
        boolean containsRelationships = true;
        for (QueryResultElement queryResultElement : qr.getElements()) {
            if (queryResultElement.getRelations().isEmpty()) {
                containsRelationships = false;
                break;
            }
        }
        assertTrue("Not every QRE contained relationships. " + message, containsRelationships);
    }

    static void checkSomeElementsContainRelationships(final String message, final QueryResult qr) {
        boolean containsRelationships = false;
        for (QueryResultElement queryResultElement : qr.getElements()) {
            if (!queryResultElement.getRelations().isEmpty()) {
                containsRelationships = true;
                break;
            }
        }
        assertTrue("QR did not contain any relationships. " + message, containsRelationships);
    }

    static void checkAnElementContainsRelationships(final String message, final QueryResult qr) {
        boolean containsRelationships = false;
        for (QueryResultElement queryResultElement : qr.getElements()) {
            if (!queryResultElement.getRelations().isEmpty()) {
                containsRelationships = true;
                break;
            }
        }
        assertTrue("QueryResult did not contain relationships. " + message, containsRelationships);
    }

    static void checkNoElementsContainEquivalenceRelationships(final String message, final QueryResult qr) {
        boolean containsRelationships = false;
        for (QueryResultElement queryResultElement : qr.getElements()) {
            if (!queryResultElement.getEquivalenceRelations().isEmpty()) {
                containsRelationships = true;
                break;
            }
        }
        assertFalse("A QRE contained equivalence relationships. " + message, containsRelationships);
    }

    static void checkAllElementsContainEquivalenceRelationships(final String message, final QueryResult qr) {
        boolean containsRelationships = true;
        for (QueryResultElement queryResultElement : qr.getElements()) {
            if (queryResultElement.getEquivalenceRelations().isEmpty()) {
                containsRelationships = false;
                break;
            }
        }
        assertTrue("Not every QRE did not contained equivalence relationships. " + message, containsRelationships);
    }

    static void checkAnElementContainsEquivalenceRelationships(final String message, final QueryResult qr) {
        boolean containsRelationships = false;
        for (QueryResultElement queryResultElement : qr.getElements()) {
            if (!queryResultElement.getEquivalenceRelations().isEmpty()) {
                containsRelationships = true;
                break;
            }
        }
        assertTrue("QueryResult did not contain equivalence relationships. " + message, containsRelationships);
    }

    static QueryResult checkRelationships(final LoomClient client, final String tapestryId, final String threadId) {
        return checkRelationships(client, tapestryId, threadId, true);
    }

    static QueryResult checkRelationships(final LoomClient client, final String tapestryId, final String threadId,
            final boolean allElementsContainRelations) {
        QueryResult qr =
                BasicQueryOperations.getThreadWithWait(client, tapestryId, threadId, IntegrationTestBase.greaterThan0);
        if (log.isTraceEnabled()) {
            log.trace("Query " + threadId + ": " + qr);
        }
        assertFalse("Query did not contain elements for " + qr.getItemType().getId() + " in" + threadId,
                qr.getElements().isEmpty());
        if (allElementsContainRelations) {
            checkAllElementsContainRelationships("Set of " + qr.getItemType().getId() + " in" + threadId, qr);
        } else {
            checkSomeElementsContainRelationships("Set of " + qr.getItemType().getId() + " in" + threadId, qr);
        }
        return qr;
    }

    static void checkCrossThreadRelationships(final QueryResult first, final QueryResult second) {
        // Index first QueryResult logical IDs
        Set<String> firstIds = new HashSet<String>();
        for (QueryResultElement qre : first.getElements()) {
            firstIds.add(qre.getEntity().getLogicalId());
        }

        // Index second QueryResult logical IDs
        Set<String> secondIds = new HashSet<String>();
        for (QueryResultElement qre : second.getElements()) {
            secondIds.add(qre.getEntity().getLogicalId());
            if (first.getExcludedItems() != null) {
                secondIds.add(first.getExcludedItems().getEntity().getLogicalId());
            }
        }

        // Check that every relation in first QueryResult matches an element in second thread
        for (QueryResultElement qre : first.getElements()) {
            for (String relatedTo : qre.getRelations()) {
                assertTrue("QRE " + qre.getEntity().getLogicalId() + " relation not found " + relatedTo,
                        secondIds.contains(relatedTo));
            }
        }

        // Check that every relation in second QueryResult matches an element in first thread
        for (QueryResultElement qre : second.getElements()) {
            for (String relatedTo : qre.getRelations()) {
                assertTrue("QRE " + qre.getEntity().getLogicalId() + " relation not found " + relatedTo,
                        firstIds.contains(relatedTo));
            }
        }
    }

    static void checkCrossThreadEquivalenceRelationships(final QueryResult first, final QueryResult second) {
        // Index first QueryResult logical IDs
        Set<String> firstIds = new HashSet<String>();
        for (QueryResultElement qre : first.getElements()) {
            firstIds.add(qre.getEntity().getLogicalId());
        }

        // Index second QueryResult logical IDs
        Set<String> secondIds = new HashSet<String>();
        for (QueryResultElement qre : second.getElements()) {
            secondIds.add(qre.getEntity().getLogicalId());
        }

        // Check that every relation in first QueryResult matches an element in second thread
        for (QueryResultElement qre : first.getElements()) {
            for (String relatedTo : qre.getEquivalenceRelations()) {
                assertTrue("QRE " + qre.getEntity().getLogicalId() + " equivalence relation not found " + relatedTo,
                        secondIds.contains(relatedTo));
            }
        }

        // Check that every relation in second QueryResult matches an element in first thread
        for (QueryResultElement qre : second.getElements()) {
            for (String relatedTo : qre.getEquivalenceRelations()) {
                assertTrue("QRE " + qre.getEntity().getLogicalId() + " equivalence relation not found " + relatedTo,
                        firstIds.contains(relatedTo));
            }
        }
    }


    static void checkCrossThreadRelationships(final List<QueryResult> queryResults) {
        // Index all fibres in QueryResults by logical ID
        Set<String> ids = new HashSet<String>();
        for (QueryResult qr : queryResults) {
            log.debug("queryResult contains " + qr.getElements().size() + " elements for " + qr.getLogicalId() + " "
                    + qr.getItemType().getId());
            for (QueryResultElement qre : qr.getElements()) {
                ids.add(qre.getEntity().getLogicalId());
            }
            if (qr.getExcludedItems() != null) {
                ids.add(qr.getExcludedItems().getEntity().getLogicalId());
            }

        }
        log.debug("There are a total of " + ids.size() + " elements on screen");

        // Check that all of the relations are actually fibres on-screen
        for (QueryResult qr : queryResults) {
            for (QueryResultElement qre : qr.getElements()) {
                for (String relatedTo : qre.getRelations()) {
                    if (!relatedTo.contains("!")) {
                        assertTrue(
                                "Thread " + qr.getLogicalId() + " " + qr.getItemType().getId() + " entity "
                                        + qre.getEntity().getLogicalId() + " relation not found " + relatedTo,
                                ids.contains(relatedTo));
                    }
                }
                for (String equivalentTo : qre.getEquivalenceRelations()) {
                    assertTrue(
                            "Thread " + qr.getLogicalId() + " " + qr.getItemType().getId() + " entity "
                                    + qre.getEntity().getLogicalId() + " relation not found " + equivalentTo,
                            ids.contains(equivalentTo));
                }
            }
        }
    }
}
