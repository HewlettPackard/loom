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
define(["require", "exports", '../util/row_css'], function (require, exports, row_css) {
    var SortState;
    (function (SortState) {
        SortState[SortState["ASC"] = 0] = "ASC";
        SortState[SortState["DSC"] = 1] = "DSC";
    })(SortState || (SortState = {}));
    var SortController = (function () {
        function SortController(selector, attributes) {
            this.selector = selector;
            this.attributes = attributes;
            this.current_sorted_column = -1;
            this.current_sort_state = SortState.ASC;
        }
        SortController.prototype.refresh = function () {
            if (this.current_sorted_column < 0)
                return;
            if (this.current_sort_state === SortState.ASC) {
                this.current_sort_state = SortState.DSC;
            }
            else {
                this.current_sort_state = SortState.ASC;
            }
            this.sort_column(this.current_sorted_column);
        };
        SortController.prototype.sort_column = function (col) {
            var _this = this;
            if (col < 0)
                return;
            this.clear_old_col();
            if (col === this.current_sorted_column &&
                this.current_sort_state === SortState.ASC) {
                this.current_sort_state = SortState.DSC;
            }
            else {
                this.current_sort_state = SortState.ASC;
            }
            this.current_sorted_column = col;
            this.set_sort_css_for_col(col);
            var child_selector = '.mas-table-row';
            this.selector.$root()
                .children(child_selector)
                .sortElements(function (a, b) {
                var a_data = $(a).data("view");
                var b_data = $(b).data("view");
                a_data = a_data ? a_data.model.get(_this.attributes[col]) : -1;
                b_data = b_data ? b_data.model.get(_this.attributes[col]) : -1;
                if (typeof a_data === 'string' && typeof b_data === 'number') {
                    b_data = '';
                }
                if (typeof a_data === 'number' && typeof b_data === 'string') {
                    a_data = '';
                }
                if (a_data === b_data)
                    return 0;
                if (_this.current_sort_state === SortState.ASC) {
                    return a_data > b_data ? 1 : -1;
                }
                else {
                    return a_data < b_data ? 1 : -1;
                }
            });
            this.selector.$root()
                .children('.mas-last-row')
                .removeClass('mas-last-row');
            this.selector.$root()
                .children(child_selector)
                .each(function (index, elem) {
                $(elem).addClass(row_css(index).join(' '));
            });
        };
        SortController.prototype.set_sort_css_for_col = function (col) {
            var newHeaderCol = this.selector.select_col_header(col);
            newHeaderCol.addClass('is-sorting-column');
            if (this.current_sort_state === SortState.ASC) {
                newHeaderCol.addClass('is-sorted-asc');
            }
            else {
                newHeaderCol.addClass('is-sorted-dsc');
            }
        };
        SortController.prototype.clear_old_col = function () {
            if (this.current_sorted_column >= 0) {
                var oldHeadercol = this.selector.select_col_header(this.current_sorted_column);
                oldHeadercol.removeClass('is-sorting-column');
                if (this.current_sort_state === SortState.ASC) {
                    oldHeadercol.removeClass('is-sorted-asc');
                }
                else {
                    oldHeadercol.removeClass('is-sorted-dsc');
                }
            }
        };
        return SortController;
    })();
    return SortController;
});
