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

  require('backbone.stickit');

  /** @type AbstractElementView */
  var AbstractElementView = require('./AbstractElementView');
  var FiberOverview = require('./FiberOverview');
  var ElementDetailsView = require('./ElementDetailsView');


  /**
   * @type BaseView
   * @Type EventBusMixin
   */
  var BaseView = require('./../BaseView');
  var MetricDisplayController = require('./../ElementView/MetricDisplayController');
  var ElementStateController = require('./../ElementView/ElementStateController');

  /**
   * ElementView handles the display of elements
   * @class ElementView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends AbstractElementView
   * @constructor
   */
  var ElementView = AbstractElementView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ElementView
     * @final
     */
    constructorName: 'LOOM_ElementView',

    /**
     * @property bindings
     * @type {Object}
     */
    bindings: {
      ':el': {
        classes: {
          'is-related': 'related',
          'is-furtherRelated': 'furtherRelated',
          'is-highlighted': 'highlighted',
          'is-fromHighlightedProvider': 'isFromHighlightedProvider'
        }
      }
    },

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click': function () {
        var threadView = this.$el.parents('.mas-threadView').data('view');
        this.EventBus.trigger('fiber:click', {
          fiber: this.model,
          fiberView: this,
          thread: threadView.model,
          threadView: threadView
        });
      },
      'click .mas-action--filter': 'filterElement'
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.fiberOverview = new FiberOverview({
        model: this.model
      });
      this.fiberOverview.$('.mas-fiberOverview--block').addClass('mas-element--content');
      this.$el.append(this.fiberOverview.$el);
      AbstractElementView.prototype.initialize.apply(this);
      this.listenTo(this.model, 'change:isPartOfFilter', this.setFilter);
      this.metricDisplayController = new MetricDisplayController(this, this.options.metrics);
      this.elementStateController = new ElementStateController({
        el: this.el,
        model: this.model
      });
      this.$el.attr('data-id', this.model.get('l.logicalId'));
      this.listenTo(this.EventBus, 'fiber:selected', this.selectElement);
      this.listenTo(this.EventBus, 'fiber:unselected', this.unselectElement);
      this.listenTo(this.EventBus, 'thread:fiber:action:focus', this.filterElement);
    },

    /**
     * @method selectElement
     */
    selectElement: function (event) {
      if (event.fiberView !== this) {
        return;
      }
      this._updateSelectedState(true);
      this.showTooltip();
    },

    /**
     * @method unselectElement
     */
    unselectElement: function (event) {
      if (event.fiberView === this) {
        this._updateSelectedState(false);
        this.hideTooltip();
      }
    },

    /**
     * @method disableActions
     */
    disableActions: function () {
      if (this.details) {
        this.details.disableActions();
      }
    },

    /**
     * @method enableActions
     */
    enableActions: function () {
      if (this.details) {
        this.details.enableActions();
      }
    },

    /**
     * Shows a tooltip over the element
     * @method showTooltip
     * @param  {Object} vertical       The vertical offset of the tooltip
     * @param  {Object} horizontal     The v offset of the tooltip
     */
    showTooltip: function (vertical, horizontal) {
      this.$('.mas-fiberOverview--tooltip').css({
        bottom: vertical || 0,
        right: horizontal || 0
      });
      this.$el.addClass('has-visibleTooltip');
    },

    /**
     * Hides the tooltip displayed over the element
     * @method hideTooltip
     */
    hideTooltip: function () {
      this.$el.removeClass('has-visibleTooltip');
    },

    /**
     * @method filterElement
     */
    filterElement: function (event) {
      if (event.fiberView === this) {
        if (!this.model.get('disabled')) {
          this.model.set('isPartOfFilter', !this.model.get('isPartOfFilter'));
        }
      }
    },

    /**
     * @method setFilter
     */
    setFilter: function () {
      if (this.model.get('isPartOfFilter')) {
        this._dispatchEvent('action:addFilterElement');
        this.$el.addClass('is-partOfFilter');
      } else {
        this._dispatchEvent('action:removeFilterElement');
        this.$el.removeClass('is-partOfFilter');
      }
    },

    /**
     * @method remove
     */
    remove: function () {
      this.broadcast('element:will:remove', {
        view: this
      });
      this.metricDisplayController.deactivate();
      this.elementStateController.deactivate();
      AbstractElementView.prototype.remove.apply(this, arguments);
    },

    /**
     * @method updateLabel
     */
    updateLabel: function (str) {
      this.fiberOverview.setLabel(str);
    },

    /**
     *  @method render
     */
    render: function () {
      AbstractElementView.prototype.render.apply(this);
      this.stickit();
      this.updateLabel(this.model.get('name'));
    },

    /**
     * @method _updateElementDetails
     * @private
     */
    _updateElementDetails: function (selected) {
      if (selected) {
        this.details = new ElementDetailsView({
          model: this.model
        });
        this.details.$el.addClass('mas-element--content').prependTo(this.el);
      } else if (this.details) {
        this.details.remove();
      }
    },

    /**
     * @method _updateSelectedState
     * @private
     */
    _updateSelectedState: function (selected) {
      if (selected) {
        this.$el.addClass('is-selected');
      } else {
        this.$el.removeClass('is-selected');
      }
      //this._updateElementDetails(selected);
      this.model.set('selected', selected);
      this.model.trigger('refresh');
    }
  });

  return ElementView;
});
