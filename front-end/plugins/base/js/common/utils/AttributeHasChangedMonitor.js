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
define(["require", "exports", 'lodash'], function (require, exports, _) {
    var dt = 300;
    var timeShown = 10000;
    var AttributeHasChangedMonitor = (function () {
        function AttributeHasChangedMonitor(model) {
            this.model = model;
            this.model.on('change', this._addAttribute, this);
            this.attributesRecentlyChanged = {};
        }
        AttributeHasChangedMonitor.prototype._addAttribute = function () {
            var _this = this;
            var newAttributes = this.model.getDisplayablePropertiesThatHasChanged();
            _.forEach(newAttributes, function (attribute) {
                // Time in ms.
                _this.attributesRecentlyChanged[attribute] = timeShown;
            });
            this._triggerShortly();
        };
        AttributeHasChangedMonitor.prototype._triggerShortly = function () {
            if (!this.timeoutId) {
                this.timeoutId = setTimeout(_.bind(this._refreshList, this), dt);
            }
        };
        AttributeHasChangedMonitor.prototype._refreshList = function () {
            var newsAttributes = _.keys(_.omit(this.attributesRecentlyChanged, function (attribute) { return attribute !== timeShown; }));
            var oldAttributes = _.keys(_.omit(this.attributesRecentlyChanged, function (attribute) { return attribute > 0; }));
            this.attributesRecentlyChanged = _.omit(this.attributesRecentlyChanged, function (attribute) { return attribute < 0; });
            this.attributesRecentlyChanged = _.mapValues(this.attributesRecentlyChanged, function (attribute) { return attribute - dt; });
            this.timeoutId = undefined;
            if (!_.isEmpty(this.attributesRecentlyChanged)) {
                this._triggerShortly();
            }
            if (newsAttributes.length > 0 || oldAttributes.length > 0) {
                this.model.trigger('attributesHaveChanged', { newAttributes: newsAttributes, notNewAnymoreAttributes: oldAttributes });
            }
        };
        return AttributeHasChangedMonitor;
    })();
    return AttributeHasChangedMonitor;
});
//# sourceMappingURL=AttributeHasChangedMonitor.js.map