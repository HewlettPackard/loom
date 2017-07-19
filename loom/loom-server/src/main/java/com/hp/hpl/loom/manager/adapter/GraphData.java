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
package com.hp.hpl.loom.manager.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect
@JsonInclude(Include.NON_NULL)
public class GraphData {
    private List<Data2> nodes = new ArrayList<>();
    private Set<Data2> edges = new HashSet<>();

    /**
     * @return the nodes
     */
    public List<Data2> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(List<Data2> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the edges
     */
    public Set<Data2> getEdges() {
        return edges;
    }

    /**
     * @param edges the edges to set
     */
    public void setEdges(Set<Data2> edges) {
        this.edges = edges;
    }
}


/*
 * { "nodes": [{ "data": { "id": "OpenStack-instance", "label": "OsInstance" } },
 */

@JsonInclude(Include.NON_NULL)
class Data2 {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        // compare source & target
        Data2 other = (Data2) obj;
        if (data.getId() == null) {
            return data.getRelationshipName().equals(other.getData().getRelationshipName());
        } else {
            return data.getId().equals(other.getData().getId());
        }
    }

    @Override
    public int hashCode() {
        return (data.getId() + "" + data.getRelationshipName()).hashCode();
    }
}


@JsonInclude(Include.NON_NULL)
class Data {
    private String id;
    private Boolean stitch = null;
    private String name;
    private String source;
    private String target;
    private String providerId;
    private String providerType;
    private Boolean root = null;
    private String[] layers;

    public Data() {}

    public Data(Data source, Data target) {
        this.source = source.id;
        this.target = target.id;
    }

    public Data(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected String getRelationshipName() {
        if (id == null) {
            String combinedName = source.compareTo(target) <= 0 ? source + target : target + source;
            return combinedName + name;
        } else {
            return id;
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the providerId
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @param providerId the providerId to set
     */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /**
     * @return the providerType
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * @param providerType the providerType to set
     */
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    /**
     * @return the root
     */
    public Boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(Boolean root) {
        this.root = root;
    }

    /**
     * @return the stitch
     */
    public Boolean getStitch() {
        return stitch;
    }

    /**
     * @param stitch the stitch to set
     */
    public void setStitch(Boolean stitch) {
        this.stitch = stitch;
    }

    /***
     * 
     * @param layer the layer to set
     */
    public void setLayers(String[] layers) {
        this.layers = layers;
    }

    /**
     * @return the layers
     */
    public String[] getLayers() {
        return layers;
    }

}
