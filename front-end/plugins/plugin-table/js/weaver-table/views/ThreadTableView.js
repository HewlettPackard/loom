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
define(["require", "exports", 'weaver/views/ThreadView', './TableView'], function (require, exports, ThreadView, TableView) {
    var ThreadTableView = (function (_super) {
        __extends(ThreadTableView, _super);
        function ThreadTableView(options) {
            _super.call(this, options);
            this.listenTo(this.model, 'didPressActionView', this._toggleThreadDisplay);
        }
        ThreadTableView.prototype._createViewElements = function () {
            return new TableView({
                className: 'mas-thread--fibers',
                model: this.model
            });
        };
        return ThreadTableView;
    })(ThreadView);
    return ThreadTableView;
});
