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
 * Thrown when the pattern isn't found.
 */
public class NoSuchPatternException extends CheckedLoomException {
    private String patternId;

    /**
     * @param patternId the patternId
     */
    public NoSuchPatternException(final String patternId) {
        super("Pattern " + patternId + " does not exist");
        this.patternId = patternId;
    }

    /**
     * @param patternId the patternId
     * @param cause the cause
     */
    public NoSuchPatternException(final String patternId, final Throwable cause) {
        super("Pattern " + patternId + " does not exist", cause);
        this.patternId = patternId;
    }

    /**
     * Get the patternId.
     *
     * @return the patternId
     */
    public String getPatternId() {
        return patternId;
    }
}
