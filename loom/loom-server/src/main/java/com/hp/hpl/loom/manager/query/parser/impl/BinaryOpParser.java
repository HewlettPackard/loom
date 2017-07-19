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
package com.hp.hpl.loom.manager.query.parser.impl;

import com.hp.hpl.loom.manager.query.filter.element.Element;
import com.hp.hpl.loom.manager.query.filter.element.OperatorElement;
import com.hp.hpl.loom.manager.query.filter.parser.Parser;
import com.hp.hpl.loom.manager.query.filter.parser.Token;

/**
 * Infix parser for a binary arithmetic operator it handles: equals, not equals, greater, and,
 * lesser, and or.
 */
public class BinaryOpParser implements InfixParser {

    private final int precedence;

    /**
     * Takes the precedence for this parser.
     *
     * @param precedence the precedence
     */
    public BinaryOpParser(final int precedence) {
        this.precedence = precedence;
    }

    @Override
    public Element parse(final Parser parser, final Element left, final Token token) {
        Element right = parser.parseExpression(precedence);
        return new OperatorElement(left, token.getType(), right);
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

}
