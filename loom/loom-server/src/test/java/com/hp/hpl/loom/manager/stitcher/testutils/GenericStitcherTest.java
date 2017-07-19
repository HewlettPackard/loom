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
package com.hp.hpl.loom.manager.stitcher.testutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.manager.stitcher.StitcherUpdater;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.stitcher.StitcherRulePair;

public abstract class GenericStitcherTest {

    private static final Log LOG = LogFactory.getLog(GenericStitcherTest.class);

    // CLASSES -------------------------------------------------------------------------------------

    abstract class PolygonAreaComparator<Src extends ColouredPolygon, Dest extends ColouredPolygon>
            extends PolygonMeasureComparator<Src, Dest> {
        public PolygonAreaComparator() {
            super(PolygonMeasureComparator::getArea, delta, useIndex);
        }

        @Override
        public String indexKey() {
            return AREA_KEY;
        }

        @Override
        public String otherIndexKey() {
            return AREA_KEY;
        }
    }

    class PolygonComparatorCircleTriangleArea extends PolygonAreaComparator<Circle, Triangle> {

        @Override
        public String sourceTypeId() {
            return CIRCLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return TRIANGLE_TYPE_ID;
        }
    }

    class PolygonComparatorTriangleCircleArea extends PolygonAreaComparator<Triangle, Circle> {

        @Override
        public String sourceTypeId() {
            return TRIANGLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return CIRCLE_TYPE_ID;
        }

        @Override
        public String indexKey() {
            return AREA_KEY;
        }

        @Override
        public String otherIndexKey() {
            return AREA_KEY;
        }
    }

    class PolygonComparatorCircleSquareArea extends PolygonAreaComparator<Circle, Square> {

        @Override
        public String sourceTypeId() {
            return CIRCLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return SQUARE_TYPE_ID;
        }

        @Override
        public String indexKey() {
            return AREA_KEY;
        }

        @Override
        public String otherIndexKey() {
            return AREA_KEY;
        }
    }

    class PolygonComparatorSquareCircleArea extends PolygonAreaComparator<Square, Circle> {

        @Override
        public String sourceTypeId() {
            return SQUARE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return CIRCLE_TYPE_ID;
        }
    }

    class PolygonComparatorSquareTriangleArea extends PolygonAreaComparator<Square, Triangle> {

        @Override
        public String sourceTypeId() {
            return SQUARE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return TRIANGLE_TYPE_ID;
        }
    }

    class PolygonComparatorTriangleSquareArea extends PolygonAreaComparator<Triangle, Square> {

        @Override
        public String sourceTypeId() {
            return TRIANGLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return SQUARE_TYPE_ID;
        }
    }

    class PolygonComparatorCircleTriangleColour extends PolygonColourComparator<Circle, Triangle> {

        @Override
        public String sourceTypeId() {
            return CIRCLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return TRIANGLE_TYPE_ID;
        }

        PolygonComparatorCircleTriangleColour() {
            super(useIndex);
        }
    }

    class PolygonComparatorTriangleCircleColour extends PolygonColourComparator<Triangle, Circle> {

        @Override
        public String sourceTypeId() {
            return TRIANGLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return CIRCLE_TYPE_ID;
        }

        public PolygonComparatorTriangleCircleColour() {
            super(useIndex);
        }
    }

    class PolygonComparatorCircleSquareColour extends PolygonColourComparator<Circle, Square> {

        @Override
        public String sourceTypeId() {
            return CIRCLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return SQUARE_TYPE_ID;
        }

        public PolygonComparatorCircleSquareColour() {
            super(useIndex);
        }
    }

    class PolygonComparatorSquareCircleColour extends PolygonColourComparator<Square, Circle> {

        @Override
        public String sourceTypeId() {
            return SQUARE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return CIRCLE_TYPE_ID;
        }

        public PolygonComparatorSquareCircleColour() {
            super(useIndex);
        }
    }

    class PolygonComparatorSquareTriangleColour extends PolygonColourComparator<Square, Triangle> {

        @Override
        public String sourceTypeId() {
            return SQUARE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return TRIANGLE_TYPE_ID;
        }

        public PolygonComparatorSquareTriangleColour() {
            super(useIndex);
        }
    }

    class PolygonComparatorTriangleSquareColour extends PolygonColourComparator<Triangle, Square> {

        @Override
        public String sourceTypeId() {
            return TRIANGLE_TYPE_ID;
        }

        @Override
        public String otherTypeId() {
            return SQUARE_TYPE_ID;
        }

        public PolygonComparatorTriangleSquareColour() {
            super(useIndex);
        }
    }

    // CLASSES - END -------------------------------------------------------------------------------

    // VARIABLES AND CONSTANTS ---------------------------------------------------------------------

    static final boolean modifiedItemsIncludeSources = false;

    static final double delta = 1E-6;

    static final String CIRCLE = "Circle";
    static final String SQUARE = "Square";
    static final String TRIANGLE = "Triangle";

    static final String CIRCLE_PROVIDER = CIRCLE + ' ' + "Provider";
    static final String SQUARE_PROVIDER = SQUARE + ' ' + "Provider";
    static final String TRIANGLE_PROVIDER = TRIANGLE + ' ' + "Provider";

    static final String AREA_KEY = "area";

    static final String CIRCLE_TYPE_ID = "TestCircleType";
    static final String SQUARE_TYPE_ID = "TestSquareType";
    static final String TRIANGLE_TYPE_ID = "TestTriangleType";

    static final String C_S_AREA_RULE = "CircleSquareArea";
    static final String C_S_COLOUR_RULE = "CircleSquareColour";
    static final String C_T_AREA_RULE = "CircleTriangleArea";
    static final String C_T_COLOUR_RULE = "CircleTriangleColour";
    static final String S_T_AREA_RULE = "SquareTriangleArea";
    static final String S_T_COLOUR_RULE = "SquareTriangleColour";

    static final String POLYGON_TYPES_1[] =
            {CIRCLE, CIRCLE, CIRCLE, SQUARE, SQUARE, SQUARE, TRIANGLE, TRIANGLE, TRIANGLE};
    static final double POLYGON_AREAS_1[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    static final Colour POLYGON_COLOURS_1[] = {Colour.CYAN, Colour.MAGENTA, Colour.YELLOW, Colour.YELLOW,
            Colour.MAGENTA, Colour.CYAN, Colour.CYAN, Colour.CYAN, Colour.CYAN};

    static final String POLYGON_TYPES_2[] =
            {CIRCLE, CIRCLE, CIRCLE, CIRCLE, CIRCLE, SQUARE, SQUARE, SQUARE, TRIANGLE, TRIANGLE};
    static final double POLYGON_AREAS_2[] = {4.0, 2.0, 2.0, 6.0, 2.0, 4.0, 5.0, 6.0, 6.0, 6.0};
    static final Colour POLYGON_COLOURS_2[] = {Colour.CYAN, Colour.YELLOW, Colour.WHITE, Colour.WHITE, Colour.MAGENTA,
            Colour.BLACK, Colour.CYAN, Colour.YELLOW, Colour.MAGENTA, Colour.MAGENTA};

    static CircleType circleType;
    static SquareType squareType;
    static TriangleType triangleType;

    protected boolean useIndex = false;

    private Map<String, ColouredPolygon> polygons;
    private Map<String, StitcherRulePair<? extends ColouredPolygon, ? extends ColouredPolygon>> stitchingRules;


    // VARIABLES AND CONSTANTS - END ---------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    public GenericStitcherTest() {
        LOG.info("GenericStitcherTest useIndex=" + useIndex);

        circleType = new CircleType(CIRCLE_TYPE_ID);
        circleType.setId(CIRCLE_TYPE_ID);

        squareType = new SquareType(SQUARE_TYPE_ID);
        squareType.setId(SQUARE_TYPE_ID);

        triangleType = new TriangleType(TRIANGLE_TYPE_ID);
        triangleType.setId(TRIANGLE_TYPE_ID);
    }

    @Before
    public void setUp() {
        if (stitchingRules == null) {
            LOG.info("Setup generic test useIndex=" + useIndex);
            stitchingRules = generateRulePairs();
        }
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Test
    public void testPolygons() {

        // -----------------------------------------------------------------------------------------
        // This test is not related to the stitcher itself. This test checks that the implemented
        // polygons (helper classes for testing) work as expected.
        // -----------------------------------------------------------------------------------------

        // Constants
        double expectedArea, expectedPerimeter, expectedRatio, expectedSide;

        // Create polygons
        Circle circle = new Circle("TestCircle", circleType);
        Square square = new Square("TestSquare", squareType);
        Triangle triangle = new Triangle("TestTriangle", triangleType);

        // Test circle
        circle.setRefLenght(1.0);
        expectedRatio = 1.0;
        expectedArea = circleArea(expectedRatio);
        expectedPerimeter = circlePerimeter(expectedRatio);
        verifyValues(circle, expectedRatio, expectedArea, expectedPerimeter);

        circle.setRefLenght(2.0);
        expectedRatio = 2.0;
        expectedArea = circleArea(expectedRatio);
        expectedPerimeter = circlePerimeter(expectedRatio);
        verifyValues(circle, expectedRatio, expectedArea, expectedPerimeter);

        circle.setArea(10.0);
        expectedRatio = Math.sqrt(10.0 / Math.PI);
        expectedArea = 10.0;
        expectedPerimeter = circlePerimeter(expectedRatio);
        verifyValues(circle, expectedRatio, expectedArea, expectedPerimeter);

        circle.setPerimeter(5.0);
        expectedRatio = 5.0 / (2.0 * Math.PI);
        expectedArea = circleArea(expectedRatio);
        expectedPerimeter = 5.0;
        verifyValues(circle, expectedRatio, expectedArea, expectedPerimeter);

        // Test square
        square.setRefLenght(1.0);
        expectedSide = 1.0;
        expectedArea = squareArea(expectedSide);
        expectedPerimeter = squarePerimeter(expectedSide);
        verifyValues(square, expectedSide, expectedArea, expectedPerimeter);

        square.setRefLenght(2.0);
        expectedSide = 2.0;
        expectedArea = squareArea(expectedSide);
        expectedPerimeter = squarePerimeter(expectedSide);
        verifyValues(square, expectedSide, expectedArea, expectedPerimeter);

        square.setArea(9.0);
        expectedSide = 3.0;
        expectedArea = 9.0;
        expectedPerimeter = squarePerimeter(expectedSide);
        verifyValues(square, expectedSide, expectedArea, expectedPerimeter);

        square.setPerimeter(40.0);
        expectedSide = 10.0;
        expectedArea = squareArea(expectedSide);
        expectedPerimeter = 40.0;
        verifyValues(square, expectedSide, expectedArea, expectedPerimeter);

        // Test triangle
        triangle.setRefLenght(1.0);
        expectedSide = 1.0;
        expectedArea = triangleArea(expectedSide);
        expectedPerimeter = trianglePerimeter(expectedSide);
        verifyValues(triangle, expectedSide, expectedArea, expectedPerimeter);

        triangle.setRefLenght(2.0);
        expectedSide = 2.0;
        expectedArea = triangleArea(expectedSide);
        expectedPerimeter = trianglePerimeter(expectedSide);
        verifyValues(triangle, expectedSide, expectedArea, expectedPerimeter);

        triangle.setArea(8.0);
        expectedSide = 4.0;
        expectedArea = 8.0;
        expectedPerimeter = trianglePerimeter(expectedSide);
        verifyValues(triangle, expectedSide, expectedArea, expectedPerimeter);

        triangle.setPerimeter(10.0);
        expectedSide = 10.0 / (1 + Math.sqrt(5.0));
        expectedArea = triangleArea(expectedSide);
        expectedPerimeter = 10.0;
        verifyValues(triangle, expectedSide, expectedArea, expectedPerimeter);
    }

    @Test
    public void testNullArguments() {

        // -----------------------------------------------------------------------------------------
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);

        // -----------------------------------------------------------------------------------------
        // Setting up sceneario (1/2): Adding circles (C1, C2, C3)
        // -----------------------------------------------------------------------------------------
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (2/2): Adding squares (S4, S5, S6)
        // -----------------------------------------------------------------------------------------
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        // -----------------------------------------------------------------------------------------
        // Test behaviour with null arguments (exceptions expected)
        // -----------------------------------------------------------------------------------------
        // Argument 1/6
        try {
            stitcherSession.stitchItems(null, SQUARE_PROVIDER, allSquares, newItems, updatedItems, deletedItems);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        // Argument 2/6
        try {
            stitcherSession.stitchItems(SQUARE_TYPE_ID, null, allSquares, newItems, updatedItems, deletedItems);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        // Argument 3/6
        try {
            stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, null, newItems, updatedItems, deletedItems);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        // // Argument 4/6
        // try {
        // stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, null,
        // updatedItems, deletedItems);
        // Assert.fail();
        // } catch (IllegalArgumentException e) {
        // // As expected
        // }
        //
        // // Argument 5/6
        // try {
        // stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, null,
        // deletedItems);
        // Assert.fail();
        // } catch (IllegalArgumentException e) {
        // // As expected
        // }
        //
        // // Argument 6/6
        // try {
        // stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems,
        // updatedItems, null);
        // Assert.fail();
        // } catch (IllegalArgumentException e) {
        // // As expected
        // }
    }

    @Test
    public void testAddingAndDeletingOrderDoesNotMatter1() {

        // -----------------------------------------------------------------------------------------
        // This test and the next one will check that the order in which the items are added to the
        // stitcher makes no difference. At the end of both tests, the same stitches will exist.
        // Test 1: Add circles, then add squares.
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);

        // -----------------------------------------------------------------------------------------
        // Adding circles (C1, C2, C3)
        // -----------------------------------------------------------------------------------------
        state = "Adding circles (C1 C2 C3)";
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding squares (S4, S5, S6)
        // -----------------------------------------------------------------------------------------
        state = "Adding squares (S4 S5 S6)";
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, S4, S5, S6
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1, C2, C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - NEW!
        // - C2 <---> S5 - NEW!
        // - C3 <---> S4 - NEW!
        verifyStitches(state, "C1-S6  C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Removing C1
        // -----------------------------------------------------------------------------------------
        state = "Removing C1";
        updateCollections(newItems, "", updatedItems, "", deletedItems, "C1", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, S6
            verifyExpectedModifiedElements(state, "C1 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): S6
            verifyExpectedModifiedElements(state, "S6", modifiedItems);
        }

        // Stitches:
        // - C1 <· ·> S6 - DELETED!
        // - C2 <---> S5
        // - C3 <---> S4
        verifyStitches(state, "C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Removing S6
        // -----------------------------------------------------------------------------------------
        state = "Removing S6";
        updateCollections(newItems, "S6", updatedItems, "", deletedItems, "S6", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): S6
            verifyExpectedModifiedElements(state, "S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S5
        // - C3 <---> S4
        verifyStitches(state, "C2-S5  C3-S4", (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testAddingAndDeletingOrderDoesNotMatter2() {

        // -----------------------------------------------------------------------------------------
        // This test and the previous one will check that the order in which the items are added to
        // the stitcher makes no difference. At the end of both tests, the same stitches will exist.
        // Test 2: Add squares, then add circle.
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);

        // -----------------------------------------------------------------------------------------
        // Adding squares (S4, S5, S6)
        // -----------------------------------------------------------------------------------------
        state = "Adding squares (S4 S5 S6)";
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): S4, S5, S6
            verifyExpectedModifiedElements(state, "S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding circles (C1, C2, C3)
        // -----------------------------------------------------------------------------------------
        state = "Adding circles (C1 C2 C3)";
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, S4, S5, S6
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): S4, S5, S6
            verifyExpectedModifiedElements(state, "S4 S5 S6", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - NEW!
        // - C2 <---> S5 - NEW!
        // - C3 <---> S4 - NEW!
        verifyStitches(state, "C1-S6  C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Removing S6
        // -----------------------------------------------------------------------------------------
        state = "Removing S6";
        updateCollections(newItems, "", updatedItems, "", deletedItems, "S6", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, S6
            verifyExpectedModifiedElements(state, "C1 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1
            verifyExpectedModifiedElements(state, "C1", modifiedItems);
        }

        // Stitches:
        // - C1 <· ·> S6 - DELETED!
        // - C2 <---> S5
        // - C3 <---> S4
        verifyStitches(state, "C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Removing C1
        // -----------------------------------------------------------------------------------------
        state = "Removing C1";
        updateCollections(newItems, "", updatedItems, "", deletedItems, "C1", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1
            verifyExpectedModifiedElements(state, "C1", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S5
        // - C3 <---> S4
        verifyStitches(state, "C2-S5  C3-S4", (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testDifferentColoursPerType() {

        // -----------------------------------------------------------------------------------------
        // Each polygon type will have a different colour. No stitches will be found.
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);

        // -----------------------------------------------------------------------------------------
        // Adding circles (C1, C2, C3) and colouring them cyan
        // -----------------------------------------------------------------------------------------
        state = "Adding circles (C1, C2, C3) and colouring them cyan";
        retrievePolygon("C1").setColour(Colour.CYAN);
        retrievePolygon("C2").setColour(Colour.CYAN);
        retrievePolygon("C3").setColour(Colour.CYAN);
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding squares (S4, S5, S6) and colouring them magenta
        // -----------------------------------------------------------------------------------------
        state = "Adding squares (S4, S5, S6) and colouring them magenta";
        retrievePolygon("S4").setColour(Colour.MAGENTA);
        retrievePolygon("S5").setColour(Colour.MAGENTA);
        retrievePolygon("S6").setColour(Colour.MAGENTA);
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): S4, S5, S6
            verifyExpectedModifiedElements(state, "S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding triangles (T7, T8, T9) and colouring them yellow
        // -----------------------------------------------------------------------------------------
        state = "Adding triangles (T7, T8, T9) and colouring them yellow";
        retrievePolygon("T7").setColour(Colour.YELLOW);
        retrievePolygon("T8").setColour(Colour.YELLOW);
        retrievePolygon("T9").setColour(Colour.YELLOW);
        updateCollections(newItems, "T7 T8 T9", updatedItems, "", deletedItems, "", allTriangles);
        modifiedItems = stitcherSession.stitchItems(TRIANGLE_TYPE_ID, TRIANGLE_PROVIDER, allTriangles, newItems,
                updatedItems, deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): T7, T8, T9
            verifyExpectedModifiedElements(state, "T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testAllTypesSameColourTwoTypes() {

        // -----------------------------------------------------------------------------------------
        // All the circles in this test will be stitched to all the squares and all the triangles
        // At the end of the test, there will be 18 stitches
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);
        retrievePolygon("C1").setColour(Colour.WHITE);
        retrievePolygon("C2").setColour(Colour.WHITE);
        retrievePolygon("C3").setColour(Colour.WHITE);
        retrievePolygon("S4").setColour(Colour.WHITE);
        retrievePolygon("S5").setColour(Colour.WHITE);
        retrievePolygon("S6").setColour(Colour.WHITE);
        retrievePolygon("T7").setColour(Colour.WHITE);
        retrievePolygon("T8").setColour(Colour.WHITE);
        retrievePolygon("T9").setColour(Colour.WHITE);

        // -----------------------------------------------------------------------------------------
        // Adding squares (S4, S5, S6), no stitches found yet
        // -----------------------------------------------------------------------------------------
        state = "Adding squares (S4, S5, S6), no stitches found yet";
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): S4, S5, S6
            verifyExpectedModifiedElements(state, "S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding triangles (T7, T8, T9), no stitches found yet
        // -----------------------------------------------------------------------------------------
        state = "Adding triangles (T7, T8, T9), stitches found yet";
        updateCollections(newItems, "T7 T8 T9", updatedItems, "", deletedItems, "", allTriangles);
        modifiedItems = stitcherSession.stitchItems(TRIANGLE_TYPE_ID, TRIANGLE_PROVIDER, allTriangles, newItems,
                updatedItems, deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): T7, T8, T9
            verifyExpectedModifiedElements(state, "T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding circles (C1, C2, C3), each circle connected to all squares and triangles
        // -----------------------------------------------------------------------------------------
        state = "Adding circles (C1, C2, C3), each circle connected to all squares and triangles";
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, S4, S5, S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6 T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): S4, S5, S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "S4 S5 S6 T7 T8 T9", modifiedItems);
        }

        // Stitches (for each circle, 18 bidirectional stitches total amount):
        // - CX <---> S4 - NEW!
        // - CX <---> S5 - NEW!
        // - CX <---> S6 - NEW!
        // - CX <---> T7 - NEW!
        // - CX <---> T8 - NEW!
        // - CX <---> T9 - NEW!
        verifyStitches(state, "C1-S4  C1-S5  C1-S6  C1-T7  C1-T8  C1-T9  "
                + "C2-S4  C2-S5  C2-S6  C2-T7  C2-T8  C2-T9  " + "C3-S4  C3-S5  C3-S6  C3-T7  C3-T8  C3-T9",
                (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testAllTypesSameColourThreeTypes() {

        // -----------------------------------------------------------------------------------------
        // All the circles, squares and triangles in this test will have the same colour.
        // At the end of the test, there will be 27 stitches
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));
        ruleManager.addStitcherRulePair(S_T_COLOUR_RULE, stitchingRules.get(S_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);
        retrievePolygon("C1").setColour(Colour.WHITE);
        retrievePolygon("C2").setColour(Colour.WHITE);
        retrievePolygon("C3").setColour(Colour.WHITE);
        retrievePolygon("S4").setColour(Colour.WHITE);
        retrievePolygon("S5").setColour(Colour.WHITE);
        retrievePolygon("S6").setColour(Colour.WHITE);
        retrievePolygon("T7").setColour(Colour.WHITE);
        retrievePolygon("T8").setColour(Colour.WHITE);
        retrievePolygon("T9").setColour(Colour.WHITE);

        // -----------------------------------------------------------------------------------------
        // Adding circles (C1, C2, C3)
        // -----------------------------------------------------------------------------------------
        state = "Adding circles (C1, C2, C3)";
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding squares (S4, S5, S6)
        // -----------------------------------------------------------------------------------------
        state = "Adding squares (S4, S5, S6)";
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, S4, S5, S6
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1 C2 C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        }

        // Stitches:
        // - CX <---> S4 - NEW!
        // - CX <---> S5 - NEW!
        // - CX <---> S6 - NEW!
        verifyStitches(state, "C1-S4  C1-S5  C1-S6  C2-S4  C2-S5  C2-S6  C3-S4  C3-S5  C3-S6",
                (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding triangles (T7, T8, T9). Fully stitched.
        // -----------------------------------------------------------------------------------------
        state = "Adding triangles (T7, T8, T9). Fully stitched.";
        updateCollections(newItems, "T7 T8 T9", updatedItems, "", deletedItems, "", allTriangles);
        modifiedItems = stitcherSession.stitchItems(TRIANGLE_TYPE_ID, TRIANGLE_PROVIDER, allTriangles, newItems,
                updatedItems, deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, S4, S5, S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6 T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1, C2, C3, S4, S5, S6
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6", modifiedItems);
        }

        // Stitches:
        // - CX <---> S4
        // - CX <---> S5
        // - CX <---> S6
        // - CX <---> T7 - NEW!
        // - CX <---> T8 - NEW!
        // - CX <---> T9 - NEW!
        // - SX <---> T7 - NEW!
        // - SX <---> T8 - NEW!
        // - SX <---> T9 - NEW!
        String fullyStitched = "";
        fullyStitched += "C1-S4  C1-S5  C1-S6  C2-S4  C2-S5  C2-S6  C3-S4  C3-S5  C3-S6  ";
        fullyStitched += "C1-T7  C1-T8  C1-T9  C2-T7  C2-T8  C2-T9  C3-T7  C3-T8  C3-T9  ";
        fullyStitched += "S4-T7  S4-T8  S4-T9  S5-T7  S5-T8  S5-T9  S6-T7  S6-T8  S6-T9  ";
        verifyStitches(state, fullyStitched, (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testModifyItemsNotChangingStitches() {

        // -----------------------------------------------------------------------------------------
        // In this test, some items will be modified although this will NOT imply that their
        // stitches change. The purpose of this test is to check that the list of modified items
        // returned by the stitchItems(...) method reflects only those items whose stitches have
        // actually changed.
        // All the returned modified items lists in this test should be empty.
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_S_AREA_RULE, stitchingRules.get(C_S_AREA_RULE));
        ruleManager.addStitcherRulePair(C_T_AREA_RULE, stitchingRules.get(C_T_AREA_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);

        // -----------------------------------------------------------------------------------------
        // Setting up circles and adding them (C2 C3)
        // -----------------------------------------------------------------------------------------
        state = "Setting up circles and adding them (C2 C3)";
        retrievePolygon("C2").setColour(Colour.MAGENTA);
        retrievePolygon("C2").setArea(2.0);
        retrievePolygon("C3").setColour(Colour.YELLOW);
        retrievePolygon("C3").setArea(1.0);
        updateCollections(newItems, "C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C2, C3
            verifyExpectedModifiedElements(state, "C2 C3", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Setting up squares and adding them (S4 S5)
        // -----------------------------------------------------------------------------------------
        state = "Setting up squares and adding them (S4 S5)";
        retrievePolygon("S4").setColour(Colour.YELLOW);
        retrievePolygon("S4").setArea(2.0);
        retrievePolygon("S5").setColour(Colour.MAGENTA);
        retrievePolygon("S5").setArea(1.0);
        updateCollections(newItems, "S4 S5", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C2, C3, S4, S5
            verifyExpectedModifiedElements(state, "C2 C3 S4 S5", modifiedItems);
        } else {
            // Modified items (only destination stitches): C2, C3
            verifyExpectedModifiedElements(state, "C2 C3", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S4 - NEW! - Info: Same area (2.0)
        // - C2 <---> S5 - NEW! - Info: Same colour (magenta)
        // - C3 <---> S4 - NEW! - Info: Same colour (yellow)
        // - C3 <---> S5 - NEW! - Info: Same area (1.0)
        verifyStitches(state, "C2-S4  C2-S5  C3-S4  C3-S5", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // 1st Update: circles (stitches should not be modified)
        // -----------------------------------------------------------------------------------------
        state = "1st Update: circles (stitches should not be modified)";
        retrievePolygon("C2").setColour(Colour.YELLOW);
        retrievePolygon("C2").setArea(1.0);
        retrievePolygon("C3").setColour(Colour.MAGENTA);
        retrievePolygon("C3").setArea(2.0);
        updateCollections(newItems, "", updatedItems, "C2 C3", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S4 - Info: Same colour (yellow)
        // - C2 <---> S5 - Info: Same area (1.0)
        // - C3 <---> S4 - Info: Same area (2.0)
        // - C3 <---> S5 - Info: Same colour (magenta)
        verifyStitches(state, "C2-S4  C2-S5  C3-S4  C3-S5", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // 2nd Update: squares (stitches should not be modified)
        // -----------------------------------------------------------------------------------------
        state = "2nd Update: squares (stitches should not be modified)";
        retrievePolygon("S5").setColour(Colour.YELLOW);
        retrievePolygon("S5").setArea(2.0);
        updateCollections(newItems, "", updatedItems, "S5", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S4 - Info: Same colour (yellow)
        // - C2 <---> S5 - Info: Same colour (yellow)
        // - C3 <---> S4 - Info: Same area (2.0)
        // - C3 <---> S5 - Info: Same area (2.0)
        verifyStitches(state, "C2-S4  C2-S5  C3-S4  C3-S5", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // 3rd Update: circles (stitches should not be modified)
        // Reached this point, all four polygons are yellow and have an area of 2.0
        // -----------------------------------------------------------------------------------------
        state = "3rd Update: circles (stitches should not be modified)";
        retrievePolygon("C2").setArea(2.0);
        retrievePolygon("C3").setColour(Colour.YELLOW);
        updateCollections(newItems, "", updatedItems, "C2 C3", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // Stitches:
        // - C2 <===> S4 - Info: Same colour (yellow) and area (2.0)
        // - C2 <===> S5 - Info: Same colour (yellow) and area (2.0)
        // - C3 <===> S4 - Info: Same colour (yellow) and area (2.0)
        // - C3 <===> S5 - Info: Same colour (yellow) and area (2.0)
        verifyStitches(state, "C2-S4  C2-S5  C3-S4  C3-S5", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // 4th Update: squares (stitches should not be modified)
        // All four polygons have an area of two, but circles and squares have different colours
        // -----------------------------------------------------------------------------------------
        state = "4th Update: squares (stitches should not be modified)";
        retrievePolygon("S4").setColour(Colour.MAGENTA);
        retrievePolygon("S5").setColour(Colour.MAGENTA);
        updateCollections(newItems, "", updatedItems, "S4 S5", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S4 - Info: Same area (2.0)
        // - C2 <---> S5 - Info: Same area (2.0)
        // - C3 <---> S4 - Info: Same area (2.0)
        // - C3 <---> S5 - Info: Same area (2.0)
        verifyStitches(state, "C2-S4  C2-S5  C3-S4  C3-S5", (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testSeveralOperationsSequentially() {

        // -----------------------------------------------------------------------------------------
        // Test including several operations sequentially: adding, removing, adding again, and
        // modifying.
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 1
        polygons = generateInitialPolygons(POLYGON_TYPES_1, POLYGON_COLOURS_1, POLYGON_AREAS_1);

        // -----------------------------------------------------------------------------------------
        // Adding circles (C1, C2, C3)
        // -----------------------------------------------------------------------------------------
        state = "Adding circles (C1 C2 C3)";
        updateCollections(newItems, "C1 C2 C3", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding squares (S4, S5, S6)
        // -----------------------------------------------------------------------------------------
        state = "Adding squares (S4 S5 S6)";
        updateCollections(newItems, "S4 S5 S6", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, S4, S5, S6
            verifyExpectedModifiedElements(state, "C1 C2 C3 S4 S5 S6", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1, C2, C3
            verifyExpectedModifiedElements(state, "C1 C2 C3", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - NEW!
        // - C2 <---> S5 - NEW!
        // - C3 <---> S4 - NEW!
        verifyStitches(state, "C1-S6  C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding triangles (T7, T8, T9)
        // -----------------------------------------------------------------------------------------
        state = "Adding triangles (T7 T8 T9)";
        updateCollections(newItems, "T7 T8 T9", updatedItems, "", deletedItems, "", allTriangles);
        modifiedItems = stitcherSession.stitchItems(TRIANGLE_TYPE_ID, TRIANGLE_PROVIDER, allTriangles, newItems,
                updatedItems, deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, T7, T8, T9
            verifyExpectedModifiedElements(state, "C1 T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1
            verifyExpectedModifiedElements(state, "C1", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6
        // - C1 <---> T7 - NEW!
        // - C1 <---> T8 - NEW!
        // - C1 <---> T9 - NEW!
        // - C2 <---> S5
        // - C3 <---> S4
        verifyStitches(state, "C1-S6  C1-T7  C1-T8  C1-T9  C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Removing C1
        // -----------------------------------------------------------------------------------------
        state = "Removing C1";
        updateCollections(newItems, "", updatedItems, "", deletedItems, "C1", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "C1 S6 T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "S6 T7 T8 T9", modifiedItems);
        }

        // Stitches:
        // - C2 <---> S5
        // - C3 <---> S4
        // - C1 <· ·> S6 - DELETED!
        // - C1 <· ·> T7 - DELETED!
        // - C1 <· ·> T8 - DELETED!
        // - C1 <· ·> T9 - DELETED!
        verifyStitches(state, "C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding C1 again
        // -----------------------------------------------------------------------------------------
        state = "Adding C1 again";
        updateCollections(newItems, "C1", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "C1 S6 T7 T8 T9", modifiedItems);
        } else {
            // Modified items (only destination stitches): S6, T7, T8, T9
            verifyExpectedModifiedElements(state, "S6 T7 T8 T9", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - NEW!
        // - C1 <---> T7 - NEW!
        // - C1 <---> T8 - NEW!
        // - C1 <---> T9 - NEW!
        // - C2 <---> S5
        // - C3 <---> S4
        verifyStitches(state, "C1-S6  C1-T7  C1-T8  C1-T9  C2-S5  C3-S4", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Changing S5 colour to yellow
        // -----------------------------------------------------------------------------------------
        state = "Changing S5 colour to yellow";
        retrievePolygon("S5").setColour(Colour.YELLOW);
        updateCollections(newItems, "", updatedItems, "S5", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C2, C3, S5
            verifyExpectedModifiedElements(state, "C2 C3 S5", modifiedItems);
        } else {
            // Modified items (only destination stitches): C2, C3
            verifyExpectedModifiedElements(state, "C2 C3", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6
        // - C1 <---> T7
        // - C1 <---> T8
        // - C1 <---> T9
        // - C2 <· ·> S5 - DELETED!
        // - C3 <---> S4
        // - C3 <---> S5 - NEW!
        verifyStitches(state, "C1-S6  C1-T7  C1-T8  C1-T9  C3-S4  C3-S5", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Changing C2 colour to yellow
        // -----------------------------------------------------------------------------------------
        state = "Changing C2 colour to yellow";
        retrievePolygon("C2").setColour(Colour.YELLOW);
        updateCollections(newItems, "", updatedItems, "C2", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C2 S4 S5
            verifyExpectedModifiedElements(state, "C2 S4 S5", modifiedItems);
        } else {
            // Modified items (only destination stitches): S4 S5
            verifyExpectedModifiedElements(state, "S4 S5", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6
        // - C1 <---> T7
        // - C1 <---> T8
        // - C1 <---> T9
        // - C2 <---> S4 - NEW!
        // - C2 <---> S5 - NEW!
        // - C3 <---> S4
        // - C3 <---> S5
        verifyStitches(state, "C1-S6  C1-T7  C1-T8  C1-T9  C2-S4  C2-S5  C3-S4  C3-S5",
                (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testSeveralOperationsAtOnceTwoRelationships() {

        // -----------------------------------------------------------------------------------------
        // Test including several operations simultaneously
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_S_AREA_RULE, stitchingRules.get(C_S_AREA_RULE));
        ruleManager.addStitcherRulePair(C_T_AREA_RULE, stitchingRules.get(C_T_AREA_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 2
        polygons = generateInitialPolygons(POLYGON_TYPES_2, POLYGON_COLOURS_2, POLYGON_AREAS_2);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (1/3): Adding circles (C1, C2, C3, C4)
        // -----------------------------------------------------------------------------------------
        state = "Setting up scenario (1/3): Adding circles (C1, C2, C3, C4)";
        updateCollections(newItems, "C1 C2 C3 C4", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, C4
            verifyExpectedModifiedElements(state, "C1 C2 C3 C4", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (2/3): Adding squares (S6, S7, S8)
        // -----------------------------------------------------------------------------------------
        state = "Setting up scenario (2/3): Adding squares (S6, S7, S8)";
        updateCollections(newItems, "S6 S7 S8", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C4, S6, S7, S8
            verifyExpectedModifiedElements(state, "C1 C2 C4 S6 S7 S8", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1, C2, C4
            verifyExpectedModifiedElements(state, "C1 C2 C4", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - NEW! - Info: Same area (4.0)
        // - C1 <---> S7 - NEW! - Info: Same colour (cyan)
        // - C2 <---> S8 - NEW! - Info: Same colour (yellow)
        // - C4 <---> S8 - NEW! - Info: Same area (6.0)
        verifyStitches(state, "C1-S6 C1-S7 C2-S8 C4-S8", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (3/3): Adding triangles (T9, T10)
        // -----------------------------------------------------------------------------------------
        state = "Setting up scenario (3/3): Adding triangles (T9, T10)";
        updateCollections(newItems, "T9 T10", updatedItems, "", deletedItems, "", allTriangles);
        modifiedItems = stitcherSession.stitchItems(TRIANGLE_TYPE_ID, TRIANGLE_PROVIDER, allTriangles, newItems,
                updatedItems, deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C4, T9, T10
            verifyExpectedModifiedElements(state, "C4 T9 T10", modifiedItems);
        } else {
            // Modified items (only destination stitches): C4
            verifyExpectedModifiedElements(state, "C4", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - Info: Same area (4.0)
        // - C1 <---> S7 - Info: Same colour (cyan)
        // - C2 <---> S8 - Info: Same colour (yellow)
        // - C4 <---> S8 - Info: Same area (6.0)
        // - C4 <---> T9 - NEW! - Info: Same area (6.0)
        // - C4 <---> T10 - NEW! - Info: Same area (6.0)
        verifyStitches(state, "C1-S6 C1-S7 C2-S8 C4-S8 C4-T9 C4-T10", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding C5, deleting C4, updating C1 (now yellow)
        // -----------------------------------------------------------------------------------------
        state = "Adding C5, deleting C4, modifying C1 (now yellow)";
        retrievePolygon("C1").setColour(Colour.YELLOW);
        updateCollections(newItems, "C5", updatedItems, "C1", deletedItems, "C4", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C4, C5, S7, S8, T9, T10
            verifyExpectedModifiedElements(state, "C1 C4 C5 S7 S8 T9 T10", modifiedItems);
        } else {
            // Modified items (only destination stitches): S7, S8, T9, T10
            verifyExpectedModifiedElements(state, "S7 S8 T9 T10", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - Info: Same area (4.0)
        // - C1 <· ·> S7 - DELETED! - Info: No longer the same colour
        // - C1 <---> S8 - Info: Same colour (yellow)
        // - C2 <---> S8 - Info: Same colour (yellow)
        // - C4 <· ·> S8 - DELETED!
        // - C4 <· ·> T9 - DELETED!
        // - C4 <· ·> T10 - DELETED!
        // - C5 <---> T9 - NEW! - Info: Same colour (magenta)
        // - C5 <---> T10 - NEW! - Info: Same colour (magenta)
        verifyStitches(state, "C1-S6 C1-S8 C2-S8 C5-T9 C5-T10", (ItemEquivalence) stitcherSession);
    }

    @Test
    public void testSeveralOperationsAtOnceThreeRelationships() {

        // -----------------------------------------------------------------------------------------
        // Test including several operations simultaneously with all three item types connected to
        // each other (in the previous test squares and circles had no relationship).
        // -----------------------------------------------------------------------------------------

        StitcherRuleManager ruleManager;
        StitcherUpdater stitcherSession;
        Collection<Item> newItems, updatedItems, deletedItems, modifiedItems, allCircles, allSquares, allTriangles;
        String state;

        // Initialise rule manager
        ruleManager = createNewStitcherRuleManager();
        ruleManager.addStitcherRulePair(C_S_COLOUR_RULE, stitchingRules.get(C_S_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_T_COLOUR_RULE, stitchingRules.get(C_T_COLOUR_RULE));
        ruleManager.addStitcherRulePair(S_T_COLOUR_RULE, stitchingRules.get(S_T_COLOUR_RULE));
        ruleManager.addStitcherRulePair(C_S_AREA_RULE, stitchingRules.get(C_S_AREA_RULE));
        ruleManager.addStitcherRulePair(C_T_AREA_RULE, stitchingRules.get(C_T_AREA_RULE));
        ruleManager.addStitcherRulePair(S_T_AREA_RULE, stitchingRules.get(S_T_AREA_RULE));

        // Initialise stitcher
        stitcherSession = createNewStitcherSession(ruleManager);

        // Initialise collections to be used
        newItems = new ArrayList<Item>();
        updatedItems = new ArrayList<Item>();
        deletedItems = new ArrayList<Item>();
        allCircles = new ArrayList<Item>();
        allSquares = new ArrayList<Item>();
        allTriangles = new ArrayList<Item>();

        // Initialise polygons - Settings 2
        polygons = generateInitialPolygons(POLYGON_TYPES_2, POLYGON_COLOURS_2, POLYGON_AREAS_2);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (1/3): Adding circles (C1, C2, C3, C4)
        // -----------------------------------------------------------------------------------------
        state = "Setting up scenario (1/3): Adding circles (C1, C2, C3, C4)";
        updateCollections(newItems, "C1 C2 C3 C4", updatedItems, "", deletedItems, "", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, C4
            verifyExpectedModifiedElements(state, "C1 C2 C3 C4", modifiedItems);
        } else {
            // Modified items (only destination stitches): None
            verifyExpectedModifiedElements(state, "", modifiedItems);
        }

        // No stitches
        verifyStitches(state, "", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (2/3): Adding squares (S6, S7, S8)
        // -----------------------------------------------------------------------------------------
        state = "Setting up scenario (2/3): Adding squares (S6, S7, S8)";
        updateCollections(newItems, "S6 S7 S8", updatedItems, "", deletedItems, "", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C4, S6, S7, S8
            verifyExpectedModifiedElements(state, "C1 C2 C4 S6 S7 S8", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1, C2, C4
            verifyExpectedModifiedElements(state, "C1 C2 C4", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - NEW! - Info: Same area (4.0)
        // - C1 <---> S7 - NEW! - Info: Same colour (cyan)
        // - C2 <---> S8 - NEW! - Info: Same colour (yellow)
        // - C4 <---> S8 - NEW! - Info: Same area (6.0)
        verifyStitches(state, "C1-S6 C1-S7 C2-S8 C4-S8", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Setting up scenario (3/3): Adding triangles (T9, T10)
        // -----------------------------------------------------------------------------------------
        state = "Setting up scenario (3/3): Adding triangles (T9, T10)";
        updateCollections(newItems, "T9 T10", updatedItems, "", deletedItems, "", allTriangles);
        modifiedItems = stitcherSession.stitchItems(TRIANGLE_TYPE_ID, TRIANGLE_PROVIDER, allTriangles, newItems,
                updatedItems, deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C4, S8, T9, T10
            verifyExpectedModifiedElements(state, "C4 S8 T9 T10", modifiedItems);
        } else {
            // Modified items (only destination stitches): C4, S8
            verifyExpectedModifiedElements(state, "C4 S8", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - Info: Same area (4.0)
        // - C1 <---> S7 - Info: Same colour (cyan)
        // - C2 <---> S8 - Info: Same colour (yellow)
        // - C4 <---> S8 - Info: Same area (6.0)
        // - C4 <---> T9 - NEW! - Info: Same area (6.0)
        // - C4 <---> T10 - NEW! - Info: Same area (6.0)
        // - S8 <---> T9 - NEW! - Info: Same area (6.0)
        // - S8 <---> T10 - NEW! - Info: Same area (6.0)
        verifyStitches(state, "C1-S6 C1-S7 C2-S8 C4-S8 C4-T9 C4-T10 S8-T9 S8-T10", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Adding C5, deleting C4, updating C1 (now yellow)
        // -----------------------------------------------------------------------------------------
        state = "Adding C5, deleting C4, modifying C1 (now yellow)";
        retrievePolygon("C1").setColour(Colour.YELLOW);
        updateCollections(newItems, "C5", updatedItems, "C1", deletedItems, "C4", allCircles);
        modifiedItems = stitcherSession.stitchItems(CIRCLE_TYPE_ID, CIRCLE_PROVIDER, allCircles, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C4, C5, S7, S8, T9, T10
            verifyExpectedModifiedElements(state, "C1 C4 C5 S7 S8 T9 T10", modifiedItems);
        } else {
            // Modified items (only destination stitches): S7, S8, T9, T10
            verifyExpectedModifiedElements(state, "S7 S8 T9 T10", modifiedItems);
        }

        // Stitches:
        // - C1 <---> S6 - Info: Same area (4.0)
        // - C1 <· ·> S7 - DELETED! - Info: No longer the same colour
        // - C1 <---> S8 - Info: Same colour (yellow)
        // - C2 <---> S8 - Info: Same colour (yellow)
        // - C4 <· ·> S8 - DELETED!
        // - C4 <· ·> T9 - DELETED!
        // - C4 <· ·> T10 - DELETED!
        // - C5 <---> T9 - NEW! - Info: Same colour (magenta)
        // - C5 <---> T10 - NEW! - Info: Same colour (magenta)
        // - S8 <---> T9 - Info: Same area (6.0)
        // - S8 <---> T10 - Info: Same area (6.0)
        verifyStitches(state, "C1-S6 C1-S8 C2-S8 C5-T9 C5-T10 S8-T9 S8-T10", (ItemEquivalence) stitcherSession);

        // -----------------------------------------------------------------------------------------
        // Deleting S6, updating S7 and S8 (both now magenta and area=2.0)
        // -----------------------------------------------------------------------------------------
        state = "Deleting S6, updating S7 (now magenta and area=2.0)";
        retrievePolygon("S7").setColour(Colour.MAGENTA).setArea(2.0);
        retrievePolygon("S8").setColour(Colour.MAGENTA).setArea(2.0);
        updateCollections(newItems, "", updatedItems, "S7 S8", deletedItems, "S6", allSquares);
        modifiedItems = stitcherSession.stitchItems(SQUARE_TYPE_ID, SQUARE_PROVIDER, allSquares, newItems, updatedItems,
                deletedItems);

        if (modifiedItemsIncludeSources) {
            // Modified items (source and destination stitches): C1, C2, C3, C5, S6, S7, S8, T9, T10
            verifyExpectedModifiedElements(state, "C1 C2 C3 C5 S6 S7 S8 T9 T10", modifiedItems);
        } else {
            // Modified items (only destination stitches): C1, C2, C3, C5, T9, T10
            verifyExpectedModifiedElements(state, "C1 C2 C3 C5 T9 T10", modifiedItems);
        }

        // Stitches:
        // - C1 <· ·> S6 - DELETED!
        // - C1 <· ·> S8 - DELETED! - Info: No longer the same colour
        // - C2 <---> S7 - NEW! - Info: Same area (2.0)
        // - C2 <---> S8 - Info: Same area (2.0) (Notice it used to be colour before)
        // - C3 <---> S7 - NEW! - Info: Same area (2.0)
        // - C3 <---> S8 - NEW! - Info: Same area (2.0)
        // - C5 <===> S7 - NEW! - Info: Same colour (mangenta) and area (2.0)
        // - C5 <===> S8 - NEW! - Info: Same colour (mangenta) and area (2.0)
        // - C5 <---> T9 - Info: Same colour (magenta)
        // - C5 <---> T10 - Info: Same colour (magenta)
        // - S7 <---> T9 - NEW! - Info: Same colour (magenta)
        // - S7 <---> T10 - NEW! - Info: Same colour (magenta)
        // - S8 <---> T9 - NEW! - Info: Same colour (magenta)
        // - S8 <---> T10 - NEW! - Info: Same colour (magenta)
        verifyStitches(state, "C2-S7 C2-S8 C3-S7 C3-S8 C5-S7 C5-S8 C5-T9 C5-T10 S7-T9 S7-T10 S8-T9 S8-T10",
                (ItemEquivalence) stitcherSession);
    }

    // METHODS - END -------------------------------------------------------------------------------

    // HELPER METHODS ------------------------------------------------------------------------------

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, StitcherRulePair<? extends ColouredPolygon, ? extends ColouredPolygon>> generateRulePairs() {
        Map<String, StitcherRulePair<? extends ColouredPolygon, ? extends ColouredPolygon>> rules =
                new HashMap<String, StitcherRulePair<? extends ColouredPolygon, ? extends ColouredPolygon>>();

        // Circle-Square area and colour
        rules.put(C_S_AREA_RULE, new StitcherRulePair(C_S_AREA_RULE, new PolygonComparatorCircleSquareArea(),
                new PolygonComparatorSquareCircleArea()));
        rules.put(C_S_COLOUR_RULE, new StitcherRulePair(C_S_COLOUR_RULE, new PolygonComparatorCircleSquareColour(),
                new PolygonComparatorSquareCircleColour()));

        // Circle-Triangle area and colour
        rules.put(C_T_AREA_RULE, new StitcherRulePair(C_T_AREA_RULE, new PolygonComparatorCircleTriangleArea(),
                new PolygonComparatorTriangleCircleArea()));
        rules.put(C_T_COLOUR_RULE, new StitcherRulePair(C_T_COLOUR_RULE, new PolygonComparatorCircleTriangleColour(),
                new PolygonComparatorTriangleCircleColour()));

        // Square-Triangle area and colour
        rules.put(S_T_AREA_RULE, new StitcherRulePair(S_T_AREA_RULE, new PolygonComparatorSquareTriangleArea(),
                new PolygonComparatorTriangleSquareArea()));
        rules.put(S_T_COLOUR_RULE, new StitcherRulePair(S_T_COLOUR_RULE, new PolygonComparatorSquareTriangleColour(),
                new PolygonComparatorTriangleSquareColour()));

        return rules;
    }

    private Map<String, ColouredPolygon> generateInitialPolygons(final String[] types, final Colour[] colours,
            final double[] areas) {
        Map<String, ColouredPolygon> polygons = new HashMap<String, ColouredPolygon>();
        ColouredPolygon currentPolygon;

        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case CIRCLE:
                    currentPolygon = new Circle("C" + (i + 1), circleType);
                    break;
                case SQUARE:
                    currentPolygon = new Square("S" + (i + 1), squareType);
                    break;
                case TRIANGLE:
                    currentPolygon = new Triangle("T" + (i + 1), triangleType);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected polygon name");
            }
            currentPolygon.setArea(areas[i]).setColour(colours[i]);
            polygons.put(currentPolygon.toString(), currentPolygon);
        }

        return polygons;
    }

    private void updateCollections(final Collection<Item> newItems, final String newItemsIds,
            final Collection<Item> updatedItems, final String updatedItemsIds, final Collection<Item> deletedItems,
            final String deletedItemsIds, final Collection<Item> allItems) {
        fillCollectionFromShortIds(newItems, newItemsIds);
        fillCollectionFromShortIds(updatedItems, updatedItemsIds);
        fillCollectionFromShortIds(deletedItems, deletedItemsIds);
        updateAllItemsCollection(allItems, newItems, deletedItems);
    }

    private void updateAllItemsCollection(final Collection<Item> allItems, final Collection<Item> newItems,
            final Collection<Item> deletedItems) {
        allItems.removeAll(deletedItems);
        allItems.addAll(newItems);
    }

    private void fillCollectionFromShortIds(final Collection<Item> items, final String ids) {
        items.clear();
        StringTokenizer st = new StringTokenizer(ids);
        while (st.hasMoreTokens()) {
            items.add(retrievePolygon(st.nextToken()));
        }
    }

    // private void clearCollections(Collection<Item> newItems, Collection<Item> updatedItems,
    // Collection<Item> deletedItems) {
    // newItems.clear();
    // updatedItems.clear();
    // deletedItems.clear();
    // }

    private void verifyValues(final ColouredPolygon p, final double refLenght, final double area,
            final double perimeter) {
        // Constants
        final double delta = 1E-6;

        Assert.assertEquals(refLenght, p.getRefLenght(), delta);
        Assert.assertEquals(area, p.getArea(), delta);
        Assert.assertEquals(perimeter, p.getPerimeter(), delta);
    }

    private <T> void verifySameCollections(final String message, final Collection<T> expected,
            final Collection<T> actual) {
        Assert.assertTrue(message, expected.containsAll(actual));
        Assert.assertTrue(message, actual.containsAll(expected));
    }

    private void verifyExpectedModifiedElements(final String message, final String expected,
            final Collection<Item> actual) {
        Collection<Item> expectedAsCollection = new ArrayList<Item>();
        fillCollectionFromShortIds(expectedAsCollection, expected);
        verifySameCollections(message + " - Expected modified elements: " + expected, expectedAsCollection, actual);
    }

    private void parseSymmetricStitch(final String stitchAsString, final Map<Item, Collection<Item>> stitches) {
        try {
            // Parse stitch
            StringTokenizer st = new StringTokenizer(stitchAsString, "-");
            ColouredPolygon pol1 = retrievePolygon(st.nextToken());
            ColouredPolygon pol2 = retrievePolygon(st.nextToken());
            if (st.hasMoreTokens()) {
                throw new IllegalArgumentException(
                        "Stitch to be parsed not correctly constructed: \"" + stitchAsString + '"');
            }

            // Retrieve data structures
            Collection<Item> pol1Coll = stitches.get(pol1);
            if (pol1Coll == null) {
                pol1Coll = new HashSet<Item>();
                stitches.put(pol1, pol1Coll);
            }
            Collection<Item> pol2Coll = stitches.get(pol2);
            if (pol2Coll == null) {
                pol2Coll = new HashSet<Item>();
                stitches.put(pol2, pol2Coll);
            }

            // Add stitches (Notice that they should be crossed!)
            pol1Coll.add(pol2);
            pol2Coll.add(pol1);

        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(
                    "Stitch to be parsed not correctly constructed: \"" + stitchAsString + '"');
        }

    }

    private Map<Item, Collection<Item>> buildSymmetricStitchesFromString(final String stitchesAsString) {
        Map<Item, Collection<Item>> stitches = new HashMap<Item, Collection<Item>>();

        StringTokenizer st = new StringTokenizer(stitchesAsString);
        while (st.hasMoreTokens()) {
            parseSymmetricStitch(st.nextToken(), stitches);
        }

        return stitches;
    }

    private void verifyStitches(final String message, final String expected, final ItemEquivalence actual) {
        Map<Item, Collection<Item>> stitches = buildSymmetricStitchesFromString(expected);

        for (Item current : stitches.keySet()) {
            verifySameCollections(message + " - Expected stitches for item " + current + ": " + expected,
                    stitches.get(current), actual.getEquivalentItems(current));
        }
    }

    /**
     * Format: XN*
     * <p>
     * where X is a letter which identifies the polygon type (C: Circle, S: Square, T: Triangle) and
     * N* is a number which may have more than one digit.
     * <p>
     *
     * Examples: "C1" (Circle 1), S8 (Square 8), T17 (Triangle 17)
     *
     * @param id
     * @return
     */
    private ColouredPolygon retrievePolygon(final String id) {
        if (id.charAt(0) == 'C') {
            // Correct
        } else if (id.charAt(0) == 'S') {
            // Correct
        } else if (id.charAt(0) == 'T') {
            // Correct
        } else {
            // Unexpected
            throw new IllegalArgumentException("Polygon name is not valid: \"" + id + '"');
        }

        ColouredPolygon polygon = polygons.get(id);
        if (polygon == null) {
            throw new IllegalArgumentException("Polygon name does not match any existing one: \"" + id + '"');
        }

        return polygon;
    }

    private double circleArea(final double ratio) {
        return Math.PI * ratio * ratio;
    }

    private double circlePerimeter(final double ratio) {
        return 2 * Math.PI * ratio;
    }

    private double squareArea(final double side) {
        return side * side;
    }

    private double squarePerimeter(final double side) {
        return 4 * side;
    }

    private double triangleArea(final double side) {
        return side * side / 2;
    }

    private double trianglePerimeter(final double side) {
        return (Math.sqrt(5.0) + 1) * side;
    }

    protected abstract StitcherRuleManager createNewStitcherRuleManager();

    protected abstract StitcherUpdater createNewStitcherSession(StitcherRuleManager ruleManager);

    // HELPER METHODS - END ------------------------------------------------------------------------
}
