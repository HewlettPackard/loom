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
package com.hp.hpl.loom.adapter.twitter.updaters;

import java.util.List;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
// import org.apache.log4j.Logger;
import com.hp.hpl.loom.adapter.twitter.TwItemCollector;
import com.hp.hpl.loom.adapter.twitter.items.TwTweet;
import com.hp.hpl.loom.adapter.twitter.items.TwTweetAttributes;
import com.hp.hpl.loom.adapter.twitter.models.Tweet;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TwTweetUpdater extends TwAggregationUpdater<TwTweet, TwTweetAttributes, Tweet> {

    // private static Logger LOG = Logger.getLogger(TwTweetUpdater.class);

    public TwTweetUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final TwItemCollector ic) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, ic);
    }

    @Override
    protected TwTweet createEmptyItem(final String logicalId) {
        return new TwTweet(logicalId, this.getItemType());
    }

    @Override
    protected List<Tweet> fetchData() {
        return getTweets();
    }

    @Override
    protected TwTweetAttributes createItemAttributes(final Tweet resource) {
        TwTweetAttributes twTweetAttributes = new TwTweetAttributes();
        twTweetAttributes.setItemId(resource.getId());
        twTweetAttributes.setItemName(Long.toString(resource.getStatusId()));

        twTweetAttributes.setInReplyToStatusId(resource.getInReplyToStatusId());
        twTweetAttributes.setText(resource.getText());
        twTweetAttributes.setIsFavorited(resource.isFavorited());
        twTweetAttributes.setIsRetweeted(resource.isRetweeted());
        twTweetAttributes.setFavoriteCount(resource.getFavoriteCount());
        twTweetAttributes.setRetweetCount(resource.getRetweetCount());
        twTweetAttributes.setUserId(resource.getUserId());
        twTweetAttributes.setUserScreenName(resource.getUserScreenName());
        return twTweetAttributes;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final TwTweetAttributes itemAttr, final Tweet resource) {
        boolean isDifferent = false;

        if (itemAttr.getIsFavorited() != resource.isFavorited()) {
            isDifferent = true;
        }

        if (itemAttr.getIsRetweeted() != resource.isRetweeted()) {
            isDifferent = true;
        }

        if (itemAttr.getFavoriteCount() != resource.getFavoriteCount()) {
            isDifferent = true;
        }

        if (itemAttr.getRetweetCount() != resource.getRetweetCount()) {
            isDifferent = true;
        }
        if (!isDifferent) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final Tweet resource) {
        // TODO Auto-generated method stub

    }
}
