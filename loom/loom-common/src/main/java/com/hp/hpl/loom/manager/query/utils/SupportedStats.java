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
package com.hp.hpl.loom.manager.query.utils;

/**
 * Enumeration of the supported stats functions.
 */
public enum SupportedStats {

    /**
     * Average of the values.
     */
    AVG("_avg"),
    /**
     * Count of the values.
     */
    COUNT("_count"),
    /**
     * Max of the values.
     */
    MAX("_max"),
    /**
     * Min of the values.
     */
    MIN("_min"),
    /**
     * Sum of the values.
     */
    SUM("_sum"),
    /**
     * Geometric mean of the values.
     */
    GEO_MEAN("_geometricMean"),
    /**
     * Sum of the squares of the values.
     */
    SUM_SQ("_sumOfSquares"),
    /**
     * Standard deviation of the values.
     */
    STD("_std"),
    /**
     * Variance of the values.
     */
    VAR("_var"),
    /**
     * test.
     */
    SKEW("_skew"),
    /**
     * Measure of the "peakedness" of the probability distribution of a real-valued random variable
     * of the values.
     */
    KURTOSIS("_kurtosis"),
    /**
     * Median of the values.
     */
    MEDIAN("_median"),
    /**
     * Mode of the values.
     */
    MODE("_mode");


    private String name;

    private SupportedStats(final String name) {
        this.name = name;
    }

    /**
     * Equals check based on comparing the names.
     *
     * @param otherName name to compare with
     * @return true if the names are equal
     */
    public boolean equalName(final String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    @Override
    public String toString() {
        return name;
    }
}
