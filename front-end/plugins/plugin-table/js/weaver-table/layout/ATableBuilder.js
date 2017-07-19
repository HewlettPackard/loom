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
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
define(["require", "exports", "plugins/common/layout/AbstractBuilder"], function (require, exports, AbstractBuilder) {
    var ATableBuilder = (function (_super) {
        __extends(ATableBuilder, _super);
        function ATableBuilder() {
            _super.apply(this, arguments);
        }
        ATableBuilder.prototype.col_css = function (i) {
            return '';
        };
        ATableBuilder.prototype.build_line = function (values, classes) {
            if (classes.cols) {
                classes.cols.sort(function (a, b) {
                    if (a.col < b.col) {
                        return -1;
                    }
                    if (a.col > b.col) {
                        return 1;
                    }
                    return 0;
                });
            }
            else {
                classes.cols = [];
            }
            var row = this.buildRow(classes.row);
            row.appendChild(this.buildTag({
                tag: 'td',
                classes: this.firstCellClass()
            }));
            for (var j = 0, i = 0; i < values.length; ++i) {
                var val = [classes.generic_cell, this.col_css(i)];
                if (j < classes.cols.length && classes.cols[j].col === i) {
                    if (typeof classes.cols[j].classes === 'string') {
                        val.push(classes.cols[j].classes);
                    }
                    if (typeof classes.cols[j].classes === 'object') {
                        val = val.concat(classes.cols[j].classes);
                    }
                    j++;
                }
                var cell = this.buildCell(i, val, values[i].as_str());
                row.appendChild(cell);
                if (i !== values.length - 1) {
                    var separator = this.buildTag({
                        tag: cell.tagName,
                        classes: classes.separator,
                    });
                    row.appendChild(separator);
                }
            }
            return row;
        };
        ATableBuilder.prototype.firstCellClass = function () {
            return ['mas-fiberState'];
        };
        ATableBuilder.prototype.buildRow = function (classes) {
            return this.buildTag({
                tag: 'tr',
                classes: classes,
            });
        };
        ATableBuilder.prototype.buildCell = function (i, classes, value) {
            var cell = this.buildTag({
                tag: 'td',
                classes: classes,
            });
            var nested = this.buildNested(i, value);
            cell.appendChild(nested);
            return cell;
        };
        ATableBuilder.prototype.buildNested = function (i, value) {
            var nested = this.buildTag({ tag: 'div', classes: 'nested' });
            nested.appendChild(this.buildText(value));
            return nested;
        };
        return ATableBuilder;
    })(AbstractBuilder);
    return ATableBuilder;
});
