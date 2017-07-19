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
package com.hp.hpl.loom.adapter.load.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;


public class PercentageDistro extends AbstractIntegerDistribution {
    private static final int PERCENT_100 = 100;
    private LinkedHashMap<Integer, Integer> chances;
    private Integer lower;
    private Integer upper;

    @SuppressWarnings("checkstyle:innerassignment")
    public PercentageDistro(RandomGenerator rng, LinkedHashMap<Integer, Integer> chances) {
        super(rng);
        this.chances = chances;

        chances.keySet().stream().max(Comparator.comparing(i -> i)).ifPresent(maxInt -> upper = maxInt);
        chances.keySet().stream().min(Comparator.comparing(i -> i)).ifPresent(minInt -> lower = minInt);

        chances = chances.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Integer totalPercent = chances.entrySet().stream().mapToInt(Map.Entry::getValue).sum();

        if (totalPercent != PERCENT_100) {
            throw new RuntimeException("Need to have 100% allocated");
        }

    }

    @Override
    public double cumulativeProbability(int x) {
        if (x < lower) {
            return 0;
        }
        if (x > upper) {
            return 1;
        }

        // TODO - rewrite as Lambda
        double total = 0;
        Set<Integer> keys = chances.keySet();
        for (Integer key : keys) {
            if (key <= x) {
                total += chances.get(key);
            }
        }

        if (total == -1) {
            return 1;
        }

        return total / PERCENT_100;
    }

    @Override
    public double getNumericalMean() {
        int total = 0;
        for (Integer key : chances.keySet()) {
            total += key * chances.get(key);
        }
        // TODO - rewrite as Lambda

        return (double) total / PERCENT_100;
    }

    @Override
    public double getNumericalVariance() {

        double mean = this.getNumericalMean();
        double sum = 0;
        // calculate the Variance, take each difference (between it and the mean), square it, and
        // then average the total
        for (Integer key : chances.keySet()) {
            double v = key - mean;
            double v2 = v * v;
            sum += v2 * chances.get(key);
        }
        return sum / PERCENT_100;
    }

    @Override
    public int getSupportLowerBound() {
        return lower;
    }

    @Override
    public int getSupportUpperBound() {
        return upper;
    }

    @Override
    public boolean isSupportConnected() {
        return true;
    }

    @Override
    public double probability(int x) {
        if (x < lower || x > upper) {
            return 0;
        }
        return (double) chances.get(x) / PERCENT_100;
    }
    //
    //
    // public static void main(String[] args) {
    // LinkedHashMap<Integer, Integer> chances = new LinkedHashMap<>();
    // chances.put(0, 10);
    // chances.put(1, 40);
    // chances.put(2, 40);
    // chances.put(3, 10);
    // JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();
    // jdkRandomGenerator.setSeed(0);
    //
    // PercentageDistro testDistro = new PercentageDistro(jdkRandomGenerator, chances);
    //
    // for (int i = testDistro.lower; i <= testDistro.upper; i++) {
    // System.out.println(i + " " + testDistro.cumulativeProbability(i) + " " +
    // testDistro.probability(i));
    // }
    //
    // System.out.println("------");
    // System.out.println("lower/upper = " + testDistro.getSupportLowerBound() + "/"
    // + testDistro.getSupportUpperBound());
    // System.out.println("Mean: " + testDistro.getNumericalMean());
    // System.out.println("Variance: " + testDistro.getNumericalVariance());
    //
    //
    // int[] sample = testDistro.sample(10000000);
    // List<Integer> intList = new ArrayList<Integer>();
    // for (int index = 0; index < sample.length; index++) {
    // intList.add(sample[index]);
    // }
    // float f = intList.size() / 100;
    // System.out.println(Collections.frequency(intList, 0) + " " + (Collections.frequency(intList,
    // 0) / f));
    // System.out.println(Collections.frequency(intList, 1) + " " + (Collections.frequency(intList,
    // 1) / f));
    // System.out.println(Collections.frequency(intList, 2) + " " + (Collections.frequency(intList,
    // 2) / f));
    // System.out.println(Collections.frequency(intList, 3) + " " + (Collections.frequency(intList,
    // 3) / f));
    //
    // }
}
