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

import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public interface QueryExecutor {

    QueryResult processQuery(final Session session, final ThreadDefinition threadDef) throws NoSuchSessionException,
            LogicalIdAlreadyExistsException, NoSuchAggregationException, InvalidQueryInputException, OperationException,
            ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    /**
     * Test only method, provides no relationships
     *
     * @param session
     * @param threadDef
     * @param derivedAgreggLogicalId
     * @return
     * @throws NoSuchSessionException
     * @throws LogicalIdAlreadyExistsException
     * @throws NoSuchAggregationException
     * @throws InvalidQueryInputException
     * @throws OperationException
     * @throws ItemPropertyNotFound
     * @throws RelationPropertyNotFound
     * @throws ThreadDeletedByDynAdapterUnload
     */
    QueryResult processQuery(final Session session, final ThreadDefinition threadDef, String derivedAgreggLogicalId)
            throws NoSuchSessionException, LogicalIdAlreadyExistsException, NoSuchAggregationException,
            InvalidQueryInputException, OperationException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload;


    QueryResult processQuery(final Session session, final ThreadDefinition threadDef, List<ThreadDefinition> threads,
            boolean forceRelationsRecalculation)
            throws NoSuchSessionException, NoSuchAggregationException, InvalidQueryInputException, ItemPropertyNotFound,
            OperationException, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    boolean isSupportedOperation(String opId);

    void clearUnused(Session s, String logicalId)
            throws NoSuchSessionException, ItemPropertyNotFound, NoSuchAggregationException;

    void clear(final Session thisSession);
}
