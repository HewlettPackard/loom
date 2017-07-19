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

  var MapEvent = require('./MapEventV2');

  var MapLayerGroup = MapEvent.extend({

    initialize: function (options) {
      this.layers = {};
      this.idgroup = options.idgroup;

      var d3map = options.d3map;
      this._group = d3map
        .append('g')
          .attr('idGroup', this.idgroup);
      this._root = this._group
        .append('g');

    },

    addLayer: function (id, rule) {
      if (!this.layers[id]) {
        this.layers[id] = {
          d3Element: this._appendGroup(id, rule),
          rule: rule,
        };
      }

      return this.layers[id].d3Element;
    },

    removeFromMap: function () {
      this._group.remove();
    },

    root: function () {
      return this._root;
    },

    group: function () {
      return this._group;
    },
    
    _appendGroup: function (id, rule) {
      switch (rule) {
      case MapLayerGroup.RULE_LAST:
        return this._group.append('g');
      case MapLayerGroup.RULE_AFTER_ROOT:
        var afterRoot = this._root.node().nextSibling;
        return this._group.insert('g', function () {
          return afterRoot;
        });
      }
    },
  });

  MapLayerGroup.RULE_LAST = 'last';
  MapLayerGroup.RULE_AFTER_ROOT = 'after_root';

  return MapLayerGroup;
});