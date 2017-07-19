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
 * Thrown if there is an attempt to register the same adapter twice.
 */
public class DuplicateAdapterException extends CheckedLoomException {
    private String provider;

    /**
     * @param provider the adapter's provider string
     */
    public DuplicateAdapterException(final String provider) {
        super("Provider " + provider + " already exists");
        this.provider = provider;
    }

    /**
     * @param provider the adapter's provider string
     * @param cause the cause
     */
    public DuplicateAdapterException(final String provider, final Throwable cause) {
        super("Provider " + provider + " already exists", cause);
        this.provider = provider;
    }

    /**
     * @param provider the adapter's provider string
     * @param msg the message
     */
    public DuplicateAdapterException(final String provider, final String msg) {
        super("Provider " + provider + " already exists. " + msg);
        this.provider = provider;
    }

    /**
     * @param provider the adapter's provider string
     * @param msg the message
     * @param cause the cause
     */
    public DuplicateAdapterException(final String provider, final String msg, final Throwable cause) {
        super("Provider " + provider + " already exists. " + msg, cause);
        this.provider = provider;

    }

    /**
     * Get the adapter's provider string info.
     *
     * @return the adapter's provider string
     */
    public String getProviderInfo() {
        return provider;
    }
}
