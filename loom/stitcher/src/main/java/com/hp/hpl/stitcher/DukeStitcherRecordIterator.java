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
package com.hp.hpl.stitcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.RecordIterator;

public class DukeStitcherRecordIterator<T> extends RecordIterator {

    private Collection<Record> records;
    private Iterator<Record> it;

    public DukeStitcherRecordIterator() {
        records = new ArrayList<Record>();
    }

    public void addRecord(Record record) {
        records.add(record);
    }

    public void addRecords(Collection<Record> recordsToAdd) {
        records.addAll(recordsToAdd);
    }

    public DukeStitcherRecordIterator<T> start() {
        it = records.iterator();
        return this;
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    public Record next() {
        return it.next();
    }

}
