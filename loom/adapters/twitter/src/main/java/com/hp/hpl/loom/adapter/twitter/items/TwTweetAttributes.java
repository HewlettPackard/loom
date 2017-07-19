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
package com.hp.hpl.loom.adapter.twitter.items;

import java.util.Date;

import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;

public class TwTweetAttributes extends TwItemAttributes {

    // ------------------------------------------------------------------ //
    // PRIVATE FIELDS //
    // ------------------------------------------------------------------ //
    @LoomAttribute(key = "Date", supportedOperations = {DefaultOperations.SORT_BY})
    private Date createdAt;
    @LoomAttribute(key = "Status ID", supportedOperations = {DefaultOperations.SORT_BY})
    private long statusId;
    @LoomAttribute(key = "In reply to", supportedOperations = {DefaultOperations.SORT_BY})
    private long inReplyToStatusId;
    @LoomAttribute(key = "Text", supportedOperations = {DefaultOperations.SORT_BY})
    private String text;
    @LoomAttribute(key = "Favorited", supportedOperations = {DefaultOperations.SORT_BY})
    private boolean isFavorited;
    @LoomAttribute(key = "Retweeted", supportedOperations = {DefaultOperations.SORT_BY})
    private boolean isRetweeted;
    @LoomAttribute(key = "Favorite count", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true)
    private int favoriteCount;
    @LoomAttribute(key = "Retweet count", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true)
    private int retweetCount;
    @LoomAttribute(key = "User ID", supportedOperations = {DefaultOperations.SORT_BY})
    private long userId;
    @LoomAttribute(key = "Screen name", supportedOperations = {DefaultOperations.SORT_BY})
    private String userScreenName;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getStatusId() {
        return statusId;
    }

    public void setStatusId(final long statusId) {
        this.statusId = statusId;
    }

    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(final long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(final boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public boolean getIsRetweeted() {
        return isRetweeted;
    }

    public void setIsRetweeted(final boolean isRetweeted) {
        this.isRetweeted = isRetweeted;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(final int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(final int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public void setUserScreenName(final String userScreenName) {
        this.userScreenName = userScreenName;
    }

    // @Override
    // public boolean isDifferentFrom(final Item oldItem) {
    // TwTweet oi = (TwTweet) oldItem;
    //
    // boolean isDifferent = false;
    //
    // if (isFavorited != oi.getIsFavorited()) {
    // isDifferent = true;
    // }
    //
    // if (isRetweeted != oi.getIsRetweeted()) {
    // isDifferent = true;
    // }
    //
    // if (favoriteCount != oi.getFavoriteCount()) {
    // isDifferent = true;
    // }
    //
    // if (retweetCount != oi.getRetweetCount()) {
    // isDifferent = true;
    // }
    //
    // Integer oldAlertLevel = oldItem.getAlertLevel();
    //
    // if (favoriteCount > 200 || retweetCount > 200) {
    // this.setAlertLevel(TwTweet.MORE_THAN_200);
    // this.setAlertDescription("Favorited/Retweeted > 200");
    // } else if (favoriteCount > 100 || retweetCount > 100) {
    // this.setAlertLevel(TwTweet.MORE_THAN_100);
    // this.setAlertDescription("Favorited/Retweeted > 100");
    // } else if (favoriteCount > 50 || retweetCount > 50) {
    // this.setAlertLevel(TwTweet.MORE_THAN_50);
    // this.setAlertDescription("Favorited/Retweeted > 50");
    // } else if (favoriteCount > 10 || retweetCount > 10) {
    // this.setAlertLevel(TwTweet.MORE_THAN_10);
    // this.setAlertDescription("Favorited/Retweeted > 10");
    // } else if (favoriteCount > 5 || retweetCount > 5) {
    // this.setAlertLevel(TwTweet.MORE_THAN_5);
    // this.setAlertDescription("Favorited/Retweeted > 5");
    // }
    //
    // if ((oldAlertLevel != null) || (this.getAlertLevel() > 0)) {
    // if (oldAlertLevel.equals(this.getAlertLevel())) {
    // this.setAlertLevel(oldAlertLevel);
    // this.setDescription(oldItem.getAlertDescription());
    // } else {
    // isDifferent = true;
    // }
    // }
    //
    // return isDifferent;
    // }
}
