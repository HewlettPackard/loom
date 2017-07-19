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
package com.hp.hpl.loom.manager.aggregation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-testAggregationManager.xml")
public class AggregationMapperImplTest {

    AggregationMapper aggregationMapper;

    @Before
    public void setUp() throws Exception {
        aggregationMapper = new AggregationMapperImpl();
    }

    @Test
    public void testMapGroundedAggregation() throws Exception {
        Set<String> mapped = aggregationMapper.getMap("/test/fake1");
        assertEquals("Should initially be null", null, mapped);
        aggregationMapper.mapGroundedAggregation("/test/fakeProv/fake1", "/test/fake1");
        mapped = aggregationMapper.getMap("/test/fake1");
        assertEquals("Should be 1", 1, mapped.size());
    }

    // @Test
    // public void testUpdatePatterns() throws Exception {
    // Meta meta = new Meta();
    //
    // Provider prov1 = new ProviderImpl("p1","p1","p1");
    // Provider prov2 = new ProviderImpl("p2","p2","p2");
    //
    // PatternDefinition pat11 = new PatternDefinition("p1");
    // ThreadDefinition th1 = new ThreadDefinition();
    // QueryDefinition query1 = new QueryDefinition("/test/p1/fake1");
    // th1.setQuery(query1);
    // List<ThreadDefinition> threads1 = new ArrayList<ThreadDefinition>(1);
    // threads1.add(th1);
    // pat11.setThreads(threads1);
    // pat11.setProviderType("p1");
    // pat11.set_meta(meta);
    //
    // PatternDefinition pat12 = new PatternDefinition("p2");
    // ThreadDefinition th2 = new ThreadDefinition();
    // QueryDefinition query2 = new QueryDefinition("/test/p1/fake2");
    // th2.setQuery(query2);
    // List<ThreadDefinition> threads2 = new ArrayList<ThreadDefinition>(1);
    // threads2.add(th2);
    // pat12.setThreads(threads2);
    // pat12.setProviderType("p1");
    // pat12.set_meta(meta);
    //
    // PatternDefinition pat13 = new PatternDefinition("p3");
    // ThreadDefinition th3 = new ThreadDefinition();
    // QueryDefinition query3 = new QueryDefinition("/test/p1/fake3");
    // th3.setQuery(query3);
    // List<ThreadDefinition> threads3 = new ArrayList<ThreadDefinition>(1);
    // threads3.add(th3);
    // pat13.setThreads(threads3);
    // pat13.setDefaultPattern(true);
    // pat13.setProviderType("p1");
    // pat13.set_meta(meta);
    //
    // tapestryManager.addPatternDefinition(prov1,pat11);
    // tapestryManager.addPatternDefinition(prov1,pat12);
    // tapestryManager.addPatternDefinition(prov1,pat13);
    //
    // PatternDefinition pat21 = new PatternDefinition("p21");
    // ThreadDefinition th21 = new ThreadDefinition();
    // QueryDefinition query21 = new QueryDefinition("/test/p2/fake1");
    // th21.setQuery(query21);
    // List<ThreadDefinition> threads21 = new ArrayList<ThreadDefinition>(1);
    // threads21.add(th21);
    // pat21.setThreads(threads21);
    // pat21.setProviderType("p2");
    // pat21.set_meta(meta);
    //
    // PatternDefinition pat22 = new PatternDefinition("p22");
    // ThreadDefinition th22 = new ThreadDefinition();
    // QueryDefinition query22 = new QueryDefinition("/test/p2/fake2");
    // th22.setQuery(query22);
    // List<ThreadDefinition> threads22 = new ArrayList<ThreadDefinition>(1);
    // threads22.add(th22);
    // pat22.setThreads(threads22);
    // pat22.setProviderType("p2");
    // pat22.set_meta(meta);
    //
    // PatternDefinition pat23 = new PatternDefinition("p23");
    // ThreadDefinition th23 = new ThreadDefinition();
    // QueryDefinition query23 = new QueryDefinition("/test/p2/fake3");
    // th23.setQuery(query23);
    // List<ThreadDefinition> threads23 = new ArrayList<ThreadDefinition>(1);
    // threads23.add(th23);
    // pat23.setThreads(threads23);
    // pat23.setDefaultPattern(true);
    // pat23.setProviderType("p2");
    // pat23.set_meta(meta);
    //
    // tapestryManager.addPatternDefinition(prov2,pat21);
    // tapestryManager.addPatternDefinition(prov2,pat22);
    // tapestryManager.addPatternDefinition(prov2,pat23);
    //
    // aggregationMapper.mapGroundedAggregation("/test/p1/fake1","/test/fake1");
    // aggregationMapper.mapGroundedAggregation("/test/p2/fake1","/test/fake1");
    //
    // aggregationMapper.mapGroundedAggregation("/test/p1/fake2","/test/fake2");
    // aggregationMapper.mapGroundedAggregation("/test/p2/fake2","/test/fake2");
    //
    // aggregationMapper.mapGroundedAggregation("/test/p1/fake3","/test/fake3");
    // aggregationMapper.mapGroundedAggregation("/test/p2/fake3","/test/fake3");
    //
    // List<String> merged = new ArrayList<>(3);
    // merged.add("/test/fake1");
    // merged.add("/test/fake2");
    // merged.add("/test/fake3");
    //
    // aggregationMapper.updatePatterns(merged);
    //
    // Set<String> fake1 = aggregationMapper.getMap("/test/fake1");
    // Set<String> fake2 = aggregationMapper.getMap("/test/fake2");
    // Set<String> fake3 = aggregationMapper.getMap("/test/fake3");
    //
    // assertEquals("Expected two elements in set", 2, fake1.size());
    // assertEquals("Expected two elements in set", 2, fake2.size());
    // assertEquals("Expected two elements in set", 2, fake3.size());
    //
    // assertTrue(fake1.contains("/test/p1/fake1"));
    // assertTrue(fake1.contains("/test/p2/fake1"));
    //
    // assertTrue(fake2.contains("/test/p1/fake2"));
    // assertTrue(fake2.contains("/test/p2/fake2"));
    //
    // assertTrue(fake3.contains("/test/p1/fake3"));
    // assertTrue(fake3.contains("/test/p2/fake3"));
    //
    //
    // PatternDefinition newP1=tapestryManager.getPattern("dynp1");
    // PatternDefinition newP2=tapestryManager.getPattern("dynp2");
    // assertNotNull(newP1);
    // assertNotNull(newP2);
    // assertTrue(newP1.getThreads().get(0).getQuery().getInputs().get(0).equalsIgnoreCase("/test/fake1"));
    // assertFalse(newP1.getThreads().get(0).getQuery().getInputs().get(0).equalsIgnoreCase("/test/fake2"));
    // assertTrue(newP2.getThreads().get(0).getQuery().getInputs().get(0).equalsIgnoreCase("/test/fake2"));
    //
    // PatternDefinition newP21=tapestryManager.getPattern("dynp21");
    // PatternDefinition newP22=tapestryManager.getPattern("dynp22");
    // assertNotNull(newP21);
    // assertNotNull(newP22);
    // assertTrue(newP21.getThreads().get(0).getQuery().getInputs().get(0).equalsIgnoreCase("/test/fake1"));
    // assertTrue(newP22.getThreads().get(0).getQuery().getInputs().get(0).equalsIgnoreCase("/test/fake2"));
    //
    //
    // }

    @Test
    public void testGetMap() throws Exception {

    }

    @Test
    public void testCleanMap() throws Exception {

    }
}
