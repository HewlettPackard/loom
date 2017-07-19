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
package com.hp.hpl.loom.manager.query.utils.functions;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.model.Fibre;

public class PolygonClustering extends LoomFunction {

    public PolygonClustering() {}


    @Override
    public Map<Object, List<Fibre>> apply(final List<Fibre> input, final Map<String, Object> params,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        // validate params

        List<?> listOfPolygons;
        List<?> attributes;
        List<Path2D.Double> polygons;
        Integer defaultPolygon;

        try {
            attributes = (List<?>) params.get(QueryOperation.ATTRIBUTES);
            listOfPolygons = (List<?>) params.get(QueryOperation.POLYGONS);
            polygons = new ArrayList<Path2D.Double>(listOfPolygons.size());
        } catch (ClassCastException e) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, e.getMessage());
            return new HashMap<Object, List<Fibre>>(0);
        }

        try {
            for (Object poly : listOfPolygons) {
                List<?> points = (List<?>) poly;
                Path2D.Double polygon = this.parsePolygon(points);
                polygons.add(polygon);
            }
        } catch (ClassCastException e) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, e.getMessage());
            return new HashMap<Object, List<Fibre>>(0);
        }

        try {
            Object param = params.get(QueryOperation.DEFAULT_POLYGON);

            if (param != null) {
                defaultPolygon = ((Number) param).intValue();
            } else {
                defaultPolygon = -1;
            }

        } catch (ClassCastException e) {
            errors.put(OperationErrorCode.UnsupportedOperationParameter, e.getMessage());
            return new HashMap<Object, List<Fibre>>(0);
        }

        return this.polygonClustering(polygons, defaultPolygon, input, (List<String>) attributes, errors, context);
    }


    @Override
    public boolean isCluster() {
        return true;
    };

    private Map<Object, List<Fibre>> polygonClustering(final List<Path2D.Double> polygons, final Integer defaultPolygon,
            final List<Fibre> input, final List<String> attributes, final Map<OperationErrorCode, String> errors,
            final OperationContext context) {
        Map<Object, List<Fibre>> result;

        result = new ConcurrentHashMap<Object, List<Fibre>>();

        input.stream().forEach(le -> {
            List<Double> values = LoomQueryUtils.convertAttributesToNumbers(attributes, le, errors, context);
            int polygonIndex = this.findContainer(polygons, defaultPolygon, values.get(0), values.get(1));

            if (result.get(polygonIndex) == null) {
                result.put(polygonIndex, new LinkedList<Fibre>());
            }
            result.get(polygonIndex).add(le);
        });

        return result;
    }

    private int findContainer(final List<Path2D.Double> polygons, final Integer defaultPolygon, final double x,
            final double y) {
        int polygonIndex = defaultPolygon;

        for (int i = 0; i < polygons.size(); ++i) {
            Path2D.Double polygon = polygons.get(i);
            if (polygon.contains(x, y)) {
                polygonIndex = i;
                break;
            }
        }
        return polygonIndex;
    }

    private Path2D.Double parsePolygon(final List<?> points) {
        Path2D.Double polygon = new Path2D.Double(Path2D.WIND_EVEN_ODD, points.size());
        Iterator<?> iterPoint = points.iterator();
        List<?> firstPoint = (List<?>) iterPoint.next();
        polygon.moveTo(((Number) firstPoint.get(0)).doubleValue(), ((Number) firstPoint.get(1)).doubleValue());

        while (iterPoint.hasNext()) {
            List<?> point = (List<?>) iterPoint.next();
            polygon.lineTo(((Number) point.get(0)).doubleValue(), ((Number) point.get(1)).doubleValue());
        }

        polygon.closePath();

        return polygon;
    }
}
