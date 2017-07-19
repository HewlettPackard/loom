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
define(["require", "exports", 'lodash', './Rect'], function (require, exports, _, rect_module) {
    var Rect = rect_module.Rect;
    var Direction = rect_module.Direction;
    var opposite = rect_module.opposite;
    var Treemap = (function () {
        function Treemap() {
        }
        Treemap.prototype.squarify = function (elements, box) {
            this.x = box.x;
            this.y = box.y;
            this.cdir = box.shorter_direction();
            this.total = _.reduce(elements, function (s, e) { return e.area + s; }, 0);
            this.cbox = new Rect(box);
            var fullbox_area = box.width * box.height;
            var row = [];
            var i = 0;
            this.row_total = 0;
            while (i < elements.length) {
                var child = elements[i];
                if (row.length == 0) {
                    row.push(child);
                    this.row_total += child.area;
                    i++;
                }
                else {
                    if (this.is_ratio_improved(row, child)) {
                        row.push(child);
                        this.row_total += child.area;
                        i++;
                    }
                    else {
                        this.layout_row(row, fullbox_area);
                        row = [];
                        this.total -= this.row_total;
                        this.row_total = 0;
                        this.cdir = this.cbox.shorter_direction();
                    }
                }
            }
            if (row.length > 0) {
                this.layout_row(row, fullbox_area);
            }
        };
        Treemap.prototype.is_ratio_improved = function (row, child) {
            var _this = this;
            var row_total = this.row_total;
            var worst_ratio = _.reduce(row, function (s, e) {
                return Math.max(_this.ratio(e, row_total), s);
            }, 0, this);
            row_total += child.area;
            var new_worst_ratio = _.reduce(row, function (s, e) {
                return Math.max(_this.ratio(e, row_total), s);
            }, 0, this);
            return worst_ratio > new_worst_ratio;
        };
        Treemap.prototype.ratio = function (element, row_total) {
            var a = this.get_direction_size(element, row_total);
            var b = this.get_opposite_dir_size(row_total);
            return Math.max(a / b, b / a);
        };
        Treemap.prototype.layout_row = function (row, fullbox_area) {
            var row_total = this.row_total;
            var b = this.get_opposite_dir_size(row_total);
            var x = this.x;
            var y = this.y;
            var el;
            while (el = row.pop()) {
                var a = this.get_direction_size(el, row_total);
                el.box.set(this.cdir, a);
                el.box.set(opposite(this.cdir), b);
                el.box.x = x;
                el.box.y = y;
                if (this.cdir == Direction.Vertical) {
                    y += a;
                }
                else {
                    x += a;
                }
            }
            if (this.cdir == Direction.Vertical) {
                this.x += b;
            }
            else {
                this.y += b;
            }
            var oldvalue = this.cbox.get(opposite(this.cdir));
            this.cbox.set(opposite(this.cdir), oldvalue - b);
        };
        Treemap.prototype.get_direction_size = function (element, row_total) {
            return this.cbox.get(this.cdir) * element.area / row_total;
        };
        Treemap.prototype.get_opposite_dir_size = function (row_total) {
            return row_total *
                this.cbox.get(opposite(this.cdir)) / this.total;
        };
        return Treemap;
    })();
    exports.Treemap = Treemap;
});
