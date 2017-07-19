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
  var MapEvent = require('./MapEventV2');
  var Element = require('weft/models/Element');

  var MapOffScreenChangesController = MapEvent.extend({
    events: {
      'hasLeftContainer-bottom': function (event) {
        var target = event.originalEvent.args;
        this._switchIfChanged(target, this.views.bottomView);
      },
      'hasLeftContainer-top': function (event) {
        var target = event.originalEvent.args;
        this._switchIfChanged(target, this.views.topView);
      },
      'hasLeftContainer-left': function (event) {
        var target = event.originalEvent.args;
        this._switchIfChanged(target, this.views.leftView);
      },
      'hasLeftContainer-right': function (event) {
        var target = event.originalEvent.args;
        this._switchIfChanged(target, this.views.rightView);
      },

      'isInsideContainer': function (event) {
        var target = event.originalEvent.args;
        this._removeFromView(target);
      },
      'removeFromOffscreenController': function (event) {
        var target = event.originalEvent.args;
        this._removeFromView(target);
      },
      
      'add:(thread,fibre)': '_addThread',
      'removeAll:(thread)': '_removeThread',
    },

    initialize: function (argsMap) {
      
      this.relatedCss = {};
      this.views = {};
      this._threadList = {};

      _.forOwn(argsMap.views, function (value, key) {
        this.views[key + 'View'] = value;
        value.setController(this);
        this.relatedCss[value.cid] = {};
      }, this);
    },

    initializeWhenAttached: function () {
      this.targetInView = {};
    },
    
    _addThread: function (thread) {
      this._generateId(thread);
      var id = this._getId(thread);
      
      this._threadList[id] = thread;
    },
    
    _removeThread: function (thread) {
      
      var id = this._getId(thread);
      delete this._threadList[id];
    },
    
    getGlobalCount: function () {
      
      return _.reduce(this._threadList, function (sum, thread) {
        return sum + _.reduce(thread.get('elements').models, function (res, model) {
          return res + model.get('alertCount') + (model.state ? 1: 0);
        }, 0);
      }, 0);
    },

    _switchIfChanged: function (element, view) {
      this._generateId(element);

      if (this.targetInView[element.__idMapOffScreenChangesController] !== view) {
        this._removeFromView(element);
        this.targetInView[element.__idMapOffScreenChangesController] = view;
        this._listenToElement(element);
      }
    },

    _stopListenToElement: function (element) {
      if (this.targetInView[element.__idMapOffScreenChangesController]) {
        this.stopListening(element.model);
        this.stopListening(element.model.alert);
      }
    },

    _listenToElement: function (element) {
      this.listenTo(element.model.alert, 'change:level', function (event, level) {
        this._updateElementAlert(element, level);
      });

      this.listenTo(element.model, 'change:related', function (model, related) {
        this._updateRelatedCss(element, related);
      });

      this.listenTo(element.model, 'didSetState', function (state, model, oldState) {
        this._updateElementState(element, state, oldState);
      });

      this._updateElementAlert(element, element.model.alert.get('level'));
      this._updateElementState(element, element.model.state);
      this._updateRelatedCss(element, element.model.get('related'));
    },

    _generateId: function (target) {
      if (!target.__idMapOffScreenChangesController) {
        target.__idMapOffScreenChangesController = _.uniqueId();
      }
    },
      
    _getId: function (target) {
      return target.__idMapOffScreenChangesController;
    },

    _removeFromView: function (target) {
      this._stopListenToElement(target);

      if (this.targetInView[target.__idMapOffScreenChangesController]) {
        this._clearElementState(target, 'updated');
        this._clearElementState(target, 'added');
        this._updateElementAlert(target, 0);
        this._updateRelatedCss(target, false);
        var relatedCss = this.relatedCss[this.targetInView[target.__idMapOffScreenChangesController].cid];
        delete relatedCss[target.__idMapOffScreenChangesController];
        delete this.targetInView[target.__idMapOffScreenChangesController];
      }
    },

    _updateElementAlert: function (element, level) {
      if (level > 0) {
        this.targetInView[element.__idMapOffScreenChangesController].add('alert', element);
      } else {
        this.targetInView[element.__idMapOffScreenChangesController].remove('alert', element);
      }
    },

    _updateElementState: function (element, state, oldState) {
      oldState = this.cleanState(oldState);
      state = this.cleanState(state);
      this.targetInView[element.__idMapOffScreenChangesController].remove(oldState, element);
      this.targetInView[element.__idMapOffScreenChangesController].add(state, element);
    },

    _clearElementState: function (element, state) {
      this.targetInView[element.__idMapOffScreenChangesController].remove(state, element);
    },

    _updateRelatedCss: function (element, related) {
      var relatedCss = this.relatedCss[this.targetInView[element.__idMapOffScreenChangesController].cid];
      relatedCss[element.__idMapOffScreenChangesController] = related;
      if (_.some(relatedCss)) {
        this.targetInView[element.__idMapOffScreenChangesController]._addClass('is-related');
      } else {
        this.targetInView[element.__idMapOffScreenChangesController]._removeClass('is-related');
      }
    },
    
    cleanState: function (state) {
      if (state === Element.STATE_UPDATED || state === 'nestedStateChanges') {
        return 'updated'
      }
      return state;
    },
    
    
  });

  return MapOffScreenChangesController;
});