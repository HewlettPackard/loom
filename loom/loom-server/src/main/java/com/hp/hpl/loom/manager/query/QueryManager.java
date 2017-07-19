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
package com.hp.hpl.loom.manager.query;

import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.TapestryDefinition;

public interface QueryManager {


    void preCreateQueryResponses(Session session, TapestryDefinition newTap)
            throws NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            NoSuchAggregationException, NoSuchSessionException, NoSuchTapestryDefinitionException,
            InvalidQueryInputException, ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    QueryResult getThread(Session session, String threadId, boolean forceRelationsRecalculation)
            throws NoSuchSessionException, NoSuchThreadDefinitionException, NoSuchTapestryDefinitionException,
            NoSuchQueryDefinitionException, NoSuchAggregationException, LogicalIdAlreadyExistsException,
            InvalidQueryInputException, OperationException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload;

    void tapestryDefinitionChanged(final Session session, final TapestryDefinition oldTapestry,
            final TapestryDefinition newTapestry)
            throws NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            NoSuchAggregationException, NoSuchSessionException, NoSuchTapestryDefinitionException,
            InvalidQueryInputException, ItemPropertyNotFound, RelationPropertyNotFound, ThreadDeletedByDynAdapterUnload;

    void clear(final Session session);
}
