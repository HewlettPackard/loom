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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The result of a query operation to retrieve a Thread. An aggregation of ThreadResultElements.
 * Used for on-the-wire wrapping.
 */
@JsonAutoDetect
public class QueryResult {

    /**
     * The status for the QueryResult.
     */
    public enum Statuses {
        /**
         * QueryResult READY status.
         */
        READY,
        /**
         * QueryResult PENDING status.
         */
        PENDING
    };

    /**
     * Logical ID of the DA for the result of the query.
     */
    private String logicalId;

    /**
     * Optional name for the result.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    private ArrayList<QueryResultElement> elements;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ItemType itemType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Statuses status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long alertCount;

    /**
     * Highest alert level in the cluster.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer highestAlertLevel;

    /**
     * Description of (one of the) highest alert level(s) in the cluster.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String highestAlertDescription;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)

    private Map<String, Long> timings = new HashMap<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long totalTime;
    private QueryResultElement excludedItems;

    /**
     * Default constructor.
     */
    public QueryResult() {
        elements = new ArrayList<QueryResultElement>();
        status = Statuses.READY;
    }

    /**
     * Constructor with logicalId.
     *
     * @param logicalId the logicalId
     */
    public QueryResult(final String logicalId) {
        super();
        this.logicalId = logicalId;
    }

    /**
     * Constructor with a logicalId and name.
     *
     * @param logicalId the logicalId
     * @param name the name
     */
    public QueryResult(final String logicalId, final String name) {
        super();
        this.logicalId = logicalId;
        this.name = name;
    }

    /**
     * Constructor with a aggregation.
     *
     * @param aggregation aggregation to construct from
     */
    public QueryResult(final Aggregation aggregation) {
        elements = new ArrayList<QueryResultElement>(aggregation.getElements().size());
        logicalId = aggregation.getLogicalId();
        for (Fibre entity : aggregation.getElements()) {
            QueryResultElement queryResultElement = new QueryResultElement(entity);
            addElement(queryResultElement);
        }
        status = Statuses.READY;

        alertCount = aggregation.getAlertCount();
        highestAlertDescription = aggregation.getHighestAlertDescription();
        highestAlertLevel = aggregation.getHighestAlertLevel();
    }

    /**
     * Get the alertCount value.
     *
     * @return the alertCount
     */
    public Long getAlertCount() {
        return alertCount;
    }

    /**
     * Set the alertCount value.
     *
     * @param alertCount the alertCount
     */
    public void setAlertCount(final Long alertCount) {
        this.alertCount = alertCount;
    }

    /**
     * Get the highestAlertLevel.
     *
     * @return the highestAlertLevel
     */
    public Integer getHighestAlertLevel() {
        return highestAlertLevel;
    }

    /**
     * Set the highestAlertLevel.
     *
     * @param highestAlertLevel the highestAlertLevel
     */
    public void setHighestAlertLevel(final Integer highestAlertLevel) {
        this.highestAlertLevel = highestAlertLevel;
    }

    /**
     * Get the highestAlertDescription.
     *
     * @return the highestAlertDescription
     */
    public String getHighestAlertDescription() {
        return highestAlertDescription;
    }

    /**
     * Set the HighestAlertDescription.
     *
     * @param highestAlertDescription the highestAlertDescription
     */
    public void setHighestAlertDescription(final String highestAlertDescription) {
        this.highestAlertDescription = highestAlertDescription;
    }

    /**
     * Get the statuses.
     *
     * @return the statuses
     */
    public Statuses getStatus() {
        return status;
    }

    /**
     * Set the status.
     *
     * @param status the status
     */
    public void setStatus(final Statuses status) {
        this.status = status;
    }

    /**
     * Get the logicalId.
     *
     * @return the logicalId
     */
    public String getLogicalId() {
        return logicalId;
    }

    /**
     * Set the logicalId.
     *
     * @param logicalId the logicalId
     */
    public void setLogicalId(final String logicalId) {
        this.logicalId = logicalId;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the query result elements.
     *
     * @return the query result elements list.
     */
    public ArrayList<QueryResultElement> getElements() {
        return elements;
    }

    /**
     * Set the query result elements.
     *
     * @param elements the query result elements list.
     */
    public void setElements(final ArrayList<QueryResultElement> elements) {
        this.elements = elements;
    }

    /**
     * Add the queryResultElement.
     *
     * @param queryResultElement the queryResultElement
     */
    public void addElement(final QueryResultElement queryResultElement) {
        elements.add(queryResultElement);
    }

    /**
     * Get the ItemType.
     *
     * @return the itemType
     */
    public ItemType getItemType() {
        return itemType;
    }

    /**
     * Set the ItemType.
     *
     * @param itemType the itemType
     */
    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    /**
     * Calculate total number of relations across all Resource elements.
     *
     * @return the total size of the relations.
     */
    public int totalRelations() {
        int total = 0;
        for (QueryResultElement resource : getElements()) {
            total += resource.getRelations().size();
        }
        return total;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonRep = "";
        try {
            jsonRep = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println("JsonProcessingException " + e);
        }
        return jsonRep;
    }

    /**
     * Recursively get all Fibre contained by Aggregation and all of its children. It returns a map
     * of the containing aggregation to the fibre.
     *
     * @return a map of the aggregation or fibre logical id to the item logical id.
     */
    @JsonIgnore
    public final HashMap<String, String> getContainedItemsLogicalIds() {
        HashMap<String, String> logicalIds = new HashMap<>();
        List<QueryResultElement> items = this.getElements();
        for (QueryResultElement queryResultElement : items) {
            Fibre fibre = queryResultElement.getEntity();
            if (fibre.isAggregation()) {
                Aggregation agg = (Aggregation) fibre;
                getItems(logicalIds, agg);
            } else {
                logicalIds.put(fibre.getLogicalId(), this.getLogicalId());
            }
        }
        return logicalIds;
    }

    /**
     * Walks the given aggregation adding in the items + containing logical id to the map provided.
     *
     * @param map The aggregation/fibre logical id to the item logical id
     * @param aggregation The aggregation to walk down
     */
    private void getItems(final HashMap<String, String> map, final Aggregation aggregation) {
        List<Fibre> fibres = aggregation.getElements();
        for (Fibre fibre : fibres) {
            if (fibre.isAggregation()) {
                this.getItems(map, (Aggregation) fibre);
            } else {
                map.put(fibre.getLogicalId(), aggregation.getLogicalId());
            }
        }
    }

    /**
     * Add timing information.
     *
     * @param action action we are timing for
     * @param time the time
     */
    public void addTiming(String action, long time) {
        timings.put(action, time);
    }

    /**
     * Set the total time.
     *
     * @param time the time
     */
    public void setTotalTiming(Long time) {
        this.totalTime = time;
    }

    /**
     * Get the totalTiming.
     *
     * @return the total timing
     */
    public Long getTotalTiming() {
        return totalTime;
    }

    /**
     * Get the timing info back for this query result.
     *
     * @return get the timing for this query result
     */
    public Map<String, Long> getTimings() {
        return timings;
    }

    /**
     * @return the excludedItems
     */
    public QueryResultElement getExcludedItems() {
        return excludedItems;
    }

    /**
     * @param excludedItems the excludedItems to set
     */
    public void setExcludedItems(QueryResultElement excludedItems) {
        this.excludedItems = excludedItems;
    }
}
