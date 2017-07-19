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
package com.hp.hpl.loom.manager.stitcher.simple;

import java.util.Collection;
import java.util.HashSet;

import com.hp.hpl.loom.model.Item;
import com.hp.hpl.stitcher.ConditionedStitcher;

public class LoomMicroStitcher extends ConditionedStitcher<Item, Item> {

    public LoomMicroStitcher() {
        super();
    }

    public LoomMicroStitcher(int baseInitialCapacity, int candidateInitialCapacity) {
        super(baseInitialCapacity, candidateInitialCapacity);
    }

    public String sourceTypeId() {
        // TODO: Correct this method
        return "source";
    }

    public String destinationTypeId() {
        // TODO: Correct this method
        return "destination";
    }

    @Override
    protected Collection<Item> newDataStructureForStitching() {
        return new HashSet<Item>();
    }

}
