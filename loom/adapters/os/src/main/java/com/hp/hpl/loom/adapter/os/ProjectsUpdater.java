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

import java.util.Iterator;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;

public abstract class ProjectsUpdater<R> extends AggregationUpdater<OsProject, OsProjectAttributes, R>
        implements Iterator<R> {

    Iterator<? extends R> resourceIterator;

    public ProjectsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final ItemCollector itemCollector) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, itemCollector);
    }

    @Override
    protected Iterator<R> getResourceIterator() {
        resourceIterator = getResources();
        return this;
    }

    @Override
    public boolean hasNext() {
        return resourceIterator.hasNext();
    }

    @Override
    public R next() {
        return resourceIterator.next();
    }

    @Override
    public void remove() {
        // not needed so not implemented
    }

    protected abstract Iterator<? extends R> getResources();

    @Override
    protected OsProject createEmptyItem(final String logicalId) {
        return new OsProject(logicalId, itemType);
    }

}
