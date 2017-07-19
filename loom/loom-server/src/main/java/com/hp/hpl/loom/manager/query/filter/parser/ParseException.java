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

/**
 * Parsing exception.
 */
@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
    /**
     * Basic exception constructor - based off a message.
     *
     * @param message message string
     */
    public ParseException(final String message) {
        super(message);
    }

    /**
     * Exception constructor off a message and exception.
     *
     * @param message message string
     * @param ex exception
     */
    public ParseException(final String message, final Exception ex) {
        super(message, ex);
    }
}
