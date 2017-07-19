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
package com.hp.hpl.loom.api;

import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.loom.exceptions.NoSuchPatternException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.PatternDefinitionList;

public interface PatternDefinitionService {

    /**
     * Gets all patterns for all providers.
     *
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a list of all pattern definitions
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     */
    PatternDefinitionList getPatterns(String sessionId, HttpServletResponse response) throws NoSuchSessionException;

    /**
     * Gets a pattern for a given patternId.
     *
     * @param patternId - an ID of the requested pattern
     * @param sessionId - session's cookie sent by a requester
     * @param response - http response to be sent back to the requester
     * @return a pattern definition requested
     * @throws InterruptedException - when a thread is interrupted
     * @throws NoSuchSessionException - if the cookie (sessionId) sent does not exist
     * @throws NoSuchPatternException - if no pattern definition matches the ID provided
     */
    PatternDefinition getPattern(String patternId, String sessionId, HttpServletResponse response)
            throws InterruptedException, NoSuchSessionException, NoSuchPatternException;

}
