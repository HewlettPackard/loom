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
package com.hp.hpl.loom.manager.query.utils.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.model.Fibre;

public class Kmeans extends LoomFunction {

    private static final int MAX_FIBRES = 45;
    private static final int DEFAULT_K = 10;
    private static final int MAX_ITERATIONS = 10;

    private static class Observation {
        public double x;
        public double y;
        public Fibre associatedFibre;
    }

    private static class Mean {
        public double x;
        public double y;
        public Set<Observation> set;

        public Mean() {
            x = 0;
            y = 0;
            set = Collections.newSetFromMap(new ConcurrentHashMap<Observation, Boolean>());
        }
    }


    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> inputs, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        // validate params
        Integer maxFibres;
        Integer k;
        List<?> attributes;
        try {
            maxFibres = (Integer) params.get(QueryOperation.MAX_FIBRES);
            k = (Integer) params.get(QueryOperation.K);
            attributes = (List<?>) params.get(QueryOperation.ATTRIBUTES);
        } catch (ClassCastException e) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, e.getMessage());
            return new HashMap<Object, List<Fibre>>(0);
        }

        if (!this.validateNotNullAttribute(maxFibres, errors)) {
            errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                    "missing attribute 'maxFibres' was replaced with default value.");
            maxFibres = MAX_FIBRES;
        }

        if (!this.validateNotNullAttribute(k, errors)) {
            errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                    "missing attribute 'k' was replaced with default value.");
            k = DEFAULT_K;
        }

        if (attributes == null || attributes.isEmpty()) {
            return new HashMap<Object, List<Fibre>>(0);
        }

        return this.kMeans(inputs, maxFibres, k, (List<String>) attributes, errors, context);
    }

    private List<Observation> initializeObservations(final List<Fibre> input, final List<String> attributes,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        List<Observation> observations = new ArrayList<Kmeans.Observation>();

        for (Fibre fibre : input) {
            Observation o = new Observation();
            List<Double> values = LoomQueryUtils.convertAttributesToNumbers(attributes, fibre, errors, context);
            o.associatedFibre = fibre;
            o.x = values.get(0); // Perform a mercator projection
            o.y = values.get(1); // here ? -> how to differentiate long from latitude ?
            observations.add(o);
        }

        return observations;
    }

    private List<Mean> pickKmeans(final List<Observation> observations, final Integer k) {

        HashMap<Integer, Mean> res = new HashMap<Integer, Kmeans.Mean>(k);

        for (int i = 0; i < k; ++i) {
            res.put(i, new Mean());
        }

        observations.sort((a, b) -> Double.compare(a.x, b.x));

        double minValue = observations.get(0).x;
        observations.get(observations.size() - 1);
        observations.get(0);

        for (Observation o : observations) {

            int i = (int) Math.min(Math.floor(o.x - minValue), k - 1);
            Mean mean = res.get(i);
            mean.x += o.x;
            mean.y += o.y;
            mean.set.add(o);
        }

        res.values().stream().forEach((m) -> {
            m.x /= m.set.size();
            m.y /= m.set.size();
        });

        return res.values().stream().collect(Collectors.toList());
    }

    private double distance(final Mean mean, final Observation o) {
        return Math.sqrt((mean.x - o.x) * (mean.x - o.x) + (mean.y - o.y) * (mean.y - o.y));
    }

    private Map<Object, List<Fibre>> kMeans(final List<Fibre> input, final Integer maxFibres, final Integer k,
            final List<String> attributes, final Map<OperationErrorCode, String> errors,
            final OperationContext context) {

        if (input.size() <= maxFibres) {
            return Collections.singletonMap("DontAggregate", input);
        }

        // Initialization step:
        List<Observation> observations = initializeObservations(input, attributes, errors, context);
        List<Mean> means = pickKmeans(observations, k);

        int it = 0;
        do {
            // Clear step
            for (Mean mean : means) {
                mean.set.clear();
            }

            // Assignment step:
            observations.stream().parallel().forEach((o) -> {
                Mean best = means.get(0);
                double dist = Double.MAX_VALUE;
                for (Mean mean : means) {
                    if (distance(mean, o) <= dist) {
                        best = mean;
                        dist = distance(mean, o);
                    }
                }
                best.set.add(o);
            });

            // Update step:
            for (Mean mean : means) {
                double size = mean.set.size();
                mean.x = mean.set.stream().mapToDouble((o) -> o.x).sum() / size;
                mean.y = mean.set.stream().mapToDouble((o) -> o.y).sum() / size;
            }

            it++;
        } while (it < MAX_ITERATIONS);

        // Remove possible empty means
        means.removeIf((m) -> m.set.isEmpty());

        // Kmeans has failed -> default to braid
        if (means.size() == 1) {

            Map<Object, List<Fibre>> output = new HashMap<Object, List<Fibre>>(k);
            for (Entry<Object, List<Fibre>> e : LoomQueryUtils.braid(input, k).entrySet()) {
                output.put("[" + e.getKey() + "]", e.getValue());
            }
            return output;
        } else {
            // Kmeans has succeeded ! Return the result
            return means.stream().collect(Collectors.toMap((m) -> String.format("%.1f, %.1f", m.x, m.y), (m) -> {
                return m.set.stream().map((o) -> o.associatedFibre).collect(Collectors.toList());
            }));
        }

    }


    @Override
    public boolean isCluster() {
        return true;
    }

}
