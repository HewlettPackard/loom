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

  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('./../BaseView');
  var AlertNotificationView = require('./AlertNotificationView');
  var template = $(require('./FiberOverview.html'))[0];

  /**
   * @class FiberOverview
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberOverview = BaseView.extend({

    /**
     * @method initialize
     */
    initialize: function () {
      this.setElement(template.cloneNode(true));
      this.$label = this.$('.mas-fiberOverview--label');
      this.alertNotificationView = new AlertNotificationView({
        model: this.model ? this.model.alert : undefined
      });
      this.alertNotificationView.$el.addClass('mas-fiberOverview--alert').appendTo(this.$el);
    },

    /**
     * @method remove
     */
    remove: function () {
      this.alertNotificationView.remove();
    },

    /**
     * @method setLabel
     * @param label
     */
    setLabel: function (label) {
      this.$label.html(label);
    }
  });

  return FiberOverview;
});
