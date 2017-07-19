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
package com.hp.hpl.stitcher.extras;

import java.util.Set;

import com.hp.hpl.stitcher.StitchChecker;

public class CoraRecordStitchChecker implements StitchChecker<CoraRecord, CoraRecord> {

    private double authorsWeight, titleWeight;

    public CoraRecordStitchChecker() {
        authorsWeight = titleWeight = 0.5;
    }

    public double getAuthorsWeight() {
        return authorsWeight;
    }

    public double getTitleWeight() {
        return titleWeight;
    }

    public void setWeights(double authors, double titles) {
        if (authors + titles != 1.0) {
            throw new IllegalArgumentException("Weights should sum up to one");
        }

        authorsWeight = authors;
        titleWeight = titles;
    }

    @Override
    public double checkStitch(CoraRecord baseElement, CoraRecord candidateElement) {

        // Calculate author similarity
        double authorsJaccard =
                jaccardSimilarity(baseElement.getAuthorsShingles(), candidateElement.getAuthorsShingles());

        // Calculate title similarity
        double titleJaccard = jaccardSimilarity(baseElement.getTitleShingles(), candidateElement.getTitleShingles());

        return authorsWeight * authorsJaccard + titleWeight + titleJaccard;
    }

    public static <T> double jaccardSimilarity(Set<T> set1, Set<T> set2) {
        if (set1.size() == 0 || set2.size() == 0) {
            return 0.0;
        }

        double intersectionSize = SetOperations.intersection(set1, set2).size();
        double unionSize = SetOperations.union(set1, set2).size();

        return intersectionSize / unionSize;
    }
}
