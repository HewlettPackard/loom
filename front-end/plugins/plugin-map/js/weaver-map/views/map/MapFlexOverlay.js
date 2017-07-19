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

  var template = require('./MapFlexOverlay.html');
  var $ = require('jquery');
  var Backbone = require('backbone');

  var MapFlexOverlay = Backbone.View.extend({
    initialize: function () {
      var $dom = $(template);
      this.setElement($dom[0]);
      this.$el.data('view', this);
    },

    setChildView: function (position, view) {
      // TODO : remove previous element inside the element.
      this.$('.mas-mapBarPlace-' + position).append(view.$el);
    },

    applyMapRatio: function (ratio) {
      this.$('.mas-mapWidthRatio').css('width', (100 / ratio) + '%');
    },

    attach: function (map) {
      this.applyMapRatio(map.getMapRatio());
      map.$('.mas-mapContainer').append(this.$el);
    },
  });

  return MapFlexOverlay;
});