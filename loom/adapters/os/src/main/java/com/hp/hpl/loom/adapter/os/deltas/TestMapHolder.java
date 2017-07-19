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
package com.hp.hpl.loom.adapter.os.deltas;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.loom.adapter.os.OsImageType;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsNetworkType;
import com.hp.hpl.loom.adapter.os.OsProjectType;
import com.hp.hpl.loom.adapter.os.OsRegionType;
import com.hp.hpl.loom.adapter.os.OsSubnetType;
import com.hp.hpl.loom.adapter.os.OsVolumeType;

public class TestMapHolder {

    private Map<String, TestUpdateCount> testMap;
    private int aggCount = 0;

    public TestMapHolder() {
        testMap = new HashMap<>();
    }

    private void setMap(final Map<String, TestUpdateCount> testMap) {
        this.testMap = testMap;
    }

    public int getAggCount() {
        return aggCount;
    }

    public void setAggCount(final int aggCount) {
        this.aggCount = aggCount;
    }

    public TestMapHolder copy() {
        TestMapHolder newTmh = new TestMapHolder();
        newTmh.setMap(new HashMap<>(testMap));
        newTmh.setAggCount(aggCount);
        return newTmh;
    }

    public void setInstance(final int allNbr, final int newNbr, final int updatedNbr, final int deletedNbr,
            final int delDeltaNbr, final int relDeltaNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        thc.setUpdatedNbr(updatedNbr);
        thc.setDeletedNbr(deletedNbr);
        thc.setDeletedDeltaNbr(delDeltaNbr);
        thc.setRelDeltaNbr(relDeltaNbr);
        testMap.put(OsInstanceType.TYPE_LOCAL_ID, thc);
    }

    public void setProject(final int allNbr, final int newNbr, final int updatedNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        thc.setUpdatedNbr(updatedNbr);
        testMap.put(OsProjectType.TYPE_LOCAL_ID, thc);
    }

    public void setRegion(final int allNbr, final int newNbr, final int updatedNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        thc.setUpdatedNbr(updatedNbr);
        testMap.put(OsRegionType.TYPE_LOCAL_ID, thc);
    }

    public void setVolume(final int allNbr, final int newNbr, final int updatedNbr, final int deletedNbr,
            final int delDeltaNbr, final int relDeltaNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        thc.setUpdatedNbr(updatedNbr);
        thc.setDeletedNbr(deletedNbr);
        thc.setDeletedDeltaNbr(delDeltaNbr);
        thc.setRelDeltaNbr(relDeltaNbr);
        testMap.put(OsVolumeType.TYPE_LOCAL_ID, thc);
    }

    public void setImage(final int allNbr, final int newNbr, final int updatedNbr, final int relDeltaNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        thc.setUpdatedNbr(updatedNbr);
        thc.setRelDeltaNbr(relDeltaNbr);
        testMap.put(OsImageType.TYPE_LOCAL_ID, thc);
    }

    public void setSubnet(final int allNbr, final int newNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        testMap.put(OsSubnetType.TYPE_LOCAL_ID, thc);
    }

    public void setNetwork(final int allNbr, final int newNbr) {
        TestUpdateCount thc = new TestUpdateCount();
        thc.setAllNbr(allNbr);
        thc.setNewNbr(newNbr);
        testMap.put(OsNetworkType.TYPE_LOCAL_ID, thc);
    }

    public TestUpdateCount getNbrs(final String typeLocalId) {
        return testMap.get(typeLocalId);
    }

}
