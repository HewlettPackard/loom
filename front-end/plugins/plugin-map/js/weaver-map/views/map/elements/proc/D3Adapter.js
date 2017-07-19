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
  var DOMTraverseHelper = require('weaver-map/views/map/elements/helpers/DOMTraverseHelper');
  
  var D3Adapter = (function () {
    
    // Provides traverseDOMTree method
    _.extend(D3Adapter.prototype, DOMTraverseHelper);

    function D3Adapter(data) {
      this.data = data;
    }
    
    D3Adapter.prototype.adapt = function (domTree) {
      
      this.traverseDOMTree(domTree, _.bind(this._attachData, this));
    };
    
    D3Adapter.prototype._attachData = function (node) {
      var id = node.id;//node.getAttributeNS(null, 'id');
      
      if (id) {
        node.__data__ = this.data[id];
      }
    };
    
    return D3Adapter;
    
  })();
  
  return D3Adapter;
});