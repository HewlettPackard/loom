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
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonAutoDetect
public class Identity {

    private String[] methods;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Password password;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Token token;


    public String[] getMethods() {
        return methods;
    }

    public void setMethods(final String[] methods) {
        this.methods = methods;
    }

    public void setMethodsToPassword() {
        methods = new String[1];
        methods[0] = "password";
    }

    public void setMethodsToToken() {
        methods = new String[1];
        methods[0] = "token";
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(final Password password) {
        this.password = password;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(final Token token) {
        this.token = token;
    }


}
