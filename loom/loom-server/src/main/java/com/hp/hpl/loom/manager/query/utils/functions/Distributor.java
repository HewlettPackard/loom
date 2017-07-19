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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;


public class Distributor extends LoomFunction {

    private static final int INITIAL_FIBRES = 45;

    // private static final Log LOG = LogFactory.getLog(HourGlass.class);

    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> inputs, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        Integer maxFibres = (Integer) params.get(QueryOperation.BUCKETS);

        if (!validateNotNullAttribute(maxFibres, errors)) {
            errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                    "missing attribute numBuckets was replaced with default value.");
            maxFibres = INITIAL_FIBRES;
        }
        String property = params.get(QueryOperation.PROPERTY).toString();

        if (!validateNotNullAttribute(property, errors)) {
            errors.put(OperationErrorCode.NullParam, "missing property.");
            return new HashMap<Object, List<Fibre>>(0);
        }

        return distribute(inputs, maxFibres, property, context);

    }

    @Override
    public boolean isCluster() {
        return false;
    }

    private Map<Object, List<Fibre>> distribute(final List<Fibre> input, final Integer maxFibres, final String property,
            final OperationContext context) {
        if (input.size() > maxFibres && maxFibres > 1) {
            Map<String, Double> cacheOfPropertyValues = new LinkedHashMap<>(input.size());
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            double val;
            for (Fibre fibre : input) {
                val = Double
                        .parseDouble(FibreIntrospectionUtils.introspectProperty(property, fibre, context).toString());
                if (val < min) {
                    min = val;
                }
                if (val > max) {
                    max = val;
                }
                cacheOfPropertyValues.put(fibre.getLogicalId(), val);
            }
            int range = (int) Math.ceil(Math.abs(max - min) / maxFibres);

            Map<Object, List<Fibre>> buckets = new HashMap<>(maxFibres);
            for (int i = 0; i < maxFibres; i++) {
                buckets.put(i, new ArrayList<Fibre>(input.size() / maxFibres));
            }

            int indx;
            List<Fibre> bucket;
            for (Fibre fibre : input) {
                val = cacheOfPropertyValues.get(fibre.getLogicalId());
                indx = (int) (val / range);

                if (indx >= maxFibres) {
                    indx = maxFibres - 1;
                }
                bucket = buckets.get(indx);

                bucket.add(fibre);
                buckets.put(indx, bucket);
            }
            return buckets;
        } else {
            Map<Object, List<Fibre>> braided = new ConcurrentHashMap<Object, List<Fibre>>(1); // Buckets
            braided.put(0, input);
            return braided;
        }
    }
}
