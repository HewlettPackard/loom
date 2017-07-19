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
  var BaseView = require('../BaseView');

  /**
   * @class  UpdatedAttributesHighlightController
   * @namespace screens.TapestryScreen
   * @module weaver
   * @submodule screens.TapestryScreen
   * @constructor
   * @extends BaseView
   */
  var UpdatedAttributesHighlightController = BaseView.extend({

    HIGHLIGHT_CLASSNAME: 'mas-sideMenuListItem-updated',

    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.activate();
    },

    /**
     * @method activate
     */
    activate: function () {
      if (this.model) {
        this.listenTo(this.model, 'change:updatedAttributes', this._refreshHighlightedAttributes);
      }
    },

    /**
     * @method deactivate
     */
    deactivate: function () {
      this.stopListening();
    },

    /**
     * @method _refreshHighlightedAttributes
     * @param element
     * @param attributes
     * @private
     */
    _refreshHighlightedAttributes: function (element, attributes) {
      this._clearHighlightedAttributes();
      this._highlightUpdatedAttributes(attributes);
    },

    /**
     * @method _clearHighlightedAttributes
     * @private
     */
    _clearHighlightedAttributes: function () {
      this.$('.' + this.HIGHLIGHT_CLASSNAME).removeClass(this.HIGHLIGHT_CLASSNAME);
    },

    /**
     * @method _highlightUpdatedAttributes
     * @param attributes
     * @private
     */
    _highlightUpdatedAttributes: function (attributes) {
      _.forEach(attributes, function (attribute) {
        var $propertyElement = this.$el.find("[data-attribute='" + attribute + "']");
        $propertyElement.addClass(this.HIGHLIGHT_CLASSNAME);
      }, this);
    }
  });

  return UpdatedAttributesHighlightController;
});
