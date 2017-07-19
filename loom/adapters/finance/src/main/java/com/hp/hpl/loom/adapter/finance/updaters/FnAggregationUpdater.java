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
package com.hp.hpl.loom.adapter.finance.updaters;

import java.net.Proxy;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

// import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.finance.FnAdapter;
import com.hp.hpl.loom.adapter.finance.FnItemCollector;
import com.hp.hpl.loom.adapter.finance.items.FnItemAttributes;
import com.hp.hpl.loom.adapter.finance.models.BaseModel;
import com.hp.hpl.loom.adapter.finance.models.FnRestManager;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.ItemType;

public abstract class FnAggregationUpdater<T extends AdapterItem<A>, A extends FnItemAttributes, R extends BaseModel>
        extends AggregationUpdater<T, A, R> {

    // private static Logger LOG = Logger.getLogger(FnAggregationUpdater.class);

    protected final FnItemCollector ic;
    protected RestTemplate rt;
    protected Proxy proxy;

    public FnAggregationUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final FnItemCollector ic, final Proxy proxy) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType.getLocalId(), ic);
        this.ic = ic;
        this.proxy = proxy;
        this.createRestTemplate(proxy);
    }

    @Override
    public Aggregation getAggregation() {
        return aggregation;
    }

    /**
     * @see BaseAdapter#getItemLogicalId(String, String)
     */

    @Override
    protected Iterator<R> getResourceIterator() {
        return this.fetchData().iterator();
    }

    @Override
    protected String getItemId(final R resource) {
        return resource.getId();
    }

    // ------------------------------------------------------------------ //
    // PRIVATE INTERFACE //
    // ------------------------------------------------------------------ //

    protected ItemType getItemType() {
        return itemType;
    }

    public String[] getSymbols() {
        return ((FnAdapter) adapter).getSymbols();
    }

    public String getMarketSymbol() {
        return ((FnAdapter) adapter).getProvider().getProviderId();
    }

    protected URI getQuotesURI(final String prependSymbols, final String appendSymbols) {
        String uri = prependSymbols;
        for (String symbol : getSymbols()) {
            uri += "\"" + symbol + "\",";
        }
        uri = uri.substring(0, uri.length() - 1);
        uri += appendSymbols;
        return new UriTemplate(uri).expand();
    }

    protected URI getMarketURI(final String prependSymbols, final String appendSymbols) {
        String uri = prependSymbols + "\"" + getMarketSymbol() + "\"" + appendSymbols;
        return new UriTemplate(uri).expand();
    }

    private void createRestTemplate(final Proxy proxy) {
        this.rt = FnRestManager.createRestTemplateWithJsonSupport(proxy);
    }

    // ------------------------------------------------------------------ //
    // ABSTRACT INTERFACE //
    // ------------------------------------------------------------------ //

    protected abstract List<R> fetchData();
}
