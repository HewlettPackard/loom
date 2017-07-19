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

  var _ = require('lodash');
  var $ = require('jquery');
  /** @type BaseView */
  var BaseView = require('../BaseView');
  var Aggregation = require('weft/models/Aggregation');

  /**
   * The ElementActionsView displays the actions available for a given element
   * @class  ElementActionsView
   * @module  weaver
   * @submodule views.ElementDetailsView
   * @namespace views.ElementDetailsView
   * @constructor
   * @extends BaseView
   */
  var ElementActionsView = BaseView.extend({

    constructorName: 'LOOM_ElementActionsView',

    className: 'mas-elementActions',

    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this.$el.addClass(this.className);
      this._renderElementActions();
      if (this.model instanceof Aggregation) {
        this._renderViewAction();
      }
      this._renderFilterRelatedAction();
      this._renderAddToFilterAction();
    },

    /**
     * @method _renderElementActions
     * @private
     */
    _renderElementActions: function () {
      var actions = this.model.getActionDefinitions();
      _.forEach(actions, this._renderElementAction, this);
    },

    /**
     * @method _renderFilterRelatedAction
     * @private
     */
    _renderFilterRelatedAction: function () {
      var $HTMLElement = this._renderAction('Filter related fibers', 'fa-share-alt');
      $HTMLElement.addClass('mas-action-filterRelated').data('fiber', this.model);
    },

    /**
     * @method _renderElementAction
     * @param action
     * @private
     */
    _renderElementAction: function (action) {
      var $HTMLElement = this._renderAction(action.id, action.get('icon'));
      $HTMLElement.addClass('mas-action-showDialog').data('action', action);
    },

    /**
     * @method _renderViewAction
     * @private
     */
    _renderViewAction: function () {
      var $HTMLElement = this._renderAction('Display', 'fa-level-down');
      $HTMLElement.addClass('mas-action--view').data('aggregation', this.model);
    },

    /**
     * @method _renderAddToFilterAction
     * @private
     */
    _renderAddToFilterAction: function () {
      var $HTMLElement = this._renderAction('Add to filter', 'fa-bookmark');
      $HTMLElement.addClass('mas-action--filter');
    },

    /**
     * @method _renderAction
     * @param title
     * @param iconClass
     * @returns {JQuery|jQuery|HTMLElement}
     * @private
     */
    _renderAction: function (title, iconClass) {
      var $HTMLElement = $('<button class="mas-action fa">');
      $HTMLElement.attr('title', title).addClass(iconClass);
      $HTMLElement.appendTo(this.el);
      return $HTMLElement;
    }
  });

  return ElementActionsView;
});
