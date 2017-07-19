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
  /** @type BaseView */
  var BaseView = require('./../BaseView');
  var template = require('./ElementDetailsView.html');
  var ElementActionsView = require('./ElementActionsView');
  var UpdatedAttributesHighlightController = require('./UpdatedAttributesHighlightController');
  var AlertDetailsView = require('./AlertDetailsView');
  var FiberAttributeView = require('./../FiberAttributeView');

  /**
   * ElementDetailsViews display the properties of Threads and Items
   * and provide ways to execute actions upon them
   * @class ElementDetailsView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ElementDetailsView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ElementDetailsView
     * @final
     */
    constructorName: 'LOOM_ElementDetailsView',

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    events: {
      'click .mas-action--view': function(event) {
        // @todo jae, rename this message to something meaningful :)
        this.EventBus.trigger('some-message', event);
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      // this.model.on('change', this._updatePropertyValues, this);
      this.listenTo(this.model, 'change:outdated', this._updateActionsAvailability);
      this.listenTo(this.model.alert, 'change:level', this._updateAlert);
      this.actionDetailsView = new AlertDetailsView({
        model: this.model.alert
      });
      this.$('.mas-alertDetails').replaceWith(this.actionDetailsView.el);
      this.updatedAttributesHighlightController = new UpdatedAttributesHighlightController({
        model: this.model.updatedAttributesMonitor,
        el: this.el
      });
      this.actions = {};
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this._renderActions();
      this._updateActionsAvailability();
      this._updateTitle();
      this._updateProperties();
      this._updateAlert();
    },

    /**
     * Disables the actions available for this element
     * @method disableActions
     */
    disableActions: function () {
      this.$('.mas-action').attr('disabled', true);
    },

    /**
     * Enables the actions available for this element
     * @method enableActions
     */
    enableActions: function () {
      this.$('.mas-action').removeAttr('disabled');
    },

    /**
     * @method _updateActionsAvailability
     * @private
     */
    _updateActionsAvailability: function () {
      if (this.model.get('outdated')) {
        this.disableActions();
      } else {
        this.enableActions();
      }
    },

    /**
     * Updates the list of actions available for the element
     * @method _updateAction
     * @private
     */
    _renderActions: function () {
      this.elementActions = new ElementActionsView({
        el: this.$('.mas-elementDetails--actions'),
        model: this.model
      });
    },

    /**
     * Cleans a label if undefined or described as being so
     * @param label
     * @returns {string}
     * @private
     */
    _checkLabel: function(label) {
      return (label === 'l.undefined' || label === undefined) ? '' : label;
    },

    /**
     * @method _updateTitle
     * @private
     */
    _updateTitle: function () {
      var label = this._checkLabel(this.model.get('name'));
      var thread = this.model.get('parent');
      if (thread && thread.isGrouped()) {
        var groupByOperation = thread.getLastGroupByOperation();
        var unit = thread.getUnit(groupByOperation.parameters.property);
        if (unit) {
          label += ' ' + unit;
        }
      }
      this.$('.mas-elementDetails--title').html('<p>' + label + '</p>');
      // TODO: Improve animation so the text does not disappear
      // to the left of the element
      //this.labelScrollTimeout = setTimeout(_.bind(this._scrollLabel, this), 2000);
    },

    /**
     * @method _scrollLabel
     * @private
     */
    _scrollLabel: function () {
      var title = this.$('.mas-elementDetails--title');
      var actionBar = this.$('.mas-elementDetails--actions');
      if (title.find('p').width() > (title.width() - actionBar.width())) {
        title.addClass('mas-elementDetails--scrolling');
      }
    },

    /**
     * @method _updateProperties
     * @private
     */
    _updateProperties: function () {
      var $properties = this.$('.mas-elementDetails--properties');
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
    },

    /**
     * @method _updateAlert
     * @private
     */
    _updateAlert: function () {
      if (this.model.alert.get('level')) {
        this.$el.removeClass('mas-elementDetails-noAlert');
      } else {
        this.$el.addClass('mas-elementDetails-noAlert');
      }
    }
  });

  return ElementDetailsView;
});
