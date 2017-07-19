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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An expression lexer that converts the search expression into a series of tokens. Operators and
 * punctuation are mapping to the correct TokenTypes. It also handles tokenising quoted strings into
 * a single value.
 */
public class ExpressionLexer implements Iterator<Token> {
    private final Map<String, TokenType> punctuators = new HashMap<String, TokenType>();
    private final String text;
    private int i = 0;

    /**
     * Creates a new ExpressionLexer to tokenise the given string.
     *
     * @param text String to tokenise.
     */
    public ExpressionLexer(final String text) {
        i = 0;
        this.text = text;
        // Register all of the TokenTypes that are explicit punctuators.
        for (TokenType type : TokenType.values()) {
            String punctuator = type.punctuator();
            if (punctuator != null) {
                punctuators.put(punctuator, type);
            }
        }
    }

    /**
     * Returns a token this string starts with.
     *
     * @return
     */
    private String startWith(final String value) {
        for (String token : punctuators.keySet()) {
            if (value.startsWith(token)) {
                return token;
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Token next() {
        while (i < text.length()) {
            int index = text.indexOf(" ", i);
            if (index == -1) {
                index = text.length();
            }
            String n = text.substring(i, index);
            char c = text.charAt(i++);

            if (punctuators.containsKey(n)) {
                // Handle punctuation.
                i += n.length() - 1; // jump forward the length of the operator - minus one as
                                     // we incremented above already
                return new Token(punctuators.get(n), n);
            } else if (punctuators.containsKey(c + "")) {
                // Handle punctuation.
                return new Token(punctuators.get(c + ""), c + "");
            } else if (startWith(n) != null) {
                String token = startWith(n);
                i += token.length() - 1;
                return new Token(punctuators.get(token), token);
            } else if (Character.isLetterOrDigit(c) || c == '\"' || c == '.' || c == '*' || c == '-') {
                // handles letters, quotes, . and *
                boolean startQuote = false;

                // Handle names.
                int start = i - 1;
                if (c == '\"') {
                    startQuote = true;
                    start++;
                }
                while (i < text.length()) {
                    if ((!Character.isLetterOrDigit(text.charAt(i)) && !startQuote && text.charAt(i) != '.'
                            && text.charAt(i) != '*' && text.charAt(i) != '-' && text.charAt(i) != '/')
                            || (startQuote && text.charAt(i) == '\"')) {
                        startQuote = false;
                        break;
                    }
                    i++;
                }

                String name = text.substring(start, i);
                return new Token(TokenType.NAME, name);
            }
        }
        // returns EOF forever
        return new Token(TokenType.EOF, "");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
