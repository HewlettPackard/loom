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
package com.hp.hpl.loom.adapter.keystonev3.updaters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AuthenticationFailureException;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.keystonev3.KeystoneCollector;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;

public abstract class KeystoneUpdater<T extends AdapterItem<A>, A extends CoreItemAttributes, R>
        extends AggregationUpdater<T, A, R> {

    private static final Log LOG = LogFactory.getLog(DomainsUpdater.class);

    protected KeystoneCollector kc;

    public KeystoneUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final KeystoneCollector kc) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, kc);
        this.kc = kc;
    }

    protected List<R> getJsonResources(final String uriSuffix) {
        RestTemplate rt = kc.getRestTemplateWithToken();
        String resourcesUri = kc.getKeystoneUriBase() + "/" + uriSuffix + "s";
        try {
            URI keystoneURI = new URI(resourcesUri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("GET to " + keystoneURI.toString());
            }
            return getResourcesFromGet(rt, keystoneURI);
        } catch (URISyntaxException ex) {
            LOG.error("unable to build resourcesUri: " + resourcesUri);
        } catch (HttpStatusCodeException ex) {
            LOG.error("unable to obtain resources for: " + resourcesUri + " - " + ex.getMessage());
            // check if it's a 401
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthenticationFailureException(ex);
            } else {
                throw ex;
            }
        }
        return new ArrayList<R>(0);
    }

    protected abstract List<R> getResourcesFromGet(RestTemplate rt, URI targetUri)
            throws HttpStatusCodeException, UpdaterHttpException;

}
