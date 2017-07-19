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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;
import com.hp.hpl.loom.relationships.RelationshipCalculator;
import com.hp.hpl.loom.relationships.RelationshipsModel;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testTapestryManager.xml")
public class OperationContextImplTest {

    private static final Log LOG = LogFactory.getLog(OperationContextImplTest.class);

    @Autowired
    private AdapterManager adapterManager;

    @Autowired
    private AggregationManager aggregationManager;

    @Autowired
    private RelationshipCalculator relationshipCalculator;

    @Autowired
    private Tacker stitcher;

    @Autowired
    private SessionManager sessionManager;

    Session session;

    OperationContext context;



    @Test
    public void testGetRelModel() throws Exception {
        HttpServletResponse response = new MockHttpServletResponse();
        session = sessionManager.createSession("assessionId", response);
        context = new OperationContextImpl(adapterManager, aggregationManager, relationshipCalculator, stitcher,
                session, new ItemType());
        RelationshipsModel model = context.getRelationshipsModel(new HashMap<>(0));
        assertNotNull(model);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRelModelNullSession() throws Exception {
        context = new OperationContextImpl(adapterManager, aggregationManager, relationshipCalculator, stitcher,
                session, new ItemType());
        Map<OperationErrorCode, String> errors = new HashMap<>(0);
        context.getRelationshipsModel(errors);
    }

    @Test
    public void testGetRelModelInvalidSession() throws Exception {

        session = new SessionImpl("wrongsession", sessionManager.getInterval());
        context = new OperationContextImpl(adapterManager, aggregationManager, relationshipCalculator, stitcher,
                session, new ItemType());
        Map<OperationErrorCode, String> errors = new HashMap<>(0);
        context.getRelationshipsModel(errors);
        assertEquals(1, errors.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullItem() throws Exception {
        HttpServletResponse response = new MockHttpServletResponse();
        session = sessionManager.createSession("assessionId", response);
        context = new OperationContextImpl(adapterManager, aggregationManager, relationshipCalculator, stitcher,
                session, new ItemType());
        Map<OperationErrorCode, String> errors = new HashMap<>(0);
        context.getItemWithLogicalId("fake", errors);
    }

    @Test
    public void testGetNullSlashItem() throws Exception {
        session = new SessionImpl("wrongsession", sessionManager.getInterval());
        context = new OperationContextImpl(adapterManager, aggregationManager, relationshipCalculator, stitcher,
                session, new ItemType());
        Map<OperationErrorCode, String> errors = new HashMap<>(0);
        Item item = context.getItemWithLogicalId("agg1/fake", errors);

        assertEquals(null, item);

        // No such session and no such fibre
        assertEquals(2, errors.size());
    }



}
