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


import com.hp.hpl.loom.model.QueryResult;

public class QueryResultPendingForTooLongException extends Throwable {

    private String message;

    private QueryResult qr = null;

    public QueryResultPendingForTooLongException(final String message, final QueryResult qr) {
        this.message = message;
        this.qr = qr;
    }

    public QueryResultPendingForTooLongException(final QueryResult qr) {
        this.qr = qr;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public QueryResult getQueryResult() {
        return qr;
    }
}
