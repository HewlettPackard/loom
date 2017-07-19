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

public interface SelectiveStitcher<B, C> extends Stitcher<B, C> {

    /**
     * Given a list of potential stitches (for each base element, a collection of candidate elements
     * is provided), verifies which of them are actual stitches. All the provided elements will be
     * checked against the filters (base and candidate) that this <tt>Stitcher</tt> contains.
     * 
     * @param potentialStitches A map of base elements and collections of candidate elements which
     *        may potentially be stitched.
     * @return All the verified stitches from the input map.
     */
    public Map<B, Collection<C>> stitch(Map<B, Collection<C>> potentialStitches);

}
