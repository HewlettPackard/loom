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
package com.hp.hpl.loom.adapter.load.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class Node {
    private static Random rand = new Random(0);
    private String itemType;
    private String id;

    private Map<String, Set<Node>> relations = new HashMap<>();

    private Set<String> relationNames = new HashSet<>();

    public Node(String itemType, long id) {
        this.id = itemType + "_" + id;
        this.itemType = itemType;
    }

    public int getEdgeCount() {
        int edges = 0;
        for (String key : relations.keySet()) {
            edges += relations.get(key).size();
        }
        return edges;
    }

    /**
     * @return the itemType
     */
    public String getItemType() {
        return itemType;
    }

    public boolean addRelation(boolean unique, String relationName, Node item) {
        if (unique && item.relationNames.contains(relationName)) {
            return false;
        } else {
            Set<Node> items = relations.get(relationName);
            if (items == null) {
                items = new HashSet<>();
                relations.put(relationName, items);
            }
            item.relationNames.add(relationName);
            return items.add(item);
        }
    }

    public Set<Node> getRelations(String relationName) {
        Set<Node> items = relations.get(relationName);
        if (items == null) {
            items = new HashSet<>();
        }
        return items;
    }

    public Map<String, Set<Node>> getRelationsMap() {
        return relations;
    }

    public String toString() {
        return id;
    }

    public String getId() {
        return id;
    }

}
