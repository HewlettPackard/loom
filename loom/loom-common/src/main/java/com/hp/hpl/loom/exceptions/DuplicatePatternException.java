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
 * Thrown when attempting to register a duplicate pattern.
 */
public class DuplicatePatternException extends CheckedLoomException {
    private String patternId;

    /**
     * @param patternId the pattern id
     */
    public DuplicatePatternException(final String patternId) {
        super("Pattern# " + patternId + " already exists");

        this.patternId = patternId;
    }

    /**
     * @param patternId the pattern id
     * @param cause the cause
     */
    public DuplicatePatternException(final String patternId, final Throwable cause) {
        super("Pattern# " + patternId + " already exists", cause);

        this.patternId = patternId;
    }

    /**
     * The pattern id.
     *
     * @return the pattern id
     */
    public String getPatternId() {
        return patternId;
    }
}
