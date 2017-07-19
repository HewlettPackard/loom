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

/**
 * Objects implementing this interface may receive type C objects and decide whether they fulfil
 * certain conditions or not. This conditions shall depend on a specific type B object.
 * <p>
 * 
 * This may be useful to reduce the amount of comparisons between base and candidate elements in
 * Stitcher.
 *
 * @param <B> The type of the element on which the filter will depend.
 * @param <C> The type of elements to be filtered.
 */
public interface BaseDependentFilter<B, C> {

    /**
     * This method takes a base and a candidate element and decides whether the latter complies with
     * certain conditions so as to go through the stitching process.
     * 
     * @param baseElement Base element on which the filtering process will depend on.
     * @param candidateElement Candidate element to be analysed.
     * @return <tt>true</tt> if this candidate element should be removed from the stitching sample.
     */
    public boolean filter(B baseElement, C candidateElement);

    /**
     * This method takes a base element and a list of candidate elements and returns a new
     * collection in which those candidate elements which did not comply with certain conditions
     * have been removed.
     * 
     * @param baseElement Base element on which the filtering process will depend on.
     * @param candidateElements List of candidate elements to be filtered.
     * @return A <u>new</u> list containing only those candidate elements which fulfil the filtering
     *         conditions.
     */
    public Collection<C> filter(B baseElement, Collection<C> candidateElements);

}
