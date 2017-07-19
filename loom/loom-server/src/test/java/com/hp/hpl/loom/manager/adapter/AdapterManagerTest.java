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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.adapter.LoomUtils;
import com.hp.hpl.loom.adapter.os.DoNothingAdapter;
import com.hp.hpl.loom.adapter.os.FakeType;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.fake.FakeAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeProviderImpl;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.CheckedLoomException;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.DuplicateItemTypeException;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchUserException;
import com.hp.hpl.loom.exceptions.NullItemTypeIdException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;
import com.hp.hpl.loom.tapestry.PatternDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testAdapterManager.xml")
public class AdapterManagerTest {

    private static final Log LOG = LogFactory.getLog(AdapterManagerTest.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private AdapterManager adapterManager;
    @Autowired
    private TapestryManager tapestryManager;
    @Autowired
    private ItemTypeManager itemTypeManager;
    @Autowired
    private AggregationManager aggregationManager;
    @Autowired
    private Tacker stitcher;
    @Autowired
    private AdapterLoader adapterLoader;

    private FakeAdapter fakeAdapter;
    private Provider provider;


    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        fakeAdapter = (FakeAdapter) adapterLoader.getAdapter("fakeAdapterPrivate.properties");
        provider = fakeAdapter.getProvider();
    }

    @After
    public void shutDown() throws Exception {
        Provider prov = new ProviderImpl("os", "testfake", "http://whatever", "Testfake", "com");
        createUnregisteredDoNothingAdapter(prov);
        try {
            itemTypeManager.removeAllItemTypes(prov);
        } catch (NoSuchProviderException ex) {

        }
        LOG.info("shutDown test");
    }

    @Test(expected = NoSuchProviderException.class)
    public void testGetProvider() throws DuplicateAdapterException, NoSuchProviderException {
        // fakeAdapter.registerWithAdapterManager();
        LOG.info("testGetProvider start");
        assertTrue("getProvider returns wrong provider or null",
                adapterManager.getProvider(provider.getProviderType(), provider.getProviderId()).equals(provider));
        adapterManager.getProvider("noProviderType", "noProviderId");
        LOG.info("testGetProvider end");
    }

    @Test
    public void testGetProviders() {
        LOG.info("testGetProviders start");
        List<Provider> providers = adapterManager.getProviders();
        assertTrue("The number of providers is wrong " + providers.size(), adapterManager.getProviders().size() == 3);
        // confirm the order
        assertEquals("Private", providers.get(0).getProviderName());
        assertEquals("Private", providers.get(1).getProviderName());
        assertEquals("Public", providers.get(2).getProviderName());
        LOG.info("testGetProviders end");
    }

    @Test
    public void testGetProvidersOfType() {
        LOG.info("testGetProvidersOfType start");
        assertTrue("The number of providers for a given type is wrong " + adapterManager.getProviders().size(),
                adapterManager.getProviders(provider.getProviderType()).size() == 3);
        LOG.info("testGetProvidersOfType end");
    }

    @Test(expected = DuplicateAdapterException.class)
    public void testProviderIdClash()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing ProviderIdClash");
        watch.start();
        // fakeAdapter.registerWithAdapterManager();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(provider));
        Provider dnp =
                new ProviderImpl(provider.getProviderType(), provider.getProviderId(), "newEndpoint", "newName", "com");
        DoNothingAdapter dna = new DoNothingAdapter(dnp);
        adapterManager.registerAdapter(dna);
        watch.stop();
        LOG.info("tested ProviderIdClash --> " + watch);
    }

    @Test
    public void testNewProviderId()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing NewProviderId");
        watch.start();
        // fakeAdapter.registerWithAdapterManager();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(provider));
        Provider dnp = new ProviderImpl(provider.getProviderType(), "newProviderId", "newEndpoint", "newName", "com");
        DoNothingAdapter dna = new DoNothingAdapter(dnp);
        adapterManager.registerAdapter(dna);
        assertTrue("provider is not registered", adapterManager.getProviders().contains(dnp));
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested NewProviderId --> " + watch);
    }

    @Test
    public void testNewProviderType()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing NewProviderType");
        watch.start();
        // fakeAdapter.registerWithAdapterManager();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(fakeAdapter.getProvider()));
        Provider dnp = new ProviderImpl("newProviderType", provider.getProviderId(), "newEndpoint", "newName", "com");
        DoNothingAdapter dna = new DoNothingAdapter(dnp);
        adapterManager.registerAdapter(dna);
        assertTrue("provider is not registered", adapterManager.getProviders().contains(dnp));
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested NewProviderType --> " + watch);
    }

    // adapter (de)registration


    @Test
    public void testAdapterRegistration() throws NoSuchProviderException, DuplicateAdapterException {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterRegistration");
        watch.start();
        // fakeAdapter.registerWithAdapterManager();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(provider));
        // adapterManager.deregisterAdapter(fakeAdapter);
        watch.stop();
        LOG.info("tested AdapterRegistration --> " + watch);
    }

    @Test
    public void testAdapterDeregistration()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterDeregistration");
        watch.start();
        Provider testProvider = new FakeProviderImpl("test", "test", "test", "test", "test", "com");
        DoNothingAdapter dna = new DoNothingAdapter(testProvider);

        dna.setAdapterManager(adapterManager, null);
        adapterManager.registerAdapter(dna);
        dna.onLoad();

        assertTrue("provider is not registered", adapterManager.getProviders().contains(testProvider));
        // now deregister

        dna.onUnload();
        assertFalse("provider is still registered", adapterManager.getProviders().contains(testProvider));
        watch.stop();
        LOG.info("tested AdapterDeregistration --> " + watch);
    }

    @Test
    public void testPatternRegistrationWithAdapter() throws NoSuchProviderException, DuplicateAdapterException {
        StopWatch watch = new StopWatch();
        LOG.info("testing PatternRegistration");
        watch.start();
        // patterns can only be registered for a registered Provider/Adapter
        // fakeAdapter.registerWithAdapterManager();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(provider));
        // register Pattern
        // pattern should be automatically registered by registering the adapter
        // check if tapestryManager has received it
        for (PatternDefinition pd : tapestryManager.getPatterns(provider)) {
            LOG.info("FOUND pattern: " + pd.getId());
        }
        Collection<PatternDefinition> patterns = fakeAdapter.getAllPatterns();
        List<String> ids = new ArrayList<String>();
        for (PatternDefinition patternDefinition : patterns) {
            ids.add(patternDefinition.getId());
            LOG.info("FOUND pattern: " + patternDefinition.getId());
        }

        for (PatternDefinition pd : tapestryManager.getPatterns(provider)) {
            if (!pd.getId().equals("Providers")) {
                LOG.info("tapestryManager pattern: " + pd.getId());
                assertTrue("tapestryManager should have received the pattern: " + pd.getId(), ids.contains(pd.getId()));
            } else {
                LOG.info("Providers pattern added by default now");
            }
        }

        watch.stop();
        LOG.info("tested PatternRegistration --> " + watch);
    }

    @Test
    public void testItemTypeRegistrationWithAdapter() throws NoSuchProviderException, DuplicateAdapterException {
        StopWatch watch = new StopWatch();
        LOG.info("testing ItemRegistration");
        watch.start();
        // itemType can only be registered for a registered Provider/Adapter
        // fakeAdapter.registerWithAdapterManager();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(provider));
        // register Pattern
        // itemType should be automatically registered by registering the adapter
        // check if tapestryManager has received it
        assertTrue("tapestryManager should have received the itemType",
                itemTypeManager.getItemTypes(provider).contains(fakeAdapter.getItemType(OsInstanceType.TYPE_LOCAL_ID)));
        assertTrue("adapterManager should prefix Id with providerType",
                fakeAdapter.getItemType(OsInstanceType.TYPE_LOCAL_ID).getId().startsWith(provider.getProviderType()));
        // now deregister
        // fakeAdapter.deregisterWithAdapterManager();
        watch.stop();
        LOG.info("tested PatternRegistration --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdapterRegNullAdapter()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterRegNullAdapter");
        watch.start();
        adapterManager.registerAdapter(null);
        watch.stop();
        LOG.info("tested AdapterRegNullAdapter --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdapterRegNullProvider()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterRegNullProvider");
        watch.start();
        adapterManager.registerAdapter(new DoNothingAdapter(null));
        watch.stop();
        LOG.info("tested AdapterRegNullProvider --> " + watch);
    }

    @Test(expected = DuplicateAdapterException.class)
    public void testAdapterRegTwice()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing AdapterRegTwice");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        adapterManager.registerAdapter(new DoNothingAdapter(prov));
        watch.stop();
        LOG.info("tested AdapterRegTwice --> " + watch);
    }

    // user connection

    @Test
    public void testUserConnected() throws NoSuchProviderException, DuplicateAdapterException, NoSuchSessionException,
            SessionAlreadyExistsException, UserAlreadyConnectedException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing UserConnected");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        Session session = createSession();
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        adapterManager.userConnected(session, dna.getProvider(), null);
        assertTrue("both sessions should be the same", session.equals(dna.getSession()));
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested UserConnected --> " + watch);
    }

    @Test
    public void testUserDisconnected() throws NoSuchProviderException, DuplicateAdapterException,
            NoSuchSessionException, SessionAlreadyExistsException, UserAlreadyConnectedException, NoSuchUserException,
            DuplicateItemTypeException, NullItemTypeIdException, DuplicatePatternException, NullPatternIdException,
            UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing UserDisconnected");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        Session session = new SessionImpl("id1", sessionManager.getInterval());
        aggregationManager.createSession(session);
        stitcher.createSession(session);
        adapterManager.userConnected(session, dna.getProvider(), null);
        assertTrue("both sessions should be the same", session.equals(dna.getSession()));
        adapterManager.userDisconnected(session, dna.getProvider(), null);
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested UserDisconnected --> " + watch);
    }

    // patterns

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterPatternUnknownProvider() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternUnknownProvider");
        watch.start();
        Provider prov = createProvider();
        createUnregisteredDoNothingAdapter(prov);
        adapterManager.addPatternDefinition(prov, new PatternDefinition());
        watch.stop();
        LOG.info("tested RegisterPatternUnknownProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterPatternNullProvider() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternNullProvider");
        watch.start();
        adapterManager.addPatternDefinition(null, new PatternDefinition());
        watch.stop();
        LOG.info("tested RegisterPatternNullProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterPatternNullPattern() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternNullPattern");
        watch.start();
        Provider prov = createProvider();
        adapterManager.addPatternDefinition(prov, null);
        watch.stop();
        LOG.info("tested RegisterPatternNullPattern --> " + watch);
    }

    @Test(expected = NullPatternIdException.class)
    public void testRegisterPatternNullId()
            throws NoSuchProviderException, DuplicateAdapterException, DuplicatePatternException,
            NullPatternIdException, UnsupportedOperationException, DuplicateItemTypeException, NullItemTypeIdException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternNullId");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        adapterManager.addPatternDefinition(prov, new PatternDefinition());
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested RegisterPatternNullId --> " + watch);
    }

    @Test
    public void testDereisteringAllPatterns() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException,
            DuplicateItemTypeException, NullItemTypeIdException {
        StopWatch watch = new StopWatch();
        LOG.info("testing DereisteringAllPatterns");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        PatternDefinition pat1 = new PatternDefinition("pat1");
        PatternDefinition pat2 = new PatternDefinition("pat2");
        HashSet<PatternDefinition> patternSet = new HashSet<PatternDefinition>();
        patternSet.add(pat1);
        patternSet.add(pat2);
        adapterManager.addPatternDefinitions(prov, patternSet);
        assertTrue("tapestryManager should have received pat1", tapestryManager.getPatterns(prov).contains(pat1));
        assertTrue("tapestryManager should have received pat2", tapestryManager.getPatterns(prov).contains(pat2));
        assertTrue("adapterManager should profix Id with providerType",
                pat1.getId().startsWith(prov.getProviderType()));
        // clean up
        adapterManager.removePatternDefinitions(prov);
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested DereisteringAllPatterns --> " + watch);
    }


    @Test
    public void testItemTypeDeRegistrationWithAdapter() throws NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, NoSuchItemTypeException, DuplicateAdapterException, DuplicatePatternException,
            NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing ItemTypeDeRegistrationWithAdapter");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        ItemType fakeType = new FakeType();
        adapterManager.addItemType(prov, fakeType);
        adapterManager.removeAllItemTypes(prov);
        watch.stop();
        LOG.info("tested ItemTypeDeRegistrationWithAdapter --> " + watch);
    }

    @Test
    public void testRegisterTwoPatterns() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException,
            DuplicateItemTypeException, NullItemTypeIdException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterTwoPatterns");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        PatternDefinition pat1 = new PatternDefinition("pat1");
        PatternDefinition pat2 = new PatternDefinition("pat2");
        HashSet<PatternDefinition> patternSet = new HashSet<PatternDefinition>();
        patternSet.add(pat1);
        patternSet.add(pat2);

        adapterManager.addPatternDefinitions(prov, patternSet);
        assertTrue("tapestryManager should have received pat1", tapestryManager.getPatterns(prov).contains(pat1));
        assertTrue("tapestryManager should have received pat2", tapestryManager.getPatterns(prov).contains(pat2));
        assertTrue("adapterManager should profix Id with providerType",
                pat1.getId().startsWith(prov.getProviderType()));
        // clean up
        adapterManager.removePatternDefinitions(prov);
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested RegisterTwoPatterns --> " + watch);
    }


    @Test
    public void testRegisterDeregisterTwoPatterns() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException,
            DuplicateItemTypeException, NullItemTypeIdException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterDeregisterTwoPatterns");
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        PatternDefinition pat1 = new PatternDefinition("pat1");
        PatternDefinition pat2 = new PatternDefinition("pat2");
        HashSet<PatternDefinition> patternSet = new HashSet<PatternDefinition>();
        patternSet.add(pat1);
        patternSet.add(pat2);
        adapterManager.addPatternDefinitions(prov, patternSet);
        assertTrue("tapestryManager should have received pat1", tapestryManager.getPatterns(prov).contains(pat1));
        assertTrue("tapestryManager should have received pat2", tapestryManager.getPatterns(prov).contains(pat2));
        // clean up
        adapterManager.removePatternDefinitions(prov);

        Collection<PatternDefinition> patterns = tapestryManager.getPatterns(prov);
        assertEquals(1, patterns.size());
        adapterManager.deregisterAdapter(dna, new HashSet<>(0));
        watch.stop();
        LOG.info("tested RegisterDeregisterTwoPatterns --> " + watch);
    }

    @Test(expected = NoSuchProviderException.class)
    public void testRegisterPatternsUnknownProvider() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternsUnknownProvider");
        watch.start();
        Provider prov = createProvider();
        createUnregisteredDoNothingAdapter(prov);
        PatternDefinition pat1 = new PatternDefinition("pat1");
        PatternDefinition pat2 = new PatternDefinition("pat2");
        HashSet<PatternDefinition> patternSet = new HashSet<PatternDefinition>();
        patternSet.add(pat1);
        patternSet.add(pat2);
        adapterManager.addPatternDefinitions(prov, patternSet);
        watch.stop();
        LOG.info("tested RegisterPatternsUnknownProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterPatternsNullProvider() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternsNullProvider");
        watch.start();
        HashSet<PatternDefinition> patternSet = new HashSet<PatternDefinition>();
        adapterManager.addPatternDefinitions(null, patternSet);
        watch.stop();
        LOG.info("tested RegisterPatternsNullProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterPatternsNullPatterns() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterPatternsNullPatterns");
        watch.start();
        Provider prov = new ProviderImpl("os", "testfake", "http://whatever", "Testfake", "com");
        adapterManager.addPatternDefinitions(prov, null);
        watch.stop();
        LOG.info("tested RegisterPatternsNullPatterns --> " + watch);
    }

    @Test(expected = NullPatternIdException.class)
    public void testRegisterTwoPatternsOneNullId() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicatePatternException, NullPatternIdException, NoSuchPatternException, UnsupportedOperationException,
            DuplicateItemTypeException, NullItemTypeIdException {
        LOG.info("testing RegisterTwoPatternsOneNullId");
        StopWatch watch = new StopWatch();
        watch.start();
        Provider prov = createProvider();
        DoNothingAdapter dna = createUnregisteredDoNothingAdapter(prov);
        adapterManager.registerAdapter(dna);
        PatternDefinition pat1 = new PatternDefinition("pat1");
        PatternDefinition pat2 = new PatternDefinition();
        HashSet<PatternDefinition> patternSet = new HashSet<PatternDefinition>();
        patternSet.add(pat1);
        patternSet.add(pat2);
        adapterManager.addPatternDefinitions(prov, patternSet);
        watch.stop();
        LOG.info("tested RegisterTwoPatternsOneNullId --> " + watch);
    }

    private DoNothingAdapter createUnregisteredDoNothingAdapter(final Provider prov) {
        Collection<Provider> provs = adapterManager.getProviders();
        DoNothingAdapter dna = new DoNothingAdapter(prov);
        if (provs.contains(prov)) {
            try {
                adapterManager.deregisterAdapter(dna, new HashSet<>(0));
            } catch (NoSuchProviderException nspe) {
            }
        }
        return dna;
    }

    // ItemTypes

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterItemTypeUnknownProvider() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterItemTypeUnknownProvider");
        watch.start();
        Provider prov = createProvider();
        createUnregisteredDoNothingAdapter(prov);
        adapterManager.addItemType(prov, new FakeType());
        watch.stop();
        LOG.info("tested RegisterItemTypeUnknownProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterItemTypeNullProvider() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterItemTypeNullProvider");
        watch.start();
        adapterManager.addItemType(null, new FakeType());
        watch.stop();
        LOG.info("tested RegisterItemTypeNullProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterItemTypeNullItemType() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterItemTypeNullItemType");
        watch.start();
        Provider prov = createProvider();
        createUnregisteredDoNothingAdapter(prov);
        adapterManager.addItemType(prov, null);
        watch.stop();
        LOG.info("tested RegisterItemTypeNullItemType --> " + watch);
    }

    @Test(expected = NullItemTypeIdException.class)
    public void testRegisterItemTypeNullId() throws NoSuchProviderException, DuplicateAdapterException,
            DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException, DuplicatePatternException,
            NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing RegisterItemTypeNullId");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        ItemType fakeType = new FakeType();
        fakeType.setLocalId(null);
        adapterManager.addItemType(prov, fakeType);
        watch.stop();
        LOG.info("tested RegisterItemTypeNullId --> " + watch);
    }

    // @Test(expected = NoSuchProviderException.class)
    // public void testRegisterDeregisterTwoItemTypes() throws NoSuchProviderException,
    // DuplicateAdapterException,
    // DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException,
    // DuplicatePatternException,
    // NullPatternIdException, UnsupportedOperationException {
    // StopWatch watch = new StopWatch();
    // LOG.info("testing RegisterTwoItemTypes");
    // watch.start();
    // Provider prov = createProvider();
    // adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
    // ItemType fakeType1 = new FakeType();
    // fakeType1.setLocalId("ft1");
    // ItemType fakeType2 = new FakeType();
    // fakeType2.setLocalId("ft2");
    // HashSet<ItemType> types = new HashSet<ItemType>();
    // types.add(fakeType1);
    // types.add(fakeType2);
    // adapterManager.registerItemTypes(prov, types);
    // assertTrue("itemTypeManager should have received fakeType1",
    // itemTypeManager.getItemTypes(prov).contains(fakeType1));
    // assertTrue("itemTypeManager should have received fakeType2",
    // itemTypeManager.getItemTypes(prov).contains(fakeType2));
    // // checkIds
    // assertTrue("adapterManager should prefix Id with providerType",
    // fakeType1.getId().startsWith(prov.getProviderType()));
    // assertTrue("adapterManager should prefix Id with providerType",
    // fakeType2.getId().startsWith(prov.getProviderType()));
    // // clean up
    // adapterManager.deregisterItemTypes(prov);
    // // should throw an exception
    // itemTypeManager.getItemTypes(prov);
    // watch.stop();
    // LOG.info("tested RegisterTwoItemTypes --> " + watch);
    // }

    // @Test(expected = IllegalArgumentException.class)
    // public void testRegisterItemTypesUnknownProvider() throws NoSuchProviderException,
    // DuplicateAdapterException,
    // DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException {
    // StopWatch watch = new StopWatch();
    // LOG.info("testing RegisterItemTypesUnknownProvider");
    // watch.start();
    // Provider prov = createProvider();
    // createUnregisteredDoNothingAdapter(prov);
    // ItemType fakeType1 = new FakeType();
    // fakeType1.setLocalId("ft1");
    // ItemType fakeType2 = new FakeType();
    // fakeType2.setLocalId("ft2");
    // HashSet<ItemType> types = new HashSet<ItemType>();
    // types.add(fakeType1);
    // types.add(fakeType2);
    // adapterManager.registerItemTypes(prov, types);
    // watch.stop();
    // LOG.info("tested RegisterItemTypesUnknownProvider --> " + watch);
    // }

    // @Test(expected = IllegalArgumentException.class)
    // public void testRegisterItemTypesNullProvider() throws NoSuchProviderException,
    // DuplicateAdapterException,
    // DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException {
    // StopWatch watch = new StopWatch();
    // LOG.info("testing RegisterItemTypesNullProvider");
    // watch.start();
    // ItemType fakeType1 = new FakeType();
    // fakeType1.setLocalId("ft1");
    // ItemType fakeType2 = new FakeType();
    // fakeType2.setLocalId("ft2");
    // HashSet<ItemType> types = new HashSet<ItemType>();
    // types.add(fakeType1);
    // types.add(fakeType2);
    // adapterManager.registerItemTypes(null, types);
    // watch.stop();
    // LOG.info("tested RegisterItemTypesNullProvider --> " + watch);
    // }

    // @Test(expected = IllegalArgumentException.class)
    // public void testRegisterItemTypesNullItemTypes() throws NoSuchProviderException,
    // DuplicateAdapterException,
    // DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException {
    // StopWatch watch = new StopWatch();
    // LOG.info("testing RegisterItemTypesNullItemTypes");
    // watch.start();
    // Provider prov = createProvider();
    // createUnregisteredDoNothingAdapter(prov);
    // adapterManager.registerItemTypes(prov, null);
    // watch.stop();
    // LOG.info("tested RegisterItemTypesNullItemTypes --> " + watch);
    // }

    // @Test(expected = NullItemTypeIdException.class)
    // public void testRegisterTwoItemTypesOneNullId() throws NoSuchProviderException,
    // DuplicateAdapterException,
    // DuplicateItemTypeException, NullItemTypeIdException, NoSuchItemTypeException,
    // DuplicatePatternException,
    // NullPatternIdException, UnsupportedOperationException {
    // StopWatch watch = new StopWatch();
    // LOG.info("testing RegisterTwoItemTypesOneNullId");
    // watch.start();
    // Provider prov = createProvider();
    // adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
    // ItemType fakeType1 = new FakeType();
    // fakeType1.setLocalId("ft1");
    // ItemType fakeType2 = new FakeType();
    // fakeType2.setLocalId(null);
    // HashSet<ItemType> types = new HashSet<ItemType>();
    // types.add(fakeType1);
    // types.add(fakeType2);
    // adapterManager.registerItemTypes(prov, types);
    // watch.stop();
    // LOG.info("tested RegisterTwoItemTypesOneNullId --> " + watch);
    // }

    private Provider createProvider() {
        return new ProviderImpl("os", "testfake", "http://whatever", "Testfake", "com");
    }

    @Test
    public void testGetItemLogicalId()
            throws NoSuchProviderException, DuplicateAdapterException, NoSuchSessionException,
            SessionAlreadyExistsException, LogicalIdAlreadyExistsException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetItemLogicalId");
        watch.start();
        Provider prov = createProvider();
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        Session session = createSession();
        aggregationManager.createSession(session);
        Aggregation agg = adapterManager.createGroundedAggregation(session, prov, type, null, true, "vms",
                "List of virtual machines - fake", 44);
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        String itemId = "a123";
        assertTrue("item LogicalId should combine agg logicalId with itemId ",
                LoomUtils.getItemLogicalId(agg, itemId).equals(agg.getLogicalId() + "/" + itemId));
        watch.stop();
        LOG.info("tested GetItemLogicalId --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetItemLogicalIdNullAggregation() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetItemLogicalIdNullAggregation");
        watch.start();
        LoomUtils.getItemLogicalId(null, "a123");
        watch.stop();
        LOG.info("tested GetItemLogicalIdNullAggregation --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetItemLogicalIdNullId() throws NoSuchProviderException, NoSuchSessionException,
            SessionAlreadyExistsException, LogicalIdAlreadyExistsException {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetItemLogicalIdNullId");
        watch.start();
        Provider prov = createProvider();
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        Session session = createSession();
        aggregationManager.createSession(session);
        Aggregation agg = adapterManager.createGroundedAggregation(session, prov, type, null, true, "vms",
                "List of virtual machines - fake", 44);
        LoomUtils.getItemLogicalId(agg, null);
        watch.stop();
        LOG.info("tested GetItemLogicalIdNullId --> " + watch);
    }

    @Test
    public void testGetMergedLogicalIdFromItemType() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetMergedLogicalIdFromItemType");
        watch.start();
        Provider prov = createProvider();
        String itemTypeLocalId = "aaaa";
        String localId = "bbb";
        assertTrue("mergedLogicalid should combine provide rtype with ids",
                LoomUtils.getMergedLogicalIdFromItemType(prov, itemTypeLocalId, localId)
                        .equals(prov.getProviderType() + "/" + itemTypeLocalId + "s/" + localId));
        watch.stop();
        LOG.info("tested GetMergedLogicalIdFromItemType --> " + watch);
    }

    @Test
    public void testGetMergedLogicalIdFromItemTypeNullLocalId() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetMergedLogicalIdFromItemTypeNullLocalId");
        watch.start();
        Provider prov = createProvider();
        String itemTypeLocalId = "aaaa";
        assertTrue("mergedLogicalid should combine provide rtype with ids",
                LoomUtils.getMergedLogicalIdFromItemType(prov, itemTypeLocalId, null)
                        .equals(prov.getProviderType() + "/" + itemTypeLocalId + "s"));
        watch.stop();
        LOG.info("tested GetMergedLogicalIdFromItemTypeNullLocalId --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMergedLogicalIdFromItemTypeNullProvider() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetMergedLogicalIdFromItemTypeNullProvider");
        watch.start();
        String itemTypeLocalId = "aaaa";
        String localId = "bbb";
        LoomUtils.getMergedLogicalIdFromItemType(null, itemTypeLocalId, localId);
        watch.stop();
        LOG.info("tested GetMergedLogicalIdFromItemTypeNullProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMergedLogicalIdFromItemTypeNullItemTypeLocalId() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetMergedLogicalIdFromItemTypeNullItemTypeLocalId");
        watch.start();
        Provider prov = createProvider();
        String localId = "bbb";
        LoomUtils.getMergedLogicalIdFromItemType(prov, null, localId);
        watch.stop();
        LOG.info("tested GetMergedLogicalIdFromItemTypeNullItemTypeLocalId --> " + watch);
    }

    @Test
    public void testGetAggregationLogicalIdFromItemType() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetAggregationLogicalIdFromItemType");
        watch.start();
        Provider prov = createProvider();
        String itemTypeLocalId = "aaaa";
        String localId = "bbb";
        assertTrue("aggregationLogicalid should combine provider type & id with ids",
                LoomUtils.getAggregationLogicalIdFromItemType(prov, itemTypeLocalId, localId).equals(
                        prov.getProviderType() + "/" + prov.getProviderId() + "/" + itemTypeLocalId + "s/" + localId));
        watch.stop();
        LOG.info("tested GetAggregationLogicalIdFromItemType --> " + watch);
    }

    @Test
    public void testGetAggregationLogicalIdFromItemTypeNullLocalId() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetAggregationLogicalIdFromItemTypeNullLocalId");
        watch.start();
        Provider prov = createProvider();
        String itemTypeLocalId = "aaaa";
        assertTrue("aggregationLogicalid should combine provider type & id with ids",
                LoomUtils.getAggregationLogicalIdFromItemType(prov, itemTypeLocalId, null)
                        .equals(prov.getProviderType() + "/" + prov.getProviderId() + "/" + itemTypeLocalId + "s"));
        watch.stop();
        LOG.info("tested GetAggregationLogicalIdFromItemTypeNullLocalId --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAggregationLogicalIdFromItemTypeNullProvider() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetAggregationLogicalIdFromItemTypeNullProvider");
        watch.start();
        String itemTypeLocalId = "aaaa";
        String localId = "bbb";
        LoomUtils.getAggregationLogicalIdFromItemType(null, itemTypeLocalId, localId);
        watch.stop();
        LOG.info("tested GetAggregationLogicalIdFromItemTypeNullProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAggregationLogicalIdFromItemTypeNullItemTypeLocalId() {
        StopWatch watch = new StopWatch();
        LOG.info("testing GetAggregationLogicalIdFromItemTypeNullItemTypeLocalId");
        watch.start();
        Provider prov = createProvider();
        String localId = "bbb";
        LoomUtils.getAggregationLogicalIdFromItemType(prov, null, localId);
        watch.stop();
        LOG.info("tested GetAggregationLogicalIdFromItemTypeNullItemTypeLocalId --> " + watch);
    }

    // aggregation
    @Test
    public void testCreateOneAggregation()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateOneAggregation");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        Session session = createSession();
        aggregationManager.createSession(session);
        adapterManager.createGroundedAggregation(session, prov, type, null, true, "vms",
                "List of virtual machines - fake", 44);
        assertTrue("aggregationManager should know this aggregation",
                aggregationManager.getAggregation(session, "os/testfake/testfakes") != null);
        watch.stop();
        LOG.info("tested CreateOneAggregation --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOneAggregationNullProvider()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateOneAggregationNullProvider");
        watch.start();
        Provider prov = createProvider();
        Adapter adapter = createUnregisteredDoNothingAdapter(prov);
        // adapterManager.deregisterAdapter(adapter, new HashSet<Session>());

        adapterManager.registerAdapter(adapter);
        ItemType type = new FakeType();
        adapterManager.addItemType(prov, type);
        Session session = createSession();
        aggregationManager.createSession(session);
        adapterManager.createGroundedAggregation(session, null, type, null, true, "vms",
                "List of virtual machines - fake", 44);
        watch.stop();
        LOG.info("tested CreateOneAggregationNullProvider --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOneAggregationNullSession()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateOneAggregationNullSession");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        ItemType type = new FakeType();
        adapterManager.addItemType(prov, type);
        Session session = createSession();
        aggregationManager.createSession(session);
        adapterManager.createGroundedAggregation(null, prov, type, null, true, "vms", "List of virtual machines - fake",
                44);
        watch.stop();
        LOG.info("tested CreateOneAggregationNullSession --> " + watch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOneAggregationNullItemType()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateOneAggregationNullItemType");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        Session session = createSession();
        aggregationManager.createSession(session);
        adapterManager.createGroundedAggregation(session, prov, null, null, true, "vms",
                "List of virtual machines - fake", 44);
        watch.stop();
        LOG.info("tested CreateOneAggregationNullItemType --> " + watch);
    }

    @Test
    public void testCreateOneAggregationNullLocalIdFalseMapped()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateOneAggregationNullLocalIdFalseMapped");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        Session session = createSession();
        aggregationManager.createSession(session);
        adapterManager.createGroundedAggregation(session, prov, type, null, false, "vms",
                "List of virtual machines - fake", 44);
        watch.stop();
        LOG.info("tested CreateOneAggregationNullLocalIdFalseMapped --> " + watch);
    }

    @Test(expected = NoSuchSessionException.class)
    public void testCreateOneAggregationUnknownSession()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateOneAggregationUnknownSession");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        Session session = createSession();
        adapterManager.createGroundedAggregation(session, prov, type, null, true, "vms",
                "List of virtual machines - fake", 44);
        watch.stop();
        LOG.info("tested CreateOneAggregationUnknownSession --> " + watch);
    }

    @Test
    public void testCreateTwoAggregations()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateTwoAggregations");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        Session session = createSession();
        aggregationManager.createSession(session);
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        adapterManager.createGroundedAggregation(session, prov, type, "fake-sfx", true, "vms",
                "List of virtual machines - fake", 44);
        adapterManager.createGroundedAggregation(session, prov, type, "fake-sfx2", true, "vms",
                "List of virtual machines - fake", 44);
        assertTrue("both aggregations should be in", aggregationManager.listAggregations(session).size() == 2);
        watch.stop();
        LOG.info("tested CreateTwoAggregations --> " + watch);
    }

    @Test(expected = LogicalIdAlreadyExistsException.class)
    public void testCreateRepeatAggregations()
            throws NoSuchSessionException, SessionAlreadyExistsException, DuplicateAdapterException,
            LogicalIdAlreadyExistsException, NoSuchProviderException, DuplicateItemTypeException,
            NullItemTypeIdException, DuplicatePatternException, NullPatternIdException, UnsupportedOperationException {
        StopWatch watch = new StopWatch();
        LOG.info("testing CreateRepeatAggregations");
        watch.start();
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        Session session = createSession();
        aggregationManager.createSession(session);
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        adapterManager.createGroundedAggregation(session, prov, type, "fake-sfx", true, "vms",
                "List of virtual machines - fake", 44);
        adapterManager.createGroundedAggregation(session, prov, type, "fake-sfx", true, "vms",
                "List of virtual machines - fake", 44);
        assertTrue("both aggregations should be in", aggregationManager.listAggregations(session).size() == 2);
        watch.stop();
        LOG.info("tested CreateRepeatAggregations --> " + watch);
    }

    @Test
    public void testGetAggregationForItem() throws CheckedLoomException {
        StopWatch watch = new StopWatch();
        LOG.info("testGetAggregationForItem start");
        watch.start();
        assertNotNull("Aggregation manager instance not set", aggregationManager);
        Provider prov = createProvider();
        adapterManager.registerAdapter(createUnregisteredDoNothingAdapter(prov));
        Session session = createSession();
        aggregationManager.createSession(session);
        ItemType type = new FakeType();
        type.setId("fake-" + type.getLocalId());
        Aggregation aggregation = adapterManager.createGroundedAggregation(session, prov, type, null, true, "vms",
                "List of virtual machines - fake", 44);
        String logicalId = aggregation.getLogicalId() + "/i-7e9675e2-c5c0-49e3-8b17-2cfaf14307e8";
        Aggregation retrievedAggregation = adapterManager.getAggregationForItem(session, logicalId);
        assertNotNull("Get Aggregation for Item returned null result", retrievedAggregation);
        assertEquals("Returned aggregation had incorrect logicalId", aggregation.getLogicalId(),
                retrievedAggregation.getLogicalId());
        watch.stop();
        LOG.info("testGetAggregationForItem end --> " + watch);
    }

    private Session createSession() {
        aggregationManager.deleteAllSessions();
        return new SessionImpl("sessionOne", sessionManager.getInterval());
    }
}
