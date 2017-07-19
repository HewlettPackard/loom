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
package com.hp.hpl.loom.adapter.finance.items;

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;

@ItemTypeInfo(FnQuote.TYPE_LOCAL_ID)
@ConnectedTo(toClass = FnMarket.class,
        relationshipDetails = @LoomAttribute(key = "Market", supportedOperations = {DefaultOperations.GROUP_BY}))
public class FnQuote extends FnItem<FnQuoteAttributes> {
    public static final String TYPE_LOCAL_ID = "quote";

    private static final int ONE_PERCENT = 1;
    private static final int TWO_PERCENT = 2;
    private static final int THREE_PERCENT = 3;
    private static final int FOUR_PERCENT = 4;
    private static final int FIVE_PERCENT = 5;

    // ------------------------------------------------------------------ //
    // PUBLIC INTERFACE //
    // ------------------------------------------------------------------ //
    public FnQuote(final String logicalId, final ItemType quoteType) {
        super(logicalId, quoteType);
    }

    @Override
    public boolean update() {
        boolean update = super.update();
        this.getAlertLevel();
        Double percentChange = this.getCore().getPercentChange();
        if (Math.abs(percentChange) > FIVE_PERCENT) {
            this.setAlertLevel(FnQuote.FIVE_PERCENT);
            this.setAlertDescription("> 5% Change");
            update = true;
        } else if (Math.abs(percentChange) > FOUR_PERCENT) {
            this.setAlertLevel(FnQuote.FOUR_PERCENT);
            this.setAlertDescription("> 4% Change");
            update = true;
        } else if (Math.abs(percentChange) > THREE_PERCENT) {
            this.setAlertLevel(FnQuote.THREE_PERCENT);
            this.setAlertDescription("> 3% Change");
            update = true;
        } else if (Math.abs(percentChange) > TWO_PERCENT) {
            this.setAlertLevel(FnQuote.TWO_PERCENT);
            this.setAlertDescription("> 2% Change");
            update = true;
        } else if (Math.abs(percentChange) > ONE_PERCENT) {
            this.setAlertLevel(FnQuote.ONE_PERCENT);
            this.setAlertDescription("> 1% Change");
            update = true;
        }

        return update;
    }
}
