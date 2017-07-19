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
// import org.apache.log4j.Logger;
import com.hp.hpl.loom.adapter.finance.items.FnMarket;
import com.hp.hpl.loom.adapter.finance.items.FnMarketAttributes;
import com.hp.hpl.loom.adapter.finance.items.FnQuote;
import com.hp.hpl.loom.adapter.finance.items.FnQuoteAttributes;
import com.hp.hpl.loom.adapter.finance.models.Quote;
import com.hp.hpl.loom.adapter.finance.models.YahooQuote;
import com.hp.hpl.loom.adapter.finance.models.YahooResponses;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class FnQuoteUpdater extends FnAggregationUpdater<FnQuote, FnQuoteAttributes, Quote> {

    private static final String PREPEND_SYMBOLS =
            "http://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.quotes where symbol in (";
    private static final String APPEND_SYMBOLS = ")&env=store://datatables.org/alltableswithkeys&format=json";

    // private static Logger LOG = Logger.getLogger(FnQuoteUpdater.class);

    public FnQuoteUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final FnItemCollector ic) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, ic, ic.getProxy());
    }

    @Override
    protected FnQuote createEmptyItem(final String logicalId) {
        return new FnQuote(logicalId, this.getItemType());
    }

    @Override
    protected List<Quote> fetchData() {
        YahooResponses response =
                rt.getForObject(this.getQuotesURI(PREPEND_SYMBOLS, APPEND_SYMBOLS), YahooResponses.class);
        if (response.getQuery() == null || response.getQuery().getResults() == null
                || response.getQuery().getResults().getQuote() == null) {
            // TODO: log the error
            return new ArrayList<Quote>(0);
        }
        List<YahooQuote> yahooQuotes = response.getQuery().getResults().getQuote();
        List<Quote> quotes = new ArrayList<>(response.getQuery().getCount());
        for (YahooQuote yQ : yahooQuotes) {

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
    protected FnQuoteAttributes createItemAttributes(final Quote resource) {
        FnQuoteAttributes fnQuoteAttributes = new FnQuoteAttributes();
        fnQuoteAttributes.setItemId(resource.getId());
        fnQuoteAttributes.setItemName(resource.getName());
        fnQuoteAttributes.setItemDescription(resource.getName() + " (" + resource.getSymbol() + ")");
        fnQuoteAttributes.setSymbol(resource.getSymbol());
        fnQuoteAttributes.setLast(resource.getLast());
        fnQuoteAttributes.setPrior(resource.getPrior());
        fnQuoteAttributes.setOpen(resource.getOpen());
        fnQuoteAttributes.setHigh(resource.getHigh());
        fnQuoteAttributes.setLow(resource.getLow());
        fnQuoteAttributes.setVolume(resource.getVolume());
        fnQuoteAttributes.setChange(resource.getChange());
        fnQuoteAttributes.setPercentChange(resource.getPercentChange());
        fnQuoteAttributes.setStatus(resource.getStatus());
        fnQuoteAttributes.setCurrency(resource.getCurrency());
        return fnQuoteAttributes;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final FnQuoteAttributes itemAttr, final Quote resource) {
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
        FnMarketAttributes marketAttr = ic.getLatestMarketAttributes(adapter.getProvider().getProviderId());
        if (marketAttr != null) {
            item.setRelationship(adapter.getProvider(), FnMarket.TYPE_LOCAL_ID, marketAttr.getItemId());
        }
        // FnMarket market = ic.getMarket(adapter.getProvider().getProviderId());
        // if (market != null) {
        // item.setRelationship(FnMarket.TYPE_LOCAL_ID, market.getCore().getItemId());
        // }
    }
}
