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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hp.hpl.stitcher.extras.Verifier;

/**
 * <tt>Stitcher</tt> implementation which for each base element only checks those candidate elements
 * which comply with the base dependent filters, speeding up the process.
 * <p>
 * 
 * <b>NOTE 1:</b> A <tt>ConditionedStitcher</tt> with no <tt>BaseDependentFilter</tt>s will require
 * the same (or maybe slightly more) resources as a <tt>BruteStitcher</tt>, hence providing no
 * benefit.
 * <p>
 * 
 * <b>NOTE 2:</b> The filtering operation should be <i>considerably lighter</i> than the stitching
 * operation, as the purpose of this conditioned stitcher is to reduce the running time by avoiding
 * checking expectedly unmatched stitches. If the filtering operation is <i>heavier</i>, then this
 * conditional stitcher brings no improvement.
 * <p>
 *
 * @param <B> Base elements' type (elements whose relationship are to be found).
 * @param <C> Candidate elements' type (elements which are going to be related).
 */
public class ConditionedStitcher<B, C> extends AbstractStitcher<B, C> implements SelectiveStitcher<B, C> {

    public ConditionedStitcher() {
        super();
    }

    public ConditionedStitcher(int baseInitialCapacity, int candidateInitialCapacity) {
        super(baseInitialCapacity, candidateInitialCapacity);
    }

    @Override
    public Map<B, Collection<C>> stitch(Map<B, Collection<C>> potentialStitches) {
        if (verifyStitchChecker) {
            Verifier.illegalStateIfEmtpy(stitchCheckers);
        }

        Map<B, Collection<C>> filteredPotentialStitches = new HashMap<B, Collection<C>>(potentialStitches);

        // Filter base elements...
        potentialStitches.keySet().parallelStream().forEach((B b) -> {
            if (isAccepted(b, bFilters)) {
                // ...and candidate elements
                filteredPotentialStitches.get(b).removeIf((C c) -> {
                    return !isAccepted(c, cFilters);
                });
            } else {
                filteredPotentialStitches.remove(b);
            }
        });

        // Reached this point, all the potential stitches in <filteredPotentialStitches> are to be
        // checked.

        // Retrieve base elements to be stitched
        Collection<B> filteredB = filteredPotentialStitches.keySet();

        // Create Map where stitches will be saved
        Map<B, Collection<C>> stitches = new HashMap<B, Collection<C>>(filteredB.size(), 1.0f);
        for (B b : filteredB) {
            stitches.put(b, newDataStructureForStitching());
        }

        // Create Map where similarities will be saved
        if (saveSimilarities) {
            lastRunSimilarities = new HashMap<B, Map<C, Double>>(filteredB.size(), 1.0f);
            for (B b : filteredB) {
                lastRunSimilarities.put(b,
                        new ConcurrentHashMap<C, Double>(filteredPotentialStitches.get(b).size(), 1.0f));
            }
        } else {
            lastRunSimilarities = null;
        }

        // for (StitchChecker<B, C> stitchChecker : stitchCheckers) {
        //
        // // Parallel execution
        // filteredB.parallelStream().forEach(
        // b -> {
        // List<C> matches =
        // filteredPotentialStitches.get(b).stream()
        // .filter(c -> isAccepted(b, c, baseDependentFilters))
        // .filter(c -> stitchToBeSaved(stitches, stitchChecker, b, c))
        // .collect(Collectors.toList());
        // stitches.get(b).addAll(matches);
        // });
        // }

        // Parallel execution
        filteredB.parallelStream().forEach(b -> {
            // Generate map for temporary stitches
            Map<B, Collection<C>> tempStitches = new HashMap<B, Collection<C>>(1, 1.0f);
            tempStitches.put(b, newDataStructureForStitching());

            // Stitch and save results in our local map
            Collection<C> matches = subStitch(Arrays.asList(b), filteredPotentialStitches.get(b), tempStitches).get(b);
            stitches.get(b).addAll(matches);
        });

        return stitches;
    }

    @Override
    protected Map<B, Collection<C>> subStitch(Collection<B> filteredBaseElements,
            Collection<C> filteredCandidateElements, Map<B, Collection<C>> stitches) {

        // Old sequential implementation. Keep here for debugging purposes.
        // for (StitchChecker<B, C> stitchChecker : stitchCheckers) {
        // for (B b : filteredBaseElements) {
        // for (C c : filteredCandidateElements) {
        // if (isAccepted(b, c, baseDependentFilters)) {
        // if (stitchToBeSaved(stitches, stitchChecker, b, c)) {
        // // Match between this objects
        // stitches.get(b).add(c); // Add element to stitch table
        // }
        // }
        // }
        // }
        // }

        // All the StitchCheckers contained in this Stitcher are going to be applied sequentially.
        // The reason is to prevent concurrency related errors when the stitches are saved. Besides,
        // as the process for each StitchChecker is already parallelised, this sequential execution
        // should entail no delay.
        for (StitchChecker<B, C> stitchChecker : stitchCheckers) {

            // Parallel execution
            filteredBaseElements.parallelStream().forEach(b -> {
                List<C> matches = filteredCandidateElements.stream().filter(c -> isAccepted(b, c, baseDependentFilters))
                        .filter(c -> stitchToBeSaved(stitches, stitchChecker, b, c)).collect(Collectors.toList());
                stitches.get(b).addAll(matches);
            });
        }

        return stitches;
    }

    @Override
    protected Collection<C> newDataStructureForStitching() {
        return new ArrayList<C>();
    }

}
