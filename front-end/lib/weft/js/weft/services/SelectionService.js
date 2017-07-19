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

  var Backbone = require('backbone');

  /**
   * SelectionService handle which element is currently selected.
   * If provided a `flag` option, the service will set this property on the element when selected
   * and unset it when deselected
   * @class SelectionService
   * @namespace services
   * @module weft
   * @submodule services
   * @extends Backbone.Model
   */
  var SelectionService = Backbone.Model.extend({

    constructorName: 'LOOM_SelectionService',

    /**
     * The elements currently selected
     * @property selection
     * @type Backbone.Model
     */

    /**
     * The property that will be set on selected elements
     * If undefined, no flag will be set on selected elements
     * @property flag
     * @type String
     * @default undefined
     */

    /**
     * Adds element to the selection.
     * No-op if the element is currently selected
     * @method select
     * @param element {Backbone.Model} The element to add to the selection
     */
    select: function (element) {
      if (element !== this.get('selection')) {
        if (this.get('selection')) {
          this._unsetFlag(this.get('selection'));
        }
        this.set('selection', element);
        this._setFlag(element);
      }
    },

    /**
     * Removes element from the selection
     * No-op if the element is not in the selection or the selection is empty
     * @method unselect
     * @param element {Backbone.Model} The element to remove from the selection
     */
    unselect: function (element) {
      if (this.get('selection') === element) {
        this.clearSelection();
      }
    },

    /**
     * Clears the selection
     * @method clearSelection
     */
    clearSelection: function () {
      this._unsetFlag(this.get('selection'));
      this.set('selection', null);
    },

    /**
     * @method _setFlag
     * @param element
     * @private
     */
    _setFlag: function (element) {
      if (this.get('flag')) {
        element.set(this.get('flag'), true);
      }
    },

    /**
     * @method _unsetFlag
     * @param element
     * @private
     */
    _unsetFlag: function (element) {
      if (this.get('flag')) {
        if (element) {
          element.set(this.get('flag'), false);
        }
      }
    }
  });

  return SelectionService;
});
