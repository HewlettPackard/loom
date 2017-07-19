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

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;

public class OsItemTypeTest {

    private static final Log LOG = LogFactory.getLog(OsItemTypeTest.class);
    private Provider provider =
            new ProviderImpl("providerType", "providerId", "authEndpoint", "providerName", "adapterPackage");

    @Before
    public void setUp() throws Exception {
        LOG.info("Setup test");
    }

    @After
    public void shutDown() throws Exception {
        LOG.info("shutDown test");
    }

    private String toJson(final Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        jsonRep = mapper.writeValueAsString(object);
        return jsonRep;
    }

    private void testItemTypeToJson(final String msg, final ItemType itemType) throws JsonProcessingException {
        StopWatch watch = new StopWatch();
        LOG.info("testing " + msg);
        watch.start();
        LOG.info("start");
        LOG.info("created JSON is\n" + toJson(itemType));
        watch.stop();
        LOG.info("tested " + msg + " --> " + watch);
    }

    @Test
    public void testInstanceTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("InstanceTypeToJson", new OsInstanceType(provider));
    }

    @Test
    public void testProjectTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("ProjectTypeToJson", new OsProjectType());
    }

    @Test
    public void testRegionTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("RegionTypeToJson", new OsRegionType());
    }

    @Test
    public void testVolumeTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("VolumeTypeToJson", new OsVolumeType(provider));
    }

    @Test
    public void testImageTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("ImageTypeToJson", new OsImageType(provider));
    }

    @Test
    public void testNetworkTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("NetworkTypeToJson", new OsNetworkType(provider));
    }

    @Test
    public void testSubnetTypeToJson() throws JsonProcessingException {
        testItemTypeToJson("SubnetTypeToJson", new OsSubnetType(provider));
    }

}
