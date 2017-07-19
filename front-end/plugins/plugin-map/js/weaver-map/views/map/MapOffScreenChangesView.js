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

  var d3 = require('d3');
  var $ = require('jquery');
  var _ = require('lodash');
  var template = require('./MapOffScreenChangesView.html');

  var CssClassHelper = require('./CssClassHelper');
  var CustomEventDispatcher = require('weaver/views/CustomEventDispatcher');
  var Aggregation = require('weft/models/Aggregation');

  var MapOffScreenChangesView = CustomEventDispatcher.extend({

    initialize: function (options) {
      var $dom = $(template);
      this.setElement($dom[0]);
      this.$el.data('view', this);
      this.d3el = d3.select(this.el);
      this._initListClass(this.d3el);
      this._fillWithExistingClasses();

      this._transformClasses(function (clazz) {
        return clazz.replace('-direction', options.direction);
      });

      this._addClass('mas-is-visibility-hidden');


      if (options.direction === 'horizontal') {
        this.useWidth = true;
      }

      this.d3elchild = {
        'updated': this.d3el.select('.mas-offscreen-' + options.direction + '-updated'),
        'added':   this.d3el.select('.mas-offscreen-' + options.direction + '-added'),
        'alert':   this.d3el.select('.mas-offscreen-' + options.direction + '-alert'),
      };

      this._setOfElements = {
        'updated': {},
        'added':   {},
        'alert':   {}
      };

      this.totalCount = 0;
    },

    add: function (state, element) {

      if (!_.isUndefined(this._setOfElements[state])) {

        var count = this._getCount(state, element.model);

        this._setOfElements[state][element.__idMapOffScreenChangesController] = count;

        this.totalCount += count;

        this._updateBarSize();
      }
    },

    remove: function (state, element) {

      var id = element.__idMapOffScreenChangesController;

      if (this._setOfElements[state] && !_.isUndefined(this._setOfElements[state][id])) {

        var count = this._setOfElements[state][id];

        this.totalCount -= count;

        delete this._setOfElements[state][id];

        this._updateBarSize();
      }
    },

    clear: function () {
      _(this._setOfElements).forEach(function (stateContainer) {
        _(stateContainer).forEach(function (value, key) {
          delete stateContainer[key];
        });
      });

      this.totalCount = 0;

      this._updateBarSize();
    },

    setController: function (controller) {
      this.controller = controller;
    },

    _getGlobalCount: function () {
      return this.controller.getGlobalCount();
    },

    _getCount: function (state, fibre) {

      if (fibre instanceof Aggregation && state === 'alert') {
        return fibre.get('alertCount');
      } else {
        return 1;
      }
    },

    _updateBarSize: function () {

      _.forOwn(this._setOfElements, function (group, state) {
        var state_count = _.reduce(group, function (sum, num) { return sum + num }, 0)
        this._setNbElementForState(state, state_count);
      }, this);

      this._updateWidthEl(this.totalCount);
    },

    _setNbElementForState: function (state, nbElements) {
      if (nbElements) {
        if (this.useWidth) {
          this.d3elchild[state].style({
            'display': 'block',
            'min-width': '10px',
            'flex-basis': (nbElements * 100 / this.totalCount) + '%'
          });
        } else {
          this.d3elchild[state].style({
            'display': 'block',
            'min-height': '10px',
            'flex-basis': (nbElements * 100 / this.totalCount) + '%'
          });
        }
      } else {
        this.d3elchild[state].attr("style", "");
      }
    },

    _updateWidthEl: function (nb) {
      var factor = Math.min(nb / (this._getGlobalCount() + 1), 1);
      if (this.useWidth) {
        this.d3el.style({
          width: (factor * 60) + '%'
        });
      } else {
        this.d3el.style({
          height: (factor * 60) + '%'
        });
      }
      if (nb === 0) {
        this._addClass('mas-is-visibility-hidden');
      } else {
        this._removeClass('mas-is-visibility-hidden');
      }
    },

    _transformClasses: function (transform) {
      this._eachClass(transform);

      d3.select(this.el).selectAll('div')
        .each(function () {
          var obj = $(this);
          var classList = obj.attr('class').split(/\s+/);
          $.each(classList, function (index, item) {
            obj.removeClass(item);
            obj.addClass(transform(item));
          });
        });
    },

  });

  // Here is part of your answer for CssClassHelper :)
  // It's a kind of multiple inheritance.
  CssClassHelper.assignTo(MapOffScreenChangesView);

  return MapOffScreenChangesView;
});