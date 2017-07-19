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
package com.hp.hpl.loom.adapter.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import twitter4j.TwitterStream;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.twitter.items.TwTweet;
// import org.apache.log4j.Logger;
import com.hp.hpl.loom.adapter.twitter.updaters.TwTweetUpdater;
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

public class TwItemCollector extends AggregationUpdaterBasedItemCollector {

    // private static Logger LOG = Logger.getLogger(TwItemCollector.class);
    // private static final int ACTION_THREADS = 1;

    // ------------------------------------------------------------------ //
    // PRIVATE FIELDS //
    // ------------------------------------------------------------------ //

    private Collection<String> collectList;
    private Collection<String> updaterList;
    private TwitterStream twitterStream;

    // ------------------------------------------------------------------ //
    // PUBLIC INTERFACE //
    // ------------------------------------------------------------------ //
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public TwItemCollector(final Session session, final TwAdapter adapter, final AdapterManager adapterManager) {
        super(session, adapter, adapterManager);
    }

    public TwTweet getTweet(final String id) {
        return (TwTweet) updaterMap.get(TwTweet.TYPE_LOCAL_ID).getItem(id);
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        if (updaterList == null) {
            updaterList = this.createUpdateItemTypeIdList();
        }
        return updaterList;
    }

    protected Collection<String> createUpdateItemTypeIdList() {
        return new ArrayList<String>(Arrays.asList(TwTweet.TYPE_LOCAL_ID));
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        if (collectList == null) {
            collectList = this.createCollectionItemTypeIdList();
        }
        return collectList;
    }

    protected Collection<String> createCollectionItemTypeIdList() {
        return new ArrayList<String>(Arrays.asList(TwTweet.TYPE_LOCAL_ID));
    }

    @Override
    public ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        // NO ACTION IS SUPPORTED YET
        throw new InvalidActionSpecificationException("No action supported for typeId " + itemTypeId);
    }

    @Override
    public void close() {
        super.close();
        twitterStream.clearListeners();
        twitterStream.cleanUp();
        twitterStream.shutdown();
    }

    public void setTwitterStream(final TwitterStream twitterStream) {
        this.twitterStream = twitterStream;
    }

    // ------------------------------------------------------------------ //
    // PRIVATE INTERFACE //
    // ------------------------------------------------------------------ //

    @Override
    protected AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> getAggregationUpdater(
            final Aggregation aggregation) throws NoSuchProviderException, NoSuchItemTypeException {
        String typeId = aggregation.getTypeId();
        ItemType itemType;

        itemType = adapter.getItemType(TwTweet.TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TwTweetUpdater(aggregation, adapter, itemType, this);
        }

        return null;
    }
}
