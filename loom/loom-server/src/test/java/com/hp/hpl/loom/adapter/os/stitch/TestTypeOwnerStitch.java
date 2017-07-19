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
package com.hp.hpl.loom.adapter.os.stitch;

import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.SeparableItem;

/**
 * Test Instance to test Connected Relationships via standard representation.
 */
@ItemTypeInfo(TestTypeOwnerStitch.TYPE_ID)
public class TestTypeOwnerStitch extends SeparableItem<TestTypeOwnerStitchAttributes> {

    public static final String TYPE_ID = "owner";


    public TestTypeOwnerStitch() {
        super();
    }

    public TestTypeOwnerStitch(final String logicalId, final String name, final String owner, final ItemType type) {
        super(logicalId, type, name);
        TestTypeOwnerStitchAttributes core = new TestTypeOwnerStitchAttributes();
        core.setOwner(owner);
        this.setCore(core);
    }

    @Override
    public boolean update() {
        return true;
    }

}
