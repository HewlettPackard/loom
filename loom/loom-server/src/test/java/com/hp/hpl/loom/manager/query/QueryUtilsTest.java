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
package com.hp.hpl.loom.manager.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.hp.hpl.loom.manager.query.filter.FilterParser;
import com.hp.hpl.loom.manager.query.filter.element.Element;
import com.hp.hpl.loom.manager.query.filter.parser.ExpressionLexer;
import com.hp.hpl.loom.manager.query.filter.parser.Parser;

public class QueryUtilsTest {
    @Test
    public void testMatchRawString() {
        // public static boolean match(final List<String> attributes, final String pattern) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("test", "test");
        Map<String, Object> attributesNoMatch = new HashMap<>();
        attributesNoMatch.put("notInHere", "notInHere");

        assertTrue("test not found in attributes", QueryUtils.match(attributes, buildPattern("test")));
        assertTrue("t* in attributes", QueryUtils.match(attributes, buildPattern("t*")));
        assertTrue("*est in attributes", QueryUtils.match(attributes, buildPattern("*est")));

        assertFalse("test found in attributes", QueryUtils.match(attributesNoMatch, buildPattern("test")));
    }

    @Test
    public void testMatchAndString() {
        // public static boolean match(final List<String> attributes, final String pattern) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "test");
        attributes.put("type", "atype");

        assertTrue("name=test found in attributes", QueryUtils.match(attributes, buildPattern("name=test")));
        assertTrue("type=atype found in attributes", QueryUtils.match(attributes, buildPattern("type=atype")));
        assertTrue("name=test found in attributes", QueryUtils.match(attributes, buildPattern(" name   =  test  ")));
        assertTrue("type=atype found in attributes", QueryUtils.match(attributes, buildPattern("   type  = atype ")));
    }

    private Element buildPattern(final String pattern) {
        ExpressionLexer lexer = new ExpressionLexer(pattern);
        Parser parser = new FilterParser(lexer);
        Element patternElement = parser.parseExpression();
        return patternElement;

    }
}
