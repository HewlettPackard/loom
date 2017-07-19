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

import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.model.Item;


/**
 * The interface for the GraphProcessor.
 */
public interface GraphProcessor {

    /**
     * Traverse the relationship graph for the specific item, processing each visited item with the
     * specified reporter.
     *
     * @param item The starting item in the graph traversal.
     * @param relations Set of strings that the reporter can modify.
     * @param relationsMap The map of relationships keyed relationship name
     * @param relationPaths The map of relation paths (the route taken to get each item)
     * @param reporter RelationsReporter that is invoked on every visited item in the graph
     *        traversal.
     * @param model The RelationshipsModel applicable for the graph traversal.
     * @param notRelatedToTypes Optional set of type ids, used to limit the graph traversal.
     * @param disallowedTypes Types not to follow
     * @param visitedIds Already visited ids
     * @param followEquivalence True if the graph traversal should follow equivalence relations.
     * @param itemEquivalence RelationsReporter that is invoked on every visited equivalent item in
     *        the graph traversal.
     * @param equivalenceReporter Optional set of type ids, used to limit the graph traversal.
     * @param equivalenceRelations Set of strings that the equivalence reporter can modify.
     * @return true if the graph traversal was terminated early by the reporter.
     * @throws RelationPropertyNotFound thrown if the relation property is not found
     */
    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:parameternumber"})
    boolean doProcessGraphForItem(final Item item, final Set<String> relations,
            final Map<String, Set<String>> relationsMap, final Map<String, List<String>> relationPaths,
            final RelationsReporter reporter, final RelationshipsModel model, final Set<String> notRelatedToTypes,
            final Set<String> disallowedTypes, final Set<String> visitedIds,
            // Equivalence parameters
            final boolean followEquivalence, final ItemEquivalence itemEquivalence, // Determine
                                                                                    // equivalence
                                                                                    // relationships
            final RelationsReporter equivalenceReporter, // Report equivalence relationships an
                                                         // equivalence jump
            final Set<String> equivalenceRelations) throws RelationPropertyNotFound;

}
