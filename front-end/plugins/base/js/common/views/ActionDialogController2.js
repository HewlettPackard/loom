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
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", 'weaver/views/ElementDetailsView/ActionDialogController', './ActionDialogView2'], function (require, exports, ActionDialogController, ActionDialogView2) {
    var ActionDialogController2 = (function (_super) {
        __extends(ActionDialogController2, _super);
        function ActionDialogController2() {
            _super.apply(this, arguments);
        }
        ActionDialogController2.prototype.showDialog = function (actionDefinition) {
            this.hideDialog();
            this.dialog = new ActionDialogView2({
                model: actionDefinition,
                element: this.model
            });
            // IMPROVE: Create `showAction()` and `showData()` method on the `ElementDetailsView`
            this.$('.mas-elementDetails--body').prepend(this.dialog.$el);
        };
        return ActionDialogController2;
    })(ActionDialogController);
    return ActionDialogController2;
});
//# sourceMappingURL=ActionDialogController2.js.map