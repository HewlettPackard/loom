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
define(["require", "exports", 'jquery', 'lodash'], function (require, exports, $, _) {
    var SelectorController = (function () {
        function SelectorController(context) {
            this.id = _.uniqueId();
            this.context = context ? context : $;
        }
        SelectorController.prototype.col_css = function (i) {
            return 'table-' + this.id + '-col-' + i;
        };
        SelectorController.prototype.select_col = function (i) {
            return this.context('.' + this.col_css(i));
        };
        SelectorController.prototype.select_col_header = function (i) {
            return this.context('.mas-table-header .' + this.col_css(i));
        };
        SelectorController.prototype.$root = function () {
            return this.context('.mas-table tbody');
        };
        return SelectorController;
    })();
    return SelectorController;
});
