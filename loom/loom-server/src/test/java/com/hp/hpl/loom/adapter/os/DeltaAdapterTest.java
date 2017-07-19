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
package com.hp.hpl.loom.adapter.os;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.AggregationUpdate;
import com.hp.hpl.loom.adapter.ItemDeletionDelta;
import com.hp.hpl.loom.adapter.ItemRelationsDelta;
import com.hp.hpl.loom.adapter.UpdateDelta;
import com.hp.hpl.loom.adapter.UpdateResult;
import com.hp.hpl.loom.adapter.os.deltas.DeltaAdapter;
import com.hp.hpl.loom.adapter.os.deltas.DeltaOsSystem;
import com.hp.hpl.loom.adapter.os.deltas.DeltaResourceManager;
import com.hp.hpl.loom.adapter.os.deltas.TestMapHolder;
import com.hp.hpl.loom.adapter.os.fake.FakeItemCollector;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testDeltaAdapter.xml")
public class DeltaAdapterTest {

    private static final Log LOG = LogFactory.getLog(DeltaAdapterTest.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    private Tacker stitcher;
    @Autowired
    private AdapterManager adapterManager;
    @Autowired
    AdapterLoader adapterLoader;

    DeltaAdapter fakeAdapter;
    @Autowired
    ApplicationContext appContext;

    Provider prov;
    Session session;
    TestAdapterManagerImpl tam;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        fakeAdapter = (DeltaAdapter) adapterLoader.getAdapter("deltaAdapterPrivate.properties");
        prov = fakeAdapter.getProvider();
        session = new SessionImpl("session-" + UUID.randomUUID().toString(), sessionManager.getInterval());
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        // this triggers the data collection
        adapterManager.userConnected(session, prov, null);
        tam = (TestAdapterManagerImpl) adapterManager;
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
        if (session != null) {
            adapterManager.userDisconnected(session, prov, null);
            aggregationManager.deleteSession(session);
            stitcher.deleteSession(session);
            tam.clearAggregationUpdatesMap(session);
        }
    }


    @Test
    public void testJustSleep() throws InterruptedException {
        StopWatch watch = new StopWatch();
        LOG.info("testing JustSleep");
        watch.start();
        LOG.info("test just sleeping...");
        // sleep(1 * 60000);
        watch.stop();
        LOG.info("tested JustSleep --> " + watch);
    }

    @Test
    public void testDeltas() throws InterruptedException {
        StopWatch watch = new StopWatch();
        LOG.info("testing Deltas");
        watch.start();
        FakeItemCollector fic = (FakeItemCollector) fakeAdapter.getItemCollector(session);
        DeltaResourceManager drm = ((DeltaOsSystem) fic.getFos()).getFirstResourceManager("project-0", "region-0");
        Map<Integer, TestMapHolder> dataMap = drm.getDataMap();
        int roundMax = 7;
        Collection<AggregationUpdate> aggUpdates;
        int roundIdx = 1;
        while (roundIdx < roundMax) {
            int roundLimit = waitForNextUpdate(roundIdx);
            for (int i = roundIdx; i <= roundLimit; ++i) {
                LOG.debug("starting round: " + i);
                TestMapHolder tmh = dataMap.get(i);
                assertTrue("Expected non null TestMapHolder", tmh != null);
                int aggCount = tmh.getAggCount();
                if (!isDeltaMode(i)) {
                    aggCount = 7;
                }
                aggUpdates = tam.getAggregationUpdates(session, i);
                assertTrue("Expected " + aggCount + " aggregations: " + aggUpdates.size(),
                        aggUpdates.size() == aggCount);
                for (AggregationUpdate aggUp : aggUpdates) {
                    UpdateResult upRes = aggUp.getUpdateResult();
                    upRes.setIgnore(false);
                    String typeLocalId = aggUp.getAggregation().getTypeId().substring("os-".length());
                    checkUpdateResult(upRes, typeLocalId, tmh);
                    UpdateDelta upDelta = upRes.getUpdateDelta();
                    if (isDeltaMode(i)) {
                        if (i == 1) {
                            assertTrue("Expected NULL UpdateDelta for " + typeLocalId + "s ", upDelta == null);
                        } else {
                            assertTrue("Expected non null UpdateDelta for " + typeLocalId + "s ", upDelta != null);
                            checkUpdateDelta(upDelta, typeLocalId, tmh);
                        }
                    } else {
                        assertTrue("Expected NULL UpdateDelta for " + typeLocalId + "s ", upDelta == null);
                    }
                }
                LOG.debug("SUCCESS for round " + i);
                roundIdx = i + 1;
            }
        }
        // end of test
        watch.stop();
        LOG.info("tested Deltas --> " + watch);
    }

    private boolean isDeltaMode(final int i) {
        // return false;
        // return (i % 2 == 0);
        return true;
    }

    private int waitForNextUpdate(final int nextExpected) throws InterruptedException {
        int mapSize = tam.getAggregationUpdatesMapSize(session);
        int waitMax = 20;
        while ((mapSize < nextExpected) && ((waitMax--) > 0)) {
            LOG.info("waiting for aggregation updates - 1s sleep...");
            sleep(1000);
            mapSize = tam.getAggregationUpdatesMapSize(session);
        }
        assertTrue("Expected an aggregation update within 20 secs", waitMax > 0);
        return mapSize;
    }

    private void checkUpdateResult(final UpdateResult upRes, final String typeLocalId, final TestMapHolder tmh) {
        ArrayList<Item> allItems = upRes.getAllItems();
        ArrayList<Item> newItems = upRes.getNewItems();
        ArrayList<Item> deletedItems = upRes.getDeletedItems();
        ArrayList<Item> updatedItems = upRes.getUpdatedItems();
        assertTrue("Expected " + tmh.getNbrs(typeLocalId).getAllNbr() + " ALL " + typeLocalId + "s: " + allItems.size(),
                allItems.size() == tmh.getNbrs(typeLocalId).getAllNbr());
        assertTrue("Expected " + tmh.getNbrs(typeLocalId).getNewNbr() + " NEW " + typeLocalId + "s: " + newItems.size(),
                newItems.size() == tmh.getNbrs(typeLocalId).getNewNbr());
        assertTrue("Expected " + tmh.getNbrs(typeLocalId).getUpdatedNbr() + " UPDATED " + typeLocalId + "s: "
                + updatedItems.size(), updatedItems.size() == tmh.getNbrs(typeLocalId).getUpdatedNbr());
        assertTrue("Expected " + tmh.getNbrs(typeLocalId).getDeletedNbr() + " DELETED " + typeLocalId + "s: "
                + deletedItems.size(), deletedItems.size() == tmh.getNbrs(typeLocalId).getDeletedNbr());
    }

    private void checkUpdateDelta(final UpdateDelta upDelta, final String typeLocalId, final TestMapHolder tmh) {
        Collection<ItemRelationsDelta> relDeltas = upDelta.getRelationsDelta();
        assertTrue("Expected non null Collection<ItemRelationsDelta>", relDeltas != null);
        int relDeltaNbr = tmh.getNbrs(typeLocalId).getRelDeltaNbr();
        // add in the new records count (to cover the provider links)
        assertTrue(
                "Expected " + (relDeltaNbr + tmh.getNbrs(typeLocalId).getNewNbr()) + " Relationship Deltas for "
                        + typeLocalId + "s: " + relDeltas.size(),
                relDeltas.size() == relDeltaNbr + tmh.getNbrs(typeLocalId).getNewNbr());
        Collection<ItemDeletionDelta> delDeltas = upDelta.getDeletionDelta();
        int delDeltaNbr = tmh.getNbrs(typeLocalId).getDeletedDeltaNbr();
        assertTrue("Expected " + delDeltaNbr + " Deletion Deltas for " + typeLocalId + "s: " + delDeltas.size(),
                delDeltas.size() == delDeltaNbr);
    }

    // test json serialization
    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }

    private void sleep(final long sleepTime) throws InterruptedException {
        LOG.info("test sleeping for: " + sleepTime);
        Thread.sleep(sleepTime);
        LOG.info("test just woke up");
    }

}
