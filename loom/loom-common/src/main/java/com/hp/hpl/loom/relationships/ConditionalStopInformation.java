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
package com.hp.hpl.loom.relationships;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hp.hpl.loom.model.Item;

/***
 * Information that is tested by the {@link ConnectedRelationships} in order to evaluate the stop
 * conditions. Currently the information being tracked is: layers and traversal path.
 */
public class ConditionalStopInformation {

    /***
     * True if the item should be reported by the @link RelationsReporter.
     */
    private boolean reportItem = true;

    /***
     * Path traversed until this item was reached.
     */
    private List<Item> sequenceOfTraversedItemsUntilHere = new ArrayList<>();

    /***
     * Initialise a map with the layers this top item belongs to. All layers will still be possible
     * to visit for now. As flags turn to true, that layer was left during traversal and will not be
     * returned.
     */
    private Map<String, Boolean> layersAlreadyVisitedOnPathTraversal;

    /***
     * Constructor used only when starting the complete traversal, on the top item.
     *
     * @param currentItem Item where traversal is starting.
     */
    public ConditionalStopInformation(Item currentItem) {
        sequenceOfTraversedItemsUntilHere.add(currentItem);
        layersAlreadyVisitedOnPathTraversal = new HashMap<String, Boolean>();

        for (String layer : currentItem.getItemType().getLayers()) {
            layersAlreadyVisitedOnPathTraversal.put(layer, false);
        }
    }

    /***
     * Constructor used for every visited item during the traversal, after the top condition has the
     * initialised.
     *
     * @param equivalentStopInfo previous item ConditionalStopInformation.
     * @param currentItem The item that is currently being analysed on the traversal.
     */
    public ConditionalStopInformation(ConditionalStopInformation equivalentStopInfo, Item currentItem) {
        // Clone the traversal sequence
        sequenceOfTraversedItemsUntilHere =
                equivalentStopInfo.getSequenceOfTraversedItemsUntilHere().stream().collect(Collectors.toList());

        // Update layer information:
        layersAlreadyVisitedOnPathTraversal = updateAlreadyVisitedLayersForCurrentItem(currentItem,
                equivalentStopInfo.getLayersAlreadyVisitedOnPathTraversal());

        // Adds current item to traversal:
        sequenceOfTraversedItemsUntilHere.add(currentItem);
    }

    /**
     * @return the layersAlreadyVisitedOnPathTraversal
     */
    public Map<String, Boolean> getLayersAlreadyVisitedOnPathTraversal() {
        return layersAlreadyVisitedOnPathTraversal;
    }

    /***
     * Clones the layers already visited and update information, taking into consideration the
     * current item. If a layer is not present anymore, it is set as true.
     *
     * @param item
     * @param argLayersAlreadyVisitedOnPathTraversal
     * @return an updated layer hashmap, where all keys with value true were already visited before.
     */
    private Map<String, Boolean> updateAlreadyVisitedLayersForCurrentItem(Item item,
            Map<String, Boolean> argLayersAlreadyVisitedOnPathTraversal) {

        // clones:
        final Map<String, Boolean> updatedMap = argLayersAlreadyVisitedOnPathTraversal.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        // update with layers that were already left:
        String[] layerOnCurrentItem = item.getItemType().getLayers();

        // puts the new layers, if no present.
        for (String layer : layerOnCurrentItem) {
            if (!updatedMap.containsKey(layer)) {
                updatedMap.put(layer, false);
            }
        }

        // Set all layers that are not in the current layers has finished visiting:
        Set<String> currentItemLayers = new HashSet<String>(Arrays.asList(layerOnCurrentItem));
        // All Layers available in the Map
        Set<String> originalLayers = new HashSet<String>(updatedMap.keySet());

        // Removes the layers that this item belongs to:
        originalLayers.removeAll(currentItemLayers);

        // Set all layer that were before in the map and that this item does not belong to has
        // already visited.
        originalLayers.forEach(layer -> updatedMap.put(layer, true));

        return updatedMap;
    }

    /**
     * @return the sequenceOfTraversedItemsUntilHere Items that were traversed until reaching here.
     */
    public List<Item> getSequenceOfTraversedItemsUntilHere() {
        return sequenceOfTraversedItemsUntilHere;
    }

    /***
     * Item that the is being currently analysed.
     *
     * @return the current Item
     */
    public Item getCurrentItem() {
        return sequenceOfTraversedItemsUntilHere.get(sequenceOfTraversedItemsUntilHere.size() - 1);
    }

    /***
     * Source of the traversal.
     *
     * @return the source of the whole traversal until the item
     */
    public Item getTraversalSource() {
        return sequenceOfTraversedItemsUntilHere.get(0);
    }

    /**
     * Some user defined stop conditions may require that the traversed item be not reported. In
     * this case, this flag will be set to false.
     *
     * @return the reportItem
     */
    public boolean shouldItemBeReported() {
        return reportItem;
    }

    /**
     * @param reportItem the reportItem to set
     */
    public void setReportItem(boolean reportItem) {
        this.reportItem = reportItem;
    }
}
