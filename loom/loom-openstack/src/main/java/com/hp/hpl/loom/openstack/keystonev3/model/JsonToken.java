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
package com.hp.hpl.loom.openstack.keystonev3.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object to model the token.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonToken {
    private List<JsonEndpoints> catalog = new ArrayList<>(0);
    private JsonUser user;
    private JsonProject project;

    /**
     * @return the catalog
     */
    public List<JsonEndpoints> getCatalog() {
        return catalog;
    }

    /**
     * @param catalog the catalog to set
     */
    public void setCatalog(final List<JsonEndpoints> catalog) {
        this.catalog = catalog;
    }

    /**
     * @return the user
     */
    public JsonUser getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(final JsonUser user) {
        this.user = user;
    }

    /**
     * @return the project
     */
    public JsonProject getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(final JsonProject project) {
        this.project = project;
    }
}
