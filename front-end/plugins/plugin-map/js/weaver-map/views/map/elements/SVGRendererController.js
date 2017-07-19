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
define (function (require) {
  "use strict";
  
  var _ = require('lodash');
  var Backbone = require('backbone');
  
  var SVGRendererController = (function () {
    
    _.extend(SVGRendererController.prototype, Backbone.Events);
    
    function SVGRendererController(templateSVGRenderer, onRefresh) {

      this._refreshDebounced = _.debounce(function () {
        if (!this._cancelRefresh) {
          templateSVGRenderer.refresh();
          onRefresh();
        }
      }, 20);
    }
    
    SVGRendererController.prototype.remove = function () {
      this.stopListening();
      this._cancelRefresh = true;
    };

    SVGRendererController.prototype.refresh = function () {
      this._refreshDebounced();
    };
    
    SVGRendererController.prototype.listenToRefreshFor = function (mapModel) {
      this.listenTo(mapModel, 'refresh', _.bind(this._refreshDebounced, this));
    };
    
    SVGRendererController.prototype.stopListenningTo = function (mapModel) {
      this.stopListenning(mapModel);
    };
      
    return SVGRendererController;
  })();
  
  return SVGRendererController;
});