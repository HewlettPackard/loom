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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Quote implements BaseModel {

    public enum Status {
        UP, DOWN, NOCHANGE;
    }

    private String name;
    private String symbol;
    private double last;
    private double prior;
    private double open;
    private double high;
    private double low;
    private int volume;
    private double change;
    private double percentChange;
    private Status status;
    private String currency;

    public Quote() {}

    public Quote(final String name, final String symbol, final double last, final double prior, final double open,
            final double high, final double low, final int volume, final double change, final double percentChange,
            final String currency) {
        this.name = name;
        this.symbol = symbol;
        this.last = last;
        this.prior = prior;
        this.open = open;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.change = change;
        this.percentChange = percentChange;
        if (this.change > 0) {
            status = Status.UP;
        } else if (this.change < 0) {
            status = Status.DOWN;
        } else {
            status = Status.NOCHANGE;
        }
        this.currency = currency;
    }

    @Override
    public String getId() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public double getLast() {
        return last;
    }

    public void setLast(final double last) {
        this.last = last;
    }

    public double getPrior() {
        return prior;
    }

    public void setPrior(final double prior) {
        this.prior = prior;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(final double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(final double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(final double low) {
        this.low = low;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(final int volume) {
        this.volume = volume;
    }

    public double getChange() {
        return change;
    }

    public void setChange(final double change) {
        this.change = change;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(final double percentChange) {
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Quote quote = (Quote) o;
        if (symbol == null) {
            return super.equals(o);
        }
        return new EqualsBuilder().append(symbol, quote.symbol).isEquals();
    }

    @Override
    public int hashCode() {
        if (symbol == null) {
            return super.hashCode();
        }
        return new HashCodeBuilder().append(symbol).toHashCode();
    }
}
