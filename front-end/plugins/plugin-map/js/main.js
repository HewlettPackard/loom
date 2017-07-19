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

  // New thread view for the map display mode
  var ThreadMapView = require('weaver-map/views/ThreadMapView');
  // Query cleaner for transition from the new display mode
  var MapQueryCleaner = require('weaver-map/models/MapQueryCleaner');

  // Plugin registration
  var DisplayMode = require('plugins/common/utils/DisplayMode');
  var pluginAPI = require('plugins/pluginAPI');

  DisplayMode.MAP = 'map';
  pluginAPI.registerDisplayMode({
    name: DisplayMode.MAP,
    humanReadableName: 'Map',
    availability: function (thread) {
      return thread.getGeoAttributes() !== undefined;
    },
    threadViewClass: ThreadMapView,
    queryCleaner: new MapQueryCleaner(),
  });
  pluginAPI.registerThreadAttributes('geoAttributes');

  // Retrieves the translation for country codes into english country names.
  var ccToEnNames = require('weaver-map/data/map-countrycodes-names_en.json');
  pluginAPI.addTranslations("en", ccToEnNames);

  // Plugin modifications
  require('weaver-map/models/ThreadDecorator');
  //require('weaver-map/views/ThreadSettingsMenuDecorator');
  require('weaver-map/models/AggregationDecorator');
  require('weaver-map/views/ElementDetailsViewDecorator');
  require('weaver-map/views/ThreadViewElementsDecorator');
  require('weaver-map/views/FiberOverviewDecorator');
  require('weaver-map/views/ThreadTitleViewDecorator');
});
