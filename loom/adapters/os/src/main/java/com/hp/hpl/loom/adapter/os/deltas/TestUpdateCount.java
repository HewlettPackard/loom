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

public class TestUpdateCount {

    private int newNbr;
    private int allNbr;
    private int deletedNbr;
    private int updatedNbr;
    private int deletedDeltaNbr;
    private int relDeltaNbr;
    private int attrDeltaNbr;


    public void setNbrs(final int allNbr, final int newNbr, final int deletedNbr, final int updatedNbr,
            final int deletedDeltaNbr, final int relDeltaNbr, final int attrDeltaNbr) {
        setAllNbr(allNbr);
        setNewNbr(newNbr);
        setDeletedNbr(deletedNbr);
        setUpdatedNbr(updatedNbr);
        setDeletedDeltaNbr(deletedDeltaNbr);
        setRelDeltaNbr(relDeltaNbr);
        setAttrDeltaNbr(attrDeltaNbr);
    }

    public int getNewNbr() {
        return newNbr;
    }

    public void setNewNbr(final int newNbr) {
        this.newNbr = newNbr;
    }

    public int getAllNbr() {
        return allNbr;
    }

    public void setAllNbr(final int allNbr) {
        this.allNbr = allNbr;
    }

    public int getDeletedNbr() {
        return deletedNbr;
    }

    public void setDeletedNbr(final int deletedNbr) {
        this.deletedNbr = deletedNbr;
    }

    public int getUpdatedNbr() {
        return updatedNbr;
    }

    public void setUpdatedNbr(final int updatedNbr) {
        this.updatedNbr = updatedNbr;
    }

    public int getDeletedDeltaNbr() {
        return deletedDeltaNbr;
    }

    public void setDeletedDeltaNbr(final int deletedDeltaNbr) {
        this.deletedDeltaNbr = deletedDeltaNbr;
    }

    public int getRelDeltaNbr() {
        return relDeltaNbr;
    }

    public void setRelDeltaNbr(final int relDeltaNbr) {
        this.relDeltaNbr = relDeltaNbr;
    }

    public int getAttrDeltaNbr() {
        return attrDeltaNbr;
    }

    public void setAttrDeltaNbr(final int attrDeltaNbr) {
        this.attrDeltaNbr = attrDeltaNbr;
    }
}
