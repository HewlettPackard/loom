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
package com.hp.hpl.loom.manager.query.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.hpl.loom.adapter.os.OsFlavour;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceAttributes;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.adapter.os.OsVolume;
import com.hp.hpl.loom.adapter.os.OsVolumeAttributes;
import com.hp.hpl.loom.adapter.os.OsVolumeType;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.QueryUtils;
import com.hp.hpl.loom.manager.query.utils.functions.FilterByRegion;
import com.hp.hpl.loom.manager.query.utils.functions.GridClustering;
import com.hp.hpl.loom.manager.query.utils.functions.HourGlass;
import com.hp.hpl.loom.manager.query.utils.functions.Kmeans;
import com.hp.hpl.loom.manager.query.utils.functions.PolygonClustering;
import com.hp.hpl.loom.manager.query.utils.functions.Pyramid;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.ProviderImpl;



public class LoomQueryUtilsTest {

    public static class GeoPointType extends ItemType {
        static final String TYPE_LOCAL_ID = "geopoint";
        static final String PROVIDER_ID = "geo";
        static final String TYPE_ID = PROVIDER_ID + "-" + TYPE_LOCAL_ID;

        public GeoPointType() {
            super(TYPE_LOCAL_ID);
            setId(PROVIDER_ID + "-" + getLocalId());
        }
    }

    private Provider provider = new ProviderImpl("os", "providerId", "authEndpoint", "providerName", "adapterPackage");

    private static GeoPointType geoPointType = new GeoPointType();

    @Test
    public void testFilterByRegion() throws Exception {
        List<Fibre> out = this.generate2DPoints(0, 1, 0, 1, 10);
        List<Fibre> out2 = this.generate2DPoints(2.00001, 3, 2.00001, 3, 10);
        List<Fibre> in = this.generate2DPoints(1.00001, 2, 1.00001, 2, 10);


        out.addAll(out2);
        List<Fibre> all = new ArrayList<Fibre>(out.size() + in.size());
        all.addAll(in);
        all.addAll(out);

        // Classic arguments tests
        Map<String, Object> params = new HashMap<>(2);
        List<String> attributes = Arrays.asList("x0", "x1");
        params.put(QueryOperation.MAXIMUMS, Arrays.asList(2, 2));
        params.put(QueryOperation.MINIMUMS, Arrays.asList(1, 1));
        params.put(QueryOperation.ATTRIBUTES, attributes);
        FunctionSpec spec = new FunctionSpec(params, new FilterByRegion());

        Map<OperationErrorCode, String> errors = new HashMap<OperationErrorCode, String>(0);
        Map<Object, List<Fibre>> result = LoomQueryUtils.braid(all, spec, errors, null);

        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse("Result should not be empty.", result.isEmpty());
        Assert.assertEquals("Result should be one", 1, result.size());
        Assert.assertEquals("In should be the only possible output", result.values().iterator().next(), in);

        // Complement test
        params.clear();
        params.put(QueryOperation.MAXIMUMS, Arrays.asList(1, 1));
        params.put(QueryOperation.MINIMUMS, Arrays.asList(2, 2));
        params.put(QueryOperation.COMPLEMENT, true);
        params.put(QueryOperation.ATTRIBUTES, attributes);

        spec = new FunctionSpec(params, new FilterByRegion());
        result = LoomQueryUtils.braid(all, spec, errors, null);

        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse("Result should not be empty.", result.isEmpty());
        Assert.assertEquals("Result should be one", 1, result.size());
        Assert.assertEquals("Out should be the only possible output", result.values().iterator().next(), out);
    }

    private List<Fibre> generate2DPoints(final double xmin, final double xmax, final double ymin, final double ymax,
            final int number) {
        List<Fibre> result = new ArrayList<Fibre>(number);

        Random rand = new Random();

        for (int i = 0; i < number; ++i) {
            result.add(new Point2DItem(rand.nextDouble() * (xmax - xmin) + xmin,
                    rand.nextDouble() * (ymax - ymin) + ymin));
        }

        return result;
    }

    public static class Point2DItem extends Item {

        private Double x0;
        private Double x1;

        public Point2DItem(final double x0, final double x1) {
            this.x0 = x0;
            this.x1 = x1;
        }

        public Double getX0() {
            return x0;
        }

        public Double getX1() {
            return x1;
        }

        public void setX0(final Double x0) {
            this.x0 = x0;
        }

        public void setX1(final Double x1) {
            this.x1 = x1;
        }

        @Override
        public String toString() {
            return "(" + x0 + ", " + x1 + ")";
        }
    }

    @Test
    public void testGridClustering() throws Exception {
        List<Fibre> group1 = this.generate2DPoints(0, 1, 0, 1, 10);
        List<Fibre> group2 = this.generate2DPoints(2.00001, 3, 2.00001, 3, 10);
        List<Fibre> group3 = this.generate2DPoints(1.00001, 2, 1.00001, 2, 10);

        List<Fibre> all = new ArrayList<Fibre>(group1.size() + group2.size() + group3.size());
        all.addAll(group1);
        all.addAll(group2);
        all.addAll(group3);

        // Three aggregation would be created.
        Map<String, Object> params = new HashMap<>(2);
        List<String> attributes = Arrays.asList("x0", "x1");
        params.put(QueryOperation.TRANSLATIONS, Arrays.asList(0, 0));
        params.put(QueryOperation.DELTAS, Arrays.asList(1, 1));
        params.put(QueryOperation.MAX_FIBRES, 0);
        params.put(QueryOperation.ATTRIBUTES, attributes);
        FunctionSpec spec = new FunctionSpec(params, new GridClustering());

        Map<OperationErrorCode, String> errors = new HashMap<OperationErrorCode, String>(0);
        Map<Object, List<Fibre>> result = LoomQueryUtils.braid(all, spec, errors, null);

        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.containsValue(group1));
        Assert.assertTrue(result.containsValue(group2));
        Assert.assertTrue(result.containsValue(group3));

        // One aggregation here.
        params.clear();
        params.put(QueryOperation.TRANSLATIONS, Arrays.asList(0, 0));
        params.put(QueryOperation.DELTAS, Arrays.asList(3, 3));
        params.put(QueryOperation.MAX_FIBRES, 0);
        params.put(QueryOperation.ATTRIBUTES, attributes);
        spec = new FunctionSpec(params, new GridClustering());

        errors = new HashMap<OperationErrorCode, String>(0);
        result = LoomQueryUtils.braid(all, spec, errors, null);

        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsValue(all));

        // Still one aggregation here because of the maxFibres
        params.clear();
        params.put(QueryOperation.TRANSLATIONS, Arrays.asList(0, 0));
        params.put(QueryOperation.DELTAS, Arrays.asList(0.00001, 0.00001));
        params.put(QueryOperation.MAX_FIBRES, 50);
        params.put(QueryOperation.ATTRIBUTES, attributes);
        spec = new FunctionSpec(params, new GridClustering());

        errors = new HashMap<OperationErrorCode, String>(0);
        result = LoomQueryUtils.braid(all, spec, errors, null);

        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(all, result.get("DontAggregate"));
    }

    @Test
    public void testPolygonClustering() throws Exception {
        List<Fibre> left = this.generate2DPoints(0, 1, 1.00001, 2, 10);
        List<Fibre> right = this.generate2DPoints(2.00001, 3, 1.00001, 2, 10);
        List<Fibre> bot = this.generate2DPoints(1.00001, 2, 0, 1, 10);
        List<Fibre> top = this.generate2DPoints(1.00001, 2, 2.00001, 3, 10);

        List<Fibre> all = new ArrayList<Fibre>(left.size() + right.size() + top.size() + bot.size());
        all.addAll(left);
        all.addAll(right);
        all.addAll(top);
        all.addAll(bot);

        Map<String, Object> params = new HashMap<>(2);
        List<String> attributes = Arrays.asList("x0", "x1");
        params.put(QueryOperation.POLYGONS, Arrays.asList(createPolygon(0, 3, 1, 2, 1, 1, 0, 0), // Left
                createPolygon(0, 3, 3, 3, 2, 2, 1, 2), // Top
                createPolygon(2, 2, 3, 3, 3, 0, 2, 1), // Right
                createPolygon(0, 0, 1, 1, 2, 1, 3, 0) // Bot
        ));
        params.put(QueryOperation.ATTRIBUTES, attributes);

        FunctionSpec spec = new FunctionSpec(params, new PolygonClustering());
        Map<OperationErrorCode, String> errors = new HashMap<OperationErrorCode, String>(0);
        Map<Object, List<Fibre>> result = LoomQueryUtils.braid(all, spec, errors, null);

        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(left, result.get(0));
        Assert.assertEquals(top, result.get(1));
        Assert.assertEquals(right, result.get(2));
        Assert.assertEquals(bot, result.get(3));
    }

    private List<List<Double>> createPolygon(final double x0, final double y0, final double x1, final double y1,
            final double x2, final double y2, final double x3, final double y3) {
        return Arrays.asList(Arrays.asList(x0, y0), Arrays.asList(x1, y1), Arrays.asList(x2, y2),
                Arrays.asList(x3, y3));
    }

    @Test
    public void testKMeans() throws Exception {
        List<Fibre> ent = this.createGeo(10, 0, "geo", 5);

        Map<String, Object> params = new HashMap<>(3);
        List<String> attributes = Arrays.asList("longit", "lat");
        params.put(QueryOperation.MAX_FIBRES, 5);
        params.put(QueryOperation.K, 4);
        params.put(QueryOperation.ATTRIBUTES, attributes);
        FunctionSpec spec = new FunctionSpec(params, new Kmeans());

        Map<OperationErrorCode, String> errors = new HashMap<OperationErrorCode, String>(0);
        Map<Object, List<Fibre>> result = LoomQueryUtils.braid(ent, spec, errors, null);
        Assert.assertTrue("Parameters should match", errors.isEmpty());
        Assert.assertFalse("Result should not be empty.", result.isEmpty());
    }


    private List<Fibre> createGeo(final int numItems, final int startId, final String typeId, final int k) {
        List<Fibre> entities = new ArrayList<>(numItems);
        // generate ten initial points acting as seeds
        List<Fibre> init = new ArrayList<>(k);
        Random r = new Random();
        GeoPoint centre;
        for (int i = 0; i < k; i++) {
            centre = new GeoPoint(r.nextDouble() * -180.0 + 90.0, r.nextDouble() * -360.0 + 180.0, geoPointType);
            init.add(centre);
        }
        entities.addAll(init);
        // create entities around those
        int block = (numItems - k) / k;
        int count = 0;
        centre = (GeoPoint) init.get(count);
        for (int i = k; i < numItems; i++) {
            entities.add(generateAround(centre.longit, centre.lat, 100d));
            if (count == block) {
                count++;
                centre = (GeoPoint) init.get(count);
            }
        }
        return entities;
    }

    private void checkBraidedSize(final Map<Object, List<Fibre>> braided, final int size) {
        int braidedSize = 0;
        for (List<Fibre> braid : braided.values()) {
            braidedSize += braid.size();
        }
        org.junit.Assert.assertEquals("Braided size not equal to original", size, braidedSize);
    }

    @Test
    public void testSimpleBraid() throws Exception {
        int size = 1092;
        Map<Object, List<Fibre>> braided =
                LoomQueryUtils.simpleBraid(createInstances(size, 0, new OsInstanceType(provider).getId()), 45);
        checkBraidedSize(braided, size);
        org.junit.Assert.assertEquals("Expected items", 45, braided.size());
        org.junit.Assert.assertEquals("Expect items", 25, braided.get(0).size());
        org.junit.Assert.assertEquals("Expect items", 24, braided.get(braided.size() - 1).size());
    }

    @Test
    public void testMaxAtBottomOfPyramidBraidTightPacking() throws Exception {
        int size = 1092;

        Map<String, Object> params = new HashMap<>(2);
        params.put("maxFibres", 45);
        params.put("tightPacking", false);
        FunctionSpec spec = new FunctionSpec(params, new Pyramid());


        Map<Object, List<Fibre>> braided =
                LoomQueryUtils.braid(createInstances(size, 0, new OsInstanceType(provider).getId()), spec,
                        new HashMap<OperationErrorCode, String>(0), null);


        checkBraidedSize(braided, size);
        org.junit.Assert.assertEquals("Expected items", 25, braided.size());
        org.junit.Assert.assertEquals("Expect items", 44, braided.get(0).size());
        org.junit.Assert.assertEquals("Expect items", 36, braided.get(braided.size() - 1).size());
    }

    @Test
    public void testMaxAtBottomOfPyramidBraidEvenPacking() throws Exception {
        int size = 1092;
        Map<String, Object> params = new HashMap<>(2);
        params.put("maxFibres", 45);
        params.put("tightPacking", true);
        FunctionSpec spec = new FunctionSpec(params, new Pyramid());


        Map<Object, List<Fibre>> braided =
                LoomQueryUtils.braid(createInstances(size, 0, new OsInstanceType(provider).getId()), spec,
                        new HashMap<OperationErrorCode, String>(0), null);
        checkBraidedSize(braided, size);
        org.junit.Assert.assertEquals("Expected items", 25, braided.size());
        org.junit.Assert.assertEquals("Expect items", 45, braided.get(0).size());
        org.junit.Assert.assertEquals("Expect items", 12, braided.get(braided.size() - 1).size());
    }

    @Test
    public void testHourGlassBraidOneLevel() throws Exception {
        int size = 1092;

        Map<String, Object> params = new HashMap<>(2);
        params.put("maxFibres", 45);
        FunctionSpec spec = new FunctionSpec(params, new HourGlass());


        Map<Object, List<Fibre>> braided =
                LoomQueryUtils.braid(createInstances(size, 0, new OsInstanceType(provider).getId()), spec,
                        new HashMap<OperationErrorCode, String>(0), null);
        checkBraidedSize(braided, size);
        org.junit.Assert.assertEquals("Expected items", 25, braided.size());
        org.junit.Assert.assertEquals("Expect items", 44, braided.get(0).size());
        org.junit.Assert.assertEquals("Expect items", 36, braided.get(braided.size() - 1).size());
    }

    @Test
    public void testHourGlassBraidTwoLevel() throws Exception {
        int size = 2800;

        Map<String, Object> params = new HashMap<>(2);
        params.put("maxFibres", 45);
        FunctionSpec spec = new FunctionSpec(params, new HourGlass());


        Map<Object, List<Fibre>> braided =
                LoomQueryUtils.braid(createInstances(size, 0, new OsInstanceType(provider).getId()), spec,
                        new HashMap<OperationErrorCode, String>(0), null);
        checkBraidedSize(braided, size);
        org.junit.Assert.assertEquals("Expected items", 45, braided.size());
        org.junit.Assert.assertEquals("Expect items", 63, braided.get(0).size());
        org.junit.Assert.assertEquals("Expect items", 28, braided.get(braided.size() - 1).size());
    }

    private List<Fibre> createInstances(final int numItems, final int startId, final String typeId) {
        List<Fibre> items = new ArrayList<Fibre>(numItems);

        ItemType volType = new OsVolumeType(provider);
        volType.setId("os-" + volType.getLocalId());

        OsVolumeAttributes ova1 = new OsVolumeAttributes("vol1", "vId1", 0, "", "", "", "", "");
        OsVolumeAttributes ova2 = new OsVolumeAttributes("vol2", "vId2", 0, "", "", "", "", "");
        OsVolumeAttributes ova3 = new OsVolumeAttributes("vol3", "vId3", 0, "", "", "", "", "");
        OsVolumeAttributes ova4 = new OsVolumeAttributes("vol4", "vId4", 0, "", "", "", "", "");
        OsVolume vol1 = new OsVolume("vol1", volType);
        OsVolume vol2 = new OsVolume("vol2", volType);
        OsVolume vol3 = new OsVolume("vol3", volType);
        OsVolume vol4 = new OsVolume("vol4", volType);
        vol1.setCore(ova1);
        vol2.setCore(ova2);
        vol3.setCore(ova3);
        vol4.setCore(ova4);
        List<OsVolume> volumes = new ArrayList<>(4);
        volumes.add(vol1);
        volumes.add(vol2);
        volumes.add(vol3);
        volumes.add(vol4);

        ItemType instanceType = new OsInstanceType(provider);
        instanceType.setId("os-" + instanceType.getLocalId());
        for (int count = startId; count < numItems + startId; count++) {
            String logicalId = "/os/fake/instances/i" + count;
            String name = "vm" + count;
            int flvId = count % 4;
            OsFlavour flavour = new OsFlavour(Integer.toString(flvId), "small" + flvId, 1, 2048, 3);
            OsInstance instance = new OsInstance(logicalId, instanceType);
            OsInstanceAttributes oia = new OsInstanceAttributes(flavour);
            oia.setItemName(name);
            oia.setItemId(name);
            instance.setCore(oia);
            instance.addConnectedRelationships(volumes.get(count % 4), "");
            instance.addConnectedRelationships(volumes.get(0), "");
            items.add(instance);
        }
        return items;
    }

    public class GeoPoint extends Item {
        @JsonProperty
        private Double longit;
        @JsonProperty
        private Double lat;

        public static final double R = 6371; // Radius of the earth in km

        public GeoPoint(final Double longit, final Double lat, final ItemType type) {
            super("/earth/long/" + longit + "/lat/" + lat, type);
            this.longit = longit;
            this.lat = lat;
        }

        public Double getLongit() {
            return longit;
        }

        public void setLongit(final Double longit) {
            this.longit = longit;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(final Double lat) {
            this.lat = lat;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Fibre that = (Fibre) o;

            return Objects.equals(getFibreCreated(), that.getFibreCreated())
                    && Objects.equals(getFibreDeleted(), that.getFibreDeleted())
                    && Objects.equals(getDescription(), that.getDescription())
                    && Objects.equals(getFibreType(), that.getFibreType())
                    && Objects.equals(getLogicalId(), that.getLogicalId()) && Objects.equals(getName(), that.getName())
                    && Objects.equals(getTypeId(), that.getTypeId())
                    && Objects.equals(getFibreUpdated(), that.getFibreUpdated());

        }


        @Override
        public int hashCode() {
            return Objects.hash(getFibreType(), getLogicalId(), getTypeId(), getName(), getDescription(),
                    getFibreCreated(), getFibreUpdated(), getFibreDeleted());
        }


        @Override
        public String toString() {
            return longit + " : " + lat;
        }
    }

    public GeoPoint generateAround(final Double lon, final Double lat, final Double radius) {
        Double degrees = new Random().nextDouble() * (radius * 360 / (2 * Math.PI * GeoPoint.R));

        if (new Random().nextDouble() > 0.5) {
            if (new Random().nextDouble() > 0.5) {
                return new GeoPoint(lon - degrees, lat + degrees, geoPointType);
            } else {
                return new GeoPoint(lon + degrees, lat + degrees, geoPointType);
            }
        } else {
            if (new Random().nextDouble() > 0.5) {
                return new GeoPoint(lon - degrees, lat - degrees, geoPointType);
            } else {
                return new GeoPoint(lon + degrees, lat - degrees, geoPointType);
            }
        }
    }

    @Test
    public void testWildCardMatching() {

        String patt = "vm-*1*";
        String text = "vm- should not match";

        List<String> patterns = new ArrayList<>(1);
        patterns.add(patt);
        org.junit.Assert.assertFalse(QueryUtils.wildCardMatch(text, patterns));

        text = "vm-11";
        org.junit.Assert.assertTrue(QueryUtils.wildCardMatch(text, patterns));

        patt = "vm-*1";
        patterns.clear();
        patterns.add(patt);

        text = "vm-21";
        org.junit.Assert.assertTrue(QueryUtils.wildCardMatch(text, patterns));

        patterns.clear();
        patterns.add("vm-22");
        org.junit.Assert.assertFalse(QueryUtils.wildCardMatch(text, patterns));

        patterns.add(patt);
        org.junit.Assert.assertTrue(QueryUtils.wildCardMatch(text, patterns));
    }
}
