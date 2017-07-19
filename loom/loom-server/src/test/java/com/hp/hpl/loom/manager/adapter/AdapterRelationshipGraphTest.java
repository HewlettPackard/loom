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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.adapter.os.DoNothingAdapter;
import com.hp.hpl.loom.adapter.os.fake.FakeAdapter;
import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.DuplicateAdapterException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.itemtype.ItemTypeManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.manager.tapestry.TapestryManager;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testAdapterManager.xml")
public class AdapterRelationshipGraphTest {
    private static final Log LOG = LogFactory.getLog(AdapterRelationshipGraphTest.class);

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
        Provider prov = new ProviderImpl("os", "testfake", "http://whatever", "Testfake",
                fakeAdapter.getClass().getPackage().getName());
        createUnregisteredDoNothingAdapter(prov);
        try {
            itemTypeManager.removeAllItemTypes(prov);
        } catch (NoSuchProviderException ex) {

        }
        LOG.info("shutDown test");
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

    @Test
    public void testAdapterGraph() throws NoSuchProviderException, DuplicateAdapterException {
        StopWatch watch = new StopWatch();
        LOG.info("testing testAdapterGraph");
        watch.start();
        assertTrue("provider is not registered", adapterManager.getProviders().contains(provider));

        Set<Provider> loggedInProviders = new HashSet<>();
        List<Provider> providers = adapterManager.getProviders();
        for (Provider provider : providers) {
            loggedInProviders.add(provider);
        }
        List<Class<? extends Item>> items = new ArrayList<>();
        Reflections reflections = new Reflections("com");

        Set<Class<? extends Item>> subTypes = reflections.getSubTypesOf(Item.class);

        for (Class<? extends Item> class1 : subTypes) {
            items.add(class1);
        }



        Map<Provider, Collection<ItemType>> providerTolocalIds = itemTypeManager.getProvidersToItemTypes();

        GenerateSchema drawer = new GenerateSchema();
        String data = drawer.process(stitcher, items, loggedInProviders);
        LOG.info("tested testAdapterGraph --> " + data);


        watch.stop();
        LOG.info("tested testAdapterGraph --> " + watch);
    }

    @Test
    public void testAdapterItemTypes() {
        Collection<ItemType> itemTypes = itemTypeManager.getItemTypes();
        for (ItemType itemType : itemTypes) {
            System.out.println(itemType.getId());
        }

    }

}
