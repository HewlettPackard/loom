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
package com.hp.hpl.loom.openstack.keystonev3;

import com.hp.hpl.loom.openstack.keystonev3.model.JsonUser;

/**
 * This class manages the tokens and the json user.
 */
public class TokenManager {
    private TokenHolder tokenHolder = null;
    private JsonUser jsonUser = null;

    /**
     * Construct a new manager with a given unscoped token.
     *
     * @param unscoped unscoped token
     */
    public TokenManager(final String unscoped) {
        tokenHolder = new TokenHolder(unscoped);
    }

    /**
     * @return the tokenHolder
     */
    public TokenHolder getTokenHolder() {
        return tokenHolder;
    }

    /**
     * @param tokenHolder the tokenHolder to set
     */
    public void setTokenHolder(final TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    /**
     * @return the jsonUser
     */
    public JsonUser getJsonUser() {
        return jsonUser;
    }

    /**
     * @param jsonUser the jsonUser to set
     */
    public void setJsonUser(final JsonUser jsonUser) {
        this.jsonUser = jsonUser;
    }


}
