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

public class Pyramid extends LoomFunction {

    private static final int DEFAULT_MAX_FIBRES = 45;


    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> inputs, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        Integer maxFibres = (Integer) params.get(QueryOperation.MAX_FIBRES);

        if (!validateNotNullAttribute(maxFibres, errors)) {
            errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                    "missing attribute max fibres was replaced with default value.");
            maxFibres = DEFAULT_MAX_FIBRES;
        }

        Boolean tightPacking = null;
        if (params.containsKey(QueryOperation.TIGHT_PACKING)) {
            tightPacking = Boolean.valueOf(params.get(QueryOperation.TIGHT_PACKING).toString());
        }
        if (tightPacking == null) {
            errors.put(OperationErrorCode.ReplacedMissingParameterWithDefault,
                    "missing attribute tightPacking was replaced with default value.");
            tightPacking = true;
        }
        return maxAtBottomOfPyramidBraid(inputs, maxFibres, tightPacking);

    }

    private Map<Object, List<Fibre>> maxAtBottomOfPyramidBraid(final List<Fibre> input, final Integer maxFibres,
            final boolean tightPacking) {
        if (input.size() > maxFibres) {
            LoomQueryUtils.ThreadDimension threadDimension = LoomQueryUtils.calcThreadDimension(input, maxFibres);

            int numFibres = threadDimension.getNumFibres();
            int elemsPerFibre = tightPacking ? (int) Math.pow(maxFibres, threadDimension.getNumLevels())
                    : (int) Math.ceil((float) input.size() / numFibres);

            List<Integer> separators = LoomQueryUtils.calcSeparators(input, numFibres, elemsPerFibre);
            return LoomQueryUtils.convertToBuckets(input, separators);
        } else {
            Map<Object, List<Fibre>> braided = new ConcurrentHashMap<Object, List<Fibre>>(1); // Buckets
            braided.put(0, input);
            return braided;
        }
    }


    @Override
    public boolean isCluster() {
        return false;
    }

}
