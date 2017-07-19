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
package com.hp.hpl.loom.adapter.cubesensors;


import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.CoreItemAttributes;

/**
 * This models the DeviceItem Attributes.
 *
 */
public class DeviceItemAttributes extends CoreItemAttributes {
    @LoomAttribute(key = "Type", supportedOperations = {DefaultOperations.SORT_BY})
    private String type;

    @LoomAttribute(key = "RoomType", supportedOperations = {DefaultOperations.SORT_BY})
    private String roomType;

    @LoomAttribute(key = "Name", supportedOperations = {DefaultOperations.SORT_BY})
    private String name;

    @LoomAttribute(key = "uid", supportedOperations = {DefaultOperations.SORT_BY})
    private String uid;

    @LoomAttribute(key = "Last Updated", supportedOperations = {DefaultOperations.SORT_BY})
    private String time;

    @LoomAttribute(key = "Battery", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "100", unit = "%")
    private int battery;

    @LoomAttribute(key = "Humidity", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "100", unit = "%")
    private int humidity;

    @LoomAttribute(key = "Light", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "25000", unit = "Lux")
    private int light;

    @LoomAttribute(key = "Noise", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "100000", unit = "RMS")
    // check range
    private int noise;

    @LoomAttribute(key = "NoiseDba", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "100", unit = "dBA")
    // check range
    private int noiseDba;

    @LoomAttribute(key = "Pressure", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "2000", unit = "mbar")
    // check range
    private int pressure;

    @LoomAttribute(key = "RSSI", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "1", unit = "Rssi")
    // check range
    private int rssi;

    @LoomAttribute(key = "Temperature", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "-100", max = "100", unit = "°C")
    // check range
    private float temp;

    @LoomAttribute(key = "Voc", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "100000", unit = "ppm")
    // check range
    private int voc;

    @LoomAttribute(key = "VocResistance", supportedOperations = {DefaultOperations.SORT_BY}, plottable = true,
            type = NumericAttribute.class, min = "0", max = "100000", unit = "ppm")
    // check range
    private int vocResistance;


    // Battery > 76
    // Humidity > 48
    // Light > 0
    // Noise > null
    // Pressure > 998
    // Rssi > -73
    // Temp > 1935
    // Voc > 2571
    // VocResistance > 81705
    // TimeZone > 2014-12-14T19:09Z


    /**
     * Default constructor.
     */
    public DeviceItemAttributes() {}

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return the roomType
     */
    public String getRoomType() {
        return roomType;
    }

    /**
     * @param roomType the roomType to set
     */
    public void setRoomType(final String roomType) {
        this.roomType = roomType;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(final String uid) {
        this.uid = uid;
    }

    /**
     * @return the battery
     */
    public int getBattery() {
        return battery;
    }

    /**
     * @param battery the battery to set
     */
    public void setBattery(final int battery) {
        this.battery = battery;
    }

    /**
     * @return the humidity
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(final int humidity) {
        this.humidity = humidity;
    }

    /**
     * @return the light
     */
    public int getLight() {
        return light;
    }

    /**
     * @param light the light to set
     */
    public void setLight(final int light) {
        this.light = light;
    }

    /**
     * @return the noise
     */
    public int getNoise() {
        return noise;
    }

    /**
     * @param noise the noise to set
     */
    public void setNoise(final int noise) {
        this.noise = noise;
    }

    /**
     * @return the noiseDba
     */
    public int getNoiseDba() {
        return noiseDba;
    }

    /**
     * @param noiseDba the noiseDba to set
     */
    public void setNoiseDba(final int noiseDba) {
        this.noiseDba = noiseDba;
    }

    /**
     * @return the pressure
     */
    public int getPressure() {
        return pressure;
    }

    /**
     * @param pressure the pressure to set
     */
    public void setPressure(final int pressure) {
        this.pressure = pressure;
    }

    /**
     * @return the rssi
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * @param rssi the rssi to set
     */
    public void setRssi(final int rssi) {
        this.rssi = rssi;
    }

    /**
     * @return the temp
     */
    public float getTemp() {
        return temp;
    }

    /**
     * @param temp the temp to set
     */
    public void setTemp(final float temp) {
        this.temp = temp;
    }

    /**
     * @return the voc
     */
    public int getVoc() {
        return voc;
    }

    /**
     * @param voc the voc to set
     */
    public void setVoc(final int voc) {
        this.voc = voc;
    }

    /**
     * @return the vocResistance
     */
    public int getVocResistance() {
        return vocResistance;
    }

    /**
     * @param vocResistance the vocResistance to set
     */
    public void setVocResistance(final int vocResistance) {
        this.vocResistance = vocResistance;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(final String time) {
        this.time = time;
    }

}
