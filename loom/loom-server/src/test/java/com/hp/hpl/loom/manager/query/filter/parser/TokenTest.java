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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.loom.manager.query.filter.FilterParser;
import com.hp.hpl.loom.manager.query.filter.element.Element;

public class TokenTest {
    @Test
    public void testBasic() {
        Token token = new Token(TokenType.NAME, "test");
        Assert.assertEquals("test", token.getText());
    }

    @Test
    public void testStar() {
        Token token = new Token(TokenType.NAME, "test*");
        Assert.assertEquals("^test.*$", token.getText());
    }

    @Test
    public void testTwoStar() {
        Token token = new Token(TokenType.NAME, "*test*");
        Assert.assertEquals("^.*test.*$", token.getText());
    }

    @Test
    public void testDot() {
        Token token = new Token(TokenType.NAME, "test.");
        Assert.assertEquals("test.", token.getText());
    }

    @Test
    public void testTwoDot() {
        Token token = new Token(TokenType.NAME, ".test.");
        Assert.assertEquals(".test.", token.getText());
    }

    @Test
    public void testWildcard() {
        String name = "loom.log";
        Token token = new Token(TokenType.NAME, "loom.log");
        Assert.assertTrue(name.matches(token.getText()));

        token = new Token(TokenType.NAME, "*.log");
        Assert.assertTrue(name.matches(token.getText()));

        token = new Token(TokenType.NAME, "*.2log");
        Assert.assertFalse(name.matches(token.getText()));

        token = new Token(TokenType.NAME, "*");
        Assert.assertTrue(name.matches(token.getText()));

        token = new Token(TokenType.NAME, "*.*");
        Assert.assertTrue(name.matches(token.getText()));
    }

    @Test
    public void testWildcard2() {
        ExpressionLexer lexer = new ExpressionLexer("filename=*.log");
        Parser parser = new FilterParser(lexer);
        Element patternElement = parser.parseExpression();
        Map<String, Object> pair = new HashMap<>();
        pair.put("filename", "loom.log");
        Assert.assertTrue(patternElement.match(pair));
    }

    @Test
    public void testSinglePattern() {
        ExpressionLexer lexer = new ExpressionLexer("*vm-*2*");
        Parser parser = new FilterParser(lexer);
        Element patternElement = parser.parseExpression();

        Map<String, Object> pair = new HashMap<>();
        pair.put("name", "testvm-1234");
        Assert.assertTrue(patternElement.match(pair));
    }

    @Test
    public void test() {
        Map<String, Object> pair = new HashMap<>();
        pair.put("item", "rack/0/enclosure/0/node/0/zbridge/0");

        ExpressionLexer lexer = new ExpressionLexer("enclosure/0/node/0");
        Parser parser = new FilterParser(lexer);
        Element patternElement = parser.parseExpression();
        Assert.assertFalse(patternElement.match(pair));

        lexer = new ExpressionLexer("*enclosure/0/node/0*");
        parser = new FilterParser(lexer);
        patternElement = parser.parseExpression();
        Assert.assertTrue(patternElement.match(pair));
    }


}
