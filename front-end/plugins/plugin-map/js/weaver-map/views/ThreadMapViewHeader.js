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

  var ThreadViewHeader = require('weaver/views/ThreadViewHeader');

  var ThreadMapViewHeader = ThreadViewHeader.extend({

    initialize: function () {
      ThreadViewHeader.prototype.initialize.apply(this, arguments);

      this.headerActionsMenu.expand();
      this.headerActionsMenu.collapse();
      this.headerActionsMenu.metricsMenu.disable();
      // TODO: dirty hack to really disable the sorting menu. Don't even work on sub threads :(
      var self = this;
      this.headerActionsMenu.sortingMenu.listenTo(this.headerActionsMenu.sortingMenu.model.availableSorts, 'add remove', function () {
        self.headerActionsMenu.sortingMenu.disable();
        self.headerActionsMenu.sortingMenu.model.availableSorts.size = function () { return 0; };
      });
      this.headerActionsMenu.sortingMenu.disable();
    },
  });

  return ThreadMapViewHeader;
});