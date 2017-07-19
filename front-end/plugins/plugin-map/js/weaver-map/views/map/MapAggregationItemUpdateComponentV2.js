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

  var MapComponent = require('./MapComponentV2');
  var GroupModel = require('./elements/models/GroupModel');
  
  var SVGRendererController = require('./elements/SVGRendererController');
  
  var MapAggregationItemUpdateComponent = MapComponent.extend({

    events: {
      'change:zoom': function (event) {
        this.refresh.refresh();
      }
    },
    
    initialize: function (options) {
      MapComponent.prototype.initialize.apply(this, arguments);
      
      this.thread = options.thread;
    },

    /**
     * Create the containerModel that can then
     * be accessed in subclasses.
     */
    initializeWhenAttached: function () {
      
      this.containerModel = new GroupModel(this.thread);
      this.mapRenderModel.addGroupModel(this.containerModel);
    
      this.refresh = new SVGRendererController(
        this.mapRenderer,
        _.bind(this._onRefresh, this)
      );
      
      this.listenTo(this.thread, 'change:focus', function () {
        this.mapRenderer.sortChildren(function (element) {
          return element.hasFocus();
        }, function (element) {
          return element instanceof GroupModel;
        })
      });
    },
    
    /**
     * Clean up when remove is called.
     * If you override this function, make sure to call the super one.
     */
    removeFromMap: function () {
      this.refresh.remove();
      this.containerModel.remove();
    },
    
    /**
     * This function is called by the AggregationItemManager
     * before starting a new update with the new model.
     * The subclass will prepare the element that needs to be rendered,
     * and can also resize its cache based on the expected number of elements.
     * 
     * @param  {Integer} numberOfFibres the number of expected elements to process.
     */
    prepareFibreUpdate: function (/* numberOfFibres */) {
      this.tmpRefresh = new SVGRendererController(
        this.mapRenderer,
        _.bind(this._onRefresh, this)
      );
    },

    /**
     * This function is called during the process of the update phase.
     * The model given is an aggregation. The id given can be used to avoid
     * recreating DOM elements and just updating them.
     * 
     * @param  {String}      id    An id that can be used to identified an aggregation
     * @param  {Aggregation} model The aggregation.
     */
    putAggregation: function ( id, model) {
      this.tmpRefresh.listenToRefreshFor(model);
    },

    /**
     * Same as putAggregation but work with an Item instead.
     * @param  {String} id    An id uniquely identifying the item
     * @param  {Item}   model The item.
     */
    putItem: function ( id, model ) {
      this.tmpRefresh.listenToRefreshFor(model);
    },

    /**
     * This function is called if the update has been cancelled because newer data is available.
     * The component can do whatever appropriate to 
     */
    cancelFibreUpdate: function () {
      this.tmpRefresh.remove();
    },
    
    /**
     * Last function called when the fibre update is finished.
     * Any cache clean up can be performed here.
     */
    finishFibreUpdate: function () {
      this.refresh.remove();
      this.refresh = this.tmpRefresh;
    },

    /**
     * Full render of everything: a call to update is made to force
     * the compilation of every templates.
     */
    renderAndUpdate: function () {

      this.mapRenderer.update();
      this.mapRenderer.render();
      this._triggerUpdateScene();
    },

    /////////////////////////////
    ///   PRIVATE INTERFACE
    /////////////////////////////

    _onRefresh: function () {
      this._triggerUpdateScene();
    },

    _triggerUpdateScene: function () {
      this.triggerMapEvent('update:layer-scene');
    },
  });

  return MapAggregationItemUpdateComponent;
});