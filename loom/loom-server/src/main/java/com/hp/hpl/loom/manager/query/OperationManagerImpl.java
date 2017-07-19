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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.manager.query.executor.QueryExecutorImpl;
import com.hp.hpl.loom.manager.query.filter.FilterParser;
import com.hp.hpl.loom.manager.query.filter.element.Element;
import com.hp.hpl.loom.manager.query.filter.parser.ExpressionLexer;
import com.hp.hpl.loom.manager.query.filter.parser.Parser;
import com.hp.hpl.loom.manager.query.utils.FunctionSpec;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.manager.query.utils.functions.Bucketizer;
import com.hp.hpl.loom.manager.query.utils.functions.Distributor;
import com.hp.hpl.loom.manager.query.utils.functions.FilterByRegion;
import com.hp.hpl.loom.manager.query.utils.functions.GridClustering;
import com.hp.hpl.loom.manager.query.utils.functions.HourGlass;
import com.hp.hpl.loom.manager.query.utils.functions.Kmeans;
import com.hp.hpl.loom.manager.query.utils.functions.PolygonClustering;
import com.hp.hpl.loom.manager.query.utils.functions.Pyramid;
import com.hp.hpl.loom.manager.stitcher.ItemEquivalence;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Parameter;
import com.hp.hpl.loom.model.ParameterEnum;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;
import com.hp.hpl.loom.relationships.GraphProcessor;
import com.hp.hpl.loom.relationships.RelationsReporter;
import com.hp.hpl.loom.relationships.RelationshipsModel;


@Component
public class OperationManagerImpl implements OperationManager {

    private static final double PERCENT_100 = 100.;
    private static final double HALF = 0.5;
    private static final int SORT_PARAMS_SIZE = 3;
    private static final int BUCKET_SIZE = 10;
    private static final Log LOG = LogFactory.getLog(OperationManagerImpl.class);
    public static final UUID LOOM_UUID = UUID.randomUUID();

    private Map<String, QuadFunctionMeta> allOperations = new HashMap<>();

    public OperationManagerImpl() {

        setupIdentifyOp();
        setupGroupOp();
        setupBraidOp();
        setupBucketizeOp();
        setupKmeansOp();
        setupPyramidOp();
        setupDistributeOp();
        setupSortOp();
        setupFilterOp();
        setupFilterRelatedOp();
        setupFirstNOp();
        setupFilterByRegionOp();
        setupGridClusterOp();
        setupPolygonClusterOp();
        setupPercentilesOp();
        setupSummaryOp();
    }

    private void setupSummaryOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.SUMMARY + " as one of the default operations in Loom");
        }

        QueryOperation pack = new QueryOperation((inputs, params, errors, itemType) -> {
            Map<Object, List<Fibre>> braided = new HashMap<Object, List<Fibre>>(1); // Buckets

            List<Fibre> in = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(in, errors) || !validateNotEmptyList(in, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            braided.put(0, in);
            return new PipeLink<Fibre>(braided);
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> true));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Summary", pack, true, false);
        quadFunctionMeta.setIcon("fa-sitemap");
        registerOperation(DefaultOperations.SUMMARY.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupPercentilesOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.PERCENTILES + " as one of the default operations in Loom");
        }
        QueryOperation percent = new QueryOperation((inputs, params, errors, itemType) -> {

            List<Fibre> in = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(in, errors) || !validateNotEmptyList(in, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }

            final int n = in.size();


            int bucketNumber = BUCKET_SIZE;
            if (params.get(QueryOperation.BUCKETS) != null) {
                bucketNumber = Integer.parseInt(params.get(QueryOperation.BUCKETS).toString());
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Replaced default number of buckets (10) with: " + bucketNumber);
                }
            }

            Map<Object, List<Fibre>> percentiles = new HashMap<>(bucketNumber);
            // assuming an even distribution of data across deciles
            // (not normal) would give
            // n/10 elements per bucket(decile)
            // in a normal distribution, extreme deciles will have
            // fewer datapoints (e.g.
            // percentile 10 and 90), while
            // central deciles (40-60) would contain more. 7 is a
            // tradeoff initialisation
            // value for the ArrayLists representing
            // each decile.
            final int decileOccupancy = 7;
            for (int i = 0; i < bucketNumber; i++) {
                percentiles.put(i, new ArrayList<>(n / decileOccupancy));
            }

            Map<String, Object> sortParams = new HashMap<>(SORT_PARAMS_SIZE);
            String property = params.get(QueryOperation.PROPERTY).toString();
            sortParams.put(QueryOperation.PROPERTY, property);
            if (!validateNotNullAttribute(property, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <property> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            sortParams.put(QueryOperation.ORDER, QueryOperation.ASC_ORDER);

            @SuppressWarnings("checkstyle:linelength")
            QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> sortOp =
                    getFunction(DefaultOperations.SORT_BY.toString());
            PipeLink<Fibre> sorted = sortOp.apply(inputs, sortParams, errors, null);
            List<Fibre> sortedList = LoomQueryUtils.getFirstInput(sorted, errors);
            List<Double> percentileList = IntStream.range(0, sortedList.size())
                    .mapToObj(i -> (PERCENT_100 * ((i + 1) - HALF) / n)).collect(Collectors.toList());


            double perc = 0.;
            Fibre val = null;
            for (int i = 0; i < n; i++) {
                perc = percentileList.get(i);
                int indx = (int) (Math.floor(perc) / bucketNumber);

                if (indx >= bucketNumber) {
                    indx = bucketNumber - 1;
                }
                val = sortedList.get(i);

                percentiles.get(indx).add(val);
            }

            return new PipeLink<Fibre>(percentiles);
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Percentiles", percent, true, false);
        quadFunctionMeta.setIcon("fa-sitemap");
        quadFunctionMeta.setDisplayParameters(new String[] {"property"});

        // build the parameters
        Parameter bucketsParam = new Parameter(QueryOperation.BUCKETS, ParameterEnum.INT);
        Parameter propertyParam = new Parameter(QueryOperation.PROPERTY, ParameterEnum.ATTRIBUTE_LIST);
        Map<String, Set<OrderedString>> attributes = new HashMap<>();
        propertyParam.setAttributes(attributes);

        Set<Parameter> params = new HashSet<>();
        params.add(propertyParam);
        params.add(bucketsParam);

        quadFunctionMeta.setParams(params);


        registerOperation(DefaultOperations.PERCENTILES.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupPolygonClusterOp() {
        QueryOperation polygonClustering = new QueryOperation((inputs, params, errors, context) -> {
            FunctionSpec spec = new FunctionSpec(params, new PolygonClustering());
            return new PipeLink<Fibre>(
                    LoomQueryUtils.braid(LoomQueryUtils.getFirstInput(inputs, errors), spec, errors, null));
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> true));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Polygon clustering", polygonClustering, false, false);
        quadFunctionMeta.setIcon("fa-sitemap");

        Parameter listOfPologonParam = new Parameter(QueryOperation.POLYGONS, ParameterEnum.NUMBER_ARRAY);
        Parameter defaultPologonParam = new Parameter(QueryOperation.DEFAULT_POLYGON, ParameterEnum.FLOAT);
        Parameter attributesParam = new Parameter(QueryOperation.ATTRIBUTES, ParameterEnum.STRING_ARRAY);
        Set<Parameter> params = new HashSet<>();
        params.add(listOfPologonParam);
        params.add(defaultPologonParam);
        params.add(attributesParam);
        quadFunctionMeta.setParams(params);


        registerOperation(DefaultOperations.POLYGON_CLUSTERING.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupGridClusterOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.GRID_CLUSTERING + " as one of the default operations in Loom");
        }
        QueryOperation gridclustering = new QueryOperation((inputs, params, errors, context) -> {
            FunctionSpec spec = new FunctionSpec(params, new GridClustering());
            return new PipeLink<Fibre>(
                    LoomQueryUtils.braid(LoomQueryUtils.getFirstInput(inputs, errors), spec, errors, null));
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> pipeLink.get("DontAggregate") == null));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Grid clustering", gridclustering, false, false);
        quadFunctionMeta.setIcon("fa-sitemap");

        // build the parameters
        Parameter maxFibresParam = new Parameter(QueryOperation.MAX_FIBRES, ParameterEnum.INT);
        Parameter translationsParam = new Parameter(QueryOperation.TRANSLATIONS, ParameterEnum.NUMBER_ARRAY);
        Parameter deltasParam = new Parameter(QueryOperation.DELTAS, ParameterEnum.NUMBER_ARRAY);
        Parameter attributesParam = new Parameter(QueryOperation.ATTRIBUTES, ParameterEnum.STRING_ARRAY);
        Set<Parameter> params = new HashSet<>();
        params.add(maxFibresParam);
        params.add(translationsParam);
        params.add(deltasParam);
        params.add(attributesParam);
        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.GRID_CLUSTERING.toString(), quadFunctionMeta, LOOM_UUID);

    }

    private void setupFilterByRegionOp() {
        QueryOperation filterByRegion = new QueryOperation((inputs, params, errors, context) -> {
            FunctionSpec spec = new FunctionSpec(params, new FilterByRegion());
            return new PipeLink<Fibre>(
                    LoomQueryUtils.braid(LoomQueryUtils.getFirstInput(inputs, errors), spec, errors, null));
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Filter by region", filterByRegion, false, true);

        quadFunctionMeta.setIcon("fa-filter");


        // build the parameters
        Parameter complementFilterParam = new Parameter(QueryOperation.COMPLEMENT, ParameterEnum.STRING);
        Parameter minimumsParam = new Parameter(QueryOperation.MINIMUMS, ParameterEnum.NUMBER_ARRAY);
        Parameter maximumsParam = new Parameter(QueryOperation.MAXIMUMS, ParameterEnum.NUMBER_ARRAY);
        Parameter attributesParam = new Parameter(QueryOperation.ATTRIBUTES, ParameterEnum.STRING_ARRAY);
        Set<Parameter> params = new HashSet<>();
        params.add(complementFilterParam);
        params.add(minimumsParam);
        params.add(maximumsParam);
        params.add(attributesParam);
        quadFunctionMeta.setParams(params);


        registerOperation(DefaultOperations.FILTER_BY_REGION.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupFirstNOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.GET_FIRST_N + " as one of the default operations in Loom");
        }
        QueryOperation first = new QueryOperation((inputs, params, errors, itemType) -> {
            Integer n = Integer.parseInt(params.get(QueryOperation.N).toString());
            if (!validateNotNullAttribute(n, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <N> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            return new PipeLink<Fibre>(n,
                    LoomQueryUtils.getFirstInput(inputs, errors).stream().limit(n).collect(Collectors.toList()));
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Get first N", first, true, true);
        quadFunctionMeta.setIcon("fa-star");
        quadFunctionMeta.setDisplayParameters(new String[] {"N"});

        // build the parameters
        Parameter nParam = new Parameter(QueryOperation.N, ParameterEnum.INT);

        Set<Parameter> params = new HashSet<>();
        params.add(nParam);

        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.GET_FIRST_N.toString(), quadFunctionMeta, LOOM_UUID);

    }

    private void setupFilterRelatedOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.FILTER_RELATED + " as one of the default operations in Loom");
        }
        QueryOperation filterRel = new QueryOperation((inputs, params, errors, context) -> {
            String id = (String) params.get(QueryOperation.ID);
            if (!validateNotNullAttribute(id, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <id> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            // Find the list of items that are related to the
            // specified Item
            Collection<Item> items = new ArrayList<>(1);

            if (id.startsWith(QueryExecutorImpl.DA)) {
                Collection<Item> logicalItems = context.getItemsWithLogicalId(id, errors);
                if (logicalItems != null) {
                    items.addAll(logicalItems);
                }
            } else {
                Item item = context.getItemWithLogicalId(id, errors);
                if (item != null) {
                    items.add(item);
                }
            }
            RelationshipsModel model = context.getRelationshipsModel(errors);
            GraphProcessor graphProcessor = context.getGraphProcessor();
            Set<String> relatedIds = new HashSet<>();
            Map<String, Set<String>> relationsMap = new HashMap<>();
            Map<String, List<String>> relationPaths = new HashMap<>();
            String matchingId = context.getType().getId();
            try {
                boolean followEquivalence = false;
                ItemEquivalence itemEquivalence = context.getItemEquivalence(errors);
                RelationsReporter relationsReporter = (final Item i, String relType, final Set<String> r,
                        Map<String, Set<String>> r2, Map<String, List<String>> rp, List<String> path) -> {
                    if (i.getTypeId().equals(matchingId)) {
                        r.add(i.getLogicalId()); // Report
                                                 // relations
                        Set<String> rel = r2.get(relType);
                        if (rel == null) {
                            rel = new HashSet<>();
                            r2.put(relType, rel);
                        }
                        rel.add(i.getLogicalId());
                        // to item

                        rp.put(i.getLogicalId(), path);
                    }
                    return false;
                };

                RelationsReporter equivalenceReporter = relationsReporter;
                Set<String> equivalenceRelations = new HashSet<>();

                for (Item i : items) {
                    graphProcessor.doProcessGraphForItem(i, relatedIds, relationsMap, relationPaths, relationsReporter,
                            model, null, new HashSet<String>(), new HashSet<String>(), followEquivalence,
                            itemEquivalence, equivalenceReporter, equivalenceRelations);
                }

            } catch (RelationPropertyNotFound e) {
                errors.put(OperationErrorCode.NotReadableField, e.toString());
            }
            List<Fibre> in = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(in, errors) || !validateNotEmptyList(in, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            List<Fibre> filtered = in.stream().filter(le -> QueryUtils.matchToSet(le.getLogicalId(), relatedIds))
                    .collect(Collectors.toList());
            return new PipeLink<Fibre>(DefaultOperations.FILTER_RELATED.toString(), filtered);
        }, true);


        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Filter related", filterRel, true, true);
        quadFunctionMeta.setIcon("fa-filter");

        // build the parameters
        Parameter filterParam = new Parameter(QueryOperation.ID, ParameterEnum.STRING);
        Set<Parameter> params = new HashSet<>();
        params.add(filterParam);
        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.FILTER_RELATED.toString(), quadFunctionMeta, LOOM_UUID);

    }

    private void setupFilterOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.FILTER_STRING + " as one of the default operations in Loom");
        }
        QueryOperation freeTxtFilter = new QueryOperation((inputs, params, errors, context) -> {
            String pattern = (String) params.get(QueryOperation.PATTERN);
            if (!validateNotNullAttribute(pattern, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <pattern> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            List<Fibre> in = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(in, errors) || !validateNotEmptyList(in, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }

            ExpressionLexer lexer = new ExpressionLexer(pattern);
            Parser parser = new FilterParser(lexer);
            Element patternElement = parser.parseExpression();

            List<Fibre> filtered = in
                    .stream().filter(le -> QueryUtils
                            .match(FibreIntrospectionUtils.introspectProperties(errors, le, context), patternElement))
                    .collect(Collectors.toList());

            return new PipeLink<Fibre>(DefaultOperations.FILTER_STRING.toString(), filtered);
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Filter string", freeTxtFilter, true, true);
        quadFunctionMeta.setIcon("fa-filter");
        quadFunctionMeta.setDisplayParameters(new String[] {"pattern"});

        // // build the parameters
        Parameter filterParam = new Parameter(QueryOperation.PATTERN, ParameterEnum.STRING);
        Set<Parameter> params = new HashSet<>();
        params.add(filterParam);
        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.FILTER_STRING.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupSortOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.SORT_BY + " as one of the default operations in Loom");
        }
        QueryOperation sort = new QueryOperation((inputs, params, errors, context) -> {
            String attribute = (String) params.get(QueryOperation.PROPERTY);

            if (!validateNotNullAttribute(attribute, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <property> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            String dir = (String) params.get(QueryOperation.ORDER);

            Comparator<Fibre> sortComparator = LoomQueryUtils.buildSortComparator(attribute, errors, context);
            List<Fibre> sorted;
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }

            if (dir == null || dir.isEmpty() || dir.equalsIgnoreCase(QueryOperation.ASC_ORDER)) {
                errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                        "missing attribute <order> was replaced with default value.");
                sorted = input.stream().sorted(sortComparator).collect(Collectors.toList());
            } else {
                sorted = input.stream().sorted(sortComparator.reversed()).collect(Collectors.toList());
            }
            return new PipeLink<Fibre>(DefaultOperations.SORT_BY.toString(), sorted);
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Sort by", sort, false, false);
        quadFunctionMeta.setIcon("fa-exchange");
        quadFunctionMeta.setDisplayParameters(new String[] {"property", "order"});

        // // build the parameters
        Parameter orderParam = new Parameter(QueryOperation.ORDER, ParameterEnum.ENUM);
        Set<String> r = new HashSet<>();
        r.add("DESC");
        r.add("ASC");
        orderParam.setRange(r);
        orderParam.setDefaultValue("ASC");

        Parameter propertyParam = new Parameter(QueryOperation.PROPERTY, ParameterEnum.ATTRIBUTE_LIST);
        Map<String, Set<OrderedString>> attributes = new HashMap<>();
        propertyParam.setAttributes(attributes);

        Set<Parameter> params = new HashSet<>();
        params.add(propertyParam);
        params.add(orderParam);

        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.SORT_BY.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupDistributeOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.DISTRIBUTE + " as one of the default operations in Loom");
        }
        QueryOperation distribute = new QueryOperation((inputs, params, errors, context) -> {
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            FunctionSpec spec = new FunctionSpec(params, new Distributor());
            return new PipeLink<Fibre>(LoomQueryUtils.braid(input, spec, errors, null));
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> true));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Distribute", distribute, false, false);
        quadFunctionMeta.setIcon("fa-sitemap");

        // build the parameters
        Parameter bucketsParam = new Parameter(QueryOperation.BUCKETS, ParameterEnum.INT);
        Parameter propertyParam = new Parameter(QueryOperation.PROPERTY, ParameterEnum.ATTRIBUTE_LIST);
        Map<String, Set<OrderedString>> attributes = new HashMap<>();
        propertyParam.setAttributes(attributes);

        Set<Parameter> params = new HashSet<>();
        params.add(propertyParam);
        params.add(bucketsParam);

        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.DISTRIBUTE.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupPyramidOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.PYRAMID + " as one of the default operations in Loom");
        }
        QueryOperation pyramid = new QueryOperation((inputs, params, errors, context) -> {
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            FunctionSpec spec = new FunctionSpec(params, new Pyramid());
            return new PipeLink<Fibre>(LoomQueryUtils.braid(input, spec, errors, null));
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> true));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Pyramid", pyramid, false, false);
        quadFunctionMeta.setIcon("fa-sitemap");

        // build the parameters
        Parameter maxFibresParam = new Parameter(QueryOperation.MAX_FIBRES, ParameterEnum.INT);
        Parameter tightPackingParam = new Parameter(QueryOperation.TIGHT_PACKING, ParameterEnum.BOOL);

        Set<Parameter> params = new HashSet<>();
        params.add(maxFibresParam);
        params.add(tightPackingParam);
        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.PYRAMID.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupKmeansOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.KMEANS + " as one of the default operations in Loom");
        }
        QueryOperation kmeans = new QueryOperation((inputs, params, errors, context) -> {
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            FunctionSpec spec = new FunctionSpec(params, new Kmeans());
            return new PipeLink<Fibre>(LoomQueryUtils.braid(input, spec, errors, null));
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> pipeLink.get("DontAggregate") == null));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Kmeans", kmeans, false, false);
        quadFunctionMeta.setIcon("TODO");

        Parameter maxFibresParam = new Parameter(QueryOperation.MAX_FIBRES, ParameterEnum.INT);
        Parameter kParam = new Parameter(QueryOperation.K, ParameterEnum.INT);
        Parameter attributesParam = new Parameter(QueryOperation.ATTRIBUTES, ParameterEnum.STRING_ARRAY);
        Set<Parameter> params = new HashSet<>();
        params.add(maxFibresParam);
        params.add(kParam);
        params.add(attributesParam);
        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.KMEANS.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupBucketizeOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.BUCKETIZE + " as one of the default operations in Loom");
        }
        QueryOperation bucketize = new QueryOperation((inputs, params, errors, context) -> {
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }
            FunctionSpec spec = new FunctionSpec(params, new Bucketizer());
            return new PipeLink<Fibre>(LoomQueryUtils.braid(input, spec, errors, null));
        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> true));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Bucketize", bucketize, false, false);
        quadFunctionMeta.setIcon("fa-sitemap");

        Parameter propertyParam = new Parameter(QueryOperation.PROPERTY, ParameterEnum.ATTRIBUTE_LIST);
        Map<String, Set<OrderedString>> attributes = new HashMap<>();
        propertyParam.setAttributes(attributes);

        Set<Parameter> params = new HashSet<>();
        params.add(propertyParam);

        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.BUCKETIZE.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupBraidOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.BRAID + " as one of the default operations in Loom");
        }
        QueryOperation braid = new QueryOperation((inputs, params, errors, context) -> {
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                return new PipeLink<Fibre>(0);
            }
            FunctionSpec spec = new FunctionSpec(params, new HourGlass());
            return new PipeLink<Fibre>(LoomQueryUtils.braid(input, spec, errors, null));
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Braid", braid, true, false);
        quadFunctionMeta.setIcon("fa-sitemap");
        quadFunctionMeta.setDisplayParameters(new String[] {"maxFibres"});

        Parameter maxFibresParam = new Parameter(QueryOperation.MAX_FIBRES, ParameterEnum.INT);
        Set<Parameter> params = new HashSet<>();
        params.add(maxFibresParam);
        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.BRAID.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupGroupOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.GROUP_BY + " as one of the default operations in Loom");
        }
        QueryOperation group = new QueryOperation((inputs, params, errors, context) -> {
            String attribute = (String) params.get(QueryOperation.PROPERTY);
            if (!validateNotNullAttribute(attribute, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <property> supplied by client.");
                return new PipeLink<Fibre>(0);
            }

            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                errors.put(OperationErrorCode.NullParam, "null <input> supplied by client.");
                return new PipeLink<Fibre>(0);
            }

            return new PipeLink<Fibre>(
                    input.stream().collect(Collectors.groupingBy((final Fibre le) -> FibreIntrospectionUtils
                            .introspectPropertyNoNull(attribute, le, errors, context))),
                    attribute, context);

        }, false, Arrays.asList((Predicate<PipeLink<Fibre>>) pipeLink -> true));

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Group by", group, false, false);
        quadFunctionMeta.setIcon("fa-cubes");
        quadFunctionMeta.setDisplayParameters(new String[] {"property"});

        Parameter propertyParam = new Parameter(QueryOperation.PROPERTY, ParameterEnum.ATTRIBUTE_LIST);
        Map<String, Set<OrderedString>> attributes = new HashMap<>();
        propertyParam.setAttributes(attributes);

        Set<Parameter> params = new HashSet<>();
        params.add(propertyParam);

        quadFunctionMeta.setParams(params);

        registerOperation(DefaultOperations.GROUP_BY.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private void setupIdentifyOp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading " + DefaultOperations.IDENTITY + " as one of the default operations in Loom");
        }
        QueryOperation identity = new QueryOperation((inputs, params, errors, context) -> {

            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);
            if (!validateNotNullAttribute(input, errors) || !validateNotEmptyList(input, errors)) {
                return new PipeLink<Fibre>(0);
            }

            return new PipeLink<Fibre>(DefaultOperations.IDENTITY.toString(), input);
        }, false);

        QuadFunctionMeta quadFunctionMeta = new QuadFunctionMeta("Identity", identity, true, false);
        quadFunctionMeta.setIcon("TODO");
        registerOperation(DefaultOperations.IDENTITY.toString(), quadFunctionMeta, LOOM_UUID);
    }

    private boolean validateNotNullAttribute(final Object attribute, final Map<OperationErrorCode, String> errors) {
        if (attribute == null) {
            errors.put(OperationErrorCode.NullParam, "property value was null");
            return false;
        }
        return true;
    }

    private boolean validateNotEmptyList(final List<Fibre> attribute, final Map<OperationErrorCode, String> errors) {
        if (attribute.size() == 0) {
            errors.put(OperationErrorCode.EmptyInput, "empty input for operation");
            return false;
        }
        return true;
    }

    @Override
    public void registerOperation(final String opId, final QuadFunctionMeta op, final UUID uuid) {
        if (LOOM_UUID.equals(uuid)) {
            if (!validateOperation(opId, op)) {
                return;
            }
            allOperations.putIfAbsent(opId, op);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempt to register op " + op + "with id: " + opId + " with worng UUID " + uuid);
            }
        }
    }

    @Override
    public void registerOperation4Provider(final String opId, final QuadFunctionMeta op, final Provider prov) {
        if (prov == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempt to register op: " + opId + " on NULL provider");
            }
            return;
        }
        if (!validateOperation(opId, op)) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempted to register operation: " + opId + " for provider: " + prov.getProviderTypeAndId());
        }
        allOperations.putIfAbsent((prov.getProviderTypeAndId() + Provider.PROV_SEPARATOR + opId), op);
    }

    @Override
    public void updateOperation(final String opId, final QuadFunctionMeta op) {
        if (!validateOperation(opId, op)) {
            return;
        }

        List<String> defaultOperations = getDefault();
        if (!defaultOperations.contains(opId)) {
            allOperations.put(opId, op);
        }
    }

    private boolean validateOperation(final String opId, final QuadFunctionMeta opMeta) {
        if (StringUtils.isEmpty(opId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempt to update operation with empty id " + opMeta);
            }
            return false;
        }
        if (opMeta == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempt ro register null operation with ID " + opId);
            }
            return false;
        }
        return true;
    }

    @Override
    public void deleteOperation(final String opId, final UUID uuid) {
        if (LOOM_UUID.equals(uuid)) {
            if (StringUtils.isEmpty(opId)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attempt to update operation with empty id " + opId);
                }
                return;
            }

            List<String> defaultOperations = getDefault();
            if (!defaultOperations.contains(opId)) {
                LOG.info("Removing " + opId);
                allOperations.remove(opId);
            } else {
                LOG.info(opId + " not removed");
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempt to delete operation with wrong uuid " + uuid);
            }
            return;
        }
    }

    @Override
    public void deleteOperationsForProvider(final Provider prov) {
        if (prov == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempted to delete all operations for a NULL provider");
            }
            return;
        }
        deleteOperations(prov.getProviderTypeAndId());
    }

    @Override
    public void deleteOperations(final String providerTypeAndId) {
        if (StringUtils.isEmpty(providerTypeAndId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempted to delete all operations for a provider with invalid type/id (" + providerTypeAndId
                        + ")");
            }
            return;
        }

        List<String> toBeDeleted = new ArrayList<>();

        for (String op : listOperations(LOOM_UUID)) {
            if (op.contains(providerTypeAndId)) {
                toBeDeleted.add(op);
            }
        }

        for (String op : toBeDeleted) {
            deleteOperation(op, LOOM_UUID);
        }
    }

    @Override
    public List<String> getDefault() {
        List<String> defaultOps = new ArrayList<String>(DefaultOperations.values().length);
        for (DefaultOperations op : DefaultOperations.values()) {
            defaultOps.add(op.toString());
        }
        return defaultOps;
    }

    @Override
    public List<String> listOperations(final UUID uuid) {
        if (LOOM_UUID.equals(uuid)) {
            return new ArrayList<>(allOperations.keySet());
        }
        return new ArrayList<>(0);
    }

    @Override
    public List<String> listOperations(final Provider prov) {
        if (prov == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("attempt to list operations for a NULL provider");
            }
            return new ArrayList<>(0);
        } else {
            return listOperations(prov.getProviderTypeAndId());
        }
    }

    @Override
    public List<String> listOperations(final String providerTypeAndId) {
        if (StringUtils.isEmpty(providerTypeAndId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempted to delete operations for an invalid provider: " + providerTypeAndId);
            }
            return new ArrayList<>(0);
        } else {
            List<String> filteredOps = new ArrayList<>();
            for (String op : allOperations.keySet()) {
                if (op.contains(providerTypeAndId)) {
                    filteredOps.add(op);
                }
            }

            filteredOps.addAll(getDefault());

            return filteredOps;
        }
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> getFunction(
            final String opId) {

        QuadFunctionMeta op = allOperations.get(opId);

        if (op == null) {
            throw new UnsupportedOperationException();
        }
        return op.getQueryOperation().getFunction();
    }

    @Override
    public QuadFunctionMeta getOperation(final String opId) {
        return allOperations.get(opId);
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> getAllFunctions(
            final UUID uuid) {
        if (LOOM_UUID.equals(uuid)) {
            return operationNames2Map(new ArrayList<>(allOperations.keySet()));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting all functions with wrong UUID " + uuid + ". Sugestive of programming error.");
            }
            return new HashMap<>(0);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> getAllFunctions(
            final Provider prov) {
        if (prov != null) {
            return getAllFunctions(prov.getProviderTypeAndId());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting all functions with NULL Provider");
            }
            return new HashMap<>(0);
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> getAllFunctions(
            final String providerTypeAndId) {
        if (StringUtils.isEmpty(providerTypeAndId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting all functions with wrong Provider type and Id: " + providerTypeAndId);
            }
            return new HashMap<>(0);
        } else {
            List<String> filteredOps = allOperations.keySet().stream().filter(op -> op.contains(providerTypeAndId))
                    .collect(Collectors.toList());

            filteredOps.addAll(getDefault());
            return operationNames2Map(filteredOps);
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> operationNames2Map(
            final List<String> opNames) {
        @SuppressWarnings("checkstyle:linelength")
        Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> allFunctions =
                new HashMap<>(opNames.size());

        for (String key : opNames) {
            allFunctions.put(key, getFunction(key));
        }
        return allFunctions;
    }
}
