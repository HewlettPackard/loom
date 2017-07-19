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
package com.hp.hpl.loom.tapestry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This class describes a pattern definition.
 *
 */
@JsonAutoDetect
public class PatternDefinition {

    private String id;
    private boolean defaultPattern;
    private List<ThreadDefinition> threads;
    private String providerType;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> metrics;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;
    @SuppressWarnings("checkstyle:membername")
    private Meta _meta;

    /**
     * The default constructor.
     */
    public PatternDefinition() {
        threads = new ArrayList<ThreadDefinition>();
        metrics = new ArrayList<String>();
    }

    /**
     * Constructor with an id.
     *
     * @param id PatternDefinition Id
     */
    public PatternDefinition(final String id) {
        this.id = id;
        threads = new ArrayList<ThreadDefinition>();
        metrics = new ArrayList<String>();
    }

    /**
     * Constructor with an id, thread definitions, provider type and meta types.
     *
     * @param id PatternDefinition Id
     * @param threads threadDefinitions
     * @param providerType providerType
     * @param types meta types
     */
    public PatternDefinition(final String id, final List<ThreadDefinition> threads, final String providerType,
            final Meta types) {
        // empty Ids are allowed
        validate(id, threads, providerType, types);

        this.id = id;
        this.threads = threads;
        this.providerType = providerType;
        this._meta = types;
        this.metrics = new ArrayList<String>();
    }

    private void validate(final String idValue, final List<ThreadDefinition> threadsValue,
            final String providerTypeValue, final Meta typesValue) {
        if (idValue == null || idValue.isEmpty() || threadsValue == null || providerTypeValue == null
                || providerTypeValue.isEmpty() || threadsValue.size() == 0 || typesValue == null
                || typesValue.getItemTypes().size() != threadsValue.size()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructor with an id, thread definitions, provider type, meta types and name.
     *
     * @param id PatternDefinition Id
     * @param threads threadDefinitions
     * @param providerType providerType
     * @param types meta types
     * @param name pattern name
     */
    public PatternDefinition(final String id, final List<ThreadDefinition> threads, final String providerType,
            final Meta types, final String name) {
        this(id, threads, providerType, types, name, null);
    }

    /**
     * Constructor with an id, thread definitions, provider type, meta types and name.
     *
     * @param id PatternDefinition Id
     * @param threads threadDefinitions
     * @param providerType providerType
     * @param types meta types
     * @param name pattern name
     * @param metrics list of metrics to be displayed by default when the pattern is loaded
     */
    public PatternDefinition(final String id, final List<ThreadDefinition> threads, final String providerType,
            final Meta types, final String name, final List<String> metrics) {
        this(id, threads, providerType, types);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.threads = threads;
        if (metrics == null) {
            this.metrics = new ArrayList<>();
        } else {
            this.metrics = metrics;
        }
    }

    /**
     * Returns true if all the threads are being replaced.
     *
     * @return true if all the threads are being replaced
     */
    @JsonIgnore
    public boolean areAllReplaced() {
        boolean allReplaced = true;
        for (ThreadDefinition threadDef : threads) {
            if (!threadDef.isReplaced()) {
                return false;
            }
        }
        return allReplaced;
    }

    /**
     * Returns true if this is the default pattern.
     *
     * @return true if this is the default pattern
     */
    public boolean isDefaultPattern() {
        return defaultPattern;
    }

    /**
     * Set the default pattern flag.
     *
     * @param defaultPattern default pattern flag.
     */
    public void setDefaultPattern(final boolean defaultPattern) {
        this.defaultPattern = defaultPattern;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the meta.
     *
     * @return the meta
     */
    @SuppressWarnings("checkstyle:methodname")
    public Meta get_meta() {
        return _meta;
    }

    /**
     * Set the meta.
     *
     * @param _meta the meta.
     */
    @SuppressWarnings({"checkstyle:methodname", "checkstyle:parametername"})
    public void set_meta(final Meta _meta) {
        if (_meta == null) {
            throw new IllegalArgumentException();
        }
        this._meta = _meta;
    }

    /**
     * Get the provider type.
     *
     * @return the provider type.
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * Set the provider type.
     *
     * @param providerType the provider type.
     */
    public void setProviderType(final String providerType) {
        if (providerType == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.providerType = providerType;
    }

    /**
     * Set the id.
     *
     * @param id the id
     */
    public void setId(final String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.id = id;
    }

    /**
     * Set the metric definition list.
     *
     * @param metrics the metric definition list.
     */
    public void setMetrics(final List<String> metrics) {
        if (metrics == null || metrics.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.threads = threads;
    }

    /**
     * Get the metrics.
     *
     * @return the list of metrics.
     */
    public List<String> getMetrics() {
        return new ArrayList(metrics);
    }


    /**
     * Set the thread definition list.
     *
     * @param threads the thread definition list.
     */
    public void setThreads(final List<ThreadDefinition> threads) {
        if (threads == null || threads.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.threads = threads;
    }

    /**
     * Get the pattern id.
     *
     * @return the pattern id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the threads.
     *
     * @return the list of threads.
     */
    public List<ThreadDefinition> getThreads() {
        return new ArrayList(threads);
    }

    /**
     * Clone the pattern definition.
     *
     * @return a clone of the pattern definition.
     */
    @JsonIgnore
    public PatternDefinition clonePatternDefinition() {
        PatternDefinition newPat = new PatternDefinition();
        newPat.setId("dyn" + id);
        newPat.setThreads(new ArrayList(threads));
        newPat.setThreads(new ArrayList(metrics));
        newPat.setProviderType(providerType);
        newPat.set_meta(_meta);
        return newPat;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("patternId-> " + id);
        str.append("; threads-> " + threads);
        str.append("; metrics-> " + metrics);
        str.append("; providerType-> " + providerType);
        str.append("; itemType-> " + _meta);
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
        PatternDefinition that = (PatternDefinition) o;
        return Objects.equals(id, that.id) && Objects.equals(threads, that.threads)
                && Objects.equals(metrics, that.metrics) && Objects.equals(providerType, that.providerType)
                && Objects.equals(_meta, that._meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, threads, metrics, providerType, _meta);
    }
}
