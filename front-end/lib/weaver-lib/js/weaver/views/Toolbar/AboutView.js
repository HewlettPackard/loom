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

  'use strict';

  var _ = require('lodash');
  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('./../BaseView');
  var serverVersion;

  /**
   * @class AboutView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  return BaseView.extend({

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-about',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.service = this.options.service;
      this.render();
      this.listenTo(this.service.get('adapters'), 'add', this.render);
      this.listenTo(this.service.get('adapters'), 'remove', this.render);
      this.listenTo(this.service.get('adapters'), 'change', this.render);
      this.listenTo(this.service, 'change:serverVersion', this.render);
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.empty();
      this.$el.append('<h1 class="mas-propertySelector--title">About Weaver</h1>');
      var infos = document.createElement('dl');
      infos.classList.add('mas-about--infos');
      $('<dt>Version</dt>').appendTo(infos);
      // Late require to avoid circular dependencies
      $('<dd>').text(require('weaver/Weaver').VERSION).appendTo(infos);
      $('<dt>Server Version</dt>').appendTo(infos);
      serverVersion = $('<dd>' + this.service.get('serverVersion') + '</dd>');
      serverVersion.appendTo(infos);
      this._redrawAdapterList(infos);
      this.$el.append(infos);
    },
    /**
     * @method _redrawAdapterList
     * @param dl
     * @private
     */
    _redrawAdapterList: function (dl) {
      $('<dt>Loaded Adapters</dt>').appendTo(dl);
      _.each(this.service.get('adapters').models, function (adapt) {
        var statusEvents = adapt.get('statusEvents');
        var message = $('<dd>' + adapt.get('id') + ' ' + adapt.get('version') + '</dd>');
        message.addClass('mas-about--adapter');
        if (statusEvents) {
          message.addClass('mas-about--warn');
        }
        message.appendTo(dl);
      });
    }
  });
});
