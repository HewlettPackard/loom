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
define(["require", "exports", '../../features/utils/display_mode'], function (require, exports, display_mode) {
    var displayModeAvailables = display_mode.displayModeAvailables;
    var DisplayMode = (function () {
        function DisplayMode(options) {
            this.thread = options.thread;
        }
        DisplayMode.prototype.hasManyDisplayMode = function () {
            return this.getPossibleDisplayModeArray().length > 1;
        };
        DisplayMode.prototype.getPossibleDisplayModeObject = function () {
            var _this = this;
            return _.omit(displayModeAvailables, function (context) {
                return context.available(_this.thread) !== true;
            });
        };
        DisplayMode.prototype.getPossibleDisplayModeWithReadableName = function () {
            return _.mapValues(this.getPossibleDisplayModeObject(), function (context) {
                return context.readableName;
            });
        };
        DisplayMode.prototype.getPossibleDisplayModeArray = function () {
            return _.keys(this.getPossibleDisplayModeObject());
        };
        DisplayMode.CLASSIC = 'classic';
        return DisplayMode;
    })();
    return DisplayMode;
});
//# sourceMappingURL=DisplayMode.js.map