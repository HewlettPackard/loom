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
package com.hp.hpl.loom.adapter.twitter.models;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonAutoDetect
public class Tweet implements BaseModel {

    private Date createdAt;
    private long statusId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long inReplyToStatusId;
    private String text;
    private boolean isFavorited;
    private boolean isRetweeted;
    private int favoriteCount;
    private int retweetCount;
    private long userId;
    private String userScreenName;

    public Tweet() {}

    public Tweet(final Date createdAt, final long statusId, final long inReplyToStatusId, final String text,
            final boolean isFavorited, final boolean isRetweeted, final int favoriteCount, final int retweetCount,
            final long userId, final String userScreenName) {
        this.createdAt = createdAt;
        this.statusId = statusId;
        this.inReplyToStatusId = inReplyToStatusId;
        this.text = text;
        this.isFavorited = isFavorited;
        this.isRetweeted = isRetweeted;
        this.favoriteCount = favoriteCount;
        this.retweetCount = retweetCount;
        this.userId = userId;
        this.userScreenName = userScreenName;
    }

    @Override
    public String getId() {
        return Long.toString(statusId);
    }

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

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(final boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public boolean isRetweeted() {
        return isRetweeted;
    }

    public void setRetweeted(final boolean isRetweeted) {
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Tweet quote = (Tweet) o;
        return new EqualsBuilder().append(statusId, quote.statusId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(statusId).toHashCode();
    }
}
