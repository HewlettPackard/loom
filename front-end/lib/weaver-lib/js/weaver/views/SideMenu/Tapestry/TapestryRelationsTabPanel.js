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
   * TapestryRelationsTabPanel displays the unselected state of a fiber
   *
   * @class TapestryRelationsTabPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var TapestryRelationsTabPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_TapestryRelationsTabPanel
     * @final
     */
    constructorName: 'LOOM_TapestryRelationsTabPanel',

    className: "mas-relationsTab",

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {TapestryRelationsTabPanel}
     * @chainable
     */
    render: function() {
      this.$el.empty().append('<div>This feature is currently out of scope</div>');
      return this;
    }

  });

  return TapestryRelationsTabPanel;
});
