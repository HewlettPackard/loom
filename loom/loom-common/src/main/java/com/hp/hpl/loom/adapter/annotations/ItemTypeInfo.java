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
package com.hp.hpl.loom.adapter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, applied to a subclass of Item, that defines additional information about the item
 * type associated with the specific Java class, sub-classed from Item, used to represent the item.
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ItemTypeInfo {

    /***
     * Layer where all items belong, if not defined by the adapter writer.
     */
    String DEFAULT_LAYER = "default";

    /** Local ID of the ItemType associated with items represented by the annotated Item class. */
    String value();

    /**
     * The sorting order.
     */
    Sort[] sorting() default {};

    /**
     * Get the additional operations defined by this adapter that operate on this itemType.
     */
    String[] supportedAdditionalOperations() default {};

    /**
     * Used for multiplex graphs. It contains the layers that this belongs to. During traversal
     * time, the Relationship Calculator will never revisit layers that it visited before. If not
     * defined, default is assumes.
     */
    String[] layers() default {DEFAULT_LAYER};

}
