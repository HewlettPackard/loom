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

  /**
   * AttributesUpdateMonitor is a helper that monitors the changes
   * of the attributes of the elements and marks them as updated
   * for a given period of time
   *
   * todo: this class should be removed. it doesn't do anything non 'element' specific and should be merged into element
   * @param {[type]} element [description]
   * @class AttributeUpdatesMonitor
   * @module weft
   * @submodule models.element
   * @namespace models.element
   */
  function AttributeUpdatesMonitor(element) {
    this.element = element;
    this.element.on('change', this._markAttributeUpdates, this);
  }

  _.extend(AttributeUpdatesMonitor.prototype, {
    /**
     * a method that marks an element as updated if any of the displayable properties have changed
     * @method _markAttributeUpdates
     * @private
     */
    _markAttributeUpdates: function () {
      if (this.element.hasAnyDisplayablePropertyChanged()) {
        this.element.setUpdatedState();
      }
    }
  });

  return AttributeUpdatesMonitor;
});
