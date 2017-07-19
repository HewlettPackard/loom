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
package com.hp.hpl.loom.tapestry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Class to model the operation.
 */
@JsonAutoDetect
public class Operation {


    private String operator;
    private Map<String, Object> parameters;

    /**
     * @param operator operation operator
     * @param parameters operation parameter.s
     */
    public Operation(final String operator, final Map<String, Object> parameters) {

        if (operator == null || parameters == null) {
            throw new IllegalArgumentException();
        }

        this.operator = operator;
        this.parameters = parameters;

    }

    /**
     * No-arg constructor for json serialisation.
     */
    public Operation() {
        parameters = new HashMap<String, Object>();
        operator = "/loom/loom/IDENTITY";
    }

    /**
     * Get the operator.
     *
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Get the parameters map.
     *
     * @return map of parameters
     */
    public Map<String, Object> getParameters() {
        return new HashMap<String, Object>(parameters);
    }

    /**
     * @param operator set the operator
     */
    public void setOperator(final String operator) {
        if (operator == null) {
            throw new IllegalArgumentException();
        }
        this.operator = operator;
    }

    /**
     * @param parameters map of parameters
     */
    public void setParameters(final Map<String, Object> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException();
        }

        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("operator-> " + operator);
        str.append("; parameters-> " + parameters);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Operation that = (Operation) o;

        return Objects.equals(operator, that.operator) && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, parameters);
    }
}
