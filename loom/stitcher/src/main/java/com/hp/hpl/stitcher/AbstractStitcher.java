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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hp.hpl.stitcher.extras.Verifier;

/**
 * Abstract implementation of the <tt>Stitcher</tt> interface.
 *
 * @param <B> Base elements' type, as defined in <tt>Stitcher</tt>.
 * @param <C> Candidate elements' type, as defined in <tt>Stitcher</tt>.
 */
public abstract class AbstractStitcher<B, C> implements FilteredStitcher<B, C> {

    // VARIABLES -----------------------------------------------------------------------------------

    public static final double DEFAULT_ACCEPTANCE_THRESHOLD = 1.0;
    public static final boolean SAVE_SIMILARITIES_BY_DEFAULT = false;

    protected static final int DEFAULT_BASE_CAPACITY = 1000;
    protected static final int DEFAULT_CANDIDATE_CAPACITY = 1000;

    protected double acceptanceThreshold;
    protected Collection<StitchChecker<B, C>> stitchCheckers;
    protected Collection<Filter<B>> bFilters;
    protected Collection<Filter<C>> cFilters;
    protected Collection<BaseDependentFilter<B, C>> baseDependentFilters;
    protected Collection<B> baseElements;
    protected Collection<C> candidateElements;
    protected Map<B, Map<C, Double>> lastRunSimilarities;
    protected boolean saveSimilarities;
    protected boolean verifyStitchChecker; // Some implementations may run without a StitchChecker

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    /**
     * Constructs an AbstractStitcher.
     */
    public AbstractStitcher() {
        this(DEFAULT_BASE_CAPACITY, DEFAULT_CANDIDATE_CAPACITY);
    }

    /**
     * Constructs an AbstractStitcher with the specified capacities for the base and candidate
     * element collections. This is meant to prevent these lists from auto-expanding when a large
     * amount of elements is added, reducing resource consumption.
     *
     * @param baseInitialCapacity Initial capacity for the base element list.
     * @param candidateInitialCapacity Initial capacity for the candidate element list.
     */
    public AbstractStitcher(int baseInitialCapacity, int candidateInitialCapacity) {
        if (baseInitialCapacity <= 0) {
            baseInitialCapacity = DEFAULT_BASE_CAPACITY;
        }
        if (candidateInitialCapacity <= 0) {
            candidateInitialCapacity = DEFAULT_CANDIDATE_CAPACITY;
        }

        acceptanceThreshold = DEFAULT_ACCEPTANCE_THRESHOLD;
        saveSimilarities = SAVE_SIMILARITIES_BY_DEFAULT;
        bFilters = new ArrayList<Filter<B>>();
        cFilters = new ArrayList<Filter<C>>();
        baseDependentFilters = new ArrayList<BaseDependentFilter<B, C>>();
        baseElements = new ArrayList<B>(baseInitialCapacity);
        candidateElements = new ArrayList<C>(candidateInitialCapacity);
        stitchCheckers = new ArrayList<StitchChecker<B, C>>();
        verifyStitchChecker = true;
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Helper method for the <tt>AbstractStitcher</tt> class. Receives two filtered lists: 1) base
     * elements and 2) candidate elements, and the data structure where the stitches are going to be
     * saved. It does the actual stitching between the two groups.
     *
     * @param filteredBaseElements Collection of base elements where those that did not fulfil
     *        certain conditions have been removed.
     * @param filteredCandidateElements Collection of candidate elements where those that did not
     *        fulfil certain conditions have been removed.
     * @param stitches Map where the stitches are to be saved. It should be ready to use.
     * @return For each base element, a collection of its candidate elements considered matches.
     */
    protected abstract Map<B, Collection<C>> subStitch(Collection<B> filteredBaseElements,
            Collection<C> filteredCandidateElements, Map<B, Collection<C>> stitches);

    /**
     * The <tt>stitch()</tt> and <tt>stitch(B)</tt> methods return a <tt>Map</tt> which contains for
     * each base element a <tt>Collection</tt> of candidate elements who are related. This method
     * returns an implementation of such <tt>Collection</tt>. Depending on the elements' types,
     * different data structures (e.g. <tt>List</tt>, <tt>Set</tt>, etc.) may be desired. It is up
     * to the implementer to decide which one should be used.
     *
     * @return A new data structure which will be store in the <tt>Map</tt> used for stitching.
     */
    protected abstract Collection<C> newDataStructureForStitching();

    @Override
    public Map<B, Collection<C>> stitch() {
        if (verifyStitchChecker) {
            Verifier.illegalStateIfEmtpy(stitchCheckers);
        }

        // Filter base elements... (sequential)
        // Collection<B> filteredB = baseElements;
        // for (Filter<B> f : bFilters) {
        // filteredB = f.filter(filteredB);
        // }

        // ...and candidate elements (sequential)
        // Collection<C> filteredC = candidateElements;
        // for (Filter<C> f : cFilters) {
        // filteredC = f.filter(filteredC);
        // }

        // Filter base elements...
        Collection<B> filteredB =
                baseElements.parallelStream().filter(b -> isAccepted(b, bFilters)).collect(Collectors.toList());

        // ...and candidate elements
        Collection<C> filteredC =
                candidateElements.parallelStream().filter(c -> isAccepted(c, cFilters)).collect(Collectors.toList());

        // Create Map where stitches will be saved
        Map<B, Collection<C>> stitches = new HashMap<B, Collection<C>>(filteredB.size(), 1.0f);
        for (B b : filteredB) {
            stitches.put(b, newDataStructureForStitching());
        }

        // Create Map where similarities will be saved (if necessary)
        if (saveSimilarities) {
            lastRunSimilarities = new HashMap<B, Map<C, Double>>(filteredB.size(), 1.0f);
            for (B b : filteredB) {
                lastRunSimilarities.put(b, new ConcurrentHashMap<C, Double>(filteredC.size(), 1.0f));
            }
        } else {
            lastRunSimilarities = null;
        }

        // Record linkage
        return subStitch(filteredB, filteredC, stitches);
    }

    @Override
    public Collection<C> stitch(final B baseElement) {
        if (verifyStitchChecker) {
            Verifier.illegalStateIfEmtpy(stitchCheckers);
        }
        Verifier.illegalArgumentIfNull(baseElement);

        // Filter base element... (method ends here if it is not accepted)
        if (!isAccepted(baseElement, bFilters)) {
            return null;
        }

        // Filter candidate elements (old)
        // Collection<C> filteredC = candidateElements;
        // for (Filter<C> f : cFilters) {
        // filteredC = f.filter(filteredC);
        // }

        // ...and candidate elements (old)
        Collection<C> filteredC =
                candidateElements.parallelStream().filter(c -> isAccepted(c, cFilters)).collect(Collectors.toList());

        // Create Map where stitches will be saved
        Map<B, Collection<C>> stitches = new HashMap<B, Collection<C>>(1, 1.0f);
        stitches.put(baseElement, newDataStructureForStitching());

        // Create Map where similarities will be saved (if necessary)
        if (saveSimilarities) {
            lastRunSimilarities = new HashMap<B, Map<C, Double>>(1, 1.0f);
            lastRunSimilarities.put(baseElement, new ConcurrentHashMap<C, Double>(filteredC.size(), 1.0f));
        } else {
            lastRunSimilarities = null;
        }

        // Record linkage
        return subStitch(Collections.singletonList(baseElement), filteredC, stitches).get(baseElement);
    }

    @Override
    public Map<B, Map<C, Double>> getLastRunSimilarities() {
        if (!saveSimilarities) {
            throw new IllegalStateException("Similarities are not to be saved");
        }
        if (lastRunSimilarities == null) {
            throw new IllegalStateException("No similarities currently saved");
        }

        return Collections.unmodifiableMap(lastRunSimilarities);
    }

    @Override
    public boolean getSaveSimilarities() {
        return saveSimilarities;
    }

    @Override
    public void setSaveSimilarities(final boolean saveSimilarities) {
        if (!saveSimilarities) {
            lastRunSimilarities = null;
        }
        this.saveSimilarities = saveSimilarities;
    }

    @Override
    public double getAcceptanceThreshold() {
        return acceptanceThreshold;
    }

    @Override
    public void setAcceptanceThreshold(final double acceptanceThreshold) {
        if (acceptanceThreshold < 0.0 || acceptanceThreshold > 1.0) {
            throw new IllegalArgumentException("Acceptance threshold out of range");
        }

        this.acceptanceThreshold = acceptanceThreshold;
    }

    @Override
    public void addBaseElements(final Collection<B> elements) {
        Verifier.illegalArgumentIfNull(elements);

        baseElements.addAll(elements);
    }

    @Override
    public boolean removeBaseElements(final Collection<B> elements) {
        Verifier.illegalArgumentIfNull(elements);

        return baseElements.removeAll(elements);
    }

    @Override
    public void clearBaseElements() {
        baseElements.clear();
    }

    @Override
    public void addCandidateElements(final Collection<C> elements) {
        Verifier.illegalArgumentIfNull(elements);

        candidateElements.addAll(elements);
    }

    @Override
    public boolean removeCandidateElements(final Collection<C> elements) {
        Verifier.illegalArgumentIfNull(elements);

        return candidateElements.removeAll(elements);
    }

    @Override
    public void clearCandidateElements() {
        candidateElements.clear();
    }

    @Override
    public void addStitchChecker(final StitchChecker<B, C> stitchChecker) {
        Verifier.illegalArgumentIfNull(stitchChecker);

        stitchCheckers.add(stitchChecker);
    }

    @Override
    public boolean removeStitchChecker(final StitchChecker<B, C> stitchChecker) {
        return stitchCheckers.remove(stitchChecker);
    }

    @Override
    public Collection<StitchChecker<B, C>> getStitchCheckers() {
        return stitchCheckers;
    }

    @Override
    public void addBaseFilter(final Filter<B> baseFilter) {
        Verifier.illegalArgumentIfNull(baseFilter);

        bFilters.add(baseFilter);
    }

    @Override
    public boolean removeBaseFilter(final Filter<B> baseFilter) {
        Verifier.illegalArgumentIfNull(baseFilter);

        return bFilters.remove(baseFilter);
    }

    @Override
    public void addCandidateFilter(final Filter<C> candidateFilter) {
        Verifier.illegalArgumentIfNull(candidateFilter);

        cFilters.add(candidateFilter);
    }

    @Override
    public boolean removeCandidateFilter(final Filter<C> candidateFilter) {
        Verifier.illegalArgumentIfNull(candidateFilter);

        return cFilters.remove(candidateFilter);
    }

    @Override
    public void addBaseDependentFilter(final BaseDependentFilter<B, C> baseDependentFilter) {
        baseDependentFilters.add(baseDependentFilter);
    }

    @Override
    public boolean removeBaseDependentFilter(final BaseDependentFilter<B, C> baseDependentFilter) {
        return baseDependentFilters.remove(baseDependentFilter);
    }

    /**
     * Checks whether an element (either base or candidate) is accepted by the current filters.
     *
     * @param<T> Type of element.
     * 
     * @param element Element to be checked.
     * @param filters Filters to be applied to the aforementioned element.
     * @return <tt>true</tt> if this element complies with all the filters, consequently taking part
     *         in the stitching process.
     */
    protected <T> boolean isAccepted(final T element, final Collection<Filter<T>> filters) {

        // Filters are applied sequentially. As soon as the element does not comply with one of
        // them, this method ends.
        for (Filter<T> f : filters) {
            if (f.filter(element)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a candidate element is accepted by the current base dependent filters.
     *
     * @param baseElement Base element to be provided to the base dependent filter.
     * @param candidateElement Candidate element to be checked
     * @param filters Base dependent filters to be applied to the aforementioned candidate element.
     * @return <tt>true</tt> if this element complies with all the filters, consequently taking part
     *         in the stitching process.
     */
    protected boolean isAccepted(final B baseElement, final C candidateElement,
            final Collection<BaseDependentFilter<B, C>> filters) {

        // Filters are applied sequentially. As soon as the element does not comply with one of
        // them, this method ends.
        for (BaseDependentFilter<B, C> f : filters) {
            if (f.filter(baseElement, candidateElement)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method which checks whether a base element and a candidate element have been already
     * stitched. If not, it tries to stitch them.
     *
     * @param currentStitches Stitches registered so far in the stitching process.
     * @param stitchChecker <tt>StitchChecker</tt> to be used to compare the given base and
     *        candidate elements.
     * @param baseElement Given base element.
     * @param candidateElement Given candidate element.
     * @return <tt>true</tt> if this elements are a stitch that has not been saved yet (hence it
     *         should be).
     */
    protected boolean stitchToBeSaved(final Map<B, Collection<C>> currentStitches,
            final StitchChecker<B, C> stitchChecker, final B baseElement, final C candidateElement) {
        boolean alreadySaved = false;

        // Has this stitch been already saved?
        if (currentStitches.get(baseElement).contains(candidateElement)) {
            // Saved -> No need to save it again
            alreadySaved = true;

            // If similarities are not to be saved -> Finish here
            if (!saveSimilarities) {
                return false;
            }

            // If these elements have the highest similarity, there is no need to check if it can be
            // any higher -> Finish here
            if (lastRunSimilarities.get(baseElement).get(candidateElement) == 1.0) {
                return false;
            }
        }

        // Calculate similarity
        double similarity = stitchChecker.checkStitch(baseElement, candidateElement);

        if (saveSimilarities) {
            // If "No previous similarity saved" or "new similarity is higher" then:
            if ((lastRunSimilarities.get(baseElement).get(candidateElement) == null)
                    || (lastRunSimilarities.get(baseElement).get(candidateElement) < similarity)) {
                // Save similarity
                lastRunSimilarities.get(baseElement).put(candidateElement, similarity);
            }
        }

        if (alreadySaved) {
            return false;
        } else {
            // Use calculated similarity to decide whether it should be saved or not
            return similarity >= acceptanceThreshold;
        }

    }
    // METHODS - END -------------------------------------------------------------------------------

}
