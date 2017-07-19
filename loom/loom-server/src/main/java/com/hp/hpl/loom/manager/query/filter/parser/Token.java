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

import com.hp.hpl.loom.manager.query.QueryUtils;

/**
 * A token task that is used by the expression parser.
 */
public final class Token {
    private final TokenType type;
    private String text;

    /**
     * Constructs a new token object, based on the type and text.
     *
     * If the type is NAME then it converts any *'s into .*? to support java native regex.
     *
     * @param type the Token type
     * @param text the text
     */
    public Token(final TokenType type, final String text) {
        this.type = type;
        this.text = text;
        if (type.equals(TokenType.NAME)) {
            // quote the entire string then replace the * with \E.*?\Q so it is unquoted
            // if (text.contains("*")) {
            // String regex = "\\Q" + text + "\\E";
            // regex = regex.replace("*", "\\E.*?\\Q");
            // this.text = regex;
            // }
            if (text.contains("*")) {
                this.text = QueryUtils.wildcardToRegex(text);
            } else {
                this.text = text;
            }
        }
    }

    /**
     * Get the token type.
     *
     * @return the token type
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Get the token text.
     *
     * @return the token text
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }


}
