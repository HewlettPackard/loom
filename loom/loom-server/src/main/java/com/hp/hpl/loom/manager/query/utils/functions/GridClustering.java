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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.model.Fibre;

public class GridClustering extends LoomFunction {

    private static final int INITIAL_FIBRES = 30;

    public GridClustering() {}

    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> input, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {

        // validate params
        List<?> translations;
        List<?> attributes;
        List<?> deltas;
        Integer maxFibres;

        try {
            translations = (List<?>) params.get(QueryOperation.TRANSLATIONS);
            attributes = (List<?>) params.get(QueryOperation.ATTRIBUTES);
            deltas = (List<?>) params.get(QueryOperation.DELTAS);
            maxFibres = (Integer) params.get(QueryOperation.MAX_FIBRES);
        } catch (ClassCastException e) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, e.getMessage());
            return new HashMap<Object, List<Fibre>>(0);
        }

        if (attributes == null || attributes.isEmpty()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, "attributes parameters is missing or empty.");
            return new HashMap<Object, List<Fibre>>(0);
        }
        if (translations == null || translations.isEmpty()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter,
                    "translations parameters is missing or empty.");
            return new HashMap<Object, List<Fibre>>(0);
        }
        if (deltas == null || deltas.isEmpty()) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, "deltas parameters is missing or empty.");
            return new HashMap<Object, List<Fibre>>(0);
        }
        if (maxFibres == null || maxFibres == 0) {
            maxFibres = INITIAL_FIBRES;
        }

        return this.gridClustering(input, (List<String>) attributes, (List<Number>) translations, (List<Number>) deltas,
                maxFibres, errors, context);
    }

    private Map<Object, List<Fibre>> gridClustering(final List<Fibre> input, final List<String> attributes,
            final List<Number> translations, final List<Number> deltas, final Integer maxFibres,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        Map<Object, List<Fibre>> result;

        if (input.size() < maxFibres) {

            result = new HashMap<Object, List<Fibre>>();
            result.put("DontAggregate", input);

        } else {

            result = new ConcurrentHashMap<Object, List<Fibre>>();

            input.stream()
                    // .parallel()
                    .forEach(le -> {
                        List<Double> values =
                                LoomQueryUtils.convertAttributesToNumbers(attributes, le, errors, context);
                        Iterator<Double> iterValue = values.iterator();
                        Iterator<Number> iterTrans = translations.iterator();
                        Iterator<Number> iterDelta = deltas.iterator();
                        List<Integer> boxIndex = new ArrayList<Integer>(values.size());
                        while (iterValue.hasNext()) {
                            boxIndex.add((int) Math.floor((iterValue.next() + iterTrans.next().doubleValue())
                                    / iterDelta.next().doubleValue()));
                        }
                        if (result.get(boxIndex) == null) {
                            result.put(boxIndex, new LinkedList<Fibre>());
                        }
                        result.get(boxIndex).add(le);
                    });
        }

        return result;
    }

    @Override
    public boolean isCluster() {

        return true;
    }

}
