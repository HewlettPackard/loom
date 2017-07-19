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
package com.hp.hpl.loom.adapter.hpcloud.item;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.ItemType;

/**
 * This class models the HPCloud Items retrieved from the mongodb.
 *
 * @param <A>
 */
public class HpCloudItem<A extends CoreItemAttributes> extends AdapterItem<A> {
    // VARIABLES - END --------------------------------------------------------

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * The full constructor for the HpCloudItem.
     * 
     * @param logicalId logical id of this item
     * @param type The itemtype
     * @param name the name
     * @param description the description
     */
    public HpCloudItem(final String logicalId, final ItemType type, final String name, final String description) {
        super(logicalId, type, name, description);
        // TODO Auto-generated constructor stub
    }

    /**
     * The partial constructor (it doesn't take the description).
     * 
     * @param logicalId logical id of this item
     * @param type The itemtype
     * @param name the name
     */
    public HpCloudItem(final String logicalId, final ItemType type, final String name) {
        super(logicalId, type, name);
        // TODO Auto-generated constructor stub
    }

    /**
     * The partial constructor (it doesn't take the name or description).
     * 
     * @param logicalId logical id of this item
     * @param type The itemtype
     */
    public HpCloudItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
        // TODO Auto-generated constructor stub
    }

    // CONSTRUCTORS - END -----------------------------------------------------

}
