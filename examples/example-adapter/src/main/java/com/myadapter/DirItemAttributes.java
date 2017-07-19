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

import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.CoreItemAttributes;

/**
 * Attributes of the DirItem.
 */
public class DirItemAttributes extends CoreItemAttributes {

    @LoomAttribute(key = "dirname", supportedOperations = {DefaultOperations.SORT_BY})
    private String dirname;

    @LoomAttribute(key = "path", supportedOperations = {DefaultOperations.SORT_BY})
    private String path;

    /**
     * Default constructor.
     */
    public DirItemAttributes() {}

    /**
     * @return the dirname
     */
    public String getDirname() {
        return dirname;
    }

    /**
     * @param dirname the dirname to set
     */
    public void setDirname(final String dirname) {
        this.dirname = dirname;
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
}
