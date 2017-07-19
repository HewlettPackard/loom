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
package com.hp.hpl.loom.adapter.os.real;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.os.OsAggregationUpdater;
import com.hp.hpl.loom.adapter.os.OsImage;
import com.hp.hpl.loom.adapter.os.OsImageAttributes;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.openstack.NoSupportedApiVersion;
import com.hp.hpl.loom.openstack.glance.model.JsonImage;

public class RealImagesUpdater extends OsAggregationUpdater<OsImage, OsImageAttributes, JsonImage> {
    private static final Log LOG = LogFactory.getLog(RealImagesUpdater.class);


    private RealItemCollector ric;

    public RealImagesUpdater(final Aggregation aggregation, final BaseAdapter adapter, final String itemTypeLocalId,
            final RealItemCollector ric) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemTypeLocalId, ric);
        this.ric = ric;
    }

    @Override
    protected String getItemId(final JsonImage resource) {
        return resource.getId();
    }

    @Override
    protected Iterator<? extends JsonImage> getResources(final int prjIdx, final int regIdx) {
        String regionId = regions[regIdx];
        String projectId = projects.get(prjIdx).getCore().getItemId();
        String imageVersion = adapter.getAdapterConfig().getPropertiesConfiguration()
                .getString(Constants.IMAGE + Constants.VERSION_SUFFIX);

        String[] imageVersions = {};
        if (imageVersion != null) {
            imageVersions = imageVersion.split(",");
        }

        // List<JsonImage> images = new ArrayList<>();
        // JsonImages jsonImages;
        try {
            return ric.getOpenstackApi().getGlanceApi(imageVersions, projectId, regionId).getGlanceImage()
                    .getIterator();
        } catch (NoSupportedApiVersion e) {
            throw new RuntimeException("Problem accessing compute images for version: " + imageVersions + " projectId: "
                    + projectId + " regionId: " + regionId);
        }
        // if (jsonImages != null) {
        // return jsonImages.getImages().iterator();
        // } else {
        // return images.iterator();
        // }
    }

    @Override
    protected String[] getConfiguredRegions(final String projectName) {
        String projectId = getProjectId(projectName);
        return ric.getOpenstackApi().getTokenManager().getTokenHolder().getRegions(projectId, Constants.COMPUTE,
                Constants.PUBLIC);
    }

    @Override
    protected OsImage createEmptyItem(final String logicalId) {
        return new OsImage(logicalId, itemType);
    }

    @Override
    protected OsImageAttributes createItemAttributes(final JsonImage image) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("REAL: project: " + currentProject.getName() + " found image: " + image.getName() + " id: "
                    + image.getId());
        }
        OsImageAttributes oia = new OsImageAttributes();
        oia.setItemName(image.getName());
        oia.setItemId(image.getId());
        if (image.getCreated() != null) {
            oia.setCreated(image.getCreated().toString());
        }
        if (image.getUpdated() != null) {
            oia.setUpdated(image.getUpdated().toString());
        }
        if (image.getStatus() != null) {
            oia.setStatus(image.getStatus().toString());
        }
        oia.setMinDisk(image.getMinDisk());
        oia.setMinRam(image.getMinRam());

        oia.setChecksum(image.getChecksum());
        oia.setContainerFormat(image.getContainerFormat());
        oia.setDiskFormat(image.getDiskFormat());
        oia.setSize(image.getSize());
        oia.setVisibility(image.getVisibility());

        return oia;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final OsImageAttributes oia, final JsonImage image) {
        if (oia.getStatus() != null && !oia.getStatus().toString().equals(image.getStatus().toString())) {
            return ChangeStatus.CHANGED_UPDATE;
        } else {
            return ChangeStatus.UNCHANGED;
        }
    }

    @Override
    protected void setRelationships(final ConnectedItem osImage, final JsonImage image) {
        osImage.setRelationship(adapter.getProvider(), OsRegionType.TYPE_LOCAL_ID, currentRegion);
        // images are not tied to a project - region scope?
        // osImage.setRelationship(OsProjectType.TYPE_LOCAL_ID, currentProject.getCore().getId());
    }
}
