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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.hp.hpl.stitcher.extras.Verifier;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.Processor;
import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.matchers.MatchListener;

/**
 * Abstract subclass of <tt>AbstractStitcher</tt> which will use Duke for the record linkage
 * process.
 *
 * @param <B> Base elements' type, as defined in <tt>Stitcher</tt>.
 * @param <C> Candidate elements' type, as defined in <tt>Stitcher</tt>.
 */
public abstract class DukeStitcher<B, C> extends AbstractStitcher<B, C> {

    // NESTED INTERFACES ---------------------------------------------------------------------------

    /**
     * Objects implementing this class will be able to return an item used by Duke providing only
     * its ID (property "ID").
     *
     * @param <T> Type of the items to be returned.
     */
    public interface DukeObjectRetriever<T> {

        /**
         * Retrieve an item denoted by its ID.
         * 
         * @param id "ID" property of the item to be retrieved
         * @return The original item provided as input to Duke in its corresponding
         *         <tt>DataSource</tt>.
         */
        public T retrieve(String id);

    }

    // NESTED INTERFACES - END ---------------------------------------------------------------------

    // NESTED CLASSES ------------------------------------------------------------------------------

    protected class DukeStitcherMatchListener implements MatchListener {

        private Map<B, Collection<C>> stitches;

        public DukeStitcherMatchListener(Map<B, Collection<C>> stitches) {
            this.stitches = stitches;
        }

        @Override
        public void batchReady(int size) {}

        @Override
        public void batchDone() {}

        @Override
        public void matches(Record base, Record candidate, double confidence) {
            String baseId = base.getValue(ID_PROP);
            String candidateId = candidate.getValue(ID_PROP);
            stitches.get(baseObjectRetriever.retrieve(baseId)).add(candidateObjectRetriever.retrieve(candidateId));
        }

        @Override
        public void matchesPerhaps(Record base, Record candidate, double confidence) {
            // Ignoring unsure matches
        }

        @Override
        public void noMatchFor(Record base) {
            // Ignoring base elements with no matches
        }

        @Override
        public void startProcessing() {}

        @Override
        public void endProcessing() {}

    }

    // NESTED CLASSES - END ------------------------------------------------------------------------

    // VARIABLES -----------------------------------------------------------------------------------

    public static final String ID_PROP = "ID";

    protected Processor proc;
    protected Configuration config;
    protected Map<String, ?> basePropertiesInfo;
    protected Map<String, ?> candidatePropertiesInfo;
    protected DukeObjectRetriever<B> baseObjectRetriever;
    protected DukeObjectRetriever<C> candidateObjectRetriever;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    /**
     * Constructs a <tt>DukeStitcher</tt>.
     * 
     * @param config The <tt>Configuration</tt> needed by the <tt>Processor</tt>. It should include
     *        the threshold(s) and the properties (class <tt>Property</tt>). It should <u>not</u>
     *        include <tt>DataSource</tt>s, as they will be generated in this class.
     * @param basePropertiesInfo A <tt>Map</tt> containing all the properties that will be
     *        considered for the base items and an <tt>Object</tt> associated with them. This
     *        <tt>Object</tt> may help to retrieve the values from the base item and store them in
     *        the <tt>Record</tt>s used by Duke. How this <tt>Object</tt> will be treated is up to
     *        the implementer.
     * @param candidatePropertiesInfo A <tt>Map</tt> containing all the properties that will be
     *        considered for the candidate items and an <tt>Object</tt> associated with them. This
     *        <tt>Object</tt> may help to retrieve the values from the candidate item and store them
     *        in the <tt>Record</tt>s used by Duke. How this <tt>Object</tt> will be treated is up
     *        to the implementer.
     */
    public DukeStitcher(Configuration config, Map<String, ?> basePropertiesInfo,
            Map<String, ?> candidatePropertiesInfo) {
        this(config, basePropertiesInfo, candidatePropertiesInfo, null, null, false);
    }

    /**
     * Constructs a <tt>DukeStitcher</tt>.
     * 
     * @param config The <tt>Configuration</tt> needed by the <tt>Processor</tt>. It should include
     *        the threshold(s) and the properties (class <tt>Property</tt>). It should <u>not</u>
     *        include <tt>DataSource</tt>s, as they will be generated in this class.
     * @param basePropertiesInfo A <tt>Map</tt> containing all the properties that will be
     *        considered for the base items and an <tt>Object</tt> associated with them. This
     *        <tt>Object</tt> may help to retrieve the values from the base item and store them in
     *        the <tt>Record</tt>s used by Duke. How this <tt>Object</tt> will be treated is up to
     *        the implementer.
     * @param candidatePropertiesInfo A <tt>Map</tt> containing all the properties that will be
     *        considered for the candidate items and an <tt>Object</tt> associated with them. This
     *        <tt>Object</tt> may help to retrieve the values from the candidate item and store them
     *        in the <tt>Record</tt>s used by Duke. How this <tt>Object</tt> will be treated is up
     *        to the implementer.
     * @param baseObjectRetriever The <tt>DukeObjectRetriever</tt> that will be used during the
     *        stitching process to return the original base items.
     * @param candidateObjectRetriever The <tt>DukeObjectRetriever</tt> that will be used during the
     *        stitching process to return the original candidate items.
     */
    public DukeStitcher(Configuration config, Map<String, ?> basePropertiesInfo, Map<String, ?> candidatePropertiesInfo,
            DukeObjectRetriever<B> baseObjectRetriever, DukeObjectRetriever<C> candidateObjectRetriever) {
        this(config, basePropertiesInfo, candidatePropertiesInfo, baseObjectRetriever, candidateObjectRetriever, true);
    }

    protected DukeStitcher(Configuration config, Map<String, ?> basePropertiesInfo,
            Map<String, ?> candidatePropertiesInfo, DukeObjectRetriever<B> baseObjectRetriever,
            DukeObjectRetriever<C> candidateObjectRetriever, boolean checkRetrievers) {

        // Verify that none of the arguments are null
        Verifier.illegalArgumentIfNull(config);
        Verifier.illegalArgumentIfNull(basePropertiesInfo, candidatePropertiesInfo);
        if (checkRetrievers) {
            Verifier.illegalArgumentIfNull(baseObjectRetriever, candidateObjectRetriever);
        }

        this.config = config; // Does not need to include DataSources, as they will be generated
                              // here. It should include the actual properties (not to be confused
                              // with the properties info)

        // These "properties info" include references as where to find the properties' values
        this.basePropertiesInfo = basePropertiesInfo;
        this.candidatePropertiesInfo = candidatePropertiesInfo;
        this.baseObjectRetriever = baseObjectRetriever;
        this.candidateObjectRetriever = candidateObjectRetriever;

        // As DukeStitcher is not using a StitchChecker, it will not be checked whether it is null
        this.verifyStitchChecker = false;
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Abstract method to be implemented by subclasses of this one. It should generate a
     * <tt>DataSource</tt> which will be provided to Duke for its record linkage process.
     * <p>
     * 
     * @param baseElements Base elements which will be processed by Duke.
     * @return A <tt>DataSource</tt> that will generate the base elements that will be provided to
     *         Duke as <tt>DukeStitcherRecord</tt>s.
     */
    protected abstract DataSource generateBaseDataSource(Collection<B> baseElements);

    /**
     * Abstract method to be implemented by subclasses of this one. It should generate a
     * <tt>DataSource</tt> which will be provided to Duke for its record linkage process.
     * <p>
     * 
     * @param candidateElements Candidate elements which will be processed by Duke.
     * @return A <tt>DataSource</tt> that will generate the candidate elements that will be provided
     *         to Duke as <tt>DukeStitcherRecord</tt>s.
     */
    protected abstract DataSource generateCandidateDataSource(Collection<C> candidateElements);

    @Override
    protected Map<B, Collection<C>> subStitch(Collection<B> filteredBaseElements,
            Collection<C> filteredCandidateElements, Map<B, Collection<C>> stitches) {
        // Set Datasources
        DataSource baseDataSource, candidateDataSource;
        baseDataSource = generateBaseDataSource(filteredBaseElements);
        candidateDataSource = generateCandidateDataSource(filteredCandidateElements);
        config.addDataSource(1, candidateDataSource); // NOTE: Read comment below
        config.addDataSource(2, baseDataSource); // NOTE: Read comment below

        // IMPORTANT NOTICE:
        // Duke's linking process takes its data source number 1 and indexes it, then traverses its
        // data source number 2 to look for matches from the first one. This means that Duke's
        // candidates group is its data source 1 and its base group is its data source 2.
        // As in this stitcher we are considering that the base data source is our first group and
        // the candidate data source is our second group, these needs to be swapped to comply with
        // Duke's behaviour. This explains why in the two previous lines our "candidateDataSource"
        // is set as data source number 1 and our "baseDataSource" is set as data source number 2.

        // Validate configuration before starting linking
        config.validate(); // Will throw a RuntimeException if not correct

        @SuppressWarnings("unused")
        long startTime, stopTime;
        startTime = System.currentTimeMillis();

        try {
            Processor proc = new Processor(config);
            proc.addMatchListener(new DukeStitcherMatchListener(stitches));
            proc.link();
            proc.close();
        } catch (IOException e) {
            return null;
        }

        stopTime = System.currentTimeMillis();

        return stitches;
    }

    @Override
    protected Collection<C> newDataStructureForStitching() {
        return new ArrayList<C>();
    }

    /**
     * Getter method for the <tt>Configuration</tt> used by Duke.
     * 
     * @return The current <tt>Configuration</tt>.
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Setter method for the <tt>Configuration</tt> used by Duke.
     * 
     * @param config The new <tt>Configuration</tt> to be set.
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }

    /**
     * Getter method for the <tt>DukeObjectRetriever</tt> used by Duke to retrieve its base items.
     * 
     * @return The aforementioned <tt>DukeObjectRetriever</tt>.
     */
    public DukeObjectRetriever<B> getBaseObjectRetriever() {
        return baseObjectRetriever;
    }

    /**
     * Setter method for the <tt>DukeObjectRetriever</tt> used by Duke to retrieve its base items.
     * 
     * @param baseObjectRetriever The aforementioned <tt>DukeObjectRetriever</tt> to be set.
     */
    public void setBaseObjectRetriever(DukeObjectRetriever<B> baseObjectRetriever) {
        this.baseObjectRetriever = baseObjectRetriever;
    }

    /**
     * Getter method for the <tt>DukeObjectRetriever</tt> used by Duke to retrieve its candidate
     * items.
     * 
     * @return The aforementioned <tt>DukeObjectRetriever</tt>.
     */
    public DukeObjectRetriever<C> getCandidateObjectRetriever() {
        return candidateObjectRetriever;
    }

    /**
     * Setter method for the <tt>DukeObjectRetriever</tt> used by Duke to retrieve its candidate
     * items.
     * 
     * @param candidateObjectRetriever The aforementioned <tt>DukeObjectRetriever</tt> to be set.
     */
    public void setCandidateObjectRetriever(DukeObjectRetriever<C> candidateObjectRetriever) {
        this.candidateObjectRetriever = candidateObjectRetriever;
    }

    // METHODS - END -------------------------------------------------------------------------------

}
