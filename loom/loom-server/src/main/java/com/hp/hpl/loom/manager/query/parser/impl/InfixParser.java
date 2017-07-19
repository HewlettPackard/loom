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
import com.hp.hpl.loom.manager.query.filter.parser.Parser;
import com.hp.hpl.loom.manager.query.filter.parser.Token;

/**
 * InfixParser parses tokens that appear between two expressions. Its parse() method will be called
 * when the left hand side has been parsed and it is responsible for parsing after the token.
 */
public interface InfixParser {
    /**
     * Parse the next part of the token.
     *
     * @param parser the parser (to read the next expression from)
     * @param left the left had side of the expression
     * @param token the token
     * @return the expression / element
     */
    Element parse(Parser parser, Element left, Token token);

    /**
     * Returns the order of precedence of this parser vs other ones.
     *
     * @return the precedence
     */
    int getPrecedence();
}
