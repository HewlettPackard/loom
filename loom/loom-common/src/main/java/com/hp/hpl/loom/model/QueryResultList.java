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
 * Model for the QueryResultList, it contains a list of QueryResult.
 */
@JsonAutoDetect
public class QueryResultList {

    private List<QueryResult> queryResults;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public QueryResultList() {
        queryResults = new ArrayList<>();
    }

    /**
     * @param queryResults list of QueryResults
     */
    public QueryResultList(final List<QueryResult> queryResults) {
        this.queryResults = queryResults;
    }

    /**
     * Get the list of queryResult.
     *
     * @return the list of queryResults
     */
    public List<QueryResult> getQueryResults() {
        return queryResults;
    }

    /**
     * @param queryResults list of QueryResults
     */
    public void setQueryResults(final List<QueryResult> queryResults) {
        this.queryResults = queryResults;
    }
}
