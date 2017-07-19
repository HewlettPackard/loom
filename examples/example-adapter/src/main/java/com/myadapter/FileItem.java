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
package com.myadapter;

import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionParameter;
import com.hp.hpl.loom.adapter.annotations.ActionRange;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;

import com.hp.hpl.loom.adapter.annotations.ActionTypes;

/**
 * FileItem represents a fileItem.
 *
 */
@ItemTypeInfo(value = Types.FILE_TYPE_LOCAL_ID, sorting = {
        @Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Size", "Path", "Filename", "Directory", "Value"}),
        @Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Directory"})},
        supportedAdditionalOperations = {FileSystemAdapter.EXTRACT_OP})
@ConnectedTo(toClass = DirItem.class, relationshipDetails = @LoomAttribute(key = "Directory", supportedOperations = {
        DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}), type = "contains", typeName = "Contains")
@ConnectedTo(toClass = DirItem.class, relationshipDetails = @LoomAttribute(key = "Directory", supportedOperations = {
        DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}), type = "ancestor", typeName = "Ancestor")
@ActionDefinition(id = "rename", name = "Rename", type = ActionTypes.Item, icon = "icon-rename",
        description = "Rename file", parameters = {@ActionParameter(id = "name", name = "Filename",
                type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
@ActionDefinition(id = "delete", name = "Delete", type = ActionTypes.Item, icon = "icon-delete",
        description = "Delete file", parameters = {@ActionParameter(id = "confirm", name = "Confirmation",
                type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, ranges = {
                        @ActionRange(id = "no", name = "No"), @ActionRange(id = "yes", name = "Yes")})})
@ActionDefinition(id = "readonly", name = "Readonly", type = ActionTypes.Item, icon = "icon-readonly",
        description = "Readonly", parameters = {@ActionParameter(id = "readonly", name = "Confirmation",
                type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, ranges = {
                        @ActionRange(id = "no", name = "No"), @ActionRange(id = "yes", name = "Yes")})})

@ActionDefinition(id = "newFile", name = "New File", type = ActionTypes.Thread, icon = "icon-readonly",
description = "New File", parameters = {@ActionParameter(id = "filename", name = "filename",
        type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
public class FileItem extends BaseItem<FileItemAttributes> {

    /**
     * Default constructor.
     *
     * @param itemType The item type
     */
    public FileItem(final ItemType itemType) {
        super(null, itemType);
    }

    /**
     * Constructs a FileItem using the provided logicalId and itemType.
     *
     * @param logicalId The logical id
     * @param itemType The itemType
     */
    public FileItem(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }
}
