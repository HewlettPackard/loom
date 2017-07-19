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
package com.hp.hpl.loom.adapter.finance;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.finance.items.FnMarket;
import com.hp.hpl.loom.adapter.finance.items.FnMarketAttributes;
import com.hp.hpl.loom.adapter.finance.items.FnQuote;
import com.hp.hpl.loom.adapter.finance.updaters.FnMarketUpdater;
import com.hp.hpl.loom.adapter.finance.updaters.FnQuoteUpdater;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;

public class FnItemCollector extends AggregationUpdaterBasedItemCollector {

    // private static Logger LOG = Logger.getLogger(FnItemCollector.class);
    // private static final int ACTION_THREADS = 1;

    // ------------------------------------------------------------------ //
    // PRIVATE FIELDS //
    // ------------------------------------------------------------------ //

    // private final ExecutorService actionExec;

    private Collection<String> collectList;
    private Collection<String> updaterList;
    private Proxy proxy;

    // ------------------------------------------------------------------ //
    // PUBLIC INTERFACE //
    // ------------------------------------------------------------------ //

    public FnItemCollector(final Session session, final FnAdapter adapter, final AdapterManager adapterManager,
            final Proxy proxy) {
        super(session, adapter, adapterManager);
        // actionExec = Executors.newFixedThreadPool(FnItemCollector.ACTION_THREADS);
        this.proxy = proxy;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public FnQuote getQuote(final String id) {
        return (FnQuote) updaterMap.get(FnQuote.TYPE_LOCAL_ID).getItem(id);
    }

    public FnMarket getMarket(final String id) {
        return (FnMarket) updaterMap.get(FnMarket.TYPE_LOCAL_ID).getItem(id);
    }

    public FnMarketAttributes getLatestMarketAttributes(final String id) {
        return (FnMarketAttributes) updaterMap.get(FnMarket.TYPE_LOCAL_ID).getLatestCoreItemAttributes(id);
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        if (updaterList == null) {
            updaterList = this.createUpdateItemTypeIdList();
        }
        return updaterList;
    }

    protected Collection<String> createUpdateItemTypeIdList() {
        return new ArrayList<String>(Arrays.asList(FnQuote.TYPE_LOCAL_ID, FnMarket.TYPE_LOCAL_ID));
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        if (collectList == null) {
            collectList = this.createCollectionItemTypeIdList();
        }
        return collectList;
    }

    protected Collection<String> createCollectionItemTypeIdList() {
        return new ArrayList<String>(Arrays.asList(FnQuote.TYPE_LOCAL_ID, FnMarket.TYPE_LOCAL_ID));
    }

    @Override
    public ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        // NO ACTION IS SUPPORTED YET
        throw new InvalidActionSpecificationException("No action supported for typeId " + itemTypeId);
    }

    // ------------------------------------------------------------------ //
    // PRIVATE INTERFACE //
    // ------------------------------------------------------------------ //

    @Override
    protected AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> getAggregationUpdater(
            final Aggregation aggregation) throws NoSuchProviderException, NoSuchItemTypeException {
        String typeId = aggregation.getTypeId();
        ItemType itemType;

        itemType = adapter.getItemType(FnQuote.TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new FnQuoteUpdater(aggregation, adapter, itemType, this);
        }
        itemType = adapter.getItemType(FnMarket.TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new FnMarketUpdater(aggregation, adapter, itemType, this);
        }
        return null;
    }
}
