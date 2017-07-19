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


import java.util.List;

import com.hp.hpl.loom.adapter.CollectionType;
import com.hp.hpl.loom.adapter.GeoAttribute;
import com.hp.hpl.loom.adapter.LiteralAttribute;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.TimeAttribute;
import com.hp.hpl.loom.adapter.annotations.LiteralRange;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.CoreItemAttributes;

/**
 * This models the FileItem Attributes.
 *
 */
public class FileItemAttributes extends CoreItemAttributes {
    @LoomAttribute(key = "Filename", supportedOperations = {DefaultOperations.SORT_BY})
    private String filename;

    @LoomAttribute(key = "Path", supportedOperations = {DefaultOperations.SORT_BY})
    private String path;

    @LoomAttribute(key = "Size", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "1000000000000", unit = "Bytes")
    private long size;

    @LoomAttribute(key = "Sizes", supportedOperations = {DefaultOperations.SORT_BY}, plottable = false,
            type = NumericAttribute.class, min = "0", max = "1000000000000", unit = "Bytes",
            collectionType = CollectionType.ARRAY, ignoreUpdate = true)
    private Long[] sizes;

    @LoomAttribute(key = "SizesList", supportedOperations = {DefaultOperations.SORT_BY}, plottable = false,
            type = NumericAttribute.class, min = "0", max = "1000000000000", unit = "Bytes",
            collectionType = CollectionType.ARRAY, ignoreUpdate = true)
    private List<Long> sizesList;

    @LoomAttribute(key = "Value", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "1", unit = "")
    private float value;

    @LoomAttribute(key = "Readonly", supportedOperations = {DefaultOperations.SORT_BY}, plottable = false,
            type = LiteralAttribute.class, range = {@LiteralRange(key = "true", name = "True"),
                    @LiteralRange(key = "false", name = "False")})
    private String readonly;

    @LoomAttribute(key = "latitude",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.KMEANS,
                    DefaultOperations.POLYGON_CLUSTERING, DefaultOperations.GRID_CLUSTERING,
                    DefaultOperations.FILTER_BY_REGION}, type = GeoAttribute.class, group = "latLong", latitude = true)
    private Double latitude;

    @LoomAttribute(key = "longitude",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.KMEANS,
                    DefaultOperations.POLYGON_CLUSTERING, DefaultOperations.GRID_CLUSTERING,
                    DefaultOperations.FILTER_BY_REGION}, type = GeoAttribute.class, group = "latLong", longitude = true)
    private Double longitude;

    @LoomAttribute(key = "created",
            supportedOperations = {DefaultOperations.SORT_BY}, type = TimeAttribute.class, format = "MMM dd, YYYY",
            shortFormat = "MMM dd", plottable = true)
    private String created;

    @LoomAttribute(key = "updated",
            supportedOperations = {DefaultOperations.SORT_BY}, type = TimeAttribute.class, format = "MMM dd, YYYY",
            shortFormat = "MMM dd", plottable = true)
    private String updated;


    @LoomAttribute(key = "country", supportedOperations = {DefaultOperations.SORT_BY}, type = GeoAttribute.class,
            group = "latLong", country = true)
    private String country;

    /**
     * Default constructor.
     */
    public FileItemAttributes() {}

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * Get readonly flag.
     *
     * @return the readonly flag
     */
    public String getReadonly() {
        return readonly;
    }

    /**
     * Set readonly flag.
     *
     * @param readonly The readonly string
     */
    public void setReadonly(final String readonly) {
        this.readonly = readonly;
    }

    /**
     * @param value of the file
     */
    public void setValue(final float value) {
        this.value = value;
    }

    /**
     * Get value.
     *
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @return the sizes
     */
    public Long[] getSizes() {
        return sizes;
    }

    /**
     * @param sizes the sizes to set
     */
    public void setSizes(final Long[] sizes) {
        this.sizes = sizes;
    }

    /**
     * @return the sizesList
     */
    public List<Long> getSizesList() {
        return sizesList;
    }

    /**
     * @param sizesList the sizesList to set
     */
    public void setSizesList(final List<Long> sizesList) {
        this.sizesList = sizesList;
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(final String country) {
        this.country = country;
    }

    /**
     * @return the created
     */
    public String getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * @return the updated
     */
    public String getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(String updated) {
        this.updated = updated;
    }


}
