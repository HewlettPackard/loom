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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.exceptions.DuplicatePatternException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LogicalIdAlreadyExistsException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.PendingQueryResultsException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.manager.aggregation.AggregationManager;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.TapestryDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testTapestryManager.xml")
public class TapestryManagerTest {
    private static final Log LOG = LogFactory.getLog(TapestryManagerTest.class);

    private static final String MY_ID = "myid";

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private TapestryManagerImpl tapestryManager;

    @Autowired
    private AggregationManager aggregationManager;

    @After
    public void shutDown() throws Exception {
        if (tapestryManager != null) {
            tapestryManager.removeAllPatternDefinitions();
        }
    }

    // ----------------------------------------------------------------------------------------------------------
    // Pattern Tests
    // ----------------------------------------------------------------------------------------------------------

    @Test
    public void testAddPattern() throws NoSuchProviderException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);
        String patternId = tapestryManager.addPatternDefinition(provider, pattern);

        assertEquals(MY_ID, patternId);
        assertNotNull(tapestryManager.getPatterns(provider));

        Collection<PatternDefinition> patterns = tapestryManager.getPatterns(provider);
        assertEquals(2, patterns.size());
        assertTrue("Input definition and stored definition should match because the ID should have been updated",
                patterns.contains(pattern));

        PatternDefinition storedPattern = patterns.iterator().next();
        assertNotNull(storedPattern.getId());
        assertTrue("Input definition and stored definition should match because the ID should have been updated",
                storedPattern.equals(pattern));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddUnsupportedUserDefinedPattern() throws NoSuchProviderException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition();

        tapestryManager.addPatternDefinition(provider, pattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPatternNullProvider() throws DuplicatePatternException, NoSuchProviderException {
        tapestryManager.addPatternDefinition(null, new PatternDefinition(MY_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPattern() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        tapestryManager.addPatternDefinition(provider, null);
    }

    @Test(expected = DuplicatePatternException.class)
    public void testAddSamePatternTwiceBySameProvider() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        tapestryManager.addPatternDefinition(provider, pattern);
        tapestryManager.addPatternDefinition(provider, pattern);
    }

    @Test
    public void testAddSamePatternByTwoProviders() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider1 = new ProviderImpl("type", "id1", "uri", "name", "com");
        Provider provider2 = new ProviderImpl("type", "id2", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        String patternId1 = tapestryManager.addPatternDefinition(provider1, pattern);
        String patternId2 = tapestryManager.addPatternDefinition(provider2, pattern);

        assertNotNull(patternId1);
        assertNotNull(patternId2);
        assertEquals(patternId1, patternId2);
    }

    @Test
    public void testAddPatterns() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        Collection<PatternDefinition> patterns = new ArrayList<PatternDefinition>();

        patterns.add(new PatternDefinition(MY_ID));
        patterns.add(new PatternDefinition(MY_ID + "2"));

        Collection<String> patternIds = tapestryManager.addPatternDefinitions(provider, patterns);

        assertEquals(patterns.size(), patternIds.size());
        assertEquals(patterns.size(), tapestryManager.getAllPatterns().size());
        // adding one for the global provider pattern
        assertEquals(patterns.size() + 1, tapestryManager.getPatternsForProviderType("type").size());
    }

    @Test
    public void testAddPatternWithSingleDefault() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern1 = new PatternDefinition(MY_ID);
        PatternDefinition defaultPattern = new PatternDefinition(MY_ID + "2");

        defaultPattern.setDefaultPattern(true);

        tapestryManager.addPatternDefinition(provider, pattern1);
        assertNull(tapestryManager.getDefaultPatternDefinition(provider));

        tapestryManager.addPatternDefinition(provider, defaultPattern);
        assertEquals(2, tapestryManager.getAllPatterns().size());
        assertNotNull(tapestryManager.getDefaultPatternDefinition(provider));
    }

    @Test
    public void testAddPatternAndThenSetDefault()
            throws DuplicatePatternException, NoSuchProviderException, NoSuchPatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern1 = new PatternDefinition(MY_ID);
        PatternDefinition pattern2 = new PatternDefinition(MY_ID + "2");

        tapestryManager.addPatternDefinition(provider, pattern1);
        assertNull(tapestryManager.getDefaultPatternDefinition(provider));

        tapestryManager.addPatternDefinition(provider, pattern2);
        assertEquals(2, tapestryManager.getAllPatterns().size());
        assertNull(tapestryManager.getDefaultPatternDefinition(provider));

        // Now declare the already added pattern as the default
        PatternDefinition oldDefault = tapestryManager.setDefaultPatternDefinition(provider, pattern2);

        assertNull(oldDefault);
        assertNotNull(tapestryManager.getDefaultPatternDefinition(provider));
        assertEquals(pattern2, tapestryManager.getDefaultPatternDefinition(provider));

        // Now change the default pattern
        oldDefault = tapestryManager.setDefaultPatternDefinition(provider, pattern1);

        assertEquals(pattern2, oldDefault);
        assertEquals(pattern1, tapestryManager.getDefaultPatternDefinition(provider));

        // Check that there is still only 1 default pattern
        assertEquals(2, tapestryManager.getAllPatterns().size());

        int defaultCount = 0;

        for (PatternDefinition pattern : tapestryManager.getAllPatterns()) {
            if (pattern.isDefaultPattern()) {
                ++defaultCount;
            }
        }

        assertEquals(1, defaultCount);
    }

    @Test
    public void testAddPatternWithTwoDefaults() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern1 = new PatternDefinition(MY_ID);
        PatternDefinition defaultPattern = new PatternDefinition(MY_ID + "2");

        defaultPattern.setDefaultPattern(true);
        pattern1.setDefaultPattern(true);

        tapestryManager.addPatternDefinition(provider, pattern1);
        PatternDefinition storedDefault = tapestryManager.getDefaultPatternDefinition(provider);
        assertNotNull(storedDefault);
        assertEquals(pattern1, storedDefault);

        tapestryManager.addPatternDefinition(provider, defaultPattern);
        storedDefault = tapestryManager.getDefaultPatternDefinition(provider);
        assertEquals(2, tapestryManager.getAllPatterns().size());
        assertNotNull(tapestryManager.getDefaultPatternDefinition(provider));
        assertEquals(defaultPattern, storedDefault);
    }

    @Test
    public void testAddPatternsWithSingleDefault() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        Collection<PatternDefinition> patterns = new ArrayList<PatternDefinition>();
        PatternDefinition defaultPattern = new PatternDefinition(MY_ID + "2");

        patterns.add(new PatternDefinition(MY_ID));
        defaultPattern.setDefaultPattern(true);
        patterns.add(defaultPattern);

        Collection<String> patternIds = tapestryManager.addPatternDefinitions(provider, patterns);

        assertEquals(patterns.size(), patternIds.size());
        assertEquals(patterns.size(), tapestryManager.getAllPatterns().size());

        PatternDefinition storedDefault = tapestryManager.getDefaultPatternDefinition(provider);

        assertNotNull(storedDefault);
        assertEquals(defaultPattern, storedDefault);
    }

    @Test(expected = NoSuchPatternException.class)
    public void testSetNonExistentDefaultPattern() throws NoSuchProviderException, NoSuchPatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        tapestryManager.setDefaultPatternDefinition(provider, new PatternDefinition(MY_ID + "1"));
    }

    //
    // @Test
    // public void testCloningOfThreadsGivenInputLogicalIds() throws
    // DuplicatePatternException, NoSuchProviderException {
    // List<String> logicalIds = Arrays.asList(new
    // String[]{"/test/fake1","/test/fake2","/test/fake3"});
    //
    // Meta meta = new Meta();
    //
    // PatternDefinition pat1 = new PatternDefinition("p1");
    // ThreadDefinition th1 = new ThreadDefinition();
    // QueryDefinition query1 = new QueryDefinition("/test/fake1");
    // th1.setQuery(query1);
    // List<ThreadDefinition> threads1 = new ArrayList<ThreadDefinition>(1);
    // threads1.add(th1);
    // pat1.setThreads(threads1);
    // pat1.setProviderType("LOOM");
    // pat1.set_meta(meta);
    //
    // PatternDefinition pat2 = new PatternDefinition("p2");
    // ThreadDefinition th2 = new ThreadDefinition();
    // QueryDefinition query2 = new QueryDefinition("/test/fake2");
    // th2.setQuery(query2);
    // List<ThreadDefinition> threads2 = new ArrayList<ThreadDefinition>(1);
    // threads2.add(th2);
    // pat2.setThreads(threads2);
    // pat2.setProviderType("LOOM");
    // pat2.set_meta(meta);
    //
    // PatternDefinition pat3 = new PatternDefinition("p3");
    // ThreadDefinition th3 = new ThreadDefinition();
    // QueryDefinition query3 = new QueryDefinition("/test/fake3");
    // th3.setQuery(query3);
    // List<ThreadDefinition> threads3 = new ArrayList<ThreadDefinition>(1);
    // threads3.add(th3);
    // pat3.setThreads(threads3);
    // pat3.setDefaultPattern(true);
    // pat3.setProviderType("LOOM");
    // pat3.set_meta(meta);
    //
    // tapestryManager.addPatternDefinition(AggregationMapperImpl.LOOM_PROVIDER,pat1);
    // tapestryManager.addPatternDefinition(AggregationMapperImpl.LOOM_PROVIDER,pat2);
    // tapestryManager.addPatternDefinition(AggregationMapperImpl.LOOM_PROVIDER,pat3);
    //
    // Set<PatternDefinition>
    // contain=tapestryManager.getCopyOfContainingPatterns(new
    // HashSet(logicalIds));
    //
    // List<String> containList= new ArrayList<String>(contain.size());
    // for(PatternDefinition pat : contain){
    // log.info(pat);
    //
    // containList.add(pat.getThreads().get(0).getQuery().getInputs().get(0));
    // }
    //
    //
    // assertEquals("Size should be 3", 3, containList.size());
    // assertTrue(containList.containsAll(logicalIds));
    //
    //
    // List<String> changedInputs = new ArrayList<String>(1);
    // changedInputs.add("/test/fake4");
    // query3.setInputs(changedInputs);
    //
    // contain=tapestryManager.getCopyOfContainingPatterns(new
    // HashSet(logicalIds));
    //
    // containList= new ArrayList<String>(contain.size());
    // for(PatternDefinition pat : contain){
    // log.info(pat);
    //
    // containList.add(pat.getThreads().get(0).getQuery().getInputs().get(0));
    // }
    //
    //
    // assertEquals("Size should be 2", 2, containList.size());
    //
    // }

    @Test
    public void testAddMorePatternsWithSingleDefault() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        Collection<PatternDefinition> patterns = new ArrayList<PatternDefinition>();
        PatternDefinition defaultPattern = new PatternDefinition(MY_ID + "2");

        patterns.add(new PatternDefinition(MY_ID));
        defaultPattern.setDefaultPattern(true);
        patterns.add(defaultPattern);

        tapestryManager.addPatternDefinitions(provider, patterns);

        assertEquals(patterns.size(), tapestryManager.getAllPatterns().size());

        PatternDefinition storedDefault = tapestryManager.getDefaultPatternDefinition(provider);

        assertNotNull(storedDefault);
        assertEquals(defaultPattern, storedDefault);

        // Now add some Patterns with one marked as default which should
        // supercede the
        // previous default Pattern.
        Collection<PatternDefinition> morePatterns = new ArrayList<PatternDefinition>();
        PatternDefinition newDefaultPattern = new PatternDefinition(MY_ID + "3");

        newDefaultPattern.setDefaultPattern(true);
        morePatterns.add(newDefaultPattern);
        morePatterns.add(new PatternDefinition(MY_ID + "4"));

        tapestryManager.addPatternDefinitions(provider, morePatterns);
        assertEquals(patterns.size() + morePatterns.size(), tapestryManager.getAllPatterns().size());

        storedDefault = tapestryManager.getDefaultPatternDefinition(provider);

        assertNotNull(storedDefault);
        assertEquals(newDefaultPattern, storedDefault);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPatternsWithTwoDefaults() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        Collection<PatternDefinition> patterns = new ArrayList<PatternDefinition>();
        PatternDefinition pattern1 = new PatternDefinition(MY_ID);
        PatternDefinition defaultPattern = new PatternDefinition(MY_ID + "2");

        pattern1.setDefaultPattern(true);
        defaultPattern.setDefaultPattern(true);
        patterns.add(pattern1);
        patterns.add(defaultPattern);

        tapestryManager.addPatternDefinitions(provider, patterns);
    }

    @Test(expected = DuplicatePatternException.class)
    public void testAddPatternsDifferentProviderTypes() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider1 = new ProviderImpl("type1", "id", "uri", "name", "com");
        Provider provider2 = new ProviderImpl("type2", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        tapestryManager.addPatternDefinition(provider1, pattern);
        tapestryManager.addPatternDefinition(provider2, pattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPatterns() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        tapestryManager.addPatternDefinitions(provider, null);
    }

    @Test(expected = DuplicatePatternException.class)
    public void testAddDuplicatePattern() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        String id = tapestryManager.addPatternDefinition(provider, pattern);

        assertNotNull(id);

        tapestryManager.addPatternDefinition(provider, pattern);
    }

    @Test(expected = DuplicatePatternException.class)
    public void testAddDuplicatePatterns() throws DuplicatePatternException, NoSuchProviderException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        Collection<PatternDefinition> patterns = new ArrayList<PatternDefinition>();

        patterns.add(new PatternDefinition(MY_ID));

        Collection<String> patternIds = tapestryManager.addPatternDefinitions(provider, patterns);

        assertEquals(patterns.size(), patternIds.size());

        tapestryManager.addPatternDefinitions(provider, patterns);
    }

    @Test
    public void testAddOneRemoveOnePattern()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        String patternId = tapestryManager.addPatternDefinition(provider, pattern);

        assertNotNull(patternId);

        Collection<PatternDefinition> patterns = tapestryManager.getPatterns(provider);

        assertEquals(2, patterns.size());

        String removedId = tapestryManager.removePatternDefinition(provider, pattern);

        assertEquals(removedId, patternId);

        // This will throw an exception because the Provider was removed when we
        // removed
        // the only Pattern registered against it.
        patterns = tapestryManager.getPatterns(provider);
        assertEquals(1, patterns.size());
    }

    @Test
    public void testAddTwoRemoveOnePattern()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition("id1");
        PatternDefinition pattern2 = new PatternDefinition("id2");

        String patternId1 = tapestryManager.addPatternDefinition(provider, pattern);
        tapestryManager.addPatternDefinition(provider, pattern2);

        Collection<PatternDefinition> patterns = tapestryManager.getPatterns(provider);

        assertEquals(3, patterns.size());

        String removedId = tapestryManager.removePatternDefinition(provider, pattern);

        assertEquals(removedId, patternId1);

        patterns = tapestryManager.getPatterns(provider);

        assertEquals(2, patterns.size());
    }

    @Test(expected = NoSuchPatternException.class)
    public void testRemoveNonExistentPattern()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        tapestryManager.addPatternDefinition(provider, pattern);
        pattern.setId("nosuchid");
        tapestryManager.removePatternDefinition(provider, pattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullPattern()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        tapestryManager.addPatternDefinition(provider, new PatternDefinition(MY_ID));
        tapestryManager.removePatternDefinition(provider, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullPattern2()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");

        tapestryManager.addPatternDefinition(provider, new PatternDefinition(MY_ID));
        tapestryManager.removePatternDefinition(provider, null);
    }

    @Test(expected = NoSuchPatternException.class)
    public void testRemovePatternFromNonExistentProvider()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);
        pattern.setId("nosuchid");
        tapestryManager.removePatternDefinition(provider, pattern);
    }

    @Test
    public void testRemovePatterns() throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition("id1");
        PatternDefinition pattern2 = new PatternDefinition("id2");

        tapestryManager.addPatternDefinition(provider, pattern);
        tapestryManager.addPatternDefinition(provider, pattern2);

        Collection<PatternDefinition> patterns = tapestryManager.getPatterns(provider);

        assertEquals(3, patterns.size());

        Collection<String> removedIds = tapestryManager.removePatternDefinitions(provider);

        assertEquals(2, removedIds.size());

        patterns = tapestryManager.getPatterns(provider);
        assertEquals(1, patterns.size());
    }

    @Test
    public void testRemoveSamePatternByTwoProviders()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider1 = new ProviderImpl("type", "id1", "uri", "name", "com");
        Provider provider2 = new ProviderImpl("type", "id2", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition("id1");

        tapestryManager.addPatternDefinition(provider1, pattern);
        tapestryManager.addPatternDefinition(provider2, pattern);

        assertEquals(2, tapestryManager.getPatterns(provider1).size());
        assertEquals(2, tapestryManager.getPatterns(provider2).size());
        assertEquals(1, tapestryManager.removePatternDefinitions(provider1).size());

        try {
            Collection<PatternDefinition> patterns = tapestryManager.getPatterns(provider1);
            assertEquals(1, patterns.size());
        } catch (NoSuchProviderException e) {
            assertTrue(true);
        }

        assertEquals(2, tapestryManager.getPatterns(provider2).size());
        assertEquals(1, tapestryManager.removePatternDefinitions(provider2).size());

        Collection<PatternDefinition> patterns = tapestryManager.getPatterns(provider2);
        assertEquals(1, patterns.size());
    }

    @Test
    public void testGetPattern() throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        String patternId = tapestryManager.addPatternDefinition(provider, pattern);

        assertNotNull(patternId);

        PatternDefinition storedPattern = tapestryManager.getPattern(patternId);

        assertNotNull(storedPattern.getId());
        assertEquals(pattern, storedPattern);
    }

    @Test(expected = NoSuchPatternException.class)
    public void testGetNonExistentPattern()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        tapestryManager.getPattern("nosuchid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullPattern() throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        tapestryManager.getPattern(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEmptyPattern()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        tapestryManager.getPattern("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPatternsForNullProviderType()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        tapestryManager.getPatternsForProviderType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPatternsForEmptyProviderType()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        tapestryManager.getPatternsForProviderType("");
    }

    @Test
    public void testGetPatternsForUnknownProviderType()
            throws NoSuchProviderException, NoSuchPatternException, DuplicatePatternException {
        Provider provider = new ProviderImpl("type", "id", "uri", "name", "com");
        PatternDefinition pattern = new PatternDefinition(MY_ID);

        tapestryManager.addPatternDefinition(provider, pattern);

        assertEquals(1, tapestryManager.getPatternsForProviderType("unknown").size());
    }

    // ----------------------------------------------------------------------------------------------------------
    // Tapestry Tests
    // ----------------------------------------------------------------------------------------------------------

    @Test
    public void testSetTapestry()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {

        Session session = new SessionImpl("asession", sessionManager.getInterval());
        TapestryDefinition tapestry = new TapestryDefinition();
        TapestryDefinition storedTapestry = tapestryManager.setTapestryDefinition(session, tapestry);

        assertNotNull(storedTapestry);
        assertNotNull(storedTapestry.getId());
        assertEquals(tapestry, storedTapestry);

        storedTapestry = tapestryManager.getTapestryDefinition(session);

        assertNotNull(storedTapestry);
        assertEquals(tapestry, storedTapestry);
    }

    @Test
    public void testSetSameTapestryTwice()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {

        Session session = new SessionImpl("asession", sessionManager.getInterval());
        TapestryDefinition tapestry = new TapestryDefinition();

        tapestryManager.setTapestryDefinition(session, tapestry);

        TapestryDefinition storedTapestry = tapestryManager.getTapestryDefinition(session);

        assertEquals(tapestry, storedTapestry);

        TapestryDefinition storedTapestry2 = tapestryManager.setTapestryDefinition(session, tapestry);

        assertEquals(storedTapestry, storedTapestry2);
    }

    @Test
    public void testUpdateTapestry()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {

        Session session = new SessionImpl("asession", sessionManager.getInterval());
        TapestryDefinition tapestry = new TapestryDefinition();
        TapestryDefinition storedTapestry = tapestryManager.setTapestryDefinition(session, tapestry);

        assertEquals(tapestry, storedTapestry);

        TapestryDefinition tapestry2 = new TapestryDefinition();
        tapestry2.setId("anid");

        storedTapestry = tapestryManager.setTapestryDefinition(session, tapestry2);

        assertEquals(storedTapestry, tapestry2);
    }

    @Test
    public void testSetTwoTapestries() throws NoSuchSessionException, NoSuchTapestryDefinitionException,
            SessionAlreadyExistsException, NoSuchAggregationException, NoSuchQueryDefinitionException,
            OperationException, NoSuchThreadDefinitionException, InvalidQueryInputException,
            LogicalIdAlreadyExistsException, InvalidQueryParametersException, PendingQueryResultsException,
            ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException, UnsupportedOperationException,
            ThreadDeletedByDynAdapterUnload {

        Session session = new SessionImpl("asession", sessionManager.getInterval());
        TapestryDefinition tapestry = new TapestryDefinition();
        TapestryDefinition tapestry2 = new TapestryDefinition();

        tapestryManager.setTapestryDefinition(session, tapestry);
        tapestryManager.setTapestryDefinition(session, tapestry2);

        assertNotEquals(tapestry.getId(), tapestry2.getId());
        aggregationManager.createSession(session);
        TapestryDefinition storedTapestry = tapestryManager.clearTapestryDefinition(session);

        assertNotNull(storedTapestry);
        assertEquals(tapestry2, storedTapestry);
    }

    @Test(expected = NoSuchSessionException.class)
    public void testSetTapestryNullSession()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {
        tapestryManager.setTapestryDefinition(null, new TapestryDefinition());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullTapestry()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {

        tapestryManager.setTapestryDefinition(new SessionImpl("assesion", sessionManager.getInterval()), null);
    }

    @Test(expected = NoSuchTapestryDefinitionException.class)
    public void testSetUnknownTapestry()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {
        TapestryDefinition tapestry = new TapestryDefinition();
        tapestry.setId("noid");

        tapestryManager.setTapestryDefinition(new SessionImpl("assesion", sessionManager.getInterval()), tapestry);
    }

    @Test(expected = NoSuchSessionException.class)
    public void testClearTapestry()
            throws NoSuchSessionException, NoSuchTapestryDefinitionException, NoSuchAggregationException,
            NoSuchQueryDefinitionException, OperationException, NoSuchThreadDefinitionException,
            InvalidQueryInputException, LogicalIdAlreadyExistsException, InvalidQueryParametersException,
            PendingQueryResultsException, ItemPropertyNotFound, RelationPropertyNotFound, IllegalArgumentException,
            UnsupportedOperationException, ThreadDeletedByDynAdapterUnload {

        Session session = new SessionImpl("asession", sessionManager.getInterval());
        TapestryDefinition tapestry = new TapestryDefinition();

        tapestryManager.setTapestryDefinition(session, tapestry);

        TapestryDefinition storedTapestry = tapestryManager.clearTapestryDefinition(null);

        assertNotNull(storedTapestry);
        assertEquals(tapestry, storedTapestry);
    }

    @Test(expected = NoSuchSessionException.class)
    public void testClearTapestryNullSession() throws NoSuchSessionException {
        tapestryManager.clearTapestryDefinition(null);
    }
}
