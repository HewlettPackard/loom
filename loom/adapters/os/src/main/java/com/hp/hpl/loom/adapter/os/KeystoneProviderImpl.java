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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.hp.hpl.loom.manager.adapter.AdapterConfig;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.openstack.OpenstackApi;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;

public class KeystoneProviderImpl extends ProviderImpl implements Runnable {
    private static final Log LOG = LogFactory.getLog(KeystoneProviderImpl.class);
    public static final String PROXY_HOST = "proxy-host";
    public static final String PROXY_PORT = "proxy-port";

    private static final int THREAD_SLEEP = 60000;

    private String proxyHost = null;
    private int proxyPort = -1;

    private OpenstackApi openstackService;

    public KeystoneProviderImpl(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final AdapterConfig adapterConfig) {
        super(providerType, providerId, authEndpoint, providerName,
                adapterConfig.getAdapterClass().substring(0, adapterConfig.getAdapterClass().lastIndexOf(".")));
        setProxyValues(adapterConfig);
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        openstackService = new OpenstackApi(authEndpoint, creds.getUsername());
        openstackService.setProxy(proxyHost, proxyPort);
        boolean result = openstackService.authenticate(creds.getPassword());
        creds.setContext(openstackService);
        if (result) {
            Thread newThread = new Thread(this);
            newThread.setName("TokenRevoking-" + this.getProviderTypeAndId());
            // newThread.start();
        }

        return result;
    }

    private void setProxyValues(final AdapterConfig adapterConfig) {
        // for now, only read from env - could get them from optional config first
        String httpsProxy = System.getenv("https_proxy");
        if (httpsProxy == null) {
            httpsProxy = adapterConfig.getPropertiesConfiguration().getString("proxy");
        }
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

    @Override
    @SuppressWarnings("checkstyle:emptyblock")
    public void run() {
        LOG.warn("****** KeystoneProviderImpl Thread going to sleep for " + THREAD_SLEEP + " secs");
        try {
            Thread.sleep(THREAD_SLEEP);
        } catch (InterruptedException ie) {
        }
        LOG.warn("****** KeystoneProviderImpl finished sleeping, revoking all tokens");
        try {
            openstackService.getKeystoneApi().getKeystoneProject().logout(authEndpoint);
        } catch (HttpClientErrorException ex) {
        }
        LOG.warn("****** all tokens have been revoked - KeystoneProviderImpl Thread is exiting...");
    }

}
