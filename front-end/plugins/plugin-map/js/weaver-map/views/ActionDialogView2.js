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

  var ActionDialogView = require("weaver/views/ActionDialogView");

  var ActionDialogView2 = ActionDialogView.extend({

    initialize: function () {
      ActionDialogView.prototype.initialize.apply(this, arguments);

      this.$el.css('padding-top', '20px');
      this.$el.css('height', '100%');
      this.$el.css('width', '100%');
      this.$el.css('position', 'absolute');
    },

  });

  return ActionDialogView2;
});