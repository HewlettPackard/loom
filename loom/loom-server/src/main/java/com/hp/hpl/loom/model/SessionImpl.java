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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session Implementation.
 */
public class SessionImpl implements Session {
    private String id;
    private long creationTime;
    private int maxInactiveInterval;
    private long lastAccessedTime;
    private Map<Provider, Boolean> providers;

    /**
     * Create a new Session with the given ID and maximum inactivity period before the Session is
     * deemed to have expired.
     *
     * @param id unique Session ID.
     * @param maxInactivityPeriod maximum period of inactivity after which the Session is deemed to
     *        have expired.
     */
    public SessionImpl(final String id, final int maxInactivityPeriod) {
        setId(id);

        long currentTime = System.currentTimeMillis();

        setCreationTime(currentTime);
        setLastAccessedTime(currentTime);

        maxInactiveInterval = maxInactivityPeriod;
        providers = new ConcurrentHashMap<Provider, Boolean>();
    }

    @Override
    public Map<Provider, Boolean> getProviders() {
        return providers;
    }

    @Override
    public void setProviders(final Map<Provider, Boolean> providers) {
        if (providers == null) {
            throw new IllegalArgumentException("Providers not specified");
        }

        this.providers = providers;
    }

    @Override
    public void removeProvider(final Provider provider) {
        validateProvider(provider);
        providers.remove(provider);
    }

    @Override
    public void addProvider(final Provider provider, final Boolean requireReAuth) {
        validateProvider(provider);
        providers.put(provider, requireReAuth);
    }

    @Override
    public void setReAuthenticate(final Provider provider, final Boolean requireReAuth) {
        validateProvider(provider);
        providers.put(provider, requireReAuth);
    }

    private void validateProvider(final Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider not specified");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    final void setId(final String id) {
        this.id = id;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    final void setCreationTime(final long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    void setMaxInactiveInterval(final int interval) {
        maxInactiveInterval = interval;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public void setLastAccessedTime(final long access) {
        lastAccessedTime = access;
    }

    @Override
    public boolean isExpired() {
        if (System.currentTimeMillis() - lastAccessedTime > maxInactiveInterval) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id-> " + id);
        str.append("; creationTime -> " + creationTime);
        str.append("; interval -> " + maxInactiveInterval);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionImpl that = (SessionImpl) o;

        return Objects.equals(id, that.id) && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(maxInactiveInterval, that.maxInactiveInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationTime, maxInactiveInterval);
    }
}
