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
define([], function () {

  "use strict";

  /** @type BaseView */
  var BaseView = require('./../BaseView');

  /**
   * ConnectionStatusView displays the status of the connection
   * to the aggregator
   * @class ConnectionStatusView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ConnectionStatusView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ConnectionStatusView
     * @final
     */
    constructorName: 'LOOM_ConnectionStatusView',

    /**
     * Threshold for the requests success rate
     * below which the connection is considered down
     * @property options.downThreshold
     * @type {Number}
     * @default 25
     * @todo make this a class constant as well as an option (0.2 below should be a constant)
     */

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-connectionStatus',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.options.downThreshold = this.options.downThreshold || 0.2;
      this._updateStatusClass(this, this.model.get('status'));
      this.listenTo(this.model, 'change:status', this._updateStatusClass);
      // TODO: Move this out into the Weaver :)
      this.$el.addClass(this.className);
      this.model.poll();
    },

    /**
     * @method _updateStatusClass
     * @param client
     * @param rate
     * @private
     * @todo refactor this, much simpler to remove all three classes and add back the one you want.. check for flicker
     */
    _updateStatusClass: function (client, rate) {
      if (rate === 1) {
        this.$el.addClass('mas-connectionStatus-ok');
        this.$el.removeClass('mas-connectionStatus-down');
        this.$el.removeClass('mas-connectionStatus-unstable');
        return;
      } else if (rate < this.options.downThreshold) {
        this.$el.removeClass('mas-connectionStatus-ok');
        this.$el.addClass('mas-connectionStatus-down');
        this.$el.removeClass('mas-connectionStatus-unstable');
        return;
      }
      this.$el.removeClass('mas-connectionStatus-ok');
      this.$el.removeClass('mas-connectionStatus-down');
      this.$el.addClass('mas-connectionStatus-unstable');
    }
  });

  return ConnectionStatusView;

});
