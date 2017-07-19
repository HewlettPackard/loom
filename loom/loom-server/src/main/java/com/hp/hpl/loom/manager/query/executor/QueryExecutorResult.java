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
package com.hp.hpl.loom.manager.query.executor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.loom.model.Fibre;

public class QueryExecutorResult {
    public final ImmutableList<Fibre> preparedOpResult;
    public final String derivedLogicalId;
    public final boolean crossThread;
    public final boolean needToCreateIntermediateDAs;

    public QueryExecutorResult(String derivedLogicalId, List<Fibre> preparedOpResult, boolean crossThread,
            boolean needToCreateIntermediateDAs) {
        this.derivedLogicalId = derivedLogicalId;
        this.preparedOpResult = ImmutableList.copyOf(preparedOpResult);
        this.crossThread = crossThread;
        this.needToCreateIntermediateDAs = needToCreateIntermediateDAs;
    }
}
