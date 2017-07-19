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
 * Objects implementing this interface may receive type T objects and decide whether they fulfil
 * certain conditions or not. This may be useful to remove specific objects in a collection.
 *
 * @param <T> The type of elements to be filtered.
 */
public interface Filter<T> {

    /**
     * This method analyses an element and decides whether it should be removed from the sample or
     * not.
     * 
     * @param element Element to be analysed.
     * @return <tt>true</tt> if this element should be removed from the sample.
     */
    public boolean filter(T element);

    /**
     * This method receives a list of elements to be analysed and returns a <u>new</u> one in which
     * those that did not fulfil the required conditions have been removed.
     * 
     * @param elements List of elements to be filtered.
     * @return A <u>new</u> list containing only those objects which fulfil the filtering
     *         conditions.
     */
    public Collection<T> filter(Collection<T> elements);

}
