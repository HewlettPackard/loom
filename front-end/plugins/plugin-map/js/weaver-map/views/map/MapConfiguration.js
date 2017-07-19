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
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var ZoomPanController = require('./ZoomPanController');
  var InfoController = require('./InfoController');
  var ActionIndicatorController = require('./ActionIndicatorController');
  //var ClusterGrid = require('./ClusterGridDebug'); // Uncomment this line if you want the debug view
  var ClusterGrid = require('./ClusterGrid');        // and comment this one.
  var IsOutsideController = require('./IsOutsideController');
  var DistortionController = require('./DistortionController');

  var MapOffScreenChangesController = require('./MapOffScreenChangesController');
  var MapOffScreenChangesView = require('./MapOffScreenChangesView');
  var MapFlexOverlay = require('./MapFlexOverlay');

  /**
   * Create a new configuration object that can be applied to MapViewElement object.
   */
  var MapConfiguration = function MapConfiguration() {
    
    this.zoomPanController = new ZoomPanController();
    this.actionIndicatorController = new ActionIndicatorController();
    this.infoController = new InfoController();
    this.isOutsideController = new IsOutsideController();
    this.distortionController = new DistortionController();

    this.clusterGrid = new ClusterGrid();

  };

  /**
   * Apply the map configuration on the given map.
   * @param  {MapViewElement} map is the map that will be enhanced by this configuration.
   */
  MapConfiguration.prototype.apply = function (map) {

    var d3map = map.getD3Element();

    if (d3map) {
      this._apply(d3map, map);
    }

  };

  /**
   * Apply the map configuration on the given map without checks.
   * @param  {D3Element} d3map is the d3 map.
   * @param  {MapViewElement} map   is the map handling the view.
   */
  MapConfiguration.prototype._apply = function (d3map, map) {

    this.clusterGrid.attach(map);
    
    /////////////////////////////////////////////////////////////////////////////
    // Set zoom/pan features.
    this.zoomPanController.attach(map);

    // Show available action to the user.
    this.actionIndicatorController.attach(map);

    // Set info features (tooltip/detail)
    // Note: this one is closer from the weaver design
    //       whereas map effect and zoomPanController are generic
    this.infoController.attach(map);

    // Controller tracking element going in and out of the viewport.
    this.isOutsideController.attach(map);

    // Controller in charge of controlling when distortion should be done and with what.
    this.distortionController.attach(map);


    /////////////////////////////////////////////////////////////////////////////
    // OffSceen Changes
    this.offScreenChanges = {
      top:    new MapOffScreenChangesView({ direction: 'horizontal' }),
      bottom: new MapOffScreenChangesView({ direction: 'horizontal' }),
      left:   new MapOffScreenChangesView({ direction: 'vertical' }),
      right:  new MapOffScreenChangesView({ direction: 'vertical' })
    };

    this.flexOverlay = new MapFlexOverlay();

    this.flexOverlay.attach(map);

    // Attach the view to the overlay:
    _.forOwn(this.offScreenChanges, function (value, key) {
      this.flexOverlay.setChildView(key, value);
    }, this);


    this.offScreenController = new MapOffScreenChangesController({
      views: this.offScreenChanges
    });

    this.offScreenController.attach(map);


  };

  return MapConfiguration;

});