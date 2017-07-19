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
  var template = require('./FiberAlertsControllerPanel.html');
  var ItemType = require('weft/models/ItemType');


  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');

  /**
   * FiberAlertsControllerPanel displays either the list of fiber actions, or the fiber action being processed,
   * or the after processing page
   *
   * @class FiberAlertsControllerPanel
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberAlertsControllerPanel = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_FiberActionList
     * @final
     */
    constructorName: 'LOOM_FiberAlertsControllerPanel',

    className: "mas-fiberAlertsControllerPanel",

    template: template,

    /**
     * @method initialize
     */
    initialize: function (options) {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = options.model;
      this.listenTo(this.EventBus, 'side-menu:fiber:tab:selected', function(event) {
        if (event.tab === 'alerts') {
          this.render();
        }
      });
      this.listenTo(this.EventBus, 'fiber:selected:alert:change', this.render);
      this.listenTo(this.EventBus, 'fiber:unselected', this.render);
      this.render();
    },

    /**
     * Toggles the alert header state between 'Error' and 'Warning'
     * @param alertIsError
     */
    toggleAlertHeader: function (alertIsError) {
      this.$el.find('.mas-fiberAlertsControllerPanel--alert-header-error').toggleClass('hideFlex', !alertIsError);
      this.$el.find('.mas-fiberAlertsControllerPanel--alert-header-warning').toggleClass('hideFlex', alertIsError);
    },

    /**
     * Puts the alert values into the correct DOM elements
     * displayAlertValues
     * @param alert
     */
    updateAlertValuesInDOM: function (alert) {
      this.$el.find('.mas-fiberAlertsControllerPanel--number-of-items').text(alert.get('count'));
      this.$el.find('.mas-fiberAlertsControllerPanel--severity-level').text(alert.get('level'));
      this.$el.find('.mas-fiberAlertsControllerPanel--message').text(alert.get('description'));
    },

    /**
     * Hides the alert
     */
    shouldShowAlert: function (showAlert) {
      this.$el.find('.mas-fiberAlertsControllerPanel--no-alerts').toggleClass('hideFlex', showAlert);
      this.$el.find('.mas-fiberAlertsControllerPanel--alerts').toggleClass('hideFlex', !showAlert);
    },

    shouldHideNumberOfItems: function() {
      if (this.model.itemType instanceof ItemType) {
        this.$el.find('.mas-fiberAlertsControllerPanel--number-of-items').closest('li').hide();
      } else {
        this.$el.find('.mas-fiberAlertsControllerPanel--number-of-items').closest('li').show();
      }
    },

    /**
     * Renders the various components into the tabbed side menu view
     * @returns {FiberAlertsControllerPanel}
     * @chainable
     */
    render: function() {
      if (this.model.alert && this.model.alert.hasAlert()) {
        this.toggleAlertHeader(this.model.alert.get('level') >= 6);
        this.updateAlertValuesInDOM(this.model.alert);
        this.shouldHideNumberOfItems();
        this.shouldShowAlert(true);
      } else {
        this.shouldShowAlert(false);
      }
      return this;
    }

  });

  return FiberAlertsControllerPanel;
});
