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
package com.hp.hpl.loom.adapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class allows for easy timing information to be produced.
 */
public class TimingBag {

    private static final int TOP_NBR_DEFAULT = 3;
    private static final int MIN_TIME_DEFAULT = 100000000;
    private String lid;
    private long minTime = MIN_TIME_DEFAULT;
    private long maxTime = 0;
    private long avgTime = 0;
    private int topNbr = TOP_NBR_DEFAULT;
    private ArrayList<Long> topList = new ArrayList<>(topNbr);
    private long runIdx = 0;
    private long latestTime;

    /**
     * Setup a TimingBag for the provided lid.
     *
     * @param lid id to record for
     */
    public TimingBag(final String lid) {
        this.lid = lid;
        // timing setup
        for (int i = 0; i < topNbr; i++) {
            topList.add((long) 0);
        }
    }

    /**
     * Record the time for given lid, this is then stored to produce min/max/avg from.
     *
     * @param value timing to store
     */
    public void recordTime(final long value) {
        runIdx++;
        latestTime = value;
        if (value < minTime) {
            minTime = value;
        }
        if (value > maxTime) {
            maxTime = value;
        }
        avgTime += value;
        // is it in topList?
        if (value > topList.get(topNbr - 1)) {
            ArrayList<Long> newTopList = new ArrayList<>(topList);
            newTopList.add(value);
            Collections.sort(newTopList, Collections.reverseOrder());
            topList = new ArrayList<>(newTopList.subList(0, topNbr));
        }
    }

    /**
     * Returns the most recent timing.
     *
     * @return latest time
     */
    public long getLatest() {
        return latestTime;
    }

    /**
     * Returns the minimum time in the bag.
     *
     * @return the min time
     */
    public long getMin() {
        return minTime;
    }

    /**
     * Returns the max time in the bag.
     *
     * @return the max time
     */
    public long getMax() {
        return maxTime;
    }

    /**
     * Returns the avg time in the bag.
     *
     * @return the avg time
     */
    public long getAvg() {
        return avgTime / runIdx;
    }

    /**
     * Gets the list of timings.
     *
     * @return the timings
     */
    public ArrayList<Long> getTopList() {
        return topList;
    }

    /**
     * Returns a string of the timing information.
     *
     * @return string of the times.
     */
    public String displayTimes() {
        StringBuffer stBuf = new StringBuffer();
        stBuf.append("\nTime taken to collect items data & rels for " + lid + "\n");
        stBuf.append("min --> " + minTime + " ms \n");
        stBuf.append("max --> " + maxTime + " ms \n");
        stBuf.append("avg --> " + avgTime / runIdx + " ms over " + runIdx + " runs\n");
        stBuf.append("topList (ms) --> " + topList + "\n");
        return stBuf.toString();
    }
}
