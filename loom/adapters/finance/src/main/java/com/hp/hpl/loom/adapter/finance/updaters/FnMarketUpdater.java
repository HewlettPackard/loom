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
package com.hp.hpl.loom.adapter.finance.updaters;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.finance.FnItemCollector;
import com.hp.hpl.loom.adapter.finance.items.FnMarket;
import com.hp.hpl.loom.adapter.finance.items.FnMarketAttributes;
import com.hp.hpl.loom.adapter.finance.models.Quote;
// import org.apache.log4j.Logger;
import com.hp.hpl.loom.adapter.finance.models.YahooQuote;
import com.hp.hpl.loom.adapter.finance.models.YahooResponse;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class FnMarketUpdater extends FnAggregationUpdater<FnMarket, FnMarketAttributes, Quote> {

    private static final String PREPEND_SYMBOLS =
            "http://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.quotes where symbol=";
    private static final String APPEND_SYMBOLS = "&env=store://datatables.org/alltableswithkeys&format=json";

    // private static Logger LOG = Logger.getLogger(FnQuoteUpdater.class);

    public FnMarketUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final FnItemCollector ic) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, ic, ic.getProxy());
    }

    @Override
    protected FnMarket createEmptyItem(final String logicalId) {
        return new FnMarket(logicalId, this.getItemType());
    }

    @Override
    protected List<Quote> fetchData() {
        YahooResponse response =
                rt.getForObject(this.getMarketURI(PREPEND_SYMBOLS, APPEND_SYMBOLS), YahooResponse.class);
        if (response.getQuery() == null || response.getQuery().getResult() == null
                || response.getQuery().getResult().getQuote() == null) {
            // TODO: log the error
            return new ArrayList<Quote>(0);
        }
        YahooQuote yQ = response.getQuery().getResult().getQuote();
        List<Quote> quotes = new ArrayList<>(1);
        if (yQ != null) {
            quotes.add(new Quote(yQ.getName(), yQ.getSymbol(), getDouble(yQ.getLastTradePriceOnly()),
                    getDouble(yQ.getPreviousClose()), getDouble(yQ.getOpen()), getDouble(yQ.getDaysHigh()),
                    getDouble(yQ.getDaysLow()), getInteger(yQ.getVolume()), getDouble(yQ.getChange()),
                    getDouble(getPercentNumber(yQ.getPercentChange())), yQ.getCurrency()));
        }
        return quotes;
    }

    private double getDouble(final String str) {
        double db = 0.0;
        if (str == null) {
            return db;
        }
        try {
            db = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            db = 0.0;
        }
        return db;
    }

    private int getInteger(final String str) {
        int in = 0;
        if (str == null) {
            return in;
        }
        try {
            in = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            in = 0;
        }
        return in;
    }

    private String getPercentNumber(final String percentChange) {
        return percentChange.replace("%", "");
    }

    @Override
    protected FnMarketAttributes createItemAttributes(final Quote resource) {
        FnMarketAttributes attributes = new FnMarketAttributes();
        attributes.setItemId(resource.getId());
        attributes.setItemName(resource.getName());
        attributes.setItemDescription(resource.getName() + " (" + resource.getSymbol() + ")");
        attributes.setSymbol(resource.getSymbol());
        attributes.setLast(resource.getLast());
        attributes.setPrior(resource.getPrior());
        attributes.setOpen(resource.getOpen());
        attributes.setHigh(resource.getHigh());
        attributes.setLow(resource.getLow());
        attributes.setVolume(resource.getVolume());
        attributes.setChange(resource.getChange());
        attributes.setPercentChange(resource.getPercentChange());
        attributes.setStatus(resource.getStatus());
        attributes.setCurrency(resource.getCurrency());
        return attributes;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final FnMarketAttributes itemAttr, final Quote resource) {
        boolean isDifferent = false;
        if (itemAttr.getChange() != null && !itemAttr.getChange().equals(resource.getChange())) {
            isDifferent = true;
        }

        if (itemAttr.getStatus() != null && !itemAttr.getStatus().equals(resource.getStatus())) {
            isDifferent = true;
        }
        if (isDifferent) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem item, final Quote resource) {


    }
}
