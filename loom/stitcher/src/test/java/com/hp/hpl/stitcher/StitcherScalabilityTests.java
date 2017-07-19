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
package com.hp.hpl.stitcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class StitcherScalabilityTests {

    private static class SameValueComparator implements StitchChecker<Integer, String> {

        @Override
        public double checkStitch(Integer baseElement, String candidateElement) {
            return (baseElement == Integer.parseInt(candidateElement) ? 1.0 : 0.0);
        }

    }

    public static int HOW_MANY_RUNS = 10;
    public static int HOW_MANY_BASE_ITEMS = 25000;
    public static int HOW_MANY_CANDIDATE_ITEMS = HOW_MANY_BASE_ITEMS;
    public static int HOW_MANY_TIMES_EACH_CANDIDATE = 1;
    public static boolean PRINT_STITCHES = false;

    public static void main(String[] args) {
        int itemNumbers[] = {400000, 300000, 1000000};
        int runs[] = {1, 2, 2};

        for (int k = 0; k < itemNumbers.length; k++) {
            HOW_MANY_BASE_ITEMS = itemNumbers[k];
            HOW_MANY_CANDIDATE_ITEMS = itemNumbers[k];
            HOW_MANY_RUNS = runs[k];

            printConditions();
            for (int i = 0; i < HOW_MANY_RUNS; i++) {
                System.out.printf("Current time: %s\n", new Date(System.currentTimeMillis()));
                test();
            }
        }
    }

    public static void test() {
        long startTime, stopTime;

        Stitcher<Integer, String> stitcher;
        Collection<Integer> baseElements;
        Collection<String> candidateElements;
        int candidatesLength = Integer.toString(HOW_MANY_CANDIDATE_ITEMS - 1).length();
        Map<Integer, Collection<String>> stitches;

        startTime = System.nanoTime();

        // Generate base elements (Integers)
        baseElements = new ArrayList<Integer>(HOW_MANY_BASE_ITEMS);
        for (int i = 0; i < HOW_MANY_BASE_ITEMS; i++) {
            baseElements.add(i);
        }

        // Generate candidate elements (Strings)
        candidateElements = new ArrayList<String>(HOW_MANY_CANDIDATE_ITEMS);
        for (int i = 0; i < HOW_MANY_CANDIDATE_ITEMS; i++) {
            for (int j = 0; j < HOW_MANY_TIMES_EACH_CANDIDATE; j++) {
                candidateElements.add(paddedString(Integer.toString(i), candidatesLength));
            }
        }

        stopTime = System.nanoTime();

        printTime("Elements generated", startTime, stopTime);

        // Prepare the stitcher
        startTime = System.nanoTime();
        stitcher = new BruteStitcher<Integer, String>(HOW_MANY_BASE_ITEMS, HOW_MANY_CANDIDATE_ITEMS);
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);
        stitcher.addStitchChecker(new SameValueComparator());
        stopTime = System.nanoTime();
        printTime("Stitcher ready", startTime, stopTime);

        // Stitch!
        startTime = System.nanoTime();
        stitches = stitcher.stitch();
        stopTime = System.nanoTime();
        printTime("Stitching finished", startTime, stopTime);

        System.out.println();

        if (PRINT_STITCHES) {
            printStitches(stitches);
        }
    }

    private static void printConditions() {
        System.out.printf("How many runs: %d\n", HOW_MANY_RUNS);
        System.out.printf("How many base items: %d\n", HOW_MANY_BASE_ITEMS);
        System.out.printf("How many candidate items: %d\n", HOW_MANY_CANDIDATE_ITEMS);
        System.out.printf("How many times each candidate: %d\n", HOW_MANY_TIMES_EACH_CANDIDATE);
        System.out.println();
    }

    private static void printTime(String message, long start, long stop) {
        System.out.printf("%s. Elapsed time (seconds): %f\n", message, ((double) (stop - start) / 1000000000));
    }

    private static void printStitches(Map<Integer, Collection<String>> stitches) {
        System.out.println("Stitches:");
        for (Integer key : stitches.keySet()) {
            System.out.printf("%-10d: %s\n", key, stitches.get(key));
        }
        System.out.println();
    }

    private static String paddedString(String s, int length) {
        StringBuilder sb = new StringBuilder("00000000000");
        sb.append(s);
        int currentLength = sb.length();
        return sb.substring(currentLength - length, currentLength);
    }
}
