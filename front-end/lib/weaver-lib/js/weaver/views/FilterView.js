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
define(['lodash', 'backbone', 'jquery'], function (_, Backbone, $) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('./BaseView');

  /**
   * FilterView displays an interface to visualise the selected filter - breadcrumb style
   * @class FilterView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FilterView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_FilterView
     * @final
     */
    constructorName: 'LOOM_FilterView',

    /**
     * @property tagName
     * @type {String}
     * @final
     * @default div
     */
    tagName: 'div',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-filter',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-filterElement': 'removeFilter'
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.service = this.options.service;
      this.listenTo(this.service.get('filters'), 'add', this.addFilterItem);
      this.listenTo(this.service.get('filters'), 'remove', this.removeFilterItem);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.addClass(this.className);
      $('<span class="mas-filter--label"> Focus :</span><ul class="mas-filter--elements"></ul>').appendTo(this.el);
      this.toggleVisibility();
    },

    /**
     * @method addFilterItem
     */
    addFilterItem: function (element) {
      this.$el.find('ul').append('<li class="mas-filterElement ' + 'mas-filter--' + element.cid + '" title="' + element.id + '" >' + element.get('name') + '<div class="mas-filter--cancel fa fa-times-circle"></div></li>');
      this.toggleVisibility();
    },

    /**
     * @method removeFilterItem
     */
    removeFilterItem: function (element) {
      this.$el.find('.mas-filter--' + element.cid).remove();
      this.toggleVisibility();
    },

    /**
     * @method removeFilter
     */
    removeFilter: function (event) {
      this.service.get('filters').remove(event.currentTarget.title);
      this.toggleVisibility();
    },

    toggleVisibility: function() {
      if (this.$el.find('li').length === 0) {
        this.$el.find('.mas-filter--label').hide();
      } else {
        this.$el.find('.mas-filter--label').show();
      }
    }
  });

  return FilterView;
});
