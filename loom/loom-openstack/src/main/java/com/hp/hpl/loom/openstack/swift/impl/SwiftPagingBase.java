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
package com.hp.hpl.loom.openstack.swift.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.openstack.OpenstackApi;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonEndpoint;

public abstract class SwiftPagingBase<R, T> extends SwiftBase<R> {
    protected Map<String, Integer> maxSizes = new HashMap<>();

    public SwiftPagingBase(final OpenstackApi openstackApp, final JsonEndpoint jsonEndpoint) {
        super(openstackApp, jsonEndpoint);
    }

    protected Iterator<? extends T> getIteratorUsingUri(final String uri) {
        Iterator<? extends T> iterator = new Iterator<T>() {
            List<T> data = null;
            int counter = -1;
            URI resourcesURI;

            @Override
            public boolean hasNext() {
                if (data == null) {
                    RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
                    // String resources = getUri(containerId);
                    try {
                        resourcesURI = new URI(uri);
                        R jsonObjects = getResourcesFromGet(rt, resourcesURI);
                        data = new ArrayList<>();
                        data.addAll(Arrays.asList((T[]) jsonObjects));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                if ((counter + 1) < data.size()) {
                    return true;
                } else {
                    if (data.size() == 0 || data.size() != maxSizes.get(resourcesURI.toString())) {
                        // check for more
                        loadMore();
                        if ((counter + 1) < data.size()) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }

            private void loadMore() {
                // String resources = getUri(containerId);
                String params = uri.contains("?") ? "&" : "?";
                if (data.size() != 0) {
                    T last = data.get(data.size() - 1);

                    URI resourcesURI;
                    String newUrl;
                    try {
                        newUrl = uri + params + "marker=" + URLEncoder.encode(last.toString(), "UTF-8");
                        resourcesURI = new URI(newUrl);
                        RestTemplate rt = getRestTemplate(jsonEndpoint.getProjectId());
                        R jsonObjects = getResourcesFromGet(rt, resourcesURI);
                        T[] t = (T[]) jsonObjects;
                        for (T t2 : t) {
                            data.add(t2);
                        }
                        // for (JsonObject element : jsonObjects) {
                        // data.add(element);
                        // }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public T next() {
                if (this.hasNext()) {
                    counter++;
                    return data.get(counter);
                } else {
                    return null;
                }

            }

        };
        return iterator;
    }


}
