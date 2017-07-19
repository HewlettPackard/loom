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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.JDKRandomGenerator;


public class GraphBuilder {
    private long seed = 0;
    private Map<String, PercentageDistro> relDesc = new HashMap<>();

    private Map<String, List<Node>> items = new HashMap<>();
    private Map<String, Integer> itemsCount = new HashMap<>();
    private List<String> uniqueRelations = new ArrayList<>();
    private Random rand = new Random(seed);

    public GraphBuilder() {}

    public void loadConfig(String filename, String username) throws FileNotFoundException {
        ItemConfigs[] loadedConfigSettings = ItemConfigs.build(filename);
        for (ItemConfigs itemConfigs : loadedConfigSettings) {
            if (itemConfigs.getUsers().contains(username)) {
                for (ItemConfig itemConfig : itemConfigs.getConfig()) {
                    itemsCount.put(itemConfig.getName(), Integer.parseInt(itemConfig.getCount()));
                    for (Relationship relationship : itemConfig.getRelationships()) {
                        JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();
                        jdkRandomGenerator.setSeed(filename.hashCode());
                        LinkedHashMap<Integer, Integer> relationConfig = new LinkedHashMap<>();
                        for (Percent percents : relationship.getPercent()) {
                            relationConfig.put(percents.getVertex(), percents.getPercentage());
                            System.out.println(percents.getVertex() + " / " + percents.getPercentage());
                        }
                        if (relationship.isUnique()) {
                            uniqueRelations.add(relationship.getName());
                        }
                        relDesc.put(relationship.getName(), new PercentageDistro(jdkRandomGenerator, relationConfig));
                    }
                }
            }
        }
    }

    public void clear() {
        itemsCount.clear();
        items.clear();
        uniqueRelations.clear();
        relDesc.clear();
    }


    public Set<String> getItemTypeNames() {
        return itemsCount.keySet();
    }

    public Map<String, List<Node>> getItems() {
        return items;
    }


    public void build(int scale) {
        // add the minimum number of each item
        for (String type : itemsCount.keySet()) {
            int count = itemsCount.get(type) * scale;
            itemsCount.put(type, count);
        }
        long t1 = System.currentTimeMillis();
        for (String type : itemsCount.keySet()) {
            System.out.println("Creating: " + itemsCount.get(type) + " of " + type);
            int counter = itemsCount.get(type);
            for (int i = 0; i < counter; i++) {
                Node item = new Node(type, i);
                addItem(item);
            }
        }
        System.out.println("Items created in " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();
        System.out.println("SETTING UP RELATIONSHIPS");
        Set<String> relations = relDesc.keySet();
        for (String relationKey : relations) {
            String[] key = relationKey.split("-");
            String type1 = key[0];
            String type2 = key[1];
            String rel = key[2];
            int[] vertexDegrees = relDesc.get(relationKey).sample(itemsCount.get(type1));
            for (int counter = 0; counter < vertexDegrees.length; counter++) {
                Node item1 = items.get(type1).get(counter);
                for (int edge = 0; edge < vertexDegrees[counter]; edge++) {
                    List<Node> itemType2 = items.get(type2);
                    boolean added = false;
                    while (!added) {
                        int pick2 = rand.nextInt(itemType2.size());
                        Node item2 = itemType2.get(pick2);

                        added = item1.addRelation(uniqueRelations.contains(relationKey),
                                buildKey(item1.getItemType(), type2, rel), item2);
                    }
                }
            }
        }
        System.out.println("SETTING UP RELATIONSHIPS - DONE in " + (System.currentTimeMillis() - t1));
        Set<String> keys = items.keySet();
        System.out.println("items.keySet().size() --> " + items.keySet().size());
        for (String string : keys) {
            System.out.println(string);
        }
    }

    private void addItem(Node item) {
        List<Node> its = items.get(item.getItemType());
        if (its == null) {
            its = new ArrayList<>();
        }
        items.put(item.getItemType(), its);
        its.add(item);
    }

    private String buildKey(String type1, String type2, String rel) {
        return type1 + "/" + type2 + "/" + rel;
    }

    public void relStats() {
        int itemCounter = 0;
        int relationCounter = 0;
        for (String type : itemsCount.keySet()) {
            for (int i = 0; i < items.get(type).size(); i++) {
                itemCounter++;
                relationCounter += items.get(type).get(i).getEdgeCount();
            }
        }
        System.out.println("ItemCounter: " + itemCounter);
        System.out.println("RelationCounter: " + relationCounter);
    }

    public String renderDot() {
        StringBuilder sb = new StringBuilder();

        sb.append("graph test {\n");
        for (String type : itemsCount.keySet()) {
            for (int i = 0; i < items.get(type).size(); i++) {
                Node item = items.get(type).get(i);
                Map<String, Set<Node>> relations = item.getRelationsMap();
                // if (relations.isEmpty()) {
                sb.append(item.toString() + " [label=\"" + item.getItemType() + "\"]");
                sb.append("\n");
                // } else {
                for (String key : relations.keySet()) {
                    Set<Node> detailRelations = relations.get(key);
                    for (Node item2 : detailRelations) {
                        sb.append(item.toString());
                        sb.append(" -- ");
                        sb.append(item2.toString());
                        sb.append("\n");
                    }
                }
                // }
            }
        }
        sb.append("}");
        return sb.toString();
    }
    //
    // public static void main(String[] args) throws IOException {
    // GraphBuilder builder = new GraphBuilder();
    // builder.loadConfig("./config.json", "test2");
    // long t1 = System.currentTimeMillis();
    // builder.build(10000);
    // long t2 = System.currentTimeMillis();
    // System.out.println("Build time: " + (t2 - t1));
    // builder.relStats();
    // File networkDot = new File("/tmp/network.dot");
    // File outfilePs = new File("/tmp/outfile.ps");
    // if (outfilePs.exists()) {
    // outfilePs.delete();
    // }
    // if (networkDot.exists()) {
    // networkDot.delete();
    // }
    // FileOutputStream fos = new FileOutputStream(networkDot);
    // fos.write(builder.renderDot().getBytes());
    // fos.close();
    // try {
    // Runtime run = Runtime.getRuntime();
    // run.exec("dot -Tsvg " + networkDot.getAbsolutePath() + " -o " +
    // outfilePs.getAbsolutePath()).waitFor();
    // run.exec("eog " + outfilePs.getAbsolutePath()).waitFor();
    // networkDot.deleteOnExit();
    // } catch (IOException e) {
    // e.printStackTrace();
    // } catch (InterruptedException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }
}
