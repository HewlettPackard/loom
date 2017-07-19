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
  var Menu = require('weaver/views/Menu');
  var positionMenuContent = require('weaver/views/helpers/positionMenuContent');
  var PropertySelector = require('weaver/views/PropertySelector');

  /**
   * AddOperationMenu is a menu that allows the user to select
   * which operation to add to the query editor among the list
   * of operation provided as its model
   * @class  AddOperationMenu
   * @namespace  views.QueryEditor
   * @module  weaver
   * @submodule views.QueryEditor
   * @constructor
   * @extends Menu
   */
  return Menu.extend({

    /**
     * @property {Array} model The list of available operations
     */

    /**
     * @method expand
     */
    expand: function () {
      positionMenuContent(this).then(_.bind(function () {
        this.propertySelector = new PropertySelector({
          model: this.model
        });
        this.propertySelector.on('change:selection', this._notifyOperationChoice, this);
        this.propertySelector.$el.addClass('mas-menu--content');

        this.$('.mas-menu--content').replaceWith(this.propertySelector.el);
        Menu.prototype.expand.apply(this, arguments);
      }, this)).done();
    },

    /**
     * @method _notifyOperationChoice
     * @param index
     * @private
     */
    _notifyOperationChoice: function (index) {
      /**
       * Notifies that the user has selected an operation to add
       * @event choseOperation
       */
      this.dispatchCustomEvent('choseOperation', {
        operator: this.model[index].id
      });
    }
  });
});