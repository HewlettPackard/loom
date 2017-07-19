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
package com.hp.hpl.loom.manager.tapestry;

import java.util.Collection;

import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.tapestry.PatternDefinition;

/**
 * All supported Pattern management operations. A Pattern contains a number of ThreadDefinitions and
 * may be associated with a single Provider type, although multiple Providers of the same type may
 * register the exact same PatternDefinition. A Provider may register multiple Patterns and may
 * optionally nominate one of those Patterns as the default. It is important that a Pattern is only
 * marked as default <b>after</b> it has been registered.
 */
public interface PatternManagementInternal {


    /**
     * Retrieve all the Patterns registered by the Provider.
     *
     * @param provider the Provider that registered the Patterns.
     * @return The Patterns associated with the Provider.
     * @throws NoSuchProviderException the Provider does not exist.
     */
    Collection<PatternDefinition> getPatterns(Provider provider) throws NoSuchProviderException;

    /**
     * Retrieve all Patterns registered by all Providers.
     *
     * @return All known Patterns.
     */
    Collection<PatternDefinition> getAllPatterns();

    /**
     * Retrieve all Patterns registered by Providers of a given type.
     *
     * @param providerType the type of Provider.
     * @return All Patterns registered by the given Provider type.
     */
    Collection<PatternDefinition> getPatternsForProviderType(String providerType);

    /**
     * Retrieve the default Pattern if there is one.
     *
     * @param provider the Provider that registered the Patterns.
     * @return The default Pattern if there is one, null if there is no default.
     * @throws NoSuchProviderException the Provider does not exist.
     */
    PatternDefinition getDefaultPatternDefinition(Provider provider) throws NoSuchProviderException;

    /**
     * Record the default Pattern for a given Provider. If a default already exists then the
     * provided Patterns supersedes it. <b>It is important that a Pattern is only marked as default
     * after it has been registered</b>.
     *
     * @param provider the Provider that registered the Patterns.
     * @param newDefault the new default Pattern.
     * @return The old default Pattern if there was one, null if there wasn't a default.
     * @throws NoSuchProviderException the Provider does not exist.
     * @throws NoSuchPatternException the given Pattern has not been previously registered by the
     *         Provider.
     */
    PatternDefinition setDefaultPatternDefinition(Provider provider, PatternDefinition newDefault)
            throws NoSuchProviderException, NoSuchPatternException;

    /**
     * Retrieve a Pattern by its identifier.
     *
     * @param patternId the identifier of the Pattern.
     * @return The Pattern associated with the given identifier.
     * @throws NoSuchPatternException the Pattern does not exist.
     */
    PatternDefinition getPattern(String patternId) throws NoSuchPatternException;
}
