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
package com.hp.hpl.loom.api;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.api.exceptions.BadRequestException;
import com.hp.hpl.loom.exceptions.AccessExpiredException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchUserException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterLoader;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.AdapterStatus;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Operation;
import com.hp.hpl.loom.model.OperationList;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderList;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.Status;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;
import com.hp.hpl.loom.tapestry.TapestryDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContextMock.xml")
public class LoomServiceTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private AdapterManager adapterManager;

    @Autowired
    private AdapterLoader adapterLoader;

    @Ignore
    @Test
    public void testStatus() {
        LoomServiceImpl loomService = (LoomServiceImpl) ctx.getBean(LoomService.class);
        MockServletContext servletContext = new MockServletContext();
        loomService.setServletContext(servletContext);
        Status status = loomService.getStatus(null, null);

        Assert.assertEquals("Commons IO", status.getBuild());
        Assert.assertEquals("2.4", status.getVersion());

        List<AdapterStatus> adapters = status.getAdapters();
        Assert.assertNotNull(adapters.size());
        Assert.assertEquals(3, adapters.size());
    }

    @Test
    public void testOperations() {

        LoomServiceImpl loomService = (LoomServiceImpl) ctx.getBean(LoomService.class);

        OperationList opList = loomService.getOperations(null, "os", null, null, null);
        Assert.assertEquals("Expecting 10 operations", 10, opList.getOperations().size());

        opList = loomService.getOperations("os-instance", null, null, null, null);
        Assert.assertEquals("Expecting 10 operations", 10, opList.getOperations().size());
        for (Operation op : opList.getOperations()) {
            Assert.assertEquals("Should only be one item type: " + op.getId() + " " + op.getName(), 1,
                    op.getItemTypes().size());
        }

        opList = loomService.getOperations(null, "os", null, null, null);
        Assert.assertEquals("Expecting 10 operations", 10, opList.getOperations().size());
        for (Operation op : opList.getOperations()) {
            Assert.assertTrue("Should only be one item type", op.getItemTypes().size() > 0);
        }

        opList = loomService.getOperations(null, null, "os", null, null);
        Assert.assertEquals("Expecting 1 operations", 1, opList.getOperations().size());
        for (Operation op : opList.getOperations()) {
            Assert.assertTrue("Should only be one item type", op.getItemTypes().size() > 0);
        }

        opList = loomService.getOperations("os-project", null, null, null, null);
        Assert.assertEquals("Expecting 9 operations", 9, opList.getOperations().size());
        for (Operation op : opList.getOperations()) {
            Assert.assertEquals("Should only be one item type: " + op.getId() + " " + op.getName(), 1,
                    op.getItemTypes().size());
        }
    }

    @Test
    public void testOperationsParameters() throws InterruptedException {
        LoomServiceImpl loomService = (LoomServiceImpl) ctx.getBean(LoomService.class);

        try {
            Thread.sleep(200);
            loomService.getOperations("os-project", "os", null, null, null);
            Assert.fail("Should have failed as itemType and provider id are set");
        } catch (BadRequestException ex) {
        }

        try {
            Thread.sleep(200);
            loomService.getOperations("os-project", "", "os", null, null);
            Assert.fail("Should have failed as itemType and declaredBy are set");
        } catch (BadRequestException ex) {
        }

        try {
            Thread.sleep(200);
            loomService.getOperations(null, "os", "os", null, null);
            Assert.fail("Should have failed as itemType and provider id are set");
        } catch (BadRequestException ex) {
        }

        try {
            Thread.sleep(200);
            loomService.getOperations("os-project", "os", "os", null, null);
            Assert.fail("Should have failed as itemType, provider id and declaredBy are set");
        } catch (BadRequestException ex) {
        }
    }

    @Test
    public void testDeregisterAdapter() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            NoSuchAggregationException, NoSuchQueryDefinitionException, LogicalIdAlreadyExistsException,
            OperationException, NoSuchThreadDefinitionException, InvalidQueryInputException,
            InvalidQueryParametersException, NoSuchItemTypeException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload, AccessExpiredException, JsonProcessingException, NoSuchProviderException,
            SessionAlreadyExistsException, UserAlreadyConnectedException, NoSuchUserException {
        LoomServiceImpl loomService = (LoomServiceImpl) ctx.getBean(LoomService.class);

        ProviderList list = loomService.getProviders(null, null);
        for (Provider prov : list.getProviders()) {
            System.out.println(prov.getProviderId() + " " + prov.getProviderName() + " " + prov.getProviderType());
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        String threadId = "";
        String sessionId = null;
        Credentials credentials = new Credentials("test", "test");
        PatternDefinitionList patternDefinitionList =
                loomService.logProvider("os", "private", "login", credentials, sessionId, response);
        List<PatternDefinition> patterns = patternDefinitionList.getPatterns();

        sessionId = response.getCookies()[0].getValue();
        TapestryDefinition tapestryDefinition = new TapestryDefinition(null, patterns.get(0).getThreads());

        tapestryDefinition = loomService.createTapestryDefinition(tapestryDefinition, sessionId, response);
        threadId = tapestryDefinition.getThreads().get(0).getId();
        loomService.getQueryResult(tapestryDefinition.getId(), threadId, sessionId, response);
        Provider prov = adapterManager.getProvider("os", "private");
        Adapter adapter = adapterLoader.getAdapter(prov);
        Session session = adapter.getSessions().iterator().next();
        synchronized (session) {
            adapterManager.deregisterAdapter(adapter, adapter.getSessions());
        }
        try {
            Thread.sleep(1000);
            loomService.getQueryResult(tapestryDefinition.getId(), threadId, sessionId, response);
            Assert.fail("Expecting an exception");
        } catch (com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload ex) {

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
