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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.manager.query.filter.element.Element;
import com.hp.hpl.loom.manager.query.parser.impl.InfixParser;
import com.hp.hpl.loom.manager.query.parser.impl.PrefixParser;

/**
 * Base parser.
 *
 */
public class Parser {
    private final Iterator<Token> tokens;
    private final List<Token> read = new ArrayList<Token>();

    private final Map<TokenType, PrefixParser> prefixParser = new HashMap<TokenType, PrefixParser>();
    private final Map<TokenType, InfixParser> infixParser = new HashMap<TokenType, InfixParser>();

    /**
     * Contructor that takes an iterator of tokens.
     *
     * @param tokens the tokens to parse
     */
    public Parser(final Iterator<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Method to register a particular parser responsible for the provided token.
     *
     * @param token the token type it deals with
     * @param parser the parser
     */
    public void register(final TokenType token, final PrefixParser parser) {
        prefixParser.put(token, parser);
    }

    /**
     * Method to register a particular parser responsible for the provided token.
     *
     * @param token the token type it deals with
     * @param parser the parser
     */
    public void register(final TokenType token, final InfixParser parser) {
        infixParser.put(token, parser);
    }

    /**
     * Gets the next element based on the provided precedence.
     *
     * @param precedence the precedence of the Element to look up
     * @return the element based on the precedence
     */
    public Element parseExpression(final int precedence) {
        Token token = consume();
        PrefixParser prefix = prefixParser.get(token.getType());

        if (prefix == null) {
            throw new ParseException("Could not parse \"" + token.getText() + "\".");
        }

        Element left = prefix.parse(this, token);

        while (precedence < getPrecedence()) {
            token = consume();

            InfixParser infix = infixParser.get(token.getType());
            left = infix.parse(this, left, token);
        }

        return left;
    }

    /**
     * Parse the current expression.
     *
     * @return returns the element
     */
    public Element parseExpression() {
        return parseExpression(0);
    }

    // /**
    // * Does this token match the expected one.
    // *
    // * @param expected the token expected
    // * @return true if it does match
    // */
    // public boolean match(final TokenType expected) {
    // Token token = lookAhead(0);
    // if (token.getType() != expected) {
    // return false;
    // }
    //
    // consume();
    // return true;
    // }

    /**
     * Consume the tokenType (based on the expected on).
     *
     * @param expected the next token type excepted.
     * @return the token
     */
    public Token consume(final TokenType expected) {
        Token token = lookAhead(0);
        if (token.getType() != expected) {
            throw new RuntimeException("Expected token " + expected + " and found " + token.getType());
        }

        return consume();
    }

    /**
     * Consume the current token.
     *
     * @return the token we are consuming
     */
    public Token consume() {
        // Make sure we've read the token.
        lookAhead(0);

        return read.remove(0);
    }

    private Token lookAhead(final int distance) {
        // Read in as many as needed.
        while (distance >= read.size()) {
            read.add(tokens.next());
        }

        // Get the queued token.
        return read.get(distance);
    }

    private int getPrecedence() {
        InfixParser parser = infixParser.get(lookAhead(0).getType());
        if (parser != null) {
            return parser.getPrecedence();
        }

        return 0;
    }
}
