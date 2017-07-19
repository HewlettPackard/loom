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
package com.hp.hpl.loom.adapter.os.swift;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.os.real.RealRegionsUpdater;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;

public class SwiftRegionsUpdater extends RealRegionsUpdater {

    private SwiftRealItemCollector sric;

    public SwiftRegionsUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final SwiftRealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        sric = ric;
    }
    //
    // @Override
    // protected String[] getConfiguredRegions(final String projectName) {
    // // String[] firstZones = sric.getNovaZones(projectName);
    // // String[] secondZones = sric.getSwiftZones(projectName);
    // // String[] combinedZones = new String[firstZones.length + secondZones.length];
    // // System.arraycopy(firstZones, 0, combinedZones, 0, firstZones.length);
    // // System.arraycopy(secondZones, 0, combinedZones, firstZones.length, secondZones.length);
    // String[] combinedZones = new String[0];
    // return combinedZones;
    // }

}
