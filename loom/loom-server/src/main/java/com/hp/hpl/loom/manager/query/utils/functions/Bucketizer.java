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

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;

public class Bucketizer extends LoomFunction {

    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> input, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        String attribute = (String) params.get(QueryOperation.PROPERTY);


        if (!validateNotNullAttribute(attribute, errors)) {
            errors.put(OperationErrorCode.NullParam, "null <attribute> supplied by client.");
            return new HashMap<>(0);
        }

        return bucketize(input, new LinkedHashMap<Object, List<Fibre>>(), attribute, errors, context);
    }


    private Map<Object, List<Fibre>> bucketize(final List<Fibre> input, final Map<Object, List<Fibre>> buckets,
            final String property, final Map<OperationErrorCode, String> errors, final OperationContext context) {

        for (Fibre le : input) {
            String keyString = FibreIntrospectionUtils.introspectProperty(property, le, errors, context).toString();
            if (keyString == null) {
                return new HashMap<>(0);
            }

            List<String> keys = convertToList(keyString);

            for (String key : keys) {
                if (!buckets.containsKey(key)) {
                    buckets.put(key, new ArrayList<Fibre>());
                }
                buckets.get(key).add(le);
            }
        }
        return buckets;
    }

    @Override
    public boolean isCluster() {
        return false;
    }

    private List<String> convertToList(final String keyString) {

        List<String> keys = new ArrayList<>(0);
        if (!keyString.equals("[]")) {
            // remove [] and split by commas
            String removeInitChar = keyString.substring(1, keyString.length() - 1);
            String[] keyArray = removeInitChar.split(",");
            String key;
            for (int i = 0; i < keyArray.length; i++) {
                key = keyArray[i].trim();
                keys.add(key);
            }
        }
        return keys;
    }



}
