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
package com.hp.hpl.loom.model;

import java.util.Map;

/**
 * A Session is created for each client and is used index data structures specific to that client. A
 * Session can be set to expire after a given period of inactivity. Note: last access time is not
 * taken into consideration when determining equality.
 */
public interface Session {
    /**
     * Retrieve the ID assigned during construction.
     *
     * @return the provided ID.
     */
    String getId();

    /**
     * Retrieve the time at which this Session was created.
     *
     * @return Creation time.
     */
    long getCreationTime();

    /**
     * Retrieve the maximum period which this Session may be inactive - as provided during
     * construction.
     *
     * @return The maximum interval.
     */
    int getMaxInactiveInterval();

    /**
     * Retrieve the last time an API call was made using this Session.
     *
     * @return The last access time.
     */
    long getLastAccessedTime();

    /**
     * Update the record of the last time this Session was accessed - intended to be used whenever
     * an API call is received.
     *
     * @param access The access timestamp.
     */
    void setLastAccessedTime(long access);

    /**
     * Determines whether the Session has expired due to lack of activity greater than
     * maxInactiveInterval.
     *
     * @return expiration status.
     */
    boolean isExpired();

    /**
     * Retrieve the Providers being used in this Session.
     *
     * @return The Providers used by this Session.
     */
    Map<Provider, Boolean> getProviders();

    /**
     * Record/replace the set of Providers associated with this session.
     *
     * @param providers the set of Providers.
     */
    void setProviders(Map<Provider, Boolean> providers);

    /**
     * Disassociates the given Provider from those associated with this Session.
     *
     * @param provider the Provider to remove.
     */
    void removeProvider(Provider provider);

    /**
     * Record that the given Provider is associated with this Session.
     *
     * @param provider the Provider to add.
     * @param requireReAuth a boolean to identify if reauthentication is required.
     */
    void addProvider(Provider provider, Boolean requireReAuth);

    /**
     * Set a flag to identify that reauthentication against a provider is required or not.
     *
     * @param provider the Provider to be flagged.
     * @param requireReAuth a boolean to identify if reauthentication is required.
     */
    void setReAuthenticate(Provider provider, Boolean requireReAuth);
}
