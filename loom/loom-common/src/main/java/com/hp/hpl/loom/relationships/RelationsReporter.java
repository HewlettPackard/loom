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
package com.hp.hpl.loom.relationships;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.loom.model.Item;

/**
 * Relations report interface.
 */
@FunctionalInterface
public interface RelationsReporter {
    /**
     * Process an item in the relationship graph.
     *
     * @param item The item to process.
     * @param relType the relationship type
     * @param relations Total set of logical IDs to be reported by graph traversal. This method can
     *        add to this set.
     * @param relationsMap The map of relationships keyed relationship name.
     * @param relationPaths The map of relation paths.
     * @param path The list of path we have visited so far.
     * @return true if graph traversal should terminate early, false otherwise.
     */
    boolean report(final Item item, final String relType, final Set<String> relations,
            final Map<String, Set<String>> relationsMap, final Map<String, List<String>> relationPaths,
            List<String> path);
}
