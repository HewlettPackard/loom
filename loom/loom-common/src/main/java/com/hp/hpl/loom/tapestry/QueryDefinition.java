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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Query definition.
 */
@JsonAutoDetect
public class QueryDefinition {

    private List<String> inputs;
    private List<Operation> operationPipeline;

    private boolean includeExcludedItems = false;

    /**
     * No-arg constructor.
     */
    public QueryDefinition() {
        operationPipeline = new ArrayList<Operation>();
        inputs = new ArrayList<>(0);
    }

    /**
     * @param input operation definition input
     */
    public QueryDefinition(final String input) {
        operationPipeline = new ArrayList<Operation>();
        inputs = new ArrayList<>(0);
        inputs.add(input);
    }

    /**
     * @param inputs operations definition input list
     */
    public QueryDefinition(final List<String> inputs) {
        operationPipeline = new ArrayList<Operation>();
        this.inputs = inputs;
    }

    /**
     * @param operationPipeline list of operations
     * @param inputs list of inputs
     */
    public QueryDefinition(final List<Operation> operationPipeline, final List<String> inputs) {
        if (operationPipeline == null || operationPipeline.size() == 0 || inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.operationPipeline = operationPipeline;
        this.inputs = inputs;
    }

    /**
     * @param operands input operands
     */
    public void setInputs(final List<String> operands) {
        inputs = operands;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("; input aggregations-> " + inputs);
        if (operationPipeline.size() == 0) {
            str.append("; operations-> None ");
        } else {
            str.append("; operations-> " + operationPipeline);
        }
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
        QueryDefinition that = (QueryDefinition) o;

        if (that.includeExcludedItems != includeExcludedItems) {
            return false;
        }

        if (operationPipeline.size() == 0) {
            if (that.operationPipeline.size() == 0) {
                return Objects.equals(inputs, that.inputs);
            } else {
                return false;
            }
        } else {
            if (that.operationPipeline.size() == 0) {
                return false;
            } else {
                return Objects.equals(inputs, that.inputs) && Objects.equals(operationPipeline, that.operationPipeline);
            }
        }
    }

    @Override
    public int hashCode() {
        if (operationPipeline.size() == 0) {
            return Objects.hash(inputs, 0) + (includeExcludedItems + "").hashCode();
        } else {
            return Objects.hash(inputs, operationPipeline) + (includeExcludedItems + "").hashCode();
        }
    }

    /**
     * Get the list of inputs.
     *
     * @return the list of input.
     */
    public List<String> getInputs() {
        return new ArrayList<>(inputs);
    }

    /**
     * Get the operation pipeline list.
     *
     * @return the list of operation pipeline list
     */
    public List<Operation> getOperationPipeline() {
        if (operationPipeline.size() == 0) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(operationPipeline);
        }
    }

    /**
     * @param operationPipeline set the operation pipeline list.
     */
    public void setOperationPipeline(final List<Operation> operationPipeline) {
        this.operationPipeline = operationPipeline;
    }

    /**
     * @return the includeExcludedItems
     */
    public boolean isIncludeExcludedItems() {
        return includeExcludedItems;
    }

    /**
     * @param includeExcludedItems the includeExcludedItems to set
     */
    public void setIncludeExcludedItems(boolean includeExcludedItems) {
        this.includeExcludedItems = includeExcludedItems;
    }

}
