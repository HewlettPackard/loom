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

import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.finance.models.Quote.Status;
import com.hp.hpl.loom.manager.query.DefaultOperations;

public class FnQuoteAttributes extends FnItemAttributes {
    // ------------------------------------------------------------------ //
    // PRIVATE FIELDS //
    // ------------------------------------------------------------------ //

    @LoomAttribute(key = "Quote Symbol", supportedOperations = {DefaultOperations.SORT_BY})
    private String symbol;

    @LoomAttribute(key = "Last Trade", type = NumericAttribute.class, min = "-Inf", max = "Inf",
            supportedOperations = {DefaultOperations.SORT_BY}, plottable = true)
    private Double last;

    @LoomAttribute(key = "Prior", type = NumericAttribute.class, min = "-Inf", max = "Inf", supportedOperations = {},
            plottable = true)
    private Double prior;

    @LoomAttribute(key = "Open", type = NumericAttribute.class, min = "-Inf", max = "Inf", supportedOperations = {},
            plottable = true)
    private Double open;

    @LoomAttribute(key = "High", type = NumericAttribute.class, min = "-Inf", max = "Inf", supportedOperations = {},
            plottable = true)
    private Double high;

    @LoomAttribute(key = "Low", type = NumericAttribute.class, min = "-Inf", max = "Inf", supportedOperations = {},
            plottable = true)
    private Double low;

    @LoomAttribute(key = "Volume", type = NumericAttribute.class, min = "-Inf", max = "Inf",
            supportedOperations = {DefaultOperations.SORT_BY}, plottable = true)
    private Integer volume;

    @LoomAttribute(key = "Change", type = NumericAttribute.class, min = "-Inf", max = "Inf",
            supportedOperations = {DefaultOperations.SORT_BY}, plottable = true)
    private Double change;

    @LoomAttribute(key = "Percent Change", type = NumericAttribute.class, min = "-Inf", max = "Inf",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.PERCENTILES}, plottable = true)
    private Double percentChange;

    @LoomAttribute(key = "Status", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private Status status;

    @LoomAttribute(key = "Currency", supportedOperations = {DefaultOperations.GROUP_BY})
    private String currency;

    // ------------------------------------------------------------------ //
    // PUBLIC INTERFACE //
    // ------------------------------------------------------------------ //
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public Double getLast() {
        return last;
    }

    public void setLast(final Double last) {
        this.last = last;
    }

    public Double getPrior() {
        return prior;
    }

    public void setPrior(final Double prior) {
        this.prior = prior;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(final Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(final Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(final Double low) {
        this.low = low;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(final Integer volume) {
        this.volume = volume;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(final Double change) {
        this.change = change;
    }

    public Double getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(final Double percentChange) {
        this.percentChange = percentChange;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }
}
