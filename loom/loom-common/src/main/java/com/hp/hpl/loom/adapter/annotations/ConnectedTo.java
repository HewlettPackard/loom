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
 * Annotation, applied to a specific subclass of Item, to specify the schema of connected
 * relationships with other Item types. Typically, these annotations express the expected schema for
 * relationships between Items created by a specific Adapter. The annotations must appear in
 * matching pairs, expressed on both sub-classes of Item involved in each side of the specific
 * relationship. For example if A and B are related sub-classes of Item, then both A and B must have
 * a ConnectTo annotation, with A specifying the relationship to B, and B specifying the equivalent
 * relationship to A.
 * <p>
 * In most cases, the naming of relationships can follow the default naming scheme based on the
 * local type IDs of the related item types. If an Item with class A with type id "a" is related to
 * an Item with class B with type id "b", then the default name of the relationship between the two
 * types of item is "a.b", where the convention is that "a" and "b" are ordered lexicographically.
 * The utility class RelationshipUtil can be used to construct these relationship names.
 * <p>
 * If more than one kind of relationship exists between a pair of resources A and B, the additional
 * relationships can be explicitly named by specifying a string identifier in the annotation. The
 * name of this identifier must be identical in the matching ConnectedTo annotation in both sides (A
 * and B) of the relationship.
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Repeatable(ConnectedTos.class)
public @interface ConnectedTo {

    /**
     * Optional name of the relationship. If not specified, the name of the relationship will be
     * taken from the combination of the local type IDs in the annotated ItemTypeInfo information
     * from the pair of related classes.
     * <p>
     * If the name is directly specified, then it must match a correspondingly named ConnectedTo
     * annotation in the connected class.
     */
    String value() default "";


    /**
     * The relationship type.
     */
    String type() default "";

    /**
     * The relationship type name.
     */
    String typeName() default "";


    /** Class of related object. */
    Class toClass();

    /**
     * The relationshipDetails.
     */
    LoomAttribute relationshipDetails() default @LoomAttribute(key = NULL, supportedOperations = {});

    /**
     * The null representation (a string "null").
     */
    String NULL = "null";
}
