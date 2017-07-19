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
package com.hp.hpl.loom.adapter.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameter;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.Session;

/**
 * Providers a updater for the FileSystemAdapter.
 *
 */
public class FileSystemCollector extends AggregationUpdaterBasedItemCollector {
    /**
     * Constructor it takes a client session, adapter and adapter Manager to register back with.
     *
     * @param session - Client session
     * @param adapter - base adapter (the file adapter)
     * @param adapterManager adapterManager to register ourselves with
     */
    public FileSystemCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager) {
        super(session, adapter, adapterManager);
    }

    @Override
    protected AggregationUpdater<?, ?, ?> getAggregationUpdater(final Aggregation aggregation)
            throws NoSuchProviderException, NoSuchItemTypeException {

        if (aggregationMatchesItemType(aggregation, Types.FILE_TYPE_LOCAL_ID)) {
            return new FileSystemUpdater(aggregation, adapter, this);
        }
        // itemType = adapter.getItemType(Types.DIR_TYPE_LOCAL_ID);
        if (aggregationMatchesItemType(aggregation, Types.DIR_TYPE_LOCAL_ID)) {
            return new DirSystemUpdater(aggregation, adapter, this);
        }

        throw new NoSuchItemTypeException(aggregation.getTypeId());
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(Types.FILE_TYPE_LOCAL_ID);
        list.add(Types.DIR_TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        List<String> list = new ArrayList<String>();
        list.add(Types.FILE_TYPE_LOCAL_ID);
        list.add(Types.DIR_TYPE_LOCAL_ID);
        return list;
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items)
            throws InvalidActionSpecificationException {
        ActionParameters params = action.getParams();
        boolean result = true;
        // all the actions only work on a single item at the moment
        FileItem item = null;

        if (action.getId().equals("newFile")) {
            try {
                String newFilename = action.getParams().get(0).getValue();
                File f = FileUtils.getFileRoot(adapter);
                Path path = Paths.get(f.getCanonicalPath(), newFilename);
                File newFile = path.toFile();
                boolean newFileCreated = newFile.createNewFile();
                if (newFileCreated) {
                    return new ActionResult(ActionResult.Status.completed);
                } else {
                    return new ActionResult(ActionResult.Status.aborted);
                }
            } catch (IOException ex) {
                return new ActionResult(ActionResult.Status.aborted);
            }
        } else {
            if (items.size() == 1) {
                item = (FileItem) items.toArray()[0];
                FileItemAttributes itemAttributes = item.getCore();
                File file = new File(itemAttributes.getPath() + File.separatorChar + itemAttributes.getFilename());

                if (action.getId().equals("rename")) {
                    for (int i = 0; i < params.size(); i++) {
                        ActionParameter actionParameter = params.get(i);
                        if (actionParameter.getId().equals("name")) {
                            result = file
                                    .renameTo(new File(itemAttributes.getPath() + "/" + actionParameter.getValue()));
                        }
                    }
                } else if (action.getId().equals("delete")) {
                    for (int i = 0; i < params.size(); i++) {
                        ActionParameter actionParameter = params.get(i);
                        if (actionParameter.getValue().equals("yes")) {
                            result = file.delete();
                        }
                    }
                } else if (action.getId().equals("readonly")) {
                    for (int i = 0; i < params.size(); i++) {
                        ActionParameter actionParameter = params.get(i);
                        if (actionParameter.getValue().equals("yes")) {
                            result = file.setReadOnly();
                        } else {
                            result = file.setWritable(true);
                        }
                    }
                }
            }
        }
        if (result) {
            return new ActionResult(ActionResult.Status.completed);
        } else {
            return new ActionResult(ActionResult.Status.aborted);
        }
    }

}
