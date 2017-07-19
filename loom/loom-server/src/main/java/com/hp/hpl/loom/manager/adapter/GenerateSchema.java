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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hp.hpl.loom.adapter.ProviderItem;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.manager.stitcher.StitcherRuleManager;
import com.hp.hpl.loom.manager.stitcher.Tacker;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.stitcher.StitcherRulePair;

public class GenerateSchema {


    public String process(final Tacker tacker, final List<Class<? extends Item>> items,
            final Set<Provider> loggedInProviders) {
        GraphData data = new GraphData();
        Map<String, Data> simpleNameToNodesMap = new HashMap<>();
        Map<String, Data> typeIdToNodesMap = new HashMap<>();
        // Build the list of nodes based on itemTypes found
        List<Class<?>> foundItemTypeInfos = new ArrayList<>();
        Map<Provider, Collection<Class<?>>> providersToItemClasses = new HashMap<>();
        for (Provider provider : loggedInProviders) {
            Collection<Class<?>> classes = new ArrayList<>();
            for (Class<?> item : items) {
                if (item.getPackage().getName().startsWith(provider.getAdapterPackage())
                        || provider.getAdapterPackage().startsWith(item.getPackage().getName())) {
                    ItemTypeInfo itemTypeInfo = (ItemTypeInfo) item.getAnnotation(ItemTypeInfo.class);
                    if (itemTypeInfo != null && !(item.equals(ProviderItem.class))) {
                        foundItemTypeInfos.add(item);
                        classes.add(item);
                        String localId = itemTypeInfo.value();
                        String[] layers = itemTypeInfo.layers();
                        Data n = new Data();
                        n.setId(provider.getProviderType() + "-" + localId);
                        n.setLayers(layers);
                        n.setName(item.getSimpleName());
                        n.setProviderType(provider.getProviderType());
                        n.setProviderId(provider.getProviderId());
                        if (item.isAnnotationPresent(Root.class)) {
                            n.setRoot(true);
                        }
                        Data2 n2 = new Data2();
                        n2.setData(n);
                        data.getNodes().add(n2);
                        simpleNameToNodesMap.put(provider.getProviderType() + "-" + item.getSimpleName(), n);
                        typeIdToNodesMap.put(n.getId(), n);
                    }

                }
            }
            providersToItemClasses.put(provider, classes);
        }

        // Build the list of edges based on connectedTo info
        for (Class<?> item : foundItemTypeInfos) {
            ItemTypeInfo itemTypeInfo = (ItemTypeInfo) item.getAnnotation(ItemTypeInfo.class);

            for (Annotation el : item.getAnnotationsByType(ConnectedTo.class)) {
                ConnectedTo ct = (ConnectedTo) el;

                for (Provider provider : loggedInProviders) {
                    Data e = new Data();
                    if (!ct.typeName().equals("")) {
                        e.setName(ct.typeName());
                    }
                    Data sourceId = simpleNameToNodesMap.get(provider.getProviderType() + "-" + item.getSimpleName());
                    Data targetId =
                            simpleNameToNodesMap.get(provider.getProviderType() + "-" + ct.toClass().getSimpleName());
                    if (sourceId != null && targetId != null) {
                        e.setSource(sourceId.getId());
                        e.setTarget(targetId.getId());
                        Data2 e2 = new Data2();
                        e2.setData(e);
                        data.getEdges().add(e2);
                    }
                }
            }
        }

        // add in the stitched relationshps
        StitcherRuleManager ruleManager = tacker.getStitcherRuleManager();
        Collection<StitcherRulePair> rules = ruleManager.getRules();
        for (StitcherRulePair<?, ?> stitcherRulePair : rules) {
            if ((typeIdToNodesMap.get(stitcherRulePair.getLeft().sourceTypeId()) != null)
                    && (typeIdToNodesMap.get(stitcherRulePair.getLeft().otherTypeId()) != null)) {
                Data e = new Data();
                e.setStitch(true);
                e.setSource(stitcherRulePair.getLeft().sourceTypeId());
                e.setTarget(stitcherRulePair.getLeft().otherTypeId());
                Data2 e2 = new Data2();
                e2.setData(e);
                data.getEdges().add(e2);
            }
        }

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = "";
        try {
            json = ow.writeValueAsString(data);
        } catch (JsonProcessingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return json;
    }

}
