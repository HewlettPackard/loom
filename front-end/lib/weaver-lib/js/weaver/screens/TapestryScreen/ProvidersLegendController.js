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

  var BaseView = require('../../views/BaseView');

  /**
   * todo: Is this even used? The functions are empty! Is this an interface?
   * @backbone no-initialize
   */
  var ProvidersLegendController = BaseView.extend({

    events: {
      'didExpand .mas-providersSelectionMenu': 'showLegend',
      'didCollapse .mas-providersSelectionMenu': 'hideLegend'
    },

    showLegend: function () {
    },

    hideLegend: function () {
    }
  });

  return ProvidersLegendController;
});