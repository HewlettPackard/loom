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
package com.hp.hpl.loom.manager.query.filter;

import com.hp.hpl.loom.manager.query.filter.parser.ExpressionLexer;
import com.hp.hpl.loom.manager.query.filter.parser.Parser;
import com.hp.hpl.loom.manager.query.filter.parser.Precedence;
import com.hp.hpl.loom.manager.query.filter.parser.TokenType;
import com.hp.hpl.loom.manager.query.parser.impl.BinaryOpParser;
import com.hp.hpl.loom.manager.query.parser.impl.GroupParser;
import com.hp.hpl.loom.manager.query.parser.impl.NameParser;

/**
 * Sets up a filter expression parser.
 *
 * It registers the different parsers infix, name and group ones.
 *
 * Based a Pratt parsers (http://en.wikipedia.org/wiki/Pratt_parser) for the expression handler
 *
 */
public class FilterParser extends Parser {
    /**
     * Create a filter parser.
     *
     * @param lexer the expressionLexer
     */
    public FilterParser(final ExpressionLexer lexer) {
        super(lexer);

        // Register all of the parsers for the grammar.
        register(TokenType.NAME, new NameParser());
        register(TokenType.LEFT_PAREN, new GroupParser());
        infixLeft(TokenType.EQUALS, Precedence.TEST);
        infixLeft(TokenType.NOT_EQUALS, Precedence.TEST);
        infixLeft(TokenType.GREATER, Precedence.TEST);
        infixLeft(TokenType.LESSER, Precedence.TEST);
        infixLeft(TokenType.OR, Precedence.OR);
        infixLeft(TokenType.AND, Precedence.AND);
    }

    /**
     * Sets a left associative binary parser for a given token and precedence.
     *
     * @param token The token
     * @param precedence the precedence of the token
     */
    public void infixLeft(final TokenType token, final int precedence) {
        register(token, new BinaryOpParser(precedence));
    }

}
