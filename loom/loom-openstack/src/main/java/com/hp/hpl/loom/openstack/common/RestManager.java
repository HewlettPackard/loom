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
package com.hp.hpl.loom.openstack.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Class to handle to create the rest templates.
 */
public final class RestManager {
    private static final Log LOG = LogFactory.getLog(RestManager.class);

    private Map<String, RestTemplate> tempMap = new HashMap<>();
    private String proxyHost = null;
    private int proxyPort = -1;

    /**
     * Default constructor.
     */
    public RestManager() {
        setProxyValues();
    }

    private void setProxyValues() {
        // for now, only read from env - could get them from optional config first
        String httpsProxy = System.getenv("https_proxy");
        if (httpsProxy != null) {
            try {
                URL proxyUrl = new URL(httpsProxy);
                proxyHost = proxyUrl.getHost();
                proxyPort = proxyUrl.getPort();
            } catch (MalformedURLException mue) {
                LOG.error("env variable https_proxy is malformed: " + httpsProxy);
            }
        }
    }

    /**
     * Set the proxy values.
     *
     * @param pHost the proxy host
     * @param pPort the proxy port
     */
    public void setProxyValues(final String pHost, final int pPort) {
        proxyHost = pHost;
        proxyPort = pPort;
    }

    /**
     * Get a RestTemplate for a given service name.
     *
     * @param service the service name to lookup a template for.
     * @return the rest template.
     */
    public RestTemplate getRestTemplate(final String service) {
        RestTemplate temp = tempMap.get(service);
        if (temp == null) {
            temp = RestUtils.createRestTemplate(proxyHost, proxyPort);
            tempMap.put(service, temp);
        }
        return temp;
    }
}
