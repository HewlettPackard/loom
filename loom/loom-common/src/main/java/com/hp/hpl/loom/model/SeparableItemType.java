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
package com.hp.hpl.loom.model;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;

/**
 * SeperableItemType allows the use of the Delta mechanism by describing part of the item that can
 * be separated.
 */
public class SeparableItemType extends ItemType {
    private static final Log LOG = LogFactory.getLog(SeparableItemType.class);

    protected Attribute itemName;
    protected Attribute itemDescription;
    protected Attribute itemId;

    /**
     * Constructor that takes the local id that it describes.
     *
     * @param localId the localId
     */
    public SeparableItemType(final String localId) {
        super(localId);
        try {
            itemName = new Attribute.Builder(ItemType.CORE_NAME + "itemName").name("itemName").visible(true)
                    .plottable(false).build();

            itemDescription = new Attribute.Builder(ItemType.CORE_NAME + "itemDescription").name("itemDescription")
                    .visible(true).plottable(false).build();

            itemId = new Attribute.Builder(ItemType.CORE_NAME + "itemId").name("itemID").visible(true).plottable(false)
                    .build();

            Set<OrderedString> sortOperations = new OperationBuilder().add(itemName, itemId, itemDescription).build();
            addOperations(DefaultOperations.SORT_BY.toString(), sortOperations);
            addOperations(DefaultOperations.GROUP_BY.toString(), sortOperations);

            addAttributes(itemId, itemName, itemDescription);

        } catch (AttributeException e) {
            LOG.error("Problem constructing OsItemType attributes: ", e);
        }
    }
}
