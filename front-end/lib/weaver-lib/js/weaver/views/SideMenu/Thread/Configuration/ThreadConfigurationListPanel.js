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
  var template = require('./ThreadConfigurationListPanel.html');

  /**
   * ThreadConfigurationListPanel displays the thread overview panel when selected
   *
   * @class ThreadConfigurationListPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadConfigurationListPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadActionList
     * @final
     */
    constructorName: 'LOOM_ThreadConfigurationListPanel',

    className: "mas-threadConfigurationListPanel",

    template: template,
    
    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = options.model;
      this.$el.find('.mas-thread-configuration--metrics').data({id: 'metrics'});
      this.$el.find('.mas-thread-configuration--quicksort').data({id: 'quicksort'});
      this.render();
    }

  });

  return ThreadConfigurationListPanel;
});
