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
define(["require", "exports"], function (require, exports) {
    var Rect = (function () {
        function Rect(x, y, width, height) {
            if (y === void 0) { y = 0; }
            if (width === void 0) { width = 0; }
            if (height === void 0) { height = 0; }
            if (typeof x == 'number') {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }
            else if (typeof x == 'object') {
                this.x = x.x;
                this.y = x.y;
                this.width = x.width;
                this.height = x.height;
            }
        }
        Rect.prototype.shorter_direction = function () {
            if (this.width > this.height) {
                return Direction.Vertical;
            }
            else {
                return Direction.Horizontal;
            }
        };
        Rect.prototype.set = function (d, value) {
            if (d == Direction.Horizontal) {
                this.width = value;
            }
            else {
                this.height = value;
            }
        };
        Rect.prototype.get = function (d) {
            if (d == Direction.Vertical) {
                return this.height;
            }
            else {
                return this.width;
            }
        };
        return Rect;
    })();
    exports.Rect = Rect;
    (function (Direction) {
        Direction[Direction["Horizontal"] = 0] = "Horizontal";
        Direction[Direction["Vertical"] = 1] = "Vertical";
    })(exports.Direction || (exports.Direction = {}));
    var Direction = exports.Direction;
    function opposite(d) {
        if (d == Direction.Horizontal) {
            return Direction.Vertical;
        }
        return Direction.Horizontal;
    }
    exports.opposite = opposite;
});
