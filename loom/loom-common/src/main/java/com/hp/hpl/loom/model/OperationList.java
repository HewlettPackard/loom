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
package com.hp.hpl.loom.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Wrapper/Container for the list of operations and their order.
 */
@JsonAutoDetect
public class OperationList {

    private List<String> operationOrder;
    private List<Operation> operations;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public OperationList() {
        operations = new ArrayList<>();
        operationOrder = new ArrayList<>();
    }

    /**
     * @param operations list of operations
     */
    public OperationList(final List<Operation> operations) {
        this.operations = operations;
    }

    /**
     * @param operations list of operations
     * @param operationOrder list of operations order
     */
    public OperationList(final List<Operation> operations, final List<String> operationOrder) {
        this.operations = operations;
        this.operationOrder = operationOrder;
    }

    /**
     * Get the operationOrder.
     *
     * @return the order of the operations list
     */
    public List<String> getOperationOrder() {
        return operationOrder;
    }

    /**
     * @param operationOrder the list of operation order
     */
    public void setOperationOrder(final List<String> operationOrder) {
        this.operationOrder = operationOrder;
    }

    /**
     * Get the operations list.
     *
     * @return the list of operations.
     */
    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * @param operations the operations list
     */
    public void setOperations(final List<Operation> operations) {
        this.operations = operations;
    }
}
