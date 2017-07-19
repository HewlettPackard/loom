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
  var MapCountry = require('./MapCountryV2');
  var MapAggregationItemUpdateComponent = require('./MapAggregationItemUpdateComponentV2');

  var countriesTemplate = require('weaver-map/views/map/elements/templates/countries/countries.svg');

  var MapComponentCountries = MapAggregationItemUpdateComponent.extend({

    constructorName: "LOOM_MapComponentCountries",

    initialize: function (options) {
      MapAggregationItemUpdateComponent.prototype.initialize.apply(this, arguments);

      this.countriesViews = {};
    },

    initializeWhenAttached: function () {
      MapAggregationItemUpdateComponent.prototype.initializeWhenAttached.apply(this, arguments);

      this.templateCountries = this.mapRenderer.add({
        template: countriesTemplate,
        modelContext: this.containerModel,
        refresh: true,
      });
    },

    createCountryView: function (countryName) {
      return this.countriesViews[countryName] = {
        view: new MapCountry({
          thread: this.thread,
          d3map: this.d3map
        }),
        markedForRemoval: false,
      };
    },

    removeFromMap: function () {
      this._clearCountries();
      MapAggregationItemUpdateComponent.prototype.removeFromMap.apply(this, arguments);
      this.mapRenderer.remove(this.templateCountries);
    },

    // ----------------------------------------------------------------------
    // OVERRIDE      - MapAggregationItemUpdateComponent -
    //

    prepareFibreUpdate: function () {
      MapAggregationItemUpdateComponent.prototype.prepareFibreUpdate.apply(this, arguments);

      // We mark all countries as potientially being removed.
      _.forEach(this.countriesViews, function (country) {

        country.markedForRemoval = true;
      });
    },

    putAggregation: function (id, aggregation) {
      MapAggregationItemUpdateComponent.prototype.putAggregation.apply(this, arguments);

      var country = this._getCountry(id);
      var idRef = id + this.map.uuid;

      if (country) {

        var countryView = country.view;

        // First clear the country
        this._clearCountry(country);

        // Set the model
        countryView.setModel(aggregation, idRef);
        countryView.setNumberOfItems(aggregation.get('numberOfItems'));

        // Update the template model
        this.containerModel.addElementModel(countryView.templateModel);

        this.dispatchInstantEvent(this.d3map, 'addlazy:tracking', countryView);

        // Don't try to clear the country again.
        country.markedForRemoval = false;

      } else {
        throw new Error("BUG !");
      }
    },

    putItem: function () {
      throw "Unreachable";
    },

    cancelFibreUpdate: function () {
      MapAggregationItemUpdateComponent.prototype.cancelFibreUpdate.apply(this, arguments);

      // We reset the marking of countries.
      _.forEach(this.countriesViews, function (country) {

        // and make sure event are stopped.
        country.markedForRemoval = false;
        country.view.detachListeners(); // Really necessary ?
      });
    },

    finishFibreUpdate: function () {
      MapAggregationItemUpdateComponent.prototype.finishFibreUpdate.apply(this, arguments);

      var self = this;

      // We clear all countries except those who have been properly initialized
      _.forEach(this.countriesViews, function (country) {

        if (country.markedForRemoval) {

          self._clearCountry(country);
          country.markedForRemoval = false;
        }
      });

      this.renderAndUpdate();
    },

    // ----------------------------------------------------------------------
    // PRIVATE INTERFACE
    //

    _getCountry: function (name) {
      return this.countriesViews[name] || this.createCountryView(name);
    },

    _clearCountries: function () {

      var self = this;

      _(this.countriesViews).forEach(function (country) {

        self._clearCountry(country);
      });
    },

    _clearCountry: function (country) {

      if (country.view) {
        this.dispatchInstantEvent(this.d3map, 'remove:tracking', country.view);
        country.view.detach();
      }
    },

  });

  return MapComponentCountries;
});