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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the methods in the <tt>AbstractStitcher</tt> and <tt>BruteStitcher</tt> classes.
 */
public class AbstractStitcherAndBruteStitcherTest {

    // // Alternative in case an ad-hoc implemented class is to be used
    // private class TestAbstractStitcher extends AbstractStitcher<Integer, String> {
    //
    // @Override
    // protected Map<Integer, Collection<String>> subStitch(Collection<Integer>
    // filteredBaseElements,
    // Collection<String> filteredCandidateElements) {
    //
    // // Create Map where stitches will be saved
    // Map<Integer, Collection<String>> stitches = new HashMap<Integer, Collection<String>>();
    // for (Integer b : filteredBaseElements) {
    // stitches.put(b, new ArrayList<String>());
    // }
    //
    // for (Integer b : filteredBaseElements) {
    // for (String c : filteredCandidateElements) {
    // if (stitchChecker.checkStitch(b, c) == 1.0) {
    // // Match between this objects
    // stitches.get(b).add(c); // Add element to stitch table
    // }
    // }
    // }
    //
    // return stitches;
    // }
    //
    // }

    static class TestAbstractStitcher extends BruteStitcher<Integer, String> {
    }

    static class TestStichCheckerLenght implements StitchChecker<Integer, String> {

        @Override
        public double checkStitch(Integer baseElement, String candidateElement) {
            // If the candidate string's length and base element are the same -> Match
            return ((candidateElement.length() == baseElement) ? 1.0 : 0.0);
        }
    }

    static class TestStichCheckerTenMinusLength implements StitchChecker<Integer, String> {

        @Override
        public double checkStitch(Integer baseElement, String candidateElement) {
            // If the candidate string's length and base element are the same -> Match
            return ((10 - candidateElement.length() == baseElement) ? 1.0 : 0.0);
        }
    }

    static class TestFilterOnlyEvenNumbers implements Filter<Integer> {

        @Override
        public boolean filter(Integer element) {
            return (element % 2 != 0);
        }

        @Override
        public Collection<Integer> filter(Collection<Integer> elements) {
            Collection<Integer> resColl = new ArrayList<Integer>();

            for (Integer element : elements) {
                if (!filter(element)) {
                    resColl.add(element);
                }
            }

            return resColl;
        }
    }

    static class TestFilterOnlyNumbersAboveFive implements Filter<Integer> {

        @Override
        public boolean filter(Integer element) {
            return (element <= 5);
        }

        @Override
        public Collection<Integer> filter(Collection<Integer> elements) {
            Collection<Integer> resColl = new ArrayList<Integer>();

            for (Integer element : elements) {
                if (!filter(element)) {
                    resColl.add(element);
                }
            }

            return resColl;
        }
    }

    static class TestFilterOnlyShortStrings implements Filter<String> {

        // NOTE: Short strings will be those whose length is up to six characters

        @Override
        public boolean filter(String element) {
            return (element.length() > 6);
        }

        @Override
        public Collection<String> filter(Collection<String> elements) {
            Collection<String> resColl = new ArrayList<String>();

            for (String element : elements) {
                if (!filter(element)) {
                    resColl.add(element);
                }
            }

            return resColl;
        }
    }

    static class TestFilterOnlyStringsWithoutLetterA implements Filter<String> {

        @Override
        public boolean filter(String element) {
            return (element.contains("A") || element.contains("a"));
        }

        @Override
        public Collection<String> filter(Collection<String> elements) {
            Collection<String> resColl = new ArrayList<String>();

            for (String element : elements) {
                if (!filter(element)) {
                    resColl.add(element);
                }
            }

            return resColl;
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

    @SuppressWarnings("unused")
    @Test
    public void testConstructor() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
    }

    @Test
    public void testAddBaseElements() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        // No elements in the stitcher yet
        assertEquals(stitcher.baseElements.size(), 0);

        Collection<Integer> elements = new ArrayList<Integer>();
        for (int i = 0; i < 50; i += 10) {
            elements.add(i);
        }

        stitcher.addBaseElements(elements);

        // All elements have been added correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.baseElements);
    }

    @Test
    public void testRemoveBaseElements() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        Collection<Integer> elements = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            elements.add(i);
        }

        stitcher.addBaseElements(elements);

        // All elements have been added correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.baseElements);

        // Base element list contains all integers between 0 and 9.
        // Remove the even ones
        elements.clear();
        for (int i = 0; i < 10; i += 2) {
            elements.add(i);
        }
        stitcher.removeBaseElements(elements);

        // Final contents should be: 1, 3, 5, 7, 9
        elements.clear();
        for (int i = 1; i < 10; i += 2) {
            elements.add(i);
        }

        // All elements have been deleted correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.baseElements);
    }

    @Test
    public void testClearBaseElements() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        Collection<Integer> elements = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            elements.add(i);
        }

        stitcher.addBaseElements(elements);

        // All elements have been added correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.baseElements);

        // Clear base elements
        stitcher.clearBaseElements();
        assertTrue(stitcher.baseElements.isEmpty());
    }

    @Test
    public void testAddCandidateElements() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        // No elements in the stitcher yet
        assertEquals(stitcher.candidateElements.size(), 0);

        Collection<String> elements = new ArrayList<String>();
        elements.add("One");
        elements.add("Two");
        elements.add("Three");
        elements.add("Four");
        elements.add("Five");

        stitcher.addCandidateElements(elements);

        // All elements have been added correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.candidateElements);
    }

    @Test
    public void testRemoveCandidateElements() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        Collection<String> elements = new ArrayList<String>();
        elements.add("One");
        elements.add("Two");
        elements.add("Three");
        elements.add("Four");
        elements.add("Five");

        stitcher.addCandidateElements(elements);

        // All elements have been added correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.candidateElements);

        // Remove "One", "Three", and "Five"
        elements.clear();
        elements.add("One");
        elements.add("Three");
        elements.add("Five");
        stitcher.removeCandidateElements(elements);

        // Final contents should be: "Two", "Four"
        elements.clear();
        elements.add("Two");
        elements.add("Four");

        // All elements have been deleted correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.candidateElements);
    }

    @Test
    public void testClearCandidateElements() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        Collection<String> elements = new ArrayList<String>();
        elements.add("One");
        elements.add("Two");
        elements.add("Three");
        elements.add("Four");
        elements.add("Five");

        stitcher.addCandidateElements(elements);

        // All elements have been added correctly
        StitcherTestsUtils.checkSameElements(elements, stitcher.candidateElements);

        // Clear base elements
        stitcher.clearCandidateElements();
        assertTrue(stitcher.candidateElements.isEmpty());
    }

    @Test
    public void testSetAndGetStitchChecker() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        StitchChecker<Integer, String> stitchChecker1 = new TestStichCheckerLenght();
        StitchChecker<Integer, String> stitchChecker2 = new StitchChecker<Integer, String>() {
            @Override
            public double checkStitch(Integer baseElement, String candidateElement) {
                return 0.0;
            }
        };

        // Add first stitch checker
        stitcher.addStitchChecker(stitchChecker1);
        assertEquals(1, stitcher.getStitchCheckers().size());
        assertTrue(stitcher.getStitchCheckers().contains(stitchChecker1));
        assertFalse(stitcher.getStitchCheckers().contains(stitchChecker2));

        // Add second stitch checker
        stitcher.addStitchChecker(stitchChecker2);
        assertEquals(2, stitcher.getStitchCheckers().size());
        assertTrue(stitcher.getStitchCheckers().contains(stitchChecker1));
        assertTrue(stitcher.getStitchCheckers().contains(stitchChecker2));

        // Remove first stitch checker
        stitcher.removeStitchChecker(stitchChecker1);
        assertEquals(1, stitcher.getStitchCheckers().size());
        assertFalse(stitcher.getStitchCheckers().contains(stitchChecker1));
        assertTrue(stitcher.getStitchCheckers().contains(stitchChecker2));

        // Remove second stitch checker
        stitcher.removeStitchChecker(stitchChecker2);
        assertEquals(0, stitcher.getStitchCheckers().size());
        assertFalse(stitcher.getStitchCheckers().contains(stitchChecker1));
        assertFalse(stitcher.getStitchCheckers().contains(stitchChecker2));
    }

    @Test
    public void testAddAndRemoveBaseFilter() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        Filter<Integer> baseFilter1 = new TestFilterOnlyEvenNumbers();
        Filter<Integer> baseFilter2 = new TestFilterOnlyNumbersAboveFive();

        // Base filter list is empty
        assertEquals(0, stitcher.bFilters.size());

        // Add first filter
        stitcher.addBaseFilter(baseFilter1);
        assertTrue(stitcher.bFilters.contains(baseFilter1));
        assertEquals(1, stitcher.bFilters.size());

        // Add second filter
        stitcher.addBaseFilter(baseFilter2);
        assertTrue(stitcher.bFilters.contains(baseFilter2));
        assertEquals(2, stitcher.bFilters.size());

        // Remove first filter
        stitcher.removeBaseFilter(baseFilter1);
        assertFalse(stitcher.bFilters.contains(baseFilter1));
        assertEquals(1, stitcher.bFilters.size());

        // Remove second filter
        stitcher.removeBaseFilter(baseFilter2);
        assertFalse(stitcher.bFilters.contains(baseFilter2));
        assertEquals(0, stitcher.bFilters.size());
    }

    @Test
    public void testAddAndRemoveCandidateFilter() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        Filter<String> candidateFilter1 = new TestFilterOnlyShortStrings();
        Filter<String> candidateFilter2 = new TestFilterOnlyStringsWithoutLetterA();

        // Base filter list is empty
        assertEquals(0, stitcher.cFilters.size());

        // Add first filter
        stitcher.addCandidateFilter(candidateFilter1);
        assertTrue(stitcher.cFilters.contains(candidateFilter1));
        assertEquals(1, stitcher.cFilters.size());

        // Add second filter
        stitcher.addCandidateFilter(candidateFilter2);
        assertTrue(stitcher.cFilters.contains(candidateFilter2));
        assertEquals(2, stitcher.cFilters.size());

        // Remove first filter
        stitcher.removeCandidateFilter(candidateFilter1);
        assertFalse(stitcher.cFilters.contains(candidateFilter1));
        assertEquals(1, stitcher.cFilters.size());

        // Remove second filter
        stitcher.removeCandidateFilter(candidateFilter2);
        assertFalse(stitcher.cFilters.contains(candidateFilter2));
        assertEquals(0, stitcher.cFilters.size());
    }

    // Stitching results should be:
    // <Empty map>
    @Test
    public void testFullStichingNoElementsWhatsoever() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
    // - 1: {}
    // - 2: {"Fluorite", "Corundum"}
    // - 3: {"Calcite", "Apatite", "Diamond"}
    // - 4: {"Talc", "Gypsum", "Quartz"}
    // - 5: {"Topaz"}
    // - 6: {"Gypsum", "Quartz, "Talc""}
    // - 7: {"Calcite", "Apatite", "Diamond"}
    // - 8: {"Fluorite", "Corundum"}
    // - 9: {}
    // - 10: {"Orthoclase"}
    @Test
    public void testFullStichingTwoStitchCheckers() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        stitcher.addStitchChecker(new TestStichCheckerLenght());
        stitcher.addStitchChecker(new TestStichCheckerTenMinusLength());
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected map of stitches
        Map<Integer, Collection<String>> expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 1; i <= 10; i++) {
            expectedStitches.put(i, new ArrayList<String>());
        }

        // Length (first StitchChecker)
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

        // Ten minus lenght (second StitchChecker)
        expectedStitches.get(6).add(TALC);
        // expectedStitches.get(5).add(TOPAZ); // Already included!
        expectedStitches.get(4).add(GYPSUM);
        expectedStitches.get(4).add(QUARTZ);
        expectedStitches.get(3).add(CALCITE);
        expectedStitches.get(3).add(APATITE);
        expectedStitches.get(3).add(DIAMOND);
        expectedStitches.get(2).add(FLUORITE);
        expectedStitches.get(2).add(CORUNDUM);
        // expectedStitches.get(0).add(ORTHOCLASE); // Position 0 does not exist!

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    @Test
    public void testFullStichingChangingStitchCheckers() {
        TestStichCheckerLenght testStichCheckerLenght = new TestStichCheckerLenght();
        TestStichCheckerTenMinusLength testStichCheckerTenMinusLength = new TestStichCheckerTenMinusLength();

        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        stitcher.addStitchChecker(testStichCheckerLenght);
        stitcher.addBaseElements(baseElements);
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Create expected map of stitches for first StitchChecker
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

        // First StitchChecker
        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());

        // Change StitchChecker
        stitcher.removeStitchChecker(testStichCheckerLenght);
        stitcher.addStitchChecker(testStichCheckerTenMinusLength);

        // Create expected map of stitches for second StitchChecker
        expectedStitches = new HashMap<Integer, Collection<String>>(10);
        for (int i = 1; i <= 10; i++) {
            expectedStitches.put(i, new ArrayList<String>());
        }
        expectedStitches.get(6).add(TALC);
        expectedStitches.get(5).add(TOPAZ);
        expectedStitches.get(4).add(GYPSUM);
        expectedStitches.get(4).add(QUARTZ);
        expectedStitches.get(3).add(CALCITE);
        expectedStitches.get(3).add(APATITE);
        expectedStitches.get(3).add(DIAMOND);
        expectedStitches.get(2).add(FLUORITE);
        expectedStitches.get(2).add(CORUNDUM);
        // expectedStitches.get(0).add(ORTHOCLASE); // Position 0 does not exist!

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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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


    // Stitching for base element "1" (to be filtered)
    // Stitching results should be: <null>
    @Test
    public void testSingleStitchingBaseElementFiltered() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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

    @Test
    public void testFullStitchingWithNoStitchChecker() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
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

    @Test
    public void testSingleStitchingWithNoStitchChecker() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        // stitcher.setStitchChecker(new TestStichChecker()); // SKIPPED
        // stitcher.addBaseElements(baseElements); // Not necessary
        stitcher.addCandidateElements(candidateElements);

        // Add filters
        // No filters to be added in this test

        // Expected IllegalStateException here
        try {
            stitcher.stitch(7);
            fail();
        } catch (IllegalStateException e) {
            // As expected
        }
    }

    @Test
    public void testAddBaseElementsNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        try {
            stitcher.addBaseElements(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testRemoveBaseElementsNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();
        try {
            stitcher.removeBaseElements(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testAddCandidateElementsNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        try {
            stitcher.addCandidateElements(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testRemoveCandidateElementsNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        try {
            stitcher.removeCandidateElements(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testAddBaseFilterNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        try {
            stitcher.addBaseFilter(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testAddCandidateFiltertNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        try {
            stitcher.addCandidateFilter(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testSetStitchCheckerNullArg() {
        TestAbstractStitcher stitcher = new TestAbstractStitcher();

        try {
            stitcher.addStitchChecker(null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }
}
