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
   * ThreadSelectedInfoView displays information about the (un)/selected thread in the side menu
   * @class SideMenuSelectedInfoView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadSelectedInfoView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadSelectedInfoView
     * @final
     */
    constructorName: 'LOOM_ThreadSelectedInfoView',

    /**
     * @property tagName
     * @type {String}
     */
    template: template,

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadSelectedInfoView',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.EventBus, 'thread:selected', function(event) {
        this.displayThread(event.threadView.model);
      });
      this.listenTo(this.EventBus, 'thread:unselected', function(){
        this.displayThread(null);
      });
      this.displayThread(null);
      this.render();
    },

    displayThread: function(thread) {
      if (thread === null) {
        this.setTitle('Thread selected');
        this.setDetail('None');
      } else {
        this.setTitle(this.getElementName(thread) + ' selected');
        this.setDetail(thread.get('name'));
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
        return 'Thread';
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

  return ThreadSelectedInfoView;
});
