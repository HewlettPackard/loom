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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItemAttributes;

import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.DataSource;

/**
 * Subclass of <tt>DukeStitcher</tt> for record linkage across different items from the HP Cloud.
 *
 * @param <B> Base elements' type, as defined in <tt>Stitcher</tt>.
 * @param <C> Candidate elements' type, as defined in <tt>Stitcher</tt>.
 */
public class HpCloudDukeStitcher<B extends HpCloudItem<? extends HpCloudItemAttributes>, C extends HpCloudItem<? extends HpCloudItemAttributes>>
        extends DukeStitcher<B, C> {

    // VARIABLES -----------------------------------------------------------------------------------
    protected boolean externalObjectProviders;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    /**
     * <i>Copied from DukeStitcher</i>
     * <p>
     * 
     * Constructs an <tt>HpCloudDukeStitcher</tt>.
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
    public HpCloudDukeStitcher(Configuration config, Map<String, ?> basePropertiesInfo,
            Map<String, ?> candidatePropertiesInfo) {
        super(config, basePropertiesInfo, candidatePropertiesInfo);
        this.externalObjectProviders = false;
        verifyProperties(basePropertiesInfo, "Base properties' type is not String");
        verifyProperties(candidatePropertiesInfo, "Candidate properties' type is not String");
    }

    /**
     * <i>Copied from DukeStitcher</i>
     * <p>
     * 
     * Constructs an <tt>HpCloudDukeStitcher</tt>.
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
    public HpCloudDukeStitcher(Configuration config, Map<String, ?> basePropertiesInfo,
            Map<String, ?> candidatePropertiesInfo, DukeObjectRetriever<B> baseObjectRetriever,
            DukeObjectRetriever<C> candidateObjectRetriever) {
        super(config, basePropertiesInfo, candidatePropertiesInfo, baseObjectRetriever, candidateObjectRetriever);
        this.externalObjectProviders = true;
        verifyProperties(basePropertiesInfo, "Base properties' type is not String");
        verifyProperties(candidatePropertiesInfo, "Candidate properties' type is not String");
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Override
    protected DataSource generateBaseDataSource(Collection<B> baseElements) {
        if (externalObjectProviders) {
            return new HpCloudDataSource<B>(baseElements, convertPropertiesInfo(basePropertiesInfo));
        } else {
            HpCloudDataSourceAndRetriever<B> ds =
                    new HpCloudDataSourceAndRetriever<B>(baseElements, convertPropertiesInfo(basePropertiesInfo));
            setBaseObjectRetriever(ds);
            return ds;
        }
    }

    @Override
    protected DataSource generateCandidateDataSource(Collection<C> candidateElements) {
        if (externalObjectProviders) {
            return new HpCloudDataSource<C>(candidateElements, convertPropertiesInfo(candidatePropertiesInfo));
        } else {
            HpCloudDataSourceAndRetriever<C> ds = new HpCloudDataSourceAndRetriever<C>(candidateElements,
                    convertPropertiesInfo(candidatePropertiesInfo));
            setCandidateObjectRetriever(ds);
            return ds;
        }
    }

    /**
     * Helper method which receives a <tt>Map&lt;String, ?></tt> containing the properties and their
     * associated <tt>String</tt>s and returns an actual <tt>Map&lt;String, String></tt>.
     * 
     * @param originalProperties
     * @return
     */
    private Map<String, String> convertPropertiesInfo(Map<String, ?> originalProperties) {
        Map<String, String> newProperties = new HashMap<String, String>();

        for (String key : originalProperties.keySet()) {
            newProperties.put(key, (String) originalProperties.get(key));
        }

        return newProperties;
    }

    private void verifyProperties(Map<String, ?> propertiesInfo, String message) {
        for (String s : propertiesInfo.keySet()) {
            if (!(propertiesInfo.get(s) instanceof String)) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    // METHODS - END -------------------------------------------------------------------------------

}
