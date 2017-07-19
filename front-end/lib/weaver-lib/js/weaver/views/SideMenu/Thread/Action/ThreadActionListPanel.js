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
define(['lodash', 'jquery'], function (_, $) {

  "use strict";

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var SortModelArrayByName = require('weft/models/SortModelArrayByName');

  /**
   * ThreadActionListPanel displays the thread overview panel when selected
   *
   * @class ThreadActionListPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadActionListPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadActionList
     * @final
     */
    constructorName: 'LOOM_ThreadActionListPanel',

    className: "mas-threadActionListPanel",

    tagName: 'ul',


    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = options.model;
      this.render();
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {ThreadActionListPanel}
     * @chainable
     */
    render: function() {
      this.$el.empty();
      if (this.model) {
        _.values(this.model.get('itemType').getThreadActions()).sort(SortModelArrayByName).map(this._renderAction, this);
      }
      return this;
    },

    /**
     * @method _renderAction
     * @param action
     * @returns {JQuery|jQuery|HTMLElement}
     * @private
     */
    _renderAction: function (action) {
      var $li = $('<li class="mas-thread-action mas-sideMenuListItem">'+action.get('name')+'</li>');
      $li.attr('title', action.id).data('thread-action',action.id).data('action', action);
      $li.appendTo(this.el);
      return $li;
    }

  });

  return ThreadActionListPanel;
});
