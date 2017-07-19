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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.loom.model.QueryResult;
import com.hp.hpl.loom.model.Session;

public class QueryResultCache extends ConcurrentHashMap<Session, Map<String, QueryResult>> {

    private static final int INITIAL_CACHE = 10;

    public QueryResultCache(final int numUsers) {
        super(numUsers);
    }

    public QueryResult getCachedResult(final Session session, final String logicalId) {
        QueryResult result = null;
        Map<String, QueryResult> queryMap = this.get(session);
        if (queryMap != null) {
            result = queryMap.get(logicalId);
        }
        return result;
    }

    public void cacheResult(final Session session, final String logicalId, final QueryResult qr) {
        Map<String, QueryResult> queryMap = this.get(session);
        if (queryMap == null) {
            queryMap = new HashMap<>(INITIAL_CACHE); // roughly 10 threads per session
        }
        queryMap.put(logicalId, qr);
        this.put(session, queryMap);
    }

    public void cleanCachedResult(final Session session, final String logicalId) {
        Map<String, QueryResult> queryMap = this.get(session);
        if (queryMap != null) {
            queryMap.remove(logicalId);
        }
    }

    public void clear(final Session session) {
        this.remove(session);
    }
}
