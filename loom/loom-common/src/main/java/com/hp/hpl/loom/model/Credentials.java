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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Model class that contains the users credentials.
 *
 */
@JsonAutoDetect
public class Credentials {

    // @JsonInclude(JsonInclude.Include.NON_EMPTY)
    // private List<OpenstackProject> OpenStackProjects;
    private String username;
    private String password;

    @JsonIgnore
    private Object context;

    /**
     * No-arg constructor for JSON serialisation.
     */
    public Credentials() {}

    /**
     * @param username the username
     * @param password the password
     */
    public Credentials(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @param context the context
     */
    public void setContext(final Object context) {
        this.context = context;
    }

    /**
     * Get the context.
     *
     * @return the credential context
     */
    @JsonIgnore
    public Object getContext() {
        return context;
    }

    /**
     * Get the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

}
