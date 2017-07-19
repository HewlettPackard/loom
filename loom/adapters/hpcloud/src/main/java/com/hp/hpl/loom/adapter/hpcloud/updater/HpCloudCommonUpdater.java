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
package com.hp.hpl.loom.adapter.hpcloud.updater;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.hpcloud.HpCloudAdapter;
import com.hp.hpl.loom.adapter.hpcloud.HpCloudCollector;
import com.hp.hpl.loom.adapter.hpcloud.db.MongoDbConnection;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItem;
import com.hp.hpl.loom.adapter.hpcloud.item.HpCloudItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;

public abstract class HpCloudCommonUpdater<T extends HpCloudItem<A>, A extends HpCloudItemAttributes>
        extends AggregationUpdater<T, A, String> {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(HpCloudCommonUpdater.class);

    protected MongoDbConnection dbConn;
    protected HpCloudCollector collector;

    protected abstract String getItemName();

    protected abstract void setRelationships(T item);

    protected abstract String[] getPayloadFields();

    // CONSTRUCTORS -----------------------------------------------------------
    protected HpCloudCommonUpdater(final Aggregation aggregation, final HpCloudAdapter adapter,
            final HpCloudCollector collector, final String typeLocalId) throws NoSuchItemTypeException {
        super(aggregation, adapter, typeLocalId, collector);

        // Handle properly the exception
        this.dbConn = adapter.getDbConnection();
        this.collector = collector;

        log.debug("Saul: Creating HpCloudCommonUpdater (Specific class: " + this.getClass() + ")");
    }

    // CONSTRUCTORS - END -----------------------------------------------------

    // METHODS ----------------------------------------------------------------

    @Override
    protected Iterator<String> getResourceIterator() {
        return dbConn.getAllIds("source.type", getItemName()).iterator();
    }

    @Override
    protected String getItemId(final String resource) {
        return resource;
    }

    protected A createItemAttributes(final A item, final String resource) {
        dbConn.getFullItem(item, resource, getPayloadFields());
        return item;
    }


    // METHODS - END ----------------------------------------------------------

}
