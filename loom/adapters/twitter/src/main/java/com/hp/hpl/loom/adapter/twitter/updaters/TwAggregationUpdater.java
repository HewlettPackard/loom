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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
// import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.twitter.TwAdapter;
import com.hp.hpl.loom.adapter.twitter.TwItemCollector;
import com.hp.hpl.loom.adapter.twitter.items.TwItemAttributes;
import com.hp.hpl.loom.adapter.twitter.models.BaseModel;
import com.hp.hpl.loom.adapter.twitter.models.Tweet;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.ItemType;

public abstract class TwAggregationUpdater<T extends AdapterItem<A>, A extends TwItemAttributes, R extends BaseModel>
        extends AggregationUpdater<T, A, R> {

    // private static Logger LOG = Logger.getLogger(TwAggregationUpdater.class);

    protected final TwItemCollector ic;
    protected TwitterStream twitterStream;
    protected List<Tweet> tweets;

    public TwAggregationUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final TwItemCollector ic) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType.getLocalId(), ic);
        this.ic = ic;
        this.createTwitterStream();
    }

    @Override
    public Aggregation getAggregation() {
        return aggregation;
    }

    /**
     * @see BaseAdapter#getItemLogicalId(String, String)
     */
    // @Override
    // public String getLogicalId(final String id) {
    // return logicalIdBase + id;
    // }

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

    protected List<Tweet> getTweets() {
        return tweets;
    }

    private void createTwitterStream() {
        tweets = new LinkedList<>();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        if (!StringUtils.isBlank(((TwAdapter) adapter).getProxyHost())
                && ((TwAdapter) adapter).getProxyPort() != null) {
            cb.setHttpProxyHost(((TwAdapter) adapter).getProxyHost())
                    .setHttpProxyPort(((TwAdapter) adapter).getProxyPort());
        }
        cb.setOAuthConsumerKey(((TwAdapter) adapter).getConsumerKey())
                .setOAuthConsumerSecret(((TwAdapter) adapter).getConsumerSecret())
                .setOAuthAccessToken(((TwAdapter) adapter).getAccessToken())
                .setOAuthAccessTokenSecret(((TwAdapter) adapter).getAccessTokenSecret());
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        ic.setTwitterStream(twitterStream);
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(final Status status) {
                // System.out.println("@" + status.getUser().getScreenName() + " - " +
                // status.getText());
                Tweet tweet = new Tweet(status.getCreatedAt(), status.getId(), status.getInReplyToStatusId(),
                        status.getText(), status.isFavorited(), status.isRetweeted(), status.getFavoriteCount(),
                        status.getRetweetCount(), status.getUser().getId(), status.getUser().getScreenName());
                tweets.add(tweet);
            }

            @Override
            public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
                Tweet tweet = new Tweet();
                tweet.setStatusId(statusDeletionNotice.getStatusId());
                int index = tweets.indexOf(tweet);
                if (index != -1) {
                    tweets.remove(index);
                }
            }

            @Override
            public void onTrackLimitationNotice(final int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(final long userId, final long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(final StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(final Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        FilterQuery filter = new FilterQuery();
        String[] keywords = ((TwAdapter) adapter).getFilterTerms();
        filter.track(keywords);
        twitterStream.filter(filter);
    }

    // ------------------------------------------------------------------ //
    // ABSTRACT INTERFACE //
    // ------------------------------------------------------------------ //

    protected abstract List<R> fetchData();
}
