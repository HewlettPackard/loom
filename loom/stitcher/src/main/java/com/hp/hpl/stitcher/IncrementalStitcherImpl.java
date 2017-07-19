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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hp.hpl.stitcher.extras.SetOperations;
import com.hp.hpl.stitcher.extras.Verifier;

public class IncrementalStitcherImpl<T extends Identifiable<String>> implements IncrementalStitcher<T> {

    // CLASSES -------------------------------------------------------------------------------------

    private class ItemIndexer {

        private Map<String, T> itemIndexById;

        public ItemIndexer() {
            itemIndexById = new ConcurrentHashMap<String, T>();
        }

        public Collection<T> getAllItems() {
            return itemIndexById.values();
        }

        public T getItemById(String id) {
            return itemIndexById.get(id);
        }

        public void addItems(Collection<T> items) {
            items.parallelStream().forEach((T item) -> {
                itemIndexById.put(item.getId(), item);
            });
        }

        public void removeItems(Collection<T> items) {
            items.parallelStream().forEach((T item) -> {
                itemIndexById.remove(item.getId());
            });
        }
    }

    // CLASSES - END -------------------------------------------------------------------------------

    // VARIABLES -----------------------------------------------------------------------------------

    private ItemIndexer itemIndex;
    private Map<String, Collection<String>> accumulatedStitches;
    private SelectiveStitcher<T, T> stitcherCore;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    public IncrementalStitcherImpl(SelectiveStitcher<T, T> stitcherCore) {
        this.stitcherCore = stitcherCore;
        itemIndex = new ItemIndexer();
        accumulatedStitches = new HashMap<String, Collection<String>>();
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Override
    public SelectiveStitcher<T, T> getStitcherCore() {
        return stitcherCore;
    }

    @Override
    public Map<T, Collection<T>> increment(Collection<T> newItems, Collection<T> updatedItems,
            Collection<T> deletedItems) {

        Verifier.illegalArgumentIfNull(newItems, updatedItems, deletedItems);

        // Index
        itemIndex.removeItems(deletedItems);
        itemIndex.addItems(newItems);

        // Remove stitches involving removed items from <accumulatedStitches>
        removeStitches(deletedItems, accumulatedStitches);

        // Stitch!
        Map<T, Collection<T>> newStitches;
        stitcherCore.clearBaseElements(); // Resetting microstitcher (1/2)
        stitcherCore.clearCandidateElements(); // Resetting microstitcher (2/2)
        stitcherCore.addBaseElements(SetOperations.union(newItems, updatedItems));
        stitcherCore.addCandidateElements(itemIndex.getAllItems());
        newStitches = stitcherCore.stitch();

        // Remove stitches involving <deletedItems> and <updatedItems> from <tacks>
        removeStitches(SetOperations.union(deletedItems, updatedItems), accumulatedStitches);

        // Add calculated stitches
        addStitches(newStitches, accumulatedStitches);

        return newStitches;
    }

    @Override
    public Map<T, Collection<T>> increment(Collection<T> newItems, Collection<T> updatedItems,
            Collection<T> deletedItems, Map<T, Collection<T>> potentialStitches) {

        Verifier.illegalArgumentIfNull(newItems, updatedItems, deletedItems, potentialStitches);

        // Index
        itemIndex.removeItems(deletedItems);
        itemIndex.addItems(newItems);

        // Remove stitches involving removed items from <accumulatedStitches>
        removeStitches(deletedItems, accumulatedStitches);

        // Stitch!
        Map<T, Collection<T>> newStitches;
        // stitcherCore.clearBaseElements(); // Resetting microstitcher (1/2)
        // stitcherCore.clearCandidateElements(); // Resetting microstitcher (2/2)
        newStitches = stitcherCore.stitch(potentialStitches);

        // Remove stitches involving <deletedItems> and <updatedItems> from <tacks>
        removeStitches(SetOperations.union(deletedItems, updatedItems), accumulatedStitches);

        // Add calculated stitches
        addStitches(newStitches, accumulatedStitches);

        return newStitches;
    }

    @Override
    public Map<T, Collection<T>> getAccumulatedStitches() {
        Map<T, Collection<T>> stitches = new ConcurrentHashMap<T, Collection<T>>();

        accumulatedStitches.keySet().parallelStream().forEach((String id) -> {
            Collection<T> stitchedTo = toTCollection(accumulatedStitches.get(id));
            stitches.put(itemIndex.getItemById(id), stitchedTo);
        });

        return stitches;
    }

    @Override
    public Map<String, Collection<String>> getAccumulatedStitchesById() {
        return Collections.unmodifiableMap(accumulatedStitches); // Read only!
    }

    private void addStitches(Map<T, Collection<T>> newStitches, Map<String, Collection<String>> accumulated) {

        for (T current : newStitches.keySet()) {
            String currentId = current.getId();
            Collection<String> stitchesForCurrent, stitchesForDest;
            Collection<T> stitchedFromCurrent = newStitches.get(current);

            // No stitches for the current item -> Nothing to do (skip iteration)
            if (stitchedFromCurrent.isEmpty()) {
                continue;
            }

            // Stitches from <current> to its destination items: Retrieve collection where they
            // should be saved (create if it does not exist)
            stitchesForCurrent = accumulated.get(currentId);
            if (stitchesForCurrent == null) {
                // stitchesForThisId = new ArrayList<String>(); // Warning! Repeated elements?
                stitchesForCurrent = new HashSet<String>(); // This one should be better.
                accumulated.put(currentId, stitchesForCurrent);
            }
            stitchesForCurrent.addAll(toStringCollection(stitchedFromCurrent)); // Saving...

            // Stitches from destination items to <current>: Retrieve collection where they should
            // be saved (create if it does not exist)
            for (T dest : stitchedFromCurrent) {
                String destId = dest.getId();
                stitchesForDest = accumulated.get(destId);
                if (stitchesForDest == null) {
                    // stitchesForDest = new ArrayList<String>(); // Warning! Repeated elements?
                    stitchesForDest = new HashSet<String>(); // This one should be better.
                    accumulated.put(destId, stitchesForDest);
                }
                stitchesForDest.add(currentId); // Saving...
            }
        }
    }

    private void removeStitches(Collection<T> itemsToBeRemoved, Map<String, Collection<String>> stitches) {
        Collection<String> itemsById = toStringCollection(itemsToBeRemoved);

        // NOTE: This approach is correct if the stitches are symmetric!
        // Should be modified if they are not. In such case, all the entries in the map should be
        // checked.
        for (String id : itemsById) {
            Collection<String> currentStitches = stitches.get(id);
            if (currentStitches != null) {
                for (String stitchedToId : currentStitches) {
                    stitches.get(stitchedToId).remove(id);
                    if (stitches.get(stitchedToId).isEmpty()) {
                        stitches.remove(stitchedToId);
                    }
                }
                stitches.remove(id);
            }
        }
    }

    private Collection<String> toStringCollection(Collection<T> tColl) {
        return tColl.parallelStream().map((T item) -> {
            return item.getId();
        }).collect(Collectors.toList());
    }

    private Collection<T> toTCollection(Collection<String> stringColl) {
        return stringColl.parallelStream().map((String id) -> {
            return itemIndex.getItemById(id);
        }).collect(Collectors.toList());
    }

    // METHODS - END -------------------------------------------------------------------------------

}
