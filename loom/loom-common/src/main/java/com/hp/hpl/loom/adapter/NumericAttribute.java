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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.exceptions.AttributeException;

/**
 * Class to model the Numeric attributes.
 */
public class NumericAttribute extends Attribute {

    private static final Log LOG = LogFactory.getLog(NumericAttribute.class);

    protected NumericAttribute(final Builder builder) {
        super(builder);
    }

    /**
     * Get the min for this attributes.
     *
     * @return the min
     */
    public String getMin() {
        return (String) displayMap.get("min");
    }

    /**
     * Get the max for this attribute.
     *
     * @return the max
     */
    public String getMax() {
        return (String) displayMap.get("max");
    }

    /**
     * Get the unit for the NumericAttribute.
     *
     * @return the unit
     */
    public String getUnit() {
        return (String) displayMap.get("unit");
    }

    /**
     * A builder for the {@link NumericAttribute}.
     */
    public static class Builder extends Attribute.Builder {
        private String fieldName;

        /**
         * Constructor for the {@link NumericAttribute} using the fieldName.
         *
         * @param fieldName the field to construct for
         */
        public Builder(final String fieldName) {
            super(fieldName);
            this.fieldName = fieldName;
            displayMap.put("type", Attribute.TYPE_NUMERIC);
        }

        @Override
        public NumericAttribute build() throws AttributeException {
            String minStr = (String) displayMap.get("min");
            String maxStr = (String) displayMap.get("max");

            if (StringUtils.isEmpty(minStr)) {
                throw new AttributeException("Min is missing for field name: " + fieldName);
            } else if (StringUtils.isEmpty(maxStr)) {
                throw new AttributeException("Max is missing for field name: " + fieldName);
            }

            // check the min - max make sense - namely both are numbers (or Inf)
            // and min is

            // than max
            if (!validateIsNumber(minStr)) {
                throw new AttributeException("Min isn't a valid number: " + minStr + " for field name: " + fieldName);
            } else if (!validateIsNumber(maxStr)) {
                throw new AttributeException("Max isn't a valid number: " + maxStr + " for field name: " + fieldName);
            }

            if (!validateMaxGreaterMin(minStr, maxStr)) {
                throw new AttributeException("Max is less than min for field name: " + fieldName);
            }

            return new NumericAttribute(this);
        }

        /**
         * Validates if it is a numebr or Inf.
         *
         * @param number
         */
        private boolean validateIsNumber(final String number) {
            boolean valid = false;
            if (number.equalsIgnoreCase("Inf") || number.equalsIgnoreCase("-Inf")) {
                valid = true;
            } else {
                try {
                    Long.valueOf(number);
                    valid = true;
                } catch (NumberFormatException ex) {
                    try {
                        Double.valueOf(number);
                        valid = true;
                    } catch (NumberFormatException ex2) {
                        valid = false; // not really required but it gets avoids
                        // a PMD error
                    }
                }
            }
            return valid;
        }

        private boolean validateMaxGreaterMin(final String minStr, final String maxStr) {
            Number min = null;
            if (!"-Inf".equalsIgnoreCase(minStr)) {
                try {
                    min = Long.valueOf(minStr);
                } catch (NumberFormatException ex) {
                    try {
                        min = Double.valueOf(minStr);
                    } catch (NumberFormatException ex2) {
                        LOG.warn("Could not convert the minimum \"" + minStr
                                + "\" to a right value. Setting MIN VALUE by default");
                        min = Double.MIN_VALUE; // not really required but
                        // it gets avoids a PMD
                        // error
                    }
                }
            } else {
                min = Double.MIN_VALUE;
            }
            Number max = null;
            if (!"Inf".equalsIgnoreCase(maxStr)) {
                try {
                    max = Long.valueOf(maxStr);
                } catch (NumberFormatException ex) {
                    try {
                        max = Double.valueOf(maxStr);
                    } catch (NumberFormatException ex2) {
                        LOG.warn("Could not convert the maximum \"" + maxStr
                                + "\" to a right value. Setting MAX VALUE by default");
                        max = Double.MAX_VALUE; // not really required but it
                        // gets avoids a PMD error
                    }
                }
            } else {
                max = Double.MAX_VALUE;
            }

            return max.doubleValue() > min.doubleValue();
        }

        /**
         * Set the min value.
         *
         * @param min the min value
         * @return the Builder
         */
        public Builder min(final String min) {
            if (min != null) {
                displayMap.put("min", min);
            }
            return this;
        }

        /**
         * Set the max value.
         *
         * @param max the max value
         * @return the Builder
         */
        public Builder max(final String max) {
            if (max != null) {
                displayMap.put("max", max);
            }
            return this;
        }

        /**
         * Set the unit value.
         *
         * @param unit the unit value
         * @return the Builder
         */
        public Builder unit(final String unit) {
            if (unit != null) {
                displayMap.put("unit", unit);
            }
            return this;
        }

        @Override
        public Builder name(final String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder visible(final boolean visible) {
            super.visible(visible);
            return this;
        }

        @Override
        public Builder plottable(final boolean plottable) {
            super.plottable(plottable);
            return this;
        }

        @Override
        public Builder type(final String type) {
            super.type(type);
            return this;
        }

        @Override
        public Builder mappable(final boolean mappable) {
            super.mappable(mappable);
            return this;
        }

        @Override
        public Builder collectionType(final CollectionType collectionType) {
            super.collectionType(collectionType);
            return this;
        }
    }

}
