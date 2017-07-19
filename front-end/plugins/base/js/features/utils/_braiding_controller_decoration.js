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
define(["require", "exports", 'weft/models/Tapestry/BraidingController'], function (require, exports, BraidingController) {
    var ref = { value: 45 };
    BraidingController.prototype.updateThreadBraiding = function (thread) {
        thread.trigger('optimalBraiding', this.get('braiding'));
    };
    BraidingController.prototype.initialize = function () {
        var _this = this;
        this.listenTo(this, 'change:braiding', function () {
            ref.value = _this.get('braiding');
        });
        this.listenTo(this, 'change:braiding', this.updateAllThreadsBraiding);
        this.listenTo(this.get('threads'), 'add', this.updateThreadBraiding);
    };
    var optimalBraiding = {
        getValue: function () {
            return ref.value;
        }
    };
    return optimalBraiding;
});
//# sourceMappingURL=_braiding_controller_decoration.js.map