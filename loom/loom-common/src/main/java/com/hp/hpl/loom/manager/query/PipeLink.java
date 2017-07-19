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
package com.hp.hpl.loom.manager.query;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;

/**
 * The pipelink class extends a LinkedHashMap to provide some more suitable constructors.
 *
 * @param <T> the type of Fibre to support
 */
public class PipeLink<T extends Fibre> extends LinkedHashMap<Object, List<T>> {

    /**
     * @param key the key to store under
     * @param value the list of Ts to store
     */
    public PipeLink(final Object key, final List<T> value) {
        super();
        this.put(key, value);
    }

    /**
     * @param collect A map to add to the list
     */
    public PipeLink(final Map<Object, List<T>> collect) {
        super();
        for (Object key : collect.keySet()) {
            this.put(key, collect.get(key));
        }
    }

    /**
     * @param collect A map to add to the list
     * @param property property to try and reflect and use as the key
     * @param context query context
     */
    public PipeLink(final Map<Object, List<T>> collect, final String property, final OperationContext context) {
        super();
        for (Object key : collect.keySet()) {
            if (Number.class.isAssignableFrom(key.getClass()) || !key.toString().contains("@")) {
                this.put(key, collect.get(key));
            } else {
                this.put(FibreIntrospectionUtils.introspectProperty(property,
                        collect.get(key).stream().findFirst().get(), new HashMap<OperationErrorCode, String>(0),
                        context), collect.get(key));
            }
        }
    }

    /**
     * @param size initial size
     */
    public PipeLink(final int size) {
        super(size);
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        for (List<T> list : this.values()) {
            stb.append(list);
        }
        return stb.toString();
    }
}
