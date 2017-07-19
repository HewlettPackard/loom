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
  var MapElement = require('./MapElementV2');

  var ElementModel = require('./elements/models/ElementModel');

  var MapElementListenToModel = MapElement.extend({

    initialize: function (options) {

      MapElement.prototype.initialize.apply(this, arguments);
    },

    ///////////////////////////////////
    // TO BE REDEFINED IN SUBCLASSES //
    ///////////////////////////////////

    __getSVGNode: _.noop,


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////

    setModel: function (model, idRef) {

      // Trying to set the same model ? Job has already be done.
      if (this.model === model) {
        return;
      }

      // If there is a model, the best we can do is throw an error.
      if (this.model) {
        throw new Error('You need to call detachListeners() before calling setModel()');
      }

      this.model = model;

      this.templateModel = new ElementModel({
        eventHandler: this,
        model: this.model,
        idRef: idRef,
      });

      // Set the element as potentially interesting for the user.
      this._addClass('mas-isOfInterest');

      // Listen to new events.
      this.listenTo(this.model, 'change:related', function (model, related) {
        this._updateRelatedState(related);
      });

      this.listenTo(this.model.alert, 'change:level', function (model, level) {
        this._updateAlertState(level);
      });

      this.listenTo(this.model, 'change:isMatchingFilter', function (model, matchesFilter) {
        this._updateFilterState(matchesFilter);
      }, this);

      this.listenTo(this.model, 'hasBeenRemoved', function () {
        if (this.isSelected) {
          this.__dispatchEvent(undefined, 'element:show-details', false);
        }
      });

      this.listenTo(this.thread, 'change:focus', function (model, hasFocus) {
        this._updateFocusState(hasFocus);
      });

      this.listenTo(this.model, 'didSetState', function (state) {
        this.model.trigger('refresh');
      });

      this.listenTo(this.model, 'change:isPartOfFilter', this.setFilter);

      this._updateAlertState(this.model.alert.get('level'));
      this._updateRelatedState(this.model.get('related'));
      this._updateFilterState(this.model.get('isMatchingFilter'));
      this._updateFocusState(this.thread.get('focus'));
    },

    detach: function () {

      this.detachListeners();
      this.detachFromElement();
    },

    detachListeners: function () {

      if (this.model) {
        this.stopListening(this.model);
        this.stopListening(this.model.alert);
        this.model = undefined;
      }

      if (this.thread) {
        this.stopListening(this.thread);
      }
    },

    detachFromElement: function () {

      if (this.templateModel) {
        this.templateModel.remove();
        this.templateModel = undefined;
      }
    },

    setFilter: function () {
      if (this.model.get('isPartOfFilter')) {
        this.__dispatchEvent(null, 'action:addFilterElement');
        this._addClass('is-partOfFilter');
      } else {
        this.__dispatchEvent(null, 'action:removeFilterElement');
        this._removeClass('is-partOfFilter');
      }
    },

    _updateFocusState: function (hasFocus) {
      if (!hasFocus) {
        this._addClass('mas-isNotFocused');
      } else {
        this._removeClass('mas-isNotFocused');
      }
    },

    _removeClass: function () {
      this.templateModel.removeClass.apply(this.templateModel, arguments);
      this.model && this.model.trigger('refresh');
    },

    _addClass: function () {
      this.templateModel.addClass.apply(this.templateModel, arguments);
      this.model && this.model.trigger('refresh');
    },

    _setClass: function () {
      this.templateModel.setClass.apply(this.templateModel, arguments);
      this.model && this.model.trigger('refresh');
    }

  });

  return MapElementListenToModel;
});
