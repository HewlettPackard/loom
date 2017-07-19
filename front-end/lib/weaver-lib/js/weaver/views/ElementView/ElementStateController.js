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

  /** @type BaseView */
  var BaseView = require('../BaseView');

  /**
   * @class  ElementStateController
   * @module weaver
   * @submodule views.ElementView
   * @namespace  views.ElementView
   * @constructor
   * @extends BaseView
   */
  var ElementStateController = BaseView.extend({

    constructorName: 'LOOM_ElementStateController',

    /**
     * Apply only the backbone fix here as we do not want to auto trigger the template or $el element
     * @method initialize
     */
    initialize: function (options) {
      this._applyBackboneOptionsFix(options);
      this.activate();
    },

    /**
     * @method activate
     */
    activate: function () {
      if (this.model) {
        this.listenTo(this.model, 'didSetState', this.updateStateClass);
        this.updateStateClass(this.model.state);
      }
    },

    /**
     * @method deactivate
     */
    deactivate: function () {
      this.stopListening();
    },

    /**
     * @method updateStateClass
     * @param {Boolean} state
     */
    updateStateClass: function (state) {
      this._removeCurrentClass();
      this._addClass(state);
    },

    /**
     * @method _removeCurrentClass
     * @private
     */
    _removeCurrentClass: function () {
      this.$el.removeClass(this.currentClass);
      this.currentClass = undefined;
    },

    /**
     * @method _addClass
     * @param {Boolean} state
     * @private
     */
    _addClass: function (state) {
      if (state) {
        this.currentClass = 'mas-element-' + state;
        this.$el.addClass(this.currentClass);
      }
    }
  });

  return ElementStateController;
});
