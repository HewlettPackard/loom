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

public class TokenHolder {

    private String unscoped;
    private Map<String, String> scopedMap = new HashMap<>();

    public TokenHolder(final String unscoped) {
        super();
        this.unscoped = unscoped;
    }

    public String getUnscoped() {
        return unscoped;
    }

    public void setUnscoped(final String unscoped) {
        this.unscoped = unscoped;
    }

    public void setScoped(final String projectId, final String token) {
        scopedMap.put(projectId, token);
    }

    public String getScoped(final String projectId) {
        return scopedMap.get(projectId);
    }

    public Collection<String> getAllScopedProjectIds() {
        return scopedMap.keySet();
    }

}
