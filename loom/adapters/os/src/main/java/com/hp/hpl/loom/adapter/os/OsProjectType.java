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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;

public class OsProjectType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsProjectType.class);
    public static final String TYPE_LOCAL_ID = "project";

    public OsProjectType() {
        super(TYPE_LOCAL_ID);
        try {
            // Static/rarely modified attributes
            Attribute description = new Attribute.Builder(CORE_NAME + "description").name("Description").visible(true)
                    .plottable(false).build();

            Attribute instancesQuota =
                    new NumericAttribute.Builder(CORE_NAME + "instancesQuota").name("Instances Quota").visible(true)
                            .plottable(true).min("0").max("Inf").unit("Instances").build();

            Attribute instancesQuotaUtilisation =
                    new NumericAttribute.Builder("instancesQuotaUtilisation").name("Instances Quota Utilisation")
                            .visible(true).plottable(true).min("0").max("100").unit("%").build();

            Attribute coresQuota = new NumericAttribute.Builder(CORE_NAME + "coresQuota").name("Cores Quota")
                    .visible(true).plottable(true).min("0").max("Inf").unit("Cores").build();

            Attribute coresQuotaUtilisation =
                    new NumericAttribute.Builder("coresQuotaUtilisation").name("Cores Quota Utilisation").visible(true)
                            .plottable(true).min("0").max("100").unit("%").build();

            Attribute ramQuota = new NumericAttribute.Builder(CORE_NAME + "ramQuota").name("RAM Quota").visible(true)
                    .plottable(true).min("0").max("Inf").unit("MB").build();

            Attribute ramQuotaUtilisation = new NumericAttribute.Builder("ramQuotaUtilisation")
                    .name("RAM Quota Utilisation").visible(true).plottable(true).min("0").max("100").unit("%").build();

            Attribute volumesQuota = new NumericAttribute.Builder(CORE_NAME + "volumesQuota").name("Volumes Quota")
                    .visible(true).plottable(true).min("0").max("Inf").unit("Instances").build();

            Attribute volumesQuotaUtilisation =
                    new NumericAttribute.Builder("volumesQuotaUtilisation").name("Volumes Quota Utilisation")
                            .visible(true).plottable(true).min("0").max("100").unit("%").build();

            Attribute gigabytesQuota = new NumericAttribute.Builder(CORE_NAME + "gigabytesQuota")
                    .name("Gigabytes Quota").visible(true).plottable(true).min("0").max("Inf").unit("GB").build();

            Attribute gigabytesQuotaUtilisation =
                    new NumericAttribute.Builder("gigabytesQuotaUtilisation").name("Gigabytes Quota Utilisation")
                            .visible(true).plottable(true).min("0").max("100").unit("%").build();

            Attribute floatingIpsQuota = new NumericAttribute.Builder(CORE_NAME + "floatingIpsQuota")
                    .name("Floating IPs Quota").visible(true).plottable(true).min("0").max("Inf").unit("IPs").build();

            Attribute injectedFilesQuota =
                    new NumericAttribute.Builder(CORE_NAME + "injectedFilesQuota").name("Injected Files Quota")
                            .visible(true).plottable(true).min("0").max("Inf").unit("Files").build();

            Attribute injectedFileContentBytes =
                    new NumericAttribute.Builder(CORE_NAME + "injectedFileContentBytes").name("Injected File Content")
                            .visible(true).plottable(true).min("0").max("Inf").unit("Bytes").build();

            Attribute securityGroupsQuota =
                    new NumericAttribute.Builder(CORE_NAME + "securityGroupsQuota").name("Security Groups Quota")
                            .visible(true).plottable(true).min("0").max("Inf").unit("Groups").build();

            Attribute securityGroupRulesQuota = new NumericAttribute.Builder(CORE_NAME + "securityGroupRulesQuota")
                    .name("Security Group Rules Quota").visible(true).plottable(true).min("0").max("Inf").unit("Rules")
                    .build();

            Attribute providerId = new Attribute.Builder(CORE_NAME + "providerId").name("Provider ID").visible(true)
                    .plottable(false).build();

            addAttributes(description, instancesQuota, instancesQuotaUtilisation, coresQuota, coresQuotaUtilisation,
                    ramQuota, ramQuotaUtilisation, volumesQuota, volumesQuotaUtilisation, gigabytesQuota,
                    gigabytesQuotaUtilisation, floatingIpsQuota, injectedFilesQuota, injectedFileContentBytes,
                    securityGroupsQuota, securityGroupRulesQuota, providerId);


            this.addOperations(DefaultOperations.SORT_BY.toString(),
                    new OperationBuilder().add(instancesQuota, instancesQuotaUtilisation, coresQuota,
                            coresQuotaUtilisation, ramQuota, ramQuotaUtilisation, volumesQuota, volumesQuotaUtilisation,
                            gigabytesQuota, gigabytesQuotaUtilisation, floatingIpsQuota, injectedFilesQuota,
                            injectedFileContentBytes, securityGroupsQuota, securityGroupRulesQuota, providerId,
                            description).build());
            this.addOperations(DefaultOperations.GROUP_BY.toString(), new OperationBuilder()
                    .add(instancesQuota, coresQuota, ramQuota, volumesQuota, gigabytesQuota, floatingIpsQuota,
                            injectedFilesQuota, injectedFileContentBytes, securityGroupsQuota, securityGroupRulesQuota)
                    .build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing OsProjectType attributes: ", e);
        }

        // TODO: Not sure how to calculate these figures
        // addAttribute(new NumericAttribute("injectedFilesQuotaUtilisation",
        // "Injected Files Quota Utilisation", true, true, Arrays.asList(new OrderedString(
        // DefaultOperations.SORT_BY.toString(), 4)), "0", "100", "%"));
        // addAttribute(new NumericAttribute("securityGroupsQuotaUtilisation",
        // "Security Groups Quota Utilisation", true, true, Arrays.asList(new OrderedString(
        // DefaultOperations.SORT_BY.toString(), 4)), "0", "100", "%"));
        // addAttribute(new NumericAttribute("securityGroupRulesQuotaUtilisation",
        // "Security Group Rules Quota Utilisation", true, true, Arrays.asList(new OrderedString(
        // DefaultOperations.SORT_BY.toString(), 4)), "0", "100", "%"));
        // addAttribute(new NumericAttribute("floatingIpsQuotaUtilisation",
        // "Floating IPs Quota Utilisation",
        // true, true, Arrays.asList(new OrderedString(DefaultOperations.SORT_BY.toString(), 4)),
        // "0",
        // "100", "%"));
    }
}
