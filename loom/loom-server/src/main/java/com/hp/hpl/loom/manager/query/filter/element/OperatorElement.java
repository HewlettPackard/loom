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
package com.hp.hpl.loom.manager.query.filter.element;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hp.hpl.loom.manager.query.filter.parser.ParseException;
import com.hp.hpl.loom.manager.query.filter.parser.TokenType;


/**
 * A binary arithmetic expression like "a and b" or "c or d" or "a = d".
 */
public class OperatorElement implements Element {
    private final Element left;
    private final TokenType operator;
    private final Element right;

    /**
     * The operator element which takes a left, right and operator.
     *
     * @param left the left hand side element
     * @param operator the operator
     * @param right the right hand side element
     */
    public OperatorElement(final Element left, final TokenType operator, final Element right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void buildExpression(final StringBuilder builder) {
        builder.append("(");
        left.buildExpression(builder);
        builder.append(" ").append(operator.punctuator()).append(" ");
        right.buildExpression(builder);
        builder.append(")");
    }

    /**
     * Provides support for the orginial wild card matching support.
     *
     * @param patts the patterns to match on
     * @param attributes the attributes to match against
     * @return true if a pattern matches a attribute
     */
    public boolean matchWild(final List<String> patts, final Collection<Object> attributes) {
        List<String> atts =
                attributes.stream().map(s -> s.toString().toLowerCase().trim()).collect(Collectors.toList());
        Boolean match = atts.stream().anyMatch(s -> wildCardMatch(s, patts));
        return match;
    }

    /**
     * Checks if the string matches the pattern using wildcards.
     *
     * @param text String to check
     * @param patterns pattern to check
     * @return true if they match
     */
    private boolean wildCardMatch(final String text, final List<String> patterns) {

        for (String pattern : patterns) {
            boolean match = true;
            match = text.matches(pattern);
            if (match) {
                return match;
            }
        }
        return false;
    }


    private boolean isStringEquals(final String value, final String input) {
        // if (input.contains(".*?")) {
        return value.matches(input);
        // } else {
        // return value.equals(input);
        // }
    }


    /**
     * The main matching code, it checks the left & right side plus the operator and determines how
     * do perform the check.
     *
     * @param values the values to match on
     * @return true if there is a match
     */
    @Override
    public boolean match(final Map<String, Object> values) {

        // if we have the equals token then compare left with right and check they are the same
        if (operator.equals(TokenType.EQUALS) || operator.equals(TokenType.NOT_EQUALS)) {
            NameElement propertyName = (NameElement) left;
            NameElement propertyValue = (NameElement) right;
            // special treatment for the l.any field (it searches everything)
            if (propertyName.getName().equals("l.any")) {
                List<String> patts = Arrays.asList(propertyValue.getName().toLowerCase().trim().split("\\s+"));
                if (operator.equals(TokenType.EQUALS)) {
                    return matchWild(patts, values.values());
                } else {
                    return !matchWild(patts, values.values());
                }
            }

            boolean result = false;
            if (values.containsKey(propertyName.getName())) {
                Object val = values.get(propertyName.getName());
                try {
                    if (val instanceof Integer) {
                        Integer number = (Integer) val;
                        Integer number2 = Integer.parseInt(propertyValue.getName());

                        if (operator.equals(TokenType.EQUALS) && number == number2) {
                            result = true;
                        } else if (operator.equals(TokenType.NOT_EQUALS) && number != number2) {
                            result = true;
                        }
                    } else if (val instanceof Double) {
                        Double number = (Double) val;
                        Double number2 = Double.parseDouble(propertyValue.getName());

                        if (operator.equals(TokenType.EQUALS) && number.doubleValue() == number2.doubleValue()) {
                            result = true;
                        } else if (operator.equals(TokenType.NOT_EQUALS)
                                && (number.doubleValue() != number2.doubleValue())) {
                            result = true;
                        }
                    } else if (val instanceof Float) {
                        Float number = (Float) val;
                        Float number2 = Float.parseFloat(propertyValue.getName());

                        if (operator.equals(TokenType.EQUALS) && number.floatValue() == number2.floatValue()) {
                            result = true;
                        } else if (operator.equals(TokenType.NOT_EQUALS)
                                && (number.floatValue() != number2.floatValue())) {
                            result = true;
                        }
                    } else if (operator.equals(TokenType.EQUALS)
                            && isStringEquals(val.toString(), propertyValue.getName())) {
                        result = true;
                    } else if (operator.equals(TokenType.NOT_EQUALS)
                            && !isStringEquals(val.toString(), propertyValue.getName())) {
                        result = true;
                    }
                } catch (NumberFormatException ex) {
                    throw new ParseException("Problem parsing number input: " + propertyValue.getName(), ex);
                }
            }
            return result;
        } else if (operator.equals(TokenType.GREATER) || operator.equals(TokenType.LESSER)) {
            NameElement propertyName = (NameElement) left;
            NameElement propertyValue = (NameElement) right;
            Double.parseDouble(propertyValue.getName());
            boolean result = false;
            if (values.containsKey(propertyName.getName())) {
                Object val = values.get(propertyName.getName());
                if (val instanceof Integer) {
                    Integer number = (Integer) val;
                    Integer number2 = Integer.parseInt(propertyValue.getName());

                    if (operator.equals(TokenType.GREATER) && number > number2) {
                        result = true;
                    } else if (operator.equals(TokenType.LESSER) && number < number2) {
                        result = true;
                    }
                } else if (val instanceof Long) {
                    Long number = (Long) val;
                    Long number2 = Long.parseLong(propertyValue.getName());

                    if (operator.equals(TokenType.GREATER) && number > number2) {
                        result = true;
                    } else if (operator.equals(TokenType.LESSER) && number < number2) {
                        result = true;
                    }
                } else if (val instanceof Double) {
                    Double number = (Double) val;
                    Double number2 = Double.parseDouble(propertyValue.getName());

                    if (operator.equals(TokenType.GREATER) && number.doubleValue() > number2.doubleValue()) {
                        result = true;
                    } else if (operator.equals(TokenType.LESSER) && (number.doubleValue() < number2.doubleValue())) {
                        result = true;
                    }
                } else if (val instanceof Float) {
                    Float number = (Float) val;
                    Float number2 = Float.parseFloat(propertyValue.getName());

                    if (operator.equals(TokenType.GREATER) && number.floatValue() > number2.floatValue()) {
                        result = true;
                    } else if (operator.equals(TokenType.LESSER) && (number.floatValue() < number2.floatValue())) {
                        result = true;
                    }
                }
            }
            return result;
        } else if (operator.equals(TokenType.OR)) {
            return left.match(values) || right.match(values);
        } else if (operator.equals(TokenType.AND)) {
            return left.match(values) && right.match(values);
        }
        throw new IllegalStateException();
    }
}
