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
package com.hp.hpl.loom.manager.query.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;

public class StatUtilsTest {

    private Aggregation buildDa() {
        // List<Long> sizes = new ArrayList<>();
        // sizes.add(10L);
        // sizes.add(20L);
        // sizes.add(30L);
        Long[] sizes = new Long[] {10L, 20L, 30L};
        Aggregation da = new Aggregation("logicalId", TestType.TYPE_LOCAL_ID, "name", "description", 10);
        TestItem testItem = new TestItem();
        testItem.setSize(10L);
        testItem.setSizes(sizes);
        TestItem testItem2 = new TestItem();
        testItem2.setSize(20L);
        testItem2.setSizes(sizes);
        TestItem testItem3 = new TestItem();
        testItem3.setSize(30L);
        testItem3.setSizes(sizes);
        testItem.setName("test1");
        testItem2.setName("test2");
        testItem3.setName("test3");
        da.add(testItem);
        da.add(testItem2);
        da.add(testItem3);
        return da;
    }

    @Test
    public void getAggregationArrayStats() {

        Aggregation da = buildDa();
        List<Item> items = da.getContainedItems();
        List<Fibre> aggregatedEntities = new ArrayList<>();
        for (Item item : items) {
            aggregatedEntities.add(item);
        }
        TestType type = new TestType();

        try {
            StatUtils.populateAggregateStats(da, type, aggregatedEntities, null);
        } catch (ItemPropertyNotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Map<String, List<Number>> aggStats2 = da.getAggregationArrayStats();
        Set<String> keys2 = aggStats2.keySet();
        for (String key : keys2) {
            // if (key.startsWith("size")) {
            System.out.println(key + " " + aggStats2.get(key));
            // }
        }

        // assert the results

        List<Number> values = new ArrayList<>();
        values.add(Double.parseDouble("30"));
        values.add(Double.parseDouble("60"));
        values.add(Double.parseDouble("90"));
        checkResults(values, aggStats2.get("sizes_sum"));

        values.clear();
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        checkResults(values, aggStats2.get("sizes_var"));

        values.clear();
        values.add(Double.parseDouble("10.000000000000002"));
        values.add(Double.parseDouble("19.999999999999996"));
        values.add(Double.parseDouble("30.000000000000004"));
        checkResults(values, aggStats2.get("sizes_geometricMean"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats2.get("sizes_median"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats2.get("sizes_avg"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats2.get("sizes_min"));

        values.clear();
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        checkResults(values, aggStats2.get("sizes_std"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats2.get("sizes_max"));

        values.clear();
        values.add(Long.parseLong("3"));
        values.add(Long.parseLong("3"));
        values.add(Long.parseLong("3"));
        checkResults(values, aggStats2.get("sizes_count"));

        values.clear();
        values.add(Double.parseDouble("300"));
        values.add(Double.parseDouble("1200"));
        values.add(Double.parseDouble("2700"));
        checkResults(values, aggStats2.get("sizes_sumOfSquares"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats2.get("sizes_mode"));
    }

    @Test
    public void getAggregationArrayStatsUnEven() {
        // List<Long> sizes = new ArrayList<>();
        // sizes.add(10L);
        // sizes.add(20L);
        // sizes.add(30L);
        Long[] sizes = new Long[] {10L, 20L, 30L};
        // List<Long> sizes2 = new ArrayList<>();
        // sizes2.add(10L);
        // sizes2.add(20L);
        // sizes2.add(30L);
        // sizes2.add(40L);
        // sizes2.add(50L);
        Long[] sizes2 = new Long[] {10L, 20L, 30L, 40L, 50L};
        Aggregation da = new Aggregation("logicalId", TestType.TYPE_LOCAL_ID, "name", "description", 10);
        TestItem testItem = new TestItem();
        testItem.setSize(10L);
        testItem.setSizes(sizes);
        TestItem testItem2 = new TestItem();
        testItem2.setSize(20L);
        testItem2.setSizes(sizes2);
        testItem.setName("test1");
        testItem2.setName("test2");
        da.add(testItem);
        da.add(testItem2);

        TestType type = new TestType();
        List<Fibre> aggregatedEntities = new ArrayList<>();
        aggregatedEntities.add(testItem);
        aggregatedEntities.add(testItem2);

        try {
            StatUtils.populateAggregateStats(da, type, aggregatedEntities, null);
        } catch (ItemPropertyNotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, List<Number>> aggStats2 = da.getAggregationArrayStats();
        Set<String> keys2 = aggStats2.keySet();
        for (String key : keys2) {
            // if (key.startsWith("size")) {
            System.out.println(key + " " + aggStats2.get(key));
            // }
        }

        // assert the results

        List<Number> values = new ArrayList<>();
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("60"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("50"));
        checkResults(values, aggStats2.get("sizes_sum"));

        values.clear();
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        checkResults(values, aggStats2.get("sizes_var"));

        values.clear();
        values.add(Double.parseDouble("10.000000000000002"));
        values.add(Double.parseDouble("19.999999999999996"));
        values.add(Double.parseDouble("30.000000000000004"));
        values.add(Double.parseDouble("40.0"));
        values.add(Double.parseDouble("49.99999999999999"));
        checkResults(values, aggStats2.get("sizes_geometricMean"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("50"));
        checkResults(values, aggStats2.get("sizes_median"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("50"));
        checkResults(values, aggStats2.get("sizes_avg"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("50"));
        checkResults(values, aggStats2.get("sizes_min"));

        values.clear();
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        values.add(Double.parseDouble("0.0"));
        checkResults(values, aggStats2.get("sizes_std"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("50"));
        checkResults(values, aggStats2.get("sizes_max"));

        values.clear();
        values.add(Long.parseLong("2"));
        values.add(Long.parseLong("2"));
        values.add(Long.parseLong("2"));
        values.add(Long.parseLong("1"));
        values.add(Long.parseLong("1"));
        checkResults(values, aggStats2.get("sizes_count"));

        values.clear();
        values.add(Double.parseDouble("200"));
        values.add(Double.parseDouble("800"));
        values.add(Double.parseDouble("1800"));
        values.add(Double.parseDouble("1600"));
        values.add(Double.parseDouble("2500"));
        checkResults(values, aggStats2.get("sizes_sumOfSquares"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        values.add(Double.parseDouble("40"));
        values.add(Double.parseDouble("50"));
        checkResults(values, aggStats2.get("sizes_mode"));
    }

    private void checkResults(final List<Number> value, final List<Number> expected) {
        for (int i = 0; i < value.size(); i++) {
            assertEquals(value.get(i), expected.get(i));
        }
    }

    @Test
    public void getArrayStatsForAggregationOfAggregations() {
        Aggregation da = buildDa();
        List<Item> items = da.getContainedItems();
        List<Fibre> aggregatedEntities = new ArrayList<>();
        for (Item item : items) {
            aggregatedEntities.add(item);
        }
        TestType type = new TestType();

        try {
            StatUtils.populateAggregateStats(da, type, aggregatedEntities, null);
        } catch (ItemPropertyNotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Aggregation da2 = buildDa();
        da2.getContainedItems();
        aggregatedEntities.clear();
        for (Item item : items) {
            aggregatedEntities.add(item);
        }

        try {
            StatUtils.populateAggregateStats(da2, type, aggregatedEntities, null);
        } catch (ItemPropertyNotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<Fibre> aggregatedEntities3 = new ArrayList<>();

        aggregatedEntities3.add(da);
        aggregatedEntities3.add(da2);
        // for (Item item : items) {
        // aggregatedEntities.add(item);
        // }
        // List<Item> items2 = da.getContainedItems();
        // for (Item item : items2) {
        // aggregatedEntities.add(item);
        // }

        Aggregation da3 = new Aggregation();
        da3.add(da);
        da3.add(da2);

        try {
            StatUtils.populateAggregateStats(da3, type, aggregatedEntities3, null);
        } catch (ItemPropertyNotFound e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("---------- DA 1 ---------");
        Map<String, List<Number>> aggStats = da.getAggregationArrayStats();
        Set<String> keys2 = aggStats.keySet();
        for (String key : keys2) {
            // if (key.startsWith("size")) {
            System.out.println(key + " " + aggStats.get(key));
            // }
        }
        System.out.println("---------- DA 2 ---------");
        aggStats = da2.getAggregationArrayStats();
        keys2 = aggStats.keySet();
        for (String key : keys2) {
            // if (key.startsWith("size")) {
            System.out.println(key + " " + aggStats.get(key));
            // }
        }
        System.out.println("---------- DA 3 ---------");
        aggStats = da3.getAggregationArrayStats();
        keys2 = aggStats.keySet();
        for (String key : keys2) {
            // if (key.startsWith("size")) {
            System.out.println(key + " " + aggStats.get(key));
            // }
        }

        // assert the results

        List<Number> values = new ArrayList<>();
        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats.get("sizes_avg"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats.get("sizes_min"));

        values.clear();
        values.add(Double.parseDouble("60"));
        values.add(Double.parseDouble("120"));
        values.add(Double.parseDouble("180"));
        checkResults(values, aggStats.get("sizes_sum"));

        values.clear();
        values.add(Double.parseDouble("10"));
        values.add(Double.parseDouble("20"));
        values.add(Double.parseDouble("30"));
        checkResults(values, aggStats.get("sizes_max"));

        values.clear();
        values.add(Double.parseDouble("6"));
        values.add(Double.parseDouble("6"));
        values.add(Double.parseDouble("6"));
        checkResults(values, aggStats.get("sizes_count"));
    }

}
