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
package com.hp.hpl.loom.adapter.os.discover;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.adapter.os.OsItemType;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;

public class OsWorkloadType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsWorkloadType.class);
    public static final String TYPE_LOCAL_ID = "workload";

    public OsWorkloadType() {
        super(TYPE_LOCAL_ID);
        try {
            /* attributes */
            Attribute workloadType = new Attribute.Builder(CORE_NAME + "workloadType").name("WorkloadType")
                    .visible(true).plottable(false).build();

            addAttributes(workloadType);
            addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder().add(workloadType).build());
            addOperations(DefaultOperations.GROUP_BY.toString(), new OperationBuilder().add(workloadType).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsWorkloadType attributes: ", e);
        }
    }

}
