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
package com.hp.hpl.loom.exceptions;

/**
 * Thrown when the query definition can't be found.
 */
public class NoSuchQueryDefinitionException extends CheckedLoomException {
    private String query;

    /**
     * @param query the query
     */
    public NoSuchQueryDefinitionException(final String query) {
        super("Query for " + query + " does not exist");
        this.query = query;
    }

    /**
     * @param query the query
     * @param cause the cause
     */
    public NoSuchQueryDefinitionException(final String query, final Throwable cause) {
        super("Query for " + query + " does not exist", cause);
        this.query = query;
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    public String getQuery() {
        return query;
    }
}
