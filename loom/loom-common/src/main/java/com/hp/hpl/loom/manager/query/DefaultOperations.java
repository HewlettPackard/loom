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
package com.hp.hpl.loom.manager.query;

/**
 * Enumeration for the default operations.
 */
public enum DefaultOperations {
    /**
     * test.
     */
    IDENTITY("IDENTITY"),
    /**
     * Group by operation.
     */
    GROUP_BY("GROUP_BY"),
    /**
     * Sort by operation.
     */
    SORT_BY("SORT_BY"),
    /**
     * Braid operation splits data into sub groups (by count per group).
     */
    BRAID("BRAID"),
    /**
     * Pyramid operation.
     */
    PYRAMID("PYRAMID"),
    /**
     * Kmean operation.
     */
    KMEANS("KMEANS"),
    /**
     * Bucketize opeartion.
     */
    BUCKETIZE("BUCKETIZE"),
    /**
     * Filter based on a string operation.
     */
    FILTER_STRING("FILTER_STRING"),
    /**
     * Filter based on a related item.
     */
    FILTER_RELATED("FILTER_RELATED"),
    /**
     * Get the first n operation.
     */
    GET_FIRST_N("GET_FIRST_N"),
    /**
     * Percentiles operation.
     */
    PERCENTILES("PERCENTILES"),
    /**
     * Summary operation.
     */
    SUMMARY("SUMMARY"),
    /**
     * Distribute operation.
     */
    DISTRIBUTE("DISTRIBUTE"),
    /**
     * Grid clustering operation.
     */
    GRID_CLUSTERING("GRID_CLUSTERING"),
    /**
     * Filter by region operation.
     */
    FILTER_BY_REGION("FILTER_BY_REGION"),
    /**
     * Polygon clustering operation.
     */
    POLYGON_CLUSTERING("POLYGON_CLUSTERING");

    private final String name;

    private DefaultOperations(final String s) {
        name = "/loom/loom/" + s;
    }

    /**
     * Compares the name to the provided name.
     *
     * @param otherName name to compare with
     * @return true if names are equal
     */
    public boolean equalsName(final String otherName) {
        return otherName == null ? false : name.equals(otherName);
    }

    @Override
    public String toString() {
        return name;
    }

}
