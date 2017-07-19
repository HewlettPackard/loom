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

public class ChefNodeType extends HpCloudItemType {
    public static final String TYPE_LOCAL_ID = "chef_node";
    public static final String[] PAYLOAD_FIELDS = {"chef_server", "ipv6_address", "lsb", "chef_environment",
            "dns_domain", "run_list", "snmpd", "serial", "id", "os_vendor", "basenode_snapshot", "sku", "uuid",
            "chassis_serial", "hostname", "virtual", "platform_version", "platform", "kernel_modules",
            "model_manufacturer", "uptime_seconds", "chef_org_id", "os_name", "ilo_pw_salt", "default_gateway", "keys",
            "interfaces", "filesystems", "ipv4_address", "mac", "address", "chef_org", "ipaddress", "created_ts",
            "name", "roles", "kernel_version", "recipes", "fqdn", "chef_client_url", "platform_family", "updated_ts",
            "os_release", "chef_packages", "model_name", "default_interface"};
    private static final Log LOG = LogFactory.getLog(ChefNodeType.class);

    public ChefNodeType() {
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
                LOG.error("Problem creating ChefNodeType", e);
            }
        }
    }

}
