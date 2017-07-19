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
  var BaseView = require('./BaseView');

  /**
   * PollingFeedbackView provides feedback when a Thread is getting polled.
   *
   * @class PollingFeedbackView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var PollingFeedbackView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_PollingFeedbackView
     * @final
     */
    constructorName: 'LOOM_PollingFeedbackView',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-pollingFeedback',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      if (this.model) {
        this.listenTo(this.model, 'change:polled', this._updatePollingState);
        this.render();
      }
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.addClass(this.className);
      this._updatePollingState();
    },

    /**
     * @method _updatePollingState
     * @private
     */
    _updatePollingState: function () {
      if (this.model.get('polled') === 'polling') {
        this.$el.addClass('mas-pollingFeedback-polling');
        this.$el.removeClass('mas-pollingFeedback-failed');
      } else if (this.model.get('polled') === 'success') {
        this.$el.removeClass('mas-pollingFeedback-polling');
        this.$el.removeClass('mas-pollingFeedback-failed');
      } else if (this.model.get('polled') === 'failed') {
        this.$el.removeClass('mas-pollingFeedback-polling');
        this.$el.addClass('mas-pollingFeedback-failed');
      }
    }
  });

  return PollingFeedbackView;
});
