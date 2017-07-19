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
define(["require", "exports", './QueryAutoUpdater', 'weft/models/Operation'], function (require, exports, QueryAutoUpdater, Operation) {
    /**
     * A QueryAutoUpdaterBraiding update the braid limit
     *
     * @class QueryAutoUpdaterBraiding
     * @namespace weaver-map.models
     * @module weaver-map
     */
    var QueryAutoUpdaterBraiding = (function (_super) {
        __extends(QueryAutoUpdaterBraiding, _super);
        function QueryAutoUpdaterBraiding(options) {
            _super.call(this, options);
        }
        QueryAutoUpdaterBraiding.prototype.creator = function (options) {
            _super.prototype.creator.apply(this, arguments);
            // --------------------------------------------------------
            // We listen to optimal braiding change
            //
            // @see BraidingControllerDecorator
            //
            this.listenTo(this.thread, 'optimalBraiding', function (optimalBraiding) {
                // Update
                this._updateBraiding(optimalBraiding);
                // Stored to allow forced update.
                this.optimalBraiding = optimalBraiding;
            });
        };
        QueryAutoUpdaterBraiding.prototype.forceUpdate = function () {
            if (this.optimalBraiding) {
                this._updateBraiding(this.optimalBraiding);
            }
        };
        QueryAutoUpdaterBraiding.prototype._updateBraiding = function (optimalBraiding) {
            // We update appropriately the thread.
            if (this.thread.hasLimit(Operation.BRAID_ID)) {
                this.thread.limitWith({
                    operator: Operation.BRAID_ID,
                    parameters: {
                        maxFibres: optimalBraiding
                    }
                });
            }
        };
        return QueryAutoUpdaterBraiding;
    })(QueryAutoUpdater);
    return QueryAutoUpdaterBraiding;
});
//# sourceMappingURL=QueryAutoUpdaterBraiding.js.map