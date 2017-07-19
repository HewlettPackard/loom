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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.model.Fibre;

/**
 * This abstract class is the base class for all the Loom functions.
 *
 */
@SuppressWarnings("checkstyle:linelength")
public abstract class LoomFunction implements
        QuadFunction<List<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, Map<Object, List<Fibre>>> {

    /**
     * Default constructor.
     */
    public LoomFunction() {
        super();
    }

    /**
     * This method returns the firstInputs or an empty input error in the errors if no input is
     * found.
     *
     * @param pipe List to find first input from
     * @param errors Error map to store any errors found
     * @return List of fibres
     */
    public static List<Fibre> getFirstInput(final Map<Object, List<Fibre>> pipe,
            final Map<OperationErrorCode, String> errors) {
        Optional<List<Fibre>> inputList = pipe.values().stream().findFirst();
        Optional<Fibre> inputElement = inputList.get().stream().findFirst();
        List<Fibre> input = new ArrayList<>(0);
        if (!inputElement.isPresent()) {
            errors.put(OperationErrorCode.EmptyInput, "");
        } else {
            input = (List<Fibre>) inputElement.get();
        }
        return input;
    }


    protected boolean validateNotNullAttribute(final Object attribute, final Map<OperationErrorCode, String> errors) {
        if (attribute == null) {
            errors.put(OperationErrorCode.NullParam, "property value was null");
            return false;
        }
        return true;
    }

    /**
     * Getter for the cluster or not details.
     *
     * @return true if this function produces clustered results
     */
    public abstract boolean isCluster();

}
