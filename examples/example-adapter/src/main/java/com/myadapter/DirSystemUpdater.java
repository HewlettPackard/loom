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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

/**
 * DirSystemUpdater - this creates the DirItems based on the directories.
 */
public class DirSystemUpdater extends AggregationUpdater<DirItem, DirItemAttributes, File> {

    protected Map<String, DirItemAttributes> dirItems = new HashMap<>();

    /**
     * Constructs a DirSystemUpdater.
     *
     * @param aggregation The aggregation this update will update
     * @param adapter The baseAdapter this updater is part of
     * @param fileSystemCollector The collector it uses
     * @throws NoSuchItemTypeException Thrown if the itemtype isn't found
     * @throws NoSuchProviderException thrown if adapter is not known
     */
    public DirSystemUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final FileSystemCollector fileSystemCollector) throws NoSuchItemTypeException, NoSuchProviderException {
        super(aggregation, adapter, fileSystemCollector);
    }


    @Override
    protected String getItemId(final File resource) {
        return resource.getAbsolutePath();
    }

    @Override
    protected Iterator<File> getResourceIterator() {
        // File f = new File("C://development//fileMonitorDir");
        File f = FileUtils.getFileRoot(adapter);
        List<File> dirs = new ArrayList<File>();
        dirs.add(f);
        FileUtils.getDirs(f, dirs);
        return dirs.iterator();
    }

    @Override
    protected DirItem createEmptyItem(final String logicalId) {
        DirItem item = new DirItem(logicalId, itemType);
        return item;
    }

    @Override
    protected DirItemAttributes createItemAttributes(final File resource) {
        DirItemAttributes dia = new DirItemAttributes();
        dia.setDirname(resource.getName());
        dia.setItemId(resource.getAbsolutePath());
        dia.setItemName(resource.getName());
        String path = resource.getParent();
        if (path != null) {
            dia.setPath(path);
        }
        dirItems.put(dia.getItemId(), dia);
        return dia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final DirItemAttributes dia, final File resource) {
        if (dia.getDirname() != null && !dia.getDirname().equals(resource.getName())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem dirItem, final File resource) {
        // no rels
    }

}
