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

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.CollectionType;
import com.hp.hpl.loom.manager.query.DefaultOperations;

/**
 * The LoomAttribute annotation.
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LoomAttribute {

    /**
     * Get the key.
     */
    String key();

    /**
     * Get the visible flag - defaulted to true.
     */
    boolean visible() default true;

    /**
     * Get the plottable flag - defaulted to false.
     */
    boolean plottable() default false;

    /**
     * Get mappable flag -defaulted to false.
     */
    boolean mappable() default false;

    /**
     * Get ignoreUpdate -defaulted to false.
     */
    boolean ignoreUpdate() default false;

    /**
     * Get the default operations.
     */
    DefaultOperations[] supportedOperations();

    /**
     * Get the additional operations defined by this adapter.
     */
    String[] supportedAdditionalOperations() default {};

    /**
     * Get the range for this attribute.
     */
    LiteralRange[] range() default {};

    /**
     * Get the operation order.
     */
    int[] operationOrder() default {};

    /**
     * Get the class type, defaults to {@link Attribute}.
     */
    Class<?> type() default Attribute.class;

    // Additional information for the NumericAttribute type.

    /**
     * The min value used for the {@link com.hp.hpl.loom.adapter.NumericAttribute}. Defaults to NULL
     * (the string "null").
     */
    String min() default NULL;

    /**
     * The max value used for the {@link com.hp.hpl.loom.adapter.NumericAttribute}. Defaults to NULL
     * (the string "null").
     */
    String max() default NULL;

    /**
     * The unit value used for the {@link com.hp.hpl.loom.adapter.NumericAttribute}. Defaults to
     * NULL (the string "null").
     */
    String unit() default NULL;

    /**
     * Whether this attribute is a collection and if so what time (only ARRAY is supported at the
     * moment) The array items are all of same type.
     *
     * @return
     */
    CollectionType collectionType() default CollectionType.NONE;

    /**
     * The group this annotation is part of.
     */
    String group() default NULL;

    /**
     * Get longitude.
     */
    boolean longitude() default false;

    /**
     * Get latitude.
     */
    boolean latitude() default false;

    /**
     * Get country.
     */
    boolean country() default false;

    /**
     * Get the format.
     */
    String format() default NULL;

    /**
     * Get the short format.
     */
    String shortFormat() default NULL;

    /**
     * NULL string = the string "NULL".
     */
    String NULL = "null";

}
