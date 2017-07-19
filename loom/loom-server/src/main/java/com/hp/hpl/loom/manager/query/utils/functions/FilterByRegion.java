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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.model.Fibre;

public class FilterByRegion extends LoomFunction {

    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> input, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        List<?> maximums;
        List<?> minimums;
        List<?> attributes;
        Object complement;

        // validate params
        try {
            maximums = (List<?>) params.get(QueryOperation.MAXIMUMS);
            minimums = (List<?>) params.get(QueryOperation.MINIMUMS);
            attributes = (List<?>) params.get(QueryOperation.ATTRIBUTES);
            complement = params.get(QueryOperation.COMPLEMENT);
        } catch (ClassCastException e) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, e.getMessage());
            return new HashMap<Object, List<Fibre>>(0);
        }

        if (maximums == null || maximums.isEmpty()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, "maximums parameter omited or empty");
            return new HashMap<Object, List<Fibre>>(0);
        }
        if (minimums == null || minimums.isEmpty()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, "minimums parameter omited or empty");
            return new HashMap<Object, List<Fibre>>(0);
        }
        if (attributes == null || attributes.isEmpty()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, "attributes parameter omited or empty");
            return new HashMap<Object, List<Fibre>>(0);
        }
        if (attributes.size() != maximums.size() && maximums.size() != attributes.size()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter,
                    "length of arrays attributes, centers and sizes does not match.");
            return new HashMap<Object, List<Fibre>>(0);
        }

        // small checks
        if (!(attributes.get(0) instanceof String)) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, "attributes must be an array of string.");
            return new HashMap<Object, List<Fibre>>(0);
        }

        // can fail if centers, sizes, attributes are not really what they're supposed to.
        //
        return this.filterByRegion(input, (List<Number>) maximums, (List<Number>) minimums, (List<String>) attributes,
                complement != null, errors, context);
    }

    private Map<Object, List<Fibre>> filterByRegion(final List<Fibre> input, final List<Number> maximums,
            final List<Number> minimums, final List<String> attributes, final boolean isComplement,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {

        Map<Object, List<Fibre>> output = new HashMap<Object, List<Fibre>>(1);

        List<Fibre> result = input.stream()
                .filter((le) -> this.isWithinBox(maximums, minimums, isComplement,
                        LoomQueryUtils.convertAttributesToNumbers(attributes, le, errors, context)))
                .collect(Collectors.toList());

        // We returns fibers and preserve the input type
        output.put(0, result);

        return output;
    }

    private boolean isWithinBox(final List<Number> maximums, final List<Number> minimums, final boolean isComplement,
            final List<Double> point) {

        boolean result = !isComplement;

        Iterator<Number> iterMaximum = maximums.iterator();
        Iterator<Number> iterMinimum = minimums.iterator();
        Iterator<Double> iterPoint = point.iterator();

        while (iterMaximum.hasNext() && iterMinimum.hasNext() && iterPoint.hasNext()) {
            double maximum = iterMaximum.next().doubleValue();
            double minimum = iterMinimum.next().doubleValue();
            double coord = iterPoint.next();
            if (maximum > minimum) {
                // min max
                // ----------|------------|----------
                // ^^^^^^^^^^^^ <- region
                if (isComplement) {
                    result |= coord > minimum && coord < maximum;
                } else {
                    result &= coord > minimum && coord < maximum;
                }
            } else {
                // max min
                // ----------|------------|----------
                // ^^^^^^^^^^ ^^^^^^^^^^ <- region
                if (isComplement) {
                    result |= coord > minimum || coord < maximum;
                } else {
                    result &= coord > minimum || coord < maximum;
                }
            }
        }

        return result;
    }

    @Override
    public boolean isCluster() {
        return false;
    }

}
