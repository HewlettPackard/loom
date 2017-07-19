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
define(['lodash'], function (_) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var FiberAttributeView = require('weaver/views/FiberAttributeView');

  /**
   * FiberOverviewPanel displays the fiber overview panel when selected
   *
   * @class FiberOverviewPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberOverviewPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_FiberOverviewPanel
     * @final
     */
    constructorName: 'LOOM_FiberOverviewPanel',

    className: "mas-fiberOverviewPanel mas-sideMenuContentPanel",

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {FiberOverviewPanel}
     * @chainable
     */
    render: function() {
      this.$el.html('<ul class="mas-fiberOverviewPanel--properties mas-scrollable"></ul>');
      if (this.model) {
        var $properties = this.$('.mas-fiberOverviewPanel--properties');
        var displayedProperties = this.model.itemType.getVisibleAttributes();
        // REFACTOR: This could use the information in the itemType to offer
        // relevant display of specific types of informations
        _(displayedProperties).omit('name').forEach(function (attribute) {
          var propertyValue = this.model.get(attribute.id);
          //do not display null or undefined values
          if (propertyValue !== null && propertyValue !== undefined) {
            var view = this.getView(attribute);
            view.$el.attr('data-attribute', attribute.id);
            $properties.append(view.$el);
          }
        }, this).value();
      }
      return this;
    },

    /**
     * @method getView
     * @param attribute
     */
    getView: function (attribute) {
      return new FiberAttributeView({
        model: this.model,
        attribute: attribute
      });
    }

  });

  return FiberOverviewPanel;
});
