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
  var Backbone = require('backbone');
  var Events = Backbone.Events;
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  var mapOptions = ['map'];

  var MapEvent = function (options) {
    options = options || {};
    _.extend(this, _.pick(options, mapOptions));

    this.initialize.call(this, options);
    MapEvent.prototype.attach.call(this, this.map);
  };

  _.extend(MapEvent.prototype, Events, {

    initialize: _.noop,

    /**
     * This function is called whenever the sub class is attached to a new map.
     * When the map is provided at the object creation, the function is called
     * right after the initialize method.
     */
    initializeWhenAttached: _.noop,

    /**
     * This function is called by remove. You can override it in sub classes.
     * It is not intended to be called directly as listeners won't be removed here.
     */
    removeFromMap: _.noop,

    /**
     * QnD hack, to provide Backbone like events system.
     */
    _quickndinit: function () {
      this.cid = _.uniqueId('mapComponent');
      this.$el = this.map.$el;
    },

    /**
     * Convenient function to trigger event to listeners of the map.
     * Arguments are forwarded to the Backbone.trigger defined in Backbone.Events
     * @param {String}    name name of the event.
     * @param {Object...} args followed by a list of arguments.
     */
    triggerMapEvent: function () {
      this.mapData.trigger.apply(this.mapData, arguments);
    },

    /**
     * Convenient function to listen a map event. Used this
     * function when you can't specify the listeners into the events
     * definition.
     * @param {String}   eventName is the name of the event
     * @param {Callback} callback is the callback called when the event is fired.
     */
    listenToMapEvent: function () {
      this.listenTo.apply(this, 
        [this.mapData].concat(
          Array.prototype.slice.call(arguments)
        )
      );
    },

    delegateEvents: function () {
      //this.undelegateEvents();

      Backbone.View.prototype.delegateEvents.apply(this);

      _.forEach(this.events, function (handler, eventName) {
        if (_.isFunction(handler)) {
          this.listenTo(this.mapData, eventName, handler);
        } else {
          this.listenTo(this.mapData, eventName, this[handler]);
        }
      }, this);
    },

    undelegateEvents: function () {
      Backbone.View.prototype.undelegateEvents.apply(this);
      this.stopListening(this.mapData);
    },

    remove: function () {
      if (this.mapData.get('mapIsReady')) {
        this.removeFromMap();
      }
      this.map = undefined;
      this.stopListening();
      this.undelegateEvents();
      return this;
    },

    attach: function (map) {
      if (this.$el) {
        this.undelegateEvents();
      }
      this.map = map;

      if (this.map) {
        this.mapData = MapDataManager.get(this.map);
        // @deprecated shouldn't be used in favor of mapRenderer
        this.d3map = this.map.getD3Element();
        // New way of accessing/modifying the svg
        this.mapRenderer = this.map.mapRenderer;
        this.mapRenderModel = this.map.mapRenderModel;

        this._quickndinit();

        if (this.mapData.get('mapIsReady')) {
          // Map is ready we can work on it.
          this.delegateEvents();
          this.initializeWhenAttached();
        } else {
          this.listenToOnce(this.mapData, 'change:mapIsReady', _.bind(function () {
            // When map is ready.
            this.delegateEvents();
            this.initializeWhenAttached();
          }, this));
        }
      }
    },

  });

  MapEvent.extend = Backbone.View.extend;

  return MapEvent;
});