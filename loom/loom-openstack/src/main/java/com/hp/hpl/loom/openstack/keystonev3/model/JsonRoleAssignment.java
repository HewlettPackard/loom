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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object to model the role assignment.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRoleAssignment {

    private JsonRole role;
    private JsonScope scope;
    private JsonUser user;

    /**
     * @return the role
     */
    public JsonRole getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(final JsonRole role) {
        this.role = role;
    }

    /**
     * @return the scope
     */
    public JsonScope getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(final JsonScope scope) {
        this.scope = scope;
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
}
