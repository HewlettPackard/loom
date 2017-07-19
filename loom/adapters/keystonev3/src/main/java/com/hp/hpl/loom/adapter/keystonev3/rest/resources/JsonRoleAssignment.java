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
package com.hp.hpl.loom.adapter.keystonev3.rest.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRoleAssignment {

    private JsonRole role;
    private JsonScope scope;
    private JsonUser user;

    public JsonRole getRole() {
        return role;
    }

    public void setRole(final JsonRole role) {
        this.role = role;
    }

    public JsonScope getScope() {
        return scope;
    }

    public void setScope(final JsonScope scope) {
        this.scope = scope;
    }

    public JsonUser getUser() {
        return user;
    }

    public void setUser(final JsonUser user) {
        this.user = user;
    }



}
