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
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Object to model the Identity.
 */
@JsonAutoDetect
public class Identity {

    private String[] methods;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Password password;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Token token;

    /**
     * Set the methods to password.
     */
    public void setMethodsToPassword() {
        methods = new String[1];
        methods[0] = "password";
    }

    /**
     * Set the methods to token.
     */
    public void setMethodsToToken() {
        methods = new String[1];
        methods[0] = "token";
    }

    /**
     * @return the methods
     */
    public String[] getMethods() {
        return methods;
    }

    /**
     * @param methods the methods to set
     */
    public void setMethods(final String[] methods) {
        this.methods = methods;
    }

    /**
     * @return the password
     */
    public Password getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final Password password) {
        this.password = password;
    }

    /**
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(final Token token) {
        this.token = token;
    }
}
