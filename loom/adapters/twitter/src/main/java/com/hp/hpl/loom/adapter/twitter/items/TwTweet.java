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

import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;

@ItemTypeInfo(TwTweet.TYPE_LOCAL_ID)
public class TwTweet extends TwItem<TwTweetAttributes> {
    public static final String TYPE_LOCAL_ID = "tweet";
    private static final int MORE_THAN_5 = 1;
    private static final int MORE_THAN_10 = 2;
    private static final int MORE_THAN_50 = 3;
    private static final int MORE_THAN_100 = 4;
    private static final int MORE_THAN_200 = 5;


    // ------------------------------------------------------------------ //
    // PUBLIC INTERFACE //
    // ------------------------------------------------------------------ //

    public TwTweet(final String logicalId, final ItemType tweetType) {
        super(logicalId, tweetType);
    }

    @Override
    public boolean update() {
        boolean update = super.update();

        int favoriteCount = getCore().getFavoriteCount();
        int retweetCount = getCore().getRetweetCount();

        if (favoriteCount > 200 || retweetCount > 200) {
            this.setAlertLevel(TwTweet.MORE_THAN_200);
            this.setAlertDescription("Favorited/Retweeted > 200");
            update = true;
        } else if (favoriteCount > 100 || retweetCount > 100) {
            this.setAlertLevel(TwTweet.MORE_THAN_100);
            this.setAlertDescription("Favorited/Retweeted > 100");
            update = true;
        } else if (favoriteCount > 50 || retweetCount > 50) {
            this.setAlertLevel(TwTweet.MORE_THAN_50);
            this.setAlertDescription("Favorited/Retweeted > 50");
            update = true;
        } else if (favoriteCount > 10 || retweetCount > 10) {
            this.setAlertLevel(TwTweet.MORE_THAN_10);
            this.setAlertDescription("Favorited/Retweeted > 10");
            update = true;
        } else if (favoriteCount > 5 || retweetCount > 5) {
            this.setAlertLevel(TwTweet.MORE_THAN_5);
            this.setAlertDescription("Favorited/Retweeted > 5");
            update = true;
        }

        return update;
    }


}
