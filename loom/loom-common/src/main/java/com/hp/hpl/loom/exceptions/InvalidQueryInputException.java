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
package com.hp.hpl.loom.exceptions;

/**
 * Thrown when the query inputs are invalid.
 */
public class InvalidQueryInputException extends CheckedLoomException {

    /**
     * @param msg the message
     */
    public InvalidQueryInputException(final String msg) {
        super(msg);
    }

    /**
     * @param msg the message
     * @param cause the cause
     */
    public InvalidQueryInputException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
