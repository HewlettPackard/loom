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
package com.hp.hpl.loom.model;

import java.util.List;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Holds the stats for the Fibre.
 */
public class FibreStat {
    private static final int PERCENT_50 = 50;
    private DescriptiveStatistics stats;
    private Frequency freq;

    /**
     * Non arg constructor, initialises the frequency and descriptive statistics.
     */
    public FibreStat() {
        freq = new Frequency();
        stats = new DescriptiveStatistics();
    }

    /**
     * Add the value to the frequency and descriptive statistics.
     *
     * @param val value to add
     */
    public void addValue(final Number val) {
        freq.addValue(val.doubleValue());
        stats.addValue(val.doubleValue());
    }

    // min, max, mean, geometric mean, n, sum, sum of squares, standard deviation, variance,
    // percentiles, skewness, kurtosis, median

    /**
     * Gets the min value.
     *
     * @return the min value
     */
    public double getMin() {
        return stats.getMin();
    }

    /**
     * Gets the max value.
     *
     * @return the max value
     */
    public double getMax() {
        return stats.getMax();
    }

    /**
     * Get the mean value.
     *
     * @return the mean value
     */
    public double getMean() {
        return stats.getMean();
    }

    /**
     * Get the geometric mean.
     *
     * @return the geometric mean
     */
    public double getGeometricMean() {
        return stats.getGeometricMean();
    }

    /**
     * Get the count of values.
     *
     * @return the count of values
     */
    public long getCount() {
        return stats.getN();
    }

    /**
     * Get the sum of the values.
     *
     * @return the sum of values
     */
    public double getSum() {
        return stats.getSum();
    }

    /**
     * Get the sum of the squares.
     *
     * @return the sum of the squares.
     */
    public double getSumOfSquares() {
        return stats.getSumsq();
    }

    /**
     * Get the standard deviation.
     *
     * @return the standard deviation
     */
    public double getStd() {
        return stats.getStandardDeviation();
    }

    /**
     * Get the variance.
     *
     * @return the variance
     */
    public double getVariance() {
        return stats.getVariance();
    }

    /**
     * Get the percentile for the provided value.
     *
     * @param p the percentile value
     * @return the percentile
     */
    public double getPercentile(final double p) {
        return stats.getPercentile(p);
    }

    /**
     * Get the skewness.
     *
     * @return the skewness
     */
    public double getSkewness() {
        return stats.getSkewness();
    }

    /**
     * Get the kurtosis.
     *
     * @return the kurtosis
     */
    public double getKurtosis() {
        return stats.getKurtosis();
    }

    /**
     * Get the median value.
     *
     * @return the median value.
     */
    public double getMedian() {
        return stats.getPercentile(PERCENT_50);
    }

    /**
     * Get the list of mode values.
     *
     * @return the mode values
     */
    public List<Comparable<?>> getMode() {
        return freq.getMode();
    }
}
