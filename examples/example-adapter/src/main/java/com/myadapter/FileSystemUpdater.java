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
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;

/**
 * FileSystemUpdater - this creates the FileItems based on the files on the file system.
 */
public class FileSystemUpdater extends AggregationUpdater<FileItem, FileItemAttributes, File> {
    private static final int MAX_NUMBER_OF_SIZES = 10;

    protected FileSystemCollector fileSystemCollector = null;

    private Map<String, List<Long>> sizes = new HashMap<>();
    private Map<String, Double> latitudes = new HashMap<>();
    private Map<String, Double> longitudes = new HashMap<>();

    private Random rand = new Random();

    /**
     * Constructs a FileSystemUpdater.
     *
     * @param aggregation The aggregation this update will update
     * @param adapter The baseAdapter this updater is part of
     * @param fileSystemCollector The collector it uses
     * @throws NoSuchItemTypeException Thrown if the itemtype isn't found
     * @throws NoSuchProviderException thrown if adapter is not known
     */
    public FileSystemUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final FileSystemCollector fileSystemCollector) throws NoSuchItemTypeException, NoSuchProviderException {
        super(aggregation, adapter, fileSystemCollector);
        this.fileSystemCollector = fileSystemCollector;
    }


    @Override
    protected String getItemId(final File resource) {
        return resource.getAbsolutePath();
    }


    @Override
    protected Iterator<File> getResourceIterator() {
        // File f = new File("C://development//fileMonitorDir");
        File f = FileUtils.getFileRoot(adapter);
        List<File> files = new ArrayList<File>();
        getFiles(f, files);
        return files.iterator();
    }

    private void getFiles(final File f, final Collection<File> currentFiles) {
        Collections.addAll(currentFiles, f.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.isFile();
            }
        }));

        File[] dirs = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return !f.isFile();
            }
        });

        for (File file : dirs) {
            this.getFiles(file, currentFiles);
        }
    }


    @Override
    protected FileItem createEmptyItem(final String logicalId) {
        FileItem item = new FileItem(logicalId, itemType);
        return item;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    protected FileItemAttributes createItemAttributes(final File resource) {
        FileItemAttributes fia = new FileItemAttributes();
        fia.setItemName(resource.getName());
        fia.setFilename(resource.getName());
        String path = resource.getParent();
        if (path != null) {
            fia.setPath(path);
        }
        fia.setItemId(resource.getAbsolutePath());
        fia.setSize(resource.length());

        List<Long> sizeList = sizes.get(fia.getItemId());
        if (sizeList == null) {
            sizeList = new ArrayList<Long>(MAX_NUMBER_OF_SIZES);
            sizes.put(fia.getItemId(), sizeList);
        }
        sizeList.add(fia.getSize());
        if (sizeList.size() > MAX_NUMBER_OF_SIZES) {
            sizeList = sizeList.subList(sizeList.size() - MAX_NUMBER_OF_SIZES, sizeList.size());
        }

        Long[] sizesArray = new Long[sizeList.size()];
        int x = 0;
        List<Long> newSizeList = new ArrayList<>();
        for (Long l : sizeList) {
            sizesArray[x] = l;
            x++;
            newSizeList.add(l);
        }

        fia.setSizes(sizesArray);
        fia.setSizesList(newSizeList);

        fia.setReadonly(Boolean.toString(!resource.canWrite()));
        String filePath = resource.getAbsolutePath();
        if (latitudes.get(filePath) == null) {
            latitudes.put(filePath, 51.5072 + (rand.nextDouble() - 0.25));
            longitudes.put(filePath, 0.1275 + (rand.nextDouble() - 0.25));
        }
        Double latitude = latitudes.get(filePath);
        Double longitude = longitudes.get(filePath);

        fia.setLatitude(latitude);
        fia.setLongitude(longitude);
        fia.setCountry("GBR");

        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(resource.toPath(), BasicFileAttributes.class);
            fia.setCreated(attr.creationTime().toString());
            if (attr.lastModifiedTime() == null || attr.lastModifiedTime().toString() == null) {
                fia.setUpdated(attr.creationTime().toString());
            } else {
                fia.setUpdated(attr.lastModifiedTime().toString());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return fia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final FileItemAttributes fia, final File resource) {
        return ChangeStatus.CHANGED_UPDATE;
        // return fia.getFilename() != null && !fia.getFilename().equals(resource.getName())
        // || fia.getSize() != resource.length() || Boolean.parseBoolean(fia.getReadonly()) ==
        // resource.canWrite();
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    protected void setRelationships(final ConnectedItem fileItem, final File resource) {
        File dir = resource.getParentFile();
        if (dir != null) {
            fileItem.setRelationshipWithType(this.adapter.getProvider(), Types.DIR_TYPE_LOCAL_ID, dir.getAbsolutePath(), "contains");
        }

        List<File> dirs = FileUtils.getAllAncestors(FileUtils.getFileRoot(adapter), dir);
        for (File file : dirs) {
            fileItem.setRelationshipWithType(this.adapter.getProvider(), Types.DIR_TYPE_LOCAL_ID, file.getAbsolutePath(), "ancestor");
        }
    }
}
