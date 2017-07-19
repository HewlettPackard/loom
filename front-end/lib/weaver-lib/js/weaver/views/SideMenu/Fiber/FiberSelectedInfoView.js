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
  var template = require('weaver/views/SideMenu/SideMenuSelectedInfoView.html');

  /**
   * FiberSelectedInfoView displays information about the (un)/selected fiber in the side menu
   * @class SideMenuSelectedInfoView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberSelectedInfoView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_FiberSelectedInfoView
     * @final
     */
    constructorName: 'LOOM_FiberSelectedInfoView',

    /**
     * @property tagName
     * @type {String}
     */
    template: template,

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-fiberSelectedInfoView',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'fiber:selected', function(event) {
        this.displayFiber(event.fiberView.model);
        this.listenTo(event.fiberView.model, 'change', this._refreshFiberFromChangeEvent);
      });
      this.listenTo(this.EventBus, 'fiber:unselected', function(event){
        this.stopListening(event.fiberView.model);
        this.displayFiber(null);
      });
      this.displayFiber(null);
      this.render();
    },

    _refreshFiberFromChangeEvent: function(model) {
      this.displayFiber(model);
    },

    displayFiber: function(fiber) {
      if (fiber === null) {
        this.setTitle('Fiber selected');
        this.setDetail('None');
      } else {
        this.setTitle(this.getElementName(fiber) + ' selected');
        this.setDetail(fiber.get('name'));
      }
    },

    getTopMostParent: function(element) {
      if (element.get('parent')) {
        return this.getTopMostParent(element.get('parent'));
      }
      return element;
    },

    getElementName: function(element) {
      if (!element.get('parent')) {
        return 'Fiber';
      }
      return _.singularize(this.getTopMostParent(element).get('name'));
    },

    /**
     * Set the title of the element
     * @method setTitle
     * @param title
     */
    setTitle: function(title) {
      this.$('.mas-sideMenuSelectedInfoView--title').text(title);
    },

    /**
     * Set the detail of the element
     * @method setDetail
     * @param detail
     */
    setDetail: function(detail) {
      this.$('.mas-sideMenuSelectedInfoView--detail').text(detail);
    }

  });

  return FiberSelectedInfoView;
});
