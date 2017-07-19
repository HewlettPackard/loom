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
package com.hp.hpl.loom.relationships;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;

/**
 * All connected relationships found for the grounded aggregations.
 */
public class RelationshipsModelImpl implements RelationshipsModel {
    private static final Log LOG = LogFactory.getLog(RelationshipsModelImpl.class);

    // ////////////////////////////////////////////////////////////////////////////////////////
    // Connected Relationships
    // ////////////////////////////////////////////////////////////////////////////////////////

    // Map of className to Relationships for that class
    private Map<String, ConnectedRelationships> classRelationships = new HashMap<String, ConnectedRelationships>();

    @Override
    public ConnectedRelationships getItemRelationships(final String className) {
        return classRelationships.get(className);
    }

    public ConnectedRelationships getItemRelationshipsByItemTypeId(final String info) {
        ConnectedRelationships connectedRelationships = null;
        for (ConnectedRelationships rel : this.classRelationships.values()) {
            if (rel != null && rel.getItemTypeInfo() != null) {
                if (rel.getItemTypeInfo().value().equals(info)) {
                    connectedRelationships = rel;
                    break;
                }
            }
        }
        return connectedRelationships;
    }

    /**
     * No-arg constructor.
     */
    public RelationshipsModelImpl() {}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hp.hpl.loom.relationships.RelationshipsModel#calculateClassRelationships(java.util.List)
     */
    @Override
    public void calculateClassRelationships(final List<Aggregation> groundedAggregations) {
        // For every thread
        for (Aggregation aggregation : groundedAggregations) {
            if (aggregation.getSize() > 0) {
                Fibre entity = aggregation.first();

                // List<Predicate<ConnectedRelationships>> traversalRules =
                // entity.getItemType().getStopTraversalRules();
                Class<? extends Fibre> entityClass = entity.getClass();

                // stopConditionMap.put(entityClass, traversalRules);

                calcRelationships(entityClass);
            }
        }
    }

    private ConnectedRelationships calcRelationships(final Class<? extends Fibre> entityClass) {

        // Check map for class, in order to avoid using Reflections retrieves the stop traversal
        // rules.
        List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopRules =
                Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.get(entityClass);

        String entityClassName = entityClass.getName();

        if (classRelationships.containsKey(entityClassName)) {
            // Already calculated relationships for this class
            return classRelationships.get(entityClassName);
        }
        Annotation[] rootAnnotations = entityClass.getAnnotationsByType(Root.class);
        boolean root = rootAnnotations != null && rootAnnotations.length > 0;

        Annotation typeInfoAnnotation = entityClass.getAnnotation(ItemTypeInfo.class);
        ItemTypeInfo itemTypeInfo = (ItemTypeInfo) typeInfoAnnotation;

        ConnectedRelationships relationships =
                new ConnectedRelationships(entityClassName, itemTypeInfo, root, stopRules);
        classRelationships.put(entityClassName, relationships);

        //
        // Relationships expressed on class
        Annotation[] connectedToAnnotations = entityClass.getAnnotationsByType(ConnectedTo.class);
        for (Annotation connectedToAnnotation : connectedToAnnotations) {
            ConnectedTo connectedTo = (ConnectedTo) connectedToAnnotation;
            if (LOG.isTraceEnabled()) {
                LOG.trace(entityClass.getSimpleName() + " ConnectedTo: value=" + connectedTo.value() + " toClass="
                        + connectedTo.toClass());
            }
            ConnectedRelationships toRelationships = calcRelationships(connectedTo.toClass());
            relationships.addConnectedTo(new ConnectedToRelationship(itemTypeInfo, connectedTo, toRelationships,
                    connectedTo.type(), connectedTo.typeName()));
        }

        return relationships;
    }

    @Override
    public Collection<ConnectedRelationships> getAllItemRelationships() {
        return classRelationships.values();
    }
}
