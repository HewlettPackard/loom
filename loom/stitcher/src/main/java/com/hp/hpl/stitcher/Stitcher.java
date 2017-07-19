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

/**
 * Objects implementing this interface may receive a collection of base elements (<tt>B</tt>) and
 * one of candidate elements (<tt>C</tt>) and find whether they are related or not according to
 * criteria specified in a <tt>StitchChecker</tt>.
 *
 * @param <B> Base elements' type (elements whose relationships are to be found).
 * @param <C> Candidate elements' type (elements which are going to be related).
 */
public interface Stitcher<B, C> {

    /**
     * Finds relationships between the elements in the base collection and the ones in the candidate
     * collection.
     *
     * @return For each element in the base collection, those in the candidate collection which are
     *         matches.
     */
    public Map<B, Collection<C>> stitch();

    /**
     * Finds the relationships between a base element and the ones in the candidate collections.
     * This element does not need to be part of the base element collection. Notice that this
     * element will be checked against all the base filters that this <tt>Stitcher</tt> contains.
     *
     * @param baseElement Base element whose relationships will be found.
     * @return Elements in the candidate collection which are matches, or <tt>null</tt> if the base
     *         element does not comply with all the filters.
     */
    public Collection<C> stitch(B baseElement);

    /**
     * Returns the similarity results calculated the last time a <tt>stitch</tt> method was called.
     *
     * @return A Map containing the similarity result for each base element and candidate element
     *         that were compared in the last execution.
     */
    public Map<B, Map<C, Double>> getLastRunSimilarities();

    /**
     * Adds elements to the base collection.
     *
     * @param elements Those elements which will be added to the base collection for future
     *        stitching.
     */
    public void addBaseElements(Collection<B> elements);

    /**
     * Removes all the given elements from the base collection.
     *
     * @param elements Elements to be removed. If an element does not exist in the base collection,
     *        it is ignored.
     * @return <tt>true</tt> if the base element collection has changed.
     */
    public boolean removeBaseElements(Collection<B> elements);

    /**
     * Deletes all the elements in the base collection.
     */
    public void clearBaseElements();

    /**
     * Adds elements to the candidate collection.
     *
     * @param elements Those elements which will be added to the candidate collection for future
     *        stitching.
     */
    public void addCandidateElements(Collection<C> elements);

    /**
     * Removes all the given elements from the candidate collection.
     *
     * @param elements Elements to be removed. If an element does not exist in the candidate
     *        collection, it is ignored.
     * @return <tt>true</tt> if the candidate element collection has changed.
     */
    public boolean removeCandidateElements(Collection<C> elements);

    /**
     * Deletes all the elements in the candidate collection.
     */
    public void clearCandidateElements();

    /**
     * Adds a <tt>StitchChecker</tt> that will be used during the stitching process to check the
     * similarity between a base and a candidate element.
     *
     * @param stitchChecker The aforementioned <tt>StitchChecker</tt>.
     */
    public void addStitchChecker(StitchChecker<B, C> stitchChecker);

    /**
     * Removes a <tt>StitchChecker</tt> which was meant be used during the stitching process to
     * check the similarity between a base and a candidate element.
     *
     * @param stitchChecker The aforementioned <tt>StitchChecker</tt>.
     * @return <tt>true</tt> if the received <tt>StitchChecker</tt> existed in its corresponding
     *         list.
     */
    public boolean removeStitchChecker(StitchChecker<B, C> stitchChecker);

    /**
     * Gets the current <tt>StitchChecker</tt>s to be used during the stitching process.
     *
     * @return The aforementioned <tt>StitchChecker</tt>s.
     */
    public Collection<StitchChecker<B, C>> getStitchCheckers();

    /**
     * Returns whether the calculated similarities between base and candidate elements should be
     * saved to be accessed later.
     *
     * @return <tt>true</tt> if similarities will be saved.
     */
    boolean getSaveSimilarities();

    /**
     * Sets whether the calculated similarities between base and candidate elements should be saved
     * to be accessed later.
     *
     * @param saveSimilarities Setter for saveSimilarities.
     */
    void setSaveSimilarities(boolean saveSimilarities);

    /**
     * Returns the acceptance threshold above which two elements are considered a stitch.
     *
     * @return The aforementioned acceptance threshold.
     */
    double getAcceptanceThreshold();

    /**
     * Sets the acceptance threshold above which two elements are considered a stitch.
     *
     * @param acceptanceThreshold The new acceptance threshold.
     */
    void setAcceptanceThreshold(double acceptanceThreshold);

}
