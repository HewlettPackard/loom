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
package com.hp.hpl.loom.api.service.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.QueryDefinition;

public interface QueryFormatting {
    static final Log log = LogFactory.getLog(QueryFormatting.class);

    static QueryDefinition createDrillDownQuery(final QueryResult qr) {
        return createDrillDownToFirstFibreQuery(qr, 0);
    }

    static QueryDefinition createDrillDownToFirstFibreQuery(final QueryResult qr, final int maxFibres) {
        return indxDrillDown(qr, maxFibres, 0);
    }

    static QueryDefinition createDrillDownToRandomFibreQuery(final QueryResult qr, final int maxFibres) {
        int min = 0;
        int max = qr.getElements().size() - 1;
        int indx = min + (int) (Math.random() * ((max - min) + 1));
        log.info("Drilling down to fibre: " + indx + " out of " + max);
        return indxDrillDown(qr, maxFibres, indx);
    }

    static QueryDefinition indxDrillDown(final QueryResult qr, final int maxFibres, final int indx) {
        String fibreLogicalId = qr.getElements().get(indx).getEntity().getLogicalId();
        QueryDefinition drillDown =
                maxFibres > 0 ? createBraidQuery(fibreLogicalId, maxFibres) : createSimpleQuery(fibreLogicalId);
        return drillDown;
    }

    static QueryDefinition createGroupByBraidQuery(final String logicalId, final Map<String, Object> groupParams,
            final int maxFibres) {
        return createGroupByBraidQuery(groupParams, maxFibres, Arrays.asList(logicalId));
    }

    static QueryDefinition createGroupByBraidQuery(final QueryResult qr1, final Map<String, Object> groupParams,
            final int maxFibres) {
        String qr1LogicalId = qr1.getLogicalId();
        log.info("selected logicalId: " + qr1LogicalId);
        return createGroupByBraidQuery(groupParams, maxFibres, Arrays.asList(qr1LogicalId));
    }

    static QueryDefinition createBraidQuery(final String input, final int maxFibres) {
        List<String> inputs = new ArrayList<String>(1);
        inputs.add(input);
        return createBraidQuery(inputs, maxFibres);
    }

    static QueryDefinition createBraidQuery(final List<String> inputs, final int maxFibres) {
        Map<String, Object> braidParams = new HashMap<>(1);
        braidParams.put(QueryOperation.MAX_FIBRES, maxFibres);
        Operation braidOperation = new Operation((DefaultOperations.BRAID.toString()), braidParams);
        List<Operation> braidPipe = new ArrayList<Operation>(1);
        braidPipe.add(braidOperation);
        return new QueryDefinition(braidPipe, inputs);
    }

    static QueryDefinition createSimpleQuery(final List<String> inputs) {
        return new QueryDefinition(inputs);
    }

    static QueryDefinition createSimpleQuery(final String input) {
        return new QueryDefinition(input);
    }

    static QueryDefinition createGroupByBraidQuery(final Map<String, Object> groupParams, final int maxFibres,
            final List<String> qrLogicalIds) {
        Operation groupOperation = new Operation((DefaultOperations.GROUP_BY.toString()), groupParams);
        Map<String, Object> braidParams = new HashMap<>(1);
        braidParams.put(QueryOperation.MAX_FIBRES, maxFibres);
        Operation braidOperation = new Operation((DefaultOperations.BRAID.toString()), braidParams);
        List<Operation> groupBraidPipe = new ArrayList<Operation>(2);
        groupBraidPipe.add(groupOperation);
        groupBraidPipe.add(braidOperation);
        return new QueryDefinition(groupBraidPipe, qrLogicalIds);
    }

    static QueryDefinition createFilterBraidQuery(final QueryResult qr1, final Map<String, Object> groupParams,
            final int maxFibres) {
        String qr1LogicalId = qr1.getLogicalId();
        log.info("selected logicalId: " + qr1LogicalId);
        return createFilterBraidQuery(groupParams, maxFibres, Arrays.asList(qr1LogicalId));
    }

    static QueryDefinition createPercentQuery(final QueryResult qr1, final Map<String, Object> groupParams) {
        String qr1LogicalId = qr1.getLogicalId();
        log.info("selected logicalId: " + qr1LogicalId);
        Operation percentOperation = new Operation((DefaultOperations.PERCENTILES.toString()), groupParams);

        List<Operation> percentPipe = new ArrayList<Operation>(1);
        percentPipe.add(percentOperation);
        return new QueryDefinition(percentPipe, Arrays.asList(qr1LogicalId));
    }

    static QueryDefinition createFilterBraidQuery(final Map<String, Object> filterParams, final int maxFibres,
            final List<String> qrLogicalIds) {
        Operation filterOperation = new Operation((DefaultOperations.FILTER_STRING.toString()), filterParams);
        Map<String, Object> braidParams = new HashMap<>(1);
        braidParams.put(QueryOperation.MAX_FIBRES, maxFibres);
        Operation braidOperation = new Operation((DefaultOperations.BRAID.toString()), braidParams);
        List<Operation> groupBraidPipe = new ArrayList<Operation>(2);
        groupBraidPipe.add(filterOperation);
        groupBraidPipe.add(braidOperation);
        return new QueryDefinition(groupBraidPipe, qrLogicalIds);
    }

    static QueryDefinition createGroupByQuery(final String logicalId, final Map<String, Object> groupParams) {
        return createGroupByQuery(groupParams, Arrays.asList(logicalId));
    }

    static QueryDefinition createGroupByQuery(final Map<String, Object> groupParams, final List<String> qrLogicalIds) {
        Operation groupOperation = new Operation((DefaultOperations.GROUP_BY.toString()), groupParams);
        List<Operation> groupBraidPipe = new ArrayList<Operation>(1);
        groupBraidPipe.add(groupOperation);
        return new QueryDefinition(groupBraidPipe, qrLogicalIds);
    }

    static QueryDefinition createSortByQuery(final QueryResult qr1, final Map<String, Object> groupParams,
            final int testFibres) {
        String qr1LogicalId = qr1.getLogicalId();
        log.info("selected logicalId: " + qr1LogicalId);
        return createSortByQuery(qr1LogicalId, groupParams, testFibres);
    }

    static QueryDefinition createSortByQuery(final String threadLogicalId, final Map<String, Object> groupParams,
            final int testFibres) {
        ArrayList<String> ins = new ArrayList<String>(1);
        ins.add(threadLogicalId);

        Operation groupOperation = new Operation((DefaultOperations.SORT_BY.toString()), groupParams);
        List<Operation> sortPipe = new ArrayList<Operation>(testFibres);
        sortPipe.add(groupOperation);
        return new QueryDefinition(sortPipe, ins);
    }

    static QueryDefinition createSortByBraidQuery(final String threadLogicalId, final Map<String, Object> groupParams,
            final int maxFibres) {
        return createSortByBraidQuery(Arrays.asList(threadLogicalId), groupParams, maxFibres);
    }

    static QueryDefinition createSortByBraidQuery(final List<String> ins, final Map<String, Object> groupParams,
            final int maxFibres) {
        Map<String, Object> braidParams = new HashMap<>(1);
        braidParams.put(QueryOperation.MAX_FIBRES, maxFibres);
        Operation braidOperation = new Operation((DefaultOperations.BRAID.toString()), braidParams);
        Operation groupOperation = new Operation((DefaultOperations.SORT_BY.toString()), groupParams);
        List<Operation> sortPipe = new ArrayList<Operation>(maxFibres);
        sortPipe.add(groupOperation);
        sortPipe.add(braidOperation);
        return new QueryDefinition(sortPipe, ins);
    }

    static QueryDefinition createFilterRelatedQuery(final List<String> ins, final String id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put(QueryOperation.ID, id);
        Operation filterOperation = new Operation((DefaultOperations.FILTER_RELATED.toString()), params);
        List<Operation> pipe = new ArrayList<Operation>(1);
        pipe.add(filterOperation);
        return new QueryDefinition(pipe, ins);
    }
}
