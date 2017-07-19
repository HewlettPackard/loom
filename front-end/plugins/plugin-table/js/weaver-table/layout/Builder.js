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
define(["require", "exports", "./ATableBuilder", 'plugins/common/layout/builder_interfaces', '../controller/TableHeaderListener', '../controller/TableHeaderSortListener', '../util/row_css'], function (require, exports, ATableBuilder, builder_interfaces, TableHeaderListener, TableHeaderSortListener, row_css) {
    var HeaderBuilder = (function (_super) {
        __extends(HeaderBuilder, _super);
        function HeaderBuilder(selector) {
            _super.call(this);
            this.selector = selector;
            this.headerCells = {};
        }
        HeaderBuilder.prototype.buildNested = function (i, value) {
            var nested = _super.prototype.buildNested.call(this, i, value);
            var node = this.buildTag({
                tag: 'div',
                classes: 'mas-sorting-indicator',
            });
            nested.insertBefore(node, nested.firstChild);
            return nested;
        };
        HeaderBuilder.prototype.col_css = function (i) {
            return this.selector.col_css(i);
        };
        HeaderBuilder.prototype.firstCellClass = function () {
            return [];
        };
        HeaderBuilder.prototype.buildCell = function (i, classes, value) {
            var cell = this.buildTag({
                tag: 'th',
                classes: classes,
            });
            var nested = this.buildNested(i, value);
            cell.appendChild(nested);
            var tooltip = this.buildTag({
                tag: 'div',
                classes: ['mas-fiberOverview--tooltip', 'mas-tooltip', 'mas-tooltip-bottomRightArrow'],
            });
            this.headerCells[i] = {
                class_col: this.col_css(i),
                attribute_pos: i,
            };
            tooltip.appendChild(this.buildText(value));
            cell.appendChild(tooltip);
            return cell;
        };
        return HeaderBuilder;
    })(ATableBuilder);
    var Builder = (function (_super) {
        __extends(Builder, _super);
        function Builder() {
            _super.call(this);
            this.headerCells = {};
        }
        Builder.prototype.build = function (header, columnsCss, elements, viewfactory, thread, builder) {
            var views = [];
            var table = this.buildTag({
                tag: 'table',
                classes: ['mas-table']
            });
            this.selector = builder.selector;
            var i = 0;
            var cols = columnsCss.map(function (css) { return { classes: [css], col: i++ }; });
            var sortController = this.createAttributeHeader(header, cols);
            var collapseColumnController = this.createCollapseHeader(cols);
            var tableHeader = this.buildTag({ tag: 'thead' });
            var tableBody = this.buildTag({ tag: 'tbody' });
            table.appendChild(tableHeader);
            table.appendChild(tableBody);
            tableHeader.appendChild(sortController.el);
            tableHeader.appendChild(collapseColumnController.el);
            var row = 0;
            _.forEach(elements, function (element) {
                var rcss = row_css(row);
                row++;
                var el = builder.buildFibre({ model: element, attribute_class: columnsCss, row_css: rcss });
                views.push(viewfactory({
                    el: el,
                    model: element,
                    builder: builder,
                    thread: thread,
                }));
                tableBody.appendChild(el);
            });
            return {
                el: table,
                views: views,
                collapseController: collapseColumnController,
                sortController: sortController,
            };
        };
        Builder.prototype.createAttributeHeader = function (header, cols) {
            var headerBuilder = new HeaderBuilder(this.selector);
            var sortColumnController = new TableHeaderSortListener(headerBuilder.build_line(header.values, {
                row: ['mas-table-header', 'mas-table-row'],
                separator: 'mas-table-separator',
                generic_cell: 'mas-table-cell',
                cols: cols
            }));
            _.forEach(headerBuilder.headerCells, function (c) {
                sortColumnController.listenToColumn(c.class_col, c.attribute_pos);
            });
            return sortColumnController;
        };
        Builder.prototype.createCollapseHeader = function (cols) {
            var collapse_values = _.times(cols.length, function () { return builder_interfaces.as_line_value(''); });
            var collapseColumnController = new TableHeaderListener(this.build_line(collapse_values, {
                row: ['mas-table-row', 'mas-table-collapse-actions'],
                separator: 'mas-table-separator',
                generic_cell: 'mas-table-cell',
                cols: cols
            }));
            _.forEach(this.headerCells, function (c) {
                collapseColumnController.listenToColumn(c.class_col, c.attribute_pos);
            });
            return collapseColumnController;
        };
        Builder.prototype.col_css = function (i) {
            return this.selector.col_css(i);
        };
        Builder.prototype.firstCellClass = function () {
            return [];
        };
        Builder.prototype.buildNested = function (i, value) {
            var nested = _super.prototype.buildNested.call(this, i, value);
            $(nested).addClass('mas-collapse-action');
            return nested;
        };
        Builder.prototype.buildCell = function (i, classes, value) {
            var el = _super.prototype.buildCell.call(this, i, classes, value);
            this.headerCells[i] = {
                class_col: this.col_css(i),
                attribute_pos: i,
            };
            return el;
        };
        return Builder;
    })(ATableBuilder);
    return Builder;
});
