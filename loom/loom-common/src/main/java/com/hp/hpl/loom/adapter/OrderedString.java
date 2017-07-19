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
package com.hp.hpl.loom.adapter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Provides a list of string with an order.
 *
 */
@JsonAutoDetect
public class OrderedString implements Comparable {

    private String key;
    private int order;

    /**
     * Constructor with just a key no order.
     *
     * @param key the key
     */
    public OrderedString(final String key) {
        this.key = key;
    }

    /**
     * Constructor with just a key with an order.
     *
     * @param key the key
     * @param order the order
     */
    public OrderedString(final String key, final int order) {
        this.key = key;
        this.order = order;
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the key.
     *
     * @param key the key
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * Set the order.
     *
     * @param order the order.
     */
    public void setOrder(final int order) {
        this.order = order;
    }

    /**
     * Get the order.
     *
     * @return the order
     */
    @JsonIgnore
    public int getOrder() {
        return order;
    }

    @Override
    @JsonValue
    public String toString() {
        return key;
    }

    @Override
    public int compareTo(final Object obj) {
        if (obj == null) {
            throw new NullPointerException("Null object compared to OrderedKey " + key);
        }
        if (!(obj instanceof OrderedString)) {
            throw new ClassCastException("object compared to OrderedKey " + key + " is not an OrderedKey");
        }
        int objOrder = ((OrderedString) obj).getOrder();
        if (this.equals(obj)) {
            return 0;
        } else {
            int orderDelta = order - objOrder;
            if (orderDelta == 0) {
                orderDelta = -1;
            }
            return orderDelta;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof OrderedString)) {
            return false;
        }
        return ((OrderedString) obj).getKey().equals(key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
