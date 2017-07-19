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
/* global describe, it, sinon, expect, beforeEach, after, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  var _ = require('lodash');
  
  var Item = require('weft/models/Item');
  var Thread = require('weft/models/Thread');

  var MapViewElement = require('weaver-map/views/map/MapViewElementV2');
  var MapElement = require('weaver-map/views/map/MapElementV2');

  describe('MapElement', function () {

    // To test the MapElementListenToModel, we need
    beforeEach(function () {

      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
      });

      // ... an item ...
      var elementID = this.elementID = 'toto';
      this.element = new Item({
        'l.logicalId': elementID,
        name: elementID,
        attributes: {
          lon: (2 * Math.random() - 1) * 180,
          lat: (2 * Math.random() - 1) * 90,
        },
      });

      // a map view
      this.mapView = new MapViewElement({
        model: this.thread,
      });

      this.mapView.render();

      // ... and a MapElement.
      this.view = new MapElement({
        d3map: this.mapView.getD3Element(),
        thread: this.thread,
      });
    });

    afterEach(function () {
    });

  });
});