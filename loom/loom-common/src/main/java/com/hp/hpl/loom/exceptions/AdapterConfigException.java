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
 * Exception to handle problems with the provided adapter config.
 *
 */
public class AdapterConfigException extends CheckedLoomException {

    /**
     * Construct an exception based on a message only.
     *
     * @param message a message
     */
    public AdapterConfigException(final String message) {
        super(message);
    }

    /**
     * Construct a exception based on a message and causing exception.
     *
     * @param message a message
     * @param cause a cause.
     */
    public AdapterConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
