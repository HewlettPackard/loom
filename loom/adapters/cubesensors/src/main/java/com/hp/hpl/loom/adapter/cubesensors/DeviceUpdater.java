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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.w3asel.cubesensors.api.v1.State;

/**
 * CubeSensorsUpdater - this creates the DeviceItems
 */
public class DeviceUpdater extends AggregationUpdater<DeviceItem, DeviceItemAttributes, DeviceAndState> {
    private static final int MAX_NUMBER_OF_SIZES = 10;

    protected DeviceCollector deviceCollector = null;

    private Map<String, List<Long>> sizes = new HashMap<>();
    CubeSensorFetcher fetcher = new CubeSensorFetcher();

    /**
     * Constructs a CubeSensorsUpdater.
     *
     * @param aggregation The aggregation this update will update
     * @param adapter The baseAdapter this updater is part of
     * @param fileSystemCollector The collector it uses
     * @throws NoSuchItemTypeException Thrown if the itemtype isn't found
     */
    public DeviceUpdater(final Aggregation aggregation, final BaseAdapter adapter, final DeviceCollector deviceCollector)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, Types.DEVICE_TYPE_LOCAL_ID, deviceCollector);
        this.deviceCollector = deviceCollector;
        //
        // String apiKey =
        // adapter.getAdapterConfig().getPropertiesConfiguration().getString("apiKey");
        // String apiSecret =
        // adapter.getAdapterConfig().getPropertiesConfiguration().getString("apiSecret");
        //
        // service =
        // new
        // ServiceBuilder().debugStream(System.out).provider(CubeSensorsAuthApi.class).apiKey(apiKey)
        // .apiSecret(apiSecret).callback("oob").signatureType(SignatureType.QueryString).build();
        // service.getRequestToken();
        Thread runner = new Thread(fetcher);
        runner.start();
    }


    @Override
    protected String getItemId(final DeviceAndState deviceAndState) {
        return deviceAndState.getDevice().getUid();
    }


    @Override
    protected Iterator<DeviceAndState> getResourceIterator() {
        Iterator<DeviceAndState> devices = fetcher.getDevices();
        return devices;
    }

    @Override
    protected DeviceItem createEmptyItem(final String logicalId) {
        DeviceItem item = new DeviceItem(logicalId, itemType);

        return item;
    }

    @Override
    protected DeviceItemAttributes createItemAttributes(final DeviceAndState resource) {
        DeviceItemAttributes deviceItem = new DeviceItemAttributes();

        deviceItem.setName(resource.getDevice().getName());
        deviceItem.setRoomType(resource.getDevice().getRoomType().name());
        deviceItem.setType(resource.getDevice().getType().name());
        deviceItem.setItemName(resource.getDevice().getName());

        State state = resource.getLastState();
        if (state != null) {
            deviceItem.setBattery(state.getBattery());
            deviceItem.setHumidity(state.getHumidity());
            deviceItem.setLight(state.getLight());
            if (state.getNoise() != null) {
                deviceItem.setNoise(state.getNoise());
            } else {
                deviceItem.setNoise(0);
            }
            deviceItem.setNoiseDba(state.getNoisedba());
            deviceItem.setPressure(state.getPressure());
            deviceItem.setRssi(state.getRssi());
            deviceItem.setTemp((float) state.getTemp() / 100);
            deviceItem.setVoc(state.getVoc());
            deviceItem.setVocResistance(state.getVocResistance());
            deviceItem.setTime(state.getTime().toString());
        }
        return deviceItem;
    }

    @Override
    protected boolean compareItemAttributesToResource(final DeviceItemAttributes device, final DeviceAndState resource) {
        State state = resource.getLastState();
        if (state != null) {
            if (device.getTime().equals(state.getTime().toString())) {
                return true;
            }
        }
        return true;
    }

    @Override
    protected void setRelationships(final ConnectedItem fileItem, final DeviceAndState resource) {}
    //
    // public static void main(final String[] args) {
    // String apiKey = "BdXc9UFS7hrf9MzKjgGD";
    // String apiSecret = "CecZcb7gf3qTGPSeRLyuTsGa";
    // service =
    // new
    // ServiceBuilder().debugStream(System.out).provider(CubeSensorsAuthApi.class).apiKey(apiKey)
    // .apiSecret(apiSecret).callback("oob").signatureType(SignatureType.QueryString).build();
    // // Token requestToken = service.getRequestToken();
    // // System.out.println("Copy these values into cubesensors.test.properties:");
    // // System.out.println("requestToken.token=" + requestToken.getToken());
    // // System.out.println("requestToken.secret=" + requestToken.getSecret());
    // //
    // // System.out.println();
    // // System.out.println("Navigate to: " + service.getAuthorizationUrl(requestToken));
    // //
    // System.out.println("Enter the string the cubesensors page gives you in cubesensors.test.properties after:");
    // // System.out.println("verifier=");
    //
    //
    // // final Verifier verifier = new Verifier("6JR188DY");
    // // Token requestToken = new Token("EAfnUnf24Zr3", "jgTyB8NsqkkhKjMS");
    // // final Token accessToken2 = service.getAccessToken(requestToken, verifier);
    // //
    // // System.out.println();
    // //
    // // System.out.println("Copy these values into cubesensors.test.properties:");
    // // System.out.println("accessToken.token=" + accessToken2.getToken());
    // // System.out.println("accessToken.secret=" + accessToken2.getSecret());
    //
    //
    // String accessToken = "qPBv5J5Dnm4C";
    // String accessSecret = "CaKliMgDF8TWVXXw";
    //
    // // step 3
    // // final Token accessToken = CubeSensorsTestProperties.getAccessToken();
    // //
    // // final OAuthRequest request = new OAuthRequest(Verb.GET,
    // // "http://api.cubesensors.com/v1/devices/");
    // // service.signRequest(accessToken, request);
    // // final Response response = request.send();
    // //
    // // System.out.println();
    // //
    // // System.out.println("Request succeeded");
    // // System.out.println(response.getBody());
    // //
    // //
    //
    // Token token = new Token(accessToken, accessSecret);
    // // final OAuthRequest request = new OAuthRequest(Verb.GET,
    // // "http://api.cubesensors.com/v1/devices/");
    // // service.signRequest(token, request);
    // // final Response response = request.send();
    // //
    // // System.out.println();
    // //
    // // System.out.println("Request succeeded");
    // // System.out.println(response.getBody());
    //
    // String url = CubeSensorsProperties.getAppCallbackUrl();
    // String key = CubeSensorsProperties.getAppKey();
    // String secret = CubeSensorsProperties.getAppSecret();
    // System.out.println(url + " (" + key + ") (" + secret + ")");
    //
    //
    //
    // CubeSensorsApiV1 api = new CubeSensorsApiV1(token);
    // List<Device> devices = api.getDevices();
    // for (Device device : devices) {
    // System.out.println("Device > " + device.name + " " + device.getRoomType().name() + " "
    // + device.getType().name());
    // State state = api.getCurrent(device.getUid());
    // System.out.println("Current data");
    // if (state != null) {
    // System.out.println(" Battery > " + state.getBattery());
    // System.out.println(" Humidity > " + state.getHumidity());
    // System.out.println(" Light > " + state.getLight());
    // System.out.println(" Noise > " + state.getNoise());
    // System.out.println(" Pressure > " + state.getPressure());
    // System.out.println(" Rssi > " + state.getRssi());
    // System.out.println(" Temp > " + state.getTemp());
    // System.out.println(" Voc > " + state.getVoc());
    // System.out.println(" VocResistance > " + state.getVocResistance());
    // System.out.println(" TimeZone > " + state.getTime());
    // }
    // System.out.println("Getting old data");
    //
    // final ZonedDateTime start = ZonedDateTime.now().minusHours(46);
    // final ZonedDateTime end = ZonedDateTime.now();
    // List<State> states = api.getSpan(device.getUid(), start, end, 60);
    // System.out.println(">>>> " + states.size());
    // for (State s : states) {
    // System.out.println(" ---" + s.getTime().toString());
    // System.out.println(" Battery > " + s.getBattery());
    // System.out.println(" Humidity > " + s.getHumidity());
    // System.out.println(" Light > " + s.getLight());
    // System.out.println(" Noise > " + s.getNoise());
    // System.out.println(" Pressure > " + s.getPressure());
    // System.out.println(" Rssi > " + s.getRssi());
    // System.out.println(" Temp > " + s.getTemp());
    // System.out.println(" Voc > " + s.getVoc());
    // System.out.println(" VocResistance > " + s.getVocResistance());
    // System.out.println(" TimeZone > " + s.getTime());
    // }
    // }
    //
    //
    //
    // }
}
