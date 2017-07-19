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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <tt>Stitcher</tt> implementation which finds the relationships between its elements by comparing
 * each base element against each candidate element. This class uses no other helper classes.
 *
 * @param <B> Base elements' type (elements whose relationship are to be found).
 * @param <C> Candidate elements' type (elements which are going to be related).
 */
public class BruteStitcher<B, C> extends AbstractStitcher<B, C> {

    public BruteStitcher() {
        super();
    }

    public BruteStitcher(int baseInitialCapacity, int candidateInitialCapacity) {
        super(baseInitialCapacity, candidateInitialCapacity);
    }

    /**
     * This is the old implementation of the subStitch method before using Streams. It is keep here
     * in case it may be useful for some purpose in the future. However, as it is meant <u>not</u>
     * to be accessed from any other class, it is declared private.
     * 
     * @param filteredBaseElements
     * @param filteredCandidateElements
     * @param stitches
     * @return
     */
    @SuppressWarnings("unused")
    @Deprecated
    private Map<B, Collection<C>> oldSubStitch(Collection<B> filteredBaseElements,
            Collection<C> filteredCandidateElements, Map<B, Collection<C>> stitches) {
        for (StitchChecker<B, C> stitchChecker : stitchCheckers) {
            for (B b : filteredBaseElements) {
                for (C c : filteredCandidateElements) {
                    if (stitchToBeSaved(stitches, stitchChecker, b, c)) {
                        // Match between this objects
                        stitches.get(b).add(c); // Add element to stitch table
                    }
                }
            }
        }

        return stitches;
    }

    @Override
    protected Map<B, Collection<C>> subStitch(Collection<B> filteredBaseElements,
            Collection<C> filteredCandidateElements, Map<B, Collection<C>> stitches) {

        // All the StitchCheckers contained in this Stitcher are going to be applied sequentially.
        // The reason is to prevent concurrency related errors when the stitches are saved. Besides,
        // as the process for each StitchChecker is already parallelised, this sequential execution
        // should entail no delay.
        for (StitchChecker<B, C> stitchChecker : stitchCheckers) {

            // Parallel execution
            filteredBaseElements.parallelStream().forEach(b -> {
                List<C> matches = filteredCandidateElements.stream()
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
