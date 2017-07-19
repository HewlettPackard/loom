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
 * A base checked loom exception.
 */
public class CheckedLoomException extends Exception implements LoomException {
    private String description = null; // Optional

    /**
     * @param message a message
     */
    public CheckedLoomException(final String message) {
        super(message);
        description = message;
    }

    /**
     * @param message a message
     * @param description a description
     */
    public CheckedLoomException(final String message, final String description) {
        super(message);

        this.description = description;
    }

    /**
     * @param message a message
     * @param cause a cause
     */
    public CheckedLoomException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message a message
     * @param description a description
     * @param cause a cause
     */
    public CheckedLoomException(final String message, final String description, final Throwable cause) {
        super(message, cause);

        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
