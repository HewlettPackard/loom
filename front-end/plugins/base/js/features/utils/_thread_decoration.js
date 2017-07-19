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
define(["require", "exports", 'weft/models/Thread', 'lodash', '../../common/models/QueryAutoUpdaterBraiding', '../../common/models/Translator', './thread_attributes_to_clone', '../../common/utils/DisplayMode'], function (require, exports, Thread, _, QueryAutoUpdaterBraiding, Translator, thread_attributes_to_clone, DisplayMode) {
    var originalDefaults = Thread.prototype.defaults;
    Thread.prototype.defaults = function () {
        var def = originalDefaults.call(this);
        return _.extend(def, {
            /**
             * The query updater is a per-thread controller that is allow to update
             * the query operations based on external events. The default one, provided here,
             * does nothing.
             *
             * @see QueryAutoUpdaterMapOperations
             * @see QueryAutoUpdaterBraiding
             *
             * @attribute queryUpdater
             * @type {QueryAutoUpdater}
             */
            queryUpdater: new QueryAutoUpdaterBraiding({
                thread: this
            }),
            /**
             * The string contains an indication for representing the data.
             * To see the full list of displayModes see DisplayMode in Weaver.
             * @property displayMode
             * @default undefined (The thread is not displayed yet)
             */
            displayMode: DisplayMode.CLASSIC,
            /**
             * Contains the different cluster operations.
             * @type {Object}
             */
            itemClusterBy: {},
            /**
             * Translator reference, Allows to show better names than
             * the one outputed by loom. Currently essentially used for countries.
             */
            translator: new Translator(),
            /**
             * Attribute storing view-specific data.
             * @attribute viewData
             */
            viewData: undefined
        });
    };
    Thread.prototype.clone = function () {
        var _this = this;
        var items = {};
        _.forEach(thread_attributes_to_clone, function (attribute) {
            items[attribute] = _this.get(attribute);
        });
        items['query'] = this.get('query').clone();
        items['viewData'] = this.get('viewData') ? this.get('viewData').clone() : undefined;
        return new Thread(items);
    };
    Thread.prototype.createNestedThread = function (aggregation) {
        var nestedThread = this.clone();
        var displayMode = aggregation.get('displayMode');
        displayMode = displayMode ? displayMode : DisplayMode.CLASSIC;
        nestedThread.set({
            parent: this,
            aggregation: aggregation,
            name: aggregation.get('name'),
            displayMode: displayMode,
            query: this.createNestedThreadQuery(aggregation)
        });
        return nestedThread;
    };
    // Ignored: Warning 'getTranslated' does not exist on type Thread
    Thread.prototype.getTranslated = function () {
        var res = Thread.prototype.get.apply(this, arguments);
        if (_.isString(res)) {
            return this.get('translator').translate(res);
        }
        return res;
    };
    var decorate = function () {
    };
    return decorate;
});
//# sourceMappingURL=_thread_decoration.js.map