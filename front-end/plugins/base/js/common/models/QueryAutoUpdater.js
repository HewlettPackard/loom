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
define(["require", "exports", 'lodash', 'backbone'], function (require, exports, _, Backbone) {
    /**
     * The query auto-updater is a per-thread controller
     * that automatically update operations that are in "auto" mode.
     * If you want to create your own, just subclass this one
     * and set it in your subclass of ThreadView.
     *
     * Currently they are two updaters availables:
     *   - QueryAutoUpdaterMapOperations
     *   - QueryAutoUpdaterBraiding
     *
     * @class QueryAutoUpdater
     * @namespace weaver-map.models
     * @module weaver-map
     */
    var QueryAutoUpdater = (function (_super) {
        __extends(QueryAutoUpdater, _super);
        function QueryAutoUpdater(options) {
            if (_.isFunction(Backbone.Events)) {
                _super.call(this);
            }
            options = options || {};
            this.thread = options.thread;
            return this.creator.apply(this, arguments);
        }
        /**
         * Contructor called that is allow to return an object on construction.
         * Usefull to allow caching.
         *
         * The default constructor should always be called, it takes care
         * of removing itself when the query updater is changed.
         *
         * @param {Object} options is the traditional arguments object map like.
         * @return {Object} Returns the constructed object.
         */
        QueryAutoUpdater.prototype.creator = function (options) {
            this.listenTo(this.thread, 'change:queryUpdater', function (thread, newQueryUpdater) {
                if (newQueryUpdater !== this) {
                    this.detach();
                }
            });
            // Unnecessary but self documenting.
            return this;
        };
        /**
         * Detach the query updater that has been replaced by a new one.
         * By default call, the stopListening method, that should be more than
         * sufficient in most of cases.
         */
        QueryAutoUpdater.prototype.detach = function () {
            this.stopListening();
        };
        /**
         * Force the update of the query.
         * You have to override this method to allow users of query Updater
         * to force an update of their query.
         */
        QueryAutoUpdater.prototype.forceUpdate = function () {
            throw "This function should be overriden";
        };
        return QueryAutoUpdater;
    })(Backbone.Events);
    /**
    * Offer Backbone.Events facilities to QueryAutoUpdaters.
    */
    _.extend(QueryAutoUpdater.prototype, Backbone.Events);
    return QueryAutoUpdater;
});
//# sourceMappingURL=QueryAutoUpdater.js.map