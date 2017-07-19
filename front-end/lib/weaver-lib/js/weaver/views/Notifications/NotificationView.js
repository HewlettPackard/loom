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
  var BaseView = require('weaver/views/BaseView');

  /**
   * A simple labeled input
   * @class LabeledInputView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var NotificationView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_NotificationView
     * @final
     */
    constructorName: 'LOOM_NotificationView',

    /**
     * @property {String} className
     * @final
     * @default mas-labeledInput-top
     */
    className: 'mas-notification',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    }

  });

  return NotificationView;
});
