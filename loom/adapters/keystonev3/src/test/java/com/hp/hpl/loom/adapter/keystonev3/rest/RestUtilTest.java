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
package com.hp.hpl.loom.adapter.keystonev3.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.adapter.keystonev3.KeystoneUtils;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonAuth;
import com.hp.hpl.loom.model.Credentials;

public class RestUtilTest {
    private static final Log LOG = LogFactory.getLog(RestUtilTest.class);

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
    }

    @Test
    public void testUnscopedAuth() throws JsonProcessingException {
        Credentials creds = new Credentials("aloha", "hawai");
        JsonAuth auth = KeystoneUtils.getUnscopedAuth(creds);
        LOG.info(toJson(auth));
    }

    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }
}
