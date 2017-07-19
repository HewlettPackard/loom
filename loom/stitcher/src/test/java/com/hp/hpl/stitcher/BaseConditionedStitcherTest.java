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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.stitcher.AbstractStitcherAndBruteStitcherTest.TestFilterOnlyEvenNumbers;
import com.hp.hpl.stitcher.AbstractStitcherAndBruteStitcherTest.TestFilterOnlyNumbersAboveFive;
import com.hp.hpl.stitcher.AbstractStitcherAndBruteStitcherTest.TestFilterOnlyShortStrings;
import com.hp.hpl.stitcher.AbstractStitcherAndBruteStitcherTest.TestFilterOnlyStringsWithoutLetterA;
import com.hp.hpl.stitcher.AbstractStitcherAndBruteStitcherTest.TestStichCheckerLenght;

public class BaseConditionedStitcherTest {

    /**
     * For an even number, this filter will remove those strings whose number of vowels is odd. For
     * an odd number, it will remove those whose number of vowels is even.
     * <p>
     * 
     * <b>Example 1:</b> Base element = 4 (even); "England" to be kept (2 vowels, even); "Germany"
     * to be removed (3 vowels, odd)
     * <p>
     * 
     * <b>Example 2:</b> Base element = 7 (odd); "England" to be removed (2 vowels, even); "Germany"
     * to be kept (3 vowels, odd)
     * <p>
     */
    static class EvenAndOddVowels implements BaseDependentFilter<Integer, String> {

        @Override
        public boolean filter(Integer baseElement, String candidateElement) {
            int count = candidateElement.length() - candidateElement.replaceAll("[AEIOUYaeiouy]", "").length();
            return count % 2 != baseElement % 2;
        }

        @Override
        public Collection<String> filter(Integer baseElement, Collection<String> candidateElements) {
            Collection<String> resColl = new ArrayList<String>();

            for (String element : candidateElements) {
                if (!filter(baseElement, element)) {
                    resColl.add(element);
                }
            }

            return resColl;
        }
    }

    class SpecificRemainder implements StitchChecker<Integer, Integer> {

        private int remainder;

        public SpecificRemainder(int remainder) {
            this.remainder = remainder;
        }

        @Override
        public double checkStitch(Integer baseElement, Integer candidateElement) {
            return (candidateElement % baseElement == remainder ? 1.0 : 0.0);
        }

    }

    private static final String TALC = "Talc";
    private static final String GYPSUM = "Gypsum";
    private static final String CALCITE = "Calcite";
    private static final String FLUORITE = "Fluorite";
    private static final String APATITE = "Apatite";
    private static final String ORTHOCLASE = "Orthoclase";
    private static final String QUARTZ = "Quartz";
    private static final String TOPAZ = "Topaz";
    private static final String CORUNDUM = "Corundum";
    private static final String DIAMOND = "Diamond";

    private static final int selectiveTestBaseNo = 500;
    private static final int selectiveTestCandNo = 10000;

    private static Collection<Integer> baseElements;
    private static Collection<String> candidateElements;

    @BeforeClass
    public static void init() {
        baseElements = new ArrayList<Integer>();
        for (int i = 1; i <= 10; i++) {
            baseElements.add(i);
        }

        // Mohs scale of mineral hardness (just an example)
        candidateElements = new ArrayList<String>();
        candidateElements.add(TALC);
        candidateElements.add(GYPSUM);
        candidateElements.add(CALCITE);
        candidateElements.add(FLUORITE);
        candidateElements.add(APATITE);
        candidateElements.add(ORTHOCLASE);
        candidateElements.add(QUARTZ);
        candidateElements.add(TOPAZ);
        candidateElements.add(CORUNDUM);
        candidateElements.add(DIAMOND);
    }

    @Test
    public void testConstructors() {
        new ConditionedStitcher<Integer, String>();
        new ConditionedStitcher<Integer, String>(-1, -1);
        new ConditionedStitcher<Integer, String>(0, 0);
        new ConditionedStitcher<Integer, String>(1000, 1000);
    }

    // Stitching results should be:
    // <Empty map>
    @Test
    public void testFullStichingNoElementsWhatsoever() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        // stitcher.addBaseElements(baseElements); // SKIPPED
        // stitcher.addCandidateElements(candidateElements); // SKIPPED

        // Add filters
        // No filters to be added in this test

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>();

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // <Empty map>
    @Test
    public void testFullStichingNoBaseElements() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        // stitcher.addBaseElements(baseElements); // SKIPPED
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>();

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 1: {}
    // - 2: {}
    // - 3: {}
    // - 4: {}
    // - 5: {}
    // - 6: {}
    // - 7: {}
    // - 8: {}
    // - 9: {}
    // - 10: {}
    @Test
    public void testFullStichingNoCandidateElements() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        // stitcher.addCandidateElements(candidateElements); // SKIPPED

        // Add filters
        // No filters to be added in this test

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 1; i <= 10; i++) {
            expectedStitches.put(i, new ArrayList<String>());
        }

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 1: {}
    // - 2: {}
    // - 3: {}
    // - 4: {"Talc"}
    // - 5: {"Topaz"}
    // - 6: {"Gypsum", "Quartz"}
    // - 7: {"Calcite", "Apatite", "Diamond"}
    // - 8: {"Fluorite", "Corundum"}
    // - 9: {}
    // - 10: {"Orthoclase"}
    @Test
    public void testFullStiching() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 1; i <= 10; i++) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(4).add(TALC);
        expectedStitches.get(5).add(TOPAZ);
        expectedStitches.get(6).add(GYPSUM);
        expectedStitches.get(6).add(QUARTZ);
        expectedStitches.get(7).add(CALCITE);
        expectedStitches.get(7).add(APATITE);
        expectedStitches.get(7).add(DIAMOND);
        expectedStitches.get(8).add(FLUORITE);
        expectedStitches.get(8).add(CORUNDUM);
        expectedStitches.get(10).add(ORTHOCLASE);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 2: {}
    // - 4: {"Talc"}
    // - 6: {"Gypsum", "Quartz"}
    // - 8: {"Fluorite", "Corundum"}
    // - 10: {"Orthoclase"}
    @Test
    public void testFullStichingFilteredBase() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addBaseFilter(new TestFilterOnlyEvenNumbers());

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 2; i <= 10; i += 2) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(4).add(TALC);
        expectedStitches.get(6).add(GYPSUM);
        expectedStitches.get(6).add(QUARTZ);
        expectedStitches.get(8).add(FLUORITE);
        expectedStitches.get(8).add(CORUNDUM);
        expectedStitches.get(10).add(ORTHOCLASE);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 1: {}
    // - 2: {}
    // - 3: {}
    // - 4: {"Talc"}
    // - 5: {"Topaz"}
    // - 6: {"Gypsum", "Quartz"}
    // - 7: {}
    // - 8: {}
    // - 9: {}
    // - 10: {}
    @Test
    public void testFullStichingFilteredCandidates() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addCandidateFilter(new TestFilterOnlyShortStrings());

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 1; i <= 10; i++) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(4).add(TALC);
        expectedStitches.get(5).add(TOPAZ);
        expectedStitches.get(6).add(GYPSUM);
        expectedStitches.get(6).add(QUARTZ);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 2: {}
    // - 4: {"Talc"}
    // - 6: {"Gypsum", "Quartz"}
    // - 8: {}
    // - 10: {}
    @Test
    public void testFullStichingFilteredBaseAndCandidates() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addBaseFilter(new TestFilterOnlyEvenNumbers());
        stitcher.addCandidateFilter(new TestFilterOnlyShortStrings());

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 2; i <= 10; i += 2) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(4).add(TALC);
        expectedStitches.get(6).add(GYPSUM);
        expectedStitches.get(6).add(QUARTZ);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 6: {"Gypsum"}
    // - 8: {}
    // - 10: {}
    @Test
    public void testFullStichingSeveralFiltersBaseAndCandidates() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addBaseFilter(new TestFilterOnlyEvenNumbers());
        stitcher.addBaseFilter(new TestFilterOnlyNumbersAboveFive());
        stitcher.addCandidateFilter(new TestFilterOnlyShortStrings());
        stitcher.addCandidateFilter(new TestFilterOnlyStringsWithoutLetterA());

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 6; i <= 10; i += 2) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(6).add(GYPSUM);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching results should be:
    // [Base element]: [Candidate matches as list]
    // - 1: {}
    // - 2: {}
    // - 3: {}
    // - 4: {}
    // - 5: {}
    // - 6: {"Gypsum", "Quartz"}
    // - 7: {"Calcite", "Diamond"}
    // - 8: {"Fluorite"}
    // - 9: {}
    // - 10: {"Orthoclase"}
    @Test
    public void testFullStichingBaseDependentFiltering() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addBaseDependentFilter(new EvenAndOddVowels());

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 1; i <= 10; i++) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(6).add(GYPSUM);
        expectedStitches.get(6).add(QUARTZ);
        expectedStitches.get(7).add(CALCITE);
        expectedStitches.get(7).add(DIAMOND);
        expectedStitches.get(8).add(FLUORITE);
        expectedStitches.get(10).add(ORTHOCLASE);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    // Stitching for base element "1" (to be filtered)
    // Stitching results should be: <null>
    @Test
    public void testSingleStitchingBaseElementFiltered() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        // stitcher.addBaseElements(baseElements); // NOT NECESSARY
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addBaseFilter(new TestFilterOnlyNumbersAboveFive());

        assertNull(stitcher.stitch(1));
    }

    // Stitching for base element "1"
    // Stitching results should be: <Empty list>
    @Test
    public void testSingleStitchingNoMatches() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        // stitcher.addBaseElements(baseElements); // NOT NECESSARY
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected list of matches
        Collection<String> expectedMatches = new ArrayList<String>();

        StitcherTestsUtils.checkSameElements(expectedMatches, stitcher.stitch(1));
    }

    // Stitching for base element "7"
    // Stitching results should be: {"Calcite", "Apatite", "Diamond"}
    @Test
    public void testSingleStitching() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        // stitcher.addBaseElements(baseElements); // NOT NECESSARY
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected list of matches
        Collection<String> expectedMatches = new ArrayList<String>();
        expectedMatches.add(CALCITE);
        expectedMatches.add(APATITE);
        expectedMatches.add(DIAMOND);

        StitcherTestsUtils.checkSameElements(expectedMatches, stitcher.stitch(7));
    }

    // Stitching for base element "7"
    // Stitching results should be: {"Calcite", "Diamond"}
    @Test
    public void testSingleStitchingBaseDependentFiltering() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        // stitcher.addBaseElements(baseElements); // NOT NECESSARY
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        stitcher.addBaseDependentFilter(new EvenAndOddVowels());

        // Create expected list of matches
        Collection<String> expectedMatches = new ArrayList<String>();
        expectedMatches.add(CALCITE);
        expectedMatches.add(DIAMOND);

        StitcherTestsUtils.checkSameElements(expectedMatches, stitcher.stitch(7));
    }

    @Test
    public void testFullStitchingWithSelectiveData() {
        int baseNo = selectiveTestBaseNo;
        int candNo = selectiveTestCandNo;

        Collection<Integer> baseElements, candidateElements;
        baseElements = new ArrayList<Integer>(baseNo);
        for (int i = 2; i < baseNo; i++) {
            baseElements.add(i);
        }
        candidateElements = new ArrayList<Integer>(candNo);
        for (int j = 0; j < candNo; j++) {
            candidateElements.add(j);
        }

        ConditionedStitcher<Integer, Integer> stitcher = new ConditionedStitcher<Integer, Integer>();
        stitcher.addStitchChecker(new SpecificRemainder(0));
        stitcher.addStitchChecker(new SpecificRemainder(1));
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected list of matches
        Map<Integer, Collection<Integer>> expectedMatches = new HashMap<Integer, Collection<Integer>>();
        for (int i = 2; i < baseNo; i++) {
            Collection<Integer> coll = new ArrayList<Integer>();
            expectedMatches.put(i, coll);
            for (int j = 0; j < candNo; j++) {
                if (j % i == 0) {
                    coll.add(j);
                } else if (j % i == 1) {
                    coll.add(j);
                }
            }
        }

        long startTime, stopTime;

        startTime = System.currentTimeMillis();
        Map<Integer, Collection<Integer>> actualMatches = stitcher.stitch();
        stopTime = System.currentTimeMillis();
        // System.out.printf("Stitching time (full): %f\n", ((double) (stopTime - startTime)) /
        // 1000.0);

        startTime = System.currentTimeMillis();
        StitcherTestsUtils.checkSameStitches(expectedMatches, actualMatches);
        stopTime = System.currentTimeMillis();
        // System.out.printf("Stitching time (full, verification): %f\n", ((double) (stopTime -
        // startTime)) / 1000.0);
    }

    @Test
    public void testSelectiveStitching() {
        int baseNo = selectiveTestBaseNo;
        int candNo = selectiveTestCandNo;

        ConditionedStitcher<Integer, Integer> stitcher = new ConditionedStitcher<Integer, Integer>();
        stitcher.addStitchChecker(new SpecificRemainder(0));
        stitcher.addStitchChecker(new SpecificRemainder(1));

        // Add filters
        // No filters to be added in this test

        // Create input
        Map<Integer, Collection<Integer>> input = new HashMap<Integer, Collection<Integer>>();
        for (int i = 2; i < baseNo; i++) {
            Collection<Integer> coll = new ArrayList<Integer>(candNo);
            input.put(i, coll);
            for (int j = 0; j < candNo; j++) {
                if (j % i == 0) {
                    coll.add(j);
                } else if (j % i == 1) {
                    coll.add(j);
                } else if (j % i == 2) {
                    coll.add(j);
                } else if (j % i == 3) {
                    coll.add(j);
                }
            }
        }

        // Create expected list of matches
        Map<Integer, Collection<Integer>> expectedMatches = new HashMap<Integer, Collection<Integer>>();
        for (int i = 2; i < baseNo; i++) {
            Collection<Integer> coll = new ArrayList<Integer>();
            expectedMatches.put(i, coll);
            for (int j = 0; j < candNo; j++) {
                if (j % i == 0) {
                    coll.add(j);
                } else if (j % i == 1) {
                    coll.add(j);
                }
            }
        }

        long startTime, stopTime;

        startTime = System.currentTimeMillis();
        Map<Integer, Collection<Integer>> actualMatches = stitcher.stitch(input);
        stopTime = System.currentTimeMillis();
        // System.out.printf("Stitching time (selective): %f\n", ((double) (stopTime - startTime)) /
        // 1000.0);

        startTime = System.currentTimeMillis();
        StitcherTestsUtils.checkSameStitches(expectedMatches, actualMatches);
        stopTime = System.currentTimeMillis();
        // System.out.printf("Stitching time (selective, verification): %f\n", ((double) (stopTime -
        // startTime)) / 1000.0);
    }

    @Test
    public void testStitchingWithNoStitchChecker() {
        ConditionedStitcher<Integer, String> stitcher = new ConditionedStitcher<Integer, String>();
        // stitcher.setStitchChecker(new TestStichChecker()); // SKIPPED
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Expected IllegalStateException here
        try {
            stitcher.stitch();
            fail();
        } catch (IllegalStateException e) {
            // As expected
        }
    }

}
