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
package com.hp.hpl.loom.manager.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.loom.adapter.os.OsFlavour;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.tapestry.Operation;

public class QueryResultFormattingHelperTest {

    private static int NUM_ITEMS = 200;
    private static int FIRST_LEVEL = 10;
    private static int SECOND_LEVEL = 2;
    private static int NO_ALERTS = 0;
    private static int ONE_ALERT = 1;

    List<Item> items;
    List<Aggregation> firstLevelAgg;
    List<Aggregation> secondLevelAgg;

    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");

    @Before
    public void setUp() {

        ItemType instanceType = new OsInstanceType(provider);
        instanceType.setId("os-" + instanceType.getLocalId());
        items = new ArrayList<>(NUM_ITEMS);
        firstLevelAgg = new ArrayList<>(10);
        Aggregation agg;
        for (int i = 0; i < FIRST_LEVEL; i++) {
            agg = new Aggregation("/os/agg" + i, Fibre.Type.Item, String.valueOf(i), String.valueOf(i),

                    String.valueOf(i), NUM_ITEMS / FIRST_LEVEL);
            agg.setAlertCount(0);
            firstLevelAgg.add(agg);
        }

        OsInstance item;
        for (int i = 0; i < NUM_ITEMS; i++) {
            // item =
            // new OsInstance("/os/instance" + i, String.valueOf(i), String.valueOf(i), new
            // OsFlavour(),
            // instanceType);
            item = new OsInstance("/os/instance" + i, instanceType);
            OsInstanceAttributes oia = new OsInstanceAttributes(new OsFlavour());
            oia.setItemName(String.valueOf(i));
            oia.setItemId(String.valueOf(i));
            item.setCore(oia);
            item.setAlertLevel(0);
            item.setAlertDescription("");
            items.add(item);
            firstLevelAgg.get(i % FIRST_LEVEL).add(item);
        }

        secondLevelAgg = new ArrayList<>(2);
        Aggregation agg1 = new Aggregation("second1", Fibre.Type.Aggregation, "1", "1", "1", 5);
        agg1.setAlertCount(0);
        Aggregation agg2 = new Aggregation("second2", Fibre.Type.Aggregation, "2", "2", "2", 5);

        agg2.setAlertCount(0);
        secondLevelAgg.add(agg1);
        secondLevelAgg.add(agg2);

        for (int i = 0; i < FIRST_LEVEL; i++) {
            secondLevelAgg.get(i % SECOND_LEVEL).add(firstLevelAgg.get(i));
        }
    }

    @Test
    public void testCalculateAggregateAlerts() throws Exception {

        // validate there are no alerts
        assertTrue(items.stream().filter(i -> i.getAlertLevel() > 0).count() == NO_ALERTS
                && secondLevelAgg.stream().filter(a -> a.getAlertCount() > 0).count() == NO_ALERTS
                && firstLevelAgg.stream().filter(a -> a.getAlertCount() > 0).count() == NO_ALERTS);

        // add alert on item and verify it is aggregated
        items.get(0).setAlertDescription("An alert");
        items.get(0).setAlertLevel(6);

        updateAggregations();
        int items1 = (int) items.stream().filter(i -> i.getAlertLevel() > 0).count();
        int secondLevel = (int) secondLevelAgg.stream().filter(a -> a.getAlertCount() > 0).count();
        int firstLevel = (int) firstLevelAgg.stream().filter(f -> f.getAlertCount() > 0).count();

        assertTrue(items1 == ONE_ALERT && secondLevel == ONE_ALERT && firstLevel == ONE_ALERT);

        // add alert on all items and verify it is aggregated
        for (int i = 0; i < NUM_ITEMS; i++) {
            items.get(i).setAlertDescription("Alert" + i);
            items.get(i).setAlertLevel(i);
        }

        updateAggregations();

        assertTrue(items.stream().filter(i -> i.getAlertLevel() > 0).count() == (NUM_ITEMS - 1) * ONE_ALERT
                && secondLevelAgg.stream().mapToInt(a -> a.getAlertCount().intValue()).reduce(0,
                        (a, b) -> a + b) == (NUM_ITEMS - 1) * ONE_ALERT
                && firstLevelAgg.stream().mapToInt(a -> a.getAlertCount().intValue()).reduce(0,
                        (a, b) -> a + b) == (NUM_ITEMS - 1) * ONE_ALERT);
        assertEquals("Should be one with level 199",
                secondLevelAgg.stream().map(a -> a.getHighestAlertLevel()).filter(l -> l == 199).count(), 1);

        // clear alert on item and verify aggregated
        // add alert on all items and verify it is aggregated
        for (int i = 0; i < NUM_ITEMS; i++) {
            items.get(i).setAlertDescription("");
            items.get(i).setAlertLevel(0);
        }

        updateAggregations();

        assertTrue(items.stream().filter(i -> i.getAlertLevel() > 0).count() == NO_ALERTS
                && secondLevelAgg.stream().filter(a -> a.getAlertCount() > 0).count() == NO_ALERTS
                && firstLevelAgg.stream().filter(a -> a.getAlertCount() > 0).count() == NO_ALERTS);
        assertEquals("Should be none",
                secondLevelAgg.stream().map(a -> a.getHighestAlertLevel()).filter(l -> l > 0).count(), 0);

    }

    private void updateAggregations() {
        for (int i = 0; i < FIRST_LEVEL; i++) {
            QueryResultFormattingHelper.calculateAggregateAlerts(firstLevelAgg.get(i));
        }

        for (int i = 0; i < SECOND_LEVEL; i++) {
            QueryResultFormattingHelper.calculateAggregateAlerts(secondLevelAgg.get(i));
        }

    }

    @Test
    public void testSetTags() {
        Aggregation derived = new Aggregation("anId", "aType", "aName", "aDescription", 0);

        Aggregation notDerived = new Aggregation("anId", "aType", "aName", "aDescription", 0);

        boolean aggregated = false;
        boolean effectiveBraid = false;
        boolean isGrouped = false;
        int aggSize = 0;
        int aggIndex = 0;
        derived = new Aggregation("anId", "aType", "aName", "aDescription", 0);



        aggregated = true;
        QueryResultFormattingHelper.setAttributes(effectiveBraid, isGrouped, derived, aggSize, aggIndex, "");
        assertTrue(StringUtils.isBlank(derived.getTags()));

        Operation fakeOp = new Operation("FAKE_OPERATOR", Collections.emptyMap());
        derived = new Aggregation("anId", "aType", "aName", "aDescription", 0);
        aggregated = false;
        QueryResultFormattingHelper.setAttributes(effectiveBraid, isGrouped, derived, aggSize, aggIndex,
                fakeOp.getOperator());
        assertTrue(derived.getTags().equalsIgnoreCase("FAKE_OPERATOR"));

        aggSize = 45;
        aggIndex = 0;
        QueryResultFormattingHelper.setAttributes(effectiveBraid, isGrouped, derived, aggSize, aggIndex,
                fakeOp.getOperator());
        assertTrue(derived.getMaxIndex() == 44);
        assertTrue(derived.getMinIndex() == 0);

        aggSize = 45;
        aggIndex = 22;
        QueryResultFormattingHelper.setAttributes(effectiveBraid, isGrouped, derived, aggSize, aggIndex,
                fakeOp.getOperator());
        assertTrue(derived.getMaxIndex() == 66);
        assertTrue(derived.getMinIndex() == 22);
    }
}
