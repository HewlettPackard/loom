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

/**
 * This helper interface allows comparing different objects to check their degree of similarity. It
 * is meant to be used in the <tt>Stitcher</tt> to compare base and candidate elements.
 * <tt>Filter</tt>s may be used to avoid comparisons between base elements and certain candidates
 * expected not to match it.
 *
 * @param <B> Base item as used in the <tt>Stitcher</tt>.
 * @param <C> Candidate item as used in the <tt>Stitcher</tt>.
 */
public interface StitchChecker<B, C> {

    /**
     * Compares the base element and candidate element to decide which is their degree of
     * similarity.
     * 
     * @param baseElement First object to be compared.
     * @param candidateElement Second object to be compared.
     * @return A float in the range <tt>[0.0, 1.0]</tt>. <tt>0.0</tt> implies that these objects are
     *         different. <tt>1.0</tt> implies that these objects are the same one. <tt>0.5</tt>
     *         means that no decision about their similarity could be made.
     */
    public double checkStitch(B baseElement, C candidateElement);

}
