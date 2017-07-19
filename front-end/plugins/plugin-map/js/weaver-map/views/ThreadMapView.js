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
  require('lodash.extensions');

  var MapViewElement = require('./map/MapViewElementV2');
  var MapConfiguration = require('./map/MapConfiguration');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');
  var MetaThread = require('weaver-map/models/MetaThread');

  var QueryEditor = require('weaver/views/QueryEditor');
  var QueryValidatorMap = require('weaver-map/models/QueryValidatorMap');
  var QueryAutoUpdater = require('plugins/common/models/QueryAutoUpdater');
  var QueryAutoUpdaterMapOperations = require('weaver-map/models/QueryAutoUpdaterMapOperations');

  var ThreadMapShadowView = require('./map/ThreadMapShadowView');


  var ThreadMapView = ThreadMapShadowView.extend({

    bindings: {
      ':el': {
        classes: {
          'is-unavailable': 'unavailable'
        }
      }
    },

    className: "mas-threadView",

    constructorName: "LOOM_ThreadMapView",

    events: _.defaults({
      'mouseup .mas-thread': _.noop,
      'swipeleft': _.noop,
      'swiperight': _.noop
    }, ThreadMapShadowView.prototype.events),

    initialize: function () {

      this.model = new MetaThread({
        thread: this.model
      });

      this.map = new MapViewElement({
        model: this.model,
      });

      ThreadMapShadowView.prototype.initialize.apply(this, arguments);

      this.$('.mas-thread--fibers').append('<div class="mas-mapView--element"></div>');
      this.$('.mas-thread--fibers').addClass('mas-elements mas-mapSpecific');

    },

    _renderElements: function () {

      if (!this.firstDone) {
        this.firstDone = 1;
        //this.renderFirst();
      } else if (this.firstDone === 1) {
        this._renderMap();
      }

      this._renderRemovedElementsView();
    },

    _renderMap: function () {
      this.$('.mas-mapView--element').replaceWith(this.map.el);

      // TODO cf ThreadMapShadowView.
      this.elementsView = this.map;

      this.map.render();

      this.mapData = MapDataManager.get(this.map);

      // Set the view data to mapData.
      this.model.set('viewData', this.mapData);

      var configuration = new MapConfiguration();
      configuration.apply(this.map);

      // The map is now ready.
      // We can delegate the initialization of map
      // related components to ThreadMapShadowView.
      this.initializeWhenMapIsReady();

      ///////////////////////////////////////////////////////////////////
      /// WARNING DIRTY CODE INCOMING !! >>>>>>>>>>>>>>>>>>
      ///
      ///   Coupling this class with MetaThread and ThreadListView
      ///
      this.listenToOnce(this.model, 'didChangeQueryUpdaterForHiddenThread', function (hiddenThread) {
        this.dispatchCustomEvent('didDisplayThread', {thread: hiddenThread});
      });

      ////////////////////////////////////////////////////////////////////
      ///   END OF DIRTY HACK <<<<<<<<<<<<<<<<<<
      ////////////////////////////////////////////////////////////////////


      // The map is ready and the model also need it.
      this.model.setMap(this.map);
    },

    _createQueryEditor: function () {

      return new QueryEditor({
        queryValidator: new QueryValidatorMap({
          thread: this.model,
          map: this.map,
        }),
        model: this.model,
        collapsed: true,
        el: this.$('.mas-thread--queryEditor')
      });
    },

    _attachEvents: function () {

      ThreadMapShadowView.prototype._attachEvents.apply(this, arguments);

      this.listenTo(this.model, 'change:focus', function (model, hasFocus) {
        if (hasFocus) {
          this.startQueryMonitor();
        } else {
          this.stopQueryMonitor();
        }
      });
    },

    startQueryMonitor: function () {

      var queryUpdater = this.model.get('queryUpdater');

      if (!(queryUpdater instanceof QueryAutoUpdaterMapOperations)) {
        this.model.set('queryUpdater', new QueryAutoUpdaterMapOperations({
          thread: this.model,
          map: this.map
        }));
      }
    },

    stopQueryMonitor: function () {

      var queryUpdater = this.model.get('queryUpdater');

      if (queryUpdater instanceof QueryAutoUpdaterMapOperations) {
        this.model.set('queryUpdater', new QueryAutoUpdater({
          thread: this.model,
        }));
      }
    },

    _registerDomEvent: function () {

      // Override parent behavior, we want to listen on map only:
      this.$('.mas-mapContainer').on('tapone', _.bind(function (ignored, ev) {
        if (!ev.originalEvent.isDefaultPrevented()) {
          // Acquire focus
          this.model.set('focus', true);
        }
      }, this));
    },

    _actionsAfterRenderNewElements: function (promise, self) {
      return ThreadMapShadowView.prototype._actionsAfterRenderNewElements.call(this,
        promise
          .then(function () {
            self._refreshDistortion();
          }),
        self
      );
    },

    _refreshDistortion: function () {
      this.itemsManager.refreshDistortion();
    },

    _deformMap: function () {
      this.itemsManager.triggerMapEvent('applyDistortion');
    },

    _reset: function () {
      this.itemsManager.triggerMapEvent('revertDistortion');
    },

    _toggle: function () {
      if (this.isMapDistorted) {
        this.isMapDistorted = false;
        this._reset();
      } else {
        this.isMapDistorted = true;
        this._deformMap();
      }
    },
  });

  return ThreadMapView;
});
