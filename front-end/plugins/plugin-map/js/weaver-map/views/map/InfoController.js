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

  var d3 = require('d3');
  var MapComponent = require('./MapComponentV2');
  var FloatElementDetailsController = require('plugins/common/utils/FloatElementDetailsController');

  var InfoController = MapComponent.extend({

    events: {
      'info:element': function (event) {
        this._showTooltip(event.originalEvent.args);
      },
      'element:show-details': function (event) {
        this._showDetails(event.originalEvent.rootEvent,
          event.originalEvent.args && event.originalEvent.model,
          event.originalEvent.thread);
      }
    },

    initialize: function () {
      MapComponent.prototype.initialize.apply(this, arguments);
    },

    initializeWhenAttached: function () {
      this.tooltip = this.map.$('.mas-mapTooltip');
      this.detailscontroller = new FloatElementDetailsController({
        el: this.map.el,
        target: this.map.el
      });
    },

    _showTooltip: function (markerData) {
      var content = markerData.name; //markerData.weaverId; //markerData.name;
      if (markerData.numberOfItems) {
        content += ' Count: ' + markerData.numberOfItems;
      }
      this.tooltip.text(content);
      var mouseCoord = d3.mouse(document.body);
      this.tooltip.css('top', (mouseCoord[1] - this.tooltip.height() - 10) + 'px');
      this.tooltip.css('left', (mouseCoord[0] - this.tooltip.width()) + 'px');
      this.tooltip.css('opacity', '1.0');
    },

    _showDetails: function (mouseEvent, modelToPresent, thread) {
      if (!modelToPresent) {
          this.triggerMapEvent('map:grow');
      }

      if (modelToPresent) {
        this.triggerMapEvent('map:shrink');
      }
    }
  });

  return InfoController;
});
