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
package com.hp.hpl.loom.manager.stitcher;

import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.model.Session;

public interface Tacker {

    StitcherRuleManager getStitcherRuleManager();

    void createSession(Session session) throws NoSuchSessionException, SessionAlreadyExistsException;

    void deleteSession(Session session) throws NoSuchSessionException;

    void deleteAllSessions();

    ItemEquivalence getItemEquivalence(Session session) throws NoSuchSessionException;

    StitcherUpdater getStitcherUpdater(Session session) throws NoSuchSessionException;
}
