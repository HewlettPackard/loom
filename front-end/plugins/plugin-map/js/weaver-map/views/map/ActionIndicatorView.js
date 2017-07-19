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

  var Backbone = require('backbone');
  var $ = require('jquery');
  var template = require('./ActionIndicatorView.html');

  var ActionIndicatorView = Backbone.View.extend({

    events: {
      'change:panup' : function (active) {
        this._updateActiveState(this.$panup, active);
      },
      'change:pandown': function (active) {
        this._updateActiveState(this.$pandown, active);
      },
      'change:panleft': function (active) {
        this._updateActiveState(this.$panleft, active);
      },
      'change:panright': function (active) {
        this._updateActiveState(this.$panright, active);
      },

      'change:zoomminus': function (active) {
        this._updateActiveState(this.$zoomminus, active);
      },

      'change:zoomplus': function (active) {
        this._updateActiveState(this.$zoomplus, active);
      },
    },

    initialize: function () {
      var $dom = $(template);
      this.setElement($dom[0]);
      this.$el.data('view', this);
      this.$zoomplus = this.$('.fa-plus');
      this.$zoomminus = this.$('.fa-minus');

      this.$panup = this.$('.fa-arrow-up');
      this.$pandown = this.$('.fa-arrow-down');
      this.$panleft = this.$('.fa-arrow-left');
      this.$panright = this.$('.fa-arrow-right');

      // Standard default state values.
      this._updateActiveState(this.$panup, true);
      this._updateActiveState(this.$pandown, true);
      this._updateActiveState(this.$panleft, true);
      this._updateActiveState(this.$panright, true);

      this._updateActiveState(this.$zoomplus, true);
      this._updateActiveState(this.$zoomminus, false);
    },

    _updateActiveState: function ($el, active) {
      if (active) {
        $el.addClass('mas-isActive');
      } else {
        $el.removeClass('mas-isActive');
      }
    }
  });

  return ActionIndicatorView;
});