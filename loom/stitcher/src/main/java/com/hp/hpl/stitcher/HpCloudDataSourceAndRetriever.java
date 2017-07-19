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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItemAttributes;
import com.hp.hpl.stitcher.DukeStitcher.DukeObjectRetriever;

/**
 * Subclass of <tt>HpCloudDataSource</tt> which saves references to the items provided as input so
 * that they can be retrieved in the future.
 *
 * @param <T> Child class of <tt>HpCloudItem</tt> whose objects are going to be provided to this
 *        <tt>DataSource</tt>.
 */
public class HpCloudDataSourceAndRetriever<T extends HpCloudItem<? extends HpCloudItemAttributes>>
        extends HpCloudDataSource<T> implements DukeObjectRetriever<T> {

    // VARIABLES -----------------------------------------------------------------------------------
    protected Map<String, T> index;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    /**
     * <i>Copied from HpCloudDataSource</i>
     * <p>
     *
     * Constructs an <tt>HpCloudDataSourceAndRetriever</tt>.
     * <p>
     *
     * <b>NOTE</b>: One entry in propertiesInfo should be: <tt>&lt;"ID","_id"&gt;</tt>
     *
     * @param items Items which will be processed and returned as <tt>Record</tt>s when the
     *        <tt>getRecords()</tt> method is called.
     * @param propertiesInfo <tt>Map</tt> which contains an entry for each property that is going to
     *        be considered by Duke. For each property, this <tt>Map</tt> should contain a
     *        <tt>String</tt> which will denote which attribute from the corresponding
     *        <tt>HpCloudItem</tt> is to be used as its value.
     */
    public HpCloudDataSourceAndRetriever(final Collection<T> items, final Map<String, String> propertiesInfo) {
        super(items, propertiesInfo);

        // Create index used to retrieve objects
        // Sequential implementation
        // index = new HashMap<String, T>(items.size());
        // for (T item : items) {
        // index.put(item.get_id(), item);
        // }

        // Create index used to retrieve objects
        // Parallel implementation
        index = items.parallelStream()
                .collect(Collectors.toMap(item -> item.getCore().getHpCloudId(), Function.identity()));
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Override
    public T retrieve(final String id) {
        return index.get(id);
    }

    // METHODS - END -------------------------------------------------------------------------------

}
