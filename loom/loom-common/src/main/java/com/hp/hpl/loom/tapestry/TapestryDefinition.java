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
package com.hp.hpl.loom.tapestry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;

/**
 * The tapestry definition.
 */
public class TapestryDefinition {

    private String id;
    private List<ThreadDefinition> threads;

    /**
     * No-arg constructor to keep json constructing happy.
     */
    public TapestryDefinition() {
        threads = new ArrayList<ThreadDefinition>();
    }

    /**
     * Constructor that takes the tapestry id and list of thread definitions.
     *
     * @param id tapestry id
     * @param threads thread definitions
     */
    public TapestryDefinition(final String id, final List<ThreadDefinition> threads) {

        if (threads == null) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.threads = threads;
    }

    /**
     * @param id tapestry id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param threads thread definitions
     */
    public void setThreads(final List<ThreadDefinition> threads) {
        this.threads = threads;
    }

    /**
     * Get the tapestry id.
     *
     * @return the tapestry id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the thread definitions.
     *
     * @return the thread definitions.
     */
    public List<ThreadDefinition> getThreads() {
        return new ArrayList<>(threads);
    }

    /**
     * Add a thread definition.
     *
     * @param thread thread definition to add
     * @return the threadDefintion
     */
    public ThreadDefinition addThreadDefinition(final ThreadDefinition thread) {
        threads.add(thread);
        return threads.get(threads.size() - 1);
    }

    /**
     * Remove the thread definition.
     *
     * @param threadId the thread id to remove
     * @return the ThreadDefinition
     * @throws NoSuchThreadDefinitionException thrown if the thread can't be found
     */
    public ThreadDefinition removeThreadDefinition(final String threadId) throws NoSuchThreadDefinitionException {
        int indx = findThreadIndexById(threadId);

        ThreadDefinition removed = null;
        if (indx != -1) {
            removed = threads.remove(indx);
        } else {
            throw new NoSuchThreadDefinitionException(threadId);
        }

        return removed;
    }

    /**
     * Remove all the threads in this definition.
     */
    public void clearThreads() {
        threads.clear();
    }

    /**
     * Update the thread definition using the thread is and threadDefiniton.
     *
     * @param threadId the thread id
     * @param threadDefinition the thread definition
     * @throws NoSuchThreadDefinitionException thrown if the thread can't be found
     */
    public void updateThreadDefinition(final String threadId, final ThreadDefinition threadDefinition)
            throws NoSuchThreadDefinitionException {
        ThreadDefinition thread = getThreadDefinition(threadId);
        thread.setItemType(threadDefinition.getItemType());
        thread.setQuery(threadDefinition.getQuery());
        thread.setName(threadDefinition.getName());
    }

    /**
     * Get the thread definition for a given id.
     *
     * @param threadId the thread id
     * @return the threadDefinition
     * @throws NoSuchThreadDefinitionException thrown if the thread can't be found
     */
    public ThreadDefinition getThreadDefinition(final String threadId) throws NoSuchThreadDefinitionException {
        int indx = findThreadIndexById(threadId);
        ThreadDefinition obtained = null;

        if (indx != -1) {
            obtained = threads.get(indx);
        } else {
            throw new NoSuchThreadDefinitionException(threadId);
        }

        return obtained;
    }

    private int findThreadIndexById(final String threadId) {

        int indx = -1;
        int currentIndx = 0;
        for (ThreadDefinition thread : threads) {
            if (thread.getId().equals(threadId)) {
                indx = currentIndx;
                break;
            }
            currentIndx++;
        }
        return indx;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("tapestryId-> " + id);
        str.append("; threads-> " + threads);
        return str.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TapestryDefinition that = (TapestryDefinition) o;
        return Objects.equals(id, that.id) && Objects.equals(threads, that.threads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, threads);
    }

}
