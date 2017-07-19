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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * ActionDefintion annotation.
 */
@Repeatable(Actions.class)
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ActionDefinition {
    /**
     * Get the id defaulting to NULL (string "NULL").
     */
    String id() default NULL;

    /**
     * Get the name defaulting to NULL (string "NULL").
     */
    String name() default NULL;

    /**
     * Get the description defaulting to NULL (string "NULL").
     */
    String description() default NULL;

    /**
     * Get the icon defaulting to NULL (string "NULL").
     */
    String icon() default NULL;

    /**
     * Get the type.
     */
    ActionTypes type();

    /**
     * Get the parameters.
     */
    ActionParameter[] parameters() default {};

    /**
     * null string type.
     */
    String NULL = "null";
}
