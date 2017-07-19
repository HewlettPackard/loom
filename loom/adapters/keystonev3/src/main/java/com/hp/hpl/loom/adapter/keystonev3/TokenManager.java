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
package com.hp.hpl.loom.adapter.keystonev3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TokenManager {
    private static TokenManager instance = null;

    private Map<String, TokenHolder> tokenHolderMap = new HashMap<>();

    public synchronized static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public void setTokenHolder(final String userName, final TokenHolder tokenHolder) {
        tokenHolderMap.put(userName, tokenHolder);
    }

    public TokenHolder getTokenHolder(final String userName) {
        return tokenHolderMap.get(userName);
    }

    public Collection<TokenHolder> getAllTokenHolders() {
        return tokenHolderMap.values();
    }
}
