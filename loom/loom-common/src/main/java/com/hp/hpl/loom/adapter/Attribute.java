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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.hpl.loom.exceptions.AttributeException;

/**
 * An item attribute class.
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Attribute {
    /**
     * Numeric type.
     */
    public static final String TYPE_NUMERIC = "numeric";
    /**
     * Literal type.
     */
    public static final String TYPE_LITERAL = "literal";
    /**
     * Geo type.
     */
    public static final String TYPE_GEO = "geo";

    /**
     * Time type.
     */
    public static final String TYPE_TIME = "time";

    private String fieldName;
    protected Map<String, Object> displayMap;

    protected Attribute(final Builder builder) {
        fieldName = builder.fieldName;
        displayMap = builder.displayMap;
    }

    /**
     * Get the fieldName.
     *
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return (String) displayMap.get("name");
    }

    /**
     * Get the visible flag.
     *
     * @return true if visible
     */
    public boolean getVisible() {
        return (Boolean) displayMap.get("visible");
    }

    /**
     * Get the plottable flag.
     *
     * @return true if plottable
     */
    public boolean getPlottable() {
        return (Boolean) displayMap.get("plottable");
    }

    /**
     * Get the mappable flag.
     *
     * @return true if mappable
     */
    public boolean getMappable() {
        return (Boolean) displayMap.get("mappable");
    }

    /**
     * Get the display values map.
     *
     * @return the display values map
     */
    public Map<String, Object> getDisplayValues() {
        return displayMap;
    }

    /**
     * Get the attribute type.
     *
     * @return the attribute type
     */
    public String getType() {
        return (String) displayMap.get("type");
    }

    /**
     * Get the collection type.
     *
     * @return the collectionType
     */
    public String getCollectionType() {
        return (String) displayMap.get("collectionType");
    }

    /**
     * Get the ignoreUpdate flag.
     *
     * @return true if ignoreUpdate
     */
    public boolean getIgnoreUpdate() {
        return (Boolean) displayMap.get("ignoreUpdate");
    }


    /**
     * Builder for the {@link Attribute}.
     */
    public static class Builder {
        private String fieldName;
        protected Map<String, Object> displayMap = new HashMap<String, Object>();

        /**
         * Builds the {@link Attribute} based on the calls to this Builder.
         *
         * @return a constructed {@link Attribute}
         * @throws AttributeException if the builder properties can't make an Attribute
         */
        public Attribute build() throws AttributeException {
            return new Attribute(this);
        }

        /**
         * Construct a build based from a fieldName
         *
         * It adds in the default properties visible, plottable, mappable, type and name.
         *
         * @param fieldName the fieldName
         */
        public Builder(final String fieldName) {
            this.fieldName = fieldName;
            // setup the defaults
            displayMap.put("visible", Boolean.FALSE);
            displayMap.put("plottable", Boolean.FALSE);
            displayMap.put("mappable", Boolean.FALSE);
            displayMap.put("type", TYPE_LITERAL);
            displayMap.put("name", fieldName);
            displayMap.put("collectionType", CollectionType.NONE.name());
            displayMap.put("ignoreUpdate", Boolean.FALSE);
        }

        /**
         * Set the name.
         *
         * @param name the name
         * @return the Builder
         */
        public Builder name(final String name) {
            displayMap.put("name", name);
            return this;
        }

        /**
         * Set the ignoreUpdate.
         *
         * @param ignoreUpdate the ignoreUpdate
         * @return the Builder
         */
        public Builder ignoreUpdate(final boolean ignoreUpdate) {
            displayMap.put("ignoreUpdate", ignoreUpdate);
            return this;
        }

        /**
         * Set the visible flag.
         *
         * @param visible the visible
         * @return the Builder
         */
        public Builder visible(final boolean visible) {
            if (visible) {
                displayMap.put("visible", Boolean.TRUE);
            }
            return this;
        }

        /**
         * Set the plottable flag.
         *
         * @param plottable the plottable
         * @return the Builder
         */
        public Builder plottable(final boolean plottable) {
            if (plottable) {
                displayMap.put("plottable", Boolean.TRUE);
            }
            return this;
        }

        /**
         * Set the mappable flag.
         *
         * @param mappable the mappable
         * @return the Builder
         */
        public Builder mappable(final boolean mappable) {
            if (mappable) {
                displayMap.put("mappable", Boolean.TRUE);
            }
            return this;
        }

        /**
         * Set the type flag.
         *
         * @param type the type
         * @return the Builder
         */
        public Builder type(final String type) {
            displayMap.put("type", type);
            return this;
        }

        /**
         * Set the collectionType of this attribute (it defaults to NONE).
         *
         * @param collectionType the collectionType
         * @return the collectionType
         */
        public Builder collectionType(final CollectionType collectionType) {
            displayMap.put("collectionType", collectionType.name());
            return this;
        }

    }
}
