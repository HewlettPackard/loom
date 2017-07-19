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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.loom.manager.query.filter.element.Element;

public class QueryUtils {
    // ///////////////////////////////////////////////////////////////////
    //
    // FILTER UTILS
    //
    // ///////////////////////////////////////////////////////////////////

    /**
     * Check if the value matches a value in the provided set - it is null save.
     *
     * @param value value to check
     * @param possibleValues Set to check in
     * @return true if the value is in the set
     */
    public static boolean matchToSet(final String value, final Set<String> possibleValues) {
        if (possibleValues == null || possibleValues.isEmpty()) {
            return false;
        }
        return possibleValues.contains(value);
    }

    /**
     * Checks if the pattern matchs the attributes provided.
     *
     * @param attributes attribute to check
     * @param pattern pattern to check
     * @return true of they match
     */
    public static boolean match(final Map<String, Object> attributes, final Element pattern) {

        if (attributes == null || attributes.isEmpty() || pattern == null) {
            return false;
        }

        return pattern.match(attributes);
        // List<String> patts = Arrays.asList(pattern.toLowerCase().trim().split("\\s+"));
        // List<String> atts = attributes.stream().map(s ->
        // s.toLowerCase().trim()).collect(Collectors.toList());
        // if (patts.size() == 1) {
        // Boolean match = atts.stream().anyMatch(s -> QueryUtils.wildCardMatch(s, patts));
        // return match;
        // } else {

        // return false;
        // }
    }

    /**
     * Checks if the string matches the pattern using wildcards.
     *
     * @param text String to check
     * @param patterns pattern to check
     * @return true if they match
     */
    public static boolean wildCardMatch(String text, final List<String> patterns) {

        for (String pattern : patterns) {
            String[] cards = pattern.split("\\*");

            boolean match = true;
            for (String card : cards) {
                int idx = text.indexOf(card);

                if (idx == -1) {
                    match = false;
                    break;
                }
                // Move ahead, towards the right of the text.
                text = text.substring(idx + card.length());
            }
            if (match) {
                return match;
            }
        }

        return false;
    }

    public static String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }
}
