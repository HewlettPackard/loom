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

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.os.TestTypeInstance;
import com.hp.hpl.loom.adapter.os.TestTypeProject;
import com.hp.hpl.loom.adapter.os.TestTypeVolume;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * Test Instance to test Connected Relationships via standard representation.
 */
@ItemTypeInfo(TestTypeInstanceStitch.TYPE_ID)
@ConnectedTo(toClass = TestTypeInstanceStitch.class)
public class TestTypeInstanceStitch extends SeparableItem<TestTypeInstanceStitchAttributes> {

    public static final String TYPE_ID = "instanceStitch";

    public TestTypeInstanceStitch() {
        super();
    }

    public TestTypeInstanceStitch(final String logicalId, final String name, final String deviceName,
            final ItemType type) {
        super(logicalId, type, name);
        TestTypeInstanceStitchAttributes core = new TestTypeInstanceStitchAttributes();
        core.setDeviceName(deviceName);
        this.setCore(core);
    }

    @Override
    public boolean update() {
        return true;
    }

}
