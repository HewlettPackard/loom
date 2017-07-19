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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.manager.query.filter.element.Element;
import com.hp.hpl.loom.manager.query.filter.parser.ExpressionLexer;
import com.hp.hpl.loom.manager.query.filter.parser.Parser;

public class FilterParserTest {

    @Before
    public void setUp() {}

    @Test
    public void testEquals() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("age", 25);
        properties.put("name", "james");
        properties.put("cash", 25.234f);
        assertTrue("Age does match", test("age = 25", "(age = 25)", properties));
        assertFalse("Age doesn't match", test("age = 30", "(age = 30)", properties));
        assertTrue("Name does match", test("name = james", "(name = james)", properties));
        assertFalse("Name doesn't match", test("name = sam", "(name = sam)", properties));
        assertTrue("Cash does match", test("cash = 25.234", "(cash = 25.234)", properties));
        assertFalse("Cash doesn't match", test("cash = 30", "(cash = 30)", properties));

        assertTrue("Age does match", test("age=25", "(age = 25)", properties));
        assertFalse("Age doesn't match", test("age=30", "(age = 30)", properties));
        assertTrue("Name does match", test("name=james", "(name = james)", properties));
        assertFalse("Name doesn't match", test("name=sam", "(name = sam)", properties));
        assertTrue("Cash does match", test("cash=25.234", "(cash = 25.234)", properties));
        assertFalse("Cash doesn't match", test("cash=30", "(cash = 30)", properties));

    }

    @Test
    public void testNotEquals() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("age", 25);
        properties.put("name", "james");
        properties.put("cash", 25.234f);
        assertTrue("Age doesn't match", test("age != 30", "(age != 30)", properties));
        assertFalse("Age doesn't match", test("age != 25", "(age != 25)", properties));
        assertFalse("Name doesn't match", test("name != james", "(name != james)", properties));
        assertTrue("Name does match", test("name != sam", "(name != sam)", properties));
        assertTrue("Cash doesn't match", test("cash != 30", "(cash != 30)", properties));
        assertFalse("Cash does match", test("cash != 25.234", "(cash != 25.234)", properties));

        assertTrue("Age doesn't match", test("age!=30", "(age != 30)", properties));
        assertFalse("Age doesn't match", test("age!=25", "(age != 25)", properties));
        assertTrue("Cash doesn't match", test("cash!=30", "(cash != 30)", properties));
        assertFalse("Cash does match", test("cash!=25.234", "(cash != 25.234)", properties));
        assertFalse("Name doesn't match", test("name!=james", "(name != james)", properties));
        assertTrue("Name does match", test("name!=sam", "(name != sam)", properties));
    }

    @Test
    public void testEqualsTypeSupport() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("age", 25);
        properties.put("money", 25.12f);
        properties.put("pi", 3.14159265359);
        assertTrue("Age matches and just an integer", test("age = 25", "(age = 25)", properties));
        assertTrue("Money matches and is a float", test("money = 25.12", "(money = 25.12)", properties));
        assertTrue("Pi matches and is a double", test("pi = 3.14159265359", "(pi = 3.14159265359)", properties));

        assertTrue("Age matches and just an integer", test("age=25", "(age = 25)", properties));
        assertTrue("Money matches and is a float", test("money=25.12", "(money = 25.12)", properties));
        assertTrue("Pi matches and is a double", test("pi=3.14159265359", "(pi = 3.14159265359)", properties));
    }

    @Test
    public void testOrLogic() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "james");
        properties.put("age", 25);
        assertTrue("Both match", test("name = james or age = 25", "((name = james) or (age = 25))", properties));
        assertTrue("Name matches", test("name = james or age = 26", "((name = james) or (age = 26))", properties));
        assertFalse("Neither age or name match",
                test("name = jamesX or age = 26", "((name = jamesX) or (age = 26))", properties));
        assertTrue("Age matches", test("name = jamesX or age = 25", "((name = jamesX) or (age = 25))", properties));

        assertTrue("Both match", test("name=james or age=25", "((name = james) or (age = 25))", properties));
        assertTrue("Name matches", test("name=james or age=26", "((name = james) or (age = 26))", properties));
        assertFalse("Neither age or name match",
                test("name=jamesX or age=26", "((name = jamesX) or (age = 26))", properties));
        assertTrue("Age matches", test("name=jamesX or age=25", "((name = jamesX) or (age = 25))", properties));
    }

    @Test
    public void testAndLogic2() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("givenName", "james");
        properties.put("surname", "brook");
        assertTrue("Both match", test("givenName = james and surname = brook",
                "((givenName = james) and (surname = brook))", properties));
        assertFalse("Both match",
                test("givenName=james and surname=test", "((givenName = james) and (surname = test))", properties));
    }


    @Test
    public void testAndLogic() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "james");
        properties.put("age", 25);
        assertTrue("Both name and age match",
                test("name = james and age = 25", "((name = james) and (age = 25))", properties));
        assertFalse("Only age should match",
                test("name = jamesX and age = 25", "((name = jamesX) and (age = 25))", properties));
        assertFalse("Neither name or age should match",
                test("name = jamesX and age = 18", "((name = jamesX) and (age = 18))", properties));
        assertFalse("Only name should match",
                test("name = james and age = 18", "((name = james) and (age = 18))", properties));

        assertTrue("Both name and age match",
                test("name=james and age=25", "((name = james) and (age = 25))", properties));
        assertFalse("Only age should match",
                test("name=jamesX and age=25", "((name = jamesX) and (age = 25))", properties));
        assertFalse("Neither name or age should match",
                test("name=jamesX and age=18", "((name = jamesX) and (age = 18))", properties));
        assertFalse("Only name should match",
                test("name=james and age=18", "((name = james) and (age = 18))", properties));

    }


    @Test
    public void testLesserThan() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("age", 25);
        properties.put("cash", 19.99f);
        properties.put("wage", 120.123);
        assertFalse("age less < 20 test failed", test("age < 20", "(age < 20)", properties));
        assertTrue("age less < 30 test failed", test("age < 30", "(age < 30)", properties));
        assertFalse("cash less < 19.26 test failed", test("cash < 19.26", "(cash < 19.26)", properties));
        assertTrue("cash less < 30 test failed", test("cash < 30", "(cash < 30)", properties));
        assertFalse("wage less < 100.123123 test failed", test("wage < 100.123123", "(wage < 100.123123)", properties));
        assertTrue("wage less < 150.345345 test failed", test("wage < 150.345345", "(wage < 150.345345)", properties));


        assertFalse("age less < 20 test failed", test("age<20", "(age < 20)", properties));
        assertTrue("age less < 30 test failed", test("age<30", "(age < 30)", properties));
        assertFalse("cash less < 19.26 test failed", test("cash<19.26", "(cash < 19.26)", properties));
        assertTrue("cash less < 30 test failed", test("cash<30", "(cash < 30)", properties));
        assertFalse("wage less < 100.123123 test failed", test("wage<100.123123", "(wage < 100.123123)", properties));
        assertTrue("wage less < 150.345345 test failed", test("wage<150.345345", "(wage < 150.345345)", properties));

    }

    @Test
    public void testGreaterThan() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("age", 25);
        properties.put("cash", 19.99f);
        properties.put("wage", 120.123);
        assertTrue("age greater > 20 test failed", test("age > 20", "(age > 20)", properties));
        assertFalse("age greater > 30 test failed", test("age > 30", "(age > 30)", properties));
        assertTrue("cash greater > 19.26 test failed", test("cash > 19.26", "(cash > 19.26)", properties));
        assertFalse("cash greater > 30 test failed", test("cash > 30", "(cash > 30)", properties));
        assertTrue("wage greater > 100.123123 test failed",
                test("wage > 100.123123", "(wage > 100.123123)", properties));
        assertFalse("wage greater > 150.345345 test failed",
                test("wage > 150.345345", "(wage > 150.345345)", properties));

        assertTrue("age greater > 20 test failed", test("age>20", "(age > 20)", properties));
        assertFalse("age greater > 30 test failed", test("age>30", "(age > 30)", properties));
        assertTrue("cash greater > 19.26 test failed", test("cash>19.26", "(cash > 19.26)", properties));
        assertFalse("cash greater > 30 test failed", test("cash>30", "(cash > 30)", properties));
        assertTrue("wage greater > 100.123123 test failed", test("wage>100.123123", "(wage > 100.123123)", properties));
        assertFalse("wage greater > 150.345345 test failed",
                test("wage>150.345345", "(wage > 150.345345)", properties));

    }

    @Test
    public void testQuotedString() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        assertFalse("double quote test failed", test("name = \"james brook\"", "(name = james brook)", properties));

        properties.put("name", "james brook");
        assertTrue("double quote test failed", test("name = \"james brook\"", "(name = james brook)", properties));

        properties.remove("name");
        assertFalse("double quote test failed", test("name=\"james brook\"", "(name = james brook)", properties));

        properties.put("name", "james brook");
        assertTrue("double quote test failed", test("name=\"james brook\"", "(name = james brook)", properties));
    }


    private boolean test(final String source, final String expected, final Map<String, Object> properties) {
        ExpressionLexer lexer = new ExpressionLexer(source);
        Parser parser = new FilterParser(lexer);
        boolean expressionResult = false;
        Element result = parser.parseExpression();
        expressionResult = result.match(properties);
        System.out.println("Match --> " + expressionResult);

        StringBuilder builder = new StringBuilder();
        result.buildExpression(builder);
        String actual = builder.toString();

        assertEquals(expected, actual);

        return expressionResult;
    }

    private void test(final String source, final String expected) {
        ExpressionLexer lexer = new ExpressionLexer(source);
        Parser parser = new FilterParser(lexer);

        Element result = parser.parseExpression();
        StringBuilder builder = new StringBuilder();
        result.buildExpression(builder);
        String actual = builder.toString();

        assertEquals(expected, actual);

    }

}
