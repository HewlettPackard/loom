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

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.hp.hpl.loom.model.introspection.IntrospectionContext;

/**
 * Item with extra attributes, stored in a separate object. Items must sub-class SeparableItem to be
 * able to use the Delta Mode when updating the Grounded Aggregation.
 *
 * @param <A> CoreItemAttribute subtype this is a SeparableItem for
 */
public class SeparableItem<A extends CoreItemAttributes> extends Item {

    /**
     * The coreName for this item.
     */
    public static final String CORE_NAME = ItemType.CORE_NAME;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonUnwrapped(prefix = CORE_NAME)
    private A core;

    /**
     * No-arg constructor for JSON serialisation.
     */
    protected SeparableItem() {
        super();
    }

    /**
     * Minimal set of parameters to pass in constructor.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     */
    public SeparableItem(final String logicalId, final ItemType type) {
        super(logicalId, type);
    }


    /**
     * Convenience constructor, with name for the item.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     * @param name Name of the item.
     */
    public SeparableItem(final String logicalId, final ItemType type, final String name) {
        super(logicalId, type, name);
    }

    /**
     * Convenience constructor, with name and description for the item.
     *
     * @param logicalId Logical ID of the item.
     * @param type Type of the item. The ID and local ID of the type must first have been correctly
     *        set.
     * @param name Name of the item.
     * @param description Description of the item.
     */
    public SeparableItem(final String logicalId, final ItemType type, final String name, final String description) {
        super(logicalId, type, name, description);
    }

    /**
     * Set the core attributes of the item. Note that if {@link CoreItemAttributes#getItemName()} or
     * {@link CoreItemAttributes#getItemDescription()} return non-null values, the value will be
     * used to set the name and description of the Item - i.e. {@link Fibre#setName(String)} or
     * {@link Fibre#setDescription(String)} will be called, possibly overriding ant value set in the
     * constructor.
     *
     * @param attributes New set of attributes.
     */
    public void setCore(final A attributes) {
        core = attributes;
        if (core != null) {
            if (core.getItemName() != null) {
                setName(core.getItemName());
            }
            if (core.getItemDescription() != null) {
                setDescription(core.getItemDescription());
            }
        }
    }

    /**
     * Return the core type this is part of.
     *
     * @return CoreItemAttribute subtype this is a SeparableItem for
     */
    public final A getCore() {
        return core;
    }

    @Override
    public final IntrospectionContext getIntrospectionContextForProperty(final String propertyName) {
        CoreItemAttributes coreItemAttributes = getCore();
        if (coreItemAttributes != null && propertyName.startsWith(CORE_NAME)) {
            return new IntrospectionContext(coreItemAttributes, propertyName.substring(CORE_NAME.length()));
        } else {
            return super.getIntrospectionContextForProperty(propertyName);
        }
    }

    @Override
    public final IntrospectionContext getIntrospectionContextForAllProperties() {
        return new IntrospectionContext(getCore() == null ? Arrays.asList(this) : Arrays.asList(this, getCore()));
    }
}
