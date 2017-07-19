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
package com.hp.hpl.loom.adapter.finance.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class YahooQuote {

    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("Ask")
    private String Ask;
    @JsonProperty("AverageDailyVolume")
    private String AverageDailyVolume;
    @JsonProperty("Bid")
    private String Bid;
    @JsonProperty("AskRealtime")
    private String AskRealtime;
    @JsonProperty("BidRealtime")
    private String BidRealtime;
    @JsonProperty("BookValue")
    private String BookValue;
    @JsonProperty("Change_PercentChange")
    private String Change_PercentChange;
    @JsonProperty("Change")
    private String Change;
    @JsonProperty("Commission")
    private String Commission;
    @JsonProperty("Currency")
    private String Currency;
    @JsonProperty("ChangeRealtime")
    private String ChangeRealtime;
    @JsonProperty("AfterHoursChangeRealtime")
    private String AfterHoursChangeRealtime;
    @JsonProperty("DividendShare")
    private String DividendShare;
    @JsonProperty("LastTradeDate")
    private String LastTradeDate;
    @JsonProperty("TradeDate")
    private String TradeDate;
    @JsonProperty("EarningsShare")
    private String EarningsShare;
    @JsonProperty("ErrorIndicationreturnedforsymbolchangedinvalid")
    private String ErrorIndicationreturnedforsymbolchangedinvalid;
    @JsonProperty("EPSEstimateCurrentYear")
    private String EPSEstimateCurrentYear;
    @JsonProperty("EPSEstimateNextYear")
    private String EPSEstimateNextYear;
    @JsonProperty("EPSEstimateNextQuarter")
    private String EPSEstimateNextQuarter;
    @JsonProperty("DaysLow")
    private String DaysLow;
    @JsonProperty("DaysHigh")
    private String DaysHigh;
    @JsonProperty("YearLow")
    private String YearLow;
    @JsonProperty("YearHigh")
    private String YearHigh;
    @JsonProperty("HoldingsGainPercent")
    private String HoldingsGainPercent;
    @JsonProperty("AnnualizedGain")
    private String AnnualizedGain;
    @JsonProperty("HoldingsGain")
    private String HoldingsGain;
    @JsonProperty("HoldingsGainPercentRealtime")
    private String HoldingsGainPercentRealtime;
    @JsonProperty("HoldingsGainRealtime")
    private String HoldingsGainRealtime;
    @JsonProperty("MoreInfo")
    private String MoreInfo;
    @JsonProperty("OrderBookRealtime")
    private String OrderBookRealtime;
    @JsonProperty("MarketCapitalization")
    private String MarketCapitalization;
    @JsonProperty("MarketCapRealtime")
    private String MarketCapRealtime;
    @JsonProperty("EBITDA")
    private String EBITDA;
    @JsonProperty("ChangeFromYearLow")
    private String ChangeFromYearLow;
    @JsonProperty("PercentChangeFromYearLow")
    private String PercentChangeFromYearLow;
    @JsonProperty("LastTradeRealtimeWithTime")
    private String LastTradeRealtimeWithTime;
    @JsonProperty("ChangePercentRealtime")
    private String ChangePercentRealtime;
    @JsonProperty("ChangeFromYearHigh")
    private String ChangeFromYearHigh;
    @JsonProperty("PercebtChangeFromYearHigh")
    private String PercebtChangeFromYearHigh; // TODO: wrong in YAHOO's API
    @JsonProperty("LastTradeWithTime")
    private String LastTradeWithTime;
    @JsonProperty("LastTradePriceOnly")
    private String LastTradePriceOnly;
    @JsonProperty("HighLimit")
    private String HighLimit;
    @JsonProperty("LowLimit")
    private String LowLimit;
    @JsonProperty("DaysRange")
    private String DaysRange;
    @JsonProperty("DaysRangeRealtime")
    private String DaysRangeRealtime;
    @JsonProperty("FiftydayMovingAverage")
    private String FiftydayMovingAverage;
    @JsonProperty("TwoHundreddayMovingAverage")
    private String TwoHundreddayMovingAverage;
    @JsonProperty("ChangeFromTwoHundreddayMovingAverage")
    private String ChangeFromTwoHundreddayMovingAverage;
    @JsonProperty("PercentChangeFromTwoHundreddayMovingAverage")
    private String PercentChangeFromTwoHundreddayMovingAverage;
    @JsonProperty("ChangeFromFiftydayMovingAverage")
    private String ChangeFromFiftydayMovingAverage;
    @JsonProperty("PercentChangeFromFiftydayMovingAverage")
    private String PercentChangeFromFiftydayMovingAverage;
    @JsonProperty("Name")
    private String Name;
    @JsonProperty("Notes")
    private String Notes;
    @JsonProperty("Open")
    private String Open;
    @JsonProperty("PreviousClose")
    private String PreviousClose;
    @JsonProperty("PricePaid")
    private String PricePaid;
    @JsonProperty("ChangeinPercent")
    private String ChangeinPercent;
    @JsonProperty("PriceSales")
    private String PriceSales;
    @JsonProperty("PriceBook")
    private String PriceBook;
    @JsonProperty("ExDividendDate")
    private String ExDividendDate;
    @JsonProperty("PERatio")
    private String PERatio;
    @JsonProperty("DividendPayDate")
    private String DividendPayDate;
    @JsonProperty("PERatioRealtime")
    private String PERatioRealtime;
    @JsonProperty("PEGRatio")
    private String PEGRatio;
    @JsonProperty("PriceEPSEstimateCurrentYear")
    private String PriceEPSEstimateCurrentYear;
    @JsonProperty("PriceEPSEstimateNextYear")
    private String PriceEPSEstimateNextYear;
    @JsonProperty("Symbol")
    private String symbol2;
    @JsonProperty("SharesOwned")
    private String SharesOwned;
    @JsonProperty("ShortRatio")
    private String ShortRatio;
    @JsonProperty("LastTradeTime")
    private String LastTradeTime;
    @JsonProperty("TickerTrend")
    private String TickerTrend;
    @JsonProperty("OneyrTargetPrice")
    private String OneyrTargetPrice;
    @JsonProperty("Volume")
    private String Volume;
    @JsonProperty("HoldingsValue")
    private String HoldingsValue;
    @JsonProperty("HoldingsValueRealtime")
    private String HoldingsValueRealtime;
    @JsonProperty("YearRange")
    private String YearRange;
    @JsonProperty("DaysValueChange")
    private String DaysValueChange;
    @JsonProperty("DaysValueChangeRealtime")
    private String DaysValueChangeRealtime;
    @JsonProperty("StockExchange")
    private String StockExchange;
    @JsonProperty("DividendYield")
    private String DividendYield;
    @JsonProperty("PercentChange")
    private String PercentChange;

    public YahooQuote() {}


    @JsonProperty("Symbol")
    public String getSymbol2() {
        return symbol2;
    }

    @JsonProperty("Symbol")
    public void setSymbol2(final String symbol2) {
        this.symbol2 = symbol2;
    }


    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }


    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }


    /**
     * @return the ask
     */
    public String getAsk() {
        return Ask;
    }


    /**
     * @param ask the ask to set
     */
    public void setAsk(final String ask) {
        Ask = ask;
    }


    /**
     * @return the averageDailyVolume
     */
    public String getAverageDailyVolume() {
        return AverageDailyVolume;
    }


    /**
     * @param averageDailyVolume the averageDailyVolume to set
     */
    public void setAverageDailyVolume(final String averageDailyVolume) {
        AverageDailyVolume = averageDailyVolume;
    }


    /**
     * @return the bid
     */
    public String getBid() {
        return Bid;
    }


    /**
     * @param bid the bid to set
     */
    public void setBid(final String bid) {
        Bid = bid;
    }


    /**
     * @return the askRealtime
     */
    public String getAskRealtime() {
        return AskRealtime;
    }


    /**
     * @param askRealtime the askRealtime to set
     */
    public void setAskRealtime(final String askRealtime) {
        AskRealtime = askRealtime;
    }


    /**
     * @return the bidRealtime
     */
    public String getBidRealtime() {
        return BidRealtime;
    }


    /**
     * @param bidRealtime the bidRealtime to set
     */
    public void setBidRealtime(final String bidRealtime) {
        BidRealtime = bidRealtime;
    }


    /**
     * @return the bookValue
     */
    public String getBookValue() {
        return BookValue;
    }


    /**
     * @param bookValue the bookValue to set
     */
    public void setBookValue(final String bookValue) {
        BookValue = bookValue;
    }


    /**
     * @return the change_PercentChange
     */
    public String getChange_PercentChange() {
        return Change_PercentChange;
    }


    /**
     * @param change_PercentChange the change_PercentChange to set
     */
    public void setChange_PercentChange(final String change_PercentChange) {
        Change_PercentChange = change_PercentChange;
    }


    /**
     * @return the change
     */
    public String getChange() {
        return Change;
    }


    /**
     * @param change the change to set
     */
    public void setChange(final String change) {
        Change = change;
    }


    /**
     * @return the commission
     */
    public String getCommission() {
        return Commission;
    }


    /**
     * @param commission the commission to set
     */
    public void setCommission(final String commission) {
        Commission = commission;
    }


    /**
     * @return the currency
     */
    public String getCurrency() {
        return Currency;
    }


    /**
     * @param currency the currency to set
     */
    public void setCurrency(final String currency) {
        Currency = currency;
    }


    /**
     * @return the changeRealtime
     */
    public String getChangeRealtime() {
        return ChangeRealtime;
    }


    /**
     * @param changeRealtime the changeRealtime to set
     */
    public void setChangeRealtime(final String changeRealtime) {
        ChangeRealtime = changeRealtime;
    }


    /**
     * @return the afterHoursChangeRealtime
     */
    public String getAfterHoursChangeRealtime() {
        return AfterHoursChangeRealtime;
    }


    /**
     * @param afterHoursChangeRealtime the afterHoursChangeRealtime to set
     */
    public void setAfterHoursChangeRealtime(final String afterHoursChangeRealtime) {
        AfterHoursChangeRealtime = afterHoursChangeRealtime;
    }


    /**
     * @return the dividendShare
     */
    public String getDividendShare() {
        return DividendShare;
    }


    /**
     * @param dividendShare the dividendShare to set
     */
    public void setDividendShare(final String dividendShare) {
        DividendShare = dividendShare;
    }


    /**
     * @return the lastTradeDate
     */
    public String getLastTradeDate() {
        return LastTradeDate;
    }


    /**
     * @param lastTradeDate the lastTradeDate to set
     */
    public void setLastTradeDate(final String lastTradeDate) {
        LastTradeDate = lastTradeDate;
    }


    /**
     * @return the tradeDate
     */
    public String getTradeDate() {
        return TradeDate;
    }


    /**
     * @param tradeDate the tradeDate to set
     */
    public void setTradeDate(final String tradeDate) {
        TradeDate = tradeDate;
    }


    /**
     * @return the earningsShare
     */
    public String getEarningsShare() {
        return EarningsShare;
    }


    /**
     * @param earningsShare the earningsShare to set
     */
    public void setEarningsShare(final String earningsShare) {
        EarningsShare = earningsShare;
    }


    /**
     * @return the errorIndicationreturnedforsymbolchangedinvalid
     */
    public String getErrorIndicationreturnedforsymbolchangedinvalid() {
        return ErrorIndicationreturnedforsymbolchangedinvalid;
    }


    /**
     * @param errorIndicationreturnedforsymbolchangedinvalid the
     *        errorIndicationreturnedforsymbolchangedinvalid to set
     */
    public void setErrorIndicationreturnedforsymbolchangedinvalid(
            final String errorIndicationreturnedforsymbolchangedinvalid) {
        ErrorIndicationreturnedforsymbolchangedinvalid = errorIndicationreturnedforsymbolchangedinvalid;
    }


    /**
     * @return the ePSEstimateCurrentYear
     */
    public String getEPSEstimateCurrentYear() {
        return EPSEstimateCurrentYear;
    }


    /**
     * @param ePSEstimateCurrentYear the ePSEstimateCurrentYear to set
     */
    public void setEPSEstimateCurrentYear(final String ePSEstimateCurrentYear) {
        EPSEstimateCurrentYear = ePSEstimateCurrentYear;
    }


    /**
     * @return the ePSEstimateNextYear
     */
    public String getEPSEstimateNextYear() {
        return EPSEstimateNextYear;
    }


    /**
     * @param ePSEstimateNextYear the ePSEstimateNextYear to set
     */
    public void setEPSEstimateNextYear(final String ePSEstimateNextYear) {
        EPSEstimateNextYear = ePSEstimateNextYear;
    }


    /**
     * @return the ePSEstimateNextQuarter
     */
    public String getEPSEstimateNextQuarter() {
        return EPSEstimateNextQuarter;
    }


    /**
     * @param ePSEstimateNextQuarter the ePSEstimateNextQuarter to set
     */
    public void setEPSEstimateNextQuarter(final String ePSEstimateNextQuarter) {
        EPSEstimateNextQuarter = ePSEstimateNextQuarter;
    }


    /**
     * @return the daysLow
     */
    public String getDaysLow() {
        return DaysLow;
    }


    /**
     * @param daysLow the daysLow to set
     */
    public void setDaysLow(final String daysLow) {
        DaysLow = daysLow;
    }


    /**
     * @return the daysHigh
     */
    public String getDaysHigh() {
        return DaysHigh;
    }


    /**
     * @param daysHigh the daysHigh to set
     */
    public void setDaysHigh(final String daysHigh) {
        DaysHigh = daysHigh;
    }


    /**
     * @return the yearLow
     */
    public String getYearLow() {
        return YearLow;
    }


    /**
     * @param yearLow the yearLow to set
     */
    public void setYearLow(final String yearLow) {
        YearLow = yearLow;
    }


    /**
     * @return the yearHigh
     */
    public String getYearHigh() {
        return YearHigh;
    }


    /**
     * @param yearHigh the yearHigh to set
     */
    public void setYearHigh(final String yearHigh) {
        YearHigh = yearHigh;
    }


    /**
     * @return the holdingsGainPercent
     */
    public String getHoldingsGainPercent() {
        return HoldingsGainPercent;
    }


    /**
     * @param holdingsGainPercent the holdingsGainPercent to set
     */
    public void setHoldingsGainPercent(final String holdingsGainPercent) {
        HoldingsGainPercent = holdingsGainPercent;
    }


    /**
     * @return the annualizedGain
     */
    public String getAnnualizedGain() {
        return AnnualizedGain;
    }


    /**
     * @param annualizedGain the annualizedGain to set
     */
    public void setAnnualizedGain(final String annualizedGain) {
        AnnualizedGain = annualizedGain;
    }


    /**
     * @return the holdingsGain
     */
    public String getHoldingsGain() {
        return HoldingsGain;
    }


    /**
     * @param holdingsGain the holdingsGain to set
     */
    public void setHoldingsGain(final String holdingsGain) {
        HoldingsGain = holdingsGain;
    }


    /**
     * @return the holdingsGainPercentRealtime
     */
    public String getHoldingsGainPercentRealtime() {
        return HoldingsGainPercentRealtime;
    }


    /**
     * @param holdingsGainPercentRealtime the holdingsGainPercentRealtime to set
     */
    public void setHoldingsGainPercentRealtime(final String holdingsGainPercentRealtime) {
        HoldingsGainPercentRealtime = holdingsGainPercentRealtime;
    }


    /**
     * @return the holdingsGainRealtime
     */
    public String getHoldingsGainRealtime() {
        return HoldingsGainRealtime;
    }


    /**
     * @param holdingsGainRealtime the holdingsGainRealtime to set
     */
    public void setHoldingsGainRealtime(final String holdingsGainRealtime) {
        HoldingsGainRealtime = holdingsGainRealtime;
    }


    /**
     * @return the moreInfo
     */
    public String getMoreInfo() {
        return MoreInfo;
    }


    /**
     * @param moreInfo the moreInfo to set
     */
    public void setMoreInfo(final String moreInfo) {
        MoreInfo = moreInfo;
    }


    /**
     * @return the orderBookRealtime
     */
    public String getOrderBookRealtime() {
        return OrderBookRealtime;
    }


    /**
     * @param orderBookRealtime the orderBookRealtime to set
     */
    public void setOrderBookRealtime(final String orderBookRealtime) {
        OrderBookRealtime = orderBookRealtime;
    }


    /**
     * @return the marketCapitalization
     */
    public String getMarketCapitalization() {
        return MarketCapitalization;
    }


    /**
     * @param marketCapitalization the marketCapitalization to set
     */
    public void setMarketCapitalization(final String marketCapitalization) {
        MarketCapitalization = marketCapitalization;
    }


    /**
     * @return the marketCapRealtime
     */
    public String getMarketCapRealtime() {
        return MarketCapRealtime;
    }


    /**
     * @param marketCapRealtime the marketCapRealtime to set
     */
    public void setMarketCapRealtime(final String marketCapRealtime) {
        MarketCapRealtime = marketCapRealtime;
    }


    /**
     * @return the eBITDA
     */
    public String getEBITDA() {
        return EBITDA;
    }


    /**
     * @param eBITDA the eBITDA to set
     */
    public void setEBITDA(final String eBITDA) {
        EBITDA = eBITDA;
    }


    /**
     * @return the changeFromYearLow
     */
    public String getChangeFromYearLow() {
        return ChangeFromYearLow;
    }


    /**
     * @param changeFromYearLow the changeFromYearLow to set
     */
    public void setChangeFromYearLow(final String changeFromYearLow) {
        ChangeFromYearLow = changeFromYearLow;
    }


    /**
     * @return the percentChangeFromYearLow
     */
    public String getPercentChangeFromYearLow() {
        return PercentChangeFromYearLow;
    }


    /**
     * @param percentChangeFromYearLow the percentChangeFromYearLow to set
     */
    public void setPercentChangeFromYearLow(final String percentChangeFromYearLow) {
        PercentChangeFromYearLow = percentChangeFromYearLow;
    }


    /**
     * @return the lastTradeRealtimeWithTime
     */
    public String getLastTradeRealtimeWithTime() {
        return LastTradeRealtimeWithTime;
    }


    /**
     * @param lastTradeRealtimeWithTime the lastTradeRealtimeWithTime to set
     */
    public void setLastTradeRealtimeWithTime(final String lastTradeRealtimeWithTime) {
        LastTradeRealtimeWithTime = lastTradeRealtimeWithTime;
    }


    /**
     * @return the changePercentRealtime
     */
    public String getChangePercentRealtime() {
        return ChangePercentRealtime;
    }


    /**
     * @param changePercentRealtime the changePercentRealtime to set
     */
    public void setChangePercentRealtime(final String changePercentRealtime) {
        ChangePercentRealtime = changePercentRealtime;
    }


    /**
     * @return the changeFromYearHigh
     */
    public String getChangeFromYearHigh() {
        return ChangeFromYearHigh;
    }


    /**
     * @param changeFromYearHigh the changeFromYearHigh to set
     */
    public void setChangeFromYearHigh(final String changeFromYearHigh) {
        ChangeFromYearHigh = changeFromYearHigh;
    }


    /**
     * @return the percebtChangeFromYearHigh
     */
    public String getPercebtChangeFromYearHigh() {
        return PercebtChangeFromYearHigh;
    }


    /**
     * @param percebtChangeFromYearHigh the percebtChangeFromYearHigh to set
     */
    public void setPercebtChangeFromYearHigh(final String percebtChangeFromYearHigh) {
        PercebtChangeFromYearHigh = percebtChangeFromYearHigh;
    }


    /**
     * @return the lastTradeWithTime
     */
    public String getLastTradeWithTime() {
        return LastTradeWithTime;
    }


    /**
     * @param lastTradeWithTime the lastTradeWithTime to set
     */
    public void setLastTradeWithTime(final String lastTradeWithTime) {
        LastTradeWithTime = lastTradeWithTime;
    }


    /**
     * @return the lastTradePriceOnly
     */
    public String getLastTradePriceOnly() {
        return LastTradePriceOnly;
    }


    /**
     * @param lastTradePriceOnly the lastTradePriceOnly to set
     */
    public void setLastTradePriceOnly(final String lastTradePriceOnly) {
        LastTradePriceOnly = lastTradePriceOnly;
    }


    /**
     * @return the highLimit
     */
    public String getHighLimit() {
        return HighLimit;
    }


    /**
     * @param highLimit the highLimit to set
     */
    public void setHighLimit(final String highLimit) {
        HighLimit = highLimit;
    }


    /**
     * @return the lowLimit
     */
    public String getLowLimit() {
        return LowLimit;
    }


    /**
     * @param lowLimit the lowLimit to set
     */
    public void setLowLimit(final String lowLimit) {
        LowLimit = lowLimit;
    }


    /**
     * @return the daysRange
     */
    public String getDaysRange() {
        return DaysRange;
    }


    /**
     * @param daysRange the daysRange to set
     */
    public void setDaysRange(final String daysRange) {
        DaysRange = daysRange;
    }


    /**
     * @return the daysRangeRealtime
     */
    public String getDaysRangeRealtime() {
        return DaysRangeRealtime;
    }


    /**
     * @param daysRangeRealtime the daysRangeRealtime to set
     */
    public void setDaysRangeRealtime(final String daysRangeRealtime) {
        DaysRangeRealtime = daysRangeRealtime;
    }


    /**
     * @return the fiftydayMovingAverage
     */
    public String getFiftydayMovingAverage() {
        return FiftydayMovingAverage;
    }


    /**
     * @param fiftydayMovingAverage the fiftydayMovingAverage to set
     */
    public void setFiftydayMovingAverage(final String fiftydayMovingAverage) {
        FiftydayMovingAverage = fiftydayMovingAverage;
    }


    /**
     * @return the twoHundreddayMovingAverage
     */
    public String getTwoHundreddayMovingAverage() {
        return TwoHundreddayMovingAverage;
    }


    /**
     * @param twoHundreddayMovingAverage the twoHundreddayMovingAverage to set
     */
    public void setTwoHundreddayMovingAverage(final String twoHundreddayMovingAverage) {
        TwoHundreddayMovingAverage = twoHundreddayMovingAverage;
    }


    /**
     * @return the changeFromTwoHundreddayMovingAverage
     */
    public String getChangeFromTwoHundreddayMovingAverage() {
        return ChangeFromTwoHundreddayMovingAverage;
    }


    /**
     * @param changeFromTwoHundreddayMovingAverage the changeFromTwoHundreddayMovingAverage to set
     */
    public void setChangeFromTwoHundreddayMovingAverage(final String changeFromTwoHundreddayMovingAverage) {
        ChangeFromTwoHundreddayMovingAverage = changeFromTwoHundreddayMovingAverage;
    }


    /**
     * @return the percentChangeFromTwoHundreddayMovingAverage
     */
    public String getPercentChangeFromTwoHundreddayMovingAverage() {
        return PercentChangeFromTwoHundreddayMovingAverage;
    }


    /**
     * @param percentChangeFromTwoHundreddayMovingAverage the
     *        percentChangeFromTwoHundreddayMovingAverage to set
     */
    public void setPercentChangeFromTwoHundreddayMovingAverage(
            final String percentChangeFromTwoHundreddayMovingAverage) {
        PercentChangeFromTwoHundreddayMovingAverage = percentChangeFromTwoHundreddayMovingAverage;
    }


    /**
     * @return the changeFromFiftydayMovingAverage
     */
    public String getChangeFromFiftydayMovingAverage() {
        return ChangeFromFiftydayMovingAverage;
    }


    /**
     * @param changeFromFiftydayMovingAverage the changeFromFiftydayMovingAverage to set
     */
    public void setChangeFromFiftydayMovingAverage(final String changeFromFiftydayMovingAverage) {
        ChangeFromFiftydayMovingAverage = changeFromFiftydayMovingAverage;
    }


    /**
     * @return the percentChangeFromFiftydayMovingAverage
     */
    public String getPercentChangeFromFiftydayMovingAverage() {
        return PercentChangeFromFiftydayMovingAverage;
    }


    /**
     * @param percentChangeFromFiftydayMovingAverage the percentChangeFromFiftydayMovingAverage to
     *        set
     */
    public void setPercentChangeFromFiftydayMovingAverage(final String percentChangeFromFiftydayMovingAverage) {
        PercentChangeFromFiftydayMovingAverage = percentChangeFromFiftydayMovingAverage;
    }


    /**
     * @return the name
     */
    public String getName() {
        return Name;
    }


    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        Name = name;
    }


    /**
     * @return the notes
     */
    public String getNotes() {
        return Notes;
    }


    /**
     * @param notes the notes to set
     */
    public void setNotes(final String notes) {
        Notes = notes;
    }


    /**
     * @return the open
     */
    public String getOpen() {
        return Open;
    }


    /**
     * @param open the open to set
     */
    public void setOpen(final String open) {
        Open = open;
    }


    /**
     * @return the previousClose
     */
    public String getPreviousClose() {
        return PreviousClose;
    }


    /**
     * @param previousClose the previousClose to set
     */
    public void setPreviousClose(final String previousClose) {
        PreviousClose = previousClose;
    }


    /**
     * @return the pricePaid
     */
    public String getPricePaid() {
        return PricePaid;
    }


    /**
     * @param pricePaid the pricePaid to set
     */
    public void setPricePaid(final String pricePaid) {
        PricePaid = pricePaid;
    }


    /**
     * @return the changeinPercent
     */
    public String getChangeinPercent() {
        return ChangeinPercent;
    }


    /**
     * @param changeinPercent the changeinPercent to set
     */
    public void setChangeinPercent(final String changeinPercent) {
        ChangeinPercent = changeinPercent;
    }


    /**
     * @return the priceSales
     */
    public String getPriceSales() {
        return PriceSales;
    }


    /**
     * @param priceSales the priceSales to set
     */
    public void setPriceSales(final String priceSales) {
        PriceSales = priceSales;
    }


    /**
     * @return the priceBook
     */
    public String getPriceBook() {
        return PriceBook;
    }


    /**
     * @param priceBook the priceBook to set
     */
    public void setPriceBook(final String priceBook) {
        PriceBook = priceBook;
    }


    /**
     * @return the exDividendDate
     */
    public String getExDividendDate() {
        return ExDividendDate;
    }


    /**
     * @param exDividendDate the exDividendDate to set
     */
    public void setExDividendDate(final String exDividendDate) {
        ExDividendDate = exDividendDate;
    }


    /**
     * @return the pERatio
     */
    public String getPERatio() {
        return PERatio;
    }


    /**
     * @param pERatio the pERatio to set
     */
    public void setPERatio(final String pERatio) {
        PERatio = pERatio;
    }


    /**
     * @return the dividendPayDate
     */
    public String getDividendPayDate() {
        return DividendPayDate;
    }


    /**
     * @param dividendPayDate the dividendPayDate to set
     */
    public void setDividendPayDate(final String dividendPayDate) {
        DividendPayDate = dividendPayDate;
    }


    /**
     * @return the pERatioRealtime
     */
    public String getPERatioRealtime() {
        return PERatioRealtime;
    }


    /**
     * @param pERatioRealtime the pERatioRealtime to set
     */
    public void setPERatioRealtime(final String pERatioRealtime) {
        PERatioRealtime = pERatioRealtime;
    }


    /**
     * @return the pEGRatio
     */
    public String getPEGRatio() {
        return PEGRatio;
    }


    /**
     * @param pEGRatio the pEGRatio to set
     */
    public void setPEGRatio(final String pEGRatio) {
        PEGRatio = pEGRatio;
    }


    /**
     * @return the priceEPSEstimateCurrentYear
     */
    public String getPriceEPSEstimateCurrentYear() {
        return PriceEPSEstimateCurrentYear;
    }


    /**
     * @param priceEPSEstimateCurrentYear the priceEPSEstimateCurrentYear to set
     */
    public void setPriceEPSEstimateCurrentYear(final String priceEPSEstimateCurrentYear) {
        PriceEPSEstimateCurrentYear = priceEPSEstimateCurrentYear;
    }


    /**
     * @return the priceEPSEstimateNextYear
     */
    public String getPriceEPSEstimateNextYear() {
        return PriceEPSEstimateNextYear;
    }


    /**
     * @param priceEPSEstimateNextYear the priceEPSEstimateNextYear to set
     */
    public void setPriceEPSEstimateNextYear(final String priceEPSEstimateNextYear) {
        PriceEPSEstimateNextYear = priceEPSEstimateNextYear;
    }


    /**
     * @return the sharesOwned
     */
    public String getSharesOwned() {
        return SharesOwned;
    }


    /**
     * @param sharesOwned the sharesOwned to set
     */
    public void setSharesOwned(final String sharesOwned) {
        SharesOwned = sharesOwned;
    }


    /**
     * @return the shortRatio
     */
    public String getShortRatio() {
        return ShortRatio;
    }


    /**
     * @param shortRatio the shortRatio to set
     */
    public void setShortRatio(final String shortRatio) {
        ShortRatio = shortRatio;
    }


    /**
     * @return the lastTradeTime
     */
    public String getLastTradeTime() {
        return LastTradeTime;
    }


    /**
     * @param lastTradeTime the lastTradeTime to set
     */
    public void setLastTradeTime(final String lastTradeTime) {
        LastTradeTime = lastTradeTime;
    }


    /**
     * @return the tickerTrend
     */
    public String getTickerTrend() {
        return TickerTrend;
    }


    /**
     * @param tickerTrend the tickerTrend to set
     */
    public void setTickerTrend(final String tickerTrend) {
        TickerTrend = tickerTrend;
    }


    /**
     * @return the oneyrTargetPrice
     */
    public String getOneyrTargetPrice() {
        return OneyrTargetPrice;
    }


    /**
     * @param oneyrTargetPrice the oneyrTargetPrice to set
     */
    public void setOneyrTargetPrice(final String oneyrTargetPrice) {
        OneyrTargetPrice = oneyrTargetPrice;
    }


    /**
     * @return the volume
     */
    public String getVolume() {
        return Volume;
    }


    /**
     * @param volume the volume to set
     */
    public void setVolume(final String volume) {
        Volume = volume;
    }


    /**
     * @return the holdingsValue
     */
    public String getHoldingsValue() {
        return HoldingsValue;
    }


    /**
     * @param holdingsValue the holdingsValue to set
     */
    public void setHoldingsValue(final String holdingsValue) {
        HoldingsValue = holdingsValue;
    }


    /**
     * @return the holdingsValueRealtime
     */
    public String getHoldingsValueRealtime() {
        return HoldingsValueRealtime;
    }


    /**
     * @param holdingsValueRealtime the holdingsValueRealtime to set
     */
    public void setHoldingsValueRealtime(final String holdingsValueRealtime) {
        HoldingsValueRealtime = holdingsValueRealtime;
    }


    /**
     * @return the yearRange
     */
    public String getYearRange() {
        return YearRange;
    }


    /**
     * @param yearRange the yearRange to set
     */
    public void setYearRange(final String yearRange) {
        YearRange = yearRange;
    }


    /**
     * @return the daysValueChange
     */
    public String getDaysValueChange() {
        return DaysValueChange;
    }


    /**
     * @param daysValueChange the daysValueChange to set
     */
    public void setDaysValueChange(final String daysValueChange) {
        DaysValueChange = daysValueChange;
    }


    /**
     * @return the daysValueChangeRealtime
     */
    public String getDaysValueChangeRealtime() {
        return DaysValueChangeRealtime;
    }


    /**
     * @param daysValueChangeRealtime the daysValueChangeRealtime to set
     */
    public void setDaysValueChangeRealtime(final String daysValueChangeRealtime) {
        DaysValueChangeRealtime = daysValueChangeRealtime;
    }


    /**
     * @return the stockExchange
     */
    public String getStockExchange() {
        return StockExchange;
    }


    /**
     * @param stockExchange the stockExchange to set
     */
    public void setStockExchange(final String stockExchange) {
        StockExchange = stockExchange;
    }


    /**
     * @return the dividendYield
     */
    public String getDividendYield() {
        return DividendYield;
    }


    /**
     * @param dividendYield the dividendYield to set
     */
    public void setDividendYield(final String dividendYield) {
        DividendYield = dividendYield;
    }


    /**
     * @return the percentChange
     */
    public String getPercentChange() {
        return PercentChange;
    }


    /**
     * @param percentChange the percentChange to set
     */
    public void setPercentChange(final String percentChange) {
        PercentChange = percentChange;
    }
}
