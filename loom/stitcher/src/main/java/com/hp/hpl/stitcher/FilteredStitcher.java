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
 * Extension of <tt>Stitcher</tt>. Some objects may be disregarded attending to conditions defined
 * by <tt>Filter</tt>s.
 *
 * @param <B> Base elements' type (elements whose relationships are to be found).
 * @param <C> Candidate elements' type (elements which are going to be related).
 */
public interface FilteredStitcher<B, C> extends Stitcher<B, C> {

    /**
     * Adds a <tt>Filter</tt> to be used during the stitching process to prevent certain base
     * objects to be matched. Notice that several <tt>Filter</tt>s may be used.
     *
     * @param baseFilter The aforementioned <tt>Filter</tt>.
     */
    public void addBaseFilter(Filter<B> baseFilter);

    /**
     * Removes a <tt>Filter</tt> which was meant to be used during the stitching process to filter
     * base objects. If such a <tt>Filter</tt> does not exists, does nothing.
     *
     * @param baseFilter The aforementioned <tt>Filter</tt>.
     * @return <tt>true</tt> if the received filter existed in its corresponding list.
     */
    public boolean removeBaseFilter(Filter<B> baseFilter);

    /**
     * Adds a <tt>Filter</tt> to be used during the stitching process to prevent certain candidate
     * objects to be matched. Notice that several <tt>Filter</tt>s may be used.
     *
     * @param candidateFilter The aforementioned <tt>Filter</tt>.
     */
    public void addCandidateFilter(Filter<C> candidateFilter);

    /**
     * Removes a <tt>Filter</tt> which was meant to be used during the stitching process to filter
     * candidate objects. If such a <tt>Filter</tt> does not exists, does nothing.
     *
     * @param candidateFilter The aforementioned <tt>Filter</tt>.
     * @return <tt>true</tt> if the received filter existed in its corresponding list.
     */
    public boolean removeCandidateFilter(Filter<C> candidateFilter);

    /**
     * Adds a <tt>BaseDependentFilter</tt> to be used during the stitching process to reduce the
     * amount of candidate objects to be matched. Notice that several <tt>BaseDependentFilter</tt> s
     * may be used.
     *
     * @param baseDependentFilter The aforementioned <tt>BaseDependentFilter</tt>.
     */
    public void addBaseDependentFilter(BaseDependentFilter<B, C> baseDependentFilter);

    /**
     * Removes a <tt>BaseDependentFilter</tt> which was meant to be used during the stitching
     * process to filter candidate objects. If such a <tt>BaseDependentFilter</tt> does not exists,
     * does nothing.
     *
     * @param baseDependentFilter The aforementioned <tt>BaseDependentFilter</tt>.
     * @return <tt>true</tt> if the received filter existed in its corresponding list.
     */
    public boolean removeBaseDependentFilter(BaseDependentFilter<B, C> baseDependentFilter);

}
