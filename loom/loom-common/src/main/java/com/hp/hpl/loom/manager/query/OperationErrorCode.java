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

/**
 * Enum type of operation error codes.
 */
public enum OperationErrorCode {
    /**
     * Field is null.
     */
    NullField("WARN_NullField"),
    /**
     * Field doesn't exist.
     */
    NonExistentField("ERROR_NonExistentField"),
    /**
     * Empty input.
     */
    EmptyInput("WARN_EmptyInput"),
    /**
     * Field not readable.
     */
    NotReadableField("WARN_NotReadableField"),
    /**
     * Can't read null field.
     */
    ReadNullObject("WARN_ReadANullObject"),
    /**
     * Can't read null object.
     */
    UnsupportedOperationParameter("ERROR_UnsupportedOperationParameter"),
    /**
     * Unsupported operation parameter.
     */
    NoSuchSession("ERROR_NoSuchSession"),
    /**
     * No such session.
     */
    NoSuchFibre("ERROR_NoSuchFibre"),
    /**
     * No such fibre.
     */
    NullParam("ERROR_NullParamProvidedByClient"),
    /**
     * Null param provided by client.
     */
    ReplacedMissingParameterWithDefault("WARN_ReplacedMissingParameterWithDefault");

    private final String name;

    private OperationErrorCode(final String s) {
        name = s;
    }

    /**
     * Compares the operation error code names.
     *
     * @param otherName name to compare with
     * @return true is the same.
     */
    public boolean equalsName(final String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    @Override
    public String toString() {
        return name;
    }
}
