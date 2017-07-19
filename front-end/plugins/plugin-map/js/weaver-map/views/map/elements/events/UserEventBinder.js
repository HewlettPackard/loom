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
  var GroupModel = require('weaver-map/views/map/elements/models/GroupModel');

  var UserEventBinder = (function () {
    
    // Provides traverseDOMTree method
    _.extend(UserEventBinder.prototype, DOMTraverseHelper);

    function UserEventBinder() {

      this.containerModel = undefined;
    }
    
    UserEventBinder.prototype.updateModel = function (model) {
      this.containerModel = model;
    };

    UserEventBinder.prototype.bind = function (svgElement) {
      
      // Tree traversal to:
      //  - bind the data element on the node for d3.
      //  - bind the handler (expect a Backbone.View here)
      if (this.containerModel instanceof GroupModel) {
        
        this.traverseDOMTree(svgElement, _.bind(this._attachDataAndBindEvents, this));
      }
    };

    UserEventBinder.prototype._attachDataAndBindEvents = function (node) {
      var attr = node.getAttributeNS(null, 'dataId');
      
      if (attr) {
        var handler = this.lookupHandler(attr);

        // Set the view data (for d3)
        node.__data__ = handler;
        // Activate backbone event
        handler.setElement(node);
      }
    };
    
    UserEventBinder.prototype.lookupHandler = function (id) {
      return this.containerModel.getElementModel(id).eventHandler;
    };

    return UserEventBinder;
  })();

  return UserEventBinder;
})