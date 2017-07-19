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

import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NullPatternIdException;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.tapestry.PatternDefinition;

/**
 * All supported Pattern management operations. A Pattern contains a number of ThreadDefinitions and
 * may be associated with a single Provider type, although multiple Providers of the same type may
 * register the exact same PatternDefinition. A Provider may register multiple Patterns and may
 * optionally nominate one of those Patterns as the default. It is important that a Pattern is only
 * marked as default <b>after</b> it has been registered.
 */
public interface PatternManagement {
    /**
     * If the Pattern is marked as default then any existing Pattern associated with the specified
     * Provider with default status will have that status revoked and the specified Pattern will
     * become the new default Pattern. The ID of the Pattern must be pre-created before calling this
     * method.
     *
     * @param provider the Provider to register the Pattern against.
     * @param pattern the Pattern to register.
     * @return The (given) identifier of the registered Pattern.
     * @throws DuplicatePatternException this Pattern has either already been registered by the
     *         Provider or has been registered by a Provider of a different type.
     * @throws NoSuchProviderException the Provider does not exist.
     * @throws NullPatternIdException thrown if the pattern id is null
     */
    String addPatternDefinition(Provider provider, PatternDefinition pattern)
            throws DuplicatePatternException, NoSuchProviderException, NullPatternIdException;

    /**
     * Adds a global pattern that will be available to all providers.
     *
     * @param pattern the pattern to add
     * @return The (given) identifier of the registered Pattern.
     * @throws DuplicatePatternException this Pattern has either already been registered
     * @throws NullPatternIdException thrown if the pattern id is null
     */
    String addGlobalDefinition(PatternDefinition pattern) throws DuplicatePatternException, NullPatternIdException;

    /**
     * If exactly one Pattern is marked as default then any existing Pattern associated with the
     * specified Provider with default status will have that status revoked and the specified
     * Pattern will become the new default Pattern. If two or more of the specified Patterns are
     * marked as default an IllegalArgumentException will be thrown. The ID of the Pattern must be
     * pre-created before calling this method.
     *
     * @param provider the Provider to register the Pattern against.
     * @param patterns the Patterns to register.
     * @return The (given) identifiers of the registered Patterns.
     * @throws DuplicatePatternException one or more Patterns have either already been registered by
     *         the Provider or have been registered by a Provider of a different type.
     * @throws NoSuchProviderException the Provider does not exist.
     * @throws NullPatternIdException thrown if the pattern id is null
     */
    Collection<String> addPatternDefinitions(Provider provider, Collection<PatternDefinition> patterns)
            throws DuplicatePatternException, NoSuchProviderException, NullPatternIdException;

    /**
     * Remove the given Pattern previously registered by the given Provider. Note that the actual
     * PatternDefinition will only be removed when the last Provider that registered it makes this
     * call. Prior to that only the link between the PatternDefinition and the specified Provider
     * will be removed.
     *
     * @param provider the Provider that registered the Pattern.
     * @param pattern the Pattern to remove.
     * @return The identifier of the successfully removed Pattern.
     * @throws NoSuchProviderException the Provider does not exist.
     * @throws NoSuchPatternException the Pattern has not been previously registered by the
     *         Provider.
     */
    String removePatternDefinition(Provider provider, PatternDefinition pattern)
            throws NoSuchProviderException, NoSuchPatternException;

    /**
     * Remove all Patterns previously registered by the given Provider.
     *
     * @param provider the Provider that registered the Patterns.
     * @return The identifiers of the successfully removed Pattern.
     * @throws NoSuchProviderException the Provider does not exist.
     * @throws NoSuchPatternException one or more Patterns have not been previously registered by
     *         the Provider.
     */
    Collection<String> removePatternDefinitions(Provider provider)
            throws NoSuchProviderException, NoSuchPatternException;
}
