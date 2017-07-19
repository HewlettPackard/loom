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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.manager.query.QueryManager;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

/**
 * Patterns can be registered by different Providers but only the ProviderType is respected when
 * storing their definitions. An attempt by the same Provider to register the same Pattern will
 * result in an exception, registration of a Pattern with the same ID by different Providers will
 * succeed but any previous definitions will be overwritten.
 */
@Component
public class TapestryManagerImpl implements TapestryManager {
    private static final int INITIAL_PATTERNS_SIZE = 5;
    private static final int INITIAL_PROVIDERS_SIZE = 1;
    private static final int INITIAL_TAPESTRIES_SIZE = 1;

    private Map<String, PatternDefinition> patterns = new HashMap<>(INITIAL_PATTERNS_SIZE);
    private Map<Provider, List<String>> providerPatterns = new HashMap<>(INITIAL_PROVIDERS_SIZE);
    private Map<String, PatternDefinition> globalPatterns = new HashMap<>(INITIAL_PROVIDERS_SIZE);
    private Map<Session, TapestryDefinition> sessionTapestries = new HashMap<>(INITIAL_TAPESTRIES_SIZE);

    @Autowired
    private AggregationManager aggregationManager;

    @Autowired
    private QueryManager queryManager;

    @Override
    public String addGlobalDefinition(final PatternDefinition pattern) throws DuplicatePatternException {

        String id = pattern.getId();

        if (id == null) {
            // User-defined pattern
            // Allocate ID for pattern
            // id = ???;
            // pattern.setId(id);

            throw new UnsupportedOperationException(
                    "Support for user-defined Patterns (with null IDs) not implemented");
        }
        globalPatterns.put(id, pattern);
        return id;
    }

    @Override
    public String addPatternDefinition(final Provider provider, final PatternDefinition pattern)
            throws DuplicatePatternException, NoSuchProviderException {
        validateProvider(provider);
        validatePattern(provider, pattern);

        String id = pattern.getId();

        if (id == null) {
            // User-defined pattern
            // Allocate ID for pattern
            // id = ???;
            // pattern.setId(id);

            throw new UnsupportedOperationException(
                    "Support for user-defined Patterns (with null IDs) not implemented");
        }

        if (patterns.get(id) != null && (hasProviderRegisteredPattern(provider, id)
                || patternRegisteredByDifferentProviderType(id, provider.getProviderType()))) {
            throw new DuplicatePatternException(id);
        }

        // If the provided Pattern is marked as default, make sure we unset
        // any existing default Pattern for the Provider (if the Provider is
        // known to us).

        if (pattern.isDefaultPattern() && providerPatterns.get(provider) != null) {
            PatternDefinition defaultPattern = getDefaultPatternDefinition(provider);

            if (defaultPattern != null) {
                defaultPattern.setDefaultPattern(false);
            }
        }

        patterns.put(id, pattern);
        registerPatternWithProvider(id, provider);

        return id;
    }

    private void validateProvider(final Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider not specified");
        }
    }

    private void validatePattern(final Provider provider, final PatternDefinition pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern not specified (Provider: " + provider.toString() + ")");
        }
    }

    private boolean patternRegisteredByDifferentProviderType(final String patternId, final String providerType) {
        boolean differentProviderType = false;

        for (Provider p : providerPatterns.keySet()) {
            List<String> providerIds = providerPatterns.get(p);

            if (providerIds.contains(patternId) && !p.getProviderType().equals(providerType)) {
                differentProviderType = true;
                break;
            }
        }

        return differentProviderType;
    }

    private boolean hasProviderRegisteredPattern(final Provider provider, final String patternId) {
        List<String> ids = providerPatterns.get(provider);
        boolean registered = false;

        if (ids != null && ids.contains(patternId)) {
            registered = true;
        }

        return registered;
    }

    // Checks for duplication pattern registration are presumed to be have already been made before
    // calling this method.
    private void registerPatternWithProvider(final String patternId, final Provider provider) {
        List<String> ids = providerPatterns.get(provider);

        if (ids == null) {
            ids = new ArrayList<String>(1);

            providerPatterns.put(provider, ids);
        }

        ids.add(patternId);
    }

    @Override
    public Collection<String> addPatternDefinitions(final Provider provider,
            final Collection<PatternDefinition> newPatterns) throws DuplicatePatternException, NoSuchProviderException {
        validateProvider(provider);

        if (newPatterns == null) {
            throw new IllegalArgumentException("Patterns not specified (Provider: " + provider.toString() + ")");
        }

        // Check that only zero or one of the provided Patterns are marked as
        // default.
        int defaultCount = 0;

        for (PatternDefinition pattern : newPatterns) {
            if (pattern.isDefaultPattern()) {
                ++defaultCount;
            }
        }

        if (defaultCount > 1) {
            throw new IllegalArgumentException(
                    "More that one Pattern is marked as default (Provider: " + provider.toString() + ")");
        }

        Collection<String> ids = new ArrayList<>(newPatterns.size());

        for (PatternDefinition pattern : newPatterns) {
            ids.add(addPatternDefinition(provider, pattern));
        }

        return ids;
    }

    @Override
    public String removePatternDefinition(final Provider provider, final PatternDefinition pattern)
            throws NoSuchProviderException, NoSuchPatternException {
        validateProvider(provider);
        validatePattern(provider, pattern);
        validatePatternId(provider, pattern.getId());

        return removePatternDefinition(provider, pattern.getId(), true);
    }

    private String removePatternDefinition(final Provider provider, final String patternId,
            final boolean purgeEmptyProvider) throws NoSuchProviderException, NoSuchPatternException {
        validatePatternId(provider, patternId);

        List<String> ids = getPatternsForProvider(provider);

        if (!ids.remove(patternId)) {
            throw new NoSuchPatternException(patternId);
        }

        // Now check to see if there are any other Providers that have
        // registered this Pattern. If not, then we must remove the
        // PatternDefinition as well.

        boolean sharedRegistration = false;

        for (Provider p : providerPatterns.keySet()) {
            List<String> providerIds = providerPatterns.get(p);

            if (providerIds.contains(patternId)) {
                sharedRegistration = true;
                break;
            }
        }

        if (!sharedRegistration) {
            patterns.remove(patternId);
        }

        // If there are no more patterns registered against a Provider, then
        // remove the Provider record too.
        if (purgeEmptyProvider && ids.isEmpty()) {
            providerPatterns.remove(provider);
        }

        return patternId;
    }

    private void validatePatternId(final Provider provider, final String patternId) {
        if (patternId == null || patternId.length() == 0) {
            throw new IllegalArgumentException("Pattern ID not specified (Provider: " + provider.toString() + ")");
        }
    }

    private void validatePatternId(final String patternId) {
        if (patternId == null || patternId.length() == 0) {
            throw new IllegalArgumentException("Pattern ID not specified");
        }
    }

    @Override
    public Collection<String> removePatternDefinitions(final Provider provider)
            throws NoSuchProviderException, NoSuchPatternException {
        validateProvider(provider);

        Collection<String> ids = new ArrayList<>(getPatternsForProvider(provider));
        Collection<String> removedIds = new ArrayList<>(ids.size());

        for (String id : ids) {
            removedIds.add(removePatternDefinition(provider, id, false));
        }

        providerPatterns.remove(provider);

        return removedIds;
    }

    /**
     * Only intended to be used by test code.
     */
    protected void removeAllPatternDefinitions() {
        providerPatterns = new HashMap<>(INITIAL_PATTERNS_SIZE);
        patterns = new HashMap<String, PatternDefinition>(INITIAL_PATTERNS_SIZE);
    }

    @Override
    public PatternDefinition getPattern(final String patternId) throws NoSuchPatternException {
        validatePatternId(patternId);

        PatternDefinition pattern = patterns.get(patternId);

        if (pattern == null) {
            pattern = globalPatterns.get(patternId);
        }

        if (pattern == null) {
            throw new NoSuchPatternException(patternId);
        }

        return pattern;
    }

    private List<String> getPatternsForProvider(final Provider provider) {
        List<String> ids = providerPatterns.get(provider);

        if (ids == null) {
            ids = new ArrayList<>();
        }

        return ids;
    }

    @Override
    public Collection<PatternDefinition> getPatterns(final Provider provider) throws NoSuchProviderException {
        validateProvider(provider);

        List<String> patternIds = getPatternsForProvider(provider);


        Collection<PatternDefinition> result = new ArrayList<>(patternIds.size());

        for (String patternId : patternIds) {
            result.add(patterns.get(patternId));
        }
        result.addAll(globalPatterns.values());
        return result;
    }

    @Override
    public Collection<PatternDefinition> getAllPatterns() {
        return patterns.values();
    }

    @Override
    public Collection<PatternDefinition> getPatternsForProviderType(final String providerType) {
        if (providerType == null || providerType.isEmpty()) {
            throw new IllegalArgumentException("ProviderType not specified");
        }

        // For each Provider of the specified type, build a set of their
        // associated Patterns.
        Set<String> ids = new HashSet<>();

        for (Provider provider : providerPatterns.keySet()) {
            if (provider.getProviderType().equals(providerType)) {
                ids.addAll(providerPatterns.get(provider));
            }
        }

        Collection<PatternDefinition> patternList = new ArrayList<>(ids.size());

        for (String id : ids) {
            patternList.add(patterns.get(id));
        }
        patternList.addAll(globalPatterns.values());
        return patternList;
    }

    @Override
    public PatternDefinition getDefaultPatternDefinition(final Provider provider) throws NoSuchProviderException {
        Collection<PatternDefinition> patternList = getPatterns(provider);

        for (PatternDefinition pattern : patternList) {
            if (pattern.isDefaultPattern()) {
                return pattern;
            }
        }

        return null;
    }

    @Override
    public PatternDefinition setDefaultPatternDefinition(final Provider provider, final PatternDefinition pattern)
            throws NoSuchProviderException, NoSuchPatternException {
        validateProvider(provider);
        validatePattern(provider, pattern);

        if (!hasProviderRegisteredPattern(provider, pattern.getId())) {
            throw new NoSuchPatternException(pattern.getId());
        }

        PatternDefinition oldDefaultPattern = null;
        PatternDefinition defaultPattern = getDefaultPatternDefinition(provider);

        if (defaultPattern != null) {
            oldDefaultPattern = defaultPattern;
            defaultPattern.setDefaultPattern(false);
        }

        pattern.setDefaultPattern(true);

        return oldDefaultPattern;
    }

    @Override
    public TapestryDefinition getTapestryDefinition(final Session session) throws NoSuchSessionException {
        validateSession(session);

        return sessionTapestries.get(session);
    }

    @Override
    public TapestryDefinition setTapestryDefinition(final Session session, final TapestryDefinition tapestry)
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, InvalidQueryInputException, OperationException,
            NoSuchThreadDefinitionException, ItemPropertyNotFound, RelationPropertyNotFound,
            ThreadDeletedByDynAdapterUnload {
        validateSession(session);
        validateTapestry(tapestry);

        TapestryDefinition oldTap = sessionTapestries.get(session);
        if (oldTap != null && oldTap.equals(tapestry)) { // do nothing
            // reset all threads in tapestry to "non deleted" state
            resetThreadsToNonDeleted(oldTap);
            return oldTap;
        }

        if (tapestry.getId() == null) { // Start again
            tapestry.setId(java.util.UUID.randomUUID().toString());
        } else if (oldTap == null) {
            throw new NoSuchTapestryDefinitionException(session.getId(), tapestry.getId(),
                    "old tapestry could not be found");
        }

        sessionTapestries.put(session, tapestry);
        queryManager.tapestryDefinitionChanged(session, oldTap, tapestry);

        return tapestry;
    }

    private void resetThreadsToNonDeleted(final TapestryDefinition oldTap) {
        for (ThreadDefinition thDef : oldTap.getThreads()) {
            thDef.setDeleted(false);
        }
    }

    private void validateTapestry(final TapestryDefinition tapestry) {
        if (tapestry == null) {
            throw new IllegalArgumentException("Tapestry not specified");
        }
    }


    private void validateSession(final Session session) throws NoSuchSessionException {
        if (session == null) {
            throw new NoSuchSessionException(session);
        }
    }

    // !! Note there is a mismatch between this method that clears all Tapestries but assumes
    // !! only one exists and the setTapestryDefinition method that permits multiple Tapestries.
    @Override
    public TapestryDefinition clearTapestryDefinition(final Session session) throws NoSuchSessionException {
        validateSession(session);

        // cleanAssociatedDAs
        aggregationManager.deleteAllDerivedAggregations(session);
        return sessionTapestries.remove(session);
    }

    @Override
    public void markAsGone(final Session session, final String logicalId) {
        TapestryDefinition tap = sessionTapestries.get(session);
        for (ThreadDefinition th : tap.getThreads()) {
            for (String in : th.getQuery().getInputs()) {
                if (in.equals(logicalId)) {
                    th.setDeleted(true);
                }
            }
        }
    }
}
