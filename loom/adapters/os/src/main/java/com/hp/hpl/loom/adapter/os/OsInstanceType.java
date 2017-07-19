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
package com.hp.hpl.loom.adapter.os;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.AttributeType;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.adapter.OrderedString;
import com.hp.hpl.loom.adapter.os.discover.OsWorkloadType;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class OsInstanceType extends OsItemType {
    public static final String ATTR_FLAVOR = CORE_NAME + "flavor";
    public static final String ATTR_STATUS = CORE_NAME + "status";
    public static final String TYPE_LOCAL_ID = "instance";
    public static final String VOLUMES = "volumes";

    private static final Log LOG = LogFactory.getLog(OsInstanceType.class);


    public OsInstanceType(final Provider provider) {
        super(TYPE_LOCAL_ID);


        try {
            // status

            Attribute status =
                    new Attribute.Builder(CORE_NAME + "status").name("Status").visible(true).plottable(false).build();

            // Flavour related attributes

            Attribute flavor = new Attribute.Builder(ATTR_FLAVOR).name("Flavor").visible(true).plottable(false).build();

            Attribute vcpus =
                    new NumericAttribute.Builder(CORE_NAME + "vcpus").name("VCPUs").visible(true).plottable(true)
                            .min("0").max("24").type(AttributeType.NUMERIC).mappable(false).unit("VCPU").build();

            Attribute ram = new NumericAttribute.Builder(CORE_NAME + "ram").name("RAM").visible(true).plottable(true)
                    .min("0").max("Inf").unit("MB").build();

            Attribute disk = new NumericAttribute.Builder(CORE_NAME + "disk").name("Disk").visible(true).plottable(true)
                    .min("0").max("Inf").unit("GB").build();

            Attribute flavorId = new Attribute.Builder(CORE_NAME + "flavorId").name("Flavor ID").visible(true)
                    .plottable(false).build();

            // Other attributes
            Attribute accessIPv4 = new Attribute.Builder(CORE_NAME + "accessIPv4").name("Access IPv4").visible(true)
                    .plottable(false).build();

            Attribute accessIPv6 = new Attribute.Builder(CORE_NAME + "accessIPv6").name("Access IPv6").visible(true)
                    .plottable(false).build();

            Attribute created =
                    new Attribute.Builder(CORE_NAME + "created").name("Created").visible(true).plottable(false).build();

            Attribute updated =
                    new Attribute.Builder(CORE_NAME + "updated").name("Updated").visible(true).plottable(false).build();

            Attribute hostId =
                    new Attribute.Builder(CORE_NAME + "hostid").name("Host ID").visible(true).plottable(false).build();

            // Relationships
            Attribute region = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsRegionType.TYPE_LOCAL_ID)).name("Region").visible(true)
                            .plottable(false).build();

            Attribute project = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsProjectType.TYPE_LOCAL_ID)).name("Project")
                            .visible(true).plottable(false).build();

            Attribute image = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsImageType.TYPE_LOCAL_ID)).name("Image").visible(true)
                            .plottable(false).build();

            Attribute workload = new Attribute.Builder(RelationshipUtil.getRelationshipNameBetweenLocalTypeIds(
                    provider.getProviderType(), TYPE_LOCAL_ID, OsWorkloadType.TYPE_LOCAL_ID)).name("Workload")
                            .visible(true).plottable(false).build();

            addAttributes(status, flavor, vcpus, ram, disk, flavorId, accessIPv4, accessIPv6, created, updated, hostId,
                    region, project, image, workload);

            Set<OrderedString> sortOperations = new OperationBuilder().add(status, vcpus, ram, disk, flavorId, flavor,
                    accessIPv4, accessIPv6, created, updated, hostId, region, project, image, workload).build();

            Set<OrderedString> groupOperations = new OperationBuilder()
                    .add(project, status, flavor, vcpus, ram, disk, flavorId, region, image, workload).build();


            addOperations(DefaultOperations.SORT_BY.toString(), sortOperations);
            addOperations(DefaultOperations.GROUP_BY.toString(), groupOperations);

            addOperations("normaliseRam", new HashSet<>(0));

            Map<String, String> powerRange = new HashMap<String, String>();

            powerRange.put("start", "Start");
            powerRange.put("stop", "Stop");
            powerRange.put("softReboot", "Soft Reboot");
            powerRange.put("hardReboot", "Hard Reboot");

            ActionParameters powerParameters = new ActionParameters();

            try {
                powerParameters.add(
                        new ActionParameter("power", ActionParameter.Type.ENUMERATED, "power options", powerRange));
            } catch (InvalidActionSpecificationException e) {
                // should never happen as the action id is properly set
                LOG.error("Unexpected error - action ID not set?", e);
            }

            try {
                // FOR ITEMS
                // single power action
                addAction("item", new Action("power", "Change instance status",
                        "Powers on/off or reboots a single Instance", "icon-cycle", powerParameters));

                // FOR AGGREGATIONS
                addAction("aggregation", new Action("power", "Change instance status",
                        "Powers on/off or reboots instances in an aggregation", "icon-cycle", powerParameters));
            } catch (InvalidActionSpecificationException e) {
                // should never happen as action id is properly set
                LOG.error("Unexpected error - action ID not set?", e);
            }
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsInstanceType attributes: ", e);
        }
    }
}
