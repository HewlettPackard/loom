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
 * Enumeration that handles the different token types.
 */
public enum TokenType {
    /**
     * Left bracket.
     */
    LEFT_PAREN,
    /**
     * Right bracket.
     */
    RIGHT_PAREN,
    /**
     * Name token.
     */
    NAME,
    /**
     * Equals token.
     */
    EQUALS,
    /**
     * Not equals token.
     */
    NOT_EQUALS,
    /**
     * Or token.
     */
    OR,
    /**
     * And token.
     */
    AND,
    /**
     * Greater than token.
     */
    GREATER,
    /**
     * Less than token.
     */
    LESSER,
    /**
     * End of file token.
     */
    EOF;

    /**
     * Get the punctuator associated with the token.
     *
     * @return the punctuator
     */
    public String punctuator() {
        switch (this) {
            case LEFT_PAREN:
                return "(";
            case RIGHT_PAREN:
                return ")";
            case EQUALS:
                return "=";
            case NOT_EQUALS:
                return "!=";
            case GREATER:
                return ">";
            case LESSER:
                return "<";
            case OR:
                return "or";
            case AND:
                return "and";
            default:
                return null;
        }
    }
}
