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
package com.hp.hpl.loom.manager.stitcher.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;

import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.manager.stitcher.StitcherUpdater;
import com.hp.hpl.loom.manager.stitcher.testutils.GenericStitcherTest;

public class TestStitcherIndexableTest extends GenericStitcherTest {

    private static final Log LOG = LogFactory.getLog(TestStitcherIndexableTest.class);

    @Override
    protected StitcherRuleManager createNewStitcherRuleManager() {
        return new TestStitcherRuleManager();
    }

    @Override
    protected StitcherUpdater createNewStitcherSession(final StitcherRuleManager ruleManager) {
        return new TestStitcherSession((TestStitcherRuleManager) ruleManager, true, true);
    }

    @Override
    @Before
    public void setUp() {
        LOG.info("Setup indexable test");
        useIndex = true;
        super.setUp();
    }
}
