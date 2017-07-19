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
package com.hp.hpl.loom.adapter.load;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.Attribute;
import com.hp.hpl.loom.adapter.OperationBuilder;
import com.hp.hpl.loom.exceptions.AttributeException;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;

public class LoadItemType extends ItemType {
    private static final Log LOG = LogFactory.getLog(LoadItemType.class);
    private static String typeLocalId = "load";

    public LoadItemType() {
        super(typeLocalId);

    }

    @Override
    public void setId(final String id) {
        typeLocalId = id;
        super.setId(id);
    }

    public void addAttribute(final String attrName) {
        try {
            Attribute attr = new Attribute.Builder(attrName).name(attrName).visible(true).plottable(false).build();
            LOG.info("setting a new Attribute on ItemtType: " + getLocalId() + " : " + attrName);
            this.addAttributes(attr);
            this.addOperations(DefaultOperations.GROUP_BY.toString(), new OperationBuilder().add(attr).build());
        } catch (AttributeException e) {
            LOG.error("Problem constructing UserType attributes: ", e);
        }
    }

}
