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

import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ProviderImpl;

public class OperationManagerImplTest {

    private static final Log LOG = LogFactory.getLog(OperationManagerImplTest.class);

    OperationManager opMgr;
    int startValues;

    QueryOperation first, newOp;
    QuadFunctionMeta firstMeta, newOpMeta;

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
        opMgr = new OperationManagerImpl();
        startValues = opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size();

        first = new QueryOperation((inputs, params, errors, itemType) -> {
            Iterator<List<Fibre>> selfIt = inputs.values().iterator();
            List<Fibre> self = selfIt.next();
            if (self == null) {
                return new PipeLink<Fibre>("FAKEOP", self);
            } else {
                return new PipeLink<Fibre>("FAKEOP", new ArrayList<>());
            }

        }, false);

        newOp = new QueryOperation((inputs, params, errors, itemType) -> {
            params.get("groupingProperty");
            Iterator<List<Fibre>> inputIt = inputs.values().iterator();
            List<Fibre> input = inputIt.next();
            return new PipeLink<Fibre>(input.parallelStream().collect(groupingBy((final Fibre loomEntity) -> "NOP")));
        }, false);

        firstMeta = new QuadFunctionMeta("First", first, false, false);
        newOpMeta = new QuadFunctionMeta("NewOp", newOp, false, false);

    }

    @Test
    public void testRegisterOperation() throws Exception {

        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrRegistration");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("FAKEOP", firstMeta, OperationManagerImpl.LOOM_UUID);

        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        watch.stop();
        LOG.info("tested OpMgrRegistration --> " + watch);
    }

    @Test
    public void testWrongUUIDRegister() {
        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrRegistration");
        watch.start();

        List<String> opList = opMgr.listOperations(UUID.randomUUID());
        assertTrue(opList.size() == 0);

        opMgr.registerOperation("FAKEOP", firstMeta, UUID.randomUUID());
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("", firstMeta, OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("FAKEOP", null, OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        watch.stop();
        LOG.info("tested OpMgrRegistration --> " + watch);

    }

    @Test
    public void testWrongProviderRegister() {
        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrRegistration");
        watch.start();

        opMgr.registerOperation4Provider("FAKEOP", firstMeta, null);
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation4Provider("", firstMeta, new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation4Provider("FAKEOP", null, new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        watch.stop();
        LOG.info("tested OpMgrRegistration --> " + watch);
    }


    @Test
    public void testUpdateOperation() throws Exception {

        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrUpdate");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("FAKEOP", firstMeta, OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.updateOperation("FAKEOP", newOpMeta);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        assertEquals(newOp.getFunction(), opMgr.getFunction("FAKEOP"));
        watch.stop();
        LOG.info("tested OpMgrUpdate --> " + watch);
    }


    @Test
    public void testWrongUpdateOperation() throws Exception {

        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrUpdate");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("FAKEOP", firstMeta, OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.updateOperation("", newOpMeta);
        assertEquals(first.getFunction(), opMgr.getFunction("FAKEOP"));

        opMgr.updateOperation("FAKEOP", null);
        assertEquals(first.getFunction(), opMgr.getFunction("FAKEOP"));


        watch.stop();
        LOG.info("tested OpMgrUpdate --> " + watch);
    }

    @Test
    public void testDeleteOperation() throws Exception {

        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrDelete");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("FAKEOP", firstMeta, OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.deleteOperation("FaKeOP", OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.deleteOperation("FAKEOP", OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        watch.stop();
        LOG.info("tested OpMgrDelete --> " + watch);

    }

    @Test
    public void testWrongDeleteOperation() throws Exception {

        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrDelete");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation("FAKEOP", firstMeta, OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.deleteOperation("FaKeOP", UUID.randomUUID());
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.deleteOperation("", OperationManagerImpl.LOOM_UUID);
        assertEquals(startValues + 1, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        watch.stop();
        LOG.info("tested OpMgrDelete --> " + watch);

    }

    @Test
    public void deleteAllOps4Provider() {
        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrDelete");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation4Provider("FAKEOP", firstMeta, new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(DefaultOperations.values().length + 1,
                opMgr.listOperations(new ProviderImpl("test", "test", "none", "test", "com")).size());

        opMgr.registerOperation4Provider("FAKEOP2", newOpMeta, new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(DefaultOperations.values().length + 2,
                opMgr.listOperations(new ProviderImpl("test", "test", "none", "test", "com")).size());

        opMgr.deleteOperationsForProvider(new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(DefaultOperations.values().length,
                opMgr.listOperations(new ProviderImpl("test", "test", "none", "test", "com")).size());

    }


    @Test
    public void deleteAllOps4ProviderWrongly() {
        StopWatch watch = new StopWatch();
        LOG.info("testing OpMgrDelete");
        watch.start();

        assertEquals(startValues, opMgr.listOperations(OperationManagerImpl.LOOM_UUID).size());

        opMgr.registerOperation4Provider("", firstMeta, new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(DefaultOperations.values().length,
                opMgr.listOperations(new ProviderImpl("test", "test", "none", "test", "com")).size());

        opMgr.registerOperation4Provider("FAKEOP2", null, new ProviderImpl("test", "test", "none", "test", "com"));
        assertEquals(DefaultOperations.values().length,
                opMgr.listOperations(new ProviderImpl("test", "test", "none", "test", "com")).size());


    }
}
