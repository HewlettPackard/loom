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
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;

public class OsRegionType extends OsItemType {
    private static final Log LOG = LogFactory.getLog(OsRegionType.class);
    public static final String TYPE_LOCAL_ID = "region";

    public OsRegionType() {
        super(TYPE_LOCAL_ID);
        try {
            /* attributes */
            Attribute providerId = new Attribute.Builder(CORE_NAME + "providerId").name("Provider ID").visible(true)
                    .plottable(false).build();

            this.addAttributes(providerId);
            this.addOperations(DefaultOperations.SORT_BY.toString(), new OperationBuilder().add(providerId).build());

        } catch (AttributeException e) {
            LOG.error("Problem constructing OsRegionType attributes: ", e);
        }
    }
}
