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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;

/**
 * Store all relationships derived from annotations for an Item class.
 */
public class ConnectedRelationships {
    private static final int DEFAULT_ARRAY_LIST_SIZE = 20;
    private String className;
    private ItemTypeInfo itemTypeInfo;
    private boolean root;

    /**
     * Default test to stop the relationship traversal is being root.
     */
    @SuppressWarnings("checkstyle:design")
    public static final BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean> STOP_ON_ROOT =
            (cr, stopInfo) -> cr.isRoot();

    /**
     * Prevent an item from visiting a layer again, after leaving it.
     */
    @SuppressWarnings("checkstyle:design")
    public static final BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean> VISIT_LAYER_ONLY_ONCE =
            (cr, stopInfo) -> stopInfo.getLayersAlreadyVisitedOnPathTraversal().values().stream()
                    .allMatch(hasLayerBeenVisited -> hasLayerBeenVisited);

    private List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopCriteria =
            new ArrayList<>();

    private LinkedList<ConnectedToRelationship> connectedTos = new LinkedList<>();

    ConnectedRelationships(final String className, final ItemTypeInfo itemTypeInfo, final boolean argRoot,
            final List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopRules) {
        this.className = className;
        this.itemTypeInfo = itemTypeInfo;
        this.root = argRoot;
        if (stopRules != null) {
            stopCriteria = stopRules;
        } else {
            stopCriteria.add(STOP_ON_ROOT);
        }
    }

    /**
     * @return returns the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return returns the item type info
     */
    public ItemTypeInfo getItemTypeInfo() {
        return itemTypeInfo;
    }

    /**
     * @return returns if this is a root relationship. Should now use the evaluate predicate
     *         instead.
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * By default, the stop condition is isRoot. Is some scenarios, more conditions may be required
     * to evaluate stopping. The user can implement the method by overriding Fibre class method on
     * its adaptor's item. Should one of the method returns true, a stop condition is found and the
     * other ones are not evaluated.
     *
     * @param information All possible information that can be taken into account when evaluating
     *        stop conditions.
     * @return true if one of the stop conditions is met.
     */
    public boolean evalutateStopCondition(ConditionalStopInformation information) {

        boolean hasToStop = true;

        /*
         * If a predicate returns true, something is forcing stop condition.
         */
        for (BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean> function : stopCriteria) {
            hasToStop = function.apply(this, information);

            if (hasToStop) {
                // There's no need to evaluate the other conditions.
                break;
            }
        }

        return hasToStop;
    }

    void addConnectedTo(final ConnectedToRelationship connectedTo) {
        if (connectedTo.getToRelationships().isRoot()) {
            connectedTos.addFirst(connectedTo); // Traverse root items first
        } else {
            connectedTos.addLast(connectedTo);
        }
    }

    /**
     * @return returns the list of connectedToRelationships
     */
    public List<ConnectedToRelationship> getConnectedTos() {
        return connectedTos;
    }
}
