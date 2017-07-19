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
package com.hp.hpl.loom.adapter.hpcloud.itemtype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;

public class HostType extends HpCloudItemType {
    public static final String TYPE_LOCAL_ID = "host";
    public static final String[] PAYLOAD_FIELDS =
            {"datastores", "ipv4_address", "product_code", "cpuModel", "memoryMB", "hostname", "numCpuPackages",
                    "datacenterName", "status", "numNics", "numCpuCores", "model", "cpuMhz", "networks", "os_vendor"};
    private static final Log LOG = LogFactory.getLog(HostType.class);

    public HostType() {
        super(TYPE_LOCAL_ID);

        // Saul: If we want to have different operations for each attribute, we should specify them
        // manually
        for (String attName : PAYLOAD_FIELDS) {
            try {
                Attribute attribute =
                        new Attribute.Builder(attName).name(attName).visible(true).plottable(false).build();

                this.addAttributes(attribute);
                this.addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder().add(attribute).build());
                this.addOperations(DefaultOperations.GROUP_BY.toString(),
                        new OperationBuilder().add(attribute).build());
            } catch (AttributeException e) {
                LOG.error("Problem creating ChefUploadedCookbookType", e);
            }
        }
    }

}
