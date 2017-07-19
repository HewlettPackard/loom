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

import java.util.Map;

import com.hp.hpl.loom.manager.query.utils.functions.LoomFunction;

/**
 * Spec for the functions.
 */
public class FunctionSpec {

    /**
     * Params to use for function. includes a maxFibres entries if needed.
     */
    private final Map<String, Object> params;
    /**
     * The loom function to run.
     */
    private final LoomFunction loomFunction;

    /**
     * @param params the params to run with the function
     * @param loomFunction the loom function
     */
    public FunctionSpec(final Map<String, Object> params, final LoomFunction loomFunction) {
        super();
        this.params = params;
        this.loomFunction = loomFunction;
    }

    /**
     * Returns true if this is cluster function.
     *
     * @return true if this is cluster function.
     */
    public boolean isCluster() {
        return loomFunction.isCluster();
    }

    /**
     * @return the params
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * @return the loomFunction
     */
    public LoomFunction getLoomFunction() {
        return loomFunction;
    }

}
