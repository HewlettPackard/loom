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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
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
import com.hp.hpl.loom.adapter.os.fake.FakeAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeItemCollector;
import com.hp.hpl.loom.adapter.os.fake.FakeOsSystem;
import com.hp.hpl.loom.adapter.os.fake.FakeProject;
import com.hp.hpl.loom.adapter.os.fake.FakeProviderImpl;
import com.hp.hpl.loom.adapter.os.fake.FakeResourceManager;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchUserException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testFakeAdapter.xml")
public class FakeAdapterTest {

    private static final Log LOG = LogFactory.getLog(FakeAdapterTest.class);

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

    FakeAdapter fakeAdapter;
    @Autowired
    ApplicationContext appContext;

    Provider prov;
    Session session;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        fakeAdapter = (FakeAdapter) adapterLoader.getAdapter("fakeAdapterPrivate.properties");
        prov = fakeAdapter.getProvider();
        session = new SessionImpl("session-" + UUID.randomUUID().toString(), sessionManager.getInterval());
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        // this triggers the data collection
        adapterManager.userConnected(session, prov, null);
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
        if (session != null) {
            adapterManager.userDisconnected(session, prov, null);
            aggregationManager.deleteSession(session);
            stitcher.deleteSession(session);
        }
    }

    private PropertiesConfiguration loadProperties() throws ConfigurationException {
        String deploymentProperties = "src/test/resources/deployment-fakeAdapterTest.properties";
        PropertiesConfiguration props = new PropertiesConfiguration(deploymentProperties);
        return props;
        // try {
        // Properties prop = null;
        // Resource res = appContext.getResource(deploymentProperties);
        // InputStream in = res.getInputStream();
        // if (in == null) {
        // LOG.warn("Failed to locate properties file on classpath: " + deploymentProperties);
        // } else {
        // LOG.info("Found '" + deploymentProperties + "' on the classpath");
        // prop = new Properties();
        // prop.load(in);
        // }
        // return prop;
        // } catch (IOException e) {
        // LOG.error("Failed to load properties file '" + deploymentProperties + "'", e);
        // throw new RuntimeException("Failed to initialise from properties file " +
        // deploymentProperties);
        // }
    }

    private int[] parseIntArray(final String arrayOfInts) {
        String[] intStringArray = arrayOfInts.split(",");
        int[] intArray = new int[intStringArray.length];
        for (int count = 0; count < intStringArray.length; count++) {
            intArray[count] = Integer.parseInt(intStringArray[count]);
        }
        return intArray;
    }

    @Test
    public void testAdapterCreation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterCreation");
        watch.start();


        Provider prov =
                new FakeProviderImpl("os", "private", "http://16.25.166.21:5000/v2.0", "Private", "test", "test");
        assertNotNull("adapter not created", fakeAdapter);
        assertEquals(true, prov.equals(fakeAdapter.getProvider()));
        watch.stop();
        LOG.info("tested AdapterCreation --> " + watch);
    }

    @Test
    public void testQuotas() throws NoSuchSessionException, InterruptedException, NoSuchItemTypeException,
            NoSuchItemTypeException, NoSuchProviderException, ConfigurationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing Quotas");
        watch.start();
        FakeItemCollector fic = (FakeItemCollector) fakeAdapter.getItemCollector(session);
        FakeOsSystem fos = fic.getFos();
        int iMax = 20;
        int i = 0;
        List<Fibre> elems = null;
        Aggregation agg = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            agg = aggregationManager.getAggregation(session,
                    fakeAdapter.getAggregationLogicalId(OsProjectType.TYPE_LOCAL_ID));
            if (agg != null) {
                elems = agg.getElements();
                if ((elems != null) && (!elems.isEmpty())) {
                    // ok we've got data
                    break;
                }
            }
            if (i < iMax) {
                sleep(500);
            }
        }
        assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation", i < iMax);
        // wait 5 secs then grab one instance and stop it
        assertTrue("elems should not be null", (elems != null));
        assertTrue("elems should include OsInstances", (elems.get(0) instanceof OsProject));
        PropertiesConfiguration prop = loadProperties();
        DataConfiguration dProps = new DataConfiguration(prop);
        assertTrue("prop should NOT be null", prop != null);
        int repeatNbr = dProps.getInt("fake.quota.repeatNbr");
        int[] instanceQuota = dProps.getIntArray("fake.quota.instances");
        int[] coreQuota = dProps.getIntArray("fake.quota.cores");
        int[] volumeQuota = dProps.getIntArray("fake.quota.volumes");
        int[] gigabyteQuota = dProps.getIntArray("fake.quota.gigabytes");
        int[] ramQuota = dProps.getIntArray("fake.quota.ram");
        // check used values with config
        // Fos should return objects in the order they were created (prj_0, prj_1, etc.): this test
        // relies on this
        ArrayList<FakeProject> projects = fos.getProjects();
        int projIdx = 0;
        for (FakeProject item : projects) {
            int loopIdx = projIdx % repeatNbr;
            OsQuota osQuota = fos.getOsQuota(item.getItemId());
            assertTrue("instanceQuota should match config: " + osQuota.getInstances() + " <> " + instanceQuota[loopIdx],
                    osQuota.getInstances() == instanceQuota[loopIdx]);
            assertTrue("coreQuota should match config: " + osQuota.getCores() + " <> " + coreQuota[loopIdx],
                    osQuota.getCores() == coreQuota[loopIdx]);
            assertTrue("volumeQuota should match config: " + osQuota.getVolumes() + " <> " + volumeQuota[loopIdx],
                    osQuota.getVolumes() == volumeQuota[loopIdx]);
            assertTrue("gigabyteQuota should match config: " + osQuota.getGigabytes() + " <> " + gigabyteQuota[loopIdx],
                    osQuota.getGigabytes() == gigabyteQuota[loopIdx]);
            assertTrue("ramQuota should match config: " + osQuota.getRam() + " <> " + ramQuota[loopIdx],
                    osQuota.getRam() == ramQuota[loopIdx]);
            projIdx++;
        }
        // check that project held by aggregationManager has the correct values
        for (Fibre elem : elems) {
            OsProject proj = (OsProject) elem;
            OsQuota osQuota = fos.getOsQuota(proj.getCore().getItemId());
            assertTrue("instanceQuota should match fos data and project",
                    osQuota.getInstances() == proj.getCore().getInstancesQuota());
            assertTrue("coreQuota should match fos data and project",
                    osQuota.getCores() == proj.getCore().getCoresQuota());
            assertTrue("volumeQuota should match fos data and project",
                    osQuota.getVolumes() == proj.getCore().getVolumesQuota());
            assertTrue("gigabyteQuota should match fos data and project",
                    osQuota.getGigabytes() == proj.getCore().getGigabytesQuota());
            assertTrue("ramQuota should match fos data and project", osQuota.getRam() == proj.getCore().getRamQuota());
        }
        watch.stop();
        LOG.info("tested Quotas --> " + watch);
    }

    @Test
    public void testJustSleep() throws InterruptedException {
        StopWatch watch = new StopWatch();
        LOG.info("testing JustSleep");
        watch.start();
        Provider prov =
                new FakeProviderImpl("os", "private", "http://16.25.166.21:5000/v2.0", "Private", "test", "test");
        assertNotNull("adapter not created", fakeAdapter);
        LOG.info("prov: " + prov);
        LOG.info("faProv: " + fakeAdapter.getProvider());
        assertEquals(true, prov.equals(fakeAdapter.getProvider()));
        LOG.info("test just sleeping...");
        // sleep(1 * 60000);
        watch.stop();
        LOG.info("tested JustSleep --> " + watch);
    }

    @Test
    public void testDataGeneration()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchUserException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing Activation");
        watch.start();
        // get aggregations
        List<String> itemTypeIdList = Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                OsImageType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID,
                OsRegionType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID);
        // compare adapter data with what the aggregationManager has
        int iMax = 20;
        FakeItemCollector fic = (FakeItemCollector) fakeAdapter.getItemCollector(session);
        FakeOsSystem fos = fic.getFos();
        for (String itemTypeId : itemTypeIdList) {
            int i = 0;
            for (i = 0; i < iMax; ++i) {
                // check if aggregationManager and adapterManager view match
                LOG.info("data check: iteration " + i);
                // LOG.warn("------------> DATA FROM ADAPTER:
                // \n"+((OsItemCollector)fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                // LOG.warn("zzz trying to get aggregation for logicalId:
                // "+fakeAdapter.getAggregationLogicalId(itemTypeId));
                Aggregation agg =
                        aggregationManager.getAggregation(session, fakeAdapter.getAggregationLogicalId(itemTypeId));
                if (agg != null) {
                    List<Fibre> elems = agg.getElements();
                    if (itemTypeId.equals(OsInstanceType.TYPE_LOCAL_ID)) {
                        if ((elems != null) && (elems.size() == fos.getTotalInstanceNbr())) {
                            if (!elems.isEmpty()) {
                                checkEquality(elems, ((OsItemCollector) fakeAdapter.getItemCollector(session))
                                        .getNewItems(itemTypeId));
                            }
                            break;
                        }
                    } else if ((elems != null) && (!elems.isEmpty())) {
                        checkEquality(elems,
                                ((OsItemCollector) fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                        break;
                    }
                }
                if (i < iMax) {
                    sleep(500);
                }
            }
            assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation",
                    i < iMax);
        }
        // now just wait to see adapter polling again
        // LOG.info("test sleeping for a minute to see if adapter polls ok");
        // sleep(5*60000);
        // LOG.info("test finished sleeping");
        watch.stop();
        LOG.info("tested Activation --> " + watch);
    }

    private void checkEquality(final List<Fibre> elems, final Collection<Item> items) {
        LOG.info("Testing equality of aggregation and adapter content");
        assertEquals(true, elems.size() == items.size());
        for (Item item : items) {
            assertTrue("item not found", elems.contains(item));
            // check fully qualified name is set
            assertTrue("fqn should be set", item.getFullyQualifiedName() != null);
        }

        /*
         * // hack to debug relationship generation if (elems.get(0) instanceof Instance) {
         * LOG.warn(
         * "RELATIONSHIP-DEBUG: from instance, Image is "+((Instance)elems.get(0)).getImage(
         * ).getLogicalId()); for (int i=0; i<elems.size(); ++i) { try{
         * LOG.warn("Instance: "+toJson((Instance)elems.get(i))); }catch(JsonProcessingException
         * e){} } } if (elems.get(0) instanceof Image){
         * LOG.warn("RELATIONSHIP-DEBUG: from Image, instance is"
         * +((Image)elems.get(0)).getThreadWithWait().iterator().next().getLogicalId());
         * LOG.warn("Count: "+((Image)elems.get(0)).getThreadWithWait().size()); try{
         * LOG.warn("Image: "+toJson((Image)elems.get(0))); }catch(JsonProcessingException e){} for
         * (LoomEntity elem : elems) { LOG.warn("COUNT: "+((Image)elem).getUsageCount()); } } if
         * (elems.get(0) instanceof Region) { for (int i=0; i<elems.size(); ++i) { try{
         * LOG.warn("Region: "+toJson((Region)elems.get(i))); }catch(JsonProcessingException e){} }
         * } if (elems.get(0) instanceof Project) { for (int i=0; i<elems.size(); ++i) { try{
         * LOG.warn("Project: "+toJson((Project)elems.get(i))); }catch(JsonProcessingException e){}
         * } }
         */
    }

    @Test
    public void testNewItems()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing NewItems");
        watch.start();
        int iMax = 20000;
        int i = 0;
        HashMap<String, CountBag> countMap = new HashMap<>();
        OsInstance vm0 = null;
        // get aggregations
        List<String> itemTypeIdList = Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                OsImageType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID,
                OsRegionType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID);
        // compare adapter data with what the aggregationManager has
        for (String itemTypeId : itemTypeIdList) {
            for (i = 0; i < iMax; ++i) {
                // check if aggregationManager and adapterManager view match
                LOG.info("data check: iteration " + i);
                // LOG.warn("------------> DATA FROM ADAPTER:
                // \n"+((OsItemCollector)fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                // LOG.warn("zzz trying to get aggregation for logicalId:
                // "+fakeAdapter.getAggregationLogicalId(itemTypeId));
                Aggregation agg =
                        aggregationManager.getAggregation(session, fakeAdapter.getAggregationLogicalId(itemTypeId));
                if (agg != null) {
                    List<Fibre> elems = agg.getElements();
                    if ((elems != null) && (!elems.isEmpty())) {
                        countMap.put(itemTypeId, new CountBag(elems.size(), 0, 0, 0, agg.getFibreUpdated()));
                        if (itemTypeId.equals(OsInstanceType.TYPE_LOCAL_ID)) {
                            vm0 = (OsInstance) elems.get(0);
                        }
                        break;
                    }
                }
                if (i < iMax) {
                    sleep(500);
                }
            }
            assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation",
                    i < iMax);
        }
        assertTrue("a VM should have been selected for adding New ones to teh same region", vm0 != null);
        int nbr = 4;
        LOG.info("iteration: " + i + " - adding extra VMs with volumes/subnet/network: " + nbr);
        FakeItemCollector fic = (FakeItemCollector) fakeAdapter.getItemCollector(session);
        FakeResourceManager frm = fic.getFos().getResourceManager(
                vm0.getFirstConnectedItemWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        prov.getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)),
                vm0.getFirstConnectedItemWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        prov.getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).getName());
        frm.addInstances(nbr);
        // now wait for the next update and check outcome
        itemTypeIdList = Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsImageType.TYPE_LOCAL_ID,
                OsRegionType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID);
        for (String itemTypeId : itemTypeIdList) {
            for (i = 0; i < iMax; ++i) {
                // check if aggregationManager and adapterManager view match
                LOG.info("data check: iteration " + i);
                // LOG.warn("------------> DATA FROM ADAPTER:
                // \n"+((OsItemCollector)fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                // LOG.warn("zzz trying to get aggregation for logicalId:
                // "+fakeAdapter.getAggregationLogicalId(itemTypeId));
                Aggregation agg =
                        aggregationManager.getAggregation(session, fakeAdapter.getAggregationLogicalId(itemTypeId));
                if (agg != null) {
                    Date aggUpdate = agg.getFibreUpdated();
                    Date lastUpdate = countMap.get(itemTypeId).getLastUpdateSeen();
                    if (aggUpdate.after(lastUpdate)) {
                        // new data
                        if (itemTypeId.equals(OsInstanceType.TYPE_LOCAL_ID)) {
                            assertTrue("expected instance all + nbr",
                                    agg.getElements().size() == (countMap.get(itemTypeId).getAll() + nbr));
                            assertTrue("expected instance new == nbr", agg.getCreatedCount() == nbr);
                            break;
                        }
                        if (itemTypeId.equals(OsImageType.TYPE_LOCAL_ID)) {
                            assertTrue("expected image all constant",
                                    agg.getElements().size() == countMap.get(itemTypeId).getAll());
                            assertTrue("expected image updated", agg.getUpdatedCount() != 0);
                            break;
                        }
                        if (itemTypeId.equals(OsRegionType.TYPE_LOCAL_ID)) {
                            assertTrue("expected region all constant",
                                    agg.getElements().size() == countMap.get(itemTypeId).getAll());
                            assertTrue("expected region updated", agg.getUpdatedCount() == 1);
                            break;
                        }
                        if (itemTypeId.equals(OsProjectType.TYPE_LOCAL_ID)) {
                            assertTrue("expected project all constant",
                                    agg.getElements().size() == countMap.get(itemTypeId).getAll());
                            assertTrue("expected project updated", agg.getUpdatedCount() == 1);
                            break;
                        }
                    }
                }
                if (i < iMax) {
                    sleep(500);
                }
            }
            assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation for "
                    + itemTypeId, i < iMax);
        }
        watch.stop();
        LOG.info("tested NewItems --> " + watch);
    }

    @Test
    public void testDeleteItems()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing DeleteItems");
        watch.start();
        int iMax = 20;
        int i = 0;
        HashMap<String, CountBag> countMap = new HashMap<>();
        OsInstance vm0 = null;
        // get aggregations
        List<String> itemTypeIdList = Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsVolumeType.TYPE_LOCAL_ID,
                OsImageType.TYPE_LOCAL_ID, OsNetworkType.TYPE_LOCAL_ID, OsSubnetType.TYPE_LOCAL_ID,
                OsRegionType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID);
        // compare adapter data with what the aggregationManager has
        for (String itemTypeId : itemTypeIdList) {
            for (i = 0; i < iMax; ++i) {
                // check if aggregationManager and adapterManager view match
                LOG.info("data check: iteration " + i);
                // LOG.warn("------------> DATA FROM ADAPTER:
                // \n"+((OsItemCollector)fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                // LOG.warn("zzz trying to get aggregation for logicalId:
                // "+fakeAdapter.getAggregationLogicalId(itemTypeId));
                Aggregation agg =
                        aggregationManager.getAggregation(session, fakeAdapter.getAggregationLogicalId(itemTypeId));
                if (agg != null) {
                    List<Fibre> elems = agg.getElements();
                    if ((elems != null) && (!elems.isEmpty())) {
                        countMap.put(itemTypeId, new CountBag(elems.size(), 0, 0, 0, agg.getFibreUpdated()));
                        if (itemTypeId.equals(OsInstanceType.TYPE_LOCAL_ID)) {
                            vm0 = (OsInstance) elems.get(0);
                        }
                        break;
                    }
                }
                if (i < iMax) {
                    sleep(500);
                }
            }
            assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation",
                    i < iMax);
        }
        assertTrue("a VM should have been selected for adding New ones to teh same region", vm0 != null);
        int nbr = 2;
        LOG.info("iteration: " + i + " - adding extra VMs with volumes/subnet/network: " + nbr);
        FakeItemCollector fic = (FakeItemCollector) fakeAdapter.getItemCollector(session);
        FakeResourceManager frm = fic.getFos().getResourceManager(
                vm0.getFirstConnectedItemWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        prov.getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)),
                vm0.getFirstConnectedItemWithRelationshipName(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                        prov.getProviderType(), OsInstanceType.TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).getName());
        frm.deleteInstances(nbr);
        // now wait for the next update and check outcome
        itemTypeIdList = Arrays.asList(OsInstanceType.TYPE_LOCAL_ID, OsImageType.TYPE_LOCAL_ID,
                OsRegionType.TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID);
        for (String itemTypeId : itemTypeIdList) {
            for (i = 0; i < iMax; ++i) {
                // check if aggregationManager and adapterManager view match
                LOG.info("data check: iteration " + i + " (" + itemTypeId + ")");
                // LOG.warn("------------> DATA FROM ADAPTER:
                // \n"+((OsItemCollector)fakeAdapter.getItemCollector(session)).getNewItems(itemTypeId));
                // LOG.warn("zzz trying to get aggregation for logicalId:
                // "+fakeAdapter.getAggregationLogicalId(itemTypeId));
                Aggregation agg =
                        aggregationManager.getAggregation(session, fakeAdapter.getAggregationLogicalId(itemTypeId));
                if (agg != null) {
                    Date aggUpdate = agg.getFibreUpdated();
                    Date lastUpdate = countMap.get(itemTypeId).getLastUpdateSeen();
                    if (aggUpdate.after(lastUpdate)) {
                        // new data
                        if (itemTypeId.equals(OsInstanceType.TYPE_LOCAL_ID)) {
                            assertTrue("expected instance all - nbr",
                                    agg.getElements().size() == (countMap.get(itemTypeId).getAll() - nbr));
                            assertTrue("expected instance deleted == nbr", agg.getDeletedCount() == nbr);
                            break;
                        }
                        if (itemTypeId.equals(OsImageType.TYPE_LOCAL_ID)) {
                            assertTrue("expected image all constant",
                                    agg.getElements().size() == countMap.get(itemTypeId).getAll());
                            assertTrue("expected image updated", agg.getUpdatedCount() != 0);
                            break;
                        }
                        if (itemTypeId.equals(OsRegionType.TYPE_LOCAL_ID)) {
                            assertTrue("expected region all constant",
                                    agg.getElements().size() == countMap.get(itemTypeId).getAll());
                            assertTrue("expected region updated", agg.getUpdatedCount() == 1);
                            break;
                        }
                        if (itemTypeId.equals(OsProjectType.TYPE_LOCAL_ID)) {
                            assertTrue("expected project all constant",
                                    agg.getElements().size() == countMap.get(itemTypeId).getAll());
                            assertTrue("expected project updated", agg.getUpdatedCount() == 1);
                            break;
                        }
                    }
                }
                if (i < iMax) {
                    sleep(500);
                }
            }
            assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation",
                    i < iMax);
        }
        watch.stop();
        LOG.info("tested DeleteItems --> " + watch);
    }

    private Action createPowerAction(final String value) {
        ActionParameters powerParameters = new ActionParameters();
        try {
            ActionParameter ap = new ActionParameter("power", ActionParameter.Type.ENUMERATED, "power options", null);
            ap.setValue(value);
            powerParameters.add(ap);
            return new Action("power", "a", "b", "icon-cycle", powerParameters);
        } catch (InvalidActionSpecificationException e) {
            return null;
        }
    }

    @Test
    public void testAction()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing Action");
        watch.start();
        int iMax = 20;
        int i = 0;
        List<Fibre> elems = null;
        Aggregation agg = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            agg = aggregationManager.getAggregation(session,
                    fakeAdapter.getAggregationLogicalId(OsInstanceType.TYPE_LOCAL_ID));
            if (agg != null) {
                elems = agg.getElements();
                if ((elems != null) && (!elems.isEmpty())) {
                    // ok we've got data
                    break;
                }
            }
            if (i < iMax) {
                sleep(500);
            }
        }
        assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation", i < iMax);
        // wait 5 secs then grab one instance and stop it
        assertTrue("elems should not be null", (elems != null));
        assertTrue("elems should include OsInstances", (elems.get(0) instanceof OsInstance));
        OsInstance vm0 = (OsInstance) elems.get(0);
        try {
            LOG.info("iteration: " + i + "- BEFORE ACTION: vm json: \n" + toJson(elems.get(0)));
        } catch (JsonProcessingException e) {
        }
        ArrayList<Item> vmList = new ArrayList<>();
        vmList.add(vm0);
        Action stopAction = createPowerAction("stop");
        fakeAdapter.doAction(session, stopAction, vmList);
        iMax = 5;
        long waitTime = 2000;
        OsInstance vm = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            for (Fibre elem : elems) {
                if (elem.getLogicalId().equals(vm0.getLogicalId())) {
                    vm = (OsInstance) elem;
                    break;
                }
            }
            assertTrue("vm should have been found and not null", vm != null);
            try {
                LOG.info("vm json: \n" + toJson(vm));
            } catch (JsonProcessingException e) {
            }
            if (vm.getCore().getStatus().equals("SHUTOFF")) {
                break;
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }
        assertTrue("should not take " + iMax * 2 + " secs for status to change to SHUTOFF", i < iMax);
        assertTrue("vm should have been found and not null", vm != null);
        assertTrue("status should be set to SHUTOFF", vm.getCore().getStatus().equals("SHUTOFF"));
        // now start it again
        vmList = new ArrayList<>();
        vmList.add(vm0);
        Action startAction = createPowerAction("start");
        fakeAdapter.doAction(session, startAction, vmList);
        iMax = 5;
        vm = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            for (Fibre elem : elems) {
                if (elem.getLogicalId().equals(vm0.getLogicalId())) {
                    vm = (OsInstance) elem;
                    break;
                }
            }
            assertTrue("vm should have been found and not null", vm != null);
            try {
                LOG.info("vm json: \n" + toJson(vm));
            } catch (JsonProcessingException e) {
            }
            if (vm.getCore().getStatus().equals("ACTIVE")) {
                break;
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }
        assertTrue("should not take " + iMax * 2 + " secs for status to change to ACTIVE", i < iMax);
        assertTrue("status should be set to ACTIVE", vm.getCore().getStatus().equals("ACTIVE"));
        // now try reboot
        vmList = new ArrayList<>();
        vmList.add(vm0);
        Action rebootAction = createPowerAction("softReboot");
        fakeAdapter.doAction(session, rebootAction, vmList);
        iMax = 10;
        vm = null;
        String oldCheckValue = "BAH";
        String checkValue = "SHUTOFF";
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            // find new object
            for (Fibre elem : elems) {
                if (elem.getLogicalId().equals(vm0.getLogicalId())) {
                    vm = (OsInstance) elem;
                    break;
                }
            }
            assertTrue("vm should have been found and not null", vm != null);
            try {
                LOG.info("vm json: \n" + toJson(vm));
            } catch (JsonProcessingException e) {
            }
            if (vm.getCore().getStatus().equals(checkValue)) {
                if ((oldCheckValue.equals("SHUTOFF")) && (checkValue.equals("ACTIVE"))) {
                    break;
                }
                oldCheckValue = "SHUTOFF";
                checkValue = "ACTIVE";
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }
        assertTrue("should not take " + iMax * 2 + " secs for status to change from ACTIVE to SHUTOFF to ACTIVE again",
                i < iMax);
        assertTrue("status should be set to ACTIVE", vm.getCore().getStatus().equals("ACTIVE"));
        watch.stop();
        LOG.info("tested Action --> " + watch);
    }

    @Test
    public void testUsageCount()
            throws NoSuchSessionException, InterruptedException, NoSuchProviderException, SessionAlreadyExistsException,
            DuplicateAdapterException, UserAlreadyConnectedException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing UsageCount");
        watch.start();
        int iMax = 20;
        int i = 0;
        List<Fibre> elems = null;
        Aggregation agg = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            agg = aggregationManager.getAggregation(session,
                    fakeAdapter.getAggregationLogicalId(OsInstanceType.TYPE_LOCAL_ID));
            if (agg != null) {
                elems = agg.getElements();
                if ((elems != null) && (!elems.isEmpty())) {
                    // ok we've got data
                    break;
                }
            }
            if (i < iMax) {
                sleep(500);
            }
        }
        assertTrue("should not take " + iMax / 2 + " secs for aggregationManager to get groundedAggregation", i < iMax);
        // wait 5 secs then grab one instance and stop it
        assertTrue("elems should not be null", (elems != null));
        assertTrue("elems should include OsInstances", (elems.get(0) instanceof OsInstance));
        OsInstance vm0 = (OsInstance) elems.get(0);
        // grab image for that instance
        OsImage baImage = (OsImage) vm0.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(prov.getProviderType(),
                        OsInstanceType.TYPE_LOCAL_ID, OsImageType.TYPE_LOCAL_ID));
        int baUsageCount = baImage.getUsageCount();
        // now issue an action
        try {
            LOG.info("BEFORE ACTION: vm json: \n" + toJson(elems.get(0)));
        } catch (JsonProcessingException e) {
        }
        ArrayList<Item> vmList = new ArrayList<>();
        vmList.add(vm0);
        Action stopAction = createPowerAction("stop");
        fakeAdapter.doAction(session, stopAction, vmList);
        iMax = 5;
        long waitTime = 2000;
        OsInstance vm = null;
        for (i = 0; i < iMax; ++i) {
            // check if aggregationManager and adapterManager view match
            LOG.info("action check: iteration " + i);
            elems = agg.getElements();
            // find new object
            for (Fibre elem : elems) {
                if (elem.getLogicalId().equals(vm0.getLogicalId())) {
                    vm = (OsInstance) elem;
                    break;
                }
            }
            assertTrue("vm should have been found and not null", vm != null);
            try {
                LOG.info("vm json: \n" + toJson(vm));
            } catch (JsonProcessingException e) {
            }
            if (vm.getCore().getStatus().equals("SHUTOFF")) {
                break;
            }
            if (i < iMax) {
                sleep(waitTime);
            }
        }
        assertTrue("should not take " + iMax * 2 + " secs to detect powerStatusChange to SHUTOFF", i < iMax);
        assertTrue("status should be set to ACTIVE", vm.getCore().getStatus().equals("SHUTOFF"));
        // grab image for that instance
        OsImage aaImage = (OsImage) vm0.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(prov.getProviderType(),
                        OsInstanceType.TYPE_LOCAL_ID, OsImageType.TYPE_LOCAL_ID));
        int aaUsageCount = aaImage.getUsageCount();
        assertTrue("usageCount should not change: before: " + baUsageCount + " after: " + aaUsageCount,
                aaUsageCount == baUsageCount);
        watch.stop();
        LOG.info("tested UsageCount --> " + watch);
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
