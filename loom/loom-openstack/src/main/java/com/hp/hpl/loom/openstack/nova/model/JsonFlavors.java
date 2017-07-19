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
package com.hp.hpl.loom.openstack.nova.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.hpl.loom.openstack.common.JsonPaging;
import com.hp.hpl.loom.openstack.common.Pagination;

/**
 * Object to model the Flavors.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonFlavors implements Pagination {
    private List<JsonFlavor> flavors;

    @JsonProperty("flavors_links")
    private List<JsonPaging> links;

    /**
     * @return the flavors
     */
    public List<JsonFlavor> getFlavors() {
        return flavors;
    }

    /**
     * @param flavors the flavors to set
     */
    public void setFlavors(final List<JsonFlavor> flavors) {
        this.flavors = flavors;
    }

    /**
     * @return the links
     */
    @Override
    public List<JsonPaging> getLinks() {
        return links;
    }

    /**
     * @param links the links to set
     */
    public void setLinks(final List<JsonPaging> links) {
        this.links = links;
    }
}
