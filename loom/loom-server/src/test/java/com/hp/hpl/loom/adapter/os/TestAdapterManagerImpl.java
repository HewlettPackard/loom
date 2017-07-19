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
package com.hp.hpl.loom.adapter.os;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.adapter.AggregationUpdate;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.adapter.AdapterManagerImpl;
import com.hp.hpl.loom.model.Session;

@Component
public class TestAdapterManagerImpl extends AdapterManagerImpl {

    private static final Log LOG = LogFactory.getLog(AdapterManagerImpl.class);
    private Map<String, Map<Integer, Collection<AggregationUpdate>>> sessionMap = new HashMap<>();

    @Override
    public synchronized void updateGroundedAggregations(final Session session,
            final Collection<AggregationUpdate> aggregationUpdates)
            throws NoSuchSessionException, NoSuchAggregationException {
        Map<Integer, Collection<AggregationUpdate>> auMap = sessionMap.get(session.getId());
        if (auMap == null) {
            auMap = new HashMap<>();
            sessionMap.put(session.getId(), auMap);
        }
        int lastSize = auMap.size();
        auMap.put((lastSize + 1), aggregationUpdates);
        // finally call super
        super.updateGroundedAggregations(session, aggregationUpdates);
    }

    public synchronized int getAggregationUpdatesMapSize(final Session session) {
        Map<Integer, Collection<AggregationUpdate>> auMap = sessionMap.get(session.getId());
        if (auMap == null) {
            auMap = new HashMap<>();
            sessionMap.put(session.getId(), auMap);
        }
        return auMap.size();
    }

    public Collection<AggregationUpdate> getAggregationUpdates(final Session session, final int idx) {
        Map<Integer, Collection<AggregationUpdate>> auMap = sessionMap.get(session.getId());
        if (auMap == null) {
            auMap = new HashMap<>();
            sessionMap.put(session.getId(), auMap);
        }
        return auMap.get(idx);
    }

    public synchronized void clearAggregationUpdatesMap(final Session session) {
        sessionMap.remove(session.getId());
    }

}
