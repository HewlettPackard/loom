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
package com.hp.hpl.loom.manager.query.filter.parser;

/**
 * Sets the precedence levels for all the different infix parses. It determines how they are
 * grouped. Larger numbers means higher precedence.
 */
public final class Precedence {
    /**
     * Or precedence.
     */
    public static final int OR = 1;
    /**
     * And precedence.
     */
    public static final int AND = 2;
    /**
     * Test precedence (=, !=, > and <).
     */
    public static final int TEST = 3;

    private Precedence() {}
}
