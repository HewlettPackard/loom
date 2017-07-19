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


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.FibreStat;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;

/**
 * Stats utils class - provides useful methods to work on Aggregation stats.
 *
 */
public final class StatUtils {
    private static final int DEFAULT_MAP_SIZE_MULTIPLER = 5;
    private static final Log LOG = LogFactory.getLog(StatUtils.class);

    private StatUtils() {}


    private static Boolean isPlottableAndVisible(final Map<String, Object> visualMap) {
        if (visualMap == null || visualMap.size() == 0) {
            return false;
        }
        Boolean visible = ((Boolean) visualMap.get("visible"));
        Boolean plottable = (Boolean) visualMap.get("plottable");
        if (visible == null || plottable == null) {
            return false;
        }
        return visible && plottable;
    }

    private static Boolean isNumeric(final Map<String, Object> numericMap) {
        if (numericMap == null || numericMap.size() == 0) {
            return false;
        }
        String numeric = ((String) numericMap.get("type"));
        if (numeric == null) {
            return false;
        }
        return numeric.equals("numeric") || numeric.equals("geo");
    }

    /**
     * @param type the itemType to get the stats for
     * @param aggregatedEntities the list of entities to gather stats for
     * @param context query context
     * @return the Aggregation of the stats.
     * @throws ItemPropertyNotFound throw if the itemType doesn't exist in the entites.
     */
    public static Aggregation getAggregateStats(final ItemType type, final List<Fibre> aggregatedEntities,
            final OperationContext context) throws ItemPropertyNotFound {
        Aggregation da = new Aggregation();
        da.setElements(aggregatedEntities);
        StatUtils.populateAggregateStats(da, type, aggregatedEntities, context);
        return da;
    }

    /**
     * @param type the itemType to get the stats map for
     * @param aggregatedEntities the list of entities to gather stats for
     * @return the Aggregation of the stats as a map
     * @param context query context
     * @throws ItemPropertyNotFound throw if the itemType doesn't exist in the entites.
     */
    public static Map<String, Number> getStatMap(final ItemType type, final List<Fibre> aggregatedEntities,
            final OperationContext context) throws ItemPropertyNotFound {
        Aggregation da = new Aggregation();
        da.setElements(aggregatedEntities);
        StatUtils.populateAggregateStats(da, type, aggregatedEntities, context);
        return da.getPlottableAggregateStats();
    }

    /**
     * @param da Aggregation to get plottable stats for
     * @param type the itemType to get the stats map for
     * @param aggregatedEntities the list of entities to gather stats for
     * @param context query context
     * @throws ItemPropertyNotFound throw if the itemType doesn't exist in the entites.
     */
    public static void populateAggregateStats(final Aggregation da, final ItemType type,
            final List<Fibre> aggregatedEntities, final OperationContext context) throws ItemPropertyNotFound {
        StopWatch watch = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("populatePlottableAggregateStats start " + da.getLogicalId());
            watch = new StopWatch();
            watch.start();
        }
        if (aggregatedEntities == null || aggregatedEntities.size() == 0) {
            return;
        }

        Map<String, Map<String, Object>> attributes = type.getAttributes();
        // get fieldNames
        List<String> plotVisNames = new ArrayList<>();
        for (String fieldName : attributes.keySet()) {
            if (StatUtils.isNumeric(attributes.get(fieldName))) {
                plotVisNames.add(fieldName);
            }
        }
        // average, minimum,
        Map<String, FibreStat> overallStats = new HashMap<>(plotVisNames.size());

        // array stats
        Map<String, List<FibreStat>> arrayStats = new HashMap<>();

        // max, std
        Optional<Fibre> itemOption = aggregatedEntities.stream().findFirst();

        if (itemOption.isPresent()) {
            boolean isItemList = itemOption.get().isItem();
            if (isItemList) {

                // for all items get prop value
                for (String name : plotVisNames) {
                    // for (Fibre entity : aggregatedEntities) {
                    // Object val = entity.introspectProperty(name);
                    List<Object> valList;
                    try {
                        valList =
                                FibreIntrospectionUtils.introspectPropertyForFibres(name, aggregatedEntities, context);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new ItemPropertyNotFound(name);
                    }
                    for (Object val : valList) {
                        if (val == null) {
                            continue;
                        }
                        // is an Numeric value
                        if (Number.class.isAssignableFrom(val.getClass())) {
                            Double n1 = ((Number) val).doubleValue();
                            FibreStat stats = overallStats.get(name);
                            if (stats == null) {
                                stats = new FibreStat();
                            }
                            stats.addValue(n1);
                            overallStats.put(name, stats);
                        } else if (val.getClass().isArray()) {
                            Number[] list = (Number[]) val;
                            int counter = 0;
                            for (Number object : list) {
                                processNumber(arrayStats, name, counter, object);
                                counter++;
                            }
                        } else if (List.class.isAssignableFrom(val.getClass())) {
                            List<Number> list = (List<Number>) val;
                            int counter = 0;
                            for (Number object : list) {
                                processNumber(arrayStats, name, counter, object);
                                counter++;
                            }
                        }
                    }
                }
                Map<String, Number> simpleStats = StatUtils.convertToSimpleMap(overallStats);
                da.setPlottableAggregateStats(simpleStats);
                Map<String, List<Number>> data = convertArrayStats(arrayStats);
                da.setAggregationArrayStats(data);
            } else {
                List<Map<String, Number>> statMaps = aggregatedEntities.stream()
                        .map(le -> ((Aggregation) le).getPlottableAggregateStats()).collect(Collectors.toList());

                List<Map<String, List<Number>>> aggStatMaps = aggregatedEntities.stream()
                        .map(le -> ((Aggregation) le).getAggregationArrayStats()).collect(Collectors.toList());

                Map<String, Number> combinedStats = StatUtils.combineMaps(statMaps, aggregatedEntities, plotVisNames);
                da.setPlottableAggregateStats(combinedStats);

                Map<String, List<Number>> combinedAggStats =
                        StatUtils.combineAggMaps(aggStatMaps, aggregatedEntities, plotVisNames);
                da.setAggregationArrayStats(combinedAggStats);
            }
        }
        if (LOG.isDebugEnabled()) {
            watch.stop();
            LOG.debug("populatePlottableAggregateStats end time=" + watch + " " + da.getLogicalId());
        }
    }


    private static void processNumber(final Map<String, List<FibreStat>> arrayStats, final String name,
            final int counter, final Number object) {
        if (Number.class.isAssignableFrom(object.getClass())) {
            Double n1 = object.doubleValue();
            List<FibreStat> allStats = arrayStats.get(name);
            if (allStats == null) {
                allStats = new ArrayList<>();
                arrayStats.put(name, allStats);
            }
            FibreStat stats = null;
            if (counter >= allStats.size()) {
                stats = new FibreStat();
                allStats.add(stats);
            }
            stats = allStats.get(counter);
            stats.addValue(n1);
        }
    }

    private static Map<String, List<Number>> convertArrayStats(final Map<String, List<FibreStat>> arrayStats) {
        // public static Map<String, Number> convertToSimpleMap(final Map<String, FibreStat>
        // overallStats) {
        Map<String, List<Number>> simpleMapOfStats = new HashMap<>();

        List<Comparable<?>> modes;

        for (String val : arrayStats.keySet()) {
            List<FibreStat> allStats = arrayStats.get(val);
            for (FibreStat stats : allStats) {
                if (!String.valueOf(stats.getMean()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.AVG.toString(), stats.getMean());
                }
                if (!String.valueOf(stats.getCount()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.COUNT.toString(), stats.getCount());
                }
                if (!String.valueOf(stats.getMax()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.MAX.toString(), stats.getMax());
                }
                if (!String.valueOf(stats.getMin()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.MIN.toString(), stats.getMin());
                }
                if (!String.valueOf(stats.getSum()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.SUM.toString(), stats.getSum());
                }
                if (!String.valueOf(stats.getGeometricMean()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.GEO_MEAN.toString(), stats.getGeometricMean());
                }
                if (!String.valueOf(stats.getSumOfSquares()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.SUM_SQ.toString(), stats.getSumOfSquares());
                }
                if (!String.valueOf(stats.getStd()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.STD.toString(), stats.getStd());
                }
                if (!String.valueOf(stats.getVariance()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.VAR.toString(), stats.getVariance());
                }
                if (!String.valueOf(stats.getSkewness()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.SKEW.toString(), stats.getSkewness());
                }
                if (!String.valueOf(stats.getKurtosis()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.KURTOSIS.toString(), stats.getKurtosis());
                }
                if (!String.valueOf(stats.getMedian()).equalsIgnoreCase("NaN")) {
                    addToList(simpleMapOfStats, val + SupportedStats.MEDIAN.toString(), stats.getMedian());
                }
                // assume there are not several modes or any is equally relevant
                if (!String.valueOf(stats.getMode()).equalsIgnoreCase("NaN")) {
                    modes = stats.getMode();
                    if (modes != null && modes.size() > 0) {
                        addToList(simpleMapOfStats, val + SupportedStats.MODE.toString(),
                                Double.valueOf(modes.get(0).toString()));
                    }
                }
            }


        }
        return simpleMapOfStats;
    }

    private static void addToList(final Map<String, List<Number>> simpleMapOfStats, final String key,
            final Number value) {
        List<Number> data = simpleMapOfStats.get(key);
        if (data == null) {
            data = new ArrayList<>();
            simpleMapOfStats.put(key, data);
        }
        data.add(value);
    }


    /**
     * Converts a map of overallStats String, fibres into a map of string, numbers.
     *
     * @param overallStats map to covert
     * @return the overall stats
     */
    private static Map<String, Number> convertToSimpleMap(final Map<String, FibreStat> overallStats) {
        Map<String, Number> simpleMapOfStats = new HashMap<>(DEFAULT_MAP_SIZE_MULTIPLER * overallStats.size());
        FibreStat stats;
        List<Comparable<?>> modes;


        for (String val : overallStats.keySet()) {
            stats = overallStats.get(val);
            if (!String.valueOf(stats.getMean()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.AVG.toString(), stats.getMean());
            }
            if (!String.valueOf(stats.getCount()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.COUNT.toString(), stats.getCount());
            }
            if (!String.valueOf(stats.getMax()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.MAX.toString(), stats.getMax());
            }
            if (!String.valueOf(stats.getMin()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.MIN.toString(), stats.getMin());
            }
            if (!String.valueOf(stats.getSum()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.SUM.toString(), stats.getSum());
            }
            if (!String.valueOf(stats.getGeometricMean()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.GEO_MEAN.toString(), stats.getGeometricMean());
            }
            if (!String.valueOf(stats.getSumOfSquares()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.SUM_SQ.toString(), stats.getSumOfSquares());
            }
            if (!String.valueOf(stats.getStd()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.STD.toString(), stats.getStd());
            }
            if (!String.valueOf(stats.getVariance()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.VAR.toString(), stats.getVariance());
            }
            if (!String.valueOf(stats.getSkewness()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.SKEW.toString(), stats.getSkewness());
            }
            if (!String.valueOf(stats.getKurtosis()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.KURTOSIS.toString(), stats.getKurtosis());
            }
            if (!String.valueOf(stats.getMedian()).equalsIgnoreCase("NaN")) {
                simpleMapOfStats.put(val + SupportedStats.MEDIAN.toString(), stats.getMedian());
            }
            // assume there are not several modes or any is equally relevant
            if (!String.valueOf(stats.getMode()).equalsIgnoreCase("NaN")) {
                modes = stats.getMode();
                if (modes != null && modes.size() > 0) {
                    simpleMapOfStats.put(val + SupportedStats.MODE.toString(), Double.valueOf(modes.get(0).toString()));
                }
            }
        }
        return simpleMapOfStats;
    }

    /**
     * Combine multiple maps to form a combined stats map.
     *
     * @param statMaps initial stats map
     * @param aggregatedEntities list of entities to merge
     * @param plotVisNames the plottable visible names
     * @return map of the combines stats
     */
    private static Map<String, Number> combineMaps(final List<Map<String, Number>> statMaps,
            final List<Fibre> aggregatedEntities, final List<String> plotVisNames) {
        Map<String, Number> simpleMapOfStats = StatUtils.prePopulateMap(plotVisNames);
        Aggregation le;
        Map<String, Number> fibreStatMap;
        int numOfItems, totalItems = 0;
        Double currVal, storedVal = 0d;
        List<String> nullEntries = new ArrayList<>();

        for (int i = 0; i < statMaps.size(); i++) {
            le = (Aggregation) aggregatedEntities.get(i);
            fibreStatMap = statMaps.get(i);
            numOfItems = (int) le.getNumberOfItems();
            totalItems += numOfItems;
            for (String name : simpleMapOfStats.keySet()) {
                if (fibreStatMap == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("empty stat map for aggregation " + aggregatedEntities);
                    }
                    continue;
                }

                if (fibreStatMap.get(name) == null || simpleMapOfStats.get(name) == null) {
                    nullEntries.add(name);
                    continue;
                }
                currVal = fibreStatMap.get(name).doubleValue();
                storedVal = simpleMapOfStats.get(name).doubleValue();
                if (name.endsWith(SupportedStats.AVG.toString())) {

                    if (i != statMaps.size() - 1) {
                        simpleMapOfStats.put(name, storedVal + currVal * numOfItems);
                    } else { // update avg
                        simpleMapOfStats.put(name, (storedVal + currVal * numOfItems) / totalItems);
                    }


                }
                if (name.endsWith(SupportedStats.SUM.toString()) || name.endsWith(SupportedStats.COUNT.toString())) {

                    simpleMapOfStats.put(name, storedVal + currVal);


                }
                if (name.endsWith(SupportedStats.MIN.toString()) && currVal < storedVal) {
                    simpleMapOfStats.put(name, currVal);
                }
                if (name.endsWith(SupportedStats.MAX.toString()) && currVal > storedVal) {
                    simpleMapOfStats.put(name, currVal);
                }
            }
        }
        for (String nullEntry : nullEntries) {
            simpleMapOfStats.remove(nullEntry);
        }

        return simpleMapOfStats;
    }

    private static Map<String, List<Number>> combineAggMaps(final List<Map<String, List<Number>>> aggStatMaps,
            final List<Fibre> aggregatedEntities, final List<String> plotVisNames) {
        Map<String, List<Number>> simpleMapOfStats = StatUtils.prePopulateAggMap(plotVisNames);
        Map<String, List<Number>> fibreStatMap;
        Double currVal, storedVal = 0d;
        List<String> nullEntries = new ArrayList<>();

        Set<String> averageListNames = new HashSet<>();


        for (int i = 0; i < aggStatMaps.size(); i++) {
            fibreStatMap = aggStatMaps.get(i);

            for (String name : simpleMapOfStats.keySet()) {
                if (fibreStatMap == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("empty stat map for aggregation " + aggregatedEntities);
                    }
                    continue;
                }
                List<Number> currentValues = fibreStatMap.get(name);
                List<Number> storedValues = simpleMapOfStats.get(name);

                if (currentValues == null || storedValues == null) {
                    nullEntries.add(name);
                    continue;
                }

                for (int x = 0; x < currentValues.size(); x++) {
                    currVal = currentValues.get(x).doubleValue();
                    if (storedValues.size() > x) {
                        storedVal = storedValues.get(x).doubleValue();
                    }
                    List<Number> vals = simpleMapOfStats.get(name);


                    if (name.endsWith(SupportedStats.AVG.toString())) {
                        // if (i != simpleMapOfStats.size() - 1) {
                        // addToList(currVal, x, vals);
                        // } else { // update avg
                        // vals.set(x, (vals.get(x).doubleValue() + currVal * numOfItems) /
                        // totalItems);
                        // }
                        averageListNames.add(name);
                        addToList(currVal, x, vals);
                    }
                    if (name.endsWith(SupportedStats.SUM.toString())
                            || name.endsWith(SupportedStats.COUNT.toString())) {
                        addToList(currVal, x, vals);
                    }
                    if (name.endsWith(SupportedStats.MIN.toString()) && currVal <= storedVal) {
                        addToList(currVal, x, vals);
                    }
                    if (name.endsWith(SupportedStats.MAX.toString()) && currVal > storedVal) {
                        addToList(currVal, x, vals);
                    }
                }
            }
        }

        for (String avgName : averageListNames) {
            String countName = avgName.substring(0, avgName.indexOf(SupportedStats.AVG.toString()))
                    + SupportedStats.COUNT.toString();
            String sumName = avgName.substring(0, avgName.indexOf(SupportedStats.AVG.toString()))
                    + SupportedStats.SUM.toString();
            List<Number> avgVals = simpleMapOfStats.get(avgName);
            List<Number> sumVals = simpleMapOfStats.get(sumName);
            List<Number> countVals = simpleMapOfStats.get(countName);
            for (int i = 0; i < sumVals.size(); i++) {
                avgVals.set(i, sumVals.get(i).doubleValue() / countVals.get(i).doubleValue());
            }
        }

        for (String nullEntry : nullEntries) {
            simpleMapOfStats.remove(nullEntry);
        }

        return simpleMapOfStats;
    }


    private static void addToList(final Double currVal, final int x, final List<Number> vals) {
        if (vals.size() <= x) {
            vals.add(currVal);
        } else {
            vals.set(x, vals.get(x).doubleValue() + currVal);
        }
    }


    private static Map<String, Number> prePopulateMap(final List<String> plotVisNames) {
        Map<String, Number> simpleMapOfStats = new HashMap<>();
        for (String val : plotVisNames) {
            simpleMapOfStats.put(val + SupportedStats.AVG.toString(), 0d);
            simpleMapOfStats.put(val + SupportedStats.COUNT.toString(), 0d);
            simpleMapOfStats.put(val + SupportedStats.MAX.toString(), Double.MIN_VALUE);
            simpleMapOfStats.put(val + SupportedStats.MIN.toString(), Double.MAX_VALUE);
            simpleMapOfStats.put(val + SupportedStats.SUM.toString(), 0d);
        }
        simpleMapOfStats.put("numberOfItems" + SupportedStats.AVG.toString(), 0d);
        simpleMapOfStats.put("numberOfItems" + SupportedStats.COUNT.toString(), 0d);
        simpleMapOfStats.put("numberOfItems" + SupportedStats.MAX.toString(), Double.MIN_VALUE);
        simpleMapOfStats.put("numberOfItems" + SupportedStats.MIN.toString(), Double.MAX_VALUE);
        simpleMapOfStats.put("numberOfItems" + SupportedStats.SUM.toString(), 0d);

        return simpleMapOfStats;
    }

    private static Map<String, List<Number>> prePopulateAggMap(final List<String> plotVisNames) {
        Map<String, List<Number>> simpleMapOfStats = new HashMap<>();
        for (String val : plotVisNames) {
            simpleMapOfStats.put(val + SupportedStats.AVG.toString(), new ArrayList<Number>());
            simpleMapOfStats.put(val + SupportedStats.COUNT.toString(), new ArrayList<Number>());
            simpleMapOfStats.put(val + SupportedStats.MAX.toString(), new ArrayList<Number>());
            simpleMapOfStats.put(val + SupportedStats.MIN.toString(), new ArrayList<Number>());
            simpleMapOfStats.put(val + SupportedStats.SUM.toString(), new ArrayList<Number>());
        }
        simpleMapOfStats.put("numberOfItems" + SupportedStats.AVG.toString(), new ArrayList<Number>());
        simpleMapOfStats.put("numberOfItems" + SupportedStats.COUNT.toString(), new ArrayList<Number>());
        simpleMapOfStats.put("numberOfItems" + SupportedStats.MAX.toString(), new ArrayList<Number>());
        simpleMapOfStats.put("numberOfItems" + SupportedStats.MIN.toString(), new ArrayList<Number>());
        simpleMapOfStats.put("numberOfItems" + SupportedStats.SUM.toString(), new ArrayList<Number>());

        return simpleMapOfStats;
    }

}
