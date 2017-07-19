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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.model.Fibre;

public class HourGlass extends LoomFunction {

    // private static final Log LOG = LogFactory.getLog(HourGlass.class);

    private static final int INITIAL_FIBRES = 45;

    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> inputs, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        Integer maxFibres = (Integer) params.get(QueryOperation.MAX_FIBRES);

        if (!validateNotNullAttribute(maxFibres, errors)) {
            errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                    "missing attribute max fibres was replaced with default value.");
            maxFibres = INITIAL_FIBRES;
        }

        return hourGlassBraid(inputs, maxFibres);

    }

    @Override
    public boolean isCluster() {
        return false;
    }

    private Map<Object, List<Fibre>> hourGlassBraid(final List<Fibre> input, final Integer maxFibres) {

        if (input.size() > maxFibres && maxFibres > 1) {

            LoomQueryUtils.ThreadDimension threadDimension = LoomQueryUtils.calcThreadDimension(input, maxFibres);

            // If nesting is above threshold, flip to hour-glass shape
            int numFibres = threadDimension.getNumLevels() > 1 ? maxFibres : threadDimension.getNumFibres(); // Flip

            int elemsPerFibre = (int) Math.ceil((float) input.size() / numFibres); // Even split

            List<Integer> separators = LoomQueryUtils.calcSeparators(input, numFibres, elemsPerFibre);
            return LoomQueryUtils.convertToBuckets(input, separators);
        } else {
            Map<Object, List<Fibre>> braided = new ConcurrentHashMap<Object, List<Fibre>>(1); // Buckets
            braided.put(0, input);
            return braided;
        }
    }
}
