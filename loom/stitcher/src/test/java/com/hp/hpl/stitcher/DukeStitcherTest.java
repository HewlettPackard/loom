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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.stitcher.DukeStitcher.DukeObjectRetriever;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.Property;
import no.priv.garshol.duke.RecordImpl;
import no.priv.garshol.duke.comparators.ExactComparator;

/**
 * This class tests the methods in the <tt>DukeStitcher</tt> class.
 */
public class DukeStitcherTest {

    private static final class Composer {

        public static String name = "name";
        public static String origin = "origin";

        private Map<String, String> attributes;

        public Composer(String name, String origin) {
            attributes = new HashMap<String, String>();
            attributes.put(Composer.name, name);
            attributes.put(Composer.origin, origin);
        }

        public String getAttribute(String attribute) {
            return attributes.get(attribute);
        }

        @Override
        public String toString() {
            return getAttribute(name);
        }
    }

    private final class DukeStitcherForTesting extends DukeStitcher<String, Composer> {
        public DukeStitcherForTesting(Configuration config, Map<String, ?> basePropertiesInfo,
                Map<String, ?> candidatePropertiesInfo) {
            super(config, basePropertiesInfo, candidatePropertiesInfo);
        }

        public DukeStitcherForTesting(Configuration config, Map<String, ?> basePropertiesInfo,
                Map<String, ?> candidatePropertiesInfo,
                com.hp.hpl.stitcher.DukeStitcher.DukeObjectRetriever<String> baseObjectRetriever,
                com.hp.hpl.stitcher.DukeStitcher.DukeObjectRetriever<Composer> candidateObjectRetriever) {
            super(config, basePropertiesInfo, candidatePropertiesInfo, baseObjectRetriever, candidateObjectRetriever);
        }

        @Override
        protected DataSource generateBaseDataSource(Collection<String> baseElements) {
            DataSource ds = mock(DataSource.class);
            Map<String, Collection<String>> map;
            DukeStitcherRecordIterator<String> it = new DukeStitcherRecordIterator<String>();

            for (String s : baseElements) {
                map = new HashMap<String, Collection<String>>(2);
                map.put(idProp, new ArrayList<String>(1));
                map.put(originProp, new ArrayList<String>(1));
                map.get(idProp).add(s);
                map.get(originProp).add(s);
                it.addRecord(new RecordImpl(map));
            }

            it.start();
            when(ds.getRecords()).thenReturn(it);

            return ds;
        }

        @Override
        protected DataSource generateCandidateDataSource(Collection<Composer> candidateElements) {
            DataSource ds = mock(DataSource.class);
            Map<String, Collection<String>> map;
            DukeStitcherRecordIterator<Composer> it = new DukeStitcherRecordIterator<Composer>();

            for (Composer c : candidateElements) {
                map = new HashMap<String, Collection<String>>(2);
                map.put(idProp, new ArrayList<String>(1));
                map.put(originProp, new ArrayList<String>(1));
                map.get(idProp).add(c.getAttribute(Composer.name));
                map.get(originProp).add(c.getAttribute(Composer.origin));
                it.addRecord(new RecordImpl(map));
            }

            it.start();
            when(ds.getRecords()).thenReturn(it);

            return ds;
        }
    }

    // TEST VARIABLES ------------------------------------------------------------------------------
    private DukeStitcher<String, Composer> stitcher;
    private Configuration config;
    private List<Property> properties;
    private DukeObjectRetriever<String> countryRetriever;
    private DukeObjectRetriever<Composer> composerRetriever;

    // Classical composers
    private static Composer bach;
    private static Composer beethoven;
    private static Composer chopin;
    private static Composer mozart;
    private static Composer pachelbel;
    private static Composer sibelius;
    private static Composer strauss;
    private static Composer tchaikovsky;
    private static Composer verdi;
    private static Composer vivaldi;
    private static String bachName = "Johann Sebastian Bach";
    private static String beethovenName = "Ludwig van Beethoven";
    private static String chopinName = "Frederic Chopin";
    private static String mozartName = "Wolfgang Amadeus Mozart";
    private static String pachelbelName = "Johann Pachelbel";
    private static String sibeliusName = "Jean Sibelius";
    private static String straussName = "Johann Strauss";
    private static String tchaikovskyName = "Pyotr Ilyich Tchaikovsky";
    private static String verdiName = "Giuseppe Verdi";
    private static String vivaldiName = "Antonio Vivaldi";
    private static Collection<Composer> composers;

    // Countries
    private static String austria;
    private static String finland;
    private static String germany;
    private static String italy;
    private static String poland;
    private static String russia;
    private static String unitedKingdom;
    private static Collection<String> countries;

    // Properties' names
    private static String idProp = "ID";
    private static String originProp = "Origin";

    // TEST VARIABLES - END ------------------------------------------------------------------------

    @BeforeClass
    public static void beforeClass() {
        austria = "Austria";
        finland = "Finland";
        germany = "Germany";
        italy = "Italy";
        poland = "Poland";
        russia = "Russia";
        unitedKingdom = "United Kingdom";
        countries = new ArrayList<String>();
        countries.add(austria);
        countries.add(finland);
        countries.add(germany);
        countries.add(italy);
        countries.add(poland);
        countries.add(russia);
        countries.add(unitedKingdom);

        bach = new Composer(bachName, germany);
        beethoven = new Composer(beethovenName, germany);
        chopin = new Composer(chopinName, poland);
        mozart = new Composer(mozartName, austria);
        pachelbel = new Composer(pachelbelName, germany);
        sibelius = new Composer(sibeliusName, finland);
        strauss = new Composer(straussName, austria);
        tchaikovsky = new Composer(tchaikovskyName, russia);
        verdi = new Composer(verdiName, italy);
        vivaldi = new Composer(vivaldiName, italy);
        composers = new ArrayList<Composer>();
        composers.add(bach);
        composers.add(beethoven);
        composers.add(chopin);
        composers.add(mozart);
        composers.add(pachelbel);
        composers.add(sibelius);
        composers.add(strauss);
        composers.add(tchaikovsky);
        composers.add(verdi);
        composers.add(vivaldi);
    }

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        properties = new ArrayList<Property>();
        properties.add(new Property(idProp)); // NOTE: Changed to PropertyImpl in Duke 1.3?)
        properties.add(new Property(originProp, new ExactComparator(), 0.01, 0.99)); // NOTE:
                                                                                     // Changed to
                                                                                     // PropertyImpl
                                                                                     // in Duke 1.3?

        config = new Configuration(); // NOTE: Changed to ConfigurationImpl in Duke 1.3?
        config.setProperties(properties);
        config.setThreshold(0.95);
        config.setMaybeThreshold(0.8);

        countryRetriever = mock(DukeObjectRetriever.class);
        when(countryRetriever.retrieve(austria)).thenReturn(austria);
        when(countryRetriever.retrieve(finland)).thenReturn(finland);
        when(countryRetriever.retrieve(germany)).thenReturn(germany);
        when(countryRetriever.retrieve(italy)).thenReturn(italy);
        when(countryRetriever.retrieve(poland)).thenReturn(poland);
        when(countryRetriever.retrieve(russia)).thenReturn(russia);
        when(countryRetriever.retrieve(unitedKingdom)).thenReturn(unitedKingdom);

        composerRetriever = mock(DukeObjectRetriever.class);
        when(composerRetriever.retrieve(bach.getAttribute(Composer.name))).thenReturn(bach);
        when(composerRetriever.retrieve(beethoven.getAttribute(Composer.name))).thenReturn(beethoven);
        when(composerRetriever.retrieve(chopin.getAttribute(Composer.name))).thenReturn(chopin);
        when(composerRetriever.retrieve(mozart.getAttribute(Composer.name))).thenReturn(mozart);
        when(composerRetriever.retrieve(pachelbel.getAttribute(Composer.name))).thenReturn(pachelbel);
        when(composerRetriever.retrieve(sibelius.getAttribute(Composer.name))).thenReturn(sibelius);
        when(composerRetriever.retrieve(strauss.getAttribute(Composer.name))).thenReturn(strauss);
        when(composerRetriever.retrieve(tchaikovsky.getAttribute(Composer.name))).thenReturn(tchaikovsky);
        when(composerRetriever.retrieve(verdi.getAttribute(Composer.name))).thenReturn(verdi);
        when(composerRetriever.retrieve(vivaldi.getAttribute(Composer.name))).thenReturn(vivaldi);
    }

    @Test
    public void testConstructorNoObjectRetriever() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();

        new DukeStitcherForTesting(config, baseProps, candidateProps);
    }

    @Test
    public void testConstructorWithObjectRetriever() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();

        new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
    }

    @Test
    public void testConstructorNoObjectRetrieverNullArgs() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();

        try {
            new DukeStitcherForTesting(null, baseProps, candidateProps);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new DukeStitcherForTesting(config, null, candidateProps);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new DukeStitcherForTesting(config, baseProps, null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testConstructorObjectRetrieverNullArgs() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();

        try {
            new DukeStitcherForTesting(null, baseProps, candidateProps, countryRetriever, composerRetriever);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new DukeStitcherForTesting(config, null, candidateProps, countryRetriever, composerRetriever);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new DukeStitcherForTesting(config, baseProps, null, countryRetriever, composerRetriever);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new DukeStitcherForTesting(config, baseProps, candidateProps, null, composerRetriever);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }

        try {
            new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, null);
            fail();
        } catch (IllegalArgumentException e) {
            // As expected
        }
    }

    @Test
    public void testSetAndGetConfiguration() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        DukeStitcher<?, ?> stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps);

        assertEquals(config, stitcher.getConfig());

        Configuration mockedConfig = mock(Configuration.class);
        stitcher.setConfig(mockedConfig);

        assertEquals(mockedConfig, stitcher.getConfig());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAndGetBaseObjectRetriever() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        DukeObjectRetriever<String> otherRetriever;
        otherRetriever = mock(DukeObjectRetriever.class);
        DukeStitcher<String, Composer> stitcher =
                new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);

        assertEquals(countryRetriever, stitcher.getBaseObjectRetriever());

        stitcher.setBaseObjectRetriever(otherRetriever);
        assertEquals(otherRetriever, stitcher.getBaseObjectRetriever());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAndGetCandidateObjectRetriever() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        DukeObjectRetriever<Composer> otherRetriever;
        otherRetriever = mock(DukeObjectRetriever.class);
        DukeStitcher<String, Composer> stitcher =
                new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);

        assertEquals(composerRetriever, stitcher.getCandidateObjectRetriever());

        stitcher.setCandidateObjectRetriever(otherRetriever);
        assertEquals(otherRetriever, stitcher.getCandidateObjectRetriever());
    }

    @Test
    public void testFullStichingNoElementsWhatsoever() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
        // stitcher.addBaseElements(countries); // SKIPPED
        // stitcher.addCandidateElements(composers); // SKIPPED

        // Create expected map of stitches
        Map<String, Collection<Composer>> expectedStitches = new HashMap<String, Collection<Composer>>();

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    @Test
    public void testFullStichingNoBaseElements() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
        // stitcher.addBaseElements(countries); // SKIPPED
        stitcher.addCandidateElements(composers);

        // Create expected map of stitches
        Map<String, Collection<Composer>> expectedStitches = new HashMap<String, Collection<Composer>>();

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    @Test
    public void testFullStichingNoCandidateElements() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
        stitcher.addBaseElements(countries);
        // stitcher.addCandidateElements(composers); // SKIPPED

        // Create expected map of stitches
        Map<String, Collection<Composer>> expectedStitches = new HashMap<String, Collection<Composer>>();
        for (String c : countries) {
            expectedStitches.put(c, new ArrayList<DukeStitcherTest.Composer>());
        }

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    @Test
    public void testFullStiching() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
        stitcher.addBaseElements(countries);
        stitcher.addCandidateElements(composers);

        // Create expected map of stitches
        Map<String, Collection<Composer>> expectedStitches = new HashMap<String, Collection<Composer>>();
        for (String c : countries) {
            expectedStitches.put(c, new ArrayList<DukeStitcherTest.Composer>());
        }
        expectedStitches.get(austria).add(mozart);
        expectedStitches.get(austria).add(strauss);
        expectedStitches.get(finland).add(sibelius);
        expectedStitches.get(germany).add(bach);
        expectedStitches.get(germany).add(beethoven);
        expectedStitches.get(germany).add(pachelbel);
        expectedStitches.get(italy).add(verdi);
        expectedStitches.get(italy).add(vivaldi);
        expectedStitches.get(poland).add(chopin);
        expectedStitches.get(russia).add(tchaikovsky);

        StitcherTestsUtils.checkSameStitches(expectedStitches, stitcher.stitch());
    }

    @Test
    public void testSingleStitchingNoMatches() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
        stitcher.addCandidateElements(composers);

        // Create expected map of stitches
        Collection<Composer> expectedMatches = new ArrayList<Composer>();

        StitcherTestsUtils.checkSameElements(expectedMatches, stitcher.stitch(unitedKingdom));
    }

    @Test
    public void testSingleStitching() {
        Map<String, ?> baseProps, candidateProps;
        baseProps = new HashMap<String, String>();
        candidateProps = new HashMap<String, String>();
        stitcher = new DukeStitcherForTesting(config, baseProps, candidateProps, countryRetriever, composerRetriever);
        stitcher.addCandidateElements(composers);

        // Create expected map of stitches
        Collection<Composer> expectedMatches = new ArrayList<Composer>();
        expectedMatches.add(mozart);
        expectedMatches.add(strauss);

        assertEquals(expectedMatches, stitcher.stitch(austria));
        StitcherTestsUtils.checkSameElements(expectedMatches, stitcher.stitch(austria));
    }

}
