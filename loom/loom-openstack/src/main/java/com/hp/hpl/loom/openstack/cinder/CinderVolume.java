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
package com.hp.hpl.loom.openstack.cinder;

import java.util.Iterator;

import com.hp.hpl.loom.openstack.cinder.model.JsonVolume;
import com.hp.hpl.loom.openstack.cinder.model.JsonVolumes;

/**
 * Interface for the cinder volume API call.
 */
public interface CinderVolume {
    /**
     * @return get the cinder volume Iterator
     */
    Iterator<JsonVolume> getIterator();

    JsonVolumes createVolume(String projectId, JsonVolumes volumes);
}
