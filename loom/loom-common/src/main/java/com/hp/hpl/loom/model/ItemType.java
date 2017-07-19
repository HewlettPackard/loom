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
package com.hp.hpl.loom.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;

/**
 * Class to model the ItemType.
 */
@JsonAutoDetect
public class ItemType {
    private static final Log LOG = LogFactory.getLog(ItemType.class);

    protected static final String NAME_SEPARATOR = ".";
    protected static final String CORE_NAME_PREFIX = "core";
    /**
     * The core name built from the CORE_NAME_PREFIX and the NAME_SEPERATOR.
     */
    public static final String CORE_NAME = CORE_NAME_PREFIX + NAME_SEPARATOR;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;
    @JsonIgnore
    private String localId;
    private String[] layers = {ItemTypeInfo.DEFAULT_LAYER};
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Map<String, Object>> attributes = new LinkedHashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ArrayList<String> orderedAttributes = new ArrayList<>();
    private List<String> excludedAttributes =
            Arrays.asList(Item.ATTR_ALERT_LEVEL, Item.ATTR_ALERT_DESCRIPTION, Fibre.ATTR_NAME);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonUnwrapped
    private GeoLocationAttributes geoAttributes;

    /**
     * Map of the operations for this ItemType.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Set<OrderedString>> operations = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Map<String, Action>> actions = new HashMap<>();

    private List<String> allowedActionTargetTypes =
            Arrays.asList(Fibre.Type.Item.toString().toLowerCase(), Fibre.Type.Aggregation.toString().toLowerCase());

    private static final int ALERT_DESCRIPTION_POS = Integer.MAX_VALUE - 1;
    private static final int ALERT_LEVEL_POS = ALERT_DESCRIPTION_POS - 1;
    private static final int FULLY_QUALIFIED_NAME_POS = ALERT_LEVEL_POS - 1;

    /**
     * No-arg constructor, it sets the default attributes such as fullyQualifiedName, alertLevel,
     * alertDescription, sort, group.
     */
    public ItemType() {
        try {
            Attribute fullyQualifiedName = new Attribute.Builder(Item.ATTR_FULLY_QUALIFIED_NAME)
                    .name("Fully Qualified Name").visible(false).plottable(false).build();
            Attribute alertLevel = new NumericAttribute.Builder(Item.ATTR_ALERT_LEVEL).min("0").max("10").unit("Level")
                    .name("Alert Level").visible(true).plottable(true).build();

            Attribute alertDescription = new Attribute.Builder(Item.ATTR_ALERT_DESCRIPTION).name("Alert Description")
                    .visible(true).plottable(false).build();

            Set<OrderedString> sort = new OperationBuilder().add(fullyQualifiedName, FULLY_QUALIFIED_NAME_POS)
                    .add(alertLevel, ALERT_LEVEL_POS).add(alertDescription, ALERT_DESCRIPTION_POS).build();
            Set<OrderedString> group = new OperationBuilder().add(alertLevel, ALERT_LEVEL_POS)
                    .add(alertDescription, ALERT_DESCRIPTION_POS).build();

            this.addAttributes(fullyQualifiedName, alertLevel, alertDescription);

            this.addOperations(DefaultOperations.SORT_BY.toString(), sort);
            this.addOperations(DefaultOperations.GROUP_BY.toString(), group);
        } catch (AttributeException e) {
            LOG.error("Problem creating attributes for ItemType", e);
        }
    }

    /**
     * Constructor that takes the local id and sets it. It calls the no-arg constructor as well.
     *
     * @param localId the localid.
     */
    public ItemType(final String localId) {
        this();
        setLocalId(localId);
    }

    /**
     * Get the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id the id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get local id.
     *
     * @return the localid
     */
    public String getLocalId() {
        return localId;
    }

    /**
     * Set the localId.
     *
     * @param localId the localId
     */
    public void setLocalId(final String localId) {
        this.localId = localId;
    }

    /**
     * Set an attribute.
     *
     * @param attr the attribute
     */
    public void setAttribute(final Attribute attr) {
        attributes.put(attr.getFieldName(), attr.getDisplayValues());
        setOrderedAttribute(attr.getFieldName());
    }

    /**
     * Get the attributes.
     *
     * @return the attributes for the itemType.
     */
    public Map<String, Map<String, Object>> getAttributes() {
        return attributes;
    }

    /**
     * Get the operations.
     *
     * @return map of operations and set of parameters.
     */
    public Map<String, Set<OrderedString>> getOperations() {
        return operations;
    }

    /**
     * Add the operation and operation parameters to this ItemType.
     *
     * @param operation operation to add
     * @param operationList operation parameters for this operation
     */
    public void addOperations(final String operation, final Set<OrderedString> operationList) {
        if (operations.get(operation) == null) {
            operations.put(operation, operationList);
        } else {
            operations.get(operation).addAll(operationList);
        }
    }

    /**
     * Get all the actions (for aggregations and items).
     *
     * @return map of types to map of string + actions.
     */
    public Map<String, Map<String, Action>> getActions() {
        return actions;
    }

    private Map<String, Action> getActions(final String targetType) {
        return actions.get(targetType);
    }

    /**
     * Get the itemActions map - item to action(s).
     *
     * @return the itemActions map.
     */
    @JsonIgnore
    public Map<String, Action> getItemActions() {
        return getActions(Fibre.Type.Item.toString().toLowerCase());
    }

    /**
     * Get the aggregationActions map - item to action(s).
     *
     * @return the aggregationActions map.
     */
    @JsonIgnore
    public Map<String, Action> getAggregationActions() {
        return getActions(Fibre.Type.Aggregation.toString().toLowerCase());
    }

    /**
     * Get the ordered attributes.
     *
     * @return the orderedAttributes
     */
    public ArrayList<String> getOrderedAttributes() {
        return orderedAttributes;
    }

    /**
     * Set the ordered attribute, it gets added to the orderedAttribute list.
     *
     * @param name the name
     */
    public void setOrderedAttribute(final String name) {
        if (!excludedAttributes.contains(name)) {
            if (orderedAttributes.contains(name)) {
                orderedAttributes.remove(name);
            }
            orderedAttributes.add(name);
        }
    }

    /**
     * Add an attribute(s) to this ItemType.
     *
     * @param attrs attributes to add
     */
    public void addAttributes(final Attribute... attrs) {
        for (Attribute attr : attrs) {
            setAttribute(attr);
        }
    }

    protected void addAction(final String targetType, final Action action) {
        if (targetType == null || !allowedActionTargetTypes.contains(targetType)) {
            throw new IllegalArgumentException();
        }
        Map<String, Action> actionMap = actions.get(targetType);
        if (actionMap == null) {
            actionMap = new HashMap<>();
            actions.put(targetType.toString(), actionMap);
        }
        actionMap.put(action.getId(), action);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("; id -> " + id);
        str.append("; attributes-> " + attributes);
        str.append("; operations-> " + operations);
        str.append("; actions-> " + actions);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemType that = (ItemType) o;
        return Objects.equals(localId, that.localId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localId);
    }

    /**
     * @return the geoAttributes
     */
    public GeoLocationAttributes getGeoAttributes() {
        return geoAttributes;
    }

    /**
     * @param geoAttributes the geoAttributes to set
     */
    public void setGeoAttributes(final GeoLocationAttributes geoAttributes) {
        this.geoAttributes = geoAttributes;
    }

    /**
     * @return the layers
     */
    public String[] getLayers() {
        return layers;
    }

    /**
     * @param layers the layers to set
     */
    public void setLayers(String[] layers) {
        this.layers = layers;
    }

}
