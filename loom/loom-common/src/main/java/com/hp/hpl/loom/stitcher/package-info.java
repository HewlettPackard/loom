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
/**
 * Interfaces and classes to define Item Equivalence Rules, also known as Stitcher rules. Stitcher
 * rules allow equivalence relationships to be defined between Items that are created by different
 * Adapters. To do this, an Adapter returns a set of Stitcher Rules that specify how to match an
 * equivalence between an Item of one type in Adapter A to Items of another type in Adapter B.
 * Stitcher rules are optionally returned by an Adapter using the
 * {@link com.hp.hpl.loom.adapter.Adapter#getStitchingRules} method.
 * <p>
 * Adapters should not use Stitcher Rules to specify relationships between Items managed by the same
 * Adapter. Instead, the connected relationships mechanism should be used.
 * <p>
 * A Stitcher rule is specified by a {@link StitcherRulePair}, which consists of a pair of
 * {@link StitcherRule} instances, one defining equivalence matching from Items of type A to Items
 * of type B, the other defining similar equivalence relationships but from B to A. A
 * {@link StitcherRule} defines the scope of the rule by specifying the type ID of both A and B,
 * allowing Items instances of the two type to be selected for comparison; the rule also defines a
 * boolean matching criteria, allowing two instances, one of type A the other of type B, to be
 * matched for equivalence.
 * <p>
 * For some rules, there may be run-time efficiency gains for the stitching process if the Stitcher
 * rules additionally implement the {@link IndexableStitcherRule} interface, which allows the server
 * to maintain an index of the specified values of Items; the intent is that the index will allow
 * identification of a much smaller candidate set of items to be tested for equivalence matching
 * using the criteria defined by the {@link SticherRule}.
 * <p>
 * An Adapter is free to return any class that implements the {@link StitcherRulePair},
 * {@link StitcherRule}, and {@link IndexableStitcherRule} interfaces. A default implementation of
 * the {@link StitcherRule} and {@link IndexableStitcherRule} interfaces, which allow equivalence
 * matching behaviour to be expressed using Java 8 lambdas, have been provided -
 * {@link StitchFunction} and {@link IndexableStitchFunction}.
 * <p>
 */
package com.hp.hpl.loom.stitcher;

