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

  var Q = require('q');

  var _ = require('lodash');
  require('lodash.extensions');

  var MapComponentMarkers = require('./MapComponentMarkersV2');
  var MapComponentCountries = require('./MapComponentCountriesV2');
  var Aggregation = require('weft/models/Aggregation');
  var Operation = require('weft/models/Operation');


  var MapComponent = require('./MapComponentV2');

  var returnThis = function () { return DoNothingPromise; };
  var DoNothingPromise = {
    then: returnThis,
    done: returnThis,
    fail: returnThis,
  };

  var AggregationItemManager = MapComponent.extend({

    initialize: function (args) {
      MapComponent.prototype.initialize.apply(this, arguments);

      this.model = args.model;

      this.deferredActivities = {};
    },

    initializeWhenAttached: function () {

      this.listenTo(this.model, 'change:query', this._updateActiveComponent);

      this._updateActiveComponent();
    },

    refreshDistortion: function () {
      this.triggerMapEvent('pointsUpdated');
    },

    /**
     * Do not call this function directly.
     * Calling remove() is the correct way to remove a MapComponent.
     */
    removeFromMap: function () {
      this.activeComponent.remove();
    },

    fillFromModel: function (elements) {
      var deferred = Q.defer();
      var numberOfItems = elements.length;
      var index = 0;
      var self = this;
      var status = { stopped: false };
      var id = _.uniqueId();

      if (!numberOfItems) {
        return DoNothingPromise;
      }

      _(this.deferredActivities).forEach(function (activity) {
        activity.stopped = true;
      });

      this.deferredActivities[id] = status;

      function iter() {
        if (!status.stopped) {

          if (_.size(self.deferredActivities) > 1) {

            // Wait others activities to stop running.
            setImmediate(iter);

          } else {

            if (index < numberOfItems) {

              // First time running
              if (index === 0) {

                self.triggerMapEvent('removeAll:(thread)', self.model);
                self.activeComponent.prepareFibreUpdate(numberOfItems);
              }

              var element = elements[index];

              if (element instanceof Aggregation) {

                self.activeComponent.putAggregation(element.get('name'), element);
              } else {

                self.activeComponent.putItem(element.get('name'), element);
              }

              // Trigger an addition.
              self.triggerMapEvent('add:(thread,fibre)', self.model, element);

              ++index;
              setImmediate(iter);

            } else {

              self.activeComponent.finishFibreUpdate();

              delete self.deferredActivities[id];
              deferred.resolve();
            }

          }

        // An other update has started: cancel this one.
        } else {

          self.activeComponent.cancelFibreUpdate();

          delete self.deferredActivities[id];
          deferred.reject(new Error('reset:elements called while processing it.'));
        }
      }

      iter();

      return deferred.promise;
    },

    _updateActiveComponent: function () {

      if (this._hasGroupByCountry()) {
        if (this.model.hasLimit()) {
          this.model.limitWith(undefined);
        }
        if (!(this.activeComponent instanceof MapComponentCountries)) {
          this.activeComponent = this.activeComponent && this.activeComponent.remove();

          this.activeComponent = new MapComponentCountries({
            thread: this.model,
            map: this.map,
          });
        }

        this.refreshDistortion();
      } else {
        if (!(this.activeComponent instanceof MapComponentMarkers)) {
          this.activeComponent = this.activeComponent && this.activeComponent.remove();

          this.activeComponent = new MapComponentMarkers({
            thread: this.model,
            map: this.map,
          });
        }
      }
    },

    _hasGroupByCountry: function () {
      var operations = this.model.getOperation(Operation.GROUP_BY_ID);

      return operations.length > 0 && operations[operations.length - 1].parameters.property === 'country';
    }
  });

  return AggregationItemManager;
});