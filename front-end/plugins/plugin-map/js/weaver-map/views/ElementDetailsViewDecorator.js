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

  var ElementDetailsView = require('weaver/views/ElementDetailsView');

  ElementDetailsView.prototype._updateTitle = function () {

    var label = this.model.getTranslated('name');     // ONLY CHANGE
    var thread = this.model.get('parent');
    if (thread && thread.isGrouped()) {
      var groupByOperation = thread.getLastGroupByOperation();
      var unit = thread.getUnit(groupByOperation.parameters.property);
      if (unit) {
        label += ' ' + unit;
      }
    }
    this.$('.mas-elementDetails--title').html('<p>' + label + '</p>');


    // TODO: Improve animation so the text does not disapear
    // to the left of the element
    //this.labelScrollTimeout = setTimeout(_.bind(this._scrollLabel, this), 2000);
  };
});