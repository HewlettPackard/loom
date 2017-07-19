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

import com.hp.hpl.loom.model.ActionParameter.Type;

/**
 * Annotation for the actions.
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ActionParameter {
    /**
     * Get the id.
     */
    String id();

    /**
     * Get the type default to STRING.
     */
    Type type() default Type.STRING;

    /**
     * Get the name default to the string "NULL".
     */
    String name() default NULL;

    /**
     * Get the ActionParameter ranges.
     */
    ActionRange[] ranges() default {};

    /**
     * NULL string = the string "NULL".
     */
    String NULL = "null";

}
